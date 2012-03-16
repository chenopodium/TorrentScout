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
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class PGMRegionFrameReader {

    static final int DAT_FRAME_KEY_0 = 0x7f;
    static final int DAT_FRAME_KEY_8_1 = 0x99;
    static final int DAT_FRAME_KEY_16_1 = 0xBB;
    static final int DAT_FRAME_DATA_MASK = 0x3FFFF;
    static final long SENTINEL_CHECK = 0xDEADBEEFL;
    static final long SKIP = 4294967295l;
    boolean DEBUG = false;
    int x_region_size;
    int y_region_size;
    int mincols;
    int minrows;
    int maxcols;
    int maxrows;
    int dx;
    int dy;
    int num_regions_x;
    int num_regions_y;
    int rows;
    int cols;
    int x;
    int y;
    boolean entireFrame;
    int realx;
    int realy;
    int state;
    int frameoffset;
    long trans;
    long total;
    int x_reg;
    int y_reg;
    int max_x_reg;
    int max_y_reg;
    int regionNum;
    int compPtrIndex;
    int nelems_x;
    int nelems_y;
    long difftoendofframe;
    long offsetendofframe;
    private boolean SHOW;
    long offset;//48 for headerSize 40
    long[] reg_offsets;
    //CompPtr = (unsigned char *)GetFileData(fd, 0, offset,cksum);
    //
    long endofframe;
    long regionoffset;
    long delta;
    int start_frame;
    int end_frame;
    int frame;
    int CompPtr[];
    int[][] FrameRegion;
    int sizeofFrameHdr = FileUtils.UINT32 * 6;
    int Val[];
    int[][] FirstFrame;
    int[][] WholeFrame;
    // long curfileoffset;
    PGMAcquisitionGlobalHeader header;
    private PGMRegionFrame[] frames;

    public PGMRegionFrameReader(PGMAcquisitionGlobalHeader header, int startx, int starty, int dx, int dy) {

        if (startx < 0) {
            startx = 0;
        }
        if (starty < 0) {
            starty = 0;
        }
        if (dx < 0) {
            dx = header.getNrCols();
        }
        if (dy < 0) {
            dy = header.getNrRows();
        }
        this.mincols = startx;
        this.minrows = starty;
        this.dx = dx;
        this.dy = dy;
        this.rows = header.getNrRows();
        this.cols = header.getNrCols();
        this.maxcols = Math.min(startx + dx, header.getNrCols() - 1);
        this.maxrows = Math.min(starty + dy, header.getNrRows() - 1);
        this.header = header;
        PGMAcquisitionRegionHeader rh = (PGMAcquisitionRegionHeader) header.getHeader();
        x_region_size = rh.getX_region_size();
        y_region_size = rh.getX_region_size();
        num_regions_x = cols / x_region_size;
        num_regions_y = rows / y_region_size;
        if (cols % x_region_size > 0) {
            num_regions_x++;
        }
        if (rows % y_region_size > 0) {
            num_regions_y++;
        }
        if (DEBUG) {
            p("PGMRegionHeader: " + rh.toString());
        }
        if (DEBUG) {
            p("Num regions: " + num_regions_x + "/" + num_regions_y);
        }


    }

    protected boolean read(int start_frame, int end_frame, DataInputStream in, boolean DEBUG) throws IOException {

        this.start_frame = start_frame;
        this.end_frame = end_frame;
        this.DEBUG = DEBUG;
        if (DEBUG) {
            p("=============== REGION FORMAT, READING FRAMES " + start_frame + "-" + end_frame);
        }
        FirstFrame = new int[cols][rows];
        WholeFrame = new int[cols][rows];
        frames = new PGMRegionFrame[end_frame - start_frame];

        entireFrame = false;
        // int PrevPtr = 0; used for what? seems to be used in other methods..
        // int offset = 0;
        trans = 0;
        total = 0;
        offset = 0x30;//48 for headerSize 40
        offset = header.getHeaderSize() + 8;
        reg_offsets = new long[x_region_size * y_region_size * 4];
        //CompPtr = (unsigned char *)GetFileData(fd, 0, offset,cksum);
        //
        sizeofFrameHdr = FileUtils.UINT32 * 6;
        Val = new int[8];
        for (frame = 0; frame < end_frame; frame++) {
            SHOW = DEBUG && frame > 40 && frame < 45;
            if (!readOneFrame(in)) {
                return false;
            }

        } // read next frame
        if (DEBUG) {
           p("Done reading all frames. Frames are: " + frames);
        }
        return true;
    }

    private boolean readOneFrame(DataInputStream in) throws IOException {
        frameoffset = 0;
        if (SHOW) {
            p("=========== PROCESSING FRAME " + frame + "============");
            p("offset: " + offset + "/" + Long.toHexString(offset) + ", frameoffset=" + frameoffset);
        }
        if (frame % 25 == 0) {
            Runtime r = Runtime.getRuntime();
            //  System.out.println("Reading frame " + frame + " total memory: " + r.totalMemory() / 1000000 + " M of " + r.maxMemory() / 1000000 + " M");
        }
        readAnyRemainder(in);
        PGMRegionFrame framedata = new PGMRegionFrame(header, mincols, minrows, dx, dy);

        if (!readFrameHeader(framedata, in)) {
            return false;
        }


        long len = 0;
        if (framedata.compressed == 0) {
            len = readUncompressedFrame(in, framedata, start_frame);
            return true;  // done with this frame
        } else {
            if (!readCompressedFrameHeader(in, framedata)) {
                return false;
            }
            //     offset += len;
        }

        entireFrame = false;
//        if (minrows == 0 && mincols == 0 && maxrows + 1 >= rows && maxcols + 1 >= cols) {
//            entireFrame = true;
//            // SHOW = true;
//        } else {
//            //StartCompPtr=-1;
//            entireFrame = false;
//        }
//        if (SHOW && frame == 0) {
//            System.out.println("Entire frame? " + entireFrame + " " + minrows + "/" + mincols + "/" + maxrows + "/" + maxcols);
//            // System.out.println("num region x=" + num_regions_x + ", num_regions_y=" + num_regions_y);
//        }

        compPtrIndex = 0;

        total = 0;
        trans = 0;
        //    int LastState;
        CompPtr = null;
        FrameRegion = null;

        difftoendofframe = framedata.framesize - sizeofFrameHdr - num_regions_x * num_regions_y * 4 + 4;
        offsetendofframe = offset + difftoendofframe + 4;

        readRegionOffsets(len, in);

        if (SHOW) {
            System.out.println("offset afterregions: " + Long.toHexString(offset) + ", frameoffset=" + frameoffset + ", sizeofframeheader=" + sizeofFrameHdr);
            System.out.println("framesize: " + framedata.framesize);
            System.out.println("difftoendofframe: " + difftoendofframe);
            System.out.println("offsetendofframe: " + Long.toHexString(offsetendofframe));
        }
        if (difftoendofframe <= 0) {
            // System.out.println("difftoendofframe<=0:" + difftoendofframe + ", framesize=" + framedata.framesize + ", sizeofheader: " + sizeofFrameHdr);
            //  SHOW = true;
        }
//        if (SHOW) {
//            long frame3 = 0x003c4d60 - 12;
//            long diff = frame3 - (offset + difftoendofframe);
//            System.out.println("Reading entire frame starting at " + offset + "  bytes=" + difftoendofframe + ", framelen-frameoffset=" + (framedata.framesize - frameoffset));
//            System.out.println("Endof frame will be: (offset+bytes)=" + (offset + difftoendofframe) + "/" + Long.toHexString(offset + difftoendofframe) + ", diff frame 3 start: " + diff);
//        }
        if (SHOW) {
            System.out.println(" ========== about to read actual data ====");
        }
        if (entireFrame) {
            readEntireFrameIntoCompPtr(difftoendofframe, in);
            FrameRegion = new int[cols][rows];
        } else {
            FrameRegion = new int[dx][dy];
            // FrameRegion = new int[cols][rows];
        }
        compPtrIndex = 0;
        max_x_reg = Math.max(0, Math.min(num_regions_x - 2, maxcols / x_region_size));
        max_y_reg = Math.max(0, Math.min(num_regions_y - 2, maxrows / y_region_size));
        if (SHOW) {
            System.out.println("max_y_reg  " + max_y_reg + ", max_x_reg: " + max_x_reg);

        }
        //long lastoffset = reg_offsets[max_y_reg * num_regions_x + max_x_reg + 1];
        endofframe = framedata.framesize - sizeofFrameHdr - num_regions_x * num_regions_y * 4 + 8;
        regionoffset = 0L;
        // 4 more because if it is a subframe, we are not doing the checksum part
        if (SHOW) {
            System.out.println("mincols=" + mincols + ", miinrows=" + minrows + ", maxcols=" + maxcols + ", maxrows=" + maxrows + ", last xreg/yreg= " + max_x_reg + "/" + max_y_reg);
        }
        // need to read until the END OF THE FRAME!!!
        y_reg = 0;
        x_reg = 0;
        for (y_reg = minrows / y_region_size; y_reg < num_regions_y && y_reg < (1 + maxrows / y_region_size); y_reg++) {
//                    if (SHOW) {
//                        System.out.println("\n+++++ Processing y_reg " + y_reg);
//                    }
            for (x_reg = mincols / x_region_size; x_reg < num_regions_x && x_reg < (1 + maxcols / x_region_size); x_reg++) {
//                        if (SHOW) {
//                            System.out.println("\n+++++ Processing x_reg " + x_reg);
//                            System.out.println("offset: " + offset + "/" + Long.toHexString(offset) + ", frameoffset=" + frameoffset);
//                        }

                regionNum = y_reg * num_regions_x + x_reg;
                if (reg_offsets[regionNum] != SKIP) {
                    if (entireFrame) { // if(StartCompPtr)                           
                        // compPtrIndex = (int) (StartCompPtr + reg_offsets[y_reg * num_regions_x + x_reg] - frameHdrLen + 8);              
                        compPtrIndex = (int) reg_offsets[regionNum];
                        //  compPtrIndex = compPtrIndex; //hack

//                        if (SHOW) {
//                            System.out.println("Entire frame: Setting comPtrIndex (in nr of bytes) to : " + compPtrIndex);
//                            System.out.println("reg_offsets[y_reg * num_regions_x + x_reg] =" + reg_offsets[y_reg * num_regions_x + x_reg] + ", " + reg_offsets[y_reg * num_regions_x + x_reg] / 2 + ", frameHdrLen=" + sizeofFrameHdr);
//                        }
                    } else {
                        regionoffset = loadNextRegionIntoCompPtr(regionoffset, offsetendofframe, max_y_reg, max_x_reg, in, framedata);
                    }

                    boolean ok = readOneSubregion(false);
                    if (!ok) {
                        return false;
                    }


                } else {
                    if (SHOW) System.out.println("SKIPPING Region " + regionNum + ", region nr=" + regionNum + ", storing data from first frame");
                    boolean ok = readOneSubregion(true);
                    if (!ok) {
                        System.out.println(" NOT ok, return without storing");
                        return false;
                    }
                }
            }
        }
        if (frame >= start_frame && frame <= end_frame) {
            for (int i = 0; i < dx; i++) {
                for (int j = 0; j < dy; j++) {
                
                    if (FrameRegion[i][j] == 0)  {
                        FrameRegion[i][j] = this.WholeFrame[i+mincols][j+minrows];
                    }}
            }
            frames[frame - start_frame] = framedata;
            frames[frame - start_frame].setImageData(FrameRegion);
            //if (skip)
//                if (SHOW) {
//                    System.out.println("Storing frame region data in frame " + (frame - start_frame) + ", it is:" + frames[frame - start_frame]);
//                    System.out.println("Frame info, mincols/minrows: " + framedata.getMincols() + "/" + framedata.getMaxrows());
//                }

        }
        if (!entireFrame) {
            skipToFrameEnd(endofframe, regionoffset, in);
        }
        if (entireFrame) {
            if (offsetendofframe >= offset + 4) {
                checkTotals(framedata, len);
                readChecksum(in);
            } else {
                //  System.out.println("NOt reading checksum, offsetendofframe=" + offsetendofframe + ", offset=" + offset);
            }
        }

        if (SHOW) {
            System.out.println("End of frame " + frame + ": offset: " + offset + "/" + Long.toHexString(offset) + ", frameoffset=" + frameoffset);
        }
        return true;
    }

    private void skipToFrameEnd(long endofframe, long regionoffset, DataInputStream in) throws IOException {
        long bytestoskipatend = Math.max(0, endofframe - regionoffset);
        if (SHOW) {
            System.out.println("End of regions.Regionoffset=" + regionoffset + ",  x_reg=" + x_reg + ", x_reg=" + y_reg + ", Skipping " + bytestoskipatend + " bytes to to the end of the frame");
        }

        offset += bytestoskipatend;
        frameoffset += bytestoskipatend;
        regionoffset += bytestoskipatend;
        in.skipBytes((int) bytestoskipatend);
    }

    public long loadNextRegionIntoCompPtr(long regionoffset, long offsetendofframe, int max_y_reg, int max_x_reg, DataInputStream in, PGMRegionFrame framedata) throws IOException {
        long newregionoffset = reg_offsets[regionNum];
        long bytestoskip = newregionoffset - regionoffset;
        if (offset + bytestoskip > offsetendofframe) {
            //  err("Should not skip to next region " + bytestoskip + "  from " + offset + "  beyond end of frame " + offsetendofframe
            //          + "\nnewregionoffset=" + newregionoffset + ", skip is: " + SKIP);
            bytestoskip = offsetendofframe - offset;
        }
        if (bytestoskip >= 0) {
            if (SHOW) {
                System.out.println("newregionoffset: reg_offsets[y_reg * num_regions_x + x_reg]=" + newregionoffset);
                System.out.println("last region offset: reg_offsets[max_y_reg * num_regions_x + max_x_reg + 1]=" + reg_offsets[max_y_reg * num_regions_x + max_x_reg + 1]);
                System.out.println("regionoffset=" + regionoffset);
                System.out.println("bytestoskip = newregionoffset - regionoffset=" + bytestoskip);
                System.out.println("skipping " + bytestoskip + " bytes");
            }


            offset += bytestoskip;
            frameoffset += bytestoskip;
            regionoffset += bytestoskip;
            in.skipBytes((int) bytestoskip);
        } else {
            System.out.println("NOT skipping bytes: " + bytestoskip + " bytes");
            System.out.println("newregionoffset: reg_offsets[y_reg * num_regions_x + x_reg]=" + newregionoffset);
            System.out.println("last region offset: reg_offsets[max_y_reg * num_regions_x + max_x_reg + 1]=" + reg_offsets[max_y_reg * num_regions_x + max_x_reg + 1]);
            System.out.println("regionoffset=" + regionoffset);
            System.out.println("bytestoskip = newregionoffset - regionoffset=" + bytestoskip);
//            for (int i = 0 ; i < reg_offsets.length; i++) {
//                p("Region offset "+i+":"+reg_offsets[i]);
//                //XXX TODO HACK remove this system exit after debugging
//               // if ( reg_offsets[i] < 0) System.exit(0);
//            }

        }

        int next = y_reg * num_regions_x + x_reg + 1;
        int tlen = 0;
        long nextoffset = framedata.framesize - 16;

        if (next < reg_offsets.length) {
            nextoffset = reg_offsets[next];
            tlen = (int) (nextoffset - newregionoffset);
        }
        // shouldn't we compute the last region as defined by maxrows/maxcols?
        //int tlen = 4 * x_region_size * y_region_size;

        if (tlen
                + offset
                > offsetendofframe) {
            if (SHOW) {
                p("Tlen should not be beyond end of frame: " + tlen + ", current offset: " + offset + ", offsetendofframe=" + offsetendofframe);
            }
            tlen = (int) (offsetendofframe - offset);
        }

        if (tlen
                <= 1) {
            tlen = (int) (offsetendofframe - offset);
        }

        if (SHOW) {
            System.out.println("nextoffset - newregionoffset=" + (nextoffset - newregionoffset));
            System.out.println("Now reading " + tlen + " shorts into CompPtr");
        }

        if (tlen
                < 0) {
            err("tlen should always be >= 0:" + tlen);
            System.out.println("newregionoffset: reg_offsets[y_reg * num_regions_x + x_reg]=" + newregionoffset);
            System.out.println("last region offset: reg_offsets[max_y_reg * num_regions_x + max_x_reg + 1]=" + reg_offsets[max_y_reg * num_regions_x + max_x_reg + 1]);
            System.out.println("regionoffset=" + regionoffset);
            System.out.println("bytestoskip = newregionoffset - regionoffset=" + bytestoskip);
            System.out.println("skipping " + bytestoskip + " bytes");
            tlen = 0;
        }
        CompPtr = new int[tlen];

        for (int i = 0;
                i < tlen
                && offset < offsetendofframe;
                i++) {
            CompPtr[i] = FileUtils.getUInt8(in);
            offset++;
            frameoffset++;
            regionoffset++;
        }
        //CompPtr = (unsigned char *)GetFileData(fd, offset + loffset, tlen, cksum);
        compPtrIndex = 0;

        return regionoffset;
    }

    private void checkTotals(PGMRegionFrame framedata, long len) {
        //if(mincols==0 && maxcols == cols && minrows==0 && maxrows==rows)
//                if (SHOW) {
//                    System.out.println("Checking transitions and totals frame");
//                }
        if (trans != framedata.headerTransitions) {
            if (SHOW) {
                System.out.println("transitions don't match: header: " + framedata.headerTransitions + " vs count: " + trans);
            }
// return;
        }
        if (total != framedata.headerTotal) {
            if (SHOW) {
                System.out.println("totals don't match!! header: " + framedata.headerTotal + " vs count: " + total + ",  len: " + len);
            }
//return;
        }
    }

    private void readAnyRemainder(DataInputStream in) throws IOException {
        int remainder = (int) (offset % 4);
        if (remainder > 0 && frame > 0) {
            if (SHOW) {
                System.out.println("Reading " + remainder + " additional bytes to get to the next block");
            }
            for (int i = 0; i < remainder; i++) {
                FileUtils.getUInt8(in);
                offset++;
            }
            if (SHOW) {
                System.out.println("offset: " + offset + "/" + Long.toHexString(offset) + ", frameoffset=" + frameoffset);
            }
        }
    }

    private void readChecksum(DataInputStream in) throws IOException {

        int tmpcksum = 0;
        int cksum = 0;
        //mincols == 0 && maxcols == cols && minrows == 0 && maxrows == rows) {
        int[] cksmPtr = new int[4];
        for (int i = 0; i < 4; i++) {
            cksmPtr[i] = FileUtils.getUInt8(in);
            offset += 1L;
            frameoffset += 1L;
        }
        //GetFileData(fd, offset, len, tmpcksum);
        if ((end_frame >= (header.getNrFrames() - 1))) {// && cksmPtr) {
            // there is a checksum?
            tmpcksum = cksmPtr[3];
            tmpcksum |= cksmPtr[2] << 8;
            tmpcksum |= cksmPtr[1] << 16;
            tmpcksum |= cksmPtr[0] << 24;
            if (tmpcksum != cksum) {
                if (SHOW) {
                    System.out.println("checksums don't match: cksum=" + cksum + ", tmpchecksum=" + tmpcksum + ", chsdmptrs[]=" + Arrays.toString(cksmPtr));
                }
                // return;
            }
        }
    }

    private void readEntireFrameIntoCompPtr(long difftoendofframe, DataInputStream in) throws IOException {
        // if(StartCompPtr)     
// changed from +4 to +8 Nov
// int bytes = framedata.framesize - sizeofFrameHdr - num_regions_x * num_regions_y * 4 + 8;
// framedata.framesize - 16;

//len = rows*cols;

        if (difftoendofframe <= 0) {
            System.out.println("difftoendofframe <0: " + difftoendofframe + ", returning no data");
            CompPtr = new int[0];
            return;
        }
        if (SHOW) {
            System.out.println("difftoendofframe: " + difftoendofframe + ", reading");
        }
        CompPtr = new int[(int) difftoendofframe];
// read region
        for (int i = 0; i < difftoendofframe; i++) {
            CompPtr[i] = FileUtils.getUInt8(in);
            frameoffset++;
            offset++;
// if (i < 100) System.out.println("uint8 "+i+"= "+CompPtr[i]);
        }

    }

    private boolean readFrameHeader(PGMRegionFrame framedata, DataInputStream in) throws IOException {
        // framedata.readFrameHeader(in);
        if (SHOW) {
            System.out.println("======Begin of frame header @ " + Long.toHexString(offset));
        }
        try {
            framedata.timestamp = FileUtils.getUInt32(in);
        } catch (EOFException e) {
            p("EOF: " + e.getMessage());
            return false;
        }
        framedata.compressed = FileUtils.getUInt32(in);
        offset += 8L;
        frameoffset += 8L;
        if (SHOW) {

            System.out.println("timestamp =" + framedata.timestamp + "  =" + Long.toHexString(framedata.timestamp));
            System.out.println("compressed=" + framedata.compressed + " =" + Long.toHexString(framedata.compressed));
        }
        return true;
    }

    private void readRegionOffsets(long len, DataInputStream in) throws IOException {
        len = (num_regions_x) * (num_regions_y);//frameHdr.len - sizeof(frameHdr) + 8;
        if (SHOW) {
            System.out.println("Reading " + len + " region offsets");
            System.out.println("offset: " + offset + "/" + Long.toHexString(offset) + ", frameoffset=" + frameoffset);
        }
        delta = 0L;
        for (int i = 0; i < len; i++) {
            reg_offsets[i] = (long)FileUtils.getUInt32(in);
            if (reg_offsets[i] < 0) {
                err("negative region offset < 0:" + reg_offsets[i]);
            }
            offset += 4L;
            frameoffset += 4L;
            if (delta ==0 && reg_offsets[i] != SKIP)  {
                delta = reg_offsets[i];
                //if (i > 0) p("Got delta from region "+i+":"+delta);
//                if (SHOW) {
//                    System.out.println("read first delta=" + delta + "/" + Long.toHexString(delta) + ", checking for 0xFFFFFFFF=" + (long) (SKIP) + ", frameoffset=" + frameoffset);
//                }

            }

            if (reg_offsets[i] == SKIP) {
               // System.out.println("Got skip: " + SKIP);
                reg_offsets[i] = SKIP;              
            } else if (reg_offsets[i] - delta > difftoendofframe) {
                System.out.println("PGMRegion: region diff is " + delta + ", beyond end of frame. Should mean skip!");
                reg_offsets[i] = SKIP;
            } else {
                long old = reg_offsets[i];
                reg_offsets[i] = (long)((long)old - (long) delta);
                if (reg_offsets[i] < 0) {
                    err("negative region offset < 0 AFTER SUBTRACTING DELTA delta=" + delta + ":" + reg_offsets[i]+", old offset="+old);
                }
            }
            if (SHOW) {
                // if (i % 100 == 0 || i + 1 == len || i < 5) {
                // y_reg * num_regions_x + x_reg
                int x = i % num_regions_x;
                int y = (i - x) / num_regions_x;
                //    System.out.println("Region Offset " + i + ", reg x=" + x + "/y=" + y + "= " + reg_offsets[i] + ", frameoffset=" + frameoffset);
                //    }
            }
        }
    }

    public void testEndian() {
        ByteOrder b = ByteOrder.nativeOrder();
        if (b.equals(ByteOrder.BIG_ENDIAN)) {
            p("Big-endian");
        } else {
            p("Little-endian");
        }
    }

    private boolean readCompressedFrameHeader(DataInputStream in, PGMRegionFrame framedata) throws IOException {
        if (SHOW) {
            System.out.println("Frame data is COMPRESSED: " + framedata.compressed);
        }
        // read other part of frame ddata
        framedata.framesize = (int) FileUtils.getUInt32(in);
        framedata.headerTransitions = FileUtils.getUInt32(in);
        framedata.headerTotal = FileUtils.getUInt32(in);
        framedata.sentinel = FileUtils.getUInt32(in);
        if (SHOW) {
            //System.out.println("Remaining frame header:");
            System.out.println("framesize          =" + framedata.framesize + " =" + Long.toHexString(framedata.framesize));
            System.out.println("headerTransitions  =" + framedata.headerTransitions + " =" + Long.toHexString(framedata.headerTransitions));
            System.out.println("headerTotal        =" + framedata.headerTotal + " =" + Long.toHexString(framedata.headerTotal));
            System.out.println("headsentinelrTotal =" + framedata.sentinel + " =" + Long.toHexString(framedata.sentinel));
        }
        offset += 16L;
        frameoffset += 16L;
        if (SHOW) {
            System.out.println("offset after reading frame header, including sentinel: off=" + Long.toHexString(offset) + ": " + Long.toHexString(offset) + ", frameoffset=" + frameoffset);
        }
        if (framedata.sentinel != SENTINEL_CHECK) {
            System.out.println("Corrupt file, sentinel wrong: " + framedata.sentinel);
            System.out.println("reading a few more 4 bytes to see if deadbeef is there: ");
            boolean ok = false;
            for (int i = 0; i < 16; i++) {
                long l = FileUtils.getUInt32(in);
                System.out.println("Offset=" + Long.toHexString(offset) + ": " + Long.toHexString(l));
                offset += 4L;
                frameoffset += 4L;
                if (l == SENTINEL_CHECK) {
                    framedata.sentinel = l;
                    p("Found sentinel - check code - probably problem with skip!");
                    ok = true;
                    DEBUG = true;
                    SHOW = true;
                    break;
                }
//            }
//            // testEndian();
//            if (framedata.sentinel != SENTINEL_CHECK) {
            }
            if (!ok) {
                return false;
            }

        } else if (SHOW) {
            System.out.println("Sentinel check ok");
        }
        return true;
    }

    private long readUncompressedFrame(DataInputStream in, PGMRegionFrame framedata, int start_frame) throws IOException {
        long len;
        //                if (DEBUG) {
        //                    p("Framedata is NOT compressed. Reading "+cols+"x"+rows+"x2 bytes="+(rows*cols*2));
        //                }
        len = rows * cols;
        //short[][] varImageData = new short[cols][rows];
        FirstFrame = new int[cols][rows];
        long count = 0;
        for (y = 0; y < rows; y++) {
            for (x = 0; x < cols; x++) {
                int val = FileUtils.getUInt16(in);
                offset += 2;
                FirstFrame[x][y] = (val & DAT_FRAME_DATA_MASK);
                total += FirstFrame[x][y];
                WholeFrame[x][y] = FirstFrame[x][y];
                count++;
            }
        }
        framedata.setImageData(FirstFrame);
        framedata.setMincols(0);
        framedata.setMinrows(0);
        if (frame >= start_frame) {
            frames[frame - start_frame] = framedata;
        }
        return len;
    }

    private boolean readOneSubregion(boolean skip) {
        state = 0; // first entry better be a state change
        nelems_x = x_region_size;
        nelems_y = y_region_size;

        if (((x_reg + 1) * x_region_size) > cols) {
            nelems_x = cols - x_reg * x_region_size;
        }
        if (((y_reg + 1) * y_region_size) > rows) {
            nelems_y = rows - y_reg * y_region_size;
        }

        realy = y_reg * y_region_size;
        //                    if (SHOW) {
        //                        System.out.println("processing " + nelems_x + ", " + nelems_y + " x and y elements, frameoffset=" + frameoffset);
        //                    }
        for (y = 0; y < (int) nelems_y; y++, realy++) {
            //					ptr = (int16_t *)(frame_data + ((y+(y_reg*y_region_size))*w + (x_reg*x_region_size)));
            // calculate imagePtr
            realx = x_reg * x_region_size;
            // if (SHOW) System.out.println("y="+y+", realy="+realy+", x=0");
            for (x = 0; x < (int) nelems_x && CompPtr != null && (skip || compPtrIndex < CompPtr.length);) {
                //  if (SHOW) p("x="+x+", y="+y+", compPtrIndex="+compPtrIndex+", CompPtr.len="+CompPtr.length);
                // SHOW = (frame < 10 && realx == 96 && realy==144);
                if (skip) {
                    if (realx >= mincols && realx < maxcols && realy >= minrows && realy < maxrows) {
                        //p("x=" + x + ", y=" + y + ", realx=" + realx + ", realy=" + realy + ", mincols=" + mincols + ", minrows=" + minrows + ", frame=" + frame);
                        // if frame 1, use prev = whole frame
                        // if frame >1, use pref frame
                        
                        // this is wrong: [4818, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        if (this.frame < 1) { // remove code
//                            if (realx == 5 && realy == 5) {
//                                p("frame=" + frame + ", realx=" + realx + ", realy=" + realy + ", whole frame value=" + WholeFrame[realx][realy]);
//                            }
                            FrameRegion[realx - mincols][realy - minrows] = WholeFrame[realx][realy];
                        } else {
//                            if (realx == 5 && realy == 5) {
//                                p("frame=" + frame + ", realx=" + realx + ", realy=" + realy + ", prev value=" + this.frames[frame - 1].getDataAt(realx, realy));
//                            }
                            FrameRegion[realx - mincols][realy - minrows] = this.frames[frame - 1].getDataAt(realx, realy);// + WholeFrame[realx][realy];
                        }
//                        if (x == 0 && y == 0) {
//                            p("SKIPPING: using: value wholeframe[" + realx + "][" + realy + "]=" + WholeFrame[realx][realy] + "  for frame " + frame);
//                        }
                    }
                    x++;
                    realx++;
                } else {
                    boolean ok = readOneStretchInASubregion();
                    if (!ok) {
                        return false;
                    }
                }
            } // end for x
        } // end for y
        return true;
    }

    private boolean readOneStretchInASubregion() {
        // read just a few wells
        if (CompPtr[compPtrIndex] == DAT_FRAME_KEY_0) {
            if (CompPtr[compPtrIndex + 1] == DAT_FRAME_KEY_16_1) {
                state = 16;

            } else {
                state = CompPtr[compPtrIndex + 1] & 0xf;
            }
            compPtrIndex += 2;
            trans++;
        }

        //if (SHOW) System.out.println("state is: "+state);
        if (state == 0) {
            err("\nIllegal state " + state + " corrupt file (bug in program :-)");
            System.out.println("\ncompPtrIndex=" + compPtrIndex + ", CompPtr.length=" + CompPtr.length + "\n frame: " + frame + ", \nGlobal file offset: " + offset + ", frame offset=" + frameoffset + "\nSize of CompPtr: " + CompPtr.length + ", maxint: " + Integer.MAX_VALUE);
            System.out.println("DAT_FRAME_KEY_0: " + DAT_FRAME_KEY_0 + ", " + Integer.toHexString(DAT_FRAME_KEY_0) + ",  DAT_FRAME_KEY_16_1:" + DAT_FRAME_KEY_16_1 + ", " + Integer.toHexString(DAT_FRAME_KEY_16_1));
            System.out.println("Checking CompPtr values in area around " + compPtrIndex);
            System.out.println("x_reg=" + x_reg + ", x=" + x + ", nelems_x=" + nelems_x + ", realx=" + realx);
            System.out.println("y_reg=" + y_reg + ", y=" + y + ", nelems_y=" + nelems_y + ", realy=" + realy + "\n");

            for (int i = Math.max(compPtrIndex - 8, 0); i < CompPtr.length && i < compPtrIndex + 8; i++) {
                int d = i - compPtrIndex;
                int s = CompPtr[i];
                System.out.println("ComPtr[i " + d + "]=" + s + ", hex: " + Integer.toHexString(s) + ", value &0xf=" + (s & 0xf));
            }
            //return false;
            System.out.println("Skipping this region and moving on to the next");
            compPtrIndex = CompPtr.length;
        }
        if (state == 2 && compPtrIndex + 8 > CompPtr.length) {
            if (SHOW) {
                System.out.println("ERROR: State " + state + ", realx=" + realx + ", realy=" + realy + " compPtrIndex " + compPtrIndex + ", frame: " + frame);
                System.out.println("Skipping to " + CompPtr.length);
            }
            compPtrIndex = CompPtr.length;
        }
        if (compPtrIndex + state > CompPtr.length) {
//            if (SHOW) {
//                System.out.println("ERROR: State " + state + ", realx=" + realx + ", realy=" + realy + " compPtrIndex " + compPtrIndex + ", frame: " + frame);
//                System.out.println("index > length! " + CompPtr.length);                
//            }
            x++;
        } else {
            short[] curvals = new short[state + 1];
            // copy current values from CompOtr to a short array for easier manipulation
            //  if (DEBUG)  p("copying values from "+compPtrIndex+", state="+state+", total array size="+CompPtr.length);
            for (int i = 0; i < state; i++) {
                curvals[i] = (short) CompPtr[compPtrIndex + i];
            }
            boolean SHOWIT = false;//(frame >41 && frame <44) && realx == 0 && realy == 0;
            if (SHOWIT) {
                System.out.println("Frame: " + frame + ", realx=" + realx + ", realy=" + realy);
                System.out.println("state: " + state);
                System.out.println("curvals: " + Arrays.toString(curvals));
            }
            switch (state) {
                case 3:
                    // get 8 values
                    Val[0] = (short) (curvals[0] >> 5) & 0x7;
                    Val[1] = (short) (curvals[0] >> 2) & 0x7;
                    Val[2] = ((short) (curvals[0] << 1) & 0x6) | ((short) (curvals[1] >> 7) & 1);
                    Val[3] = ((short) (curvals[1] >> 4) & 0x7);
                    Val[4] = ((short) (curvals[1] >> 1) & 0x7);
                    Val[5] = ((short) (curvals[1] << 2) & 0x4) | ((short) (curvals[2] >> 6) & 3);
                    Val[6] = ((short) (curvals[2] >> 3) & 0x7);
                    Val[7] = ((short) (curvals[2]) & 0x7);

                    break;

                case 4:
                    Val[0] = (short) (curvals[0] >> 4) & 0xf;
                    Val[1] = (short) (curvals[0]) & 0xf;
                    Val[2] = (short) (curvals[1] >> 4) & 0xf;
                    Val[3] = (short) (curvals[1]) & 0xf;
                    Val[4] = (short) (curvals[2] >> 4) & 0xf;
                    Val[5] = (short) (curvals[2]) & 0xf;
                    Val[6] = (short) (curvals[3] >> 4) & 0xf;
                    Val[7] = (short) (curvals[3]) & 0xf;

                    break;

                case 5:
                    Val[0] = (short) (curvals[0] >> 3) & 0x1f;
                    Val[1] = ((short) (curvals[0] << 2) & 0x1c) | ((short) (curvals[1] >> 6) & 0x3);
                    Val[2] = (short) (curvals[1] >> 1) & 0x1f;
                    Val[3] = ((short) (curvals[1] << 4) & 0x10) | ((short) (curvals[2] >> 4) & 0xf);
                    Val[4] = ((short) (curvals[2] << 1) & 0x1e) | ((short) (curvals[3] >> 7) & 0x1);
                    Val[5] = (short) (curvals[3] >> 2) & 0x1f;
                    Val[6] = ((short) (curvals[3] << 3) & 0x18) | ((short) (curvals[4] >> 5) & 0x7);
                    Val[7] = (short) (curvals[4]) & 0x1f;

                    break;

                case 6:
                    Val[0] = ((short) curvals[0] >> 2) & 0x3f;
                    Val[1] = ((short) (curvals[0] << 4) & 0x30) | ((short) (curvals[1] >> 4) & 0xf);
                    Val[2] = ((short) (curvals[1] << 2) & 0x3c) | ((short) (curvals[2] >> 6) & 0x3);
                    Val[3] = (short) (curvals[2] & 0x3f);
                    Val[4] = (short) (curvals[3] >> 2) & 0x3f;
                    Val[5] = ((short) (curvals[3] << 4) & 0x30) | ((short) (curvals[4] >> 4) & 0xf);
                    Val[6] = ((short) (curvals[4] << 2) & 0x3c) | ((short) (curvals[5] >> 6) & 0x3);
                    Val[7] = (short) (curvals[5] & 0x3f);

                    break;


                case 7:
                    Val[0] = (short) (curvals[0] >> 1) & 0x7f;
                    Val[1] = ((short) (curvals[0] << 6) & 0x40) | ((short) (curvals[1] >> 2) & 0x3f);
                    Val[2] = ((short) (curvals[1] << 5) & 0x60) | ((short) (curvals[2] >> 3) & 0x1f);
                    Val[3] = ((short) (curvals[2] << 4) & 0x70) | ((short) (curvals[3] >> 4) & 0x0f);
                    Val[4] = ((short) (curvals[3] << 3) & 0x78) | ((short) (curvals[4] >> 5) & 0x07);
                    Val[5] = ((short) (curvals[4] << 2) & 0x7c) | ((short) (curvals[5] >> 6) & 0x3);
                    Val[6] = ((short) (curvals[5] << 1) & 0x7e) | ((short) (curvals[6] >> 7) & 0x1);
                    Val[7] = ((short) curvals[6] & 0x7f);

                    break;

                case 8:
                    Val[0] = curvals[0];
                    Val[1] = curvals[1];
                    Val[2] = curvals[2];
                    Val[3] = curvals[3];
                    Val[4] = curvals[4];
                    Val[5] = curvals[5];
                    Val[6] = curvals[6];
                    Val[7] = curvals[7];

                    break;

                case 16:
                    Val[0] = (short) (curvals[0] << 8) | curvals[1];
                    Val[1] = (short) (curvals[2] << 8) | curvals[3];
                    Val[2] = (short) (curvals[4] << 8) | curvals[5];
                    Val[3] = (short) (curvals[6] << 8) | curvals[7];
                    Val[4] = (short) (curvals[8] << 8) | curvals[9];
                    Val[5] = (short) (curvals[10] << 8) | curvals[11];
                    Val[6] = (short) (curvals[12] << 8) | curvals[13];
                    Val[7] = (short) (curvals[14] << 8) | curvals[15];

                    break;

                default: {
                    err("ERROR: Illegal state " + state + " corrupt file (bug in program :-)");
                    System.out.println("ERROR: compPtrIndex=" + compPtrIndex + ", CompPtr.length=" + CompPtr.length + "\n frame: " + frame + ", \nGlobal file offset: " + offset + ", frame offset=" + frameoffset + "\nSize of CompPtr: " + CompPtr.length + ", maxint: " + Integer.MAX_VALUE);
                    System.out.println("DAT_FRAME_KEY_0: " + DAT_FRAME_KEY_0 + ", " + Integer.toHexString(DAT_FRAME_KEY_0) + ",  DAT_FRAME_KEY_16_1:" + DAT_FRAME_KEY_16_1 + ", " + Integer.toHexString(DAT_FRAME_KEY_16_1));
                    System.out.println("Checking CompPtr values in area around " + compPtrIndex);
                    System.out.println("x_reg=" + x_reg + ", x=" + x + ", nelems_x=" + nelems_x + ", realx=" + realx);
                    System.out.println("y_reg=" + y_reg + ", y=" + y + ", nelems_y=" + nelems_y + ", realy=" + realy + "\n");
                    for (int i = Math.max(compPtrIndex - 16, 0); i < CompPtr.length && i < compPtrIndex + 16; i++) {
                        int d = i - compPtrIndex;
                        int s = CompPtr[i];
                        System.out.println("ComPtr[i " + d + "]=" + s + ", hex: " + Integer.toHexString(s) + ", value &0xf=" + (s & 0xf));
                    }
                    return false;
                } // end case
            } // end switch
            compPtrIndex += state;
            if (SHOWIT) {
                System.out.println("(curvals[0] << 8)= " + ((curvals[0] << 8)));
                System.out.println("(SHORT)(curvals[0] << 8)= " + (short) ((curvals[0] << 8)));
                System.out.println("curvals[1]= " + curvals[1]);
                System.out.println("(curvals[0] << 8) | curvals[1] = " + ((curvals[0] << 8) | curvals[1]));
                System.out.println("val[0] before << (state - 1)=" + Val[0] + ", 1 << (state-1) = " + (1 << (state - 1)));

            }
            if (state != 16) {
                for (int i = 0; i < 8; i++) {
                    Val[i] -= 1 << (state - 1);
                }
            }


            for (int i = 0; i < 8; i++) {
                if (frame >= start_frame && frame <= end_frame) {
                    if (realx >= mincols && realx < maxcols && realy >= minrows && realy < maxrows) {
                        // FrameRegion[realx - mincols][imagey] = Val[i] + WholeFrame[realx][realy];
                        FrameRegion[realx - mincols][realy - minrows] = Val[i] + WholeFrame[realx][realy];
//                                            if (SHOW && realx == 982 && realy == 1340) {
//                                                p("x=" + x + ", y=" + y + ", realx=" + realx + ", realy=" + realy + "  realx-mincols=" + (realx - mincols) + ", realy-minrows=" + (realy - minrows) + "=" + Val[i]);
//                                            }
                        //if (DEBUG && realx+10 >maxcols && realy+ 10>maxrows) p("data at "+realx+"/"+realy+", val: "+ FrameRegion[realx - mincols][imagey]+" at "+(realx-mincols)+"/"+imagey);
                    }
                    // else if (DEBUG) p("realx/realy "+realx+"/"+realy+" out of bounds: "+mincols+"/"+minrows);
                }

                //  if (DETAIL) p("8 loop: x=l"+x+", y="+y+", val="+Val[i] + WholeFrame[realx][realy]);

                if (SHOWIT) {

                    System.out.println("Val[" + i + "]=" + Val[i] + ", WholeFrame[realx][realy]=" + WholeFrame[realx][realy]);

                }
                total += Val[i] + WholeFrame[realx][realy];
                WholeFrame[realx][realy] = Val[i] + WholeFrame[realx][realy];

                if (SHOWIT) {
                    System.out.println("WholeFrame[realx][realy] = Val[i] + WholeFrame[realx][realy]= " + WholeFrame[realx][realy]);
                    System.out.println("FirstFrame[realx][realy] =                                    " + FirstFrame[realx][realy]);
                }
                x++;
                realx++;
                //  if (SHOW) p("x="+x+", y="+y+" (doing 8 x values)");
            }
        }// end if (compPtrIndex + state > CompPtr.length) {

        return true;

    }

    public PGMRegionFrame[] getFrames() {
        return frames;




    }

    protected void err(String msg, Exception ex) {
        Logger.getLogger(PGMRegionFrameReader.class.getName()).log(Level.SEVERE, msg, ex);
    }

    protected void err(String msg) {
        Logger.getLogger(PGMRegionFrameReader.class.getName()).log(Level.SEVERE, msg);
    }

    protected void p(String msg) {
        System.out.println(msg);
    }
}
