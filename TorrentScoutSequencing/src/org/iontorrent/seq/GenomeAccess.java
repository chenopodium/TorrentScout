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
package org.iontorrent.seq;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class GenomeAccess {

    static String msg;
    public static DNASequence getSequence(String fastafile, long start, long end) {
        fastafile = fastafile.trim();
        File f = new File(fastafile);
        msg = null;
        if (!f.exists()) {
            msg = "Fasta file "+f+" not found";
            p("Parent "+f.getParentFile()+" exists? "+f.getParentFile().exists());
            err(msg);
            return null;
        }
        GenomeReadWriter r = GenomeReadWriter.getGenomeReadWriter(f);
        if (start < 0 || end > r.getFileSize()) {
            msg = "Positions are out of bounds :"+start+"-"+end;
            err(msg);
            end= Math.min(end, r.getFileSize());
            start = Math.max(0, start);
        }
        DNASequence seq = r.fetchSequence(start, end, true);
        return seq;
    }
    
    public String getErrorMsg() {
        return msg;
    }
    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(GenomeAccess.class.getName()).log(Level.SEVERE, msg, ex);
    }
    
    private static void err(String msg) {        
        Logger.getLogger(GenomeAccess.class.getName()).log(Level.SEVERE, msg);
    }
    
    private static void warn(String msg) {
        Logger.getLogger(GenomeAccess.class.getName()).log(Level.WARNING, msg);
    }
    
    private static void p(String msg) {
//  System.out.println("GenomeAccess: " + msg);
        //Logger.getLogger( GenomeAccess.class.getName()).log(Level.INFO, msg);
    }
}
