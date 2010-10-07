

package net.dbcrd.radiopackage.omnirigserial;

import java.util.List;
import java.util.Queue;

/**
 * Interface for a BlockingQueue worker
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
 abstract class BlkQWorker implements Runnable {

   /**
    * Provide work to the worker.
    * @param pkt a PktInfo containing  a payload to be processed
    */
    abstract void doWork(final PktInfo pkt);
    /**
     * Provide work to the worker.
     * @param pkts a PktInfo[] containing  an array  of  payloads to be processed
     */
    abstract void doWork(final PktInfo[] pkts);
    /**
     * Provide work to the worker.
     * @param pkts a List of PktInfo containing payloads to be processed
     */
    abstract  void doWork(final List<PktInfo> pkts);
    /**
     * Provide work to the worker.
     * @param pkts pkts a Queue of PktInf> containing   of  payloads to be processed
     */
    abstract  void doWork(final Queue<PktInfo> pkts);
    /**
     *  stop the worker
     */
    abstract  void stop();
    /**
     *  get the thead for the worker
     * @return A Thread of the worker
     */
    abstract  Thread getWorkerThread();
    /**
     *  Activate a worker thread.
     * @return  a boolean that is true if the worker Thread becomes active
     */
    abstract  boolean activate();
   
}
