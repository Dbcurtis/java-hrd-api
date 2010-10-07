
package net.dbcrd.radiopackage.omnirigserial;

import net.dbcrd.radiopackage.CommSetup;
import net.dbcrd.radiopackage.RadioControl;
import java.io.UnsupportedEncodingException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.io.IOException;
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
 * assumes a Yaesu ft-897 rig
 */
public class BlkCatWorkerTest {
  
    static RadioControl radio;
    static OmniRigMakeRadio radioMaker;
    static String radioid="";

    enum DataTst{
        OK,
        ERR,
        ERR1,
        TEST,
        INIT,
        LKON,
        LKOFF,
        SFREQ,
        RFREQ,
        SETM
    }
    static byte[] getExpected(final DataTst dataTst) {
        if ("FT-897".equals(radioid)) {
            switch (dataTst) {
                case OK:
                    return OK_R;
                case INIT:
                     return LOCK_OFF_CMD_R;
                case LKON:

                    return LOCK_ON_CMD_R;
                case LKOFF:
                    return LOCK_OFF_CMD_R;
                case SFREQ:
                    return SET_FREQ_CMD_R;
                case RFREQ:
                    return READ_FREQ_CMD_R;
                case SETM:
                    return SET_MODE_FM_CMD_R;

                case TEST:
                    return OK_R;
                case ERR:
                case ERR1:
                    return ERR_R;

                default:
                    return LOCK_ON_CMD_R;
            }
        }
        if ("Kenwood".equals(radioid)) {
            switch (dataTst) {
                case OK:
                    return K_NO_R;
                case TEST:
                    return K_TST_R;
                case INIT:
                case LKON:
                case ERR1:
                case LKOFF:                    
                case SFREQ:
                    return  K_NO_R;
                case RFREQ:
                    return  K_SET_FREQ_CMD;
                case SETM:
                    return K_NO_R;
                case ERR:
                    return K_TST_R;
                default:
                    return K_NO_R;
            }
        }
        return new byte[0];
    }
    static byte[] getTestData(final DataTst dataTst){
        if ("FT-897".equals(radioid)){
            switch (dataTst){
                case INIT:
                case TEST:
                case LKON:
                    return LOCK_ON_CMD;
                case LKOFF:
                    return LOCK_OFF_CMD;
                case SFREQ:
                    return SET_FREQ_CMD;
                case RFREQ:
                    return READ_FREQ_CMD;
                case SETM:
                    return SET_MODE_FM_CMD;
                default:
                    return LOCK_ON_CMD;
            }
        }
        if ("Kenwood".equals(radioid)) {
            switch (dataTst) {
                case TEST:
                    return K_TST;
                case INIT:
                    return K_INIT_ON;
                case LKON:
                    return K_LOCK_ON_CMD;
                case LKOFF:
                    return K_LOCK_OFF_CMD;
                case SFREQ:
                    return K_SET_FREQ_CMD;
                case RFREQ:
                    return K_READ_FREQ_CMD;
                case SETM:
                    return K_SET_MODE_FM_CMD;
                default:
                    return K_LOCK_ON_CMD;
            }
        }
        return new byte[0];
    }

    BlkCATWorker myInstance;
    // commands for FT-897 FT-857
    private static final byte[] LOCK_ON_CMD={0, 0, 0, 0, (byte) 0x00};
    private static final byte[] LOCK_ON_CMD_R={(byte) 0xF0};
    private static final byte[] LOCK_OFF_CMD={0, 0, 0, 0, (byte) 0x80};
    private static final byte[] LOCK_OFF_CMD_R={0};
    private static final byte[] SET_FREQ_CMD={(byte) 0x14, (byte) 0x52, (byte) 0x30, 0, (byte) 0x01};
    private static final byte[] SET_FREQ_CMD_R={};

    private static final byte[] READ_FREQ_CMD={0, 0, 0, 0, (byte) 0x03};
    private static final byte[] READ_FREQ_CMD_R={(byte) 0x14, (byte) 0x52, (byte) 0x30, 0, (byte) 0x08};
    private static final byte[] SET_MODE_FM_CMD={8, 0, 0, 0, (byte) 0x07};
    private static final byte[] SET_MODE_FM_CMD_R={};
    private static final byte[] OK_R={0};
    private static final byte[] ERR_R={(byte)0xF0};


