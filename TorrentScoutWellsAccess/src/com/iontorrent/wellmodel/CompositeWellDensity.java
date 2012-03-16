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

import com.iontorrent.expmodel.CompositeExperiment;
import com.iontorrent.expmodel.DatBlock;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.rawdataaccess.wells.BfMask;
import com.iontorrent.rawdataaccess.wells.BfMaskFlag;
import com.iontorrent.rawdataaccess.wells.GeneralWellDensity;
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
public class CompositeWellDensity extends GeneralWellDensity {

    private static String lastError;
    private static Exception lastException;
    private CompositeExperiment compexp;
    private BfHeatMap mask;
    //  public static int DEFAULT_BUCKET_SIZE = 5;    
    private int well_density[][][];
    private int maxvalues[];
    private int minvalues[];
    private boolean[] done;
    private BfMaskFlag curflag;
    private int nrflags; 
    private RawType type;
    private int flow;
    private int frame;
    
    public CompositeWellDensity(CompositeExperiment compexp, RawType type, int flow, int frame, int bucketsize) {
        super(bucketsize);
        this.compexp = compexp;
        this.type = type;
        this.flow = flow;
        this.frame= frame;
    }

    public int getNrFlags() {
        return nrflags;
    }

    public String createCompositeImages(ProgressListener progress, String rawfile, BfMaskFlag currentflag) {
        
        compexp.setCurblock(null);
        int totcols = compexp.getNrcols();
        int totrows = compexp.getNrrows();
        ExperimentContext rootcontext = compexp.getRootContext();
        mask = BfHeatMap.getMask(rootcontext);
        int bucketsize = mask.GRID;
        int compcols = totcols / bucketsize;
        int comprows = totrows / bucketsize;
p("************************ createCompositeImage, bucket="+bucketsize+" Tot rows/cols="+totrows+"/"+totcols+" flag "+currentflag +"********************");        
        DatBlock first = compexp.findBlock(0, 0);
        p("First block: " + first);       
        boolean hasallmasks = true;
         for (DatBlock block : compexp.getBlocks()) {
            ExperimentContext exp = compexp.getContext(block,false);
            WellContext context = exp.getWellContext();
            BfMask blockmask = context.getMask();
            if (blockmask == null) hasallmasks = false;
         }
         
       
            if (FileUtils.exists(rawfile)) {
                p("Already created file "+rawfile);
                if (progress != null) progress.setProgressValue(100);
                return null;
            }
            else p("Could NOT find file yet: "+rawfile);
        
        p("Creating heat map data structure for " + compcols + "/" + comprows + "  composite cols and rows, bucket=" + bucketsize + ", tot cols/rows=" + totcols + "/" + totrows);
        int[][] fullimage = new int[compcols][comprows];

        if (first == null) {
            return "Got no blocks";
        }
        int w = first.getWidth();
        int h = first.getHeight();
        // for all blocks

        p("Processing " + compexp.getNrBlocks() + " blocks");
        double inc = 100.0 / (compexp.getNrBlocks() + 1);
        double prog = 0;
        int errors = 0;
        for (DatBlock block : compexp.getBlocks()) {
            int startcol = block.getStart().getCol() / bucketsize;
            int startrow = block.getStart().getRow() / bucketsize;
            p("=========== Processing block: " + block + ", composite start col/row=" + startcol + "/" + startrow+", flag="+currentflag);
            compexp.setCurblock(block);
            if (progress != null) progress.setProgressValue((int) prog);

            ExperimentContext exp = compexp.getContext(block, false);
            WellContext context = exp.getWellContext();
            BfMask blockmask = context.getMask();
            if (currentflag == BfMaskFlag.RAW) {
                createRawHeatMap(0, exp, bucketsize, w, h, startcol, compcols, startrow, comprows, fullimage);
            } else {
                if (blockmask != null) {
                    p("Got bfmask:" + context.getMask().getNrCols() + "/" + context.getMask().getNrRows() + " cols/rows fro this block");
                    BfWellDensity wellDensity = new BfWellDensity(context.getMask(), bucketsize);
                //    p("welldensity block size=" + wellDensity.getNrCols() + "/" + wellDensity.getNrRows() + ", should be =" + w / bucketsize + "/" + h / bucketsize);
                    int maxc = Math.min(wellDensity.getNrCols(), w / bucketsize);
                    int maxr = Math.min(wellDensity.getNrRows(), h / bucketsize);
                //    p("maxc/maxr=" + maxc + "/" + maxr);
                    // LAST FLAG IS RAW
                //    for (int f = 0; f+1 < nrflags; f++) {
                 //       BfMaskFlag flag = BfMaskFlag.get(f);
                     //   p("Processing flag: "+flag);
                        wellDensity.setFlag(currentflag);
                        for (int c = 0; c < maxc; c++) {
                            for (int r = 0; r < maxr; r++) {
                                int nr = wellDensity.getCount(c, r);
                                if (startcol + c < compcols && startrow + r < comprows) {
                                    fullimage[startcol + c][startrow + r] = nr;
                                } else {
                                   if (errors <10) p("out of bounds " + (startcol + c) + "/" + (startrow + r)+", max is: "+comprows+"/"+comprows);
                                   errors++;
                                }
                            }
                        }
                  //  }
                    // ALSO DO RAW
                   // createRawHeatMap(BfMaskFlag.RAW.getCode(), exp, bucketsize, w, h, startcol, compcols, startrow, comprows, fullimage);
                }
                else p("Found no mask for this block");
            }
            // add to mask
            prog += inc;
        }
        compexp.setCurblock(null);
       
       createImageFile(currentflag, fullimage, rawfile);
        prog += inc;
        if (progress != null) progress.setProgressValue((int) prog);
        p("Done creating composite heat map for flag "+currentflag);
        return null;
    }

