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
public class XYStats {

    private double[][] histo;
    private double resolution;
    private int nrbuckets;

    private double minxvalue;
    private double interval;
     public XYStats(double resolution, double max) {
         this(resolution, 0, max);
     }
    public XYStats(double resolution,double min,  double max) {
        this.resolution = Math.max(0.0000001, resolution);
        this.minxvalue = min;
        this.interval = max-min;
        this.nrbuckets = (int) ((interval / resolution) + 1.0); //
        if (nrbuckets < 10) {
            nrbuckets = 10;
            resolution = interval/nrbuckets;
        }
        if (nrbuckets > 10000) {
            nrbuckets = 10000;
            resolution = interval/nrbuckets;
        }

        p("Got xystats with buckets:"+nrbuckets+" from "+min+"-"+max+", resolution="+resolution);
        histo = new double[nrbuckets][2];
        for (int b = 0; b < nrbuckets; b++) {
            histo[b][0] = (b) * resolution+minxvalue;
        }
    }

    public static XYStats createStats(StatPoint stats, double interval, int min, int max) {
        XYStats ident = new XYStats(interval, min, max);
        for (Double val : stats.getValues()) {
            ident.addCount(val);
            //	p("adding value "+val);
        }
        //	p("Added "+stats.getValues().size()+" values");
        return ident;
    }
     public static XYStats createStats(int[] values, double interval, int min, int max) {
        XYStats ident = new XYStats(interval, min, max);
        for (int i: values) {
            ident.addCount(i);            
        }        
        return ident;
    }
      public static XYStats createStats(long[] values, double interval, long min, long max) {
        XYStats ident = new XYStats(interval, min, max);
        for (long i: values) {
            ident.addCount(i);            
        }        
        return ident;
    }
 public static XYStats createStats(double[] values, double interval, double min, double max) {
        XYStats ident = new XYStats(interval, min, max);
        for (double i: values) {
            ident.addCount(i);            
        }        
        return ident;
    }
 public static XYStats createStats(float[] values, double interval, double min, double max) {
        XYStats ident = new XYStats(interval, min, max);
        for (double i: values) {
            ident.addCount(i);            
        }        
        return ident;
    }
    public String toString() {
        return "XYStats,  nrbuckets=" + nrbuckets + ", resolution: " + resolution;
    }

    public String toFullString() {
        String s = "";
        for (int b = 0; b < histo.length; b++) {
            s += "b=" + b + ", x=" + (histo[b][0]) + ", y=" + histo[b][1] + "\n";
        }
        return s;
    }

    public HistoStatistics createStatistics() {
        if (histo == null) {
            return null;
        } else {
            return new HistoStatistics(histo);
        }
    }

    public void addCount(double x) {
        int buck = Math.max(0, (int) ((x-minxvalue) / resolution));
        if (buck >= nrbuckets) {
            //	p("bucket too large, ignoring: "+x);
        } else {
            histo[buck][1]++;
          //  if (Math.random()>0.99)	p("Adding "+1+" at x="+x+", bucket "+buck+", "+histo[buck][1]+" for total "+histo[buck][1]);
        }

    }

    public void addValue(double x, double y) {
        int buck = (int) ((x+minxvalue) / resolution);
        if (buck >= nrbuckets || buck < 0) {
            p("x bucket too large or out of range, ignoring: " + x);
        } else {
            histo[buck][1] += y;
            //p("Setting x="+x+", y="+y+", buckx="+buck);
        }
    }

    public void normalizeByDx(double dx) {
        for (int b = 0; b < nrbuckets; b++) {
            histo[b][1] = histo[b][1] / dx;
        }
    }

    public void setValue(double x, double y) {
        int buck = (int) ((x-minxvalue) / resolution);
        if (buck >= nrbuckets || buck < 0) {
            p("x bucket too large or out of range, ignoring: " + x);
        } else {
            histo[buck][1] = y;
            //	p("Setting x="+x+", y="+y+", buckx="+buck);
        }
    }

    public static void main(String[] args) {
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(XYStats.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(XYStats.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(XYStats.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("XYStats: " + msg);
        //Logger.getLogger( XYStats.class.getName()).log(Level.INFO, msg, ex);
    }
}
