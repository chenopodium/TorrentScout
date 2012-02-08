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
public class HistoStatistics {

    /** a histogram such as pulse statistics which is a histogram 
     * of occurrences as a function of time
     */
    private double histo[][];
    private double stdevvalues[];
    private int nrBuckets;
    private double integral;
    private double area;
    private double intervalbetweenbuckets;
    protected double scalex;
    private double meanbucketvalue;
    private double averagebucketvalue;
    private double minx;
    private double maxx;
    private double var;
    private int maxybin;
    private boolean normalized;
    private int nrsamples;
    // also compute - in a LAZY fashion - the probability distribution of seing 2, 3 or more events in a certain timeframe!
    // a la poisson!
    private HistoStatistics[] poissonlike;
    private int MAX_POISSON = 1000;

    public double[] getStdevvalues() {
        return stdevvalues;
    }

    public void setStdevvalues(double[] stdevvalues) {
        this.stdevvalues = stdevvalues;
    }

    public HistoStatistics(double[][] histo) {
        this(histo, 1.0d, histo[1][0] - histo[0][0]);
    }

    public HistoStatistics(double[][] histo, double scale, double interval) {
        scalex = scale;
        setHisto(histo, interval, scalex);

    }

    public HistoStatistics() {
        // TODO Auto-generated constructor stub
    }

    public double getVar() {
        return var;
    }

    public double getMaxy() {
        double maxy = 0.0;

        for (int i = 0; i < this.nrBuckets; i++) {
            double yvalue = getCount(i);
            if (yvalue > maxy) {
                maxy = yvalue;
                maxybin = i;
            }
        }
        return maxy;
    }

    protected int checkBucket(int bucket) {
        bucket = Math.max(0, bucket);
        bucket = Math.min(this.nrBuckets - 1, bucket);
        return bucket;
    }

    private class Pair {

        double bin;
        double freq;

        public Pair(double bin, double freq) {
            this.bin = bin;
            this.freq = freq;
        }
    }

    public void resample(double interval, double start, double end) {
        if (this.intervalbetweenbuckets == interval && start == histo[0][0] && end == histo[histo.length - 1][0]) {
            //	pp("resampling not needed");
            return;
        }
        double[][] res = resample(histo, start, end, interval);
        this.setHisto(res, interval, 1.0);
    }

    public double[][] resample(double[][] datapoints, double start, double end, double interval) {
// 		{0			,	0},
//		{2.105263158,	0},
//		{4.210526316,	0},
//		{6.315789474,	5},
//		{8.421052632,	268},
//		{10.52631579,	170},
//		{12.63157895,	35},
//		{14.73684211,	16},
//		{16.84210526,	3}

        int nrpoints = (int) ((end - start) / interval + 1);
        double stats[][] = new double[nrpoints][2];

        for (int b = 0; b < nrpoints; b++) {
            stats[b][0] = start + b * interval;
        }
        // go through measurements
        for (int i = 1; i < datapoints.length; i++) {
            double x0 = datapoints[i - 1][0];
            double y0 = datapoints[i - 1][1];
            double x1 = datapoints[i][0];
            double y1 = datapoints[i][1];
            double slope = (y1 - y0) / (x1 - x0);
            // now compute values for any points BETWEEN x0 and x1
            // find closest x larger than x0, and closest x smaller than x1
            double a = findClosestLargerThan(x0, start, end, interval);

            double b = findClosestSmallerThan(x1, start, end, interval);
            if (a > b) {
                //warn("Interval: a > b:"+a+"b:"+b+", x0:"+x0+", x1:"+x1);
                // should add up values in between!
            } else if (a == b) {
                //p("Interval: a == b:"+a+"b:"+b+", x0:"+x0+", x1:"+x1);
            }
            //	p("a/b for "+x0+"/"+x1+":"+a+"/"+b);
            for (double newx = a; newx <= b; newx += interval) {
                //	p("Computing newx at "+a+" for x0/y0="+x0+"/"+y0+", slope:"+slope+", x1/y1="+x1+"/"+y1);
                double newy = y0 + (newx - x0) * slope;
                int bucket = this.computeBucket(stats, newx, interval);
                //	p("bucket for "+newx+" is "+bucket); 				
                stats[bucket][1] = newy;
            }
        }
        //p("resampling done");
        return stats;
    }

