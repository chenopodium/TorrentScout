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
package com.iontorrent.wellalgorithms;

import com.iontorrent.rawdataaccess.wells.BitMask;
import com.iontorrent.utils.ProgressListener;
import com.iontorrent.wellmodel.RasterData;
import com.iontorrent.wellmodel.WellFlowData;
import com.iontorrent.wellmodel.WellFlowDataResult;
import com.iontorrent.wellmodel.WellFlowDataResult.ResultType;
import java.util.ArrayList;

/**
 *
 * @author Chantal Roth
 */
public class NearestNeighbor extends WellAlgorithm {

    
    public NearestNeighbor(WellContextFilter filter, int span, boolean allWells) {
        super(filter, span, allWells, "median");
    }

    public NearestNeighbor(int span, String medianfunction) {
        super(null, span, false, medianfunction);
    }

    public String getName() {
        return "nearest neighbor algorithm";
    }

//    public RasterData computex(RasterData data, BitMask ignoremask, BitMask empty, ProgressListener list, int span) {
//
//        int size = data.getRaster_size();
//        //public RasterData(int raster_size, PGMAcquisitionGlobalHeader header, WellCoordinate startcoord, int startFlow, int endFlow) {
//        RasterData res = new RasterData(data.getExpContext(), size, data.getHeader(), data.getRelStartcoord(), data.getStartFlow(), data.getEndFlow());
//
//       
//        int nrframes = data.getFrames_per_flow();
//        double inc = size / 100.0;
//        double prog = 0;
//
//        float[][][][] resvals = res.getRasterdata();
//        float[][][][] vals = data.getRasterdata();
//        long[][][] sums = computeSums(data, ignoremask, empty);
//        int flow = 0;
//
//        for (int f = 0; f < nrframes; f++) {
//            res.setTimeStamp(flow, f, data.getTimeStamp(flow, f));
//        }
//
//        double tot = span * span - 4;
//        span = span/2;
//        for (int c = 0; c < size; c++) {
//            prog += inc;
//            if (list != null) {
//                list.setProgressValue((int) prog);
//            }
//            for (int r = 0; r < size; r++) {
//                if (ok(ignoremask, empty, c, r)) {
//                    // get 4 corners 1   2
//                    //                 x
//                    //               3   4
//                    int c1 = Math.max(c - span, 0);
//                    int r1 = Math.max(r - span, 0);
//
//                    int c2 = Math.min(c + span, size - 1);
//                    int r2 = Math.max(r - span, 0);
//
//                    int c3 = Math.max(c - span, 0);
//                    int r3 = Math.min(r + span, size - 1);
//
//                    int c4 = Math.min(c + span, size - 1);
//                    int r4 = Math.min(r + span, size - 1);
//
//                    int dx = c4 - c1;
//                    int dy = r4 - r1;
//                    tot = dx * dy - 1;
//                    if (tot < 1) {
//                        err("nr of wells is too small:" + tot + "dx=" + dx + ", dy=" + dy + ", " + c1 + "/" + r1 + ", " + c2 + "/" + r2 + ", " + c3 + "/" + r3 + ", " + c4 + "/" + r4);
//                        return res;
//                    }
//                    for (int f = 0; f < nrframes; f++) {
//
//                        long v1 = sums[c1][r1][f];
//                        long v2 = sums[c2][r2][f];
//                        long v3 = sums[c3][r3][f];
//                        long v4 = sums[c4][r4][f];
//
//                        long rval = v4 - v2 + v1 - v3;
//                        // sum of all signals for frame f in this little square
//                        int sum = (int) (rval / tot);
//
//                        // now take the current value, minus the average of the area
//                        resvals[c][r][flow][f] = vals[c][r][flow][f] - sum;
//                        if (f == 10 && Math.random() > 0.999) {
//                            p("For frame 10, got sum of area: " + rval + ", divided by " + tot + "=span area=" + sum + ",  signal here is: " + vals[c][r][flow][f]);
//                        }
//                    }
//
//                }
//            }
//        }
//        return res;
//    }

    public RasterData computeBetter(RasterData data, BitMask ignoremask, BitMask empty, ProgressListener list, int span) {

        if (data == null) {
            err("No data, it is null");
            return null;
        }
        int size = data.getRaster_size();
        //public RasterData(int raster_size, PGMAcquisitionGlobalHeader header, WellCoordinate startcoord, int startFlow, int endFlow) {
        RasterData res = new RasterData(data.getExpContext(), size, data.getHeader(), data.getRelStartcoord(), data.getStartFlow(), data.getEndFlow());
        int nrframes = data.getFrames_per_flow();

        float[][][][] vals = data.getRasterdata();
        float[][][][] resvals = res.getRasterdata();
        int flow = 0;

        double inc = 100.0 / nrframes;
        double prog = 0;
        for (int f = 0; f < nrframes; f++) {
            res.setTimeStamp(flow, f, data.getTimeStamp(flow, f));
        }

        span = span/2;
     //   p("ComputeBetter");
        for (int f = 0; f < nrframes; f++) {
            prog += inc;

            if (list != null) {
                list.setProgressValue((int) prog);
            }
            for (int row = 0; row < size; row++) {
                float totsum = 0;
                int good = 0;

                int r1 = Math.max(0, row - span);
                int r2 = Math.min(size, row + span);

                for (int col = 0; col < size; col++) {

                    int c1 = Math.max(0, col - span);
                    int c2 = Math.min(size, col + span);
                 //   boolean show = Math.random()>0.9999;
                  //  if (show) p("computeBetter: Processing "+col+"/"+row);
                    if (col == 0) {
                        SpanInfo inf = getSum(c1, c2, r1, r2, ignoremask, empty, vals, f);
                        totsum = inf.totsum;
                        good = inf.good;
                        
                    } else {

                        if (c1 > 0) {
                            // subtract values just left of c1 if not at he very left end
                            for (int r = r1; r < r2; r++) {
                                if (ok(ignoremask, empty, c1-1, r)) {
                                    totsum = totsum - vals[c1 - 1][r][0][f];
                                    good--;
                                }
                            }
                        }

                        if (col - 1 >= 0) {  // add middle value
                            if (ok(ignoremask, empty,  col - 1, row)) {
                                good++;
                                totsum = totsum + vals[col - 1][row][0][f];
                            }
                        }
                        // subtract the current middle
                        if (ok(ignoremask, empty, col, row)) {
                            totsum = totsum - vals[col][row][0][f];
                            good--;
                        }
                        // add right most column sum, but only if not at the very end!
                        if (col + span < size) {
                            for (int r = r1; r < r2; r++) {
                                if (ok(ignoremask, empty, c2, r)) {
                                    totsum += vals[c2][r][0][f];
                                    good++;
                                }
                            }
                        }
                    }
                    if (good > 0) {
                        float rval = (float)totsum / (float)good;
                        resvals[col][row][0][f] = (float)vals[col][row][0][f]-rval;
//                        if (show) {
//                            p("computeBetter: "+col+"/"+row+":rval="+rval+", val="+vals[col][row][0][f]+"  good="+good);
//                        }
                    }
                    else resvals[col][row][0][f] = 0;//vals[col][row][0][f];
                } // next col
            } // next row
        } //next frame
        return res;

    }

