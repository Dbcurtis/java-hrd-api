
package net.dbcrd.radiopackage.omnirigserial;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dbcrd.radiopackage.CommSetup;
import net.dbcrd.radiopackage.Mode;
import net.dbcrd.radiopackage.RadioControl;
import net.dbcrd.radiopackage.RadioIdentification;
import net.dbcrd.radiopackage.RadioStatus;
import org.junit.Ignore;
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
public class OmniRigMakeRadioTest {

    static private final ClassLoader classLoader=Thread.currentThread().getContextClassLoader();

    public OmniRigMakeRadioTest() {
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
     * Test of stop method, of class OmniRigMakeRadio.
     */
    @Test
    @Ignore
    public void testStop() {
        System.out.println("stop");
        RadioControl radio=null;
        OmniRigMakeRadio instance=new OmniRigMakeRadio();
        boolean expResult=false;
        //   boolean result=instance.stop(radio);
        //   assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of makeRadio method, of class OmniRigMakeRadio.
     */
    @Test
    public void testMakeRadio() {
        System.out.println("makeRadio and stop radio");
        // URL omniRigIniURL=classLoader.getResource("net/dbcrd/omniRigInitFiles/FT-897.ini");
        OmniRigMakeRadio instance=new OmniRigMakeRadio();

        RadioControl radio=instance.makeRadio(RadioIniFileAccess.getOmniIniFileURL(), comm); // make the radio
        assertTrue(radio.isThisRadioControlled());              // test the radio a bit
        Mode initMode=radio.getMode();
        assertTrue(radio.setMode(Mode.AM));
        assertEquals(Mode.AM, radio.getMode());
        assertTrue(radio.setMode(Mode.FM));
        assertEquals(Mode.FM, radio.getMode());
        long initfreq=radio.getFreq();
        radio.setFreq(1230000L);
        long myfreq=radio.getFreq();
        assertEquals(myfreq, 1230000L);
        radio.pttPush();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(OmniRigMakeRadioTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertTrue(radio.pttRelease());

        Properties comprop=radio.getCommunicationInfo();
        assertTrue(comprop.containsKey("TCPIPPort") && comprop.containsKey("SerialPort") && comprop.containsKey("Baud"));
        assertFalse(radio.setLock(true));
        assertFalse(radio.isLock());
        assertFalse(radio.setPlTone("100"));
        assertFalse(radio.setRepeaterOffset(600000));
        RadioStatus rs=radio.getStatus();
        long newFreq=Long.parseLong(rs.getProperty(RadioStatus.FREQ));
        assertEquals(1230000L, newFreq);

        String id=radio.getRadioID();
        assertTrue(id.endsWith("FT-897"));
        RadioIdentification rid=radio.getRadioIdentification();
        assertEquals("Omni Rig Physical", rid.toString());
        /*
         * reset back to orignal freq and mode
         */
        radio.setFreq(initfreq);
        radio.setMode(initMode);
        instance.stop(radio); // stop the radio.
        assertFalse(radio.isThisRadioControlled());
     //   assertTrue(OmniRigMakeRadio.radioMap.isEmpty());
       // Thread servert=OmniRigMakeRadio.getServer().getServerThread();
//        int cntdwn=10;
//        while (cntdwn-- > 0 && servert.isAlive()) {
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(OmniRigMakeRadioTest.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        assertFalse(servert.isAlive());
    }
}
