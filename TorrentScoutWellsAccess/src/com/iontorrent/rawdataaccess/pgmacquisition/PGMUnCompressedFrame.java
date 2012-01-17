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
package com.iontorrent.rawdataaccess.pgmacquisition;

import com.iontorrent.utils.io.FileUtils;
import java.io.DataInputStream;
import java.io.IOException;

/**
 *
 * @author Chantal Roth
 */
public class PGMUnCompressedFrame extends PGMFrame {

    /** image data [TS:rows x cols]	unsigned int16 	Frame data 
     */
    private int[] imageData;
    /** For following frames:
    len	unsigned int32 	the length of the compressed frame
    Transitions	unsigned int32 	number of transitions from 8-bit values to 16-bit values
    total	unsigned int32 	sum of all the pixel values after previous frame subtraction
    sentinel	unsigned int32 	should always be 0xDEADBEEF
    image data(variable length)	unsigned int8 	Frame data 
     */
    /** len	unsigned int32 	the length of the compressed frame */
    int len;
    /** Transitions	unsigned int32 	number of transitions from 8-bit values to 16-bit values */
    long nrTransitions;
    /** total	unsigned int32 	sum of all the pixel values after previous frame subtraction*/
    long sumOfTotalPixelValues;
    /** sentinel	unsigned int32 	should always be 0xDEADBEEF */
    static final long SENTINEL_CHECK = 0xDEADBEEFL;
    long sentinel;
    /** image data(variable length)	unsigned int8 	Frame data  */
    short[] varImageData;
   
    static final int DAT_FRAME_DATA_MASK = 0x3FFFF;
    static final boolean DEBUG = false;
    int nrcols;

    static int chan_interlace[] = { 1, 0, 3, 2, 5, 4, 7, 6 };
    public PGMUnCompressedFrame(int nrcols) {
        super();
        this.nrcols = nrcols;
    }

//    /** row major order */
    @Override
    public int getDataAt(int x, int y) {
        int pos = nrcols * y + x;
        return imageData[pos];
    }

    @Override
    public void setDataAt(int x, int y, int value) {
        int pos = nrcols * y + x;
        imageData[pos] = value;
    }
    @Override
    public boolean contains(int x, int y) {
        return true;
    }
 

    protected void read(int frame, PGMUnCompressedFrame prev, DataInputStream in, PGMAcquisitionGlobalHeader header) throws IOException {
        int rows_x_cols = header.getNrCols() * header.getNrRows();

//        int cols = header.getNrCols();
//        int rows = header.getNrRows();
        timestamp = FileUtils.getUInt32(in);
        if (header.getInterlacetype() > 0) {
            compressed = FileUtils.getUInt32(in);
        } else {
      //      p("Got interlace type: " + header.getInterlacetype());
        }
        imageData = new int[rows_x_cols];
        if (header.getInterlacetype() == 0) {            
            for (int i = 0; i < rows_x_cols; i++) {
                imageData[i] = FileUtils.getUInt16(in) & DAT_FRAME_DATA_MASK;
                //   if (i == 0) p("First 0/0 coord: "+imageData[i]);
            }
        } else {
           err("Not implemented uncompressed, interlace type: "+header.getInterlacetype());    
        }
      
    }

    public String toString() {
        return "UncompressedData: len=" + len + ", sentinel=" + Long.toHexString(sentinel);
    }

    /**
     * @return the imageData
     */
    public int[] getImageData() {
        return imageData;
    }
}
