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

import java.util.Arrays;
import org.junit.Test;

/**
 *
 * @author Chantal Roth
 */
public class PGMAcquisitionTest {

    static String raw_dir = "S:\\data\\314a\\raw\\";
    static String test_dir = "S:\\data\\corners\\orig\\";
    PGMAcquisition pgm;
    PGMAcquisition pgmcheck;
    int startflow = 0;
    String file = "acq_0000.dat";

    public PGMAcquisitionTest() {
        p("Testing file " + raw_dir + file);
        pgm = new PGMAcquisition(raw_dir + file, startflow);

//        pgmcheck = new PGMAcquisition(test_dir + file, startflow);
    }

//    @Test
//    public void testVega() {
//        p("readFile 0,0, - 5, 5 ");
//        pgm.readFile(0, 0, 10, 10, true);
//        p(pgm.toString());
//
//        int x = 0;
//        int y = 0;
//        int[] res = pgm.getDataForPos(x, y);
//        p("x=" + x + ", y=" + y + ":" + Arrays.toString(pgm.getDataForPos(x, y)));
//        p("0 ="+res[0]);
//        p("41="+res[41]);
//        p("42="+res[42]);
//        p("43="+res[43]);
//        p("44="+res[44]);
//        
//    }

//    @Test
//    public void testReadFile_4args() {
//        p("testReadFile_4args");
//
//        int d = 1;
//        int startx = 982;
//        int starty = 1340;
//
//        boolean ok = pgm.readFile(startx, starty, d, d, true);
//        pgm.closeFile();
//        if (!ok) {
//            System.exit(-1);
//        }
//        p(pgm.toString());
//        p("Data at:"+ startx+"/"+starty+":"+Arrays.toString(pgm.getDataForPos(startx, starty)));
//      
//
//    }
//   @Test
//    public void testReadOld() {
//       p("readFile "+test_dir+file);        
//        
//        pgmcheck.readFile(400, 400, 10, 10);        
//        p(pgmcheck.toString());
//        for (int x = 0; x < 4; x+=1) {
//            for (int y = 0; y < 4; y+=1) {        
//                p("x="+x+", y="+y+":"+Arrays.toString(pgmcheck.getDataForPos(x, y)));
//                
//            }
//        }
//        
//    }
//    @Test
//    public void testReadFile_4args() {
//       p("readFile");    
//       int startx =0;
//        int starty = 0;
//        int dx = 100;
//        int dy = 100;
//        pgm.readFile(startx, starty, dx, dy);        
//        p(pgm.toString());
//   //      PGMAcquisition pgmcheck=  new PGMAcquisition(test_dir+file, startflow);
//    //   pgmcheck.readFile(startx, starty, dx, dy);
//     //   p(pgmcheck.toString());
//        for (int x = 0; x < 2; x+=1) {
//            for (int y = 0; y < 2; y+=1) {        
//                p("v3 comp: x="+x+", y="+y+":"+Arrays.toString(pgm.getDataForPos(x, y)));
//       //         p("uncomp:  x="+x+", y="+y+":"+Arrays.toString(pgmcheck.getDataForPos(x, y)));
//                p("");
//            }
//        }
//        
//    }
    @Test
    public void testReadFile_0args() {
        p("readFile new ");
        pgm.readFile(0, 0, 100, 100, false);
    //    p(pgm.toString());

     //   pgmcheck.readFile(0, 0, 100, 100, true);
    //    p(pgmcheck.toString());
        int dx = 5;
        int dy = 5;
        for (int xx = 0; xx < 2; xx += 1) {
            for (int yy = 0; yy < 2; yy += 1) {
                int x = xx + dx;
                int y = yy + dy;
                p("v3 corner: x=" + x + ", y=" + y + ":" + Arrays.toString(pgm.getDataForPos(x, y)));
            //    p("orig     : x=" + x + ", y=" + y + ":" + Arrays.toString(pgmcheck.getDataForPos(x, y)));
                p("");
            }
        }

    }
//      @Test
//    public void testSpeedTest_4args() {
//        p("testSpeedTest_4args");
//       // int starty = 110;
//        int startx =410;
//        int starty = 400;
//        int dx = 100;
//        int dy = 100;
//      
//        long t0 = System.currentTimeMillis();
//        pgm.readFile(startx, starty, dx, dy);
//        long treg = System.currentTimeMillis()-t0;
//        p("\n"+pgm.toString());
//        p("Frame type: "+pgm.getFrameType());
//        p("Time to read REGION file: "+treg+" milliseconds");
//        t0 = System.currentTimeMillis();        
//      //  pgmcheck.readFile(startx, starty, dx, dy);
//        long tfull = System.currentTimeMillis()-t0;
//      //  p("\n"+pgmcheck.toString());
//      //  p("Frame type: "+pgmcheck.getFrameType());
//     //   p("Time to read FULL file: "+tfull+" milliseconds");
//      //  p("Time in percent of reading full file: "+treg*100/tfull+"%");
////         for (int x = startx; x < startx+1; x+=1) {
////            for (int y = starty; y < starty+1; y+=1) {        
////                p("v4 comp: x="+x+", y="+y+":"+Arrays.toString(pgm.getDataForPos(x, y)));
////                p("uncomp:  x="+x+", y="+y+":"+Arrays.toString(pgmcheck.getDataForPos(x, y)));
////                p("");
////            }
////        }
//    }
//    @Test
//    public void testReadFile_4args() {
//        p("testReadFile_4args");
//  //       p("read entire file first");        
////        pgm.readFile();        
////        p(pgm.toString());
////        pgm.closeFile();;
////        pgm = new PGMAcquisition(raw_dir+file, startflow);
//        p("Now reading subregions");
//         
//       // int starty = 110;
//        int d = 256;
//        for (int startx = 2500; startx < pgm.getNrCols()-d; startx+=d) {         
//            for (int starty = 500; starty < pgm.getNrRows(); starty+=d) {
//            
//                  boolean ok = pgm.readFile(startx, starty, d, d);
//                  pgm.closeFile();
//                  if (!ok) {
//                      System.exit(-1);
//                  }
//                  p(pgm.toString());
//                  
//                  pgm = new PGMAcquisition(raw_dir+file, startflow);
//            }
//        }
//      
//      
//    }
    private void p(String s) {
        System.out.println("Test: " + s);
    }
}
