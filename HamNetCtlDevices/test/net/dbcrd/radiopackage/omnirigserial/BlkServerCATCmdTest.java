/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.dbcrd.radiopackage.omnirigserial;

import net.dbcrd.radiopackage.CommSetup;
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
public class BlkServerCATCmdTest {

    /**
     *
     */
    public BlkServerCATCmdTest() {
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
     * Test of getCmdObj method, of class BlkServerCATCmd.
     */
    @Test
    public void testGetCmdObj() {
        System.out.println("getCmdObj");
        BlkServerCATCmd instance=new BlkServerCATCmd(new byte[]{0,1,2,3,4});
        byte[] expResult=new byte[]{0,1,2,3,4};
        byte[] result=instance.getCmdObj();
        assertArrayEquals(expResult, result);
        

    }

}