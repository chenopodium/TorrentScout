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
package com.iontorrent.rawdataaccess.wells;

import com.iontorrent.utils.io.FileUtils;
import com.iontorrent.wellmodel.WellCoordinate;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *The wells fileOrUrl format stores the estimated incorporation signals for each flow of each well of the chip.
 * It is a binary format consisting of a header section followed by a data section, stored in little-endian order. 
 * The section contents are outlined below.
A common question about the wells format is how to tell the total number of rows and columns. 
 * The number of rows and columns are not stored in the wells format, 
 * they can be found in the bfmask.bin fileOrUrl associated with each wells fileOrUrl.

 * @author Chantal Roth
 */
public class BfMask implements Serializable{

    /** the associated fileOrUrl */
    private String fileOrUrl;
    /** the input stream */
    private transient DataInputStream in;
    /** the header information - see subclass @Header below for description */
    private Header header;
    /** data stored in [x][y] format - see subclass @Data below for descriptions */
    // private BfMaskDataPoint[][] alldata;
    private int[][] alldata;
    private String name;

    public BfMask(){
        
    }
    public BfMask(String file) {
        this.fileOrUrl = file;
        if (!FileUtils.exists(fileOrUrl)) {
            err("File " + file + " does not exist");
        }
        in = FileUtils.openFileOrUrl(file);

    }

    public BfMask(int cols, int rows) {
        header = new Header();
        header.nCols = cols;
        header.nRows = rows;
        alldata = new int[cols][rows];
    }

    public ArrayList<BitMask> createMasks(int x0, int y0, int dx, int dy) {
        ArrayList<BitMask> res = new ArrayList<BitMask>();

        BitMask pinned = null;
        BitMask empty = null;
        BitMask ignore = null;
        BitMask bead = null;
        BitMask keypass = null;
        WellCoordinate relcoord = new WellCoordinate(x0, y0);
        p("Creating masks from bfmask at REL coord of this block/chip "+x0+"/"+y0);
        if (this.getNrCols()<1) {
            err("Cannot fill mask... got no columsn...???");
                    
        }
        for (BfMaskFlag flag : BfMaskFlag.DEFAULT_MASKS) {
            BitMask bit = new BitMask(relcoord, dx, dy);
            bit.setName(flag.getName());
            if (flag == BfMaskFlag.PINNED) {
                pinned = bit;
            } else if (flag == BfMaskFlag.EMPTY) {
                empty = bit;
            } else if (flag == BfMaskFlag.IGNORE) {
                ignore = bit;
            } else if (flag == BfMaskFlag.KEYPASS) {
                keypass = bit;
            } else if (flag == BfMaskFlag.BEAD) {
                bead = bit;
            }

            for (int x = x0; x < x0 + dx && x < this.getNrCols(); x++) {
                for (int y = y0; y < y0 + dy && y < this.getNrRows(); y++) {
                    int c = x - x0;
                    int r = y - y0;
                    BfMaskDataPoint p=this.getDataPointAt(x, y);
                    if (p != null && p.hasFlag(flag)) {
                        bit.set(c, r, true);
                    }
                }
            }
            res.add(bit);
        }
//        BitMask all = new BitMask(pinned.getNrCols(), pinned.getNrRows());
//        all.invert(all);
//        all.setName("All wells");
//        p("all 10/10: " +all.getDataPointAt(10,10)+", "+all.get(10, 10));
//        res.add(1, all);
        for (int i = 0; i < res.size(); i++) {
            BitMask m = res.get(i);
            m.setName(i + ". " + m.getName());
        }

        // now create a few default

        return res;
    }

    public String getFile() {
        return fileOrUrl;
    }

    public BfMask createEmpty() {
        BfMask res = new BfMask(getNrCols(), getNrRows());
        return res;
    }

    public BfMask copy() {
        BfMask res = new BfMask(getNrCols(), getNrRows());
        for (int r = 0; r < getNrRows(); r++) {
            for (int c = 0; c < getNrCols(); c++) {
                int v1 = getMaskAt(c, r);
                res.setMaskAt(c, r, v1);
            }
        }
        return res;
    }

    public boolean compatible(BfMask m1) {
        boolean b = m1.getNrCols() == getNrCols() && m1.getNrRows() == getNrRows();
        if (!b) {
            err("Masks are not compatible");
        }
        return b;
    }

    
    /* 1 - 1 = 0; 1 - 0=1, 0 - 1 = 0, 0 - 0 =0 -> bitwise a &(~b) */

    public boolean shift(BfMask m1, int dx, int dy) {
        if (!compatible(m1)) {
            return false;
        }
        if (m1 == this) {
            m1 = this.copy();
        }
        p("shifting " + m1 + " by " + dx + "/" + dy + " to " + this);
        for (int r = 0; r < m1.getNrRows(); r++) {
            for (int c = 0; c < m1.getNrCols(); c++) {
                int c1 = c - dx;
                int r1 = r + dy;
                if (c1 >= 0 && r1 >= 0 && c1 < m1.getNrCols() && r1 < m1.getNrRows()) {
                    int v = m1.getMaskAt(c1, r1);
                    //  p(v1 +"- "+ v2 +"= "+v3);
                    this.setMaskAt(c, r, v);
                }
                 else this.setMaskAt(c, r, 0);
            }
        }       
        return true;
    }

   
    public boolean copyFrom(BfMask m1) {
        if (!compatible(m1)) {
            return false;
        }
        for (int r = 0; r < getNrRows(); r++) {
            for (int c = 0; c < getNrCols(); c++) {
                int v1 = m1.getMaskAt(c, r);
                this.setMaskAt(c, r, v1);
            }
        }
        return true;
    }

  

