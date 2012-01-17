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
public class StatPoint {

    double mean;
    double var;
    double min;
    double max;
    String name;
    boolean isPercentage;
    boolean isCount;
    private Object moreInfo;
    ArrayList<Double> values;

    public StatPoint(String name, boolean isPercentage) {
        this.name = name;
        this.isPercentage = isPercentage;
        this.isCount = false;
        values = new ArrayList<Double>();
    }

    public ArrayList<Double> getValues() {
        return values;
    }

    public double getMax() {
        max = Double.MIN_VALUE;
        for (Double d : values) {
            if (max < d) {
                max = d;
            }
        }
        return max;
    }

    public double getMin() {
        min = Double.MAX_VALUE;
        for (Double d : values) {
            if (min > d) {
                min = d;
            }
        }
        return min;
    }

    public void setCount(boolean b) {
        this.isCount = b;
    }

    public int size() {
        return values.size();
    }

    public void add(double val) {
        values.add(val);
    }

    @Override
    public String toString() {
        String res = name + ":";
        if (isCount) {
            res += size();
        } else {
            res += mean + "+-" + var + ", " + min + "-" + max + ", " + size() + " values";
            if (getMoreInfo() != null) {
                res += getMoreInfo().toString();
            }
        }
        return res;
    }

    public String toCsv() {
        if (isCount) {
            return name + ", " + size();
        } else {
            return name + ", " + mean + ", " + var + ", " + min + ", " + max;
        }
    }

    public void computeStats() {
        double sum = 0;
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;

        int count = values.size();
        for (int i = 0; i < count; i++) {
            double val = values.get(i);
            if (Double.isNaN(val)) {
                //warn("Got a NAN value:"+val);
                val = 0;
            } else if (Double.isInfinite(val)) {
                //warn("Got a INF value:"+val);
                val = 100;
            }

            if (isPercentage && val < 0) {
                warn("val < 0:" + val);
                val = 0;
            } else if (isPercentage && val > 100) {
                warn("val > 100:" + val);
                val = 100;
            }
            if (val < min) {
                min = val;
            }
            if (val > max) {
                max = val;
            }
            sum += val;
        }

        mean = sum / (values.size());

        var = computeVariance(values, mean);
    }
  

    public double computeVariance(ArrayList<Double> values, double mean) {
        if (values.size() < 1) {
            return 1.0;
        }
        double sum = 0;
        int count = values.size();
        for (int i = 0; i < count; i++) {
            double val = values.get(i);
            if (Double.isNaN(val)) {
                err("Got a NAN value:" + val);
                val = 0;
            } else if (Double.isInfinite(val)) {
                err("Got a INF value:" + val);
                return -1;
            }
            if (val < 0 && isPercentage) {
                err("val < 0:" + val);
                val = 0;
            } else if (val > 100 && isPercentage) {
                err("val > 100:" + val);
                val = 100;
            }
            double diff = val - mean;
            sum += diff * diff;
        }

        double var = sum / (count - 1);
        //var = Math.min(100, var);
        return var;
    }

    public void setMoreInfo(Object align) {
        this.moreInfo = align;
    }

    public Object getMoreInfo() {
        return moreInfo;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(StatPoint.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(StatPoint.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(StatPoint.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("StatPoint: " + msg);
        //Logger.getLogger( StatPoint.class.getName()).log(Level.INFO, msg, ex);
    }
}