    protected int computeBucket(double[][] stats, double x, double interval) {
        for (int i = 0; i < stats.length; i++) {
            if (x < stats[i][0] + interval / 2) {
                return i;
            }
        }
        warn("x out side of range. returning largest value " + stats[stats.length - 1][0] + ", " + (stats.length - 1));
        return stats.length - 1;
    }
    // find closest value larger than x, that fits between start and end and hits one of the intervals

    private double findClosestLargerThan(double x, double start, double end, double interval) {
        if (x > end) {
            err("x " + x + " not in interval:" + start + "-" + end);
            return -1;
        }
        for (double newx = start; newx <= end; newx += interval) {
            if (newx >= x) {
                //	p("Closest value larger than "+x+" between "+start+"-"+end+" and interval "+interval+" is: "+newx);
                return newx;
            }
        }
        p("Could not find closest value larger than " + x + " between " + start + "-" + end);
        return -1;
    }

    private double findClosestSmallerThan(double x, double start, double end, double interval) {
        if (x < start) {
            err("x " + x + " not in interval:" + start + "-" + end);
            return -1;
        }
        for (double newx = end; newx >= start; newx -= interval) {
            if (newx <= x) {
                //	p("Closest value smaller than "+x+" between "+start+"-"+end+" and interval "+interval+" is: "+newx);
                return newx;
            }
        }
        err("Could not find closest value smaller than " + x + " between " + start + "-" + end);
        return -1;
    }

    public void setHisto(double[][] datapoints, double interval, double scale) {
        setHisto(datapoints, interval, scale, true);
    }

    public void setHisto(double[][] datapoints, double interval, double scale, boolean doResample) {

        //	pp("setHisto, "+datapoints.length+" datapoints, scale="+scale+" largest value:"+datapoints[datapoints.length-1][0]);
        if (scale > 1.0) {
            rescale(datapoints);
        }
        intervalbetweenbuckets = interval;
        double[][] oldstats = datapoints;
        boolean same = checkIfBucketsSame(datapoints, interval);
        if (!same && doResample) {
            oldstats = resample(datapoints, datapoints[0][0], datapoints[datapoints.length - 1][0], interval);
        }
        //	double[][] stats = makeLarger(oldstats);
        //	bucketdelta = stats[1][0] - stats[0][0];

        this.histo = oldstats;

        // compute the total
        integral = 0;
        nrsamples = 0;
        area = 0;
        maxx = -1;
        minx = -1;
        double average = 0;
        double mean = 0;
        nrBuckets = histo.length;
        for (int bx = 0; bx < nrBuckets; bx++) {
            double yvalue = histo[bx][1];
            double xvalue = histo[bx][0];
            if (yvalue > 0 && (minx < 0 || minx > xvalue)) {
                minx = xvalue;
            }
            if (yvalue > 0 && (maxx < 0 || maxx < xvalue)) {
                maxx = xvalue;
            }
            integral += yvalue;
            nrsamples += yvalue;
            average += yvalue * xvalue;
            area += yvalue * xvalue;
            if (yvalue > mean) {
                mean = yvalue;
                meanbucketvalue = xvalue;
            }
        }
        intervalbetweenbuckets = histo[1][0] - histo[0][0];

        average = (int) (average / integral);
        averagebucketvalue = average;


        //pp("Total: "+total+", average: "+averagebucketvalue+", mean: "+meanbucketvalue+", bucketdelta: "+bucketdelta);
        //	this.printStats();

    }

