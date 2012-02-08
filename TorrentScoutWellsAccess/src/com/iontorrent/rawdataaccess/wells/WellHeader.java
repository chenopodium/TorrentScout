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

import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import com.iontorrent.utils.io.FileUtils;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class WellHeader {

    private long numWells; // uint32
    int numFlows; //uint16  
    long cols;
    long rows;
    private int cycles;
    private String chipType;
    private String keySequence;
    private int nrFrames;
    private double frameTime;
    
    /** A character array with ith entry specifying the ith nucleotide flowed. 
     * the size of the array is numFlows
     */
    char[] flowSequence;

    /** return size of header data structure  in bytes */
    public int getHeaderSize() {
        return 6 + numFlows;
    }

    public String getFlowSequence() {
        return new String(flowSequence);
    }

    public int getNrFlows() {
        return numFlows;
    }

    /** return size of header data structure  in bytes */
    public int getDataSize() {
        return 8 + 4 * numFlows;
    }

    protected void readHD5(IHDF5Reader reader) {

        HDF5DataSetInformation info = reader.getDataSetInformation("wells");
        long[] dims = info.getDimensions();
        rows = dims[0];
        cols = dims[1];
        numFlows = (int) dims[2];
        numWells = cols * rows;
        String[] keys = reader.readStringArray("info_keys");
        String[] values = reader.readStringArray("info_values");
        for (int i = 0; i < keys.length && i < values.length; i++) {
            String key = keys[i];
            String value = values[i];
            if (key != null && value != null) {
                key = key.toLowerCase().trim();
                value = value.trim();
                if (key.equals("flow_order")) {
                    flowSequence = value.toCharArray();
                } else if (key.equals("flows")) {
                    numFlows = getInt(value);
                } else if (key.equals("chiptype")) {
                    chipType = value;
                } else if (key.equals("cycles")) {
                    cycles = getInt(value);
                } else if (key.equals("num frames")) {
                    nrFrames = getInt(value);
                } else if (key.equals("frame time")) {
                    frameTime = getDouble(value);
                } else if (key.equals("librarykeysequence")) {
                    keySequence = value;
                } else {
                  //  p("Unused key/value: " + key + "=" + values[i]);
                }
            }
        }
        p("Got num flows=" + numFlows + ", cols/rows=" + cols + "/" + rows);
    }

    private int getInt(String s) {
        int i = 0;
        try {
            i = Integer.parseInt(s);
        } catch (Exception e) {
        }
        return i;
    }
     private double getDouble(String s) {
        double i = 0;
        try {
            i = Double.parseDouble(s);
        } catch (Exception e) {
        }
        return i;
    }

    /**
     * :52:6 WellHeader ? Unknown key/value: RAWWELLS_VERSION=2
    15:52:6 WellHeader ? Unknown key/value: Project=amplicon
    15:52:6 WellHeader ? Unknown key/value: Sample=e12766-pool29-l1330
    15:52:6 WellHeader ? Unknown key/value: Start Time=Mon Sep 19 19:07:04 2011
    15:52:6 WellHeader ? Unknown key/value: Experiment Name=R_2011_09_19_19_07_04_user_B10-471-r126404-preanneal-smc
    15:52:6 WellHeader ? Unknown key/value: User Name=user
    15:52:6 WellHeader ? Unknown key/value: Serial Number=sn10c070801a
    15:52:6 WellHeader ? Unknown key/value: Oversample=2x
    15:52:6 WellHeader ? Unknown key/value: Frame Time=0.034402
    15:52:6 WellHeader ? Unknown key/value: Num Frames=159
    15:52:6 WellHeader ? Unknown key/value: Cycles=13
    15:52:6 WellHeader ? Unknown key/value: Flows=440
    15:52:6 WellHeader ? Unknown key/value: LibraryKeySequence=TCAG
    15:52:6 WellHeader ? Unknown key/value: ChipTemperature=51 - 0
    15:52:6 WellHeader ? Unknown key/value: PGMTemperature=31.58 - 0.00
    15:52:6 WellHeader ? Unknown key/value: PGMPressure=10.49 - 0.00
    15:52:6 WellHeader ? Unknown key/value: W2pH=7.464
    15:52:6 WellHeader ? Unknown key/value: W1pH=9.604
    15:52:6 WellHeader ? Unknown key/value: Cal Chip High/Low/InRange=1797 8499 6327361
    15:52:6 WellHeader ? Unknown key/value: ChipType=316
     *
     * @param in 
     */
    protected void read(DataInputStream in) {

        try {
            numWells = FileUtils.getUInt32Little(in);
            cols = (long) Math.sqrt(numWells);
            rows = cols;
            numFlows = FileUtils.getUInt16Little(in);
            //       p("Header: numWells=" + numWells + ", numFlows=" + numFlows);
            flowSequence = new char[numFlows];
            for (int i = 0; i < numFlows; i++) {
                byte b2 = in.readByte();
                char c = (char) b2;
                //   p("Got char "+i+":"+(int)c+"/"+c);

                flowSequence[i] = c;
            }
        } catch (IOException ex) {
            err("Could read header info of wells", ex);
        }
    }

    protected void read(RandomAccessFile in) {
        try {
            numWells = FileUtils.getUInt32Little(in);
            numFlows = FileUtils.getUInt16Little(in);
            //   p("Header: numWells=" + numWells + ", numFlows=" + numFlows);
            flowSequence = new char[numFlows];
            for (int i = 0; i < numFlows; i++) {
                byte b2 = in.readByte();
                char c = (char) b2;
                //   p("Got char "+i+":"+(int)c+"/"+c);

                flowSequence[i] = c;
            }
        } catch (IOException ex) {
            err("Could read header info of wells", ex);
        }
    }

    private void err(String msg, Exception ex) {
        Logger.getLogger(WellHeader.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void warn(String msg) {
        Logger.getLogger(WellHeader.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
      //  Logger.getLogger(WellHeader.class.getName()).log(Level.INFO, msg);

    }

    public String toString() {
        return "Header: numWells=" + numWells + ", numFlows=" + numFlows + ", flowSequence=" + Arrays.toString(flowSequence);
    }

    /**
     * @return the cycles
     */
    public int getCycles() {
        return cycles;
    }

    /**
     * @return the chipType
     */
    public String getChipType() {
        return chipType;
    }

    /**
     * @return the keySequence
     */
    public String getKeySequence() {
        return keySequence;
    }

    /**
     * @return the nrFrames
     */
    public int getNrFrames() {
        return nrFrames;
    }
}
