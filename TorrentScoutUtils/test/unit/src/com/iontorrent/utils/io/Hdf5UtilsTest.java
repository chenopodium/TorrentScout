/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.utils.io;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Chantal Roth
 */
public class Hdf5UtilsTest {
    
    public Hdf5UtilsTest() {
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

    @Test
    public void testTest() {
        System.out.println("test");
        Hdf5Utils instance = new Hdf5Utils();
        instance.test();
     
    }
}
