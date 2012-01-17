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

/**
 *
 * @author Chantal Roth
 */
public class PGMRegionFrame extends PGMFrame {

    /** image data [TS:rows x cols]	unsigned int16 	Frame data 
     */
    private int[][] imageData;
    int framesize;
    /** Transitions	unsigned int32 	number of transitions from 8-bit values to 16-bit values */
    long headerTransitions;
    /** total	unsigned int32 	sum of all the pixel values after previous frame subtraction*/
    long headerTotal;
    /** sentinel	unsigned int32 	should always be 0xDEADBEEF */
   
    long sentinel;
    /** image data(variable length)	unsigned int8 	Frame data  */
    //  short[] unInterlacedData;
    
    static final boolean DEBUG = true;
    int x_region_size;
    int y_region_size;
    private int mincols;
    private int minrows;
    private int maxcols;
    private int maxrows;
    private int dx;
    private int dy;
    private int num_regions_x;
    private int num_regions_y;
    private int rows;
    private int cols;
    private PGMAcquisitionGlobalHeader header;
    PGMAcquisitionRegionHeader rh;
    public PGMRegionFrame(PGMAcquisitionGlobalHeader header, int startx, int starty, int dx, int dy) {
        super();

        this.header = header;
        rh = (PGMAcquisitionRegionHeader) header.getHeader();
        this.mincols = startx;
        this.minrows = starty;
        this.dx = dx;
        this.dy = dy;
        this.rows = header.getNrRows();
        this.cols = header.getNrCols();
        this.maxcols = Math.min(startx + dx, header.getNrCols());
        this.maxrows = Math.min(starty + dy, header.getNrRows());
        x_region_size = rh.getX_region_size();
        y_region_size = rh.getX_region_size();
    }
    @Override
    public String toString() {
        String s = "mincols="+mincols+", maxcols="+maxcols+", minrows="+minrows+", maxrows="+maxrows+"\n";
        s += "x_region_size="+x_region_size+", y_region_size="+y_region_size+"\n";
        s+="rows="+rows+", cols="+cols+"\n";
        s+="Region header="+rh.toString()+"\n";
        s+="image dimensions="+this.imageData.length+"/"+imageData[0].length+"\n";
        s+=getFrameHeaderInfo();
        return s;
    }
    public String getFrameHeaderInfo() {
         String s = "timestamp="+timestamp+"\n";
         s+="compressed="+compressed+"\n";
         s+="frameHdrlen="+framesize+"\n";
         s+="headerTransitions="+headerTransitions+"\n";
         s+="headerTotal="+headerTotal+"\n";
       //   s+="sentinel="+sentinel+"/"+Long.toHexString(sentinel)+" (should be:"+0xDEADBEEFL+"/"+Long.toHexString(0xDEADBEEFL)+"\n";
        
         return s;
    }
    
    public void readFrameHeader(DataInputStream in) {
        try {
            timestamp = FileUtils.getUInt32(in);
            compressed = FileUtils.getUInt32(in);
            framesize = (int) FileUtils.getUInt32(in);
            headerTransitions = FileUtils.getUInt32(in);
            headerTotal = FileUtils.getUInt32(in);
            sentinel = FileUtils.getUInt32(in);
        } catch (Exception e) {
            err(e.getMessage(), e);
        }
    }

    protected void setImageData(int[][] image) {
        this.imageData = image;
    }
    protected int[][] getImageData() {
        return imageData;
    }
    @Override
    public boolean contains(int x, int y) {
        return x >= getMincols() && x < (getMaxcols()) && y >= getMinrows() && y < (getMaxrows());
    }
//    /** row major order */

    @Override
    public int getDataAt(int x, int y) {
        if (!contains(x, y))  return -1; 
       // return imageData[x][y];
        return imageData[x-getMincols()][y-getMinrows()];
    }

    
    @Override
    public void setDataAt(int x, int y, int value) {
        if (!contains(x, y))  return; 
       // return imageData[x][y];
        imageData[x-getMincols()][y-getMinrows()] = value;
    }
    /**
     * @return the mincols
     */
    public int getMincols() {
        return mincols;
    }

    /**
     * @param mincols the mincols to set
     */
    public void setMincols(int mincols) {
        this.mincols = mincols;
    }

    /**
     * @return the minrows
     */
    public int getMinrows() {
        return minrows;
    }

    /**
     * @return the maxcols
     */
    public int getMaxcols() {
        return maxcols;
    }

    /**
     * @return the maxrows
     */
    public int getMaxrows() {
        return maxrows;
    }

    /**
     * @return the dx
     */
    public int getDx() {
        return dx;
    }

    /**
     * @return the dy
     */
    public int getDy() {
        return dy;
    }

    /**
     * @return the num_regions_x
     */
    public int getNum_regions_x() {
        return num_regions_x;
    }

    /**
     * @return the num_regions_y
     */
    public int getNum_regions_y() {
        return num_regions_y;
    }

    /**
     * @return the rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * @return the cols
     */
    public int getCols() {
        return cols;
    }

    /**
     * @return the header
     */
    public PGMAcquisitionGlobalHeader getHeader() {
        return header;
    }

    void setMinrows(int i) {
        this.minrows=i;
    }

}
