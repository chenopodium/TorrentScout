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
import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class WellData {

    private BfMaskDataPoint maskdata;
    WellHeader header;
    
    long rank; // unused, uint32
    /** x or column position of well in the chip, 0-based */
    int x; // uint16
    /** y or row position of well in the chip, 0-based.  */
    int y; // uint16        
    /** The estimated incorporation signal value. 
     * the size of the array is numFlows
     */
    float[] flowValues;
    private int[] normalizedValues;

    // 8 + 4 * numFlows;
    public int getNrFlows() {
        return header.numFlows;
    }
    public String getSequence() {
        return new String(header.flowSequence);
    }
    public WellData(WellHeader header) {
        this.header = header;
    }
    public float[] getFlowValues() {
        return flowValues;
    }
    
    public float getFlowValue(int flow) {
        return flowValues[flow];
    }
    public float getFlowValueSum(int start, int end) {
       float tot = 0;
       for (int i = start; i < end; i++) {
           tot += flowValues[i];
       }
       return tot;
    }
    public float getAverageFlowValue() {
        return getAverageFlowValue(0, header.numFlows);
    }
    public float getAverageFlowValue(int start, int end) {
        if (start == end) return 0;
        return getFlowValueSum(start, end)/ (end-start);
    }
    public int getX() { return x; }
    public int getY() { return y; }
    
    public WellHeader getWellHeader() {
        return header;
    }
    
    protected void read(DataInputStream in) throws IOException {
        readXY(in);
        readFlowValues(in);
    }
 protected void read(RandomAccessFile in) throws IOException {
        readXY(in);
        readFlowValues(in);
    }
    protected void readXY(DataInputStream in) throws IOException {
        try {
            rank = FileUtils.getUInt32Little(in);
            byte b1 = in.readByte();
            byte b2 = in.readByte();
            x = (int) FileUtils.toUnsignedInt(b1, b2, (byte) 0, (byte) 0);
            b1 = in.readByte();
            b2 = in.readByte();
            y = (int) FileUtils.toUnsignedInt(b1, b2, (byte) 0, (byte) 0);
            
        } catch (IOException ex) {
            err("Could not read data info of wells at " + x + "/" + y + ", maybe file is truncated? Header: "+header.toString(), ex);
           // throw ex;
        }
    }

    protected void readFlowValues(DataInputStream in) throws IOException {
            flowValues = new float[header.numFlows];
            for (int i = 0; i < header.numFlows; i++) {
                flowValues[i] = FileUtils.getFloatLittle(in);
            }
        
    }
     protected void readXY(RandomAccessFile in) throws IOException {
        try {
            rank = FileUtils.getUInt32Little(in);
            byte b1 = in.readByte();
            byte b2 = in.readByte();
            x = (int) FileUtils.toUnsignedInt(b1, b2, (byte) 0, (byte) 0);
            b1 = in.readByte();
            b2 = in.readByte();
            y = (int) FileUtils.toUnsignedInt(b1, b2, (byte) 0, (byte) 0);
      //      p("Got x/y:"+x+"/"+y);
        } catch (IOException ex) {
            err("Could not read data info of wells at " + x + "/" + y + ", maybe file is truncated", ex);
            throw ex;
        }
    }

    protected void readFlowValues(RandomAccessFile in) throws IOException {
            flowValues = new float[header.numFlows];
            for (int i = 0; i < header.numFlows; i++) {
                flowValues[i] = FileUtils.getFloatLittle(in);
           //     if (flowValues[i] > 0 && i < 10)  p("Got value "+flowValues[i]);
            }
        
    }

    public String toString() {
        return "Data: x=" + x + ", y=" + y + ", flowValues=" + Arrays.toString(flowValues)+"\nBfMaskData: "+maskdata;
    }

    private void err(String msg, Exception ex) {
        Logger.getLogger(WellData.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void p(String msg) {
        System.out.println("Wells: " + msg);

    }

    void setMaskData(BfMaskDataPoint maskdata) {
        this.maskdata = maskdata;
    }

    /**
     * @return the maskdata
     */
    public BfMaskDataPoint getMaskdata() {
        return maskdata;
    }

    /**
     * @return the normalizedValues
     */
    public int[] getNormalizedValues() {
        return normalizedValues;
    }

    /**
     * @param normalizedValues the normalizedValues to set
     */
    public void setNormalizedValues(int[] normalizedValues) {
        this.normalizedValues = normalizedValues;
    }
}
