/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.dbcrd.hrdtcpipclient;

import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
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
public class RadioHRDTest {

    static Thread clientthread=null;
    static RadioControl radio;

    @BeforeClass
    public static void setUpClass() {
        radio=new RadioHRD();
        if(!radio.isThisRadioControlled()){
            fail("Hrd not connected");
        }
        final ThreadGroup myTGroup=Thread.currentThread().getThreadGroup();
        final Thread[] groupThreads=new Thread[myTGroup.activeCount()*2];
        myTGroup.enumerate(groupThreads);
        boolean foundthread=false;
        for(Thread td:groupThreads){
            if((td!=null)&&("HRD Client".equals(td.getName()))){
                foundthread=true;
                clientthread=td;
                break;
            }
        }
        assertTrue(foundthread);
    }

    @AfterClass
    public static void tearDownClass()  {
        RadioStatus props=radio.getStatus();
        NavigableMap<String, String>sss
                =new ConcurrentSkipListMap<String, String>();
              //  (props.entrySet());

      
        for (Map.Entry ent:props.entrySet()){
            String key = (String)ent.getKey();
            String val = (String)ent.getValue();
            sss.put(key,val);
        }

        String expected = "{Daemon=yes, Freq=145230000, PLtone=unknown, RadioName=FT-897, Served_radios=1:FT-897, " +
                "Server=Ham Radio Deluxe, Server_version=v5.0 b2494, agc=state unknown, alc=state unknown, " +
                "autom=state unknown, a~/~b=state unknown, bk=state unknown, clarifier=state unknown, " +
                "clientThread=HRD Client, ctcss=state unknown, dbf=state unknown, dnf=state unknown, " +
                "dnr=state unknown, dsp=state unknown, dw=state unknown, fast=state unknown, keyer=state unknown, " +
                "lock=yes, mod=state unknown, mode=FM, mph=state unknown, nb=state unknown, pri=state unknown, " +
                "proc=state unknown, pwr=state unknown, radio_Options1=[swr, mph, dw, pwr, dbf, dsp, bk, agc, autom, " +
                "slow, dnf, vox, proc, alc, mod, keyer, lock, dnr, a~/~b, tx, pri, nb, scan, split, ctcss, fast], " +
                "radio_Options2={tone=[dcs~on, dcs~decoder~on, dcs~encoder~on, , ctcss~on, ctcss~decoder~on, " +
                "ctcss~encoder~on, , tone~off], repeater=['-'~shift, '+'~shift, simplex], clarifier=[on, off], " +
                "mode=[lsb, usb, cw, cw-r, am, wfm, fm, dig, pkt, fm(n)]}, repeater=positive, scan=state unknown, " +
                "slow=state unknown, split=state unknown, swr=state unknown, tone=state unknown, tx=state unknown, " +
                "vox=state unknown}";
        String result=sss.toString();
        assertEquals(expected,result);

        AbstractRadio.abortClientThread=true;
        if(null!=clientthread){
            clientthread.interrupt();
            while(true){
                if(Thread.State.TERMINATED.equals(clientthread.getState())){
                    break;
                }
            }
        }
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }


