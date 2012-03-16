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

import com.iontorrent.utils.io.FileTools;
import com.iontorrent.utils.io.FileUtils;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *Experiment Files
There are several File names present in each experiment:
•	prerun_xxxx.dat - images taken before the experiment begins
•	beadfind_pre_xxxx.dat - PH step images used to find live beads.
•	acq_xxxx.dat - Actual sequencing images
•	beadfind_post_xxxx.dat - PH step images used to find washout beads
Signature	unsigned int32	 
Version	unsigned int32 	 
header size	unsigned int32 	size in bytes of the header 
data size	unsigned int32 	size in bytes of the data portion 
wall time	unsigned int32 	time of acquisition 
rows	unsigned int16 	number of rows in the following images 
cols	unsigned int16 	number of columns in the following images 
channels	unsigned int16 	number of channels in the imaged chip 
interlacetype	unsigned int16 	0=uninterlaced, 4=compressed 
frames in file	unsigned int16 	Number of frames to follow this header. 
reserved	unsigned int16 	 
sample rate (MHz)	unsigned int32 	Acquisition speed at which the image was taken 
full scale voltage [TS:4 DACs] (mV)	unsigned int16 	Max voltage for the channel A/D's   (not populated) 
channel offset [TS:4 DACs]	unsigned int16 	Current voltage for the channel A/D's 
ref electrode offset	unsigned int16 	Voltage of the fluid flowing over the chip 
frame interval	unsigned int16 	Time interval between frames. 

 * The raw data is all in big endian! So 2-byte values and 4-byte values must be swapped with on X86-based processors!  The data is also row major.
For each frame, the chip data is contained in the lower 14 bits, so typically mask (AND) with 0x3fff to get the raw counts. There are always reference rows & cols on chip data as well, they are a 4 pixel-wide border around the chip. The reference pixels are tied to alternating VREF1 and VREF2.
All image files coming off the PGM should be of interlace type 4(compressed). The compression used is pretty simple:
FrameData 1 is un-compressed and un-interlaced.
Field	Datatype	Description
timestamp (ms)	unsigned int32 	Relative time from the start of acquisition for this frame 
Compressed (0)	unsigned int32 	If this is a compressed frame or not.  If not compressed, the data follows immediately. 
image data [TS:rows x cols]	unsigned int16 	FrameData data 

 * FrameData 2 and beyond are compressed.
Field	Datatype	Description
timestamp (ms)	unsigned int32 	Relative time from the start of acquisition for this frame
Compressed (1)	unsigned int32 	If this is a compressed frame or not.
len	unsigned int32 	the length of the compressed frame
Transitions	unsigned int32 	number of transitions from 8-bit values to 16-bit values
total	unsigned int32 	sum of all the pixel values after previous frame subtraction
sentinel	unsigned int32 	should always be 0xDEADBEEF
image data(variable length)	unsigned int8 	FrameData data 

 * @author Chantal Roth
 */
public final class PGMAcquisition {

    /** the associated file */
    // private File file;
    /** the input stream */
    private DataInputStream in;
    /** the header information - see subclass @Header below for description */
    private PGMAcquisitionGlobalHeader header;
    /**  the frame data - see subclass @FrameData below for descriptions */
    private PGMFrame[] pgmframes;
    //  private PGMAcquisitionFrame[] frames;
    //  private PGMSubFrame[] subframes;
    private int flow;
    private String path;
    private static HashMap<String, PGMAcquisitionGlobalHeader> mapHeaders = new HashMap<String, PGMAcquisitionGlobalHeader>();
    private int startframe;
    private int endframe;
    static final boolean DEBUG = false;

    public PGMAcquisition(String path, int flow) {
        this.flow = flow;
        this.path = path;
        if (path == null) {
            err("got null file path!");
        }
        if (FileTools.isUrl(path)) {
            try {
                initUrl(new URL(path));
            } catch (Exception ex) {
                err("Error reading " + path, ex);

            }
        } else {
            initFile(new File(path));
        }
    }

    public String getPath() {
        return path;
    }
    public String toString() {
        return this.header.toString();
    }

    public static PGMAcquisitionGlobalHeader getHeader(String path) {

        PGMAcquisitionGlobalHeader header = mapHeaders.get(path);
        if (header == null) {
            PGMAcquisition data = new PGMAcquisition(path, 0);
        }
        return mapHeaders.get(path);
    }