    private boolean checkIfBucketsSame(double[][] datapoints, double interval) {
        boolean same = true;
        for (int i = 1; i < datapoints.length; i++) {
            if (Math.abs(datapoints[i][0] - datapoints[i - 1][0] - interval) > 0.01) {
                same = false;
            }
        }
        return same;
    }

    public static double[][] makeLarger(double[][] oldstats, int factor) {

        double[][] stats = new double[factor * oldstats.length][2];

        double intervalbetweenbuckets = oldstats[1][0] - oldstats[0][0];

        for (int i = 0; i < stats.length; i++) {
            if (i < oldstats.length) {
                stats[i][0] = oldstats[i][0];
                stats[i][1] = oldstats[i][1];
            } else {
                stats[i][0] = stats[i - 1][0] + intervalbetweenbuckets;
                stats[i][1] = 0;
            }
        }
        return stats;
    }

    public double getCount(int bucket) {
       bucket = checkBucket( bucket);
        return histo[bucket][1];
    }

    public double getX(int bucket) {
        bucket = checkBucket( bucket);
        return histo[bucket][0];
    }

    public double getMidPoint(int bucket) {
        bucket = checkBucket( bucket);
        bucket = Math.min(this.nrBuckets - 1, bucket);
        return histo[bucket][0] - intervalbetweenbuckets / 2;
    }

    public double getSecondLargestY() {
        double maxy = this.getMaxy();
        double second = 0;

        for (int i = 0; i < this.nrBuckets; i++) {
            double yvalue = getCount(i);
            if (yvalue > second && yvalue != maxy) {
                second = yvalue;
            }
        }
        if (second == 0) {
            err("Got no second largest value! I will return the largest: " + maxy);
            second = maxy;
        }
        return second;
    }

    public double getBucketXValue(int bucket) {
        bucket = checkBucket( bucket);
        return histo[bucket][0];
    }

    private void rescale(double[][] datapoints) {
        if (scalex > 1) {
            //p("Rescaling factor "+scalex);
            for (int i = 0; i < datapoints.length; i++) {
                datapoints[i][0] = scalex * datapoints[i][0];
            }
            scalex = 1.0;
            double largest = datapoints[datapoints.length - 1][0];

            //p("largest value is now:"+largest);
            if (largest > 50000) {
                err("Trouble with rescaling, largest value " + largest + " too large");
            }
        }
    }

    public double computeProbForMultipleEventsWithin(int nrevents, double timedt) {
        if (nrevents == 1) {
            return computeCumulativeProbability(0, timedt);
        }
        if (nrevents > MAX_POISSON) {
            err("Can only compute computeProbForMultipleEventsWithin for at most " + MAX_POISSON + " events");
        } else if (nrevents < 1) {
            // for 0 events, compute prob that insert took longer than timedt?
            double max = histo[nrBuckets - 1][0];
            if (timedt + 1 >= max) {
                return 0;
            } else {
                return computeCumulativeProbability(timedt + 1, max);
            }
        }
        if (poissonlike == null) {
            poissonlike = new HistoStatistics[MAX_POISSON];
        }

        HistoStatistics poiss = getPoissonStatistics(nrevents);
        //p("Statistics for "+nrevents+" events");
        //	poiss.printStats();
        return poiss.computeCumulativeProbability(0, timedt);
    }

