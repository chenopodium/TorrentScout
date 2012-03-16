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

import com.iontorrent.utils.io.FileUtils;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *typedef struct {
uint16_t rheader_length;   the number of bytes in the  
uint16_t name_length;   the number of characters in the name of the read (not including the null-terminator) 
uint32_t n_bases;   the number of bases in the read 
uint16_t clip_qual_left;   the 1-based coordinate of the first base after the (quality) left clipped region (zero if no clipping has been applied) 
uint16_t clip_qual_right;   the 1-based coordinate of the first base after the (quality) right clipped region (zero if no clipping has been applied) 
uint16_t clip_adapter_left;   the 1-based coordinate of the first base after the (adapter) left clipped region (zero if no clipping has been applied) 
uint16_t clip_adapter_right;   the 1-based coordinate of the first base after the (adapter) right clipped region (zero if no clipping has been applied) 
ion_string_t *name;   the read name  
} sff_read_header_t;
 * @author Chantal Roth
 */
public class SffReadHeader {

 
    /** uint16_t rheader_length;   the number of bytes in the   */
    int rheader_length;
    /** uint16_t name_length;   the number of characters in the name of the read (not including the null-terminator)  */
    int name_length;
    /** uint32_t n_bases;   the number of bases in the read  */
    long n_bases;
    /** uint16_t clip_qual_left;   the 1-based coordinate of the first base after the (quality) left clipped region (zero if no clipping has been applied)  */
    int clip_qual_left;
    /** uint16_t clip_qual_right;   the 1-based coordinate of the first base after the (quality) right clipped region (zero if no clipping has been applied)  */
    int clip_qual_right;
    /** uint16_t clip_adapter_left;   the 1-based coordinate of the first base after the (adapter) left clipped region (zero if no clipping has been applied)  */
    int clip_adapter_left;
    /** uint16_t clip_adapter_right;   the 1-based coordinate of the first base after the (adapter) right clipped region (zero if no clipping has been applied)  */
    int clip_adapter_right;
    /**  ion_string_t *name;   the read name  */
    String name;

    public SffReadHeader() {
        
    }

    protected int read(DataInputStream in) throws Exception {
        try {
            int n = 0;
            if (in.available() ==0) return -1;
            rheader_length = FileUtils.getUInt16(in);
            name_length = FileUtils.getUInt16(in);
            n_bases = FileUtils.getUInt32(in);
            clip_qual_left = FileUtils.getUInt16(in);
            clip_qual_right = FileUtils.getUInt16(in);
            clip_adapter_left = FileUtils.getUInt16(in);
            clip_adapter_right = FileUtils.getUInt16(in);
            n += FileUtils.UINT32 + 6 * FileUtils.UINT16;

            if (n_bases > 10000) {
                 Exception e = new Exception("DataInputStream: Too many bases, a read is not that long: "+n_bases+", check file pointer: "+toString());
                 err(e.getMessage(), e);
                 throw new Exception(e);
            }
            
            char[] namechars = new char[name_length + 1];
            for (int i = 0; i < Math.min(1000, name_length); i++) {
                namechars[i] = (char) in.readByte();
                n++;
            }
            name = new String(namechars);
            if (name_length > 200) {
                 Exception e = new Exception("DataInputStream: name length too large, a read name is not that long: "+name_length+", check file pointer: "+toString());
                 err(e.getMessage(), e);
                 throw new Exception(e);
            }
           
            
             n += FileUtils.readPadding(in, n);

       //     p("Got read header:"+toString()+"\n"+n+" bytes in header ");
            return n;

        } catch (IOException ex) {
            err("DataInputStream: Could not read sff header, eof ", ex);
        }
        return -1;
    }
    /** REFACTOR, use common data input if... */
protected int read(RandomAccessFile in) throws Exception {
        try {
            int n = 0;
            rheader_length = FileUtils.getUInt16(in);
            name_length = FileUtils.getUInt16(in);
            n_bases = FileUtils.getUInt32(in);
            clip_qual_left = FileUtils.getUInt16(in);
            clip_qual_right = FileUtils.getUInt16(in);
            clip_adapter_left = FileUtils.getUInt16(in);
            clip_adapter_right = FileUtils.getUInt16(in);
            n += FileUtils.UINT32 + 6 * FileUtils.UINT16;

           
            
            char[] namechars = new char[name_length + 1];
            for (int i = 0; i < Math.min(100, name_length); i++) {
                namechars[i] = (char) in.readByte();
                n++;
            }
            name = new String(namechars);
            if (name_length > 100) {
                 Exception e = new Exception("RandomAccessFile: name length too large, a read name is not that long: "+name_length+", check file pointer: "+toString());
                 throw new Exception(e);
            }
            if (n_bases > 1000) {
                 Exception e = new Exception("RandomAccessFile: Too many bases, a read is not that long: "+n_bases+", check file pointer: "+toString());
              //   err(e.getMessage(), e);
                 throw new Exception(e);
            }
//            if (n != rheader_length) {                
//                Exception e = new Exception("SFF Read header length " + rheader_length + " does not match read bytes " + n + ". Header is: " + toString());
//                err(e.getMessage());
//              //  throw new Exception(e);
//               
//            }
             n += FileUtils.readPadding(in, n);

       //     p("Got read header:"+toString()+"\n"+n+" bytes in header ");
            return n;

        } catch (IOException ex) {
            err("Could not read sff header, eof ", ex);
        }
        return -1;
    }
    @Override
    public String toString() {
        // I know + is slow, but toString is hardly ever used! :-)    
        String s = "name="+name + "\n";
        s += "rheader_length=" + rheader_length + "\n";
        s += "name_length=" + name_length + "\n";
        s += "n_bases=" + n_bases + "\n";
        s += "clip_qual_left=" + clip_qual_left + "\n";
        s += "clip_qual_right=" + clip_qual_right + "\n";
        s += "clip_adapter_left=" + clip_adapter_left + "\n";
        s += "clip_adapter_right=" + clip_adapter_right + "\n";
        return s;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(SffReadHeader.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(SffReadHeader.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(SffReadHeader.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
//  System.out.println("SffReadHeader: " + msg);
        //Logger.getLogger( SffReadHeader.class.getName()).log(Level.INFO, msg, ex);
    }
}
