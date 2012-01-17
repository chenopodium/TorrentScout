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

import com.iontorrent.rawdataaccess.wells.GeneralWellDensity;
import com.iontorrent.rawdataaccess.wells.ScoreMaskFlag;
import com.iontorrent.results.scores.ScoreMask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Given a certain bucket size, such as 10x10 pixels, computes the number of empty, duds, beads, live etc 
 * for all properties in the @ScoreMaskFlag class. This data structure can be used for instance 
 * in visual display.
 * @author Chantal Roth
 */
public class ScoreWellDensity extends GeneralWellDensity {

    private ScoreMask scoremask;
    
    //  public static int DEFAULT_BUCKET_SIZE = 5;    
    private int well_density[][][];
    private int maxvalues[];
    private int minvalues[];
    private boolean[] done;
    private ScoreMaskFlag curflag;

    public ScoreWellDensity(ScoreMask scoremask, int bucket_size, ScoreMaskFlag flag) {
        super(bucket_size);
        this.scoremask = scoremask;
       
        this.curflag = flag;
        initDensityPlot();
        computeDensityForFlag(flag);
    }

    public void initDensityPlot() {
        int nr_col_buckets = scoremask.getNrCols() / bucket_size + 1;
        int nr_row_buckets = scoremask.getNrRows() / bucket_size + 1;
        int nrflags = ScoreMaskFlag.getNrFlags();
        //p("bucket: " + bucket_size + ", nr cols: " + nr_col_buckets + ", nrwells: " + bucket_size * nr_col_buckets);
        well_density = new int[nr_col_buckets][nr_row_buckets][nrflags];
        maxvalues = new int[nrflags];
        minvalues = new int[nrflags];
        done = new boolean[nrflags];


    }

    @Override
    public void setFlag(Object smflag) {
        this.curflag = (ScoreMaskFlag) smflag;
        computeDensityPlot();
    }

    @Override
    public Object getFlag() {
        return curflag;
    }

    private String computeDensityForFlag(ScoreMaskFlag smflag) {

        int code = smflag.getCode();
        if (done[code]) {
            return null;
        }
        // for (int flag = 0; flag < ScoreMaskFlag.getNrFlags(); flag++) {
        int max = 0;
        int min = Integer.MAX_VALUE;
        ScoreMaskFlag scoreflag = ScoreMaskFlag.get(code);
        //    p("Computing density for flag " + scoreflag.toString());
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

    public int getMin() {
        return minvalues[curflag.getCode()];
    }

    @Override
    public int getCount(int c, int r) {
        return getCount(c, r, curflag);
    }

    public int getCount(int c, int r, ScoreMaskFlag flag) {
        return well_density[c][r][flag.getCode()];
    }

    private double computeDensity(int col, int row, ScoreMaskFlag scoreflag) {
        if (scoreflag == null) {
            return -1;
        }
        double count = 0;
        for (int c = col * bucket_size; c < Math.min((col + 1) * bucket_size, scoremask.getNrCols()); c++) {
            for (int r = row * bucket_size; r < Math.min((row + 1) * bucket_size, scoremask.getNrRows()); r++) {

                count += scoremask.getDataPointAt(scoreflag, c, r);
            }
        }
        return count;
    }

    private void err(String msg, Exception ex) {
        Logger.getLogger(ScoreWellDensity.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void p(String msg) {
        System.out.println("ScoreWellDensity: " + msg);
        //Logger.getLogger( ScoreWellDensity.class.getName()).log(Level.INFO, msg, ex);
    }

    public void update(ScoreMaskFlag scoreMaskFlag) {
        computeDensityForFlag(scoreMaskFlag);
    }

    public ScoreMask getMask() {
        return this.scoremask;
    }

    @Override
    public String computeDensityPlot() {
        return computeDensityForFlag(curflag);
    }
}
