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


import org.iontorrent.seq.sam.WellToSamIndex;
import com.iontorrent.utils.io.FileTools;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.*;
/**
 *
 * @author Chantal Roth
 */
public class SequenceReader {

    private File file;
    private String cache_dir;
    public SequenceReader(File file, String cache_dir) {
	this.file = file;
        this.cache_dir = FileTools.addSlashOrBackslash(cache_dir);
       
    }	
	
    public SAMRecord getSequenceByIndex(int x, int y) {
        String filename = file.getName()+".idx";
        File indexfile = new File(cache_dir+filename);
       WellToSamIndex index = new WellToSamIndex(file, indexfile);
       return index.findSequence(x, y);
        
    }
   

/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( SequenceReader.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( SequenceReader.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( SequenceReader.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("SequenceReader: " + msg);
        //Logger.getLogger( SequenceReader.class.getName()).log(Level.INFO, msg, ex);
    }
}
