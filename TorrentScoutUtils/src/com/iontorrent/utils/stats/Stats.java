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

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class Stats {

    private double min;
    private double max;
    private double mean= Double.NaN;
    private double var= Double.NaN;
    private String name;
    private int nrvalues;

    public Stats(String name) {
        this.name = name;        
    }

    public double getMin(ArrayList<Double> values) {
        setMin(Double.MAX_VALUE);
        for (Double d : values) {
            if (getMin() > d) {
                setMin((double) d);
            }
        }

        return getMin();
    }

    public double getMax(ArrayList<Double> values) {
        setMax(Double.MIN_VALUE);
        for (Double d : values) {
            if (getMax() < d) {
                setMax((double) d);
            }
        }
        return getMax();
    }

//    public void computeStats(ArrayList<Double> values) {
//   
//        computeStats((double[])values.toArray());
//    }
    public void computeStats(double[] values) {
   
        double sum = 0;
        
        setMin(Double.MAX_VALUE);
        setMax(Double.MIN_VALUE);

        int count = values.length;
        int use = 0;
        for (int i = 0; i < count; i++) {
            double val = values[i];

            if (Double.isNaN(val)) {
                warn("Got a NAN value:" + val + ", ignoring it");
                val = 0;

            } else if (Double.isInfinite(val)) {
                warn("Got a INF value:" + val + ", ignoring it");
                val = 0;

            } else {
                use++;
            }
            if (val < getMin()) {
                setMin(val);
            }
            if (val > getMax()) {
                setMax(val);
            }
            sum += val;
        }
        if (use <= 0) {
            warn("Could not compute mean, got only " + use + " usable values");
            setMean(0);
            setVar(Double.MAX_VALUE);
        } else {
            setMean(sum / use);
            setVar(computeVariance(values, getMean(), use));
        }
        this.nrvalues = count;


    }

    public double computeVariance(double[] values, double mean, int use) {
        if (values.length < 1) {
            return 1.0;
        }
        double sum = 0;
        int count = values.length;
        for (int i = 0; i < count; i++) {
            double val = values[i];
            if (Double.isNaN(val)) {
                err("Got a NAN value:" + val);
                val = 0;
            } else if (Double.isInfinite(val)) {
                err("Got a INF value:" + val);
                return -1;
            }

            double diff = val - mean;
            sum += diff * diff;
        }

        if (use - 1 <= 0) {
            warn("Could not compute variance, got only " + use + " usable values");
            setVar(Double.MAX_VALUE);
        } else {
            setVar(sum / (use - 1));
        }
        //var = Math.min(100, var);
        return getVar();
    }

    public String toString() {
        String res = getName() + ":";

        res += getMean() + "+-" + getVar() + ", " + getMin() + "-" + getMax() + ", " + getNrvalues() + " values";


        return res;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(Stats.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(Stats.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(Stats.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("Stats: " + msg);
        //Logger.getLogger( Stats.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the min
     */
    public double getMin() {
        return min;
    }

    /**
     * @param min the min to set
     */
    public void setMin(double min) {
        this.min = min;
    }

    /**
     * @return the max
     */
    public double getMax() {
        return max;
    }

    /**
     * @param max the max to set
     */
    public void setMax(double max) {
        this.max = max;
    }

    /**
     * @return the mean
     */
    public double getMean() {
        return mean;
    }

    /**
     * @param mean the mean to set
     */
    public void setMean(double mean) {
        this.mean = mean;
    }

    /**
     * @return the var
     */
    public double getVar() {
        return var;
    }

    /**
     * @param var the var to set
     */
    public void setVar(double var) {
        this.var = var;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the nrvalues
     */
    public int getNrvalues() {
        return nrvalues;
    }

    /**
     * @param nrvalues the nrvalues to set
     */
    public void setNrvalues(int nrvalues) {
        this.nrvalues = nrvalues;
    }
}
