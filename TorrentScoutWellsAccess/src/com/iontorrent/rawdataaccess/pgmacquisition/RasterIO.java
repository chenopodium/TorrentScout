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

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.rawdataaccess.wells.BfMaskDataPoint;
import com.iontorrent.utils.ProgressListener;
import com.iontorrent.utils.io.DataInput;
import com.iontorrent.utils.io.DataInputImpl;
import com.iontorrent.utils.io.FileUtils;
import com.iontorrent.utils.io.RandomDataInputImpl;
import com.iontorrent.wellmodel.FlowCoordinate;
import com.iontorrent.wellmodel.RasterData;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellFlowData;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class RasterIO {

    private PGMAcquisitionGlobalHeader header;
    private static int raster_size;
    private String raw_dir;
    private String cache_dir;
    private File rasterfile;
    private RawType filetype;
    private String errormsg;
    private static final int MAXVAL = Integer.MAX_VALUE;

    public RasterIO(String raw_dir, String cache_dir, RawType type) {
        this.raw_dir = raw_dir;
        this.cache_dir = cache_dir;
        this.filetype = type;
        this.raster_size = RasterData.DEFAULT_RASTER_SIZE;
    }

    public static String convertFlows(RawType filetype, String raw_dir, String cache_dir) {
        return convertFlows(filetype, raw_dir, cache_dir, 0, -1, null, null);
    }

    public static String convertFlows(RawType filetype, String raw_dir, String cache_dir, int startflow, int endflow) {
        return convertFlows(filetype, raw_dir, cache_dir, startflow, endflow, null, null);
    }

    public static String convertFlows(RawType filetype, String raw_dir, String cache_dir, int startflow, int endflow, ProgressListener listener, WellCoordinate coord) {
        int max = getNrFiles(filetype, raw_dir) - 1;

//        p("convertFlows " + filetype + ": max=" + max + ", start=" + startflow + ", end=" + endflow + ", containing coord: " + coord);
        if (endflow < startflow || endflow > max) {
            endflow = max;
        }
        if (endflow < startflow) {
            p("endflow<startflow==" + endflow + ", we are done");
            return "All files have already been converted<br>";
        }
        //p("Converting raw data from type " + filetype + ", " + startflow + "-" + endflow + " in dir " + raw_dir );

        RasterIO io = new RasterIO(raw_dir, cache_dir, filetype);

        double incr = 100.0d / (endflow - startflow + 1);
        double progress = 0;
        String msg = "";
        for (int f = startflow; f <= endflow; f++) {
            if (listener != null) {
                listener.setProgressValue((int) progress);
                progress += incr;
            }
            String res = io.convertOneFileToXY(f, coord);
            if (res != null && res.length() > 0) {
                msg += res;
            }
        }
        return msg;
    }

    /** Count nr of files of given type*/
    public static int getNrFiles(RawType filetype, String dir) {
        if (FileUtils.isUrl(dir)) {
            p("Dir is url, don't know nr");
            return 500;
        }
        File[] files = new File(dir).listFiles();
        if (files == null) {
            return 0;
        }
        int cnt = 0;
        for (File f : files) {
            String nm = f.getName();
            if (nm.startsWith(filetype.getFilename()) && nm.endsWith(".dat")) {
                cnt++;
            }
        }
        return cnt;
    }

    /** get acquisition file */
    public static String getRawFilePath(RawType filetype, String dir, int flow) {
        if (filetype == null) {
            // err("No file type!");
            return null;
        }
        String sflow = "" + flow;
        for (int i = sflow.length(); i < 4; i++) {
            sflow = "0" + sflow;
        }

        String path = dir + filetype.getFilename() + "_" + sflow + ".dat";
        return path;
    }

    public PGMAcquisition readAcquisition(String file, int flow, WellCoordinate coord) {
        PGMAcquisition data = new PGMAcquisition(file, flow);

        WellCoordinate rasterstartcoord = this.computeRasterStartCoord(coord);
        this.header = data.getHeader();
        if (header == null) return null;
        p(file + ": " + header.getNrCols() + "x" + header.getNrRows() + ", rastercoord: " + rasterstartcoord);
        try {
            data.readFile(rasterstartcoord.getCol(), rasterstartcoord.getRow(), RasterData.DEFAULT_RASTER_SIZE, RasterData.DEFAULT_RASTER_SIZE);
            p("read sub-frame for file " + file);
        } catch (Exception e) {
            String msg = "Got an exception while reading " + file + ":" + e.getMessage();
            err(msg, e);
            this.errormsg = msg;
            return null;
        }
        return data;
    }
//    public PGMAcquisition readAcquisition(String file, int flow) {
//        PGMAcquisition data = new PGMAcquisition(file, flow);
//
//
//        this.header = data.getHeader();
//        p(file + ": " + header.getNrCols() + "x" + header.getNrRows());
//        try {
//            data.readFile();
//            p("read file " + file);
//        } catch (Exception e) {
//            String msg = "Got an exception while reading " + file + ":" + e.getMessage();
//            err(msg, e);
//            this.errormsg = msg;
//            return null;
//        }
//        return data;
//    }

    /** Conver the file to a coordinate oriented format.
     * The raster_size is the size of the rectangle that is saved in one file.
     * @param raster_size 
     */
    public String convertOneFileToXY(int startflow, WellCoordinate coord) {
        // xx conver to url
        errormsg = null;

        // check for region base dformat!
        readHeader();
        if (header == null) {
            errormsg = "Could not find any file of type " + this.filetype + " in folder " + this.raw_dir;
            return errormsg;
        }
        if (header.isRegionFormat()) {
            p("Region based format, no need to cache! :-)");
            return null;
        }
        if (coord == null) {
            int size = RasterData.DEFAULT_RASTER_SIZE;
            p("Need to convert entire file (got no coord), reading multiple times of size " + size);

            readHeader();
            if (header == null) {
                errormsg = "Could not find any file of type " + this.filetype + " in folder " + this.raw_dir;
                return errormsg;
            }
            int nrcols = this.header.getNrCols();
            int nrrows = this.header.getNrRows();
            // do it AT LEAST once!
            for (int c = 0; c <= Math.max(0, nrcols - size); c += size) {
                for (int r = 0; r <= Math.max(0, nrrows - size); r += size) {
                    WellCoordinate startcoord = new WellCoordinate(c, r);
                    //         p("Created start coord: " + startcoord);
                    String msg = convertOneFileToXY(startflow, startcoord);
                    if (msg != null && msg.length() > 0) {
                        errormsg += "<br>" + msg;
                    }
                }
            }
            return errormsg;

        } else {
            String file = getRawFilePath(filetype, raw_dir, startflow);
            //     p("Converting " + file + " and coord " + coord);
            PGMAcquisition data = readAcquisition(file, startflow, coord);
            if (data == null) {
                return this.errormsg;
            }

            int nrrows = header.getNrRows();
            int nrcols = header.getNrCols();
            int startcol = 0;
            int startrow = 0;
            if (coord != null) {
                WellCoordinate startcoord = this.computeRasterStartCoord(coord);
                startcol = startcoord.getCol();
                startrow = startcoord.getRow();
                nrrows = startrow + RasterData.DEFAULT_RASTER_SIZE;
                nrcols = startcol + RasterData.DEFAULT_RASTER_SIZE;

            }
            boolean done = false;
            int nrframes = header.getNrFrames();
            // p("Got " + nrframes + " frames");
            String msg = "";
            if (nrframes < 1) {
                msg = "Too few frames: " + nrframes;
                err(msg);
                return msg;
            }
            if (nrcols < 10) {
                msg = "Too few columns: " + nrcols;
                err(msg);
                return msg;
            }
            //   p("Got frame data");
            //        PGMAcquisitionFrame[] frames = data.getFrameData();
            //        data.g

            boolean TOOLARGE = false;
            p("Converting file " + file + " for flow " + startflow + " to raster size " + raster_size);
            if (!file.toString().endsWith("" + startflow + ".dat")) {
                msg = "WRONG FILENAME FOR FLOW: " + startflow;
                err(msg);
                return msg;
            }
            int count = 0;
            while (!done) {
//                if (count < 5) {
//                    p("about to get rasterfile in dir " + cache_dir + " and col " + startcol);
//                }
                rasterfile = getRasterFileName(new WellCoordinate(startcol, startrow), startflow, false);
                if (rasterfile.exists()) {
                    p("Deleting existing rasterfile " + rasterfile);
                    rasterfile.delete();
                }
                if (count == 0) {
                    p("Writing rasterfile " + rasterfile);
                }
                count++;

                DataOutputStream out = new DataOutputStream(getAppendOutputStream(rasterfile));
                // for each xy in the raster, write the value for each frame
                // AND WRITE TIMESTAMP INTO 0/rows+1
                // row major, first iterate over  rows
                for (int y = startrow; y < startrow + raster_size && y < nrrows; y++) {
                    //  int rowpos = nrcols * y;

                    for (int x = startcol; x < startcol + raster_size && x < nrcols; x++) {
                        // int pos = rowpos + x;
                        int framedata[] = data.getDataForPos(x, y);
                        if (framedata != null) {
                            for (int frame = 0; frame < nrframes; frame++) {
                                try {
                                    int val = framedata[frame];
                                    if (val > MAXVAL) {
                                        if (!TOOLARGE) {
                                            TOOLARGE = true;
                                            msg += "Some values in " + file + " are larger than " + MAXVAL + " (" + val + ")<br>";
                                            warn("Value too large: " + val + ">" + MAXVAL);
                                        }
                                        val = MAXVAL;
                                    }
                                    out.writeInt(val);
                                } catch (IOException ex) {
                                    Logger.getLogger(RasterIO.class.getName()).log(Level.SEVERE, null, ex);
                                    return ex.getMessage();
                                }
                            }
                        }
                    }
                }
                // now write timestamp!
                long times[] = data.getTimeStamp();
                for (int i = 0; i < nrframes; i++) {
                    long timestamp = times[i];
                    try {
                        out.writeLong(timestamp);
                    } catch (IOException ex) {
                        Logger.getLogger(RasterIO.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    out.flush();
                } catch (IOException ex) {
                    Logger.getLogger(RasterIO.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    out.close();
                    // get rectangular area for each frame
                } catch (IOException ex) {
                    Logger.getLogger(RasterIO.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (!rasterfile.exists()) {
                    msg = "File " + rasterfile + " does not exist after writing to it... <br>(check permissions when they got created)";
                    err(msg);
                    return msg;
                }
                // NEXT RASTER: first increment x, then increment y
                startcol = startcol + raster_size;
                if (startcol >= nrcols) {
                    // next rstartrow
                    //  p("Next rstartrow block starting at " + rstartrow);
                    startcol = 0;
                    startrow = startrow + raster_size;
                    if (startrow >= nrrows) {
                        done = true;
                        //  p("We are done: " + rstartcol + "/" + rstartrow);
                    }
                }
            }
            return msg;
        }

    }

    public WellCoordinate computeRasterStartCoord(WellCoordinate coord) {
        int xt = (coord.getCol() / raster_size);
        int yt = (coord.getRow() / raster_size);
        int col = xt * raster_size;
        int row = yt * raster_size;
        WellCoordinate startcoord = new WellCoordinate(col, row);
        return startcoord;
    }

    public File getRasterFile() {
        return rasterfile;
    }

    public WellFlowData readOneWellFromAcq(int x, int y, int startflow) {
        WellFlowData flowdata = new WellFlowData(x, y, startflow, filetype, null);
        PGMAcquisition data = this.readAcquisition(RasterIO.getRawFilePath(filetype, raw_dir, startflow), startflow, new WellCoordinate(x, y));
        flowdata.setData(data.getDataForPos(x, y));
        flowdata.setTimestamps(data.getTimeStamp());
        return flowdata;
    }

//    public WellFlowData readOneWellFromXY(int x, int y, int startflow) {
//        WellCoordinate coord = new WellCoordinate(x, y);
//        return readOneWellFromXY(coord, startflow);
//
//
//    }
    private boolean readHeader() {
        if (header != null) {
            return true;
        }
        String file = getRawFilePath(filetype, raw_dir, 0);
        if (file == null) {
            err("Could not get raw file name for dir " + raw_dir + "  and file type " + filetype);
            return false;
        }

        header = PGMAcquisition.getHeader(file);
        if (header == null) {
            if (!FileUtils.exists(file)) {
                err("File " + file + " does not exist, check nr of flows...");
                return false;
            }
            PGMAcquisition data = new PGMAcquisition(file, 0);
            header = data.getHeader();
        }
        return true;
    }

    /** Read the data for one particular XY raster, for ALL frames one flow... (?)
     * @param raster_size 
     */
    public WellFlowData readOneWellFromXY(WellCoordinate coord, int startflow) {
        errormsg = null;
        String file = getRawFilePath(filetype, raw_dir, startflow);
        if (file == null) {
            err("Could not get raw file name for dir " + raw_dir + "  and file type " + filetype);
            return null;
        }

        header = PGMAcquisition.getHeader(file);
        if (header == null) {
            if (!FileUtils.exists(file)) {
                err("File " + file + " does not exist, check nr of flows...");
                return null;
            }
            PGMAcquisition data = new PGMAcquisition(file, startflow);
            header = data.getHeader();
        }

        WellCoordinate startcoord = computeRasterStartCoord(coord);
        rasterfile = getRasterFileName(coord, startflow, true);

        //    p("Reading ONE WELL of raster file  " + rasterfile + " for flow " + startflow + " and coord " + coord);

        FlowCoordinate rasterstartfc = new FlowCoordinate(startcoord, startflow + 1, 0);
        long endpos = this.getFilePos(rasterstartfc);
        if (!rasterfile.exists()) {
            err("file does not exist: " + rasterfile);
            return null;
        } else {
            //  p("end position for flow " + startflow + " will be " + endpos + ", file size is " + rasterfile.length());
        }

        RandomAccessFile in = FileUtils.openRAFile(rasterfile);
        FlowCoordinate fc = new FlowCoordinate(coord, startflow, 0);
        long pos = this.getFilePos(fc);
        try {
            // p("Moved to file pointer " + pos);
            in.seek(pos);
        } catch (IOException ex) {
            Logger.getLogger(RasterIO.class.getName()).log(Level.SEVERE, null, ex);
            err("Could not move to position " + pos + " in file " + rasterfile);
            return null;
        }

        int nrframes = header.getNrFrames();

        BfMaskDataPoint flag = coord.getMaskdata();
        WellFlowData flowdata = new WellFlowData(coord.getCol(), coord.getRow(),startflow, filetype, flag);

        int[] res = new int[nrframes];
        if (filetype != RawType.ACQ) p("Reading " + nrframes + "frames");
        for (int f = 0; f < nrframes; f++) {
            try {
                res[f] = in.readInt();
                // if (val > 0) p("Got value "+val+" for "+flowcoord);
            } catch (IOException ex) {
                err("Could not read flow  frame=" + f);
                Logger.getLogger(RasterIO.class.getName()).log(Level.SEVERE, null, ex);
                try {
                    in.close();
                    // get rectangular area for each frame
                } catch (IOException e) {
                    Logger.getLogger(RasterIO.class.getName()).log(Level.SEVERE, null, e);
                }
                return null;
            }
        }
        double[] data = new double[res.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = res[i];
        }
        flowdata.setData(data);
        // move to time stamp file pointer
        pos = this.getTimeStampPos(startflow);
        try {
            //  p("Moving fp to timestamp pos " +pos);
            in.seek(pos);
        } catch (IOException ex) {
            Logger.getLogger(RasterIO.class.getName()).log(Level.SEVERE, null, ex);
            err("Could not move to position " + pos + " in file " + rasterfile);
            return null;
        }

        // move file pointer to time stamp pointer
        long[] ts = readTimeStamps(new RandomDataInputImpl(in), startflow);
        flowdata.setTimestamps(ts);
        try {
            in.close();
            // get rectangular area for each frame
        } catch (IOException ex) {
            Logger.getLogger(RasterIO.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (filetype != RawType.ACQ) p("Read data:" + flowdata);
        return flowdata;
    }

//    public RasterData readFromXY(int x, int y, int startflow) throws Exception {
//        WellCoordinate coord = new WellCoordinate(x, y);
//        return readFromXY(coord, startflow, startflow, null, false);
//
//    }
    /** Read the data for one particular XY raster, for ALL frames and flows... (?)
     * @param raster_size 
     */
    
   
    public RasterData readFromXY(ExperimentContext exp, WellCoordinate coord, int startflow, int endflow, ProgressListener listener, boolean createIfMissing) throws Exception {

        String file = getRawFilePath(filetype, raw_dir, startflow);

        header = PGMAcquisition.getHeader(file);

        if (header == null) {
            if (!FileUtils.exists(file)) {
                err("File " + file + " does not exist, check nr of flows...");
                return null;
            }
            PGMAcquisition data = new PGMAcquisition(file, startflow);
            header = data.getHeader();
        }

        WellCoordinate startcoord = computeRasterStartCoord(coord);

        FlowCoordinate fc = new FlowCoordinate(startcoord, endflow, 0);



        int frames_per_flow = header.getNrFrames();
        int rows = header.getNrRows();
        int cols = header.getNrCols();

        if (frames_per_flow < 1) {
            err("Too few frames per flow:" + header);
            return null;

        }
        // raster data structure!
        // rasterdata[x][y][flow][frame]
        RasterData rasterdata = new RasterData(exp, raster_size, header, startcoord, startflow, endflow);

        int rstartcol = startcoord.getCol();
        int rstartrow = startcoord.getRow();
        // for each xy in the raster
        float[][][][] rdata = rasterdata.getRasterdata();

        double totalnr = (endflow - startflow + 1) * raster_size;
        double increment = 100.0d / totalnr;
        double progress = 5;
        if (listener != null) {
            listener.setProgressValue((int) progress);
            progress += increment;
        }

        p("Reading raster for ENTIRE flows, start=" + rstartcol + "/" + rstartrow + ", " + startflow + "-" + endflow + ", increment " + increment);
        p("rastersize=" + raster_size + ", rstartrow=" + rstartrow + ", rows=" + rows + ", nrflows=" + rasterdata.getNrflows());
        for (int flow = startflow; flow - startflow < rasterdata.getNrflows() && flow <= endflow; flow++) {
            rasterfile = getRasterFileName(coord, flow, true);
            if (rasterfile == null || !rasterfile.exists()) {
                maybeCreateMissingFiles(startflow, endflow, createIfMissing, listener, coord);
            }
            DataInputStream in = getInputStream(rasterfile);
            if (in == null) {
                Exception e = new Exception("Inputstream is null for file " + rasterfile);
                throw e;
            }

            fc = new FlowCoordinate(startcoord, startflow, 0);

            long pos = this.getFilePos(fc);
            if (pos > 0) {
                long skipped = 0;
                try {
                    skipped = in.skip(pos);
                } catch (IOException ex) {
                    Logger.getLogger(RasterIO.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (skipped != pos) {
                    err("Could not skip to pos " + pos);
                    return null;
                }
                p("Moved to file pointer " + pos);
            }


            p("Reading raster for ENTIRE flow " + flow);

            for (int y = 0; y < raster_size && y + rstartrow < rows; y++) {
                if (listener != null) {
                    //   p("Setting progress value to " + progress);
                    listener.setProgressValue((int) progress);
                    progress += increment;
                }
                //  p("y="+y);
                for (int x = 0; x < raster_size && x + rstartcol < cols; x++) {
                    //WellCoordinate relcoord = new WellCoordinate(x - rstartcol, y - rstartrow);
                    for (int f = 0; f < frames_per_flow; f++) {
                        try {
                            int val = in.readInt();
                            if (val > MAXVAL) {
                                warn("Value too large: " + val + ">" + MAXVAL);
                                val = MAXVAL;

                            }
//                            if (val == 0) {
//                                p("Got 0 at "+x+"/"+y+", flow="+flow+", frame "+f);
//                            }
//                            else if (f == 1) p("Got "+val+" at "+x+"/"+y+", flow="+flow+", frame "+f);
                            rdata[x][y][flow - startflow][f] = (val);

                        } catch (Exception ex) {
                            //  p("Got exception " + ex.getMessage());
                            err("Could not read flow " + flow + ", x=" + x + ", y=" + y + ", frame=" + f);
                            Logger.getLogger(RasterIO.class.getName()).log(Level.SEVERE, null, ex);
                            try {
                                in.close();
                                // get rectangular area for each frame
                            } catch (IOException e) {
                                Logger.getLogger(RasterIO.class.getName()).log(Level.SEVERE, null, e);
                            }
                            throw ex;
                        }

                    }
                }
            }

            long[] timestamps = readTimeStamps(new DataInputImpl(in), flow);
            rasterdata.setTimeStamps(flow - startflow, timestamps);

            try {
                in.close();
                // get rectangular area for each frame
            } catch (IOException ex) {
                Logger.getLogger(RasterIO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        return rasterdata;
    }

    /** assumes file pointer is at correct position! */
    private long[] readTimeStamps(DataInput in, int flow) {
        // at end of each flow, also read TIMESTAMPS
        int frames_per_flow = header.getNrFrames();
        long[] timestamps = new long[frames_per_flow];

        for (int f = 0; f < frames_per_flow; f++) {
            long tsprev = 0;
            try {
                long ts = in.readLong();
                // p("Read time stamp "+ts+" at flow "+flow+" and frame "+f);
                timestamps[f] = ts;
                assert (ts > tsprev);
                if (ts < tsprev) {
                    err("Sanity check failed, time reversal :-). Prev time=" + tsprev + ", current ts=" + ts);
                }
                tsprev = ts;
                // if (val > 0) p("Got value "+val+" for "+flowcoord);
            } catch (IOException ex) {
                err("Could not read time stamp for " + flow + ":" + ex.getMessage());
                Logger.getLogger(RasterIO.class.getName()).log(Level.SEVERE, null, ex);
                try {
                    in.close();
                    // get rectangular area for each frame
                } catch (IOException e) {
                    Logger.getLogger(RasterIO.class.getName()).log(Level.SEVERE, null, e);
                }
                return timestamps;
            }
        }
        return timestamps;
    }

    /** position where the values for the timestamps begin */
    public long getTimeStampPos(int flow) {
        if (header == null) {
            err("Must read header first");
            return -1;
        }

        int frames_per_flow = header.getNrFrames();

        long deltaflowonly = raster_size * raster_size * frames_per_flow * 4;

        return deltaflowonly;

    }

    public int getNrFlowsInCache() {
        String file = getRawFilePath(filetype, raw_dir, 0);
        if (file == null) {
            err("Could not get raw file name for dir " + raw_dir + "  and file type " + filetype + ", no raw data");
            return -1;
        }
        if (!FileUtils.exists(file)) {
            err("File "+file+" not found");
            return -1;
        }
        header = PGMAcquisition.getHeader(file);
        if (header == null) {
            if (!FileUtils.exists(file)) {
                p("Raw File " + file + " does not exist, -> no cached files for ..." + filetype);
                return -2;
            }
            PGMAcquisition data = new PGMAcquisition(file, 0);
            header = data.getHeader();
        }

        WellCoordinate coord = new WellCoordinate(0, 0);
        int flow = 0;
        boolean ok = true;
        while (ok) {
            if (header.isRegionFormat() || header.getNrCols()<1000) {
                String f = getRawFilePath(filetype, raw_dir, flow);
                if (!FileUtils.exists(f)) ok = false;                
            }
            else {
                rasterfile = getRasterFileName(coord, flow, true);
                if (!rasterfile.exists()) ok = false;                
            }
            flow++;
        }

        return flow-1;

    }

//    public long getSizeOfOneFlow() {
//        if (header == null) {
//            err("Must read header first");
//            return -1;
//        }
//        // determine size of one flow
//        int frames_per_flow = header.getNrFrames();
//        // int is 32 bit so times 4 to get bytes
//        long deltaperwell = frames_per_flow * 4;
//        long deltaflowonly = raster_size * raster_size * deltaperwell;
//        // 64 bits per long, so 8 bytes
//        int deltatime = frames_per_flow * 8;
//        return (deltaflowonly + deltatime);
//    }
    public long getFilePos(FlowCoordinate fcoord) {
        if (header == null) {
            err("Must read header first");
            return -1;
        }

        // determine size of one flow

        int frames_per_flow = header.getNrFrames();

        // relative coordinate inside this raster
        int xinraster = fcoord.getCol() % raster_size;
        int yinraster = fcoord.getRow() % raster_size;

        // int is 32 bit so times 4 to get bytes
        long deltaperwell = frames_per_flow * 4;
        long deltathisflow = yinraster * raster_size * deltaperwell + xinraster * deltaperwell;

        long bytes = deltathisflow;

        //  p("Start pos for " + fcoord + ", bytes=" + bytes);
        return bytes;

    }

    private void maybeCreateMissingFiles(int startflow, int endflow, boolean createIfMissing, ProgressListener listener, WellCoordinate coord) throws Exception {
        if (createIfMissing) {
//            int toflow = getNrFiles(filetype, raw_dir);
//            if (toflow > 10) {
//                int ans = JOptionPane.showConfirmDialog(null, "I have to first convert " + toflow
//                        + "  raw files, which may take a while (~" + (toflow / 2) + " minutes).\n"
//                        + "Would you still like me to continue, or do you prefer to abort?", "Need to convert raw files", JOptionPane.YES_NO_OPTION);
//                if (ans != JOptionPane.YES_OPTION) {
//                    Exception e = new Exception("Rasterfile " + rasterfile + " does not exist! Must create it first...");
//                    // try to create it 
//                    err(e.getMessage(), e);
//                    throw e;
//                }
//            }
            p("Rasterfile " + rasterfile + " does not exist, will try to create it for all flows");
            if (listener != null) {
                int df = (endflow - startflow);
                listener.setMessage("Convering " + df + " raw " + filetype.name() + " files first, this may take up to " + (df) + " minutes");
            }
            convertFlows(filetype, raw_dir, cache_dir, startflow, endflow, listener, coord);

        } else {
            Exception e = new Exception("Rasterfile " + rasterfile + " does not exist! Must create it first...");
            // try to create it 
            err(e.getMessage(), e);
            throw e;
        }
    }

    public boolean existsRasterFile(WellCoordinate coord, int flow) {
        File f = getRasterFileName(coord, flow, true);
        return f != null && f.exists();
    }

    public File getRasterFileName(WellCoordinate coord, int flow, boolean toread) {
        if (filetype == null) {
            err("File type is null");
            return null;
        }

        WellCoordinate startcoord = computeRasterStartCoord(coord);

        File file = null;
        if (toread || FileUtils.canWrite(raw_dir)) {
            file = new File(raw_dir + "raster_" + filetype.getFilename() + "_f" + flow + "_c" + startcoord.getCol() + "_r" + startcoord.getRow() + "_" + raster_size + ".dat");
            if (file.exists()) {
                return file;
            }
            // search for any raster file, find raster
            if (toread) {
                File d = new File(raw_dir);
                File files[] = d.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.getName().startsWith("raster_" + filetype.getFilename() + "_f" + flow)) {
                            p("Found raster file for flow, but maybe different raster size: " + f);
                            String n = f.getName().substring(0, f.getName().length() - 4); // without .dat
                            int u = n.lastIndexOf("_");
                            n = n.substring(u + 1);
                            p("Got raster: " + n);
                            int rast = raster_size;
                            try {
                                rast = Integer.parseInt(n);
                            } catch (Exception e) {
                            }
                            if (rast != raster_size) {
                                p("Found other raster:" + rast);
                                raster_size = rast;
                                return getRasterFileName(coord, flow, toread);
                            }
                        }
                    }
                }
            }
        }
        if (cache_dir == null) {
            err("Cache dir was null");
            cache_dir= this.raw_dir;
        }
        if (!cache_dir.endsWith("\\") && !cache_dir.endsWith("/")) {
            cache_dir += "/";
        }
        File dir = new File(cache_dir);
        if (!dir.exists()) {
            p("Cache directory does not exist, will try to create it");
            dir.mkdirs();
            dir.setExecutable(true);
            dir.setWritable(true);
        }
        if (!dir.exists()) {
            err("The cache directory  " + cache_dir + " does not exist and I was not able to create it!");
            return null;
        }
        file = new File(cache_dir + "raster_" + filetype.getFilename() + "_f" + flow + "_c" + startcoord.getCol() + "_r" + startcoord.getRow() + "_" + raster_size + ".dat");


        return file;
    }

    private BufferedOutputStream getAppendOutputStream(File file) {
        try {
            FileOutputStream fout = new FileOutputStream(file, true);
            BufferedOutputStream out = new BufferedOutputStream(fout, 1024 * 1024);
            return out;

        } catch (Exception ioe) {
            err("Could not get BufferedOutputStream for file  " + file + ":" + ioe.getMessage());
            ioe.printStackTrace();
        }
        return null;
    }

    private DataInputStream getInputStream(File file) {
        try {
            FileInputStream fout = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fout);
            return in;

        } catch (Exception ioe) {
            err("Could not get DataInputStream for file  " + file + ":" + ioe.getMessage());
            ioe.printStackTrace();
        }
        return null;
    }

    private void appendToFile(DataOutputStream out, byte[] data) {

        try {
            out.write(data);
            out.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public String getErrorMsg() {
        return errormsg;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        this.errormsg = msg + ":" + ex.getMessage();
        //System.out.println("RasterIO: " + msg + ", " + ex.getMessage());

        Logger.getLogger(RasterIO.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        this.errormsg = msg;
        // System.out.println("RasterIO: " + msg);
        Logger.getLogger(RasterIO.class.getName()).log(Level.WARNING, msg);
    }

    private void warn(String msg) {
        //  System.out.println("RasterIO: " + msg);
        Logger.getLogger(RasterIO.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        //    System.out.println("RasterIO: " + msg);
        Logger.getLogger(RasterIO.class.getName()).log(Level.INFO, msg);
    }
}
