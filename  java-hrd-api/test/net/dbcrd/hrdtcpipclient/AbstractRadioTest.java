/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.dbcrd.hrdtcpipclient;

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
public class AbstractRadioTest {



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
     * Test of getPortName method, of class AbstractRadio.
     */
    @Test
    public void testGetPortName() {
        System.out.println("getPortName");
        AbstractRadioImpl instance=new AbstractRadioImpl("com1");
        String expResult="COM1";
        String result=instance.getCommunicationInfo().getProperty(instance.RADIO_IDENTIFICATION);
        assertEquals(expResult, result);

         instance = new AbstractRadioImpl(RadioIdentification.HRD);
           expResult="Ham Radio Deluxe Server";
           result=instance.getCommunicationInfo().getProperty(instance.RADIO_IDENTIFICATION);
        assertEquals(expResult, result);
    }





    public class AbstractRadioImpl extends AbstractRadio {

        public AbstractRadioImpl(final RadioIdentification portName) {
            super(portName);
        }

        public AbstractRadioImpl( final String portName) {
            super(portName);
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