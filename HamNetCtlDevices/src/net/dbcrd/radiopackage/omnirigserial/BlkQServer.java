package net.dbcrd.radiopackage.omnirigserial;

import net.dbcrd.radiopackage.CommSetup;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A server class to receive commands from a BlkQ for distribution to radios connected by serial links.
 * This is a singleton class
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
final class BlkQServer implements Runnable {

    private static final Logger THE_LOGGER = Logger.getLogger(BlkQServer.class.getName());
    private volatile boolean keepRunning = true;
    private final BlockingQueue<PktInfo> source;
    private final static String ECHO = "ECHO";
    private final static String SIM="SIM";
    private static Thread myRunThread;
    private static BlkQServer instance = null;
    private final Map<String, BlkQWorker> portId2Cat = new ConcurrentHashMap<String, BlkQWorker>();
    private final Map<String, Thread> com2worker = new ConcurrentHashMap<String, Thread>();
    private final BlockingQueue<PktInfo> client2serverQ = new LinkedBlockingQueue<PktInfo>();

    /**
     * The sintleton instantiator
      */
    private BlkQServer() {
        this.source = client2serverQ;
    }

   /**
    *
    * @return an Iterator of a sorted set of String that identifes the registred workers
    */
    Iterator<String> getRegistered() {
        while (!source.isEmpty() || processing.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                break;
            }
        }
        return new ConcurrentSkipListSet<String>(portId2Cat.keySet()).iterator();
    }

    /**
     * Get the singleton instance of the server
     * @return the BlkQserver Instance
     */
    static synchronized BlkQServer getInstance() {
        if (null == instance) {
            instance = new BlkQServer();
            if (! instance.activate()){
                THE_LOGGER.severe("Server did not start");
                return null;
            }
        }
        return instance;
    }

    /**
     * Get the Client-to-Server blocking queue that receives the work to be distributed to the workers
     * @return a BlockingQueue<PktInfo> on which work can be submitted
     */
  BlockingQueue<PktInfo> getClient2ServerQ() {
        return client2serverQ;
    }
    /**
     *   an AtomicBoolean to indicate whether run is active
     */
    AtomicBoolean processing = new AtomicBoolean();

    public int getNumRadios() {
      return portId2Cat.size();
    }

    /**
     *  if keepRunning is false, will end the thread.
     * if processing is false, Run is waiting for the blocking queue
     */
    @Override
    public void run() {

        while (keepRunning) {
            try {
                processing.set(false);
                final PktInfo pkt=source.take();
                processing.set(true);
                if (pkt.payload instanceof BlkServerCATCmd) {
                    //      final BlkServerCATCmd payload=(BlkServerCATCmd) pkt.payload;
                    final String comm=pkt.portId;
                    if (portId2Cat.containsKey(comm)) {
                        portId2Cat.get(comm).doWork(pkt);
                    } else {
                        //unknown port... fake response
                        final PktInfo echoPkt=new PktInfo(pkt, "Illegal communication port specified".getBytes());
                        echoPkt.getHandler().handleResponse(echoPkt);
                        THE_LOGGER.severe("Illegal communication port specified");
                    }
                } else if (pkt.payload instanceof BlkServerCmd) {

                    final BlkServerCmd cmd = (BlkServerCmd) pkt.payload;
                    switch (cmd.getCmd()) {

                        case REGISTER_PORT: //register a serial port for a worker
                            if (cmd.getCmdObj() instanceof CommSetup) {
                                register(cmd);
                            } else {
                                THE_LOGGER.severe("Illegal register cmd");
                            }
                            break;

                        case DEREGISTER_PORT:  // deregister the port and release the worker
                            if (cmd.getCmdObj() instanceof String) {
                                deregister(cmd);
                            } else {
                                THE_LOGGER.severe("Illegal deregister cmd");
                            }
                            break;

                        case REGISTER_SIM:  //  register a radio simulator worker
                             if (portId2Cat.containsKey(SIM)) {
                                THE_LOGGER.warning("Multiple attempts to instanciate SIM worker");
                            } else {
                                if (cmd.getCmdObj() instanceof AbstractBlkSimWorker) {
                                    final AbstractBlkSimWorker simWorker =(AbstractBlkSimWorker) cmd.getCmdObj();
                                }
                            }
                            break;

                        case REGISTER_ECHO:  //register an echo worker
                            if (portId2Cat.containsKey(ECHO)) {
                                THE_LOGGER.warning("Multiple attempts to instanciate ECHO worker");
                            } else {
                                if (cmd.getCmdObj() instanceof String) {
                                    if (!(ECHO.equals((String) cmd.getCmdObj()))) {
                                        THE_LOGGER.severe("EchoWorker register error");
                                    }
                                    final BlkEchoWorker echoWorker = new BlkEchoWorker();
                                    if (!echoWorker.activate()) {
                                        THE_LOGGER.severe("EchoWorker did not activate");
                                    }
                                    portId2Cat.put(ECHO, echoWorker);
                                    com2worker.put(ECHO, echoWorker.getWorkerThread());
                                }
                            }
                            break;

                        default:
                            THE_LOGGER.severe("Illegal BlkServerCmd received");
                            break;
                    }
                }

            } catch (InterruptedException ex) {
                if (!keepRunning) {
                    break;
                }
            }
        }
    }

    /**
     * Deregister a  worker.  Stop the worker thread, release the serial port
     * @param cmd
     * @return a boolean, true if worker has been deregistered; false otherwise
     */
    private boolean deregister(final BlkServerCmd cmd) {
        boolean result = false;

        final String serialPortStr = (String) cmd.getCmdObj();
        final BlkQWorker serialWorker = portId2Cat.get(serialPortStr);
        final Thread workerThread = com2worker.get(serialPortStr);

        if (workerThread.isAlive()) {
            serialWorker.stop();
            workerThread.interrupt();
            result = true;
        } else {
            THE_LOGGER.severe("worker thread not running");
        }
        portId2Cat.remove(serialPortStr);
        com2worker.remove(serialPortStr);

        return result;
    }

    /**
     * Register a BlkQWorker with the server.
     * @param cmd a BlkServerCmd containing the registration information
     * @return a boolean, true if worker has been registered; false otherwise
     */
    private boolean register(final BlkServerCmd cmd) {
        boolean cmdOK = false;
        CommSetup commSetup = null;
        BlkQWorker serialWorker = null;
        Thread workerThread = null;
        try {
            commSetup = (CommSetup) cmd.getCmdObj();
            serialWorker = new BlkCATWorker(commSetup);
            if (!serialWorker.activate()) {
                THE_LOGGER.severe("Worker did not activate");
            }
            workerThread = serialWorker.getWorkerThread();
            if (null == workerThread) {
                THE_LOGGER.severe("Worker did not run");
            }
            cmdOK = true;

        } finally {
            if (cmdOK) {
                portId2Cat.put(commSetup.getSerialPortStr(), serialWorker);
                com2worker.put(commSetup.getSerialPortStr(), workerThread);
            }
        }
        return cmdOK;
    }

    /**
     * Stops all the workers, and clears the blocking queues
     */
    boolean stop() {
        keepRunning = false;
        for (Map.Entry<String, BlkQWorker> ent : portId2Cat.entrySet()) {
            ent.getValue().stop();
            com2worker.get(ent.getKey()).interrupt();
        }
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            THE_LOGGER.log(Level.SEVERE, null, ex);
            return false;
        }
        com2worker.clear();
        portId2Cat.clear();
        myRunThread.interrupt();
        int cntdwn=50;
        while (myRunThread.isAlive() && cntdwn-- > 0) {
            try {
                Thread.sleep(10);
                com2worker.clear();
                portId2Cat.clear();
            } catch (InterruptedException ex) {
                THE_LOGGER.log(Level.SEVERE, null, ex);

            }
        }
        return !myRunThread.isAlive();
    }

    /**
     * Clears the blocking queues, starts the service thread (BlkQServer), waits for the thread to start before it returns
     * Attempts to stop any workers currently running
     * @return a boolean true if the thread is not running,false if it is running
     */
    boolean activate() {
        if (null != myRunThread && myRunThread.isAlive()) {
            return false;
        }
        for (Map.Entry<String, BlkQWorker> ent : portId2Cat.entrySet()) {
            ent.getValue().stop();
            com2worker.get(ent.getKey()).interrupt();
        }
        com2worker.clear();
        portId2Cat.clear();
        keepRunning = true;
        myRunThread = new Thread(instance);
        myRunThread.setDaemon(true);
        myRunThread.setName("BlkQServer");
        myRunThread.start();
        int cntdwn = 20;
        // wit up to 2 sec for the thread to become alive
        while (!myRunThread.isAlive()) {
            try {
                if (cntdwn-- < 0) {
                    break;
                }
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                break;
            }
        }
        if (cntdwn < 0) {
            THE_LOGGER.severe("BlkQServer did not start");
            return false;
        }

        return true;
    }

    /**
     *
     * @return a Thread that is the thread running the instance
     */Thread getServerThread() {
        return myRunThread;
    }


}
