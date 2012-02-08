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
package com.iontorrent.wellalgorithms;

import com.iontorrent.expmodel.Settings;
import com.iontorrent.rawdataaccess.pgmacquisition.RawDataFacade;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.rawdataaccess.wells.BfMask;
import com.iontorrent.rawdataaccess.wells.BfMaskDataPoint;
import com.iontorrent.rawdataaccess.wells.BitMask;
import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.utils.ProgressListener;
import com.iontorrent.utils.stats.HistoStatistics;
import com.iontorrent.utils.stats.XYStats;
import com.iontorrent.wellmodel.RasterData;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellFlowData;
import com.iontorrent.wellmodel.WellFlowDataResult;
import com.iontorrent.wellmodel.WellFlowDataResult.ResultType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class WellAlgorithm {

    public static final int DEFAULT_SPAN = 8;
    private int NRBUCKETS = 50;
    private int span;
    protected int nrEmpty;
    private String msg;
    private ProgressListener listener;
    protected RawDataFacade io;
    protected ArrayList<WellFlowDataResult> results;
    protected WellContextFilter filter;
    protected boolean allWells;
    private static HashMap<String, WellFlowData> resultscache = new HashMap<String, WellFlowData>();
    private String key;
    private String medianfunction;
    private boolean mode;
    private boolean median;
    private boolean mean;

    public WellAlgorithm() {
        this(null, 5, false, "median");
    }

    public WellAlgorithm(WellContextFilter filter, int span, boolean allWells) {
        this(filter, span, allWells, "median");
    }

    public WellAlgorithm(WellContextFilter filter, int span, boolean allWells, String medianfunction) {
        this.filter = filter;
        this.allWells = allWells;
        this.span = span;
        this.medianfunction = medianfunction;

//        p("Got median function: " + medianfunction);
        if (medianfunction == null || medianfunction.equalsIgnoreCase("median")) {
            median = true;
            //  Exception e = new Exception("Tracking");
            //  p(ErrorHandler.getString(e));
        } else if (medianfunction.equalsIgnoreCase("mean")) {
            mean = true;
        } else if (medianfunction.equalsIgnoreCase("mode")) {
            mode = true;
        }
        results = new ArrayList<WellFlowDataResult>();
        msg = "";
        if (filter != null) {
            String raw = filter.getContext().getRawDirectory();
            String cache = filter.getContext().getCacheDirectory();
            this.key = filter.getKey() + ":all=" + allWells + ":s=" + span;
            //  if (!allWells) key = key + filter.getCoord();
            io = RawDataFacade.getFacade(raw, cache, filter.getRawtype());
        }
    }

    protected String getKey() {
        return key;
    }

    protected void cacheResult(String key, WellFlowData res) {
        if (resultscache.size() > 100) {
            //      p("Clearing well alg cache");
            resultscache.clear();
        }
        //  p("Caching result for "+key);
        resultscache.put(key, res);
    }
    public static void clear() {
        if (resultscache != null) resultscache.clear();
    }

    public WellFlowData getCachedResult(String key) {
        WellFlowData res = resultscache.get(key);
        if (res != null) {
            //    p("Found result for "+key+" in cache");
        }
        //  else p(key+" result NOT in cache");
        return res;
    }

    public ArrayList<WellCoordinate> getCoords() {
        return filter.getContext().getSelection().getAllWells();
    }

    public WellFlowDataResult computeMedian(ArrayList<WellFlowData> contextwells) {
        return computeMedian(contextwells, null);
    }

    public WellFlowDataResult computeMedianRaw(ArrayList<WellCoordinate> coords) {
        ArrayList<WellFlowData> wells = io.readAllWellsAndTransform(coords, filter.getFlow(), filter.getContext());
        WellFlowDataResult avgwell = computeMedian(wells);
        return avgwell;
    }

    public String getName() {
        return "well algorithm";
    }

    public boolean ok(BitMask ignoremask, BitMask take, int c, int r) {
        if (take == null && ignoremask == null) {
            return true;
        } else if (take == null) {
            return !ignoremask.get(c, r);
        } else if (ignoremask == null) {
            return take.get(c, r);
        }
        else if (take == null) {
            return !ignoremask.get(c, r);
        }
        else if (ignoremask == null) {
            return take.get(c, r);
        }

        boolean b = ((!ignoremask.get(c, r)) && take.get(c, r));
//        if (Math.random() > 0.999999) {
//            p(c + "/" + r + ", ignmore name=" + ignoremask.getName() + "/take name=" + take.getName() + ", ignore=" + ignoremask.getMaskAt(c, r) + "=" + ignoremask.get(c, r)
//                    + ", take=" + take.getMaskAt(c, r) + "=" + take.get(c, r) + "-> result: " + b);
//            //  p(" ~1: "+(~1)+", ~2:"+(~2)+", ~0: "+(~0));
//        }
        return b;
    }

    public WellFlowDataResult computeMedian(RasterData data, int flow, RawType type, BitMask ignore, BitMask takemask) {
        int nrframes = data.getFrames_per_flow();
        double medianvalues[] = new double[nrframes];

        int size = data.getRaster_size();
        int nrwells = (size * size);

        float[][][][] vals = data.getRasterdata();

        p("================ Computing median for entire rasterdata, size " + size);

        for (int f = 0; f < nrframes; f++) {
            float[] oneframe = new float[nrwells];
            int i = 0;
            for (int c = 1; c + 1 < size; c++) {
                for (int r = 1; r + 1 < size; r++) {
                    //   boolean show = Math.random() > 0.9999;

                    if (ok(ignore, takemask, c, r)) {

                        float val = vals[c][r][0][f];
//                        if (show) {
//                            p("value " + c + "/" + r + ", f=" + f + " ,  value: " + val);
//                        }
                        if (val < 160000 && val > -100000) {
                            oneframe[i] = vals[c][r][0][f];
                            i++;
                        }

                    }
                }
            }
            float[] tmp = new float[i];
            //System.r
            System.arraycopy(oneframe, 0, tmp, 0, i);
            if (i < 10) {
                err("Very small nr values for median:" + i + ":" + Arrays.toString(tmp));
                p("ignmore mask:" + ignore + ", takemask: " + takemask);
                // compute intersect
                BitMask inter = new BitMask(ignore);
                inter.subtract(takemask, ignore);
                p("take - inter: " + inter.computePercentage() + "% wells");
            }
            medianvalues[f] = computeMeanModeOrMedian(tmp);
            //    p("Median of " + tmp.length + "  values: " + medianvalues[f]);

        }

        WellFlowDataResult res = new WellFlowDataResult(0, 0, flow, type, null);
        res.setTimestamps(data.getTimeStamps(0));
        res.setResultType(ResultType.NN_RAW_BG);
        ResultType.NN_RAW_BG.setShow(true);
        res.setName("Median");
        res.setDescription("Median signal over entire area, bg subtracted");
        res.setData(medianvalues);
        // p("Got median:" + res + ", " + Arrays.toString(medianvalues));
        return res;
    }

    public double[] computeMedian(RasterData data, BitMask ignoremask, BitMask takemask, int span, int x, int y) {
        int nrframes = data.getFrames_per_flow();
        double medianvalues[] = new double[nrframes];
        int size = data.getRaster_size();
        float[][][][] vals = data.getRasterdata();

        span = span / 2;
        int r1 = Math.max(0, y - span);
        int r2 = Math.min(size, y + span);

        int c1 = Math.max(0, x - span);
        int c2 = Math.min(size, x + span);
        int nrwells = (2 * span + 1) * (2 * span + 1);

        int flow = 0;
        for (int f = 0; f < nrframes; f++) {
            double[] oneframe = new double[nrwells];
            int i = 0;
            for (int c = c1; c < c2; c++) {
                for (int r = r1; r < r2; r++) {
                    if (ok(ignoremask, takemask, c, r)) {
                        float val = vals[c][r][0][f];
                        if (val != 0) {
                            oneframe[i] = vals[c][r][flow][f];
                            i++;
                        }
                    }
                }
            }

            int[] tmp = new int[i];
            System.arraycopy(oneframe, 0, tmp, 0, i);
            medianvalues[f] = computeMeanModeOrMedian(tmp);
//            if (f == 10 && Math.random()>0.99) {
//                p("got " + i + " good wells, median is: " + medianvalues[f]);
//            }
//            if (medianvalues[f] == 0) {
//                p("Got 0 for median for frame " + f + " and key " + key);
//            }
        }

        return medianvalues;
    }

    private boolean pinned(float[] vals) {
        boolean same = true;
        for (int i = 0; i < vals.length; i++) {
            float v = vals[i];
            if (v > Settings.PIN_MAX || v < Settings.PIN_MIN) {
                return true;
            }
            if (i > 0) {
                if (v != vals[i - 1]) {
                    same = false;
                }
            }
        }
        return same;
    }

//    public long[][][] computeSums(RasterData data, BitMask ignoremask, BitMask takemask) {
//        int nrframes = data.getFrames_per_flow();
//        int size = data.getRaster_size();
//        long[][][] sums = new long[size][size][nrframes];
//        long[][][] rowsums = new long[size][size][nrframes];
//        long[][][] colsums = new long[size][size][nrframes];
//        float[][][][] vals = data.getRasterdata();
//
//        p("computing sums");
//        int flow = 0;
//        for (int c = 0; c < size; c++) {
//            for (int r = 0; r < size; r++) {
//                // boolean show = Math.random() > 0.99;
//                boolean show = false;
//                if (!pinned(vals[c][r][0])) {
////                    if (show) {
////                        p("computeSums: not pinned: " + c + "/" + r);
////                    }
//                    if (ok(ignoremask, takemask, c, r)) {
//                        int cprev = 0;
//                        int rprev = 0;
//                        if (c > 0) {
//                            cprev = c - 1;
//                        }
//                        if (r > 0) {
//                            rprev = r - 1;
//                        }
//
////                        if (show) {
////                            p("computeSums: not pinned: " + c + "/" + r + ", prevc=" + cprev + ", rprevr=" + rprev);
////                        }
//                        for (int f = 0; f < nrframes; f++) {
//                            colsums[c][r][f] = vals[c][r][flow][f];
//                            if (ok(ignoremask, takemask, c, rprev)) {
//                                colsums[c][r][f] += colsums[c][rprev][f];
//                            }
//
//                            rowsums[c][r][f] = vals[c][r][flow][f];
//                            if (ok(ignoremask, takemask, cprev, r)) {
//                                rowsums[c][r][f] += rowsums[cprev][r][f];
//                            }
//
//                            long tot = colsums[c][r][f] + rowsums[c][r][f] + vals[c][r][flow][f];
//                            if (ok(ignoremask, takemask, cprev, rprev)) {
//                                tot += sums[cprev][rprev][f];
//                                if (show && f == 10) {
//                                    p("computeSums: adding prev sum: " + sums[cprev][rprev][f]);
//                                }
//                            } else if (show && f == 10) {
//                                p("computeSums: prev pinned, not adding");
//                            }
//                            // add sums from cols/rows
//
//                            sums[c][r][f] = tot;
//                            if (show && f == 10) {
//                                p("sum: " + tot);
//                            }
//                            if (tot >= Long.MAX_VALUE - 1) {
//                                err("computeSums: Maximum reached for long in computeSums!");
//                            }
//                        }
//                    } else if (show) {
//                        p("computeSums: did not pass mask: " + c + "/" + r);
//                    }
//                } else if (show) {
//                    p("computeSums: pinned: " + c + "/" + r + ", " + vals[c][r]);
//                }
//            }
//        }
//
//        return sums;
//    }
    //private boolean  checkPinned(long vals[]) {
    //private boolean isPinned(int c, int )
    public WellFlowDataResult computeMedian(ArrayList<WellFlowData> contextwells, WellFlowData refwell) {
        //  p("Computing median for "+contextwells.size()+" wells and flow " + getFlow()+" and span "+span);

        String key = getKey();
        key += ":context=" + contextwells.size();

        WellFlowDataResult resdata = (WellFlowDataResult) this.getCachedResult(key + ":median");
        if (resdata != null) {
            //       p("Found median in cache: "+Arrays.toString(resdata.getData()));
            return resdata;
        }
        if (refwell == null) {
            refwell = createResultWell();
        }
        resdata = WellFlowDataResult.createSimilarEmtpyWell(refwell);
        int nrframes = resdata.getNrFrames();

        int nrwells = contextwells.size();

        if (nrwells == 0) {
            return null;
        }

        double medianvalues[] = new double[nrframes];


        for (int f = 0; f < nrframes; f++) {
            double[] oneframe = new double[nrwells];
            for (int wellnr = 0; wellnr < nrwells; wellnr++) {
                WellFlowData emptywell = contextwells.get(wellnr);
                oneframe[wellnr] = emptywell.getData()[f];
            }
            medianvalues[f] = computeMeanModeOrMedian(oneframe);
            if (medianvalues[f] == 0) {
                //       p("Got 0 for median for frame " + f + " and key " + key);
            }
        }
        resdata.setResultType(ResultType.MEDIAN);
        resdata.setName("Median");
        resdata.setData(medianvalues);
        //    p("Caching median: "+Arrays.toString(resdata.getData()));
        this.cacheResult(key + ":median", resdata);
        return resdata;
    }

    public static double getMedian(ArrayList<Long> values) {
        Collections.sort(values);

        if (values.size() % 2 == 1) {
            return values.get((values.size() + 1) / 2 - 1);
        } else {
            long lower = values.get(values.size() / 2 - 1);
            long upper = values.get(values.size() / 2);

            return (long) ((lower + upper) / 2.0);
        }
    }

    public double computeMeanModeOrMedian(int[] values) {
        if (values == null || values.length < 1) {
            return 0;
        }
        if (values.length < 2) {
            return values[0];
        }
        if (median) {
            // p("computing MEDIAN");
            return computeMedianInt(values);
        } else if (mean) {
            int sum = 0;
            for (int i : values) {
                sum += i;
            }
            double res = (double) sum / (double) values.length;
            ///p("computed MEAN");
            return res;
        } else { // mode: the values that appears most often - in a bucket            
            int minx = values[0];
            int maxx = values[0];
            for (int i = 0; i < values.length; i++) {
                if (values[i] > maxx) {
                    maxx = values[i];
                }
                if (values[i] < minx) {
                    minx = values[i];
                }
            }
            double interval = Math.max(0.01, (double) (maxx - minx) / (double) NRBUCKETS);

            //   p("delta bucket interval: " + interval);
            XYStats stats = XYStats.createStats(values, interval, minx, maxx);
            HistoStatistics histo = stats.createStatistics();
            histo.normalize();
            // get the bucket with the most values!
            double res = histo.getXValueForMaxY();
            p("computed MODE: " + res + ", min=" + minx + ", maxx=" + maxx + ", interval: " + interval + "histo: " + histo.toString());
            return res;
        }
    }

    public double computeMeanModeOrMedian(double[] values) {
        if (values == null || values.length < 1) {
            return 0;
        }
        if (values.length < 2) {
            return values[0];
        }
        if (median) {
            // p("computing MEDIAN");
            return computeMedianDouble(values);
        } else if (mean) {
            int sum = 0;
            for (double i : values) {
                sum += i;
            }
            double res = (double) sum / (double) values.length;
            ///p("computed MEAN");
            return res;
        } else { // mode: the values that appears most often - in a bucket            
            double minx = values[0];
            double maxx = values[0];
            for (int i = 0; i < values.length; i++) {
                if (values[i] > maxx) {
                    maxx = values[i];
                }
                if (values[i] < minx) {
                    minx = values[i];
                }
            }
            double interval = Math.max(0.01, (double) (maxx - minx) / (double) NRBUCKETS);

            //   p("delta bucket interval: " + interval);
            XYStats stats = XYStats.createStats(values, interval, minx, maxx);
            HistoStatistics histo = stats.createStatistics();
            histo.normalize();
            // get the bucket with the most values!
            double res = histo.getXValueForMaxY();
            p("computed MODE: " + res + ", min=" + minx + ", maxx=" + maxx + ", interval: " + interval + "histo: " + histo.toString());
            return res;
        }
    }

    public double computeMeanModeOrMedian(float[] values) {
        if (values == null || values.length < 1) {
            return 0;
        }
        if (values.length < 2) {
            return values[0];
        }
        if (median) {
            return computeMedianFloat(values);
        } else if (mean) {
            double sum = 0;
            for (double d : values) {
                sum += d;
            }
            return sum / values.length;
        } else { // mode: the values that appears most often - in a bucket            
            double minx = values[0];
            double maxx = values[0];
            for (int i = 0; i < values.length; i++) {
                if (values[i] > maxx) {
                    maxx = values[i];
                }
                if (values[i] < minx) {
                    minx = values[i];
                }
            }
            double interval = Math.max(0.01, (double) (maxx - minx) / (double) NRBUCKETS);
            //   p("delta bucket interval: " + interval);
            XYStats stats = XYStats.createStats(values, interval, minx, maxx);
            HistoStatistics histo = stats.createStatistics();
            // get the bucket with the most values!
            double res = histo.getXValueForMaxY();
            p("computed MODE: " + res);
            return res;
        }
    }

    public double computeMeanModeOrMedian(long[] values) {
        if (values == null || values.length < 1) {
            return 0;
        }
        if (values.length < 2) {
            return values[0];
        }
        if (median) {
            return computeMedianLong(values);
        } else if (mean) {
            double sum = 0;
            for (double l : values) {
                sum += l;
            }
            return (double) sum / (double) values.length;
        } else { // mode: the values that appears most often - in a bucket            
            long minx = values[0];
            long maxx = values[0];
            for (int i = 0; i < values.length; i++) {
                if (values[i] > maxx) {
                    maxx = values[i];
                }
                if (values[i] < minx) {
                    minx = values[i];
                }
            }
            double interval = Math.max(0.01, (double) (maxx - minx) / (double) NRBUCKETS);
            //   p("delta bucket interval: " + interval);
            XYStats stats = XYStats.createStats(values, interval, minx, maxx);
            HistoStatistics histo = stats.createStatistics();
            // get the bucket with the most values!
            double res = histo.getXValueForMaxY();
            p("computed MODE: " + res);
            return res;
        }
    }

    public WellContextFilter getFilter() {
        return filter;
    }

    protected ArrayList<WellFlowData> getWellsInContext() {
        ArrayList<WellFlowData> contextwells = new ArrayList<WellFlowData>();
        int wx = getCoord().getCol();
        int wy = getCoord().getRow();
        int sx = Math.max(0, wx - getSpan() / 2);
        int sy = Math.max(0, wy - getSpan() / 2);
        int ex = Math.min(filter.getContext().getNrCols(), sx + 2 * getSpan() / 2);
        int ey = Math.min(filter.getContext().getNrRows(), sy + 2 * getSpan() / 2);
        int total = 4 * span * span;
        if (this.allWells) {
            // use ALL wells in area, and not just the ones surrounding this well specified by span!

            WellCoordinate c1 = this.getFilter().getContext().getSelection().getCoord1();
            WellCoordinate c2 = this.getFilter().getContext().getSelection().getCoord2();
            wx = -1;
            wy = -1;
            sx = Math.min(c1.getCol(), sx);
            sy = Math.min(c1.getRow(), sy);
            ex = Math.max(c2.getCol(), ex);
            ey = Math.max(c2.getRow(), ey);
            p("getWellsInContext: Using ALL wells in area " + c1 + "-" + c2);
            total = (ex - sx) * (ey - sy);
            ///
        }

        double incr = 100.0d / (double) (total + 1);
        double progress = 0;
        BfMask mask = filter.getContext().getMask();

        for (int x = sx; x < ex; x++) {
            for (int y = sy; y < ey; y++) {
                progress += incr;
                //   p("Processing "+x+"/"+y+", progress="+progress);
                if (getListener() != null) {
                    getListener().setProgressValue(Math.min(100, (int) progress));
                }
                if (y != wy && x != wx) {
                    //   p("Checking "+x+"/"+y+", mask:"+context.getMask().getDataPointAt(x, y).toString());
                    if (mask != null) {
                        if (BfMaskDataPoint.hasFlags(filter.getMusthaveflags(), filter.getNothaveflags(), mask.getMaskAt(x, y))) {
                            //if (mask.getDataPointAt(x, y).hasFlags(filter.getMusthaveflags(), filter.getNothaveflags())) {

                            // WellFlowData emptywell = io.readOneWell(new WellCoordinate(x, y), getFlow());
                            WellFlowData emptywell = io.readOneWellAndTransform(new WellCoordinate(x, y), getFlow(), filter.getContext());
                            if (emptywell != null) {
                                contextwells.add(emptywell);
                            } else {
                                return null;
                            }
                        }
                    } else {
                        WellFlowData emptywell = io.readOneWellAndTransform(new WellCoordinate(x, y), getFlow(), filter.getContext());
                        if (emptywell != null) {
                            contextwells.add(emptywell);
                        } else {
                            return null;
                        }
                    }
                }

            }
        }
        nrEmpty = contextwells.size();

        if (nrEmpty > 0) {
            p("got " + nrEmpty + " context wells around " + getCoord());
        } else {
            msg += "There were no wells around " + getCoord() + " with span " + getSpan() + " and specified flags";
            return null;
        }
        return contextwells;
    }

    protected WellFlowDataResult createResultWell() {
        return createResultWell(filter.getContext(), getCoord(), getFlow(), filter.getRawtype());
    }

    public WellFlowData getRawWell() {
        String key = getKey();
        if (!allWells) {
            key = key + filter.getCoord();
        }
        //  else p("allWells is true, not adding coord to key");
        WellFlowData rawData = this.getCachedResult(key + ":raw");
        if (rawData != null) {
            if (rawData.getData() != null) {
                //    p("Found cached data:"+key);
                return rawData;
            }
        }
        if (this.allWells) {
            //    p("Computing median raw using ALL "+getCoords().size()+" coordinates");
            rawData = this.computeMedianRaw(getCoords());
        } else {
            p("Getting single well raw data for " + getCoord());
            //   rawData = io.readOneWell(getCoord(), getFlow());
            rawData = io.readOneWellAndTransform(getCoord(), getFlow(), filter.getContext());
        }
        if (rawData != null) {
            this.cacheResult(key + ":raw", rawData);
        }
        return rawData;
    }

    protected static WellFlowDataResult createResultWell(WellContext context, WellCoordinate coord, int flow, RawType type) {
        RawDataFacade io = RawDataFacade.getFacade(context.getRawDirectory(), context.getCacheDirectory(), type);
        WellFlowData welldata = io.readOneWellAndTransform(coord, flow, context);
        //  WellFlowData welldata = io.readOneWell(coord, flow);
        if (welldata == null) {
            //    msg += "Got no well data at " + coord + ", " + coord.getMaskdata().toString();
            // p(getMsg());
            return null;
        }

        WellFlowDataResult resultwell = WellFlowDataResult.createSimilarEmtpyWell(welldata);
        return resultwell;
    }

    public ArrayList<WellFlowDataResult> compute() {
        err("Not computing anything by default");
        return null;
    }

    /** ================== LOGGING ===================== */
    protected void err(String msg, Exception ex) {
        System.out.println("WellAlgorithm: " + msg);
        Logger.getLogger(WellAlgorithm.class.getName()).log(Level.SEVERE, msg, ex);
    }

    protected void err(String msg) {
        System.out.println("WellAlgorithm: " + msg);
        Logger.getLogger(WellAlgorithm.class.getName()).log(Level.SEVERE, msg);
    }

    protected void warn(String msg) {
        System.out.println("WellAlgorithm: " + msg);
        Logger.getLogger(WellAlgorithm.class.getName()).log(Level.WARNING, msg);
    }

    protected void p(String msg) {
        System.out.println("WellAlgorithm: " + msg);
        //Logger.getLogger( WellAlgorithm.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the span
     */
    public int getSpan() {
        return span;
    }

    /**
     * @return the nrEmpty
     */
    public int getNrEmpty() {
        return nrEmpty;
    }

    /**
     * @return the msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     * @return the listener
     */
    public ProgressListener getListener() {
        return listener;
    }

    /**
     * @param listener the listener to set
     */
    public void setListener(ProgressListener listener) {
        this.listener = listener;
    }

    /**
     * @return the rawtype
     */
    public RawType getRawtype() {
        return filter.getRawtype();
    }

    /**
     * @return the flow
     */
    public int getFlow() {
        return filter.getFlow();
    }

    /**
     * @return the coord
     */
    public WellCoordinate getCoord() {
        return filter.getCoord();
    }

    protected static double computeMedianDouble(double[] values) {
        Arrays.sort(values);
        //for (int i = 1; i < values.length; i++)
        int len = values.length;

        if (len % 2 == 1) {
            return values[(len + 1) / 2 - 1];
        } else {
            double lower = values[len / 2 - 1];
            double upper = values[len / 2];

            return ((lower + upper) / 2.0);
        }
    }

    protected static float computeMedianFloat(float[] values) {
        Arrays.sort(values);
        //for (int i = 1; i < values.length; i++)
        int len = values.length;

        if (len % 2 == 1) {
            return values[(len + 1) / 2 - 1];
        } else {
            double lower = values[len / 2 - 1];
            double upper = values[len / 2];

            return (float) ((lower + upper) / 2.0f);
        }
    }

    protected static int computeMedianInt(int[] values) {
        Arrays.sort(values);


        if (values.length % 2 == 1) {
            return values[(values.length + 1) / 2 - 1];
        } else {
            int lower = values[values.length / 2 - 1];
            int upper = values[values.length / 2];

            return (int) ((lower + upper) / 2.0);
        }
    }

    protected static long computeMedianLong(long[] values) {
        Arrays.sort(values);

        if (values.length % 2 == 1) {
            return values[(values.length + 1) / 2 - 1];
        } else {
            long lower = values[values.length / 2 - 1];
            long upper = values[values.length / 2];

            return (long) ((lower + upper) / 2.0);
        }
    }
}
