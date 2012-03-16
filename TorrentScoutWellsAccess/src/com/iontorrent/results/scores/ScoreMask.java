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
package com.iontorrent.results.scores;

import com.iontorrent.rawdataaccess.wells.ScoreMaskFlag;
import com.iontorrent.utils.io.FileUtils;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**

 * @author Chantal Roth
 */
public class ScoreMask {

    /** the associated fileOrUrl */
    //  private String imageFileOrUrl;
    /** the input stream */
    //  private DataInputStream in;
    /** the header information - see subclass @Header below for description */
    /** data stored in [x][y] format - see subclass @Data below for descriptions */
    private double[][][] alldata;
    int nRows; // uint32
    int nCols; //uint32
    //ScoreMaskFlag flag;
    ExperimentContext expcontext;
    WellContext wellcontext;
    String resdir;
    String cachedir;
    private boolean done[];
    private static HashMap<String, ScoreMask> map;

    public static ScoreMask getMask(ExperimentContext expcontext, WellContext wellcontext) {
        if (expcontext == null || wellcontext == null) {
            return null;
        }
        if (wellcontext.getMask() == null) {
            return null;
        }
        if (map == null) {
            map = new HashMap<String, ScoreMask>();
        }
        ScoreMask mask = map.get(expcontext.getResultsDirectory());
        if (mask == null) {
            mask = new ScoreMask(expcontext, wellcontext);
            if (map.size() > 0) map.clear();
            map.put(expcontext.getResultsDirectory(), mask);
        }
        return mask;

    }

    public WellContext getWellContext() {
        return wellcontext;
    }

    private ScoreMask(ExperimentContext expcontext, WellContext wellcontext) {

        this.expcontext = expcontext;
        this.wellcontext = wellcontext;
        // this.imageFileOrUrl = wellcontext.getResultsDirectory() + expcontext.getResultsName() + "_" + flag.getImageName();
        if (wellcontext != null) {
            this.nRows = wellcontext.getNrRows();
            this.nCols = wellcontext.getNrCols();
        }

    }

    public String getFile(ScoreMaskFlag flag) {
        return getImageFile(flag);
    }

    public String getImageFile(ScoreMaskFlag flag) {
        File f = findFileSomewhere("heatmap_" + flag.getImageName());
        if (f != null) {
            return f.toString();
        } else {
            return null;
        }
    }

//    public boolean hasAllWellImages() {
//        for (ScoreMaskFlag f : ScoreMaskFlag.WELLS_FLAGS) {
//            if (!hasImage(f)) {
//                return false;
//            }
//        }
//        return true;
//    }