    private class SpanInfo {

        int totsum;
        int good;
        int c1sum;

        public SpanInfo(int totsum, int good) {
            this.good = good;
            this.totsum = totsum;
        }
    }

    private SpanInfo getSum(int c1, int c2, int r1, int r2, BitMask ignoremask, BitMask empty, float[][][][] vals, int f) {
        //double[] median = computeMedian(data, ignoremask, span, c, r);
        // NOW COMPUTE MEDIAN
        int totsum = 0;
        int good = 0;
        for (int c = c1; c < c2; c++) {
            for (int r = r1; r < r2; r++) {
                if (ok(ignoremask, empty, c, r)) {
                    float val = vals[c][r][0][f];
                    if (val != 0) {
                        totsum += val;
                        good++;
                    }
                }
            }
        }
        return new SpanInfo(totsum, good);
    }

    public RasterData computeSlow(RasterData data, BitMask ignoremask, BitMask empty, ProgressListener list, int span) {

        p("ignmore mask: "+ignoremask);
        p("empty mask: "+empty);
        int size = data.getRaster_size(); 
        //public RasterData(int raster_size, PGMAcquisitionGlobalHeader header, WellCoordinate startcoord, int startFlow, int endFlow) {
        RasterData res = new RasterData(data.getExpContext(), size, data.getHeader(), data.getRelStartcoord(), data.getStartFlow(), data.getEndFlow());
        int nrframes = data.getFrames_per_flow();

        float[][][][] vals = data.getRasterdata();
        float[][][][] resvals = res.getRasterdata();
        int flow = 0;

        double inc = 100.0 / size;
        double prog = 0;
        for (int f = 0; f < nrframes; f++) {
            res.setTimeStamp(flow, f, data.getTimeStamp(flow, f));
        }

        for (int c = 0; c < data.getRaster_size(); c++) {
            prog += inc;
            if (list != null) {
                list.setProgressValue((int) prog);
            }
            for (int r = 0; r < data.getRaster_size(); r++) {
                double[] median = computeMedian(data, ignoremask, empty, span, c, r);
                for (int f = 0; f < nrframes; f++) {
                    float val = vals[c][r][flow][f];
                    float rval = val - (int) median[f];
                    resvals[c][r][flow][f] = rval;
                }
            }
        }
        return res;
    }

    @Override
    public ArrayList<WellFlowDataResult> compute() {
      // p("======================");
     //   p("Computing nn for " + getCoord() + " and flow " + getFlow() + ", all: " + this.allWells);


        WellFlowDataResult nnBg = (WellFlowDataResult) this.getCachedResult(getKey() + ":nn");
        if (nnBg == null) {
            nnBg = createResultWell();
            if (nnBg == null) {
                err("NN.compute: Got no result well!");
                return null;
            }
            ArrayList<WellFlowData> contextwells = this.getWellsInContext();

            if (contextwells == null) {
                return null;
            }

            nnBg = this.computeMedian(contextwells, nnBg);
        }

        WellFlowDataResult diffdata = WellFlowDataResult.createSimilarEmtpyWell(nnBg);

        int nrframes = nnBg.getNrFrames();
        // now also compute difference
        WellFlowData welldata = getRawWell();
        for (int f = 0; f < nrframes; f++) {
            diffdata.getData()[f] = welldata.getData()[f] - nnBg.getData()[f];
        }
        //now we have the sum of all value. Can compute average now...
        // or mean?
        nnBg.setName("Nearest neighbor, span " + getSpan());
        nnBg.setResultType(ResultType.MEDIAN);
        nnBg.setDescription("BG computation using empty wells (mean). Found " + this.getNrEmpty() + " wells within " + getSpan() + " wells in each direction");

        diffdata.setName("Raw-nn BG");
        diffdata.setResultType(ResultType.NN_RAW_BG);
        ResultType.NN_RAW_BG.setShow(true);
        diffdata.setDescription("Raw - BG subtraction using empty wells. Found " + this.getNrEmpty() + " wells within " + getSpan() + " wells in each direction");
        results.add(nnBg);
        results.add(diffdata);

        this.cacheResult(getKey() + ":nn", nnBg);
        return results;
    }
}
