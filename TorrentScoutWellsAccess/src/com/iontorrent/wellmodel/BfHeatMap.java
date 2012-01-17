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

import com.iontorrent.utils.io.FileUtils;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.rawdataaccess.wells.BfMaskFlag;
import com.iontorrent.utils.ErrorHandler;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**

 * @author Chantal Roth
 */
public class BfHeatMap {

    private int[][][] alldata;
    private int nRows; // uint32
    private int nCols; //uint32
    public int GRID = 1;
    private ExperimentContext expcontext;
    private boolean done[];
    // private static BfHeatMap mask;

    public static BfHeatMap getMask(ExperimentContext expcontext) {
        BfHeatMap mask = new BfHeatMap(expcontext);
        return mask;

    }

    private BfHeatMap(ExperimentContext expcontext) {
        this.expcontext = expcontext;
        if (expcontext.getNrcols() < 1) {
            expcontext.findColsRows(0, RawType.ACQ);
        }
        updateInfo();
    }

    public void updateInfo() {

        p("Got expcontext with rows/cols: " + expcontext.getNrrows() + "/" + expcontext.getNrcols());
        if (expcontext.getNrrows() < 1300) {
            GRID = 1;
        } else if (expcontext.getNrrows() < 2400) {
            GRID = 2;
        } else if (expcontext.getNrrows() < 4800) {
            GRID = 4;
        }
        else {
            GRID = 8;
        }
        this.nRows = expcontext.getNrrows() / GRID;
        this.nCols = expcontext.getNrcols() / GRID;
        p("GRID IS: "+GRID);
    }

    public String getFile(BfMaskFlag flag) {
        return getImageFile(flag);
    }

    public String getImageFile(BfMaskFlag flag) {
        //if (flag == BfMaskFlag.RAW) {
        //   err("TEST "+ErrorHandler.getString(new Exception("test")));
        // }
        String res = getKey();
        File f = findFileSomewhere(res+"bf_" + flag.getImageName());

        if (f != null) {
            p("Found file " + f);
            return f.toString();
        } else {
            return null;
        }
    }

    public String getImageFile(String maptype, BfMaskFlag flag, int flow, RawType type, int frame) {
        String res = maptype+getKey();
        File f = findFileSomewhere(res+type.name() + "_" + flow + "_" + frame + "_" + flag.getImageName());
        if (f != null) {
            return f.toString();
        } else {
            clearData(flag);
            return null;
        }
    }

    protected String getKey() {
        String res = this.expcontext.getFileKey();
        return res;
    }