    public boolean hasAllSffImages() {
        for (ScoreMaskFlag f : ScoreMaskFlag.SFF_FLAGS) {
            if (!hasImage(f)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasAllBamImages() {
        for (ScoreMaskFlag f : ScoreMaskFlag.SAM_FLAGS) {
            if (!hasImage(f)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasImage(ScoreMaskFlag flag) {
        File f = findFileSomewhere("heatmap_" + flag.getImageName());
        if (f == null || !f.exists()) {
            return false;
        }
        if (f.exists()) {
            long datetime = f.lastModified();
            Date d = new Date(datetime);
            String date = "2011/09/05";
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                Date lastDate = formatter.parse(date);
                if (d.before(lastDate)) {
                    p("Deleting old heat map: " + f);
                    f.delete();
                }
            } catch (ParseException e) {
                err("Could not parse " + date);
            }
        }
        return f.exists();
    }

    private File findFileSomewhere(String filename) {

        String results_path = wellcontext.getResultsDirectory();
        String cache_dir = wellcontext.getCacheDirectory();
        String plugin_dir = this.expcontext.getPluginDir();

        File f = new File(results_path + filename);
        if (f.exists()) {
            return f;
        }

        f = new File(plugin_dir + filename);
        if (f.exists()) {
            return f;
        }

        f = new File(cache_dir + filename);
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

    public double[][] createEmptyData() {
        double[][] data = new double[nCols][nRows];
        return data;
    }

    public double[][][] readAllData() {

        int nrflags = ScoreMaskFlag.getNrFlags();
        if (alldata != null) {
            //  p("Already read all data");
        } else {
            alldata = new double[nrflags][nCols][nRows];
        }
        p("ScoreMask: reading ALL data for ALL flags (if available)");

        for (ScoreMaskFlag flag : ScoreMaskFlag.values()) {
            alldata[flag.getCode()] = readData(flag);
        }
        return alldata;
    }

    public double getMin(ScoreMaskFlag flag){
        double[][] data = readData(flag);
        double min = Double.MAX_VALUE;
         for (int i = 0; i < data.length; i++) {
             for (int j = 0; j < data[0].length; j++) {
                 double val = data[i][j];
                 if (val < min) min = val;
             }                
        }
         return min/flag.multiplier();
    }
     public double getMax(ScoreMaskFlag flag){
        double[][] data = readData(flag);
        double max = Double.MIN_VALUE;
         for (int i = 0; i < data.length; i++) {
             for (int j = 0; j < data[0].length; j++) {
                 double val = data[i][j];
                 if (val > max) max = val;
             }                
        }
         return max/flag.multiplier();
    }
    public double[][] readData(ScoreMaskFlag flag) {
        int nrflags = ScoreMaskFlag.getNrFlags();

        if (done == null) {
            done = new boolean[nrflags];
        }


        if (alldata == null) {
            alldata = new double[nrflags][nCols][nRows];
        }
        if (done[flag.getCode()]) {
            p("Already read data for flag "+flag);
            return alldata[flag.getCode()];
        }

        double[][] data = new double[nCols][nRows];
        if (!hasImage(flag)) {
            p("File " + getFile(flag) + " does not exist, returning empty data.");
            alldata[flag.getCode()] = data;
            return data;
        }
        else p("Reading file "+getFile(flag));
        DataInputStream in = FileUtils.openFileOrUrl(getFile(flag));

        // READ IMAGE AS A FUNCTION OF THE FLAG
        BufferedImage image = null;
        try {
            image = ImageIO.read(in);
        } catch (IOException ex) {
            Logger.getLogger(ScoreMask.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (image == null) {
            err("Could not read image...");
            alldata[flag.getCode()] = data;
            return data;
        }

        p("read image "+getFile(flag)+", extracting data from it");
        Raster raster = image.getData();
        for (int x = 0; x < nCols; x++) {
            for (int y = 0; y < nRows; y++) {


                int r = raster.getSample(x, y, 0);
                int g = raster.getSample(x, y, 1);
                int b = raster.getSample(x, y, 2);
                int rgb = r + g * 256 + b * 256 * 256;

                double p = (double) rgb;//flag.getRealValue(rgb);               
                //double p = flag.getRealValue(rgb);
                data[x][y] = p;
            }
        }
         p("Read scoremask file " + getFile(flag));
        alldata[flag.getCode()] = data;
        if (!flag.isCustom()) {
            done[flag.getCode()] = true;
        }
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
        double[] res = new double[ScoreMaskFlag.getNrFlags()];
        for (ScoreMaskFlag flag : ScoreMaskFlag.values()) {
            int i = flag.getCode();
            res[i] = getDataPointAt(flag, col, row);
//            if (i < 5 && res[i] != null) {
//                p("Got "+i+":"+res[i]);
//            }
        }
        return res;
    }

    public double getDataPointAt(ScoreMaskFlag flag, int col, int row) {
        if (alldata == null) {
            return 0;
        }
        if (col >= this.getNrCols() || row >= this.getNrRows()) {
            return 0;
        }
        if (col < 0 || row < 0) {
            return 0;
        }

        return alldata[flag.getCode()][col][row];
    }

    private void err(String msg, Exception ex) {
        Logger.getLogger(ScoreMask.class.getName()).log(Level.SEVERE, msg, ex);
        
    }

    private void err(String msg) {
        Logger.getLogger(ScoreMask.class.getName()).log(Level.SEVERE, msg);
    }

    private void p(String msg) {
    //    System.out.println(ScoreMask.class.getName() + ":" + msg);
   //     Logger.getLogger(ScoreMask.class.getName()).log(Level.INFO, msg);
    }
//
//    public ScoreMaskDataPoint[][] getData() {
//        return alldata;
//    }

    public String toString() {
        return "ScoreMask: " + this.hashCode() + ":" + this.resdir;
    }

    public int getNrRows() {
        return nRows;
    }

    public int getNrCols() {
        return nCols;
    }

    public ArrayList<WellCoordinate> getAllCoordsWithData(ScoreMaskFlag flag, int max) {
        return getAllCoordsWithData(flag, max, 0, 0, nCols, nRows);
    }

    public ArrayList<WellCoordinate> getAllCoordsWithData(ScoreMaskFlag flag, int max, int c1, int r1, int c2, int r2) {
        c1 = Math.max(0, c1);
        r1 = Math.max(0, r1);
        c2 = Math.min(c2, nCols);
        r2 = Math.min(r2, nRows);
        ArrayList<WellCoordinate> coords = new ArrayList<WellCoordinate>();
        if (alldata == null) {
            err("Got no data");
            return null;
        }
        int code = flag.getCode();
        if (alldata[code] == null) {
            err("Got no data for flag " + flag);
            return null;
        }
        for (int c = c1; c < c2; c++) {
            for (int r = r1; r < r2; r++) {
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
