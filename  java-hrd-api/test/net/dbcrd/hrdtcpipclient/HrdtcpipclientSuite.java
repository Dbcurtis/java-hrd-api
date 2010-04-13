/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.dbcrd.hrdtcpipclient;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author dbcurtis
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    net.dbcrd.hrdtcpipclient.RadioIdentificationTest.class,
    net.dbcrd.hrdtcpipclient.AbstractVirtualRadioTest.class,
    net.dbcrd.hrdtcpipclient.AbstractRadioTest.class, net.dbcrd.hrdtcpipclient.RadioStatusTest.class,
    net.dbcrd.hrdtcpipclient.RadioHRDTest.class})
    
public class HrdtcpipclientSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
}
