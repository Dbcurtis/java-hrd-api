/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.dbcrd.radiopackage.omnirigserial;

import net.dbcrd.radiopackage.CommSetup;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
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
public class BlkQServerTest {

    static BlkQServer myInstance;
    static BlockingQueue<PktInfo> client2serverQ;
    private static final byte[] LOCK_ON_CMD={0, 0, 0, 0, (byte) 0x00};
    private static final byte[] LOCK_OFF_CMD={0, 0, 0, 0, (byte) 0x80};
    private static final byte[] SET_FREQ_CMD={
        (byte) 0x14, (byte) 0x52, (byte) 0x30, 0, (byte) 0x01};
    private static final byte[] READ_FREQ_CMD={0, 0, 0, 0, (byte) 0x03};
    private static final byte[] SET_MODE_FM_CMD={8, 0, 0, 0, (byte) 0x07};

    /**
     *
     */
    public BlkQServerTest() {
    }

    static CommSetup comm=null;

    /**
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        myInstance=BlkQServer.getInstance();//, server2clientQ);
        client2serverQ=myInstance.getClient2ServerQ();
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
        myInstance.stop();
    }

    /**
     *
     */
    @Before
    public void setUp() {
        client2serverQ.clear();

    }

    /**
     *
     */
    @After
    public void tearDown() {
    }