    // commands for Kenwood 00014523000
    private static byte[] K_INIT_ON;
    private static byte[] K_LOCK_OFF_CMD;
    private static byte[] K_LOCK_ON_CMD;
    private static byte[] K_SET_FREQ_CMD;
    private static byte[] K_READ_FREQ_CMD;
    private static byte[] K_SET_MODE_FM_CMD;
    private static byte[] K_TST;
    private static byte[] K_TST_R;
    private static byte[] K_NO_R;
 //   private static byte[]  K_SET_FREQ_CMD_R;
  //  private static byte[]  K_READ_FREQ_CMD_R;
    private static byte[]  K_SET_MODE_FM_CMD_R;


    {
        try {
            K_INIT_ON="AI0;".getBytes("US-ASCII");
            K_LOCK_OFF_CMD="LK0;".getBytes("US-ASCII");
            K_LOCK_ON_CMD="LK1;".getBytes("US-ASCII");
            K_SET_FREQ_CMD="FA00014523000;".getBytes("US-ASCII");
            K_READ_FREQ_CMD="FA;".getBytes("US-ASCII");
            K_SET_MODE_FM_CMD="MD4;".getBytes("US-ASCII");
            K_TST_R="?;".getBytes("US-ASCII");
            K_TST="aaaaaa;".getBytes("US-ASCII");
            K_NO_R=new byte[0];
           //  K_SET_FREQ_CMD_R="FA00014523000;".getBytes("US-ASCII");
           //  K_READ_FREQ_CMD_R;
             K_SET_MODE_FM_CMD_R="MD".getBytes("US-ASCII");

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(BlkCatWorkerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     */
    public BlkCatWorkerTest() {
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
        String filetemp =  RadioIniFileAccess.getOmniIniFileURL().getFile();
        radioid=filetemp.substring(filetemp.lastIndexOf("/")+1);
        radioid=radioid.substring(0, radioid.lastIndexOf(".")).trim();
    
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
        if (null != myInstance) {
            myInstance.stop();
        }
    }

    /**
     * Test of activate method, of class BlkCATWorker.
     */
    @Test
    public void testActivate() {
        System.out.println("activate, stopme, getThread");

//make sure initial states are what is expected
        myInstance=new BlkCATWorker(comm);
        assertFalse(myInstance.isConnected);
        assertFalse(myInstance.portExists);
        Thread workerThread=myInstance.getWorkerThread();
        assertNull(workerThread);

//activate and check states
        boolean actresult=myInstance.activate();
        assertTrue(actresult);
        actresult=myInstance.activate();
        assertFalse(actresult);
        assertTrue(myInstance.isConnected);
        assertTrue(myInstance.portExists);
        workerThread=myInstance.getWorkerThread();
        assertNotNull(workerThread);
        String workerThreadID=workerThread.getName();
        assertTrue(workerThreadID.startsWith(myInstance.HNC));

//stop worker and check states
        myInstance.stop();
        int cntdwn=10;
        while (workerThread.isAlive() && 0 < cntdwn--) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(BlkCatWorkerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        assertTrue(cntdwn > 0);
        assertFalse(myInstance.isConnected);
        assertFalse(myInstance.portExists);
        // reactivate and check states.
        actresult=myInstance.activate();
        assertTrue(actresult);
        assertTrue(myInstance.isConnected);
        assertTrue(myInstance.portExists);
        workerThread=myInstance.getWorkerThread();
        assertNotNull(workerThread);
        String newworkerThreadID=workerThread.getName();
        assertTrue(newworkerThreadID.startsWith(myInstance.HNC));
     //   assertFalse(newworkerThreadID.equals(workerThreadID));
        //stop it
        myInstance.stop();

    }

    /**
     * Test of stop method, of class BlkCATWorker.
     */
    @Test
    public void testStopMe() {
        System.out.println("stopMe");

        BlkCATWorker mynewInstance=null;
        try {
            myInstance=new BlkCATWorker(comm);
            Thread instanceThread=myInstance.getWorkerThread();
            assertNull("Thread automaically started", instanceThread);

            myInstance.activate();
            instanceThread=myInstance.getWorkerThread();
            assertTrue("thread not activated", null != instanceThread);
            assertTrue("thread not alive or not deamon", instanceThread.isAlive() && instanceThread.isDaemon());

            myInstance.stop();
            assertFalse("Thread did not stop", instanceThread.isAlive());
            assertTrue("stopped thread not marked as deamon", instanceThread.isDaemon());
            //does multipe stops crash?
            myInstance.stop();
            assertFalse("thread did not stop", instanceThread.isAlive());
            assertTrue("stopped thread not marked as deamon", instanceThread.isDaemon());

            //multiple activates have probem?
            assertTrue(myInstance.activate());
            assertFalse(myInstance.activate());
            instanceThread=myInstance.getWorkerThread();

            assertTrue("Thread did not start", instanceThread.isAlive() && instanceThread.isDaemon());
            myInstance.stop();

            mynewInstance=new BlkCATWorker(comm);
            assertTrue("New instnace not created", mynewInstance != myInstance);

            instanceThread=mynewInstance.getWorkerThread();
            assertNull("worker thread should not yet exist", instanceThread);
            mynewInstance.stop();
            mynewInstance.activate();

            instanceThread=mynewInstance.getWorkerThread();
            assertTrue("worker thread not started", instanceThread.isAlive() && instanceThread.isDaemon());
            mynewInstance.stop();
        } finally {
            if (null != mynewInstance) {
                mynewInstance.stop();
            }
            if (null != myInstance) {
                myInstance.stop();
            }
        }
    }

    
     void resetRadio() throws IOException{
        if ("FT-897".equals(radioid)){

            BlkQRspHandler<OmniRigCmdData> handler=new BlkQRspHandler<OmniRigCmdData>(true);
            PktInfo lockOffPkt=new PktInfo("portid", new BlkServerCATCmd(new byte[]{0, 0, 0, 0, (byte) 0x80}), handler);
            myInstance.doWork(lockOffPkt); //set initial lock state
            PktInfo resPkt=handler.waitForResponse();
        }else if ("Kenwood".equals(radioid)){
            BlkQRspHandler<OmniRigCmdData> handler=new BlkQRspHandler<OmniRigCmdData>(true);
            PktInfo junkcmd=new PktInfo("portid", new BlkServerCATCmd("asdfffdsa;".getBytes("US-ASCII")), handler);
            myInstance.doWork(junkcmd); //set initial lock state
            PktInfo resPkt=handler.waitForResponse();
            assertArrayEquals ("?;".getBytes("US-ASCII"),(byte[])resPkt.payload);
        }
    }

    /**
     * Test of doWork method, of class BlkCATWorker.
     */
    @Test
    
    public void testDoWork() {
        System.out.println("doWork - single command");

        try {

            myInstance=new BlkCATWorker(comm);
            myInstance.activate();

            resetRadio();

            BlkQRspHandler<OmniRigCmdData> handler=new BlkQRspHandler<OmniRigCmdData>(true);
            PktInfo lockTstPkt=new PktInfo("portid", new BlkServerCATCmd(getTestData(DataTst.TEST)), handler);
            myInstance.doWork(lockTstPkt); //set initial lock state
            byte[] expected=getExpected(DataTst.TEST);
            PktInfo resPkt=null;
            byte[] result=new byte[0];
            if (expected.length != 0) {
                resPkt=handler.waitForResponse();
                result=(byte[]) resPkt.payload;
                assertArrayEquals(expected, result);
            }
           

            PktInfo lockOnPkt=new PktInfo("portid", new BlkServerCATCmd(getTestData(DataTst.INIT)), handler);
            myInstance.doWork(lockOnPkt); //set initial lock state
            expected=getExpected(DataTst.INIT);
            if (expected.length != 0) {
                resPkt=handler.waitForResponse();
                result=(byte[]) resPkt.payload;
                assertArrayEquals(expected, result);
            }

            handler=new BlkQRspHandler<OmniRigCmdData>(true);
            lockOnPkt=new PktInfo("portid", new BlkServerCATCmd(getTestData(DataTst.LKON)), handler);
            myInstance.doWork(lockOnPkt); //set initial lock state
             expected=getExpected(DataTst.LKON);
            if (expected.length != 0) {
                resPkt=handler.waitForResponse();
                result=(byte[]) resPkt.payload;
                assertArrayEquals(expected, result);
            }
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(BlkCatWorkerTest.class.getName()).log(Level.SEVERE, null, ex);
//            }

            handler=new BlkQRspHandler<OmniRigCmdData>(true);
            lockOnPkt=new PktInfo("portid", new BlkServerCATCmd(getTestData(DataTst.LKON)), handler);
            myInstance.doWork(lockOnPkt); //set initial lock state again, should detect duplicate
             expected=getExpected(DataTst.ERR1);
         if (expected.length != 0) {
                resPkt=handler.waitForResponse();
                result=(byte[]) resPkt.payload;
                assertArrayEquals(expected, result);
            }

            handler=new BlkQRspHandler<OmniRigCmdData>(true);
            final PktInfo lockOffPkt=new PktInfo("portid", new BlkServerCATCmd(getTestData(DataTst.LKOFF)), handler);
            myInstance.doWork(lockOffPkt);
            expected=getExpected(DataTst.OK);
            if (expected.length != 0) {
                resPkt=handler.waitForResponse();
                result=(byte[]) resPkt.payload;
                assertArrayEquals(expected, result);
            }

            handler=new BlkQRspHandler<OmniRigCmdData>(true);
            final PktInfo setFm=new PktInfo("portid", new BlkServerCATCmd(getTestData(DataTst.SETM)), handler);
            myInstance.doWork(setFm);
            expected=getExpected(DataTst.SETM);
         if (expected.length != 0) {
                resPkt=handler.waitForResponse();
                result=(byte[]) resPkt.payload;
                assertArrayEquals(expected, result);
            }

            handler=new BlkQRspHandler<OmniRigCmdData>(true);
            final PktInfo setFreq=new PktInfo("portid", new BlkServerCATCmd(getTestData(DataTst.SFREQ)), handler);
            myInstance.doWork(setFreq); 
              expected = getExpected(DataTst.SFREQ);
         if (expected.length != 0) {
                resPkt=handler.waitForResponse();
                result=(byte[]) resPkt.payload;
                assertArrayEquals(expected, result);
            }

            handler=new BlkQRspHandler<OmniRigCmdData>(true);
            final PktInfo readFreq=new PktInfo("portid", new BlkServerCATCmd(getTestData(DataTst.RFREQ)), handler);
            myInstance.doWork(readFreq);
           expected=getExpected(DataTst.RFREQ);
             if (expected.length != 0) {
                resPkt=handler.waitForResponse();
                result=(byte[]) resPkt.payload;
                assertArrayEquals(expected, result);
            }
            myInstance.stop();
        } catch (IOException ioe) {
            final JFrame window=null; // error dialog box centers on this window if present
            final String msg="Radio did not respond io exception";
            JOptionPane.showMessageDialog(window, msg, ioe.getMessage(), JOptionPane.ERROR_MESSAGE);
            assert false;
        } finally {
            if (myInstance != null) {
                myInstance.stop();
            }
        }
    }

    /**
     * Test of getWorkerThread method, of class BlkCATWorker.
     */
    @Test
    @Ignore // tested in the other tests
    public void testGetWorkerThread() {
        System.out.println("getWorkerThread");
        BlkCATWorker instance=null;
        Thread expResult=null;
        Thread result=instance.getWorkerThread();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of run method, of class BlkCATWorker.
     */
    @Test
    @Ignore // tested in the other tests
    public void testRun() {
        System.out.println("run");
        BlkCATWorker instance=null;
        instance.run();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of doWork method, of class BlkCATWorker.
     */
    @Test
    @Ignore // already tested by testDoWork
    public void testDoWork_PktInfo() {
        System.out.println("doWork");
        PktInfo pkt=null;
        BlkCATWorker instance=null;
        instance.doWork(pkt);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of doWork method, of class BlkCATWorker.
     */
    @Test
    
    public void testDoWork_PktInfoArr() {

        System.out.println("doWork - array commands");

        try {      
            byte[] result=new byte[0];
            byte[] success=getExpected(DataTst.OK);
            byte[] fail=getExpected(DataTst.ERR);

            myInstance=new BlkCATWorker(comm);
            myInstance.activate();
              resetRadio();
            PktInfo[] pkts=new PktInfo[5];
            BlkQRspHandler[] handlers=new BlkQRspHandler[5];
            for (int i=0; i < handlers.length; i++) {
                handlers[i]=new BlkQRspHandler<OmniRigCmdData>(true);
            }

            final String comid=comm.getSerialPortStr();

            pkts[0]=new PktInfo(comid, new BlkServerCATCmd(getTestData(DataTst.LKON)), handlers[0]);
            pkts[1]=new PktInfo(comid, new BlkServerCATCmd(getTestData(DataTst.SETM)), handlers[1]);
            pkts[2]=new PktInfo(comid, new BlkServerCATCmd(getTestData(DataTst.SFREQ)), handlers[2]);
            pkts[3]=new PktInfo(comid, new BlkServerCATCmd(getTestData(DataTst.RFREQ)), handlers[3]);
            pkts[4]=new PktInfo(comid, new BlkServerCATCmd(getTestData(DataTst.LKOFF)), handlers[4]);
            myInstance.doWork(pkts);

            for (int i=0; i < handlers.length; i++) {
                PktInfo resPkt=null;
                if (i == 0) {
                    if (getExpected(DataTst.LKON).length != 0) {
                        resPkt=handlers[i].waitForResponse();
                        result=(byte[]) resPkt.payload;
                        assertArrayEquals(success, result);
                    }
                    continue;
                }
                if (i == 1) {
                    if (getExpected(DataTst.SETM).length != 0) {
                        resPkt=handlers[i].waitForResponse();
                        result=(byte[]) resPkt.payload;
                        assertArrayEquals(success, result);
                    }
                     continue;
                }
                if (i == 2) {
                    if (getExpected(DataTst.SFREQ).length != 0) {
                        resPkt=handlers[i].waitForResponse();
                        result=(byte[]) resPkt.payload;
                        assertArrayEquals(success, result);
                    }
                     continue;
                }
                if (i == 3) {
                    if (getExpected(DataTst.RFREQ).length != 0) {
                        resPkt=handlers[i].waitForResponse();
                        result=(byte[]) resPkt.payload;
                        assertArrayEquals(getExpected(DataTst.RFREQ), result);
                        //new byte[]{(byte) 0x14, (byte) 0x52, (byte) 0x30, 0, (byte) 0x08}
                    }
                     continue;
                }
                if (i == 4) {
                    if (getExpected(DataTst.LKOFF).length != 0) {
                        resPkt=handlers[i].waitForResponse();
                        result=(byte[]) resPkt.payload;
                        assertArrayEquals(success, result);
                    }
                     continue;
                }
            }
        } catch (IOException ioe) {
            final JFrame window=null; // error dialog box centers on this window if present
            final String msg="Radio did not respond";
           
            JOptionPane.showMessageDialog(window, msg, ioe.getMessage(), JOptionPane.ERROR_MESSAGE);
             assert false;
        } finally {
            if (myInstance != null) {
                myInstance.stop();
            }
        }
    }

    /**
     * Test of doWork method, of class BlkCATWorker.
     */
    @Test
     @SuppressWarnings("deprecation")
    public void testDoWork_List() {

        System.out.println("doWork - list commands");
        try {
            
            byte[] result=new byte[0];
            byte[] success=getExpected(DataTst.OK);
            byte[] fail=getExpected(DataTst.ERR);

            BlkQRspHandler[] handlers=new BlkQRspHandler[5];

            myInstance=new BlkCATWorker(comm);
            myInstance.activate();
            resetRadio();
            List<PktInfo> pkts=new LinkedList<PktInfo>();

            for (int i=0; i < handlers.length; i++) {
                handlers[i]=new BlkQRspHandler<OmniRigCmdData>(true);
            }

            final String comid=comm.getSerialPortStr();

            pkts.add(new PktInfo(comid, new BlkServerCATCmd(getTestData(DataTst.LKON)), handlers[0]));
            pkts.add(new PktInfo(comid, new BlkServerCATCmd(getTestData(DataTst.SETM)), handlers[1]));
            pkts.add(new PktInfo(comid, new BlkServerCATCmd(getTestData(DataTst.SFREQ)), handlers[2]));
            pkts.add(new PktInfo(comid, new BlkServerCATCmd(getTestData(DataTst.RFREQ)), handlers[3]));
            pkts.add(new PktInfo(comid, new BlkServerCATCmd(getTestData(DataTst.LKOFF)), handlers[4]));

            myInstance.doWork(pkts);

           for (int i=0; i < handlers.length; i++) {
                PktInfo resPkt=null;
                if (i == 0) {
                    if (getExpected(DataTst.LKON).length != 0) {
                        resPkt=handlers[i].waitForResponse();
                        result=(byte[]) resPkt.payload;
                        assertArrayEquals(success, result);
                    }
                    continue;
                }
                if (i == 1) {
                    if (getExpected(DataTst.SETM).length != 0) {
                        resPkt=handlers[i].waitForResponse();
                        result=(byte[]) resPkt.payload;
                        assertArrayEquals(success, result);
                    }
                     continue;
                }
                if (i == 2) {
                    if (getExpected(DataTst.SFREQ).length != 0) {
                        resPkt=handlers[i].waitForResponse();
                        result=(byte[]) resPkt.payload;
                        assertArrayEquals(success, result);
                    }
                     continue;
                }
                if (i == 3) {
                    if (getExpected(DataTst.RFREQ).length != 0) {
                        resPkt=handlers[i].waitForResponse();
                        result=(byte[]) resPkt.payload;
                        assertArrayEquals(getExpected(DataTst.RFREQ), result);
                        //new byte[]{(byte) 0x14, (byte) 0x52, (byte) 0x30, 0, (byte) 0x08}
                    }
                     continue;
                }
                if (i == 4) {
                    if (getExpected(DataTst.LKOFF).length != 0) {
                        resPkt=handlers[i].waitForResponse();
                        result=(byte[]) resPkt.payload;
                        assertArrayEquals(success, result);
                    }
                     continue;
                }
            }
        } catch (IOException ioe) {
            final JFrame window=null; // error dialog box centers on this window if present
            final String msg="Radio did not respond";
            JOptionPane.showMessageDialog(window, msg, ioe.getMessage(), JOptionPane.ERROR_MESSAGE);
            assert false;
        } finally {
            if (myInstance != null) {
                myInstance.stop();
            }
        }
    }

    /**
     * Test of doWork method, of class BlkCATWorker.
     */
    @Test
     @SuppressWarnings("deprecation")
    public void testDoWork_Queue() {

        System.out.println("doWork - Queue commands");
        try {
            
                byte[] result=new byte[0];
            byte[] success=getExpected(DataTst.OK);
            byte[] fail=getExpected(DataTst.ERR);

            BlkQRspHandler[] handlers=new BlkQRspHandler[5];

            myInstance=new BlkCATWorker(comm);
            myInstance.activate();
           resetRadio();
           Queue<PktInfo> pkts=new LinkedList<PktInfo>();

            for (int i=0; i < handlers.length; i++) {
                handlers[i]=new BlkQRspHandler<OmniRigCmdData>(true);
            }

            final String comid=comm.getSerialPortStr();
      pkts.add(new PktInfo(comid, new BlkServerCATCmd(getTestData(DataTst.LKON)), handlers[0]));
            pkts.add(new PktInfo(comid, new BlkServerCATCmd(getTestData(DataTst.SETM)), handlers[1]));
            pkts.add(new PktInfo(comid, new BlkServerCATCmd(getTestData(DataTst.SFREQ)), handlers[2]));
            pkts.add(new PktInfo(comid, new BlkServerCATCmd(getTestData(DataTst.RFREQ)), handlers[3]));
            pkts.add(new PktInfo(comid, new BlkServerCATCmd(getTestData(DataTst.LKOFF)), handlers[4]));
            myInstance.doWork(pkts);

            for (int i=0; i < handlers.length; i++) {
                PktInfo resPkt=null;
                if (i == 0) {
                    if (getExpected(DataTst.LKON).length != 0) {
                        resPkt=handlers[i].waitForResponse();
                        result=(byte[]) resPkt.payload;
                        assertArrayEquals(success, result);
                    }
                    continue;
                }
                if (i == 1) {
                    if (getExpected(DataTst.SETM).length != 0) {
                        resPkt=handlers[i].waitForResponse();
                        result=(byte[]) resPkt.payload;
                        assertArrayEquals(success, result);
                    }
                     continue;
                }
                if (i == 2) {
                    if (getExpected(DataTst.SFREQ).length != 0) {
                        resPkt=handlers[i].waitForResponse();
                        result=(byte[]) resPkt.payload;
                        assertArrayEquals(success, result);
                    }
                     continue;
                }
                if (i == 3) {
                    if (getExpected(DataTst.RFREQ).length != 0) {
                        resPkt=handlers[i].waitForResponse();
                        result=(byte[]) resPkt.payload;
                        assertArrayEquals(getExpected(DataTst.RFREQ), result);
                        //new byte[]{(byte) 0x14, (byte) 0x52, (byte) 0x30, 0, (byte) 0x08}
                    }
                     continue;
                }
                if (i == 4) {
                    if (getExpected(DataTst.LKOFF).length != 0) {
                        resPkt=handlers[i].waitForResponse();
                        result=(byte[]) resPkt.payload;
                        assertArrayEquals(success, result);
                    }
                     continue;
                }
            }
        } catch (IOException ioe) {
            final JFrame window=null; // error dialog box centers on this window if present
            final String msg="Radio did not respond";
            JOptionPane.showMessageDialog(window, msg, ioe.getMessage(), JOptionPane.ERROR_MESSAGE);
            assert false;
        } finally {
            if (myInstance != null) {
                myInstance.stop();
            }
        }
    }
}
