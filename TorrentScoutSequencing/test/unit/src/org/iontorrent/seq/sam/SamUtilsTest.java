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
package org.iontorrent.seq.sam;

import org.iontorrent.seq.alignment.Alignment;
import org.iontorrent.seq.indexing.ReadPos;
import java.io.File;
import java.util.ArrayList;
import net.sf.samtools.SAMRecord;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Chantal Roth
 */
public class SamUtilsTest {

    SamUtils instance;
    int x = 504;
    int y = 501;
    
    public SamUtilsTest() {
    }

    @Before
    public void setUp() {
        File samfile = new File("S:/data/beverly/results/fake.sam");
        File indexfile = new File("S:/data/beverly/results/fake.sam.idx");
     //   instance = new SamUtils(samfile, indexfile);
       
    }
//
//    @Test
//    public void testcreateReadLocationsIndexFile() {
//         System.out.println("createIndex");       
//        instance.createReadLocationsIndexFile();
//    }
    
     @Test
    public void testFindReads() {
        System.out.println("testFindReads");       
        
        ArrayList<ReadPos> res = instance.findReadsByGenomePos(7984);
        p("Got read pos: "+res);
    }
   
    
//    /**
//     * Test of createIndex method, of class SamUtils.
//     */
//    @Test
//    public void testCreateIndex() {
//        System.out.println("createIndex");       
//        instance.createIndex();
//        assertEquals(instance.hasSamIndex(), true);            
//    }

  
    /**
     * Test of getCommandLine method, of class SamUtils.
     */
//    @Test
//    public void testGetCommandLine() {
//        System.out.println("getCommandLine");
//        SAMRecord rec = instance.getSequenceByIndex(x, y);
//        String result = SamUtils.getCommandLine(rec);
//        p("command line: "+result);
//    }
//
//    /**
//     * Test of getFastaFile method, of class SamUtils.
//     */
//    @Test
//    public void testGetFastaFile() {
//        System.out.println("getFastaFile");
//       SAMRecord rec = instance.getSequenceByIndex(x, y);
//        String result = SamUtils.getFastaFile(rec);
//       p("Fasta file: "+result);
//    }

   
    /**
     * Test of getReferenceSequence method, of class SamUtils.
     */
//    @Test
//    public void testGetReferenceSequence() {
//        System.out.println("getReferenceSequence");
//        SAMRecord rec = instance.getSequenceByIndex(x, y);
//        DNASequence result = SamUtils.getReferenceSequence(rec);
//        p("ref seq:"+result);
//    }
//
//    /**
//     * Test of getReferenceNames method, of class SamUtils.
//     */
//    @Test
//    public void testGetReferenceNames() {
//        System.out.println("getReferenceNames");
//        ArrayList result = instance.getReferenceNames();
//        p("refrence names: "+result.toString());
//    }

    /**
     * Test of findRecords method, of class SamUtils.
     */
//    @Test
//    public void testFindRecords() {
//        System.out.println("findRecords");
//        long posInGenome = 0L;
//        String refname = instance.getReferenceNames().get(0);
//                
//        ArrayList result = instance.findRecords(posInGenome, refname);
//        p("result: "+result);
//    }

   
    
    private void p(String s) {
        System.out.println("TestSamUtils: "+s);
    }
}