    protected void createRawHeatMap(int arraypos, ExperimentContext exp, int bucketsize, int w, int h, int startcol, int compcols, int startrow, int comprows, int[][] fullimage) {
    //    p("No bfmask, will use .dat files");

        DatWellDensity wellDensity = new DatWellDensity(exp, bucketsize, type,flow, frame);
        try {
            String errmsg = wellDensity.computeDensityPlot();
            if (errmsg != null) {
                err(errmsg);
                return;
            }
        } catch (Exception e) {
            err("Could not compute well density for exp " + exp, e);
        }
   //     p("DatWellDensity block size=" + wellDensity.getNrCols() + "/" + wellDensity.getNrRows() + ", should be =" + w / bucketsize + "/" + h / bucketsize);
        int maxc = Math.min(wellDensity.getNrCols(), w / bucketsize);
        int maxr = Math.min(wellDensity.getNrRows(), h / bucketsize);
     //   p("maxc/maxr=" + maxc + "/" + maxr);
        // wellDensity.setFlag(flag);
        int errors = 0;
        for (int c = 0; c < maxc; c++) {
            for (int r = 0; r < maxr; r++) {
                int nr = wellDensity.getCount(c, r);
                if (startcol + c < compcols && startrow + r < comprows) {
                    fullimage[startcol + c][startrow + r] = nr;
                } else {
                  if (errors <10)  p("out of bounds " + (startcol + c) + "/" + (startrow + r));
                  errors++;
                }
            }
        }
    }

    private String createImageFile(BfMaskFlag flag, int[][] data, String rawfile) {

        String imageFileOrUrl = null;
        if (flag == BfMaskFlag.RAW) imageFileOrUrl = rawfile;
        else imageFileOrUrl = mask.getImageFile(flag);
    //    p("Creating image file " + imageFileOrUrl);
        // saves the data to an image file
        if (FileUtils.isUrl(imageFileOrUrl)) {
            return "<br>The file " + imageFileOrUrl + " is an URL, but I have to store the image in a file";
        }
        int cols = data.length;
        int rows = data[0].length;
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
            //err("No data in image ");
        } else {
           // p("Got " + tot + "  values");
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
        int nr_col_buckets = mask.getNrCols() / bucket_size + 1;        
        int nr_row_buckets = mask.getNrRows() / bucket_size + 1;
        nrflags = 1;
//        nrflags = 1;
//        if (compexp.getContext(0).getWellContext() == null) {
//            p("First block got no well context, probably just raw .dat files, so just using ONE flag RAW");
//        } else {
//            nrflags = BfMaskFlag.getNrFlags() - 1;
//        }
        nrflags = BfMaskFlag.getNrFlags();
  //      p("initDensityPlot: bucket: " + bucket_size + ", nr cols: " + nr_col_buckets+"/"+nr_row_buckets + ", nrwells: " + bucket_size * nr_col_buckets);
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
        if (done[code]) {
            return null;
        }
        // for (int flag = 0; flag < ScoreMaskFlag.getNrFlags(); flag++) {
        int max = 0;
        int min = Integer.MAX_VALUE;
        BfMaskFlag scoreflag = BfMaskFlag.get(code);
    //    p("Computing density for flag " + scoreflag.toString() + ", cols/rows=" + well_density.length);
        for (int c = 0; c < well_density.length; c++) {
            for (int r = 0; r < well_density[0].length; r++) {
                // compute density for this area                
                int count = (int) computeDensity(c, r, scoreflag);
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
   //     p("max: " + max);
        done[code] = true;
        //  }
        return null;
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
                count += mask.getDataPointAt(scoreflag, c, r, false);
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
        Logger.getLogger(CompositeWellDensity.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        lastError = msg;
        Logger.getLogger(CompositeWellDensity.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(CompositeWellDensity.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
    //    System.out.println("CompositeWellDensity: " + msg);
        Logger.getLogger(CompositeWellDensity.class.getName()).log(Level.INFO, msg);
    }
}
