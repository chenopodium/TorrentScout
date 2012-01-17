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
typedef struct {
uint32_t magic;  !< the magic number for this file 
uint32_t version;  !< the version number 
uint64_t index_offset;  !< not currently used (value is 0) 
uint32_t index_length;  !< not currently used (value is 0) 
uint32_t n_reads;  !< the number of reads in the file 
uint16_t gheader_length;  !< the number of bytes in the global header including padding 
uint16_t key_length;  !< the length of the key sequence used with these reads 
uint16_t flow_length;  !< the number of nucleotide flows used in this experiment 
uint8_t flowgram_format;  !< the manner in which signal values are encoded (value is 1) 
ion_string_t *flow;  !< the string specifying the ith nucleotide flowed  
ion_string_t *key;  !< the string specifying the ith nucleotide of the sequence key 
} sff_header_t;
@author Chantal Roth
 */
public class SffGlobalFileHeader {

    static final long SFF_MAGIC = 0x2E736666;
    static final long SFF_VERSION = 1;
    public static final long SFF_INDEX_VERSION = 1;
    public static final long SFF_INDEX_MAGIC = 0xDEADBEEF;
    /** uint32_t magic;  !< the magic number for this file  */
    long magic;
    /**  uint32_t version;  !< the version number   */
    long version;
    /**  uint64_t index_offset;  !< not currently used (value is 0)  */
    long index_offset;
    /**  uint32_t index_length;  !< not currently used (value is 0)   */
    long index_length;
    /**  uint32_t n_reads;  !< the number of reads in the file  */
    long n_reads;
    /**   uint16_t gheader_length;  !< the number of bytes in the global header including padding  */
    int gheader_length;
    /**  uint16_t key_length;  !< the length of the key sequence used with these reads   */
    int key_length;
    /** uint16_t flow_length;  !< the number of nucleotide flows used in this experiment   */
    int flow_length;
    /** uint8_t flowgram_format;  !< the manner in which signal values are encoded (value is 1)  */
    int flowgram_format;
    /**  ion_string_t *flow;  !< the string specifying the ith nucleotide flowed   
    typedef struct {
    size_t l;  !< the length of the string 
    size_t m;  !< the memory allocated forthis string 
    char *s;  !< the pointer to the string 
    } ion_string_t; */
    String flow;
    /**  ion_string_t *key;  !< the string specifying the ith nucleotide of the sequence key  */
    String key;
 

    public SffGlobalFileHeader() {
    }

    /** h->gheader_length = 0;
    h->gheader_length += 1 * sizeof(uint8_t);
    h->gheader_length += 3 * sizeof(uint16_t);
    h->gheader_length += 4 * sizeof(uint32_t);
    h->gheader_length += 1 * sizeof(uint64_t);
    h->gheader_length += (h->flow_length + h->key_length) * sizeof(char);
    if(0 != (h->gheader_length & 7)) {
    h->gheader_length += 8 - (h->gheader_length & 7); // 8 - (n % 8) -> add padding
    }*/
    public void calcBytes() {
        gheader_length = FileUtils.UINT8 + 3 * FileUtils.UINT16 + 4 * FileUtils.UINT32 + FileUtils.UINT64;
        gheader_length += (flow_length + key_length) * FileUtils.CHAR;
        if ((gheader_length % 8) != 0) {
            gheader_length += 8 - (gheader_length * 8); // 8 - (n % 8) -> add padding
        }
    }