    public boolean hasAllImages() {
        for (BfMaskFlag f : BfMaskFlag.values()) {
            if (!hasImage(f)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasImage(String maptype, BfMaskFlag flag, int flow, RawType type, int frame) {
        File f = findFileSomewhere(maptype+getKey()+type.name() + "_" + flow + "_" + frame + "_" + flag.getImageName());
        if (f == null || !f.exists()) {
            clearData(flag);
            p("Could not find  heat map: " + f);
            return false;
        }

        return true;
    }

    public boolean hasImage(BfMaskFlag flag) {
        File f = findFileSomewhere(getKey()+"bf_" + flag.getImageName());
        if (f == null || !f.exists()) {
            p("Could not find  heat map: " + f);
            return false;
        }

        return true;
    }

    private File findFileSomewhere(String filename) {

        String results_path = expcontext.getResultsDirectory();
        String cache_dir = expcontext.getCacheDir();
        String plugin_dir = this.expcontext.getPluginDir();

        File f = new File(results_path + filename);
        //  p("Checking "+f);
        if (f.exists()) {
            return f;
        }

        f = new File(plugin_dir + filename);
        //     p("Checking "+f);
        if (f.exists()) {
            return f;
        }

        f = new File(cache_dir + filename);
        //      p("Checking "+f);
        if (f.exists()) {
            return f;
        }

        if (FileUtils.canWrite(results_path)) {
            f = new File(results_path + filename);
        } else if (FileUtils.canWrite(plugin_dir)) {
            f = new File(plugin_dir + filename);
        } else {
            f = new File(cache_dir + filename);
        }
        return f;
    }

    public int[][] createEmptyData() {
        int[][] data = new int[nCols][nRows];
        return data;
    }

//    public int[][][] readAllData() {
//
//        int nrflags = BfMaskFlag.getNrFlags();
//        if (alldata != null) {
//            //  p("Already read all data");
//        } else {
//            alldata = new int[nrflags][nCols][nRows];
//        }
//        p("Composite BfMaskFlag: reading ALL data for ALL flags (if available)");
//
//        for (BfMaskFlag flag : BfMaskFlag.values()) {
//            alldata[flag.getCode()] = readData(flag, getFile(flag));
//        }
//        return alldata;
//    }
    public int[][] readData(BfMaskFlag flag) {
        if (flag == BfMaskFlag.RAW) {
            err("SHOULD NOT USE THIS METHOD FOR RAW FLAG.TEST " + ErrorHandler.getString(new Exception("test")));
        }
        return readData(flag, getFile(flag));
    }

    private void clearData(BfMaskFlag flag) {
        int nrflags = BfMaskFlag.getNrFlags();
        if (done == null) {
            done = new boolean[nrflags];
        }
        if (done[flag.getCode()]) {
            done[flag.getCode()] = false;
        }
    }

    public boolean hasRead(BfMaskFlag flag) {
        int nrflags = BfMaskFlag.getNrFlags();

         if (alldata == null) {
            p(" all data is NULL");
            alldata = new int[nrflags][nCols][nRows];
            done = null;
        }
        if (done == null) {            
            done = new boolean[nrflags];
        }

       
        if (done[flag.getCode()]) {
            p("Already read data for flag " + flag);
            return true;
        } else {
            return false;
        }
    }

    public int[][] readData(BfMaskFlag flag, String file) {
        int nrflags = BfMaskFlag.getNrFlags();
        p("-------- read Data from  " + file);
        if (nCols == 0 || nRows == 0) {

            p("bad cols rows: " + nCols + "/" + nRows + ", expcontext: " + expcontext.getNrcols());

            this.nRows = expcontext.getNrrows() / GRID;
            this.nCols = expcontext.getNrcols() / GRID;

            done = null;
            p("Clearing ALLDATA");
            alldata = null;
        }

        if (done
                == null) {
            done = new boolean[nrflags];
        }

        if (alldata
                == null) {
            alldata = new int[nrflags][nCols][nRows];
        }

        if (done[flag.getCode()]) {
            p("Already read data for flag " + flag);
            return alldata[flag.getCode()];
        }

        p("readData: ncols=" + nCols);

        if (nCols
                == 0 || nRows
                == 0) {

            err("bad cols rows: " + nCols + "/" + nRows);
        }
        int[][] data = new int[nCols][nRows];


        if (!FileUtils.exists(file)) {
            p("readData: File " + file + " does NOT exist, returning empty data.");
            alldata[flag.getCode()] = data;
            return data;
        } else {
            p("readData. Found file " + file);
        }
        DataInputStream in = FileUtils.openFileOrUrl(file);
        // READ IMAGE AS A FUNCTION OF THE FLAG
        BufferedImage image = null;


        try {
            image = ImageIO.read(in);
        } catch (IOException ex) {
            Logger.getLogger(BfHeatMap.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (image
                == null) {
            err("Could not read image...");
            alldata[flag.getCode()] = data;
            return data;
        }
        //   p("read image " + getFile(flag) + ", extracting data from it. Size=" + image.getData().getBounds());
        Raster raster = image.getData();

        p("Image dims: " + image.getWidth() + "/" + image.getHeight()+", ncols/nrows="+nCols+"/"+nRows);
        int samples = raster.getNumBands();

        for (int x = 0;
                x < Math.min(image.getWidth(), nCols); x++) {
            for (int y = 0; y < Math.min(image.getHeight(), nRows); y++) {
                int r = raster.getSample(x, y, 0);
                int g = 0;
                if (samples > 1) {
                    g = raster.getSample(x, y, 1);
                }
                int b = 0;
//                if (samples > 2) {
//                    b = raster.getSample(x, y, 2);
//                }
                int rgb = r + g * 256 + b * 256 * 256;
//                if (Math.random() > 0.9999) {
//                    p("Sample rgb data at " + x + "/" + y + ":" + rgb);
//                }
                data[x][y] = rgb;
            }
        }
        //     p("Read scoremask file " + getFile(flag));
        p("storing alldata["+flag.getCode()+"]");
        alldata[flag.getCode()] = data;
        done[flag.getCode()] = true;


        return data;
    }

    public double[] getDataPointsAt(int col, int row) {
        if (alldata == null) {
            p("getDataPointsAt> alldata is nullr, returning null ");
            return null;
        }
        if (col >= this.getNrCols() || row >= this.getNrRows()) {
            return null;
        }
        if (col < 0 || row < 0) {
            return null;
        }
        double[] res = new double[BfMaskFlag.getNrFlags()];
        for (BfMaskFlag flag : BfMaskFlag.values()) {
            int i = flag.getCode();
            res[i] = getDataPointAt(flag, col, row, false);
//            if (i < 5 && res[i] != null) {
//                p("Got "+i+":"+res[i]);
//            }
        }
        return res;
    }

    public int getDataPointAt(BfMaskFlag flag, int col, int row, boolean debug) {
        if (alldata == null) {
            if (debug) p("No alldata");
            return 0;
        }
        if (col >= this.getNrCols() || row >= this.getNrRows()) {
           if (debug) p("cols out of bounds");
            return 0;
        }
        if (col < 0 || row < 0) {
            if (debug) p("cols out of bounds");
            return 0;
        }
        int which = flag.getCode();
        if (which >= alldata.length) which = 0;

        if (col >= alldata[which].length) {
            if (debug) p("Trying to get alldata[" + col + "][" + row + "], but max c is " + alldata[which].length+", which="+which);
            // err("Tyring to get alldata[" + col + "][" + row + "], but max c is " + alldata[0].length);
            return 0;
        } else if (0 >= alldata[which][col].length) {
            if (debug) p("Tyring to get alldata[" + col + "][" + row + "], but max r is " + alldata[which][col].length+", which="+which);
            return 0;
        }
        
            return alldata[which][col][row];

    }

    private void err(String msg, Exception ex) {
        Logger.getLogger(BfHeatMap.class.getName()).log(Level.SEVERE, msg, ex);

    }

    private void err(String msg) {
        Logger.getLogger(BfHeatMap.class.getName()).log(Level.SEVERE, msg);
    }

    private void p(String msg) {
        System.out.println(BfHeatMap.class.getName() + ":" + msg);
        Logger.getLogger(BfHeatMap.class.getName()).log(Level.INFO, msg);
    }

    public int getNrRows() {
        return nRows;
    }

    public int getNrCols() {
        return nCols;
    }

    public ArrayList<WellCoordinate> getAllCoordsWithData(BfMaskFlag flag, int max) {
        return getAllCoordsWithData(flag, max, 0, 0, nCols, nRows);
    }

    public ArrayList<WellCoordinate> getAllCoordsWithData(BfMaskFlag flag, int max, int c1, int r1, int c2, int r2) {
        c1 = Math.max(0, c1);
        r1 = Math.max(0, r1);
        c2 = Math.min(c2, nCols);
        r2 = Math.min(r2, nRows);
        ArrayList<WellCoordinate> coords = new ArrayList<WellCoordinate>();
        if (alldata == null) {
            err("Got no data");
            return null;
        }
        int code = 0;
        if (flag == null) {
            code = flag.getCode();
        }
        if (code > alldata.length) {
            code = 0;
        }
        if (alldata[code] == null) {
            err("Got no data for flag " + flag);
            return null;
        }
        for (int c = c1; c < c2; c++) {
            for (int r = r1; r < r2; r++) {
                if (c >= alldata[code].length) {
                    err("Tyring to get alldata[" + c + "][" + r + "], but max c is " + alldata[code].length);
                    return coords;
                } else if (r >= alldata[code][c].length) {
                    err("Tyring to get alldata[" + c + "][" + r + "], but max r is " + alldata[code][c].length);
                    return coords;
                }
                if (alldata[code][c][r] > 0) {
                    WellCoordinate coord = new WellCoordinate(c, r);
                    coords.add(coord);
                    if (coords.size() > max) {
                        p("Found more coords thasn " + max + ", returning what I have now");
                        return coords;
                    }
                }
            }
        }

        return coords;
    }
}