    public HistoStatistics getPoissonStatistics(int nrevents) {
        if (nrevents == 1) {
            return this;
        } else if (nrevents < 1) {
            err("Cannot compute stats for zero events");
        }
        if (nrevents >= MAX_POISSON) {
            err("Can only compute " + MAX_POISSON + " poisson cumulations");
        }
        if (poissonlike == null) {
            poissonlike = new HistoStatistics[MAX_POISSON];
        }
        HistoStatistics poiss = poissonlike[nrevents];
        if (poiss != null) {
            return poiss;
        }

        HistoStatistics prev = getPoissonStatistics(nrevents - 1);
        HistoStatistics one = this;
        double[][] newhisto = new double[nrBuckets][2];

        for (int i = 0; i < nrBuckets; i++) {
            double t1 = one.histo[i][0];

            for (int j = 0; j < nrBuckets; j++) {
                double t2 = prev.histo[j][0];
                double time = t1 + t2; // this is the target bucket
                int bucket = computeBucket(time);
                double val = one.histo[i][1] * prev.histo[j][1];
                newhisto[bucket][0] = one.histo[bucket][0];
                newhisto[bucket][1] += val;
                //	p("time "+t1+"+"+t2+"="+time+" -> bucket "+bucket+" val: "+val);
            }
        }
        // now compute all combinations of
        double[][] finalhisto = new double[nrBuckets][2];
        for (int i = 0; i < nrBuckets; i++) {
            finalhisto[i][0] = one.histo[i][0];
            finalhisto[i][1] = (double) Math.sqrt(newhisto[i][1]);
        }
        poiss = new HistoStatistics(finalhisto);
        poissonlike[nrevents] = poiss;
        return poiss;
    }

    public double getAverage() {
        return averagebucketvalue;
    }

    public double computeVariance() {
        //	p("Computing variance");
        double avg = getAverage();
        //	p("avg:"+avg+", mean:"+this.getMean());
        double sum = 0;
        int count = 0;
        //	p("Number of samples:"+this.getNrSamples()+", total:"+this.getTotal());
        for (int b = 0; b < this.getNrBuckets(); b++) {
            double val = this.getX(b);

            if (Double.isNaN(val)) {
                warn("Got a NAN value:" + val);
                val = 0;
            } else if (Double.isInfinite(val)) {
                err("Got a INF value:" + val);
            }

            int found = (int) this.getCount(b);
            count += found;
            //	p("Got "+val+", count for this value is:"+found);
            for (int i = 0; i < found; i++) {
                double diff = val - avg;
                sum += diff * diff;
            }
        }
        //	p("count:"+count+", var="+var);
        double var = sum / (count - 1);
        this.var = var;
        return var;
    }

    public double getIntegral() {
        return this.integral;
    }

    public double getXValueForMaxY() {
        return this.getX(maxybin);
    }

    public double getMean() {
        return meanbucketvalue;
    }

    public double getMinx() {
        return minx;
    }

    public double getMaxx() {
        return maxx;
    }
    /* compute a random value from 0 to total
     * then computes which "bucket" in the histogram it falls into, and returns this value
     */

    public double randomValue() {
        double value = (int) (Math.random() * integral);
        double bucketvalue = inverseFunction(value);
        return bucketvalue;
    }

    private double inverseFunction(double value) {
        int counter = 0;
        int cumulative = 0;
        while (cumulative < value && counter < nrBuckets) {
            cumulative += histo[counter][1];
            counter++;
        }
        if (cumulative < value) {
            // last one
            counter = nrBuckets - 1;
        } else if (counter > 0) {
            counter--;
        }
        double bucketvalue = histo[counter][0];

        // now vary within the bucketdelta
        int delta = (int) (Math.random() * intervalbetweenbuckets);
        bucketvalue += delta;
        //	p("value: "+value+" -> bucket:"+counter+", bucketvalue: "+bucketvalue);
        return bucketvalue;
    }
 public double computeCumulativeProbabilityForBuckets(int ba, int bb) {
     return computeCumulativeProbability(this.getBucketXValue(ba), this.getBucketXValue(bb));
 }
    
