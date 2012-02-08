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
package com.iontorrent.sff.index;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.sff.Sff;
import com.iontorrent.sff.SffRead;
import com.iontorrent.utils.io.FileUtils;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *typedef struct {
uint32_t index_magic_number;   the magic number of the index 
uint32_t index_version;   the version of the index 
int32_t num_rows;   the number of rows 
int32_t num_cols;   the number of cols 
int32_t type;   the SFF index type 
uint64_t *offset;  the absolute byte offset of the readIndex in the file, in row-major order 
} sff_index_t;
 * @author Chantal Roth
 */
public class WellToSffIndex {

    private ExperimentContext exp;
    static final int SFF_INDEX_ROW_ONLY = 0; /* only the offsets of the rows are stored */

    static final int SFF_INDEX_ALL = 1; /*the offsets of all reads are stored */

    /** uint32_t index_magic_number;   the magic number of the index  */
    long index_magic_number;
    /** uint32_t index_version;   the version of the index  */
    long index_version;
    /**    int32_t num_rows;   the number of rows  */
    int num_rows;
    /**    int32_t num_cols;   the number of cols  */
    int num_cols;
    /**    int32_t type;   the SFF index type  */
    int type;
    /**    uint64_t *offset;  the absolute byte offset of the readIndex in the file, in row-major order      */
    long[][] offset;
    String sfffile;
    File indexfile;
    private String error;
    Exception ex;

    public WellToSffIndex(String sfffile, File indexfile, ExperimentContext exp) {
        this.sfffile = sfffile;
        this.indexfile = indexfile;
        this.exp = exp;
    }

    public Exception getException() {
        return ex;
    }

    public String getError() {
        return error;
    }

    public boolean readIndex() {
        // p("Reading index for " + sfffile);

        if (!hasIndex()) {
            p("Index file " + indexfile + " does not exist yet");
            return false;
        }
        DataInputStream in = FileUtils.openFile(indexfile);
        if (in == null) {
            err("Input stream is null for " + this.indexfile);
            return false;
        }
        readIndex(in);
        // check index
        if (this.countReads() < 1) {
            err("Got no reads in indexfile " + indexfile + ", will try to create");
            indexfile.delete();
            return false;

        }
        return true;
    }

    public boolean hasIndex() {
        return indexfile.exists();
    }