    /** open a binary file */
    public static DataInputStream openFile(File file) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(file), 1024 * 1024 * 32));
        } catch (Exception ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, "Could not open file " + file, ex);
        }
        return in;
    }

    private void initFile(File file) {
        //  this.file = file;
        if (!file.exists()){
            err("File "+file+" does not exist");
            return;
        }
        in = openFile(file);
        if (in == null) {
            err("Could not open file " + file);
        }
        readHeader();
    }

    private PGMAcquisition(URL url) {
        this.path = url.toString();
        initUrl(url);
    }

    private void initUrl(URL url) {
        //    this.file = file;
        in = FileUtils.openUrl(url);
        if (in == null) {
            err("Could not open url " + url);
        }
        readHeader();
    }

    public int getNrFrames() {
        return header.getNrFrames();
    }

    /** Currently only used in playground and for testing. Use raster for faster access*/
    public int[] getDataForPos(int x, int y) {
        int[] res = new int[header.getNrFrames()];
        for (int f = 0; f < header.getNrFrames(); f++) {
            if (f < startframe || f > endframe) {
                res[f] = 0;
            } else {
                // p("getting datapoint for frame "+", f in array: "+(f-startframe));
                //  p("Frame is: "+pgmframes[f-startframe]);
                if (pgmframes[f - startframe] == null) {
                    //  err("pgmframe at frame " + f + " contains no data");
                } else {
                    res[f] = pgmframes[f - startframe].getDataAt(x, y);
                }
            }
        }
        return res;
    }

    /** Currently only used in playground and for testing. Use raster for faster access*/
    public long[] getTimeStamp() {
        long[] res = new long[header.getNrFrames()];
        for (int f = 0; f < header.getNrFrames(); f++) {
            if (f < startframe || f > endframe) {
                res[f] = 0;
            } else {
                if (pgmframes[f] == null) {
                    err("Got no pgmframe at " + f);
                } else {
                    res[f] = pgmframes[f].getTimestamp();
                }
            }

        }
        return res;
    }

    public int getNrRows() {
        return header.getNrRows();
    }

    public int getNrCols() {
        return header.getNrCols();
    }

    private void readUnCompressedH3Format(int startx, int starty, int dx, int dy) {
        PGMUnCompressedFrame prev = null;
        if (startx < 0) {
          //  p("Using PGM Acqusition Frame to read data");
            pgmframes = new PGMUnCompressedFrame[header.getNrFrames()];
            for (int f = 0; f < header.getNrFrames(); f++) {
//                if (f % 25 == 0) {
//                    Runtime r = Runtime.getRuntime();
//                    p("Reading frame " + f + " total memory: " + r.totalMemory() / 1000000 + " M of " + r.maxMemory() / 1000000 + " M");
//                }
                pgmframes[f] = this.readUncompressedData(f, prev);
                prev = (PGMUnCompressedFrame) pgmframes[f];
            }
        } else {
          //  p("Using PGM UNCOMPRESSED SUB Frame to read data");
            pgmframes = new PGMSubFrame[header.getNrFrames()];
            for (int f = 0; f < header.getNrFrames(); f++) {
//                if (f % 50 == 0) {
//                    p("readUnCompressedH3Format.Reading .dat area flow=" + this.flow + ", (" + startx + "/" + starty + ")-(" + (startx + dx) + "/" + (starty + dy) + "), frame " + f + " total memory: " + Runtime.getRuntime().totalMemory() / 1000000 + " M");
//                }
                PGMUnCompressedFrame frame = readUncompressedData(f, prev);
                pgmframes[f] = new PGMSubFrame(frame, header, startx, starty, dx, dy);
                prev = frame;
            }
        }
    }

    private void readCompressedH3Format(int startx, int starty, int dx, int dy) {
        PGMCompressedFrame prev = null;
        if (dx <=0) dx = header.getNrCols();
        if (dy <=0) dy = header.getNrRows();
        if (startx < 0) {
            if (DEBUG) {
                p("Using PGM Acqusition Frame to read data");
            }
            pgmframes = new PGMCompressedFrame[header.getNrFrames()];
            for (int f = 0; f < header.getNrFrames(); f++) {
//                if (f % 25 == 0) {
//                    p("Reading frame " + f + " total memory: " + Runtime.getRuntime().totalMemory() / 1000000 + " M");
//                }
                pgmframes[f] = readInterlace4Data(f, prev);
                prev = (PGMCompressedFrame) pgmframes[f];
            }
        } else {
            if (DEBUG) {
                p("Using PGM SUB Frame to read data");
            }
            pgmframes = new PGMSubFrame[header.getNrFrames()];
            for (int f = 0; f < header.getNrFrames(); f++) {
//                if (f % 50 == 0) {
//                    p("readCompressedH3Format. Reading .dat area flow=" + this.flow + ", (" + startx + "/" + starty + ")-(" + (startx + dx) + "/" + (starty + dy) + "), frame " + f + " total memory: " + Runtime.getRuntime().totalMemory() / 1000000 + " M");
//                }
                PGMCompressedFrame frame = readInterlace4Data(f, prev);
                pgmframes[f] = new PGMSubFrame(frame, header, startx, starty, dx, dy);
                prev = frame;
            }
        }
    }

    private boolean readRegionFormat(int startframe, int endframe, int startx, int starty, int dx, int dy, boolean DEBUG) {
        // using RegionFrames
        //PGMRegionFrame frame =  new PGMRegionFrame(header, startx, starty, dx, dy);
        PGMRegionFrameReader reader = new PGMRegionFrameReader(header, startx, starty, dx, dy);
        this.startframe = startframe;
        this.endframe = endframe;
        // store frame info here
        boolean ok = true;
        try {
            ok = reader.read(startframe, endframe, in, DEBUG);
            if (!ok) {
                err("Error reading region based file. Trying again with debug on");
                this.closeFile();
                this.initFile(new File(path));
                reader.read(startframe, endframe, in, true);
            }
        } catch (IOException ex) {
            Logger.getLogger(PGMAcquisition.class.getName()).log(Level.SEVERE, "Could not read interlace type 5 data", ex);
        }
        pgmframes = reader.getFrames();
        //  p("Read region pgmframes: "+pgmframes.length+", first one is: "+pgmframes[0]);
        return ok;
    }

    public boolean readFile() {
        return readFile(-1, -1, -1, -1, 0, -1, false);
    }
 public boolean readFile(int startx, int starty, int dx, int dy ) {
        return readFile(startx, starty, dx, dy, 0, -1, false);
    }
 public boolean readFile(int startx, int starty, int dx, int dy, boolean DEBUG ) {
        return readFile(startx, starty, dx, dy, 0, -1, DEBUG);
    }
    public boolean readFile(int startx, int starty, int dx, int dy, int startframe, int endframe, boolean DEBUG) {
        // depending on HEADER!
        // if (DEBUG){  
        p("File: " + this.path + ", flow: " + flow + ", startx= " + startx + ", starty= " + starty + ", dx=" + dx + ", dy=" + dy);
        p(header.toString());
        if (endframe < startframe) endframe = header.getNrFrames();
        if (startframe < 0) startframe = 0;
        //  }
        boolean ok = true;
        if (dx <=0) dx = header.getNrCols();
        if (dy <=0) dy = header.getNrRows();
        if (header.isRegionFormat()) {
            ok = readRegionFormat(startframe, endframe, startx, starty, dx, dy, DEBUG);
        } else if (header.getVersion() == 3) {
            if (header.getInterlacetype() != 0) {
                readCompressedH3Format(startx, starty, dx, dy);
            } else {
                readUnCompressedH3Format(startx, starty, dx, dy);
            }
        }
        closeFile();
        return ok;
    }

    public PGMFrame[] getFrames() {
        return pgmframes;
    }

    public String getFrameType() {
        return pgmframes[1].getClass().getName();
    }

    public void showData() {
        p("Header :" + header.toString());
        p("Nr frames: " + pgmframes.length);
        for (int f = 0; f < pgmframes.length; f++) {
            if (f < startframe || f > endframe) {
                p("Not using frame " + f);
            } else {
                PGMFrame frame = pgmframes[f];
                p("Frame " + f + ":time=" + frame.timestamp);
            }
        }

    }

    private void err(String msg) {
        Logger.getLogger(PGMAcquisition.class.getName()).log(Level.SEVERE, msg);
    }

    public void readHeader() {
        if (in == null) {
            err("No inputstream");
        }
        header = new PGMAcquisitionGlobalHeader();
        header.read(in);
        mapHeaders.put(this.path, header);
        startframe = 0;
        endframe = header.getNrFrames();
        //   p("Got header:" + header.toString());
    }

    private PGMCompressedFrame readInterlace4Data(int frame, PGMCompressedFrame prev) {
        // p("reading frame "+frame);
        PGMCompressedFrame data = new PGMCompressedFrame(header.getNrCols());
        try {
            data.read(frame, prev, in, header);


        } catch (Exception ex) {
            Logger.getLogger(PGMAcquisition.class.getName()).log(Level.SEVERE, null, ex);


            return null;
        }

        return data;
    }

    private PGMUnCompressedFrame readUncompressedData(int frame, PGMUnCompressedFrame prev) {
      //  p("reading UNCOMPRESSED frame " + frame);
        PGMUnCompressedFrame data = new PGMUnCompressedFrame(header.getNrCols());
        try {
            data.read(frame, prev, in, header);


        } catch (Exception ex) {
            Logger.getLogger(PGMAcquisition.class.getName()).log(Level.SEVERE, null, ex);


            return null;
        }

        return data;


    }

    private void err(String msg, Exception ex) {
        Logger.getLogger(PGMAcquisition.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void p(String msg) {
        Logger.getLogger(PGMAcquisition.class.getName()).log(Level.FINE,msg);
    }

    public PGMAcquisitionGlobalHeader getHeader() {
        return header;
    }

    void closeFile() {
        //  p("closing file");
        try {
            in.close();


        } catch (IOException ex) {
            Logger.getLogger(PGMAcquisition.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean contains(int x, int y) {
        // the first frame usually contains the entire data
        if (pgmframes == null || pgmframes.length<2) return false;
        return pgmframes[1].contains(x, y);
    }

    /**
     * @return the flow
     */
    public int getFlow() {
        return flow;
    }

    /**
     * @param flow the flow to set
     */
    public void setFlow(int flow) {
        this.flow = flow;
    }
}
