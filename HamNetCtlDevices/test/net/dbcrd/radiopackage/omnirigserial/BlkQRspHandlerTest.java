/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.dbcrd.radiopackage.omnirigserial;

import org.junit.Ignore;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
public class BlkQRspHandlerTest {

    /**
     *
     */
    public BlkQRspHandlerTest() {
    }

    /**
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
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
     * Test of handleResponse method, of class BlkQRspHandler.
     */
    @Test
    @Ignore // tested by other tests
    public void testHandleResponse() {
        System.out.println("handleResponse");
        PktInfo rsp=null;
        BlkQRspHandler instance=new BlkQRspHandler();
        boolean expResult=false;
        boolean result=instance.handleResponse(rsp);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of waitForResponse method, of class BlkQRspHandler.
     */
    @Test
      @Ignore // tested by other tests
    public void testWaitForResponse() {
        System.out.println("waitForResponse");
        BlkQRspHandler instance=new BlkQRspHandler();
        PktInfo expResult=null;
        PktInfo result=null;
        try {
            result=instance.waitForResponse();
        } catch (IOException ioe) {
            final JFrame window=null; // error dialog box centers on this window if present
            final String msg="Radio did not respond";
            JOptionPane.showMessageDialog(window, msg, ioe.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
