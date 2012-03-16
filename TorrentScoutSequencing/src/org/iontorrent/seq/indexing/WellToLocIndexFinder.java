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
public class WellToLocIndexFinder extends WellToLocIndexer {

    public WellToLocIndexFinder(String path, int hashcode) {
        super(path, hashcode);

    }

    private ArrayList<String> find(long hashcode) {
        if (!checkAndLoadFiles()) {
            return null;
        }

        return find(hashcode, false);
    }

    public ArrayList<ReadPos> findReads(String readname) {


        readname = readname.trim();
        long hashcode = getHashCode(readname);
        p("Finding reads for hashcode " + readname + "/" + hashcode);
        ArrayList<String> sr = find(hashcode);
        ArrayList<ReadPos> reads = new ArrayList<ReadPos>();

        if (sr == null || sr.size() < 1) {
            p("Found no reads for " + readname + " hashcode=" + hashcode);
        } else {

            for (String s : sr) {
                // bunch of reads...
            //    p("Got read list for readname "+readname+":"+s);
                // BUCKET \t [readname, pos, endpos,...] [ ] []
                ArrayList<String> items = StringTools.parseList(s, "\t");
                for (String readposline : items) {
                 //   p("Checking: "+readposline);
                    ReadPos rp = ReadPos.fromString(readposline);
                    if (rp != null && rp.readname.equalsIgnoreCase(readname)) {
                        p("Well To Loc: Adding rp:" + rp + ":" + readposline);
                        reads.add(rp);
                    }
                    else p("RP does not match: "+readname+" vs "+rp.readname);
                }
               
            }
        }

      //  p("Got " + reads.size() + ", well to locs");
        return reads;
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(WellToLocIndexFinder.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {

        Logger.getLogger(WellToLocIndexFinder.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(WellToLocIndexFinder.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
//  System.out.println("WellToLocIndexFinder: " + msg);
        //Logger.getLogger( WellToLocIndexFinder.class.getName()).log(Level.INFO, msg);
    }

    public boolean hasIndex() {
        File f = new File(this.getMergedIndex());
        File d = new File(this.getDictFile());
        boolean has= f.exists() && f.length() > 0 && d.exists() && d.length() > 0;
        if (!has) {
       //     p("Did not find welltolocindex: "+f+" in directory "+d);
        }
        return has;
    }

    public File getIndexFile() {
        return new File(this.getMergedIndex());
    }
}