    /**
     * Test of pttHold method, of class RadioHRD.
     */
//    @Test
//    @Ignore
//    public void testPttHold() {
//        System.out.println("pttHold");
//        RadioHRD instance=radio;
//        boolean expResult=false;
//        boolean result=instance.pttHold();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of pttPush method, of class RadioHRD.
     */
    @Test
    public void testPttPush() {
        System.out.println("pttPush and release");
        RadioControl instance=radio;
        long currentfreq=instance.getFreq();
        instance.setFreq(CALL_FREQ);
        instance.setRepeaterOffset(0);
        boolean expResult=true;
        boolean result=instance.pttPush();
        assertEquals(expResult, result);
        try{
            Thread.sleep(500);
        } catch(InterruptedException ex){
            Logger.getLogger(RadioHRDTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        result=instance.pttRelease();
        assertEquals(expResult, result);
        instance.setFreq(currentfreq);
        System.out.println("do not forget to id");

    }

    /**
     * Test of pttRelease method, of class RadioHRD.
     */
    @Test
    public void testPttRelease() {
        System.out.println("pttRelease");
        RadioControl instance=radio;
        boolean expResult=true;
        boolean result=instance.pttRelease();
        assertEquals(expResult, result);
    }
    /**
     * Test of setFreq method, of class RadioHRD.
     */
    final static long INIT_FREQ=145230000;
    final static long SEC_FREQ=147015000;
    final static long CALL_FREQ=146520000;

    @Test
    public void testSetGetFreq() {
        System.out.println("setFreq and getFreq");
        RadioControl instance=radio;
        long initialFreq=radio.getFreq();
        instance.setFreq(INIT_FREQ);
        long currentFreq=instance.getFreq();
        assertEquals(INIT_FREQ, currentFreq);
        instance.setFreq(SEC_FREQ);
        currentFreq=instance.getFreq();
        assertEquals(SEC_FREQ, currentFreq);
        instance.setFreq(initialFreq);
        assertEquals(initialFreq, instance.getFreq());
    }

    /**
     * Test of setMode method, of class RadioHRD.
     */
    @Test
    public void testSetGetMode() {
        System.out.println("setMode and getMode");
        RadioControl instance=radio;
        long initialFreq=instance.getFreq();
        Mode initialMode=instance.getMode();
        instance.setMode(Mode.AM);
        assertEquals(Mode.AM, instance.getMode());
        instance.setMode(Mode.FM);
        assertEquals(Mode.FM, instance.getMode());
        instance.setMode(Mode.DIG);
        assertEquals(Mode.DIG, instance.getMode());
        instance.setMode(Mode.LSB);
        assertEquals(Mode.LSB, instance.getMode());
        instance.setMode(Mode.USB);
        assertEquals(Mode.USB, instance.getMode());
        instance.setMode(Mode.CW);
        assertEquals(Mode.CW, instance.getMode());
        instance.setMode(initialMode);
        assertEquals(initialMode, instance.getMode());
        instance.setFreq(initialFreq);
        assertEquals(initialFreq, instance.getFreq());

    }

    /**
     * Test of getRadioName method, of class RadioHRD.
     */
    @Test
    public void testGetRadioID() {
        System.out.println("getRadioID");
        RadioControl instance=radio;
        String expResult="FT-897 (HRD)";
        String result=instance.getRadioID();
        assertEquals(expResult, result);

    }

    /**
     * Test of getStatus method, of class RadioHRD.
     */
    @Test
    @Ignore
    public void testGetStatus() {
        System.out.println("getStatus");
        RadioControl instance=radio;
        RadioStatus expResult=null;
        RadioStatus result=instance.getStatus();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isThisRadioControlled method, of class RadioHRD.
     */
    @Test
    public void testIsThisRadio() {
        System.out.println("isThisRadio");
        RadioControl instance=radio;
        boolean expResult=true;
        boolean result=instance.isThisRadioControlled();
        assertEquals(expResult, result);

    }

    /**
     * Test of setPlTone method, of class RadioHRD.
     */
    @Test
    public void testSetPlTone() {
        System.out.println("setPlTone");
        String pltone="";
        RadioControl instance=radio;
        boolean expResult=true;
        boolean result=instance.setPlTone(pltone);
        assertEquals(expResult, result);

        result=instance.setPlTone("100.0");
        assertEquals(false, result);

    }

    /**
     * Test of setLock method, of class RadioHRD.
     */
    @Test
    public void testSetandIsLock() {
        System.out.println("setLock and isLock");
        RadioControl instance=radio;
        boolean initialLock=instance.isLock();
        boolean result=instance.setLock(true);
        assertEquals(true, result);
        assertEquals(true, instance.isLock());
        result=instance.setLock(false);
        assertEquals(true, result);
        assertEquals(false, instance.isLock());
        instance.setLock(initialLock);

    }

    /**
     * Test of setRepeaterOffset method, of class RadioHRD.
     */
    @Test
    public void testSetRepeaterOffset() {
        System.out.println("setRepeaterOffset");

        RadioControl instance=radio;
        boolean expResult=true;
        boolean result=instance.setRepeaterOffset(0);
        assertEquals(expResult, result);
        result=instance.setRepeaterOffset(-600);
        assertEquals(expResult, result);
        result=instance.setRepeaterOffset(+600);
        assertEquals(expResult, result);
    }

       /**
     * Test of setRepeaterOffset method, of class RadioHRD.
     */
    @Test
    public void testgetRadioID() {
        System.out.println("getRadioID");

        RadioControl instance=radio;
        String expResult="FT-897 (HRD)";
        String result = instance.getRadioID();
        assertEquals(expResult,result);
     
    }
}
