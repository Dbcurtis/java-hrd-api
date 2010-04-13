/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.dbcrd.hrdtcpipclient;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class AbstractVirtualRadioTest {

    public AbstractVirtualRadioTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of connectToRadioServer method, of class AbstractVirtualRadio.
     */
    @Test
    public void testConnectToRadioServer()  {
        System.out.println("connectToRadioServer");
        AbstractVirtualRadioImpl instance=new AbstractVirtualRadioImpl(RadioIdentification.HRD);
        try{
            instance.connectToRadioServer();
        } catch(IOException ex){
           fail("unexpected IOException");
        }
        // TODO review the generated test code and remove the default call to fail.
       assertNull(instance.inetAddress);
       assertTrue(instance.client instanceof NioClient);
    }

    /**
     * Test of getRadioName method, of class AbstractVirtualRadio.
     */
    @Test
    public void testGetRadioName() {
        System.out.println("getRadioName");
        AbstractVirtualRadioImpl instance=new AbstractVirtualRadioImpl(RadioIdentification.HRD);
        String expResult="AbstractVirtualRadio";
        String result=instance.rStatus.getProperty(instance.RADIO_NAME);
        assertEquals(expResult, result);

    }

    public class AbstractVirtualRadioImpl extends AbstractVirtualRadio {

        public AbstractVirtualRadioImpl(final RadioIdentification vertualID) {
            super(vertualID);
        }

        public RadioControl selectRadio() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean setLock(boolean lock) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isLock() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean pttPush() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean pttRelease() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean pttHold() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean setPlTone(String pltone) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean setRepeaterOffset(long repeaterOffset) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isThisRadioControlled() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean setFreq(long freq) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public long getFreq() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean setMode(Mode mode) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Mode getMode() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isConnected() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getRadioID() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}