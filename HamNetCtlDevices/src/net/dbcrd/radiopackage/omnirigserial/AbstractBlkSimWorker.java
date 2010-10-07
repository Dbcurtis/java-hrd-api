
package net.dbcrd.radiopackage.omnirigserial;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Abstract BlkSimWorker
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
 abstract class AbstractBlkSimWorker extends BlkQWorker {

    private final BlockingQueue<PktInfo> cmdsIn=new LinkedBlockingQueue<PktInfo>();
    private boolean keepRunning=true;
    private Thread thisThread;

   
    @Override
    abstract  void doWork(final PktInfo pkt);

    
    @Override
     void doWork(final PktInfo[] pkts) {
        for(PktInfo pkt:pkts){
            doWork(pkt);
        }
    }

    
    @Override
     void doWork(final List<PktInfo> pkts) {
        for(PktInfo pkt:pkts){
            doWork(pkt);
        }
    }

    
    @Override
     void doWork(final Queue<PktInfo> pkts) {
        for(PktInfo pkt:pkts){
            doWork(pkt);
        }
    }

    /**
     *
     */
    
    @Override
     void stop() {
        keepRunning=false;
        if(null!=thisThread){
            thisThread.interrupt();
        }
    }

    /**
     *
     * @return a Thread used by the worker
     */
    
    @Override
     Thread getWorkerThread() {
        return thisThread;
    }

    /**
     * Activates the worker
     * startes the AbstractBlkSimWorker thread
     * @return a boolean true always
     */
    
    @Override
     boolean activate() {
        thisThread=new Thread(this);
        thisThread.setName("BlkSimWorker");
        thisThread.start();
        return true;
    }

    abstract Object doSimRadio(Object payload);

    @Override
    public void run() {

        while(keepRunning){
            try{
                final PktInfo pkt=cmdsIn.take();
                final PktInfo echoPkt=new PktInfo(pkt, doSimRadio(pkt.payload));
                echoPkt.getHandler().handleResponse(echoPkt);
            } catch(InterruptedException ex){
                if(!keepRunning){
                    break;
                }
            }
        }
    }
}
