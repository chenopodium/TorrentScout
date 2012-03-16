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
package org.iontorrent.seq.indexing;

import com.iontorrent.utils.StringTools;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class LocToReadIndexFinder extends LocToReadIndexer {

    
    public LocToReadIndexFinder(String path, int hashcode) {
        super(path, hashcode);
        
    }

    private ArrayList<String> find(long origpos) {
        if (!checkAndLoadFiles()) return null;
        origpos = origpos / this.BUCKET_SIZE;
        return find(origpos, false);
    }

    public ArrayList<ReadPos> findReads(long start, long end) {
        ArrayList<ReadPos> reads = new ArrayList<ReadPos>();
        for (long pos = start; pos < end + BUCKET_SIZE; pos += BUCKET_SIZE) {
            ArrayList<ReadPos> res = findReads(pos);
            for (ReadPos l : res) {
                if (!reads.contains(l)) {
                    reads.add(l);
                }
            }
        }
        return reads;
    }

    public boolean inside(long x, long a, long b) {
        return (x >= a && x <= b);
    }

    public ArrayList<ReadPos> findReads(long origpos) {
        
        
        long buck = origpos / LocToReadIndexer.BUCKET_SIZE;
      //  p("Finding reads for position/Bucket "+origpos+"/"+buck);
        ArrayList<String> sr = find(origpos);
        ArrayList<ReadPos> reads = new ArrayList<ReadPos>();

        if (sr == null || sr.size() < 1) {
           		p("Found no reads at BUCKET "+buck+":");
        } else {

            for (String s : sr) {
                // bunch of reads...
                //	p("Got read list for pos "+pos+":"+s);
                // BUCKET \t [readname, pos, endpos,...] [ ] []
                ArrayList<String> items = StringTools.parseList(s, "\t");
                for (String readposline : items) {
                    ReadPos rp = ReadPos.fromString(readposline);    
                    if (rp != null) {
                       //   p("Adding rp:"+rp+":"+readposline);
                        reads.add(rp);
                        // dummy check
                        if (rp.pos/BUCKET_SIZE != buck) {
                            err("REad pos is not in same bucket: "+rp+":, buck:"+rp.pos/BUCKET_SIZE+", should be in bucket "+buck);
                        }
                    }
                }
                //reads.add(Long.parseLong(s));
            }
        }
        ArrayList<ReadPos> res = new ArrayList<ReadPos>();
        if (reads.size() < 1) {
            p("Found no reads at bucket "+buck+", search result was: "+sr);
        } else {
            // now check the actual positions            
            for (ReadPos rp : reads) {
                if (inside(origpos, rp.pos, rp.endpos)) {
                    res.add(rp);
            //        p(origpos+" IS not inside "+rp.pos+"-"+rp.endpos);
                }
             //   p(origpos+" NOT inside "+rp.pos+"-"+rp.endpos);
            }
        }
    //    p("Got potential reads:"+reads.size()+", res: "+res.size());
        return res;
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(LocToReadIndexFinder.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {

        Logger.getLogger(LocToReadIndexFinder.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(LocToReadIndexFinder.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
//  System.out.println("LocToReadIndexFinder: " + msg);
        //Logger.getLogger( LocToReadIndexFinder.class.getName()).log(Level.INFO, msg);
    }

    public boolean hasIndex() {
        File f = new File(this.getMergedIndex());
        return f.exists();
    }
    public File getIndexFile() {
        return new File(this.getMergedIndex());
    }
}
