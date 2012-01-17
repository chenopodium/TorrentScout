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
package com.iontorrent.sequenceloading;

import com.iontorrent.sff.SffRead;
import com.iontorrent.threads.TaskListener;
import java.util.ArrayList;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Chantal Roth
 */
public class SequenceLoaderTest {
    
    SequenceLoader instance;
    String results_path = "S:/data/beverly/results/";
    String raw_path = "S:/data/beverly/raw/";
    String cache_dir = "S:/data/beverly/cache/";        
    int x = 344;
    int y = 547;
    long genomepos = 7884;
    
    
    public SequenceLoaderTest() {
    }

   
    @Before
    public void setUp() {
       // instance = SequenceLoader.getSequenceLoader(raw_path, cache_dir, results_path);
    }

     @Test
    public void testGetSffReads() {
        System.out.println("getSffReads with kmers");      
        TaskListener listener = null;
        
//        String sub = "GCCTTCG";
//        ArrayList<SffRead> result = instance.getSffReads(sub, listener, "fake.sff", false);
//        p("Got sff reads:"+result.size());
//        for (SffRead r : result) {
//            if (r.getBases().indexOf(sub)<0) {
//                Assert.fail("subsequence not found:"+r.toString());
//            }
//        }
//        
        
        String sub = "CCTTCGAAAA";
        ArrayList<SffRead> result = instance.getSffReads(sub, listener, true);
        p("Got sff reads:"+result.size());
        for (SffRead r : result) {
            if (r.getBases().indexOf(sub)<0) {
                Assert.fail("subsequence not found:"+r.toString());
            }
        }
    }


//    @Test
//    public void testGetSAM() {
//        System.out.println("getSAM");
//        ProgressHandle progress = null;
//        TaskListener listener = null;
//       
//        SAMRecord result = instance.getSAM(x, y, progress, listener, null);
//        p("Got sam: "+result);
//    }

//    @Test
//    public void testFindWellCoords() {
//        System.out.println("findWellCoords");
//        ArrayList result = instance.findWellCoords(genomepos);
//        p("Got well coords:"+result);
//    }
//
//    @Test
//    public void testGetSffRead() {
//        System.out.println("getSffRead");      
//        TaskListener listener = null;
//        
//        SffRead result = instance.getSffRead(x, y, listener, null);
//        p("Got sff read:"+result);
//    }

     private void p(String s) {
        System.out.println("TestSequenceLoader: "+s);
    }
}
