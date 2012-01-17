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
import java.io.EOFException;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *typedef struct {
sff_header_t *gheader;   pointer to the global header 
sff_read_header_t *rheader;   pointer to the read header 
sff_read_t *read;   pointer to the read 
int32_t is_int;   1 if the bases are integer values, 0 otherwise 
} sff_t;
 * @author Chantal Roth
 */
public class Sff {

    private String file;
    /**  sff_header_t *gheader;   pointer to the global header */
    private SffGlobalFileHeader gheader;
    /**    sff_read_header_t *rheader;   pointer to the read header */
    private SffReadHeader curheader;
    /**    sff_read_t *read;   pointer to the read */
    private SffRead curread;
    /**    int32_t is_int;   1 if the bases are integer values, 0 otherwise */
    int is_int;
    private int readcount;
    private long filepointer;
    DataInputStream in;
    RandomAccessFile rin;

    public Sff(String file) {
        this.file = file;
    }

    public DataInputStream openFile() {
        readcount = 0;
        filepointer = 0;
        if (!FileUtils.exists(file)) {
            err("SFF File " + file + " not found");
            return null;
        }
        
        in = FileUtils.openFileOrUrl(file);
        return in;
    }
    public void closeFile() {
        if (in == null) return;
        try {
            in.close();
        }
        catch (Exception e) {}
    }
    public RandomAccessFile openRAFile() {
        readcount = 0;

        filepointer = 0;
        if (!FileUtils.exists(file)) {
            err("SFF File " + file + " not found");
            return null;
        }
        rin = FileUtils.openRAFile(new File(file));
        return rin;
    }

    public String getFlowOrder() {
        if (gheader != null) return gheader.flow;
        else return "";
    }
    public long readHeader() {
        gheader = new SffGlobalFileHeader();
        if (rin != null) {
            filepointer += gheader.read(rin);
        } else {
            filepointer += gheader.read(in);
        }

        //   p("Got global header:"+gheader+", fp="+filepointer);
        return filepointer;
    }

    public long getFilePointer() {
        return filepointer;
    }

    public void seek(long fp) {
        if (rin == null) {
            openRAFile();
        }
        try {
            rin.seek(fp);
            filepointer = fp;
        } catch (Exception e) {
            err("Could not seek to " + fp, e);
        }
    }

    public SffRead readNextRead() throws Exception {
        if (gheader == null) {
            Exception e = new Exception("readNextread: Must read global header first!");
            throw e;
        }
//        if (this.file.length() + 100 <= this.filepointer) {
//            p("reached end of file: file length: "+file.length()+"filepointer: "+this.filepointer);
//            return null;
//        }
        curheader = new SffReadHeader();
        int res = -1;
        try {
            if (rin != null) {
                res = curheader.read(rin);
            } else {
                res = curheader.read(in);
            }
        } catch (EOFException e) {
            p("reached end of file");
            return null;
        } catch (Exception e) {
            err(e.getMessage(), e);
            return null;
        }
        filepointer += res;
        if (curheader == null || res < 0) {
          //  err("Could not read read header");
            return null;
        }
        curread = new SffRead(gheader, curheader);
        if (rin != null) {
            filepointer += curread.read(rin);
        } else {
            filepointer += curread.read(in);
        }
        readcount++;
        return curread;
    }

    public int getReadCount() {
        return readcount;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(Sff.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(Sff.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(Sff.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("Sff: " + msg);
        //Logger.getLogger( Sff.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the file
     */
    public String getFile() {
        return file;
    }

    /**
     * @return the gheader
     */
    public SffGlobalFileHeader getGheader() {
        return gheader;
    }

    /**
     * @return the curheader
     */
    public SffReadHeader getCurheader() {
        return curheader;
    }

    /**
     * @param curheader the curheader to set
     */
    public void setCurheader(SffReadHeader curheader) {
        this.curheader = curheader;
    }

    /**
     * @return the curread
     */
    public SffRead getCurread() {
        return curread;
    }

    /**
     * @param curread the curread to set
     */
    public void setCurread(SffRead curread) {
        this.curread = curread;
    }
}
