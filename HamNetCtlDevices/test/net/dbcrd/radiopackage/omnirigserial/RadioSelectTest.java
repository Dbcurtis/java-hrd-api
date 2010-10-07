/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.dbcrd.radiopackage.omnirigserial;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.dbcrd.radiopackage.RadioControl;
import net.dbcrd.radiopackage.RadioControl;
import net.dbcrd.radiopackage.RadioSelect;
import net.dbcrd.radiopackage.RadioSelect;
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
public class RadioSelectTest {
static ExecutorService myWorkerPool=Executors.newCachedThreadPool();
    public RadioSelectTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        RadioSelect.setMyWorkerPool(myWorkerPool);
        RadioSelect.resetPreference();
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
     * Test of getRadioPref method, of class RadioSelect.
     */
    @Test
    public void testGetRadioPref() {
        System.out.println("getRadioPref");
        RadioControl expResult=null;
        RadioControl result=RadioSelect.getRadioPref();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of selectRadio method, of class RadioSelect.
     */
    @Test
    public void testSelectRadio() {
        System.out.println("selectRadio");
        RadioControl expResult=null;
        RadioControl result=RadioSelect.selectRadio();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of resetPreference method, of class RadioSelect.
     */
    @Test
    public void testResetPreference() {
        System.out.println("resetPreference");
        RadioSelect.resetPreference();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRadio method, of class RadioSelect.
     */
    @Test
    public void testGetRadio() {
        System.out.println("getRadio");
        String selectedRadioST="";
        RadioControl expResult=null;
        RadioControl result=RadioSelect.getRadio(selectedRadioST);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}

