/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.dbcrd.radiopackage.omnirigserial;

import net.dbcrd.radiopackage.CommSetup;
import org.junit.Ignore;
import java.util.List;
import java.util.Queue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dbcurtis
 */
public class BlkQWorkerTest {

    /**
     *
     */
    public BlkQWorkerTest() {
    }
    static CommSetup comm=null;

    /**
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        CommSetup commtemp=CommSetup4Test.getComm();
        if (null == commtemp) {
            CommSetup4Test.setComm();
        }
        comm=CommSetup4Test.getComm();
        System.out.println("tested by other tests");
    }

    /**
     *
     * @throws Exception
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     *
     */
    @Before
    public void setUp() {
    }

    /**
     *
     */
    @After
    public void tearDown() {
    }

    public void testA() {
        System.out.println("testA --- just needs to compile");
    }

    /**
     * Test of doWork method, of class BlkQWorker.
     */
    @Test
    @Ignore
    public void testDoWork_PktInfo() {
        System.out.println("doWork");
        PktInfo pkt=null;
        BlkQWorker instance=new BlkQWorkerImpl();
        instance.doWork(pkt);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of doWork method, of class BlkQWorker.
     */
    @Test
    @Ignore
    public void testDoWork_PktInfoArr() {
        System.out.println("doWork");
        PktInfo[] pkts=null;
        BlkQWorker instance=new BlkQWorkerImpl();
        instance.doWork(pkts);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of doWork method, of class BlkQWorker.
     */
    @Test
    @Ignore
    public void testDoWork_List() {
        System.out.println("doWork");
        List<PktInfo> pkts=null;
        BlkQWorker instance=new BlkQWorkerImpl();
        instance.doWork(pkts);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of doWork method, of class BlkQWorker.
     */
    @Test
    @Ignore
    public void testDoWork_Queue() {
        System.out.println("doWork");
        Queue<PktInfo> pkts=null;
        BlkQWorker instance=new BlkQWorkerImpl();
        instance.doWork(pkts);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stop method, of class BlkQWorker.
     */
    @Test
    @Ignore
    public void testStopMe() {
        System.out.println("stopMe");
        BlkQWorker instance=new BlkQWorkerImpl();
        instance.stop();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getWorkerThread method, of class BlkQWorker.
     */
    @Test
    @Ignore
    public void testGetWorkerThread() {
        System.out.println("getWorkerThread");
        BlkQWorker instance=new BlkQWorkerImpl();
        Thread expResult=null;
        Thread result=instance.getWorkerThread();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of activate method, of class BlkQWorker.
     */
    @Test
    @Ignore
    public void testActivate() {
        System.out.println("activate");
        BlkQWorker instance=new BlkQWorkerImpl();
        boolean expResult=false;
        boolean result=instance.activate();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     *
     */
    public class BlkQWorkerImpl extends BlkQWorker {

        public void doWork(PktInfo pkt) {
        }

        public void doWork(PktInfo[] pkts) {
        }

        public void doWork(List<PktInfo> pkts) {
        }

        public void doWork(Queue<PktInfo> pkts) {
        }

        public void stop() {
        }

        public Thread getWorkerThread() {
            return null;
        }

        public boolean activate() {
            return false;
        }

        public void run() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
