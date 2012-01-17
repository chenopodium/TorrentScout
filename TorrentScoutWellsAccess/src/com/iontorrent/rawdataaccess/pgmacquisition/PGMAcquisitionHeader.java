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
public class PGMAcquisitionHeader extends PGMHeader {

    /** channels	unsigned int16 	number of channels in the imaged chip   */
    private int nrChannels;
    /** reserved	unsigned int16    */
    int reserved;
    /** sample rate (MHz)	unsigned int32 	Acquisition speed at which the image was taken    */
    private long sampleRate;
    /** full scale voltage [TS:4 DACs] (mV)	unsigned int16 	Max voltage for the channel A/D's   (not populated)    */
    private int fullScaleVoltage[];
    /** channel offset [TS:4 DACs]	unsigned int16 	Current voltage for the channel A/D's  */
    private int channelOffset[];
    /** ref electrode offset	unsigned int16 	Voltage of the fluid flowing over the chip  */
    private int refElectrodeOffset;
    /**frame interval	unsigned int16 	Time interval between frames.  */
    private int frameInterval;
    
    static final int DAT_HEADER_VERSION = 3;
    static final int DAT_HEADER_UNINTERLACED = 0;
    static final int DAT_HEADER_INTERLACED = 4;

    protected void read(DataInputStream in) {
        if (in == null) {
            err("NO input stream!");
        }
        try {

            wallTime = FileUtils.getUInt32(in);
            nrRows = FileUtils.getUInt16(in);
            nrCols = FileUtils.getUInt16(in);
            nrChannels = FileUtils.getUInt16(in);
            interlacetype = FileUtils.getUInt16(in);
            if (interlacetype != DAT_HEADER_UNINTERLACED && interlacetype != DAT_HEADER_INTERLACED) {
                err("Interlacetype must be " + DAT_HEADER_UNINTERLACED + " or " + DAT_HEADER_INTERLACED + ", but it was " + interlacetype);
            }
            nrFrames = FileUtils.getUInt16(in);
            reserved = FileUtils.getUInt16(in);
            setSampleRate(FileUtils.getUInt32(in));
            fullScaleVoltage = new int[4];

            for (int i = 0; i < 4; i++) {
                fullScaleVoltage[i] = FileUtils.getUInt16(in);
            }

            channelOffset = new int[4];
            for (int i = 0; i < 4; i++) {
                channelOffset[i] = FileUtils.getUInt16(in);
            }
            refElectrodeOffset = FileUtils.getUInt16(in);
            frameInterval = FileUtils.getUInt16(in);
       //     p("Read header v3 "+toString());

        } catch (IOException ex) {
            err("Could not read header info of acquisition", ex);
        }
    }

    public String toString() {
        return "AcqHeader: nrRows=" + getNrRows() + ", nrCols=" + getNrCols() + ", nrChannels=" + getNrChannels() + ", frameInterval=" + getFrameInterval() + ", nrFrames=" + getNrFrames() + ", interlacetype=" + interlacetype;
    }

    /**
     * @return the nrChannels
     */
    public int getNrChannels() {
        return nrChannels;
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

    /**
     * @return the refElectrodeOffset
     */
    public int getRefElectrodeOffset() {
        return refElectrodeOffset;
    }

    /**
     * @return the frameInterval
     */
    public int getFrameInterval() {
        return frameInterval;
    }

    private void err(String msg, Exception ex) {
        Logger.getLogger(PGMAcquisitionHeader.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(PGMAcquisitionHeader.class.getName()).log(Level.SEVERE, msg);
    }

    private void p(String msg) {
        System.out.println("PGMAcquisitionHeader: " + msg);
        //Logger.getLogger( PGMAcquisitionHeader.class.getName()).log(Level.INFO, msg, ex);
    }
}
