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
public class RadioIdentificationTest {

    public RadioIdentificationTest() {
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
     * Test of values method, of class RadioIdentification.
     */
    @Test
    public void testValues() {
        System.out.println("values");
        RadioIdentification[] expResult={RadioIdentification.HRD,
        RadioIdentification.OMNI_RIG,RadioIdentification.YAESU_FT8x7};
        RadioIdentification[] result=RadioIdentification.values();
        assertArrayEquals(expResult, result);
        assertEquals(expResult.length,result.length);

    }

    /**
     * Test of valueOf method, of class RadioIdentification.
     */
    @Test
    public void testValueOf() {
        System.out.println("valueOf");
        String[] names={
            "HRD", "OMNI_RIG", "YAESU_FT8x7"};
        RadioIdentification[] eresult={
            RadioIdentification.HRD, RadioIdentification.OMNI_RIG, RadioIdentification.YAESU_FT8x7
        };
        for(int i=0; i<names.length; i++){
            String name=names[i];
            RadioIdentification expResult=eresult[i];
            RadioIdentification result=RadioIdentification.valueOf(name);
            assertEquals(expResult, result);
        }
        assertEquals(names.length,eresult.length);
    }

    /**
     * Test of toString method, of class RadioIdentification.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        String expResult="Ham Radio Deluxe Server//Omni Rig Server//FT-8x7//";
        RadioIdentification[] ids=RadioIdentification.values();
        StringBuilder sb = new StringBuilder();
        for (RadioIdentification rid:RadioIdentification.values()){
            sb.append(rid.toString()).append("//");
        }
        String result = sb.toString();
        assertEquals(expResult, result);

    }

    /**
     * Test of isVirtual method, of class RadioIdentification.
     */
    @Test
    public void testIsVirtual() {
        System.out.println("isVirtual");
        RadioIdentification[] ids=RadioIdentification.values();
        boolean results[] = {true,true,false};
        assertEquals(results.length,ids.length);
        for (int i=0;i<ids.length;i++){
           assertEquals(results[i],ids[i].isVirtual());
        }

    }

}