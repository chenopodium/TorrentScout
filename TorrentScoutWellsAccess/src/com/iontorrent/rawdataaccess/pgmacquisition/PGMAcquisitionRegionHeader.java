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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class PGMAcquisitionRegionHeader extends PGMHeader {

   
    /** rows	unsigned int16 	number of rows in the following images */
    private int x_region_size;
    private int y_region_size;
    private int hw_interlacetype;// for un-doing deinterlace if needed
    private int uncompFramesInFile;
    /** sample rate (MHz)	unsigned int32 	Acquisition speed at which the image was taken    */
    private long sampleRate;
    /** channel offset [TS:4 DACs]	unsigned int16 	Current voltage for the channel A/D's  */
    private int channelOffset[];
    static final int DAT_HEADER_VERSION = 4;
    static final int DAT_HEADER_UNINTERLACED = 0;
    static final int DAT_HEADER_INTERLACED = 5;
    static final int DAT_HEADER_CORNERS = 6;

    @Override
    protected void read(DataInputStream in) {

        if (in == null) {
            err("NO input stream!");
        }
        try {

            wallTime = FileUtils.getUInt32(in);
            nrRows = FileUtils.getUInt16(in);
            nrCols = FileUtils.getUInt16(in);
            x_region_size =FileUtils.getUInt16(in);
            y_region_size = FileUtils.getUInt16(in);
            nrFrames = FileUtils.getUInt16(in);
            uncompFramesInFile = FileUtils.getUInt16(in);
            sampleRate = FileUtils.getUInt32(in);

            channelOffset = new int[PGMAcquisitionGlobalHeader.MAX_CHANNELS];
            for (int i = 0; i < channelOffset.length; i++) {
                channelOffset[i] = FileUtils.getUInt16(in);
            }
            hw_interlacetype = FileUtils.getUInt16(in);
            interlacetype = FileUtils.getUInt16(in);
            if (getInterlacetype() != DAT_HEADER_UNINTERLACED && getInterlacetype() != DAT_HEADER_INTERLACED && getInterlacetype() != DAT_HEADER_CORNERS) {
                p("Interlacetype must be " + DAT_HEADER_UNINTERLACED + ", " + DAT_HEADER_INTERLACED + " or "+DAT_HEADER_CORNERS+", but it was " + getInterlacetype());
            }

         //   p(toString());

        } catch (IOException ex) {
            err("Could not read header info of acquisition", ex);
        }
    }

    @Override
    public String toString() {
        return "RegionHeader: nrRows=" + getNrRows()
                + ", nrCols=" + getNrCols() + ", x/y region size=" + this.x_region_size + "/" + this.y_region_size + ", nrFrames=" + getNrFrames() + ", interlacetype=" + getInterlacetype();
    }

   

    /**
     * @return the sampleRate
     */
    public long getSampleRate() {
        return sampleRate;
    }

    /**
     * @param sampleRate the sampleRate to set
     */
    public void setSampleRate(long sampleRate) {
        this.sampleRate = sampleRate;
    }

    private void err(String msg, Exception ex) {
        Logger.getLogger(PGMAcquisitionHeader.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(PGMAcquisitionHeader.class.getName()).log(Level.SEVERE, msg);
    }

    private void p(String msg) {
      //  System.out.println("PGMAcquisitionHeader: " + msg);
        Logger.getLogger( PGMAcquisitionHeader.class.getName()).log(Level.INFO, msg);
    }

    /**
     * @return the x_region_size
     */
    public int getX_region_size() {
        return x_region_size;
    }

    /**
     * @param x_region_size the x_region_size to set
     */
    public void setX_region_size(int x_region_size) {
        this.x_region_size = x_region_size;
    }

    /**
     * @return the y_region_size
     */
    public int getY_region_size() {
        return y_region_size;
    }

    /**
     * @return the interlacetype
     */
    @Override
    public int getInterlacetype() {
        return interlacetype;
    }

    /**
     * @return the hw_interlacetype
     */
    public int getHw_interlacetype() {
        return hw_interlacetype;
    }

    /**
     * @return the uncompFramesInFile
     */
    public int getUncompFramesInFile() {
        return uncompFramesInFile;
    }
}
