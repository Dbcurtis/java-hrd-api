package net.dbcrd.radiopackage.omnirigserial;

import net.dbcrd.radiopackage.FlowCtlSel;
import net.dbcrd.radiopackage.CommSetup;
import net.dbcrd.radiopackage.Mode;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Queue;
import java.util.TooManyListenersException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Supports serial communicatinos to a radio that is defined by an OmniRig .ini file
 *
 *
 * <link>http://rxtx.qbang.org/wiki/index.php/Main_Page </link><br />
 *  <link>http://www.rxtx.org/ </link><br />
 * http://www.rxtx.org/
 *
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
 class BlkCATWorker extends BlkQWorker {

    final static long ONE_SEC=1000L;
    final static String HNC="BLKCAT-";
    private static final Logger THE_LOGGER=Logger.getLogger(BlkCATWorker.class.getName());
    /** 
     * a BlockingQueue<<byte[]> for commands to be sent to the radio
     */
    private final LinkedBlockingQueue<PktInfo> pkts=new LinkedBlockingQueue<PktInfo>();
  
    private volatile boolean keepRunning=true;
    /**
     *
     */
    SerialPort serialPort=null;
    /**
     * data from the radio is received by the InputStream
     */
    InputStream fromRadio=null;
    /**
     * data to be sent to the radio is passed as an output stream
     */
    OutputStream toRadio=null;
    /** 
     * a boolean that indicates whether the requested port valid
     */
    boolean portExists=false;
    /** 
     * a boolean that indicates whether the requsted radio is connected to the port
     */
    boolean isConnected=false;
    /**
     * a Mode that indicates the radio's current mode
     */
    Mode currentMode=Mode.UNSPECIFIED;
    /**
     *
     */
    final CommSetup commSetup;
    Thread myThread;
    PktInfo currentPkt=null;

    /**
     *
     * 
     * @param commSetup 
     */
    BlkCATWorker(final CommSetup commSetup) {
        super();
        this.commSetup=commSetup;
    }


    /**
     *  Thread that pulls work from the blocking queue, sends the data to the serial out
     */
    @Override
    public void run() {
        long lastWriteTime=System.currentTimeMillis();

        while(keepRunning){
            try{
                final long thisTimeWrite=System.currentTimeMillis();
                if(thisTimeWrite-lastWriteTime<50){
                    Thread.sleep(50);
                }
                currentPkt=pkts.take();
                final BlkServerCATCmd payload=(BlkServerCATCmd) currentPkt.payload;
                final byte[] radioCmd=payload.getCmdObj();
 
                lastWriteTime=thisTimeWrite;
                toRadio.write(radioCmd);

               int cntdwn=10;

                while(currentPkt!=null){
                    if(cntdwn--<0){
                        currentPkt=null;
                    }
                    Thread.sleep(100);
                }
            } catch(IOException ex){
                THE_LOGGER.log(Level.SEVERE, null, ex);
            } catch(InterruptedException ex){
                if(!keepRunning){
                    break;
                }
            }
        }
    }

    /**
     *
     * @param pkt
     */
    
     void doWork(final PktInfo pkt) {
         if(!pkts.offer(pkt)){
            THE_LOGGER.severe("cmd que overflow");
        }    
    }

    /**
     * Called to activate the CAT associated with the Physical radio.
     * It makes sure that the specified communication port is available. 
     * @return
     */
    
     boolean activate() {
        if(myThread!=null&&myThread.isAlive()){
            return false;
        }

        currentPkt=null;
        pkts.clear();
        currentMode=Mode.UNSPECIFIED;
        portExists=false;
        isConnected=false;
        fromRadio=null;
        toRadio=null;
        SerialReader readerListener=null;
        try{
            final CommPortIdentifier reqPid=CommPortIdentifier.getPortIdentifier(commSetup.getSerialPortStr());
            if(null!=serialPort){
                CommPortIdentifier curPid=null;

                try{
                    curPid=CommPortIdentifier.getPortIdentifier((CommPort) serialPort);
                } catch(NoSuchPortException nspe){
                    int i=0;
                    i++; //TODO
                }
                if(curPid==null||curPid.equals(reqPid)){
                    releasePort();
                }
            }
            boolean portavailable=true;
            if(CommPortIdentifier.PORT_SERIAL==reqPid.getPortType()){
                try{
                    final CommPort thePort=reqPid.open(HNC+commSetup.getSerialPortStr(), 50);
                    thePort.close();
                } catch(PortInUseException piue){
                    portavailable=false;
                    THE_LOGGER.fine(new StringBuilder().append("Port, ").append(reqPid.getName()).
                            append(", is in use.").toString());
                } catch(Exception e){
                    portavailable=false;
                    THE_LOGGER.log(Level.WARNING, new StringBuilder().append("Failed to open port ").append(reqPid.
                            getName()).toString(), e);
                }
            }
            if(!portavailable){
                final String spname=(serialPort==null)?reqPid.getName():serialPort.getName().toString();
                JOptionPane.showMessageDialog(null,
                                              new StringBuilder().append("Port ").append(spname).append(
                        " in use, No Connection").toString(), "Port Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            serialPort=(SerialPort) reqPid.open(HNC+commSetup.getSerialPortStr(), 100);
          //  final CommPortIdentifier curPid=CommPortIdentifier.getPortIdentifier((CommPort) serialPort);
            serialPort.setSerialPortParams(
                    Integer.parseInt(commSetup.getBaud()),
                    commSetup.getDataBits().getSerialPortVal(),//SerialPort.DATABITS_8,
                    commSetup.getStopBits().getSerialPortVal(),
                    commSetup.getParity().getSerialPortVal());


            if (commSetup.getFlowctl() == FlowCtlSel.NONE) {
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            }
            else if(commSetup.getFlowctl() == FlowCtlSel.CTS_RTS) {
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT);

            } else if (commSetup.getFlowctl() == FlowCtlSel.XON_OFF) {
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_OUT|SerialPort.FLOWCONTROL_XONXOFF_IN);
            }

            fromRadio=serialPort.getInputStream();
            toRadio=serialPort.getOutputStream();
            readerListener=new SerialReader(fromRadio);
            serialPort.addEventListener(readerListener);
            portExists=true;
            serialPort.notifyOnDataAvailable(true);
            isConnected=true;

     //       final CommPortIdentifier curPida=CommPortIdentifier.getPortIdentifier((CommPort) serialPort);

        } catch(NoSuchPortException nspe){
            JOptionPane.showMessageDialog(null, new StringBuilder("Cannot connect to port ").append(serialPort.getName()).
                    toString(), "No Connection", JOptionPane.ERROR_MESSAGE);

        } catch(PortInUseException piue){
            THE_LOGGER.log(Level.SEVERE, null, piue);
        } catch(UnsupportedCommOperationException ucop){
            JOptionPane.showMessageDialog(null,
                                          new StringBuilder("Comm port ").append(ucop.getMessage()).toString(),
                                          "Illegal Option", JOptionPane.ERROR_MESSAGE);
            THE_LOGGER.info("Invalid Communication parameter");
        } catch(IOException ioe){
            THE_LOGGER.log(Level.SEVERE, null, ioe);
        } catch(TooManyListenersException tmle){
            THE_LOGGER.log(Level.SEVERE, null, tmle);
        }
        keepRunning=true;
        myThread=new Thread(this);
        myThread.setName(HNC+commSetup.getSerialPortStr());
        myThread.setDaemon(true);
        myThread.start();
        int cntdwn=10;
        while(!myThread.isAlive()&&cntdwn-->0){
            try{
                Thread.sleep(100);
            } catch(InterruptedException ex){
                return false;
            }
        }
        return cntdwn>0;
    }

    private void releasePort() {
        portExists=false;
        isConnected=false;

        try{
            if(null!=toRadio){
                toRadio.close();
            }
        } catch(IOException ioe){
        }
        try{
            if(null!=fromRadio){
                fromRadio.close();
            }
        } catch(IOException ioe){
        }
        //  serialPort.removeEventListener(); done automatically at port close.
        if(null!=serialPort){
            serialPort.close();
        }
        serialPort=null;
    }

    private void releaseme(){
        this.notify();  //TODO fix this
    }
    /**
     *
     */
    
     void stop() {
        keepRunning=false;
        if(null!=myThread&&myThread.isAlive()){
            myThread.interrupt();
        }
        releasePort();
    }

    
     Thread getWorkerThread() {
        return myThread;
    }

    
     void doWork(final PktInfo[] pkts) {
        for (PktInfo pkt:pkts){
            doWork(pkt);
        }
    }

    
     void doWork(final List<PktInfo> pkts) {
          for (PktInfo pkt:pkts){
            doWork(pkt);
        }
    }

    
     void doWork(final Queue<PktInfo> pkts) {
         for (PktInfo pkt:pkts){
            doWork(pkt);
        }
    }

    /**
     * 
     */
    class SerialReader implements SerialPortEventListener {

        private final InputStream fromRadio;
        private final byte[] buffer=new byte[1024];

        /**
         *
         * @param instream
         */
        public SerialReader(final InputStream instream) {
            this.fromRadio=instream;

        }

        /**
         *
         * @param arg0 see <link>http://java.sun.com/products/javacomm/reference/api/javax/comm/SerialPortEvent.html</link>
         */
        @Override
        public void serialEvent(final SerialPortEvent arg0) {
            if(SerialPortEvent.DATA_AVAILABLE!=arg0.getEventType()){
                return;
            }

            try{
                int len=0;
                try{
                    Thread.sleep(100);
                } catch(InterruptedException ex){
                }

                len = fromRadio.read(buffer);

                PktInfo echoPkt;
                if(len!=0){
                    final byte[] dataBurst=new byte[len];
                    System.arraycopy(buffer, 0, dataBurst, 0, len);
                    echoPkt=new PktInfo(currentPkt, dataBurst);
                } else{
                    echoPkt=new PktInfo(currentPkt, new byte[]{0});
                }
                echoPkt.getHandler().handleResponse(echoPkt);
                currentPkt=null;
            } catch(IOException ioe){
                THE_LOGGER.log(Level.SEVERE, "IOException", ioe);
            }
        }
    }
}
