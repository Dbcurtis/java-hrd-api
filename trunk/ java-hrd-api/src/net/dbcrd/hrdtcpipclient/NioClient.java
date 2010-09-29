
package net.dbcrd.hrdtcpipclient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class to handle TCP/IP communications beween a client and a server. Run as a seperate thread.
 * If AbstractRadio.abortClientThread becomes true, the thread shuts down.
 *
 * see: <link> http://rox-xmlrpc.sourceforge.net/niotut/</link>
 * some modificaitons by Dan Curtis to use generics
 */
 class NioClient implements Runnable {
    private static final Logger THE_LOGGER=Logger.getLogger(NioClient.class.getName());
    // The host:port combination to connect to
    final private InetAddress hostAddress;
    final private int port;
    // The selector we'll be monitoring
    final private Selector selector;
    // The buffer into which we'll read data when it's available
    final private ByteBuffer readBuffer=ByteBuffer.allocate(8192);
    // A list of PendingChange instances
    private final List<ChangeRequest> pendingChanges=new LinkedList<ChangeRequest>();
    // Maps a SocketChannel to a list of ByteBuffer instances
    private final Map<SocketChannel, List<ByteBuffer>> pendingData=new HashMap<SocketChannel, List<ByteBuffer>>();
    // Maps a SocketChannel to a RspHandler
    private final Map<SocketChannel, RspHandler> rspHandlers=Collections.synchronizedMap(
            new HashMap<SocketChannel, RspHandler>());

    private boolean active = false;

    /**
     * A new IO socket implememtation to control TCP/IP communications between a client and server.
     * @param hostAddress a InetAddress that specifies the host to connect to
     * @param port an int that specifies the port on the host.
     * @throws IOException
     */
     NioClient(final InetAddress hostAddress, final int port) throws IOException {
        this.hostAddress=hostAddress;
        this.port=port;
        this.selector=this.initSelector();
    }
/**
 * Indicates if the communication thread has been started.
 * @return a boolean; true if started.
 */
    public boolean isActive(){
        return active;
    }
    /**
     * Thread operations to handle the TCP/IP client-server communications. if <tt>AbstractRadio.abortClientThread</tt>
     * is true, this thread will terminate.
     */
    public void run() {
        active = true;
        while(true){
            try{
                // Process any pending changes
                synchronized (this.pendingChanges){
                    final Iterator<ChangeRequest> changes=this.pendingChanges.iterator();
                    while(changes.hasNext()){
                        final ChangeRequest change= changes.next();
                        switch(change.type){
                            case ChangeRequest.CHANGEOPS:
                                final SelectionKey key=change.socket.keyFor(this.selector);
                                key.interestOps(change.ops);
                                break;
                            case ChangeRequest.REGISTER:
                                change.socket.register(this.selector, change.ops);
                                break;
                        }
                    }
                    this.pendingChanges.clear();
                }

                // Wait for an event one of the registered channels
                this.selector.select();

                // Iterate over the set of keys for which events are available
                final Iterator<SelectionKey> selectedKeys=this.selector.selectedKeys().iterator();
                while(selectedKeys.hasNext()){
                    final SelectionKey key= selectedKeys.next();
                    selectedKeys.remove();

                    if(!key.isValid()){
                        continue;
                    }

                    // Check what event is available and deal with it
                    if(key.isConnectable()){
                        this.finishConnection(key);
                    } else if(key.isReadable()){
                        this.read(key);
                    } else if(key.isWritable()){
                        this.write(key);
                    }
                }
            } catch(Exception e){
                THE_LOGGER.log(Level.SEVERE,null,e);
            }
            if(AbstractRadio.abortClientThread){
                active=false;
                break;
            }
        }
        THE_LOGGER.info("NioClient thread ending.");
    }
  /**
     * Queue data to be sent to the server.
     * @param data a byte[] of data to be sent to the server
     * @param handler a RspHandler to receive response from the server
     * @throws IOException
     */
     void send(final byte[] data, final RspHandler handler) throws IOException {
        // Start a new connection
        final SocketChannel socket=this.initiateConnection();

        // Register the response handler
        this.rspHandlers.put(socket, handler);

        // And queue the data we want written
        synchronized (this.pendingData){
            List<ByteBuffer> queue=this.pendingData.get(socket);
            if(queue==null){
                queue=new ArrayList<ByteBuffer>();
                this.pendingData.put(socket, queue);
            }
            queue.add(ByteBuffer.wrap(data));
        }

        // Finally, wake up our selecting thread so it can make the required changes
        this.selector.wakeup();
    }

    /**
     *
     * @param key
     * @throws IOException
     */
    private void read(final SelectionKey key) throws IOException {
        final SocketChannel socketChannel=(SocketChannel) key.channel();

        // Clear out our read buffer so it's ready for new data
        this.readBuffer.clear();

        // Attempt to read off the channel
        int numRead;
        try{
            numRead=socketChannel.read(this.readBuffer);
        } catch(IOException e){
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            key.cancel();
            socketChannel.close();
            return;
        }

        if(numRead==-1){
            // Remote entity shut the socket down cleanly. Do the
            // same from our end and cancel the channel.
            key.channel().close();
            key.cancel();
            return;
        }

        // Handle the response
        this.handleResponse(socketChannel, this.readBuffer.array(), numRead);
    }

  /**
   *
   * @param socketChannel
   * @param data
   * @param numRead
   * @throws IOException
   */
    private void handleResponse(final SocketChannel socketChannel, final byte[] data,final int numRead) throws IOException {
        // Make a correctly sized copy of the data before handing it
        // to the client
        final byte[] rspData=new byte[numRead];
        System.arraycopy(data, 0, rspData, 0, numRead);

        // Look up the handler for this channel
        final RspHandler handler=this.rspHandlers.get(socketChannel);

        // And pass the response to it
        if(handler.handleResponse(rspData)){
            // The handler has seen enough, close the connection
            socketChannel.close();
            socketChannel.keyFor(this.selector).cancel();
        }
    }

    /**
     *
     * @param key
     * @throws IOException
     */
    private void write(final SelectionKey key) throws IOException {
        final SocketChannel socketChannel=(SocketChannel) key.channel();

        synchronized (this.pendingData){
           final List queue=(List) this.pendingData.get(socketChannel);

            // Write until there's not more data ...
            while(!queue.isEmpty()){
               final ByteBuffer buf=(ByteBuffer) queue.get(0);
                socketChannel.write(buf);
                if(buf.remaining()>0){
                    // ... or the socket's buffer fills up
                    break;
                }
                queue.remove(0);
            }

            if(queue.isEmpty()){
                // We wrote away all data, so we're no longer interested
                // in writing on this socket. Switch back to waiting for
                // data.
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

   /**
    * 
    * @param key
    * @throws IOException
    */
    private void finishConnection(final SelectionKey key) throws IOException {
        final SocketChannel socketChannel=(SocketChannel) key.channel();

        // Finish the connection. If the connection operation failed
        // this will raise an IOException.
        try{
            socketChannel.finishConnect();
        } catch(IOException ioe){
            // Cancel the channel's registration with our selector
            THE_LOGGER.log(Level.SEVERE, null, ioe);
            key.cancel();
            return;
        }

        // Register an interest in writing on this channel
        key.interestOps(SelectionKey.OP_WRITE);
    }

  /**
   *
   * @return
   * @throws IOException
   */
    private SocketChannel initiateConnection() throws IOException {
        // Create a non-blocking socket channel
        final SocketChannel socketChannel=SocketChannel.open();
        socketChannel.configureBlocking(false);

        // Kick off connection establishment
        socketChannel.connect(new InetSocketAddress(this.hostAddress, this.port));

        // Queue a channel registration since the caller is not the
        // selecting thread. As part of the registration we'll register
        // an interest in connection events. These are raised when a channel
        // is ready to complete connection establishment.
        synchronized (this.pendingChanges){
            this.pendingChanges.add(new ChangeRequest(socketChannel, ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
        }

        return socketChannel;
    }

   /**
    *
    * @return
    * @throws IOException
    */
    private Selector initSelector() throws IOException {
        // Create a new selector
        return SelectorProvider.provider().openSelector();
    }

//    /**
//     *
//     * @param args
//     */
//    public static void main(String[] args) {
//        try{
//            //  NioClient client=new NioClient(InetAddress.getByName("www.google.com"), 80);
//            NioClient client=new NioClient(InetAddress.getByName("localhost"), 7809);  //local ham radio deluxe
//            Thread t=new Thread(client);
//            t.setDaemon(true);
//            t.start();
//            RspHandler handler=new RspHandler();
//            client.send("Hello World".getBytes("UTF-16BE"), handler);
//            handler.waitForResponse();
//        } catch(Exception e){
//            e.printStackTrace();
//        }
//    }
}