    public void readHeader() {
        header = new Header();
        header.read();
        //    p("Bfmask header:" + header.toString());
    }

    @Override
    public String toString() {
        return getName();
    }

    public int[][] readAllData() {
        alldata = new int[header.nCols][header.nRows];
        for (int y = 0; y < header.nRows; y++) {
            for (int x = 0; x < header.nCols; x++) {
                int mask = BfMaskDataPoint.readNext(in);
                alldata[x][y] = mask;
            }
        }
        //p("Read bfmask read");
        return alldata;
    }

    public int getMaskAt(int col, int row) {
        if (col >= this.getNrCols() || row >= this.getNrRows()) {
            return -1;
        }
        if (col < 0 || row < 0) {
            return -1;
        }
        return alldata[col][row];
    }

    public void setMaskAt(int col, int row, int value) {
        if (col >= this.getNrCols() || row >= this.getNrRows()) {
            p("Out of bounds");
            return;
        }
        if (col < 0 || row < 0) {
            p("Out of bounds");
            return;
        }
        alldata[col][row] = Math.max(0, value);
    }

    public BfMaskDataPoint getDataPointAt(int col, int row) {
        if (col >= this.getNrCols() || row >= this.getNrRows()) {
            return null;
        }
        if (col < 0 || row < 0) {
            return null;
        }
        return new BfMaskDataPoint(alldata[col][row]);
    }

    public void getDataPointAt(int col, int row, BfMaskDataPoint point) {
        if (col >= this.getNrCols() || row >= this.getNrRows()) {
            return;
        }
        if (col < 0 || row < 0) {
            return;
        }
        alldata[col][row] = point.mask;
    }

//    private BfMaskDataPoint readData() {
//        BfMaskDataPoint data = new BfMaskDataPoint();
//        data.read(in);
//        //  p("Got data " + data.toString());
//        return data;
//    }
    private void err(String msg, Exception ex) {
        Logger.getLogger(Wells.class.getName()).log(Level.SEVERE, msg, ex);

    }

    private void err(String msg) {
        Logger.getLogger(Wells.class.getName()).log(Level.SEVERE, msg);
    }

    private void p(String msg) {
        Logger.getLogger(Wells.class.getName()).log(Level.INFO, msg);
    }

    public int[][] getData() {
        return alldata;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public int getNrRows() {
        return header.nRows;
    }

    public int getNrCols() {
        return header.nCols;
    }

    public ArrayList<WellCoordinate> getAllCoordsWithData(BfMaskFlag flag, int max) {
        return getAllCoordsWithData(flag, max, 0, 0, this.getNrCols(), this.getNrRows());
    }

    public ArrayList<WellCoordinate> getAllCoordsWithData(BfMaskFlag flag, int max, int c1, int r1, int c2, int r2) {
        c1 = Math.max(0, c1);
        r1 = Math.max(0, r1);
        c2 = Math.min(c2, header.nCols);
        r2 = Math.min(r2, header.nRows);
        ArrayList<WellCoordinate> coords = new ArrayList<WellCoordinate>();
        if (alldata == null) {
            err("Got no data");
            return null;
        }
        int code = flag.getCode();
        if (alldata[code] == null) {
            err("Got no data for flag " + flag);
            return null;
        }
        for (int c = c1; c < c2; c++) {
            for (int r = r1; r < r2; r++) {
                int mask = alldata[c][r];
                if (mask >= 0) {
                    BfMaskDataPoint p = new BfMaskDataPoint(mask);
                    if (p != null && p.hasFlag(flag)) {
                        WellCoordinate coord = new WellCoordinate(c, r);
                        coords.add(coord);
                        if (coords.size() > max) {
                            p("Found more coords than " + max + ", returning what I have now");
                            return coords;
                        }
                    }
                }
            }
        }


        return coords;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    protected class Header implements Serializable{

        int nRows; // uint32
        int nCols; //uint32

        protected void read() {
            try {
                nRows = (int) FileUtils.getUInt32(in);
                nCols = (int) FileUtils.getUInt32(in);
                nRows = FileUtils.INT_little_endian_TO_big_endian(nRows);
                nCols = FileUtils.INT_little_endian_TO_big_endian(nCols);
                //  p("Little endian values: " + nRows + "/" + nCols);

            } catch (IOException ex) {
                err("Could read header info of wells, file " + fileOrUrl, ex);
            }
        }

        public String toString() {
            return "Mask " + name + " (" + nRows + "x" + nCols + ")";
        }
    }
}
