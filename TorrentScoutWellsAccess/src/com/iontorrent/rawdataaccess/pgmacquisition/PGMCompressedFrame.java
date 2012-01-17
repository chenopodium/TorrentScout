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
public class PGMCompressedFrame extends PGMFrame {

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
    static final int DAT_FRAME_KEY_0 = 0x44;
    static final int DAT_FRAME_KEY_8_1 = 0x99;
    static final int DAT_FRAME_KEY_16_1 = 0xBB;
    static final int DAT_FRAME_DATA_MASK = 0x3FFFF;
    static final boolean DEBUG = false;
    int nrcols;

    static int chan_interlace[] = { 1, 0, 3, 2, 5, 4, 7, 6 };
    public PGMCompressedFrame(int nrcols) {
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

    protected void read(int frame, PGMCompressedFrame prev, DataInputStream in, PGMAcquisitionGlobalHeader header) throws IOException {
        int rows_x_cols = header.getNrCols() * header.getNrRows();

        int cols = header.getNrCols();
        int rows = header.getNrRows();
        timestamp = FileUtils.getUInt32(in);
        if (header.getInterlacetype() > 0) {
            compressed = FileUtils.getUInt32(in);
        } else {
       //     p("Got interlace type: " + header.getInterlacetype());
        }
        imageData = new int[rows_x_cols];
        if (prev == null || header.getInterlacetype() == 0 || compressed==0) {
           
            for (int i = 0; i < rows_x_cols; i++) {
                imageData[i] = FileUtils.getUInt16(in) & DAT_FRAME_DATA_MASK;
                //   if (i == 0) p("First 0/0 coord: "+imageData[i]);
            }
        } else if (header.getInterlacetype() == 3) {
           err("Not implemented: "+header.getInterlacetype());

        } else {
           
            int prevData[] = prev.getImageData();
            //assert (compressed == 4);//, "SanityCheck:frame must be compressed");
            len = (int) FileUtils.getUInt32(in);
            nrTransitions = FileUtils.getUInt32(in);
            sumOfTotalPixelValues = FileUtils.getUInt32(in);
            sentinel = FileUtils.getUInt32(in);
            assert (sentinel == SENTINEL_CHECK);
            if (sentinel != SENTINEL_CHECK) {
                err("Sentinel check failed! Got " + Long.toHexString(sentinel) + " instead of " + Long.toHexString(SENTINEL_CHECK)
                        + ", other values: sum=" + Long.toHexString(sumOfTotalPixelValues) + "  trans=" + Long.toHexString(nrTransitions));
            }
            // subtract len, transitions, total, sentinel
            //   len -= sizeof(uint32_t)*4; 
            int ctr = 0;
            len = len - 4 * 4;
            assert (0 < len);
            varImageData = new short[(int) len];

            for (int i = 0; i < len; i++) {
                varImageData[i] = (short) FileUtils.getUInt8(in);
            }
            /** de- interlace data */
            int i = 0;
            int mode = 0;

            long observed_transitions = 0;
            while (ctr < rows_x_cols) {
                if (len <= i) {
                    err("Not enough bytes read: " + len + "< " + i);
                }
                // switch to 8-bit mode, or 16-bit mode where appropriate
                //if(i < len-1 && DAT_FRAME_KEY_0 == (uint8_t)tmp_data8[i]) {
                //if(DAT_FRAME_KEY_8_1 == (uint8_t)tmp_data8[i+1]) {
                if (i < len - 1 && DAT_FRAME_KEY_0 == varImageData[i]) {
                    if (DAT_FRAME_KEY_8_1 == varImageData[i + 1]) {
                        // 16-bit to 8-bit 
                        observed_transitions++;
                        mode = 8;
                        i += 2;
                    } else if (DAT_FRAME_KEY_16_1 == varImageData[i + 1]) {
                        // 8-bit to 16-bit
                        observed_transitions++;
                        mode = 16;
                        i += 2;
                    }
                }
                // Note: assumes we must have data read between mode switches
                // read in data
                byte signedValue = (byte) varImageData[i];

                switch (mode) {
                    case 8:
                        // 8-bit mode
                        imageData[ctr] = signedValue + prevData[ctr];
                        ctr++;
                        i++;
                        break;
                    case 16:
                        // 16-bit mode
                        imageData[ctr] = ((signedValue << 8) | signedValue) & DAT_FRAME_DATA_MASK;
                        imageData[ctr] += prevData[ctr];
                        ctr++;
                        i += 2;
                        break;
                    default:
                        // mode?
                        err("Unrecognized mode: " + mode);
                        break;
                }
            }
            if (((i + 3) & ~0x3) != len) { // check that the data was quad-word aligned
                err("Data should be quad word aligned, but len is: " + len + " i=" + i);
            }

            varImageData = null;
            // check that the observed # of transitions equals the state # of
            // transitions
            if (nrTransitions != observed_transitions) {
                err("transitions " + nrTransitions + "!= observed_transitions " + observed_transitions);
            }

        }
   
    }

    public String toString() {
        return "Data: len=" + len + ", sentinel=" + Long.toHexString(sentinel);
    }

    /**
     * @return the imageData
     */
    public int[] getImageData() {
        return imageData;
    }
}