    /** compute the cumulative prob from a to 
     * 
     */
    public double computeCumulativeProbability(double a, double b) {
        if (a > b) {
            err("computeCumulativeProbability (a=" + a + ", b=" + b + ", b should be > a");
        }
        int ba = computeBucket(a);
        int bb = computeBucket(b);
        double cum = 0.0d;

        if (bb < ba || b < a) {
            err("computeCumulativeProbability (a=" + a + ", b=" + b + ": ba<bb || b < a: bb=" + bb + ", ba=" + ba);
            return 0;
        }

        if (ba == bb) {
            //	p("Same bucket!");
            double d = b - a;
            cum = histo[ba][1] * d / intervalbetweenbuckets / integral;
            //p("Cum prob from "+a+"-"+b+":"+cum);
            return cum;
        }
        //	p("a->ba: "+ba+", b->bb: "+bb);
        if (bb > nrBuckets) {
            warn("bb> nrsamples: " + bb);
        }
        //bb=nrsamples;
        // |               [0]                |
        // [0]-d/2                            [0]+d/2
        // |                   x              |
        // | dx1=x-[0]+d/2      dx2=[0]+d/2-x |
        // | a1 = a*dx1/d       a2=a*dx2/2 
        // da: right side
        double da = histo[ba][0] + intervalbetweenbuckets / 2 - a;
        if (da > intervalbetweenbuckets || da < 0) {
            this.printStats();
            p("computeCumulativeProbability " + a + "-" + b);
            p("ba:" + ba + ", bb:" + bb + ", bucketdelta:" + intervalbetweenbuckets);
            p("histo[ba][0]=" + histo[ba][0]);
            err("Bucketdelta < da: " + da);
        }
        // db: LEFT side
        double db = b - histo[bb][0] + intervalbetweenbuckets / 2;
        if (db < 0) {
            err("db<0: " + intervalbetweenbuckets + "< " + db + ", a=" + a + ", b=" + b);
        }
        if (db > intervalbetweenbuckets) {
            //err("Bucketdelta < db: "+bucketdelta+"< "+db+", a="+a+", b="+b);
            db = intervalbetweenbuckets;
        }
        cum = histo[ba][1] * da / intervalbetweenbuckets;
        cum += histo[bb][1] * db / intervalbetweenbuckets;
        for (int i = ba + 1; i < bb; i++) {
            cum += histo[i][1];
        }
        //	p("cum:"+cum+", total:"+total);
        cum = cum / this.integral;
        //p("bucketdelta: "+bucketdelta);
//		p("a: "+a+", ba:"+ba+", ba[0]="+histo[ba][0]+" da: "+da+", b: "+b+", db: "+db);
//		p("P( near a ["+ba+"])="+histo[ba][1]*da/bucketdelta+", total area is: "+histo[ba][1]);
//		p("P( near b ["+bb+"])="+histo[bb][1]*db/bucketdelta+", total area is: "+histo[bb][1]);
//
//		p("Cum prob from "+a+"-"+b+":"+cum);

        return cum;
    }

    /** based on the histogram, computes the probability that the observed bucket is seen.
     * This is basically the value in this bucket/total * 100
     */
    public double computeProbability(double bucketvalue) {
        int buck = computeBucket(bucketvalue);
        double prob = (double) ((double) histo[buck][1] / (double) integral);
        //	p("Prob for "+bucketvalue+" (=buck "+buck+") ="+prob);
        return prob;
    }

    /** given a bucket value, computes which bucket this belongs to */
    public int computeBucket(double bucketvalue) {
        for (int i = 0; i < nrBuckets; i++) {
            if (bucketvalue < histo[i][0] + intervalbetweenbuckets) {
                return i;
            }
        }
        //	warn("bucketvalue out side of range. returning largest value "+nrsamples);
        return nrBuckets - 1;
    }

    public double getBucketFraction(double dx) {
        return dx / intervalbetweenbuckets;
    }

    /** given a bucket value, computes which bucket this belongs to */
    public double computeFloatingBucket(double bucketvalue) {
        for (int i = 0; i < nrBuckets; i++) {
            if (bucketvalue < histo[i][0] + intervalbetweenbuckets) {
                double bucket = i;
                double delta = bucketvalue - histo[i][0];
                // p(bucketvalue+"-> bucket "+bucket+" bucketx="+histo[i][0]+", interval="+intervalbetweenbuckets+", delta="+delta+", delta/interv="+delta/intervalbetweenbuckets);
                return bucket + delta / intervalbetweenbuckets;
            }
        }
        //	warn("bucketvalue out side of range. returning largest value "+nrsamples);
        return nrBuckets - 1;
    }