    public SffRead findRead(int row, int col) {
        ex = null;
        error = null;
        if (row - exp.getRowOffset() >= 0) {
            row = row - exp.getRowOffset();
        }
        if (col - exp.getColOffset() >= 0) {
            col = col - exp.getColOffset();
        }
        try {
            p("Findread row="+row+", col="+col);
            if (offset == null) {
                err("Must read index first");
                if (!readIndex()) {
                    return null;
                }
            } else if (offset.length < 1) {
                err("Offset length is too small: " + offset.length);
                return null;

            }
            if (row > offset.length || col > offset[0].length || row < 0 || col < 0) {
                err("col/row " + col + "/" + row + " out of bounds. Maximum is: cols=" + offset[0].length + "/rows" + offset.length);
                return null;
            }
            long fp = offset[row][col];
            if (fp <= 0) {
                p("Offset is 0 for readIndex row/col=" + row + "/" + col + " -> we got no data");
                int cnt = this.countReads();
                p("There are "+cnt+" reads with positive offset");
                return null;
            }
            p("Offset of row:" + row + " and col: " + col + " is " + fp);
            //      p("find Read at " + row + "/" + col+" in "+sfffile);
            Sff sff = new Sff(sfffile);
            sff.openFile();
            sff.readHeader();
            if (sff.getGheader() == null) {
                err("Could not read header for some reason...");
                return null;
            }
            p("Moving fp to  " + fp);
            sff.seek(fp);
            SffRead read = null;
            try {
                read = sff.readNextRead();
            } catch (Exception e) {
                err("find Read at " + row + "/" + col + " got an error:" + e);


            }
            if (read == null) {
                err("Could not read at " + fp + " for row/col=" + row + "/" + col + ", got null. \n Indexfileis=" + this.indexfile + "\n.sff file is=" + this.sfffile);

                return null;
            }
            // p("readIndex to" + readIndex);
            read.parseWellLocationFromName();
            if (read.getRow() != row || read.getCol() != col) {
                err("Coordinates do not match:" + read.toString() + " vs " + row + "/" + col);
            }
            return read;
        } catch (Exception ex) {
            this.ex = ex;
            Logger.getLogger(WellToSffIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    protected void readIndex(DataInputStream in) {
        int r = 0;
        int c = 0;
        if (in == null) {
            err("Input stream is null for " + this.indexfile);
            return;
        }
        try {
            index_magic_number = in.readLong();
            index_version = in.readLong();
            num_rows = in.readInt();
            num_cols = in.readInt();
            type = in.readInt();
            offset = new long[num_rows][num_cols];

            p("readIndex: Row/cols:" + num_rows + "/" + num_cols);
            int count = 0;
            int reads = 0;
            for (r = 0; r < num_rows; r++) {
                for (c = 0; c < num_cols; c++) {
                    try {
                        offset[r][c] = in.readLong();
                        count++;
                        if (offset[r][c] > 0) {
                            reads++;
                        }
                    } catch (EOFException e) {
                        r = num_rows;
                        c = num_cols;
                        break;
                    }
                }
            }
            p("Got index: " + count + " values, and " + reads + " reads that are not zero. First offset[0][0]: " + offset[0][0]);

        } catch (Exception ex) {
            err("Could NOT read entire sff index, read to" + r + "/" + c, ex);
        }

    }

    public boolean createIndex(int nrrows, int nrcols) throws Exception {
        try {
            return createIndex(nrrows, nrcols, SFF_INDEX_ALL);
        } catch (Exception e) {
            err("Got error:" + e.getMessage(), e);
            return false;
        }

    }

    public int countReads() {
        int cnt = 0;

        for (int r = 0; r < this.num_rows; r++) {
            for (int c = 0; c < this.num_cols; c++) {
                if (offset[r][c] > 0) {
                    if (cnt == 0) p("Got positive offset at r/c="+r+"/"+c);
                    cnt++;
                }
            }
        }

        return cnt;
    }

    public boolean testIndex(int nr) {
        int cnt = 0;

        p("============================== testing sff index ");
        p(this.toString());
        for (int r = 50; cnt < nr && r < this.num_rows; r++) {
            for (int c = 50; cnt < nr && c < this.num_cols; c++) {
                if (offset[r][c] > 0) {
                    cnt++;
                    p("testing read " + cnt + ", at file offset " + offset[r][c]);
                    SffRead read = null;
                    try {
                        read = findRead(r, c);
                        p("Got read: " + read.getName() + " at c/r=" + read.getCol() + "/" + read.getRow() + ", seq:" + read.getBases());
                    } catch (Exception e) {
                        err("Failed to read " + r + "/" + c + ":" + e);
                        return false;
                    }
                }
            }
        }
        p("============================== testing sff index DONE");
        return true;
    }

    private boolean createIndex(int nrrows, int nrcols, int type) throws Exception {

        if (nrcols < 1) {
            err("Got no nrcols! Using 2048");
            nrcols = 2048;

        }
        if (nrrows < 1) {
            err("Got no nrrows! Using 2048");
            nrrows = 2048;

        }
        p("Creating index " + indexfile + " for file " + sfffile + ",  nrrows=" + nrrows + ", nrcols=" + nrcols + ", type=" + type);

        int row = 0;
        int col = 0;

        this.num_rows = nrrows;
        this.num_cols = nrcols;

        //  p("CREATING NEW OFFSET DATA STRUCTURE");
        offset = new long[nrrows][nrcols];

        Sff sff = new Sff(sfffile);
        if (sff == null) {
            err("Could not open file " + sfffile);
            return false;
        }
        sff.openFile();
        long prev_pos = sff.readHeader();
        //   p("sff global header:" + sff.getGheader().toString());
        if (prev_pos <= 0) {
            err("fp after opening sff " + sfffile + " is " + prev_pos);
            return false;
        }
        int nr = 0;
        boolean done = false;
        int count = 0;
        int errors = 0;
        while (!done) {
            nr++;
            if (nr % 50000 == 0) {
                p("Processing read " + (nr));
            }
            SffRead sffread = null;
            try {
                sffread = sff.readNextRead();

            } catch (Exception e) {
                err("got an error:" + e);
                done = true;

            }

            if (sffread == null) {
                p("We got null and are DONE for readIndex " + nr);
                done = true;
            } else {

                // get the row/col co-ordinates
                if (!sffread.parseWellLocationFromName()) {
                    err("could not understand the read name " + sffread.getName());
                }

                row = sffread.getRow();
                col = sffread.getCol();

                if (nr % 50000 == 0) {
                    p("read name " + sffread.getName() + "->r=" + row + "/c=" + col + ", offset=" + prev_pos);
                }
                if (row >= offset.length || col >= offset[0].length) {
                    if (errors < 10) {
                        p("read name " + sffread.getName() + " -> r=" + row + "/c=" + col + ", but index nr rows/cols is: " + offset.length + "/" + offset[0].length);
                    }
                    p("enlarging index");
                    if (row >= offset.length) {
                        nrrows = row * 2;
                    } else {
                        nrcols = col * 2;
                    }
                    this.num_rows = nrrows;
                    this.num_cols = nrcols;
                    long tmp[][] = new long[nrrows][nrcols];
                    for (int r = 0; r < offset.length; r++) {
                        for (int c = 0; c < offset[0].length; c++) {
                            tmp[r][c] = offset[r][c];
                        }
                    }
                    offset = tmp;
                }
                if (row < 0 || col < 0 || row >= offset.length || col >= offset[0].length) {
                    if (errors < 10) {
                        p("read name " + sffread.getName() + " -> r=" + row + "/c=" + col + ", but index nr rows/cols is: " + offset.length + "/" + offset[0].length);
                    }
                    errors++;
                } else {
                    offset[row][col] = prev_pos;
                    if (offset[row][col] > 0) {
                        count++;
                    }

                }
                if (prev_pos <= 0) {
                    if (errors < 10) {
                        err("read name " + sffread.getName() + "->r=" + row + "/c=" + col + ", has no pos: " + prev_pos + ", read is: " + sffread.toString());
                    }
                    errors++;
                }
                prev_pos = sff.getFilePointer();
                if (-1L == prev_pos) {
                    errors++;
                    err("Error getting file pointer from sff");
                }
            }
        }
        p("Got " + count + " reads with a positive offset processed " + nr + " reads, writing it");
        return writeIndex(indexfile);

    }

    private boolean writeIndex(File indexfile) {
        DataOutputStream out = FileUtils.openOutputStream(indexfile);
        if (out == null) {
            err("Could not write index");
            return false;
        }

        try {
            out.writeLong(index_magic_number);
            out.writeLong(index_version);
            out.writeInt(num_rows);
            out.writeInt(num_cols);
            out.writeInt(type);

            for (int r = 0; r < num_rows; r++) {
                for (int c = 0; c < num_cols; c++) {
                    long val = offset[r][c];
                    out.writeLong(val);
                }
                out.flush();
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            err("Could not write index " + indexfile, e);
        }
        p("Wrote index " + indexfile);
        return true;
    }

    @Override
    public String toString() {
        // I know + is slow, but toString is hardly ever used! :-)    
        String s = "file:" + sfffile + "\n";
        s += "index file:" + this.indexfile + "\n";
        s += "num_rows=" + num_rows + "\n";
        s += "num_cols=" + num_cols + "\n";
        s += "type=" + type + "\n";

        return s;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        this.ex = ex;
     //   System.out.println("SffIndex: " + msg);
        Logger.getLogger(WellToSffIndex.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        error = msg;
        Logger.getLogger(WellToSffIndex.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
   //     System.out.println("SffIndex: " + msg);
        Logger.getLogger(WellToSffIndex.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        //   System.out.println("SffIndex: " + msg);
   //     Logger.getLogger(WellToSffIndex.class.getName()).log(Level.INFO, msg);
    }

    public String getSffFile() {
        return this.sfffile;
    }

    public String getSffIndexFile() {
        return indexfile.toString();
    }
}
