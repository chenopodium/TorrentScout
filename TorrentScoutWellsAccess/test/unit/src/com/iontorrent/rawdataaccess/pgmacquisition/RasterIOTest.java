/*
*	Copyright (C) 2011 Life Technologies Inc.
*
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 2 of the License, or
*   (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.rawdataaccess.pgmacquisition;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Chantal Roth
 */
public class RasterIOTest {
    
//    static String raw_dir = "S:\\data\\318_04_14_GAT_321_ecoli\\";
//    static  String cache_dir = "S:\\data\\318_04_14_GAT_321_ecoli\\cache\\";        
    
    static String raw_dir =  "S:\\data\\R_2011_05_10_STO_276\\";
    static  String cache_dir = "S:\\data\\R_2011_05_10_STO_276\\";
    int x = 510;
    int y = 510;
    RawType filetype;
    int startflow = 0;
    RasterIO instance;
    String file;
    
    public RasterIOTest() {
        filetype = RawType.ACQ;
        instance = new RasterIO(raw_dir, cache_dir, filetype);
        file = instance.getRawFilePath(filetype, raw_dir, startflow);        
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

   
//
//    @Test
//    public void testReadAcquisition() {
//        System.out.println("readAcquisition");
//        
//        long t0 = System.currentTimeMillis();
//        PGMAcquisition result = instance.readAcquisition(file, flow, null)
//        p("Read acq:"+result.toString()+":"+result.getNrRows()+" rows");
//        int data [] = result.getDataForPos(x, y);
//        p("Data for "+x+"/"+y+":"+Arrays.toString(data));
//        
////       PGMAcquisition test = instance.readAcquisition(file);
////       int exp [] = result.getDataForPos(x, y);
////       p("Data for "+x+"/"+y+":"+Arrays.toString(exp));
////       assertEquals(Arrays.toString(exp), Arrays.toString(data));
//     
//          p("total memory available: "+Runtime.getRuntime().totalMemory()/1000000+"M");
//        p("free memory available:  "+Runtime.getRuntime().freeMemory()/1000000+"M");
//     
//         long dt = System.currentTimeMillis() - t0;
//         p("Time in secs: "+dt/1000);
//    }

    private void p(String s) {
        System.out.println("RasterIOTest: "+s);
    }

  
}
