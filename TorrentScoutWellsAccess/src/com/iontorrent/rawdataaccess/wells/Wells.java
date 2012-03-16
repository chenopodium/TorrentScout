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

import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.utils.io.FileUtils;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *The wells file format stores the estimated incorporation signals for each flow of each well of the chip.
 * It is a binary format consisting of a header section followed by a data section, stored in little-endian order. 
 * The section contents are outlined below.
A common question about the wells format is how to tell the total number of rows and columns. 
 * The number of rows and columns are not stored in the wells format, 
 * they can be found in the bfmask.bin file associated with each wells file.

 * @author Chantal Roth
 */
public class Wells {

    /** the associated file */
    private File file;
    /** the input stream */
    private DataInputStream in;
    /** the input stream */
    private RandomAccessFile rand;
    /** the header information - see subclass @Header below for description */
    private WellHeader header;
    /** data stored in [x][y] format - see subclass @WellData below for descriptions */
    private BfMask mask;
    private IHDF5Reader reader;

    public Wells(File file, BfMask mask) {
        this.file = file;
        this.mask = mask;
        in = FileUtils.openFile(file);
    }

    public BfMask getMask() {
        return mask;
    }

  

    private void err(String msg) {
        Logger.getLogger(WellsReader.class.getName()).log(Level.SEVERE, msg);
    }

    public boolean isHDF5() {
        return HDF5Factory.isHDF5File(file);
    }

    public void readHeader() {
        if (in == null) return;
        header = new WellHeader();
        if (isHDF5()) {
            reader = HDF5Factory.openForReading(file);
            header.readHD5(reader);
        } else {
            header.read(in);
        }
    }

    public WellData readHD5Well(int x, int y) {
        
        int[] blockdims = {1, 1, header.numFlows};
        
        long[] offset = {y, x, 0};
        
        MDFloatArray part = reader.readFloatMDArrayBlockWithOffset("wells", blockdims, offset);
        
        WellData data = new WellData(header);
        data.x = x;
        data.y = y;
        if (part != null) {
            data.flowValues = part.getAsFlatArray();            
           // p("read hdf5: "+Arrays.toString(data.flowValues));
        }
        else p("hdf5: no part, data at "+x+"/"+y);
        return data;
    }

    public WellData readWell(int col, int row) {
        p("Reading 1.wells at "+col+"/"+row +"(offset subtracted)");
        if(col < 0 || row < 0) return null;
        if(col >= header.cols || row >= header.rows) {
            p("header rows/cols: "+header.rows+"/"+header.cols+", cannot get data for "+col+"/"+row);
            return null;
        }
        WellData data = null;
        if (reader != null) {
            p("reading hd5 well");
            data= readHD5Well(col, row);
        } else {
            p("rading binary well");
            data= readBinaryWell(col, row);
        }
        if (data == null) {
            p("Got no data from 1.wells");
            return null;
        }
        BfMaskDataPoint maskdata = mask.getDataPointAt(col, row);
        data.setMaskData(maskdata); 
        return data;
    }

    public WellData readBinaryWell(int col, int row) {
        if (col < 0 || row < 0) {
            err("readWell: Negative coords, returning null.");
            return null;
        }

        p("Reading old binary file format");
        this.closeFile();
        this.rand = FileUtils.openRAFile(file);
        if (!skip(col, row, false)) {
            err("Could not skip to row " + row + " and col " + col);
            return null;
        }
        //   p("Skipped rows/cols. Reading data now");
        WellData data = readData(col, row, rand);
        if (data == null) {
            err("Was able to skip, but could not read well data at " + col + "/" + row);
            this.closeFile();
            this.rand = FileUtils.openRAFile(file);
            skip(col, row, true);
            return null;
        }
        if (data.getX() != col || data.getY() != row) {
            err("Error reading well data at " + col + "/" + row + ", got data " + data);
        }
        else try {
           // p("Read well at "+col+"/"+row+", got data "+Arrays.toString(data.flowValues));
        } catch (Exception ex) {
            Logger.getLogger(Wells.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    /** Move file pointer to column x and row y */
    private boolean skip(long skipcols, long skiprows, boolean show) {
        if (mask == null) {
            err("NO mask!");

        }
        long h = header.getHeaderSize();
        long d = header.getDataSize();
        // column major order, skipping skiprows*nrcols
        long ncols = mask.getNrCols();

        long cells = ncols * skiprows + skipcols;
        long pos = h + cells * d;
        if (pos < 0) {
            err("Negative seek offset. skiprows=" + skiprows + ", skipcols=" + skipcols + ", header size=" + h + ", data size=" + d);
            return false;
        }
        if (show) {
            p("Header size: " + h + ", datasize:" + d + ", skipping " + cells + " cells, file size=" + this.file.length());
        }
        if (show) {
            p("Skipping " + skipcols + " cols and " + skiprows + " rows, moving to pos " + pos);
        }
        try {
            rand.seek(pos);
        } catch (IOException ex) {
            err("Could not skip to pos :" + pos, ex);
            return false;
        }
        return true;
    }

    public WellData readData(int x, int y) {
        WellData data = new WellData(header);
        try {
            data.read(in);
        } catch (IOException ex) {
            err("Could not read data ", ex);
            return null;
        }

        if (data.x > 0 && data.y > 0 && (data.x != x || data.y != y)) {
            err("The coordinates do not match:" + x + "/" + y + " versus " + data.toString());
        }
        BfMaskDataPoint maskdata = mask.getDataPointAt(x, y);
        data.setMaskData(maskdata);
        return data;
    }

    public WellData readData(int x, int y, RandomAccessFile rand) {
        WellData data = new WellData(header);
        try {
            //     p("Reading from random access file at "+rand.getFilePointer());
            data.read(rand);
        } catch (IOException ex) {
            err("Could not read data ", ex);
            return null;
        }
       
        if (data.x > 0 && data.y > 0 && (data.x != x || data.y != y)) {
            err("The coordinates do not match:" + x + "/" + y + " versus " + data.toString());
        }
        return data;
    }

    private void err(String msg, Exception ex) {
        Logger.getLogger(Wells.class.getName()).log(Level.SEVERE, ErrorHandler.getString(ex));        
    }

    private void p(String msg) {
   //     System.out.println("Wells: " + msg);
    }

    public WellHeader getHeader() {
        return header;
    }

    void closeFile() {
        try {
            if (in != null) in.close();
        } catch (IOException ex) {            
        }
    }
}
