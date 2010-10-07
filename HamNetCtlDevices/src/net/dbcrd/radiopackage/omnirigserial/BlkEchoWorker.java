package net.dbcrd.radiopackage.omnirigserial;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * A worker class, that if registered with the BlkQServer will reflect the received packet
 * 
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
 final class BlkEchoWorker extends BlkQWorker {

    private static final Logger THE_LOGGER = Logger.getLogger(BlkEchoWorker.class.getName());
    private final BlockingQueue<PktInfo> cmdsIn = new LinkedBlockingQueue<PktInfo>();
    private boolean keepRunning = true;
    private Thread thisThread;

    /**
     * receives the packet from the blocking queue and echos the payload back in a response packet
     *
     */
    @Override
    public void run() {

        while (keepRunning) {
            try {
                final PktInfo pkt = cmdsIn.take();
                final PktInfo echoPkt = new PktInfo(pkt, pkt.payload);
                echoPkt.getHandler().handleResponse(echoPkt);
            } catch (InterruptedException ex) {
                if (!keepRunning) {
                    break;
                }
            }
        }
    }

    /**
     * places work on the blocking queue.  The work is lost if the work queue is full.
     * @param pkt a PktInfo that has the payload

     */
    
    @Override
     void doWork(final PktInfo pkt) {

        if (!cmdsIn.offer(pkt)) {
            THE_LOGGER.severe("Work queue is full............");
        }
    }

    /**
     * stops the worker
     */
    
    @Override
     void stop() {
        keepRunning = false;
        if (null != thisThread) {
            thisThread.interrupt();
        }
    }

    /**
     *
     * @return the Thread used by the worker
     */
    
    @Override
     Thread getWorkerThread() {
        return thisThread;
    }

    /**
     * Activates the worker
     * startes the BlkEchoWorker thread
     * @return a boolean true always
     */
    
    @Override
     boolean activate() {
        thisThread = new Thread(this);
        thisThread.setName("BlkEchoWorker");
        thisThread.start();
        return true;
    }
/**
 *
 * @param pkts a PktInfo[] that contains work that will be sequentually queued
 */
    
    @Override
     void doWork(final PktInfo[] pkts) {
        for (PktInfo pkt : pkts) {
            doWork(pkt);
        }
    }

    /**
     * 
     * @param pkts a List<PktInfo>  that contains work that will be sequentually queued
     */
    
    @Override
     void doWork(final List<PktInfo> pkts) {
        for (PktInfo pkt : pkts) {
            doWork(pkt);
        }
    }

    /**
     *
     * @param pkts a Queue<PktInfo> that contains work what will be sequentually queued.
     */
    
    @Override
     void doWork(final Queue<PktInfo> pkts) {
        for (PktInfo pkt : pkts) {
            doWork(pkt);
        }
    }

 
}