    public void showStats() {
        double factor = 2.0d / (double) integral * nrBuckets * 5;
        System.out.println("nrsamples:" + nrBuckets + ", factor:" + factor);
        for (int i = 0; i < nrBuckets; i++) {
            System.out.println(histo[i][0] + ": " + histo[i][1] + " " + stars((int) (histo[i][1] * factor)));
        }
    }

    public double getArea() {
        return area;
    }

    public double getNrSamples() {
        return nrsamples;
    }

    public double getNrBuckets() {
        return nrBuckets;
    }

    public void printStats() {
        System.out.println("Avg: " + this.getAverage() + ", Mean:" + this.getMean() + ", " + minx + "-" + maxx + ", total:" + this.getArea() + ", nr samples: " + this.getNrSamples());
        System.out.println(toCsv());
    }

    public String toString() {
        printStats();
        return toCsv();
    }

    public String toCsv() {
        String s = "x, observations, cumulative, percent";
        return toCsv(s, true);
    }

    public void normalize() {
        if (normalized || Math.abs(integral - 1.0) < 0.001) {
            p("alreay normalized, not doing it again");
            normalized = true;
            return;
        }
        integral = 0;
        for (int i = 0; i < nrBuckets; i++) {
            integral += histo[i][1];
        }
        p("Normalizing all values by dividing with total " + integral);
        for (int i = 0; i < nrBuckets; i++) {
            double val = histo[i][1];
            val = val / this.integral;
            histo[i][1] = val;
        }
        this.integral = 1.0;
        normalized = true;
    }

    public String toCsv(String header, boolean cumulative) {
        String s = header + "\n";
        //this.normalize();
        //	if (!normalized) normalize();

        double cum = 0;
        for (int i = 0; i < nrBuckets; i++) {
            double val = histo[i][1];
            if (cumulative) {
                cum = cum + val;
                s += histo[i][0] + ", " + val + ", " + cum + ", " + (1.0 - cum) + " \n";
            } else {
                s += histo[i][0] + ", " + val + "\n";
            }
        }
        return s;
    }

    private String stars(int len) {
        len = Math.min(100, len);
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < len; i++) {
            s = s.append("*");
        }
        return s.toString();
    }

    public static void main(String[] args) {
    }

    // add the values of two stats, use same bucket
    public HistoStatistics add(HistoStatistics second) {
        HistoStatistics first = this;
        double start = Math.min(histo[0][0], second.histo[0][0]);
        double end = Math.max(histo[histo.length - 1][0], second.histo[second.histo.length - 1][0]);
        double inter = Math.min(this.intervalbetweenbuckets, second.intervalbetweenbuckets);

        first.resample(inter, start, end);
        second.resample(inter, start, end);
        // now both have same interval, same start and same end
        int nrpoints = (int) ((end - start) / inter + 1);
        double[][] sum = new double[nrpoints][2];

        for (int i = 0; i < first.histo.length; i++) {
            sum[i][0] = first.histo[i][0];
            if (first.histo[i][0] != second.histo[i][0]) {
                first.showStats();
                second.showStats();
                err("resampling failed, should be the same x");
            }
            sum[i][1] = first.histo[i][1] + second.histo[i][1];
        }
        HistoStatistics res = new HistoStatistics(sum);
        return res;
    }

    public double getBucketDelta() {
        return intervalbetweenbuckets;
    }

    public double getInterval() {
        return intervalbetweenbuckets;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(HistoStatistics.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(HistoStatistics.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(HistoStatistics.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("HistoStatistics: " + msg);
        //Logger.getLogger( HistoStatistics.class.getName()).log(Level.INFO, msg, ex);
    }
}
