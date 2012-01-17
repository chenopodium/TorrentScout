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
package com.iontorrent.wellmodel;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.Settings;
import com.iontorrent.rawdataaccess.pgmacquisition.PGMAcquisitionGlobalHeader;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class RasterData {

    public static int DEFAULT_RASTER_SIZE = 64;
    private PGMAcquisitionGlobalHeader header;
    /** [x][y][flow][frame] */
    private float[][][][] rasterdata;
    /** time stamps for [flow][frame] */
    private long timestamps[][];
    private int nrflows;
    private int frames_per_flow;
    private int raster_size;
    private WellCoordinate relstartcoord;
    private int startFlow;
    private int endFlow;
    RawType type;
    ExperimentContext exp;
    // private static int MIN_PIN = -16000;
    //private static int MAX_PIN = 160000;

    public RasterData(ExperimentContext exp, PGMAcquisitionGlobalHeader header, WellCoordinate startcoord, int endFlow) {
        this(exp, DEFAULT_RASTER_SIZE, header, startcoord, 0, endFlow);
    }

    public RasterData(ExperimentContext exp, int raster_size, PGMAcquisitionGlobalHeader header, WellCoordinate startcoord, int endFlow) {
        this(exp, raster_size, header, startcoord, 0, endFlow);
    }

    public RasterData(ExperimentContext exp, int raster_size, PGMAcquisitionGlobalHeader header, WellCoordinate startcoord, int startFlow, int endFlow) {
        this.header = header;
        this.startFlow = startFlow;
        this.endFlow = endFlow;
        this.exp = exp;
        this.raster_size = raster_size;
        this.relstartcoord = startcoord;
        this.frames_per_flow = header.getNrFrames();
        this.nrflows = Math.max(1, endFlow - startFlow + 1);
        if (frames_per_flow < 1) {
            err("Too few frames per flow:" + header);

        }
        rasterdata = new float[raster_size][raster_size][nrflows][frames_per_flow + 1];
        timestamps = new long[nrflows][frames_per_flow];
    }

    public float[] getValuesForAllFrames(FlowCoordinate abscoord) {
        FlowCoordinate fc = translateToRaster(abscoord);
        return getValuesinRasterForAllFrames(fc);
    }

    private float[] getValuesinRasterForAllFrames(FlowCoordinate rastercoord) {
        float[] res = new float[this.frames_per_flow];
        for (int f = 0; f < frames_per_flow; f++) {
            res[f] = rasterdata[rastercoord.getCol()][rastercoord.getRow()][rastercoord.getFlow()][f];
        }
        return res;
    }

    public float[] getTimeSeries(int c, int r, int flow) {

        float[] res = new float[this.frames_per_flow];
        if (c < 0 || r < 0) {
            return res;
        }

        float first = rasterdata[c][r][flow][0];
        for (int f = 0; f < frames_per_flow; f++) {
            res[f] = rasterdata[c][r][flow][f] - first;
        }
        return res;
    }

    public void setAbsoluteValue(FlowCoordinate abscoord, int value) {
        WellCoordinate rcoord = translateToRaster(abscoord.getCoord());
        if (rcoord == null) {
            err("Coordinate out of bounds:" + abscoord + " vs raster start " + getRelStartcoord());
            return;
        }
        rasterdata[rcoord.getCol()][rcoord.getRow()][abscoord.getFlow() - getStartFlow()][abscoord.getFrame()] = value;

    }
//    public int[][] getDataForFlowAndFrame(int flow, int frame) {
//        
//    }

    public float getAbsoluteValue(FlowCoordinate abscoord) {
        WellCoordinate rcoord = translateToRaster(abscoord.getCoord());
        if (rcoord == null) {
            err("Coordinate out of bounds:" + abscoord + " vs raster start " + getRelStartcoord());
            return -1;
        }
        return getRasterdata()[rcoord.getCol()][rcoord.getRow()][getStartFlow() + abscoord.getFlow()][abscoord.getFrame()];
    }

    public FlowCoordinate translateToRaster(FlowCoordinate absflowcoord) {
        WellCoordinate coord = translateToRaster(absflowcoord.getCoord());
        FlowCoordinate fc = new FlowCoordinate(coord, absflowcoord.getFlow() - startFlow, absflowcoord.getFrame());
        return fc;
    }

    public static WellCoordinate translateToRaster(WellCoordinate startcoord, WellCoordinate abscoord, int raster_size) {
        int startcol = startcoord.getCol();
        int startrow = startcoord.getRow();
        int absx = abscoord.getCol();
        int absy = abscoord.getRow();
        if (absx < startcol || absy < startrow || absx > startcol + raster_size || absy > startrow + raster_size) {
            err("Coordinate " + absx + "/" + absy + " is outside of raster at " + startcol + "/" + startrow);
            return null;
        }
        int x = absx % raster_size;
        int y = absy % raster_size;
        //  p("Translating "+absx+"/"+absy+"-> "+x+"/"+y);
        return new WellCoordinate(x, y);
    }

    public WellCoordinate translateToRaster(WellCoordinate abscoord) {
        int startcol = getRelStartcoord().getCol();
        int startrow = getRelStartcoord().getRow();
        int absx = abscoord.getCol();
        int absy = abscoord.getRow();
        if (absx < startcol || absy < startrow || absx > startcol + getRaster_size() || absy > startrow + getRaster_size()) {
            err("Coordinate " + absx + "/" + absy + " is outside of raster at " + startcol + "/" + startrow);
            return null;
        }
        int x = absx % getRaster_size();
        int y = absy % getRaster_size();
        //  p("Translating "+absx+"/"+absy+"-> "+x+"/"+y);
        return new WellCoordinate(x, y);
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(RasterData.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        Logger.getLogger(RasterData.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(RasterData.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("RasterData: " + msg);
        //Logger.getLogger( RasterData.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the header
     */
    public PGMAcquisitionGlobalHeader getHeader() {
        return header;
    }

    public float getValue(int x, int y, int flow, int frame) {
        if (rasterdata == null) {
            return 0;
        }
        if (x < relstartcoord.getX() || y < relstartcoord.getY()) {
            return 0;
        }
        if (x - relstartcoord.getX() >= this.raster_size) {
            return 0;
        }
        if (y - relstartcoord.getY() >= this.raster_size) {
            return 0;
        }
        if (flow < startFlow) {
            return 0;
        }
        if (frame < 0 || frame >= this.frames_per_flow) {
            return 0;
        }
        return rasterdata[x - relstartcoord.getX()][y - relstartcoord.getY()][flow - this.startFlow][frame];
    }

    public float getMax() {
        float max = Integer.MIN_VALUE;
        // int whichflow = 0;

        for (int f = 0; f < this.nrflows; f++) {
            float val = getMax(f);
            if (val > max) {
                max = val;
                //     whichflow = max;
            }
        }
        return max;

    }

    public float getMax(int flow) {
        float max = Integer.MIN_VALUE;

        for (int f = 0; f < this.frames_per_flow; f++) {
            float val = getMax(flow, f);
            if (val > max) {
                max = val;

            }
        }
        return max;
    }

    public float getMax(int flow, int frame) {
        float max = Integer.MIN_VALUE;
        int whichx = 0;
        int whichy = 0;

        for (int x = 0; x < this.raster_size; x++) {
            for (int y = 0; y < this.raster_size; y++) {
                if (rasterdata[x][y][flow][frame] > max) {
                    max = rasterdata[x][y][flow][frame];
                    whichx = x;
                    whichy = y;
                    if (max > Settings.PIN_MAX) {
                        // err("Max seems too large: "+max+", at x/y/fl/f="+x+"/"+y+"/"+flow+"/"+frame);
                        // TODO: FIX THIS
                        // max = 160000;
                    }

                }
            }
        }
        //    p("Getting max for flow "+flow+ " and frame "+ frame+":"+max+" at coord "+whichx+"/"+whichy);
        return max;
    }

    public float getMin() {
        float min = Integer.MAX_VALUE;


        for (int f = 0; f < this.nrflows; f++) {
            float val = getMin(f);
            if (val < min) {
                min = val;
            }
        }
        return min;

    }

    public float getMin(int flow) {
        float min = Integer.MAX_VALUE;


        for (int f = 0; f < this.frames_per_flow; f++) {
            float val = getMin(flow, f);
            if (val < min) {
                min = val;
            }
        }
        return min;
    }

    public float getMin(int flow, int frame) {
        float min = Integer.MAX_VALUE;

        for (int x = 0; x < this.raster_size; x++) {
            for (int y = 0; y < this.raster_size; y++) {
                if (rasterdata[x][y][flow][frame] < min) {
                    min = rasterdata[x][y][flow][frame];

                }
            }
        }
        //   p("Getting min for flow "+flow+ " and frame "+ frame+":"+min+" at coord "+whichx+"/"+whichy);
        return min;
    }

    /**
     * @return the rasterdata
     */
    public float[][][][] getRasterdata() {
        return rasterdata;
    }

    public boolean isPinned(int c, int r) {
        return isPinned(c, r, Settings.PIN_MIN, Settings.PIN_MAX);
    }

    public boolean isPinned(int c, int r, int min, int max) {
        boolean allsame = true;
        float[] values = getRasterdata()[c][r][0];
        for (int f = 0; f < values.length; f++) {
            if (values[f] < min || values[f] > max) {
                return true;
            }
            if (values[f] != values[0]) {
                allsame = false;
            }
        }
        return allsame;
    }

    /**
     * @return the nrflows
     */
    public int getNrflows() {
        return nrflows;
    }

    /**
     * @return the frames_per_flow
     */
    public int getFrames_per_flow() {
        return frames_per_flow;
    }

    /**
     * @return the raster_size
     */
    public int getRaster_size() {
        return raster_size;
    }

    /**
     * @return the startcoord
     */
    public WellCoordinate getRelStartcoord() {
        return relstartcoord;
    }

    public int getAbsStartCol() {
        return relstartcoord.getCol()+ exp.getColOffset();
    }

    public int getAbsStartRow() {
        return relstartcoord.getRow() + exp.getRowOffset();
    }
     public int getRelStartCol() {
        return relstartcoord.getCol();
    }

    public int getRelStartRow() {
        return relstartcoord.getRow() ;
    }

    /**
     * @return the startFlow
     */
    public int getStartFlow() {
        return startFlow;
    }

    /**
     * @return the endFlow
     */
    public int getEndFlow() {
        return endFlow;
    }

    /**
     * @param nrflows the nrflows to set
     */
    public void setNrflows(int nrflows) {
        this.nrflows = nrflows;
    }

    public void setTimeStamp(int flow, int frame, long ts) {
        timestamps[flow][frame] = ts;
    }

    public long getEndTime(int flow) {
        return timestamps[flow][this.frames_per_flow - 1];
    }

    public int getDT(int frame1, int frame2) {
        long t1 = (int) getTimeStamp(0, frame1);
        long t2 = getTimeStamp(0, frame2);
        return (int) (t2 - t1);

    }

    public long getTimeStamp(int flow, int frame) {
        return timestamps[flow][frame];
    }

    public long[] getTimeStamps(int flow) {
        return timestamps[flow];
    }

    public void setTimeStamps(int flow, long[] ts) {
        timestamps[flow] = ts;
    }

    public void setStartCoord(WellCoordinate wellCoordinate) {
        this.relstartcoord = wellCoordinate;
    }

    public void setFiletype(RawType rawType) {
        this.type = rawType;
    }

    public RawType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + this.frames_per_flow;
        hash = 47 * hash + this.raster_size;
        hash = 47 * hash + (this.relstartcoord != null ? this.relstartcoord.hashCode() : 0);
        hash = 47 * hash + this.startFlow;
        hash = 47 * hash + this.endFlow;
        hash = 47 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        RasterData d = (RasterData) o;
        if (d.type != type) {
            p("Type not same");
            return false;
        }

        if (d.startFlow != startFlow) {
            p("startflow not same");
            return false;
        }
        if (d.endFlow != endFlow) {
            p("endflowe not same");
            return false;
        }
        if (!d.relstartcoord.toString().equals(relstartcoord.toString())) {
            p("startcoord not same");
            return false;
        }
        if (d.raster_size != raster_size) {
            p("raster_size not same");
            return false;
        }
        return true;
    }

    public void subtract(RasterData sub) {
        p("Subtracting rasterdata");
        for (int f = 0; f < this.frames_per_flow; f++) {
            for (int x = 0; x < this.raster_size; x++) {
                for (int y = 0; y < this.raster_size; y++) {
                    float val = rasterdata[x][y][0][f];
                    float s = sub.rasterdata[x][y][0][f];
                    rasterdata[x][y][0][f] = val - s;
                }
            }

        }

    }

    public ExperimentContext getExpContext() {
        return exp;
    }
}