    protected int read(DataInputStream in) {
        int n = 0;
        try {
            magic = FileUtils.getUInt32(in);
            version = FileUtils.getUInt32(in);
            index_offset = FileUtils.getUInt64(in);
            index_length = FileUtils.getUInt32(in);
            n_reads = FileUtils.getUInt32(in);
            gheader_length = FileUtils.getUInt16(in);
            key_length = FileUtils.getUInt16(in);
            flow_length = FileUtils.getUInt16(in);
            flowgram_format = FileUtils.getUInt8(in);

            n += 4 * FileUtils.UINT32 + FileUtils.UINT64 + 3 * FileUtils.UINT16 + FileUtils.UINT8;
            if (magic != SFF_MAGIC) {
                err("SFF magic " + magic + " did not match " + SFF_MAGIC + ". Header is:" + toString());
            }
            if (version != SFF_VERSION) {
                err("SFF version " + version + " did not match " + SFF_VERSION + ". Header is:" + toString());
            }
            char[] flowchars = new char[flow_length + 1];
            char[] keychars = new char[key_length + 1];

            for (int i = 0; i < flow_length; i++) {
                byte b = in.readByte();
                flowchars[i] = (char) b;
            }
            for (int i = 0; i < key_length; i++) {
                byte b = in.readByte();
                keychars[i] = (char) b;
            }
            flow = new String(flowchars);
            key = new String(keychars);
          //  p("Got flow:" + flow);
        //    p("Got key: " + key);
            n += flow_length + key_length;
            if (n != gheader_length) {
            //    err("SFF Global header length " + gheader_length + " does not match read bytes " + n + ". Header is: " + toString());
            }
            n += FileUtils.readPadding(in, n);
            
            return n;

        } catch (IOException ex) {
            err("Could read global sff header ", ex);
        }
        return -1;

    }
    public String getFlowOrder() {
        return flow;
    }
     protected int read(RandomAccessFile in) {
        int n = 0;
        try {
            magic = FileUtils.getUInt32(in);
            version = FileUtils.getUInt32(in);
            index_offset = FileUtils.getUInt64(in);
            index_length = FileUtils.getUInt32(in);
            n_reads = FileUtils.getUInt32(in);
            gheader_length = FileUtils.getUInt16(in);
            key_length = FileUtils.getUInt16(in);
            flow_length = FileUtils.getUInt16(in);
            flowgram_format = FileUtils.getUInt8(in);

            n += 4 * FileUtils.UINT32 + FileUtils.UINT64 + 3 * FileUtils.UINT16 + FileUtils.UINT8;
            if (magic != SFF_MAGIC) {
                err("SFF magic " + magic + " did not match " + SFF_MAGIC + ". Header is:" + toString());
            }
            if (version != SFF_VERSION) {
                err("SFF version " + version + " did not match " + SFF_VERSION + ". Header is:" + toString());
            }
            char[] flowchars = new char[flow_length + 1];
            char[] keychars = new char[key_length + 1];

            for (int i = 0; i < flow_length; i++) {
                byte b = in.readByte();
                flowchars[i] = (char) b;
            }
            for (int i = 0; i < key_length; i++) {
                byte b = in.readByte();
                keychars[i] = (char) b;
            }
            flow = new String(flowchars);
            key = new String(keychars);
         //   p("Got flow:" + flow);
          //  p("Got key: " + key);
            n += flow_length + key_length;
            if (n != gheader_length) {
          //      err("SFF Global header length " + gheader_length + " does not match read bytes " + n + ". Header is: " + toString());
            }
            n += FileUtils.readPadding(in, n);
            
            return n;

        } catch (IOException ex) {
            err("Could read global sff header ", ex);
        }
        return -1;

    }

    public String toString() {
        // I know + is slow, but toString is hardly ever used! :-)    
        String s = "magic=" + magic + "\n";
        s += "version=" + version + "\n";
        s += "n_reads=" + n_reads + "\n";
        s += "gheader_length=" + gheader_length + "\n";
        s += "key_length=" + key_length + "\n";
        s += "flow_length=" + flow_length + "\n";
        s += "flowgram_format=" + flowgram_format + "\n";
        return s;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(SffGlobalFileHeader.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(SffGlobalFileHeader.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(SffGlobalFileHeader.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("SffGlobalFileHeader: " + msg);
        //Logger.getLogger( SffGlobalFileHeader.class.getName()).log(Level.INFO, msg, ex);
    }
}
