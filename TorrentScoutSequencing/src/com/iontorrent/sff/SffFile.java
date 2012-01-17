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

package com.iontorrent.sff;


import com.iontorrent.sff.index.WellToSffIndex;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *typedef struct {
    FILE *fp;   the file pointer from which to read/write 
    sff_header_t *header;   pointer to the global header 
    sff_index_t *index;   ponter to the SFF index if available 
    uint32_t mode;   file access mode 
} sff_file_t;
 * @author Chantal Roth
 */
public class SffFile {

  /**  FILE *fp;   the file pointer from which to read/write */
   String file;   
  /**  sff_header_t *header;   pointer to the global header */
   SffGlobalFileHeader header;
  /**  sff_index_t *index;   ponter to the SFF index if available */
   WellToSffIndex index;
  /**  uint32_t mode;   file access mode */
   long mode;
   
    public SffFile() {

    }

/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( SffFile.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( SffFile.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( SffFile.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("SffFile: " + msg);
        //Logger.getLogger( SffFile.class.getName()).log(Level.INFO, msg, ex);
    }
}
