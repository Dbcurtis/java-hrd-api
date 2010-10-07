/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.dbcrd.radiopackage.omnirigserial;

import net.dbcrd.radiopackage.CommSetup;
import net.dbcrd.radiopackage.omnirigserial.BlkServerCmd.CmdType;

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
public class BlkServerCmdTest {

    /**
     *
     */
    public BlkServerCmdTest() {
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
     * Test of getCmd method, of class BlkServerCmd.
     */
    @Test
    public void testGetCmd() {
        System.out.println("getCmd");
        BlkServerCmd instance=new BlkServerCmd(BlkServerCmd.CmdType.REGISTER_PORT, "port");
        CmdType expResult=BlkServerCmd.CmdType.REGISTER_PORT;
        CmdType result=instance.getCmd();
        assertEquals(expResult, result);


    }

    /**
     * Test of getCmdObj method, of class BlkServerCmd.
     */
    @Test
    public void testGetCmdObj() {
        System.out.println("getCmdObj");
        BlkServerCmd instance=new BlkServerCmd(BlkServerCmd.CmdType.REGISTER_PORT, "port");
        Object expResult="port";
        Object result=instance.getCmdObj();
        assertEquals(expResult, result);


    }

}