    /**
     * Test of getInstance method, of class BlkQServer.
     */
    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
        BlkQServer test=BlkQServer.getInstance();
        assertSame(test, myInstance);
        test=BlkQServer.getInstance();
        assertSame(test, myInstance);
    }

    /**
     * Test of run method, of class BlkQServer.
     */
    @Test
    @Ignore
    public void testRun() {
        System.out.println("run");

        fail("The test case is a prototype.");
    }

    /**
     * Test of stopMe method, of class BlkQServer.
     */
    @Test
    public void testStop() {
        System.out.println("stop, activate and getServerThread");
        BlkQServer instance=BlkQServer.getInstance(); //server automatically activates
        boolean activateState=instance.activate();       
        Thread serverT=instance.getServerThread();
        String serverTName=serverT.getName();
        assertEquals("BlkQServer", serverTName);
        activateState=instance.activate();
        final BlkServerCmd regEcho=new BlkServerCmd(BlkServerCmd.CmdType.REGISTER_ECHO, "ECHO");
        final PktInfo recEchoPkt=new PktInfo(regEcho);
        client2serverQ.offer(recEchoPkt);

        assertFalse(activateState);
        assert (serverT.isAlive());
        instance.stop();
        int cnt=10;
        while (cnt-- >= 0 && serverT.isAlive()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(BlkQServerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        assertTrue(cnt >= 0);
        assertFalse(serverT.isAlive());

    }

    /**
     * Test of activate method, of class BlkQServer.
     */
    @Test
    public void testActivate() {
        System.out.println("activate, getClient2ServerQ, getRegistetred and stop");
        CommSetup commtemp=CommSetup4Test.getComm();
        if (null == commtemp) {
            CommSetup4Test.setComm();
        }
        final CommSetup comm=CommSetup4Test.getComm();

        // final BlkCATWorker worker=new BlkCATWorker(comm);

        final BlkQServer server=BlkQServer.getInstance();
        client2serverQ=server.getClient2ServerQ();
        server.activate();

        final BlkServerCmd regCom=new BlkServerCmd(BlkServerCmd.CmdType.REGISTER_PORT, comm);
        final BlkServerCmd regEcho=new BlkServerCmd(BlkServerCmd.CmdType.REGISTER_ECHO, "ECHO");

        Iterator<String> regit=server.getRegistered();
        assertFalse(regit.hasNext());

        final PktInfo recEchoPkt=new PktInfo(regEcho);
        final PktInfo recComPkt=new PktInfo(regCom);
        client2serverQ.offer(recEchoPkt);
        client2serverQ.offer(recComPkt);
        int testint=client2serverQ.size();

        regit=server.getRegistered();
        Set<String> regs=new HashSet<String>();
        while (regit.hasNext()) {
            regs.add(regit.next());
        }
        assertSame("wrong registration count", 2, regs.size());
        try {
            BlkServerCATCmd ccmd=new BlkServerCATCmd("this is a test".getBytes());
            BlkQRspHandler handler=new BlkQRspHandler();
            PktInfo dataPkt=new PktInfo("ECHO", ccmd, handler);
            client2serverQ.offer(dataPkt);
            PktInfo inpkt=handler.waitForResponse();
            BlkServerCATCmd rcmd=(BlkServerCATCmd) inpkt.payload;
            assertArrayEquals(rcmd.getCmdObj(), "this is a test".getBytes());

            handler=new BlkQRspHandler(true);
            dataPkt=new PktInfo("com0", ccmd, handler);  // check wrong comid response
            client2serverQ.offer(dataPkt);
            inpkt=handler.waitForResponse();
            byte[] reply=(byte[]) inpkt.payload;
            assertArrayEquals("Illegal communication port specified".getBytes(), reply);

            if (true){
                 System.out.println("Only works on FT-897 et al.***********expect non-responsive radio*********************************");
            handler=new BlkQRspHandler();
            PktInfo lockOnPkt=new PktInfo(comm.getSerialPortStr(), new BlkServerCATCmd(LOCK_ON_CMD), handler);
            client2serverQ.offer(lockOnPkt); //set initial lock state
            PktInfo resPkt=handler.waitForResponse();

            handler=new BlkQRspHandler();
            lockOnPkt=new PktInfo(comm.getSerialPortStr(), new BlkServerCATCmd(LOCK_ON_CMD), handler);
            client2serverQ.offer(lockOnPkt); //set initial lock state
            resPkt=handler.waitForResponse();
            byte[] result=(byte[]) resPkt.payload;
            byte[] expected=new byte[]{(byte) 0xF0};
            assertArrayEquals(expected, result);

            handler=new BlkQRspHandler();
            final PktInfo lockOffPkt=new PktInfo(comm.getSerialPortStr(), new BlkServerCATCmd(LOCK_OFF_CMD), handler);
            client2serverQ.offer(lockOffPkt);
            expected=new byte[]{0};
            resPkt=handler.waitForResponse();
            result=(byte[]) resPkt.payload;
            assertArrayEquals(expected, result);

            handler=new BlkQRspHandler();
            final PktInfo setFreq=new PktInfo(comm.getSerialPortStr(), new BlkServerCATCmd(SET_FREQ_CMD), handler);
            client2serverQ.offer(setFreq); //set freq 145.230
            resPkt=handler.waitForResponse();
            result=(byte[]) resPkt.payload;
            assertArrayEquals(expected, result);

            handler=new BlkQRspHandler();
            final PktInfo setFm=new PktInfo(comm.getSerialPortStr(), new BlkServerCATCmd(SET_MODE_FM_CMD), handler);
            client2serverQ.offer(setFm);
            resPkt=handler.waitForResponse();
            result=(byte[]) resPkt.payload;
            assertArrayEquals(expected, result);

            handler=new BlkQRspHandler();
            final PktInfo readFreq=new PktInfo(comm.getSerialPortStr(), new BlkServerCATCmd(READ_FREQ_CMD), handler);
            client2serverQ.offer(readFreq);

            resPkt=handler.waitForResponse();
            expected=new byte[]{(byte) 0x14, (byte) 0x52, (byte) 0x30, 0, (byte) 0x08};
            result=(byte[]) resPkt.payload;
            assertArrayEquals(expected, result);
        }

            client2serverQ.offer(new PktInfo(new BlkServerCmd(BlkServerCmd.CmdType.DEREGISTER_PORT, comm.getSerialPortStr())));

            regit=server.getRegistered();
            regs.clear();
            while (regit.hasNext()) {
                regs.add(regit.next());
            }

            assertEquals(1, regs.size());
            myInstance.stop();
            regit=server.getRegistered();
            regs.clear();
            while (regit.hasNext()) {
                regs.add(regit.next());
            }
            assertTrue(regs.isEmpty());
            server.stop();

        } catch (IOException ioe) {
            final JFrame window=null; // error dialog box centers on this window if present
            final String msg="Radio did not respond";
            JOptionPane.showMessageDialog(window, msg, ioe.getMessage(), JOptionPane.ERROR_MESSAGE);
        }

    }

    /**
     * Test of getRegistered method, of class BlkQServer.
     */
    @Test
    @Ignore
    public void testGetRegistered() {
        System.out.println("getRegistered");
        BlkQServer instance=null;
        Set expResult=null;
        Iterator result=instance.getRegistered();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getClient2ServerQ method, of class BlkQServer.
     */
    @Test
    @Ignore
    public void testGetClient2ServerQ() {
        System.out.println("getClient2ServerQ");
        BlkQServer instance=null;
        BlockingQueue expResult=null;
        BlockingQueue result=instance.getClient2ServerQ();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getServerThread method, of class BlkQServer.
     */
    @Test
    @Ignore
    public void testGetServerThread() {
        System.out.println("getServerThread");
        BlkQServer instance=null;
        Thread expResult=null;
        Thread result=instance.getServerThread();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
