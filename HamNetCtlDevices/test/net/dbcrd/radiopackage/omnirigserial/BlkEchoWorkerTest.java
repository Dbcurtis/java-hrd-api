/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.dbcrd.radiopackage.omnirigserial;

import net.dbcrd.radiopackage.CommSetup;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dbcurtis
 */
public class BlkEchoWorkerTest {

    /**
     *
     */
    public BlkEchoWorkerTest() {
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

    /**
     * Test of stop method, of class BlkEchoWorker.
     */
    @Test
    public void testStop() {
        System.out.println("stop");
        BlkEchoWorker instance=new BlkEchoWorker();
        Thread tempt=instance.getWorkerThread();
        assertNull(tempt);
        instance.stop();
        instance.activate();
        tempt=instance.getWorkerThread();
        assertTrue(null != tempt);
        instance.stop();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(BlkEchoWorkerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertFalse(tempt.isAlive());
    }

    /**
     * Test of run method, of class BlkEchoWorker.
     */
    @Test
    @Ignore
    public void testRun() {
        System.out.println("run");
        BlkEchoWorker instance=new BlkEchoWorker();
        instance.run();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getWorkerThread method, of class BlkEchoWorker.
     */
    @Test
    public void testGetWorkerThread() {
        System.out.println("getWorkerThread");
        BlkEchoWorker instance=new BlkEchoWorker();
        Thread expResult=null;
        Thread result=instance.getWorkerThread();
        assertEquals(expResult, result);
        instance.activate();
        assertTrue(null != instance.getWorkerThread());
        instance.stop();
    }

    /**
     * Test of activate method, of class BlkEchoWorker.
     */
    @Test
    public void testActivate() {
        System.out.println("activate");
        BlkEchoWorker instance=new BlkEchoWorker();
        Thread expResult=null;
        Thread result=instance.getWorkerThread();
        assertEquals(expResult, result);
        instance.activate();
        assertTrue(null != instance.getWorkerThread());
        instance.stop();
    }

    /**
     * Test of doWork method, of class BlkEchoWorker.
     */
    @Test
    public void testDoWork_PktInfoArr() {
        System.out.println("doWork array");
        BlkEchoWorker instance=new BlkEchoWorker();
        instance.activate();
        PktInfo[] pkts=new PktInfo[5];
        BlkQRspHandler[] hands=new BlkQRspHandler[5];
        for (int i=0; i < hands.length; i++) {
            hands[i]=new BlkQRspHandler();
        }

        pkts[0]=new PktInfo("ECHO", "stuff check0", hands[0]);
        pkts[1]=new PktInfo("ECHO", "stuff check1", hands[1]);
        pkts[2]=new PktInfo("ECHO", "stuff check2", hands[2]);
        pkts[3]=new PktInfo("ECHO", "stuff check3", hands[3]);
        pkts[4]=new PktInfo("ECHO", "stuff check4", hands[4]);

        try {
            instance.doWork(pkts);
            PktInfo respkt=hands[3].waitForResponse();
            assertEquals(((String) respkt.payload), ((String) pkts[3].payload));
            respkt=hands[0].waitForResponse();
            assertEquals(((String) respkt.payload), ((String) pkts[0].payload));
            respkt=hands[4].waitForResponse();
            assertEquals(((String) respkt.payload), ((String) pkts[4].payload));
            respkt=hands[1].waitForResponse();
            assertEquals(((String) respkt.payload), ((String) pkts[1].payload));
            respkt=hands[2].waitForResponse();
            assertEquals(((String) respkt.payload), ((String) pkts[2].payload));
        } catch (IOException ioe) {
            final JFrame window=null; // error dialog box centers on this window if present
            final String msg="Radio did not respond";
            JOptionPane.showMessageDialog(window, msg, ioe.getMessage(), JOptionPane.ERROR_MESSAGE);
        }

    }

    /**
     * Test of doWork method, of class BlkEchoWorker.
     */
    @Test
    public void testDoWork_List() {
        System.out.println("doWork list");
        BlkEchoWorker instance=new BlkEchoWorker();
        instance.activate();
        PktInfo[] pkts=new PktInfo[5];
        BlkQRspHandler[] hands=new BlkQRspHandler[5];
        for (int i=0; i < hands.length; i++) {
            hands[i]=new BlkQRspHandler();
        }

        pkts[0]=new PktInfo("ECHO", "stuff check0", hands[0]);
        pkts[1]=new PktInfo("ECHO", "stuff check1", hands[1]);
        pkts[2]=new PktInfo("ECHO", "stuff check2", hands[2]);
        pkts[3]=new PktInfo("ECHO", "stuff check3", hands[3]);
        pkts[4]=new PktInfo("ECHO", "stuff check4", hands[4]);

        List<PktInfo> lst=new LinkedList<PktInfo>();
        for (PktInfo pkt : pkts) {
            lst.add(pkt);
        }


        instance.doWork(lst);
        try {
            PktInfo respkt=hands[3].waitForResponse();
            assertEquals(((String) respkt.payload), ((String) pkts[3].payload));
            respkt=hands[0].waitForResponse();
            assertEquals(((String) respkt.payload), ((String) pkts[0].payload));
            respkt=hands[4].waitForResponse();
            assertEquals(((String) respkt.payload), ((String) pkts[4].payload));
            respkt=hands[1].waitForResponse();
            assertEquals(((String) respkt.payload), ((String) pkts[1].payload));
            respkt=hands[2].waitForResponse();
            assertEquals(((String) respkt.payload), ((String) pkts[2].payload));
        } catch (IOException ioe) {
            final JFrame window=null; // error dialog box centers on this window if present
            final String msg="Radio did not respond";
            JOptionPane.showMessageDialog(window, msg, ioe.getMessage(), JOptionPane.ERROR_MESSAGE);
        }

    }

    /**
     * Test of doWork method, of class BlkEchoWorker.
     */
    @Test
    public void testDoWork_Queue() {
        System.out.println("doWork Queue");
        BlkEchoWorker instance=new BlkEchoWorker();
        instance.activate();
        PktInfo[] pkts=new PktInfo[5];
        BlkQRspHandler[] hands=new BlkQRspHandler[5];
        for (int i=0; i < hands.length; i++) {
            hands[i]=new BlkQRspHandler();
        }

        pkts[0]=new PktInfo("ECHO", "stuff check0", hands[0]);
        pkts[1]=new PktInfo("ECHO", "stuff check1", hands[1]);
        pkts[2]=new PktInfo("ECHO", "stuff check2", hands[2]);
        pkts[3]=new PktInfo("ECHO", "stuff check3", hands[3]);
        pkts[4]=new PktInfo("ECHO", "stuff check4", hands[4]);

        Queue<PktInfo> lst=new LinkedList<PktInfo>();
        lst.addAll(Arrays.asList(pkts));


        instance.doWork(lst);
        try {
            PktInfo respkt=hands[3].waitForResponse();
            assertEquals(((String) respkt.payload), ((String) pkts[3].payload));
            respkt=hands[0].waitForResponse();
            assertEquals(((String) respkt.payload), ((String) pkts[0].payload));
            respkt=hands[4].waitForResponse();
            assertEquals(((String) respkt.payload), ((String) pkts[4].payload));
            respkt=hands[1].waitForResponse();
            assertEquals(((String) respkt.payload), ((String) pkts[1].payload));
            respkt=hands[2].waitForResponse();
            assertEquals(((String) respkt.payload), ((String) pkts[2].payload));
        } catch (IOException ioe) {
            final JFrame window=null; // error dialog box centers on this window if present
            final String msg="Radio did not respond";
            JOptionPane.showMessageDialog(window, msg, ioe.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Test of doWork method, of class BlkEchoWorker.
     */
    @Test
    public void testDoWork_PktInfo() {
        System.out.println("doWork Pkt");

        BlkEchoWorker instance=new BlkEchoWorker();
        instance.activate();
        Thread workerThread=instance.getWorkerThread();
        assertTrue(workerThread.isAlive());
        BlkQRspHandler hand=new BlkQRspHandler();
        PktInfo pkt=new PktInfo("ECHO", "stuff check", hand);

        instance.doWork(pkt);
        PktInfo respkt=null;
        try {
            respkt=hand.waitForResponse();
        } catch (IOException ioe) {
            final JFrame window=null; // error dialog box centers on this window if present
            final String msg="Radio did not respond";
            JOptionPane.showMessageDialog(window, msg, ioe.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
        assertTrue(pkt.getPktseqnum() == respkt.getPktSrcnum());
        assertEquals(((String) respkt.payload), ((String) pkt.payload));
        assertTrue(1 == respkt.getPktseqnum() - pkt.getPktseqnum());
        instance.stop();
    }
}
