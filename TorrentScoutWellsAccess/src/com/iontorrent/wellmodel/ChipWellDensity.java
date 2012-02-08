/*
 * Copyright (C) 2011 Life Technologies Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.iontorrent.wellmodel;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.rawdataaccess.pgmacquisition.PGMAcquisitionGlobalHeader;
import com.iontorrent.rawdataaccess.pgmacquisition.RawDataFacade;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.rawdataaccess.wells.BfMaskFlag;
import com.iontorrent.rawdataaccess.wells.GeneralWellDensity;
import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.utils.ProgressListener;
import com.iontorrent.utils.io.FileUtils;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class ChipWellDensity extends GeneralWellDensity {

    private static String lastError;
    private static Exception lastException;
    private ExperimentContext context;
    private BfHeatMap mask;
    //  public static int DEFAULT_BUCKET_SIZE = 5;    
    private int well_density[][][];
    private int maxvalues[];
    private int minvalues[];
    private boolean[] done;
    private BfMaskFlag curflag;
    private int nrflags;
    RawType type;
    int flow;
    int frame;

    public ChipWellDensity(ExperimentContext compexp, int flow, RawType type, int frame) {
        super(2);
        this.flow = flow;
        this.frame = frame;
        this.type = type;
        this.context = compexp;
        p("Created chipwelldensity with raw dir " + compexp.getRawDir());
    }

    public int getNrFlags() {
        return Math.max(1, nrflags);
    }

    public String createHeatMapImages(ProgressListener progress) {
        p("========================createCompositeImages");
        int totcols = context.getNrcols();
        int totrows = context.getNrrows();
        if (totcols == 0 || totcols == 0) {
            p("Got no totcols/totrows, trying to find them via context.findColsRows");
            context.findColsRows(flow, type);
            totcols = context.getNrcols();
            totrows = context.getNrrows();
        }
        if (totcols == 0 || totrows == 0) {
            err("Could not find out cols/rows");
        }
        mask = BfHeatMap.getMask(context);
        int bucketsize = mask.GRID;
        int compcols = totcols / bucketsize;
        int comprows = totrows / bucketsize;

        nrflags = 1;

        String imageFileOrUrl = mask.getImageFile("chip",BfMaskFlag.RAW, flow, type, frame);
        p("Checking file " + imageFileOrUrl);
        if (FileUtils.exists(imageFileOrUrl)) {
            p("Already created file "+imageFileOrUrl);
            if (progress != null) {
                progress.setProgressValue(100);
            }
            return null;
        }

        p("Creating heat map data structure for " + compcols + "/" + comprows + "  composite cols and rows, bucket=" + bucketsize + ", tot cols/rows=" + totcols + "/" + totrows);
        int[][][] fullimage = new int[nrflags][compcols][comprows];

        // for all blocks

      //  p("Processing " + context);
        int startcol = 0;
        int startrow = 0;
        double prog = 0;
//            
       
        DatWellDensity wellDensity = new DatWellDensity(context, bucketsize, type, flow, frame);
        try {
            String errmsg = wellDensity.computeDensityPlot();
            if (errmsg != null) {
                p("Could not comput well density: "+errmsg);
                return errmsg;
            }
        } catch (Exception e) {
            err("Could not compute well density for exp " + context, e);
            return "Coul dnot compute density: "+ErrorHandler.getString(e);
        }
        int w = context.getNrcols();
        int h = context.getNrrows();
        p("DatWellDensity  size=" + wellDensity.getNrCols() + "/" + wellDensity.getNrRows() + ", should be =" + w / bucketsize + "/" + h / bucketsize);
        int maxc = Math.min(wellDensity.getNrCols(), w / bucketsize);
        int maxr = Math.min(wellDensity.getNrRows(), h / bucketsize);
        if (maxc < 1 || maxr < 1) {
            err("Got illegal rows cols from dat well density");
            return "Got illegal rows cols from dat well density";
        }
        if (compcols < 1 || comprows < 1) {
            err("Got illegal comprows or compcols from dat well density");
            return "Got illegal comprows or compcols from dat well density";
        }
        p("maxc/maxr=" + maxc + "/" + maxr);
        double inc = 100.0/maxc;
        // wellDensity.setFlag(flag);
        for (int c = 0; c < maxc; c++) {
            prog += inc;
            if (progress != null) progress.setProgressValue((int)prog);
            for (int r = 0; r < maxr; r++) {
                int nr = wellDensity.getCount(c, r);
                if (startcol + c < compcols && startrow + r < comprows) {
                    fullimage[0][startcol + c][startrow + r] = nr;
                } else {
                  //  p("out of bounds " + (startcol + c) + "/" + (startrow + r)+", there are only "+compcols+"/"+comprows+" rows and cols");
                }
            }

        }
        createImageFile(fullimage[0]);
        p("Done creating composite heat maps");
        return null;
    }

    private String createImageFile(int[][] data) {

        String imageFileOrUrl = mask.getImageFile("chip",BfMaskFlag.RAW, flow, type, frame);
        p("Creating image file " + imageFileOrUrl);
        if (data.length < 1) {
            err("data array too small: " + data.length);
        }
        // saves the data to an image file
        if (FileUtils.isUrl(imageFileOrUrl)) {
            return "<br>The file " + imageFileOrUrl + " is an URL, but I have to store the image in a file";
        }
        int cols = data.length;
        int rows = data[0].length;
        if (cols < 1 || rows < 1) {
            err("Got strange rows/cols value: " + rows + "/" + cols);
        }
        BufferedImage image = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();
        //  p("Writing image " + imageFileOrUrl);
        boolean hasdata = false;
        int tot = 0;
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                int val = data[c][r];
                if (val > 0) {
                    tot += val;
                    hasdata = true;
                }
                // convert to rgb
                int red = val % 256;
                val = val / 256;
                int green = val % 256;

                val = val / 256;
                int blue = val % 256;// rrggbb
                raster.setSample(c, r, 0, red);
                raster.setSample(c, r, 1, green);
                raster.setSample(c, r, 2, blue);
            }
        }
        image.setData(raster);
        if (!hasdata) {
            err("No data in image ");
        } else {
            p("Got " + tot + "  values");
        }
        try {
            //  p("writing buffered image to " + imageFileOrUrl);
            ImageIO.write(image, "BMP", new File(imageFileOrUrl));
            //   p("Successfully wrote " + imageFileOrUrl + " for flag " + flag);
            return null;


        } catch (Exception ex) {
            Logger.getLogger(CompositeWellDensity.class.getName()).log(Level.SEVERE, null, ex);

            return "Could not write data to file " + imageFileOrUrl
                    + ":" + ex.getMessage();
        }

    }

    public void initDensityPlot() {
      //  p(" initDensityPlot: mask.cols = "+mask.getNrCols()+", bucket size="+bucket_size);
        int nr_col_buckets = mask.getNrCols() / bucket_size + 1;
        int nr_row_buckets = mask.getNrRows() / bucket_size + 1;
        nrflags = BfMaskFlag.getNrFlags();
      //  p("initDensityPlot: bucket: " + bucket_size + ", nr cols: " + nr_col_buckets+"/"+nr_row_buckets + ", nrwells: " + bucket_size * nr_col_buckets);
        well_density = new int[nr_col_buckets][nr_row_buckets][nrflags];
        maxvalues = new int[nrflags];
        minvalues = new int[nrflags];
        done = new boolean[nrflags];
    }

    @Override
    public void setFlag(Object smflag) {
        this.curflag = (BfMaskFlag) smflag;
        computeDensityPlot();
    }

    public void setMask(BfHeatMap mask) {
        this.mask = mask;
        this.initDensityPlot();


    }

    @Override
    public Object getFlag() {
        return curflag;
    }

    private String computeDensityForFlag(BfMaskFlag smflag) {
        int code = smflag.getCode();
        if (this.nrflags == 1) {
            smflag = BfMaskFlag.RAW;
            code = 0;
        }

//        if (done[code]) {
//            p("Done with "+code+", not computing");
//            return null;
//        }
        // for (int flag = 0; flag < ScoreMaskFlag.getNrFlags(); flag++) {
        int max = 0;
        int min = Integer.MAX_VALUE;
        BfMaskFlag scoreflag = BfMaskFlag.get(code);
        int size = this.bucket_size*bucket_size;
        p("Computing density for flag " + scoreflag.toString() + ", cols/rows=" + well_density.length);
        for (int c = 0; c < well_density.length; c++) {
            for (int r = 0; r < well_density[0].length; r++) {
                // compute density for this area                
                int count = (int) computeDensity(c, r, scoreflag)/size;
                if (count > max) {
                    max = count;
                }
                if (count < min) {
                    min = count;
                }
                well_density[c][r][code] = (int) count;
            }
        }
        maxvalues[code] = max;
        minvalues[code] = min;
        p("max: " + max);
        done[code] = true;
        return null;
        //  }
    }

    @Override
    public int getNrCols() {
        return well_density.length;
    }

    @Override
    public int getNrRows() {
        return well_density[0].length;
    }

    @Override
    public int getMax() {
        return maxvalues[curflag.getCode()];
    }

    @Override
    public int getMin() {
        return minvalues[curflag.getCode()];
    }

    @Override
    public int getCount(int c, int r) {
        return getCount(c, r, curflag);
    }

    public int getCount(int c, int r, BfMaskFlag flag) {
        return well_density[c][r][flag.getCode()];
    }

    private double computeDensity(int col, int row, BfMaskFlag scoreflag) {
        if (scoreflag == null) {
            return -1;
        }
        double count = 0;
        for (int c = col * bucket_size; c < Math.min((col + 1) * bucket_size, mask.getNrCols()); c++) {
            for (int r = row * bucket_size; r < Math.min((row + 1) * bucket_size, mask.getNrRows()); r++) {
                int val = mask.getDataPointAt(scoreflag, c, r, false);
                count += val;
//                if (Math.random()>0.9999) {
////                if (c % 100 == 0 && r % 100 == 0) {
//                     p("Test: chipwelldensity.computedensity " + c + "/" + r + ":" + count + " for flag " + scoreflag);                    
//                     if (val == 0) {
//                          p("Got alldata for "+scoreflag+"? "+mask.hasRead(scoreflag));
//                          mask.getDataPointAt(scoreflag, c, r, true);
//                     }
////                }
//                }
            }
        }
        return count;
    }

    public void update(BfMaskFlag scoreMaskFlag) {
        computeDensityForFlag(scoreMaskFlag);
    }

    public BfHeatMap getMask() {
        return this.mask;
    }

    @Override
    public String computeDensityPlot() {
        return computeDensityForFlag(curflag);
    }

    /** ================== LOGGING ===================== */
    public static Exception getLastException() {
        return lastException;
    }

    public static String getLastError() {
        return lastError;
    }

    private static void err(String msg, Exception ex) {
        lastException = ex;
        lastError = msg;
        Logger.getLogger(ChipWellDensity.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        lastError = msg;
        Logger.getLogger(ChipWellDensity.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(ChipWellDensity.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("ChipWellDensity: " + msg);
        Logger.getLogger(ChipWellDensity.class.getName()).log(Level.INFO, msg);
    }
}
