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
package com.iontorrent.utils.stats;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class TwoDStats {

    private static final int COUNT = 0;
    private static final int VALUE = 1;
    private static final int NORMALIZED = 2;
    private String name;
    private double maxx;
    private double maxy;
    private double minx;
    private double miny;
    private double rangex;
    private double rangey;
    private int nrxbuckets;
    private int nrybuckets;
    private double bucketdx;
    private double bucketdy;
    private double stepdx;
    private double stepdy;
    private int total;
    private double data[][][];

    public TwoDStats(double max) {
        this(0, 0, max, max, (int) max, (int) max);
    }

    public TwoDStats(double maxx, double maxy) {
        this(0, 0, maxx, maxy, (int) maxx, (int) maxy);
    }

    public TwoDStats(double minx, double miny, double maxx, double maxy) {
        this(minx, miny, maxx, maxy, (int) (maxx - minx), (int) (maxy - miny));
    }

   

    public TwoDStats(double minx, double miny, double maxx, double maxy, int bucketdx, int bucketdy) {
        rangex = maxx - minx;
        rangey = maxy - miny;
        this.minx = minx;
        this.miny = miny;
        this.maxx = maxx;
        this.maxy = maxy;
        this.bucketdx = bucketdx;
        this.bucketdy = bucketdy;
        this.nrxbuckets = (int) (rangex / (double) bucketdx);
        this.nrybuckets = (int) (rangey / (double) bucketdy);

        this.stepdx = bucketdx;
        this.stepdy = bucketdy;
//		bucketdx = dx/(double)bucketsx;
//		bucketdy = dy/(double)bucketsy;
        p("buckets: " + nrxbuckets + "/" + nrybuckets + ", bucketdx:" + bucketdx + ", bucketdy: " + bucketdy + ", rangex:" + rangex + ", rangey:" + rangey);
        data = new double[3][nrxbuckets + 1][nrybuckets + 1];
    }

    public double[][] computeMeanAndStdDev() {
        // now compute average for each read length
        double[][] data = this.getValues();
        if (data[0].length < 1) {
            err("computeMeanAndStdDev Data has wrong dimension:" + data[0].length);
        }
        double[][] expected = new double[data.length][2];
        for (int len = 0; len < data.length; len++) {
            double tot = 0.0;
            double samples = 0;
            for (int ident = 0; ident < data[len].length; ident++) {
                int samplesforthiscoord = (int) data[len][ident];
                samples += samplesforthiscoord;
                double val = (samplesforthiscoord * (ident));
                tot += val;
            }
            double averageident = tot / samples;
            double sumofdiffsquared = 0.0;
            for (int ident = 0; ident < data[len].length; ident++) {
                int samplesforthiscoord = (int) data[len][ident];
                if (samplesforthiscoord > 0) {
                    double diff = Math.abs(averageident - ident);
                    sumofdiffsquared += diff * diff * samplesforthiscoord;
                }
            }
            double var = sumofdiffsquared / samples;
            expected[len][0] = averageident;
            double std = Math.sqrt(var);
            expected[len][1] = 2 * std;
        }
        return expected;
    }

    public double[] computeStdDev() {
        // now compute average for each read length
        double[][] data = this.getValues();
        double[] stdev = new double[data.length];
        for (int len = 0; len < data.length; len++) {
            double tot = 0.0;
            double samples = 0;
            for (int ident = 0; ident < data[len].length; ident++) {
                int samplesforthiscoord = (int) data[len][ident];
                samples += samplesforthiscoord;
                double val = (samplesforthiscoord * (ident));
                tot += val;
            }
            double averageident = tot / samples;
            double sumofdiffsquared = 0.0;
            for (int ident = 0; ident < data[len].length; ident++) {
                int samplesforthiscoord = (int) data[len][ident];
                if (samplesforthiscoord > 0) {
                    double diff = Math.abs(averageident - ident);
                    sumofdiffsquared += diff * diff * samplesforthiscoord;
                }
            }
            double var = sumofdiffsquared / samples;
            double std = Math.sqrt(var);
            stdev[len] = std;
        }
        return stdev;
    }

    public void add(double value, double x, double y) {
        if (x > maxx || y > maxy) {
            warn("x or y out of range:" + x + "/" + y + "vs " + maxx + "/" + maxy);
            return;
        }
        double dxinrange = x - minx;
        double dyinrange = y - miny;
        dxinrange = Math.max(minx, dxinrange);
        dyinrange = Math.max(miny, dyinrange);
        dxinrange = Math.min(maxx, dxinrange);
        dyinrange = Math.min(maxy, dyinrange);
        //	p("dx:"+dx+", dy:"+dy);
        int bx = (int) Math.round((dxinrange - bucketdx / 2) / bucketdx);
        int by = (int) Math.round((dyinrange - bucketdy / 2) / bucketdy);
        if (bx > 0 && by > 0) {
            data[COUNT][bx][by]++;
            data[VALUE][bx][by] += value;
            total++;
        }
    }

    public XYStats getAveragesPerX() {
        XYStats avg = new XYStats((int) stepdx, (int) maxx);
        for (int bx = 0; bx < nrxbuckets; bx++) {
            double x = minx + bx * stepdx;
            double sum = 0;
            double count = 0;
            for (int by = 0; by < nrybuckets; by++) {
                double nr = data[VALUE][bx][by];
                double y = miny + by * stepdy;
                sum += nr * y;
                count += nr;
            }
            double avgy = 0;
            if (count > 0) {
                avgy = sum / count;
            }
            avg.addValue(x, avgy);
        }
        return avg;
    }

    public String toCsv() {
        String res = "X-values from " + minx + "-" + maxx + " and step size " + stepdx + "\n";
        res += "Y-values from " + miny + "-" + maxy + " and step size " + stepdy + "\n";
        res += "x, ";
        for (int by = 0; by < nrybuckets; by++) {
            res += miny + by * stepdy;
            if (by + 1 < nrybuckets) {
                res += ", ";
            }
        }
        res += "\n";
        for (int bx = 0; bx < nrxbuckets; bx++) {
            for (int by = 0; by < nrybuckets; by++) {
                double val = data[VALUE][bx][by];
                res += val;
                if (by == 0) {
                    res += minx + bx * stepdx + ", ";
                }
                if (by + 1 < nrybuckets) {
                    res += ", ";
                }
            }
            res += "\n";
        }
        res += "\n";
        return res;
    }

    public double[][] getValues() {
        return data[VALUE];
    }

    public double[][] getNormalizedValues() {
        normalize();
        return data[NORMALIZED];
    }

    public double[][] getCounts() {
        return data[COUNT];
    }

    public int getTotal() {
        return total;
    }

    public void normalize() {
        if (total == 0 || Math.abs(total - 1.0) < 0.001) {
            return;
        }
        for (int bx = 0; bx < nrxbuckets; bx++) {
            for (int by = 0; by < nrybuckets; by++) {
                double val = data[VALUE][bx][by];
                double norm = val / (double) total;
                data[NORMALIZED][bx][by] = norm;
            }
        }
        total = (int) 1.0;
    }

   
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMaxx() {
        return maxx;
    }

    public double getMaxy() {
        return maxy;
    }

    public double getMinx() {
        return minx;
    }

    public double getMiny() {
        return miny;
    }

    public double getDx() {
        return rangex;
    }

    public double getDy() {
        return rangey;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(TwoDStats.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(TwoDStats.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(TwoDStats.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("TwoDStats: " + msg);
        //Logger.getLogger( TwoDStats.class.getName()).log(Level.INFO, msg, ex);
    }
}
