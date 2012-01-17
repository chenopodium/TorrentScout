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
package com.iontorrent.torrentscout.explorer.fit;

import com.iontorrent.rawdataaccess.wells.BitMask;
import com.iontorrent.torrentscout.explorer.ExplorerContext;
import com.iontorrent.utils.stats.StatPoint;
import com.iontorrent.utils.system.Parameter;
import com.iontorrent.wellmodel.RasterData;
import com.iontorrent.wellmodel.WellCoordinate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public abstract class AbstractHistoFunction implements HistoFunction {

  

     public enum EvalType {

       RMS("RMS", "root mean square of function compared to data"),
       DIFF("difference", "data minus function value");
       //DIFF("default", "value for time series");
       private String name;
        private String desc;
       
        EvalType(String name, String desc) {
            this.name = name;          
            this.desc = desc;
        }

        public String toString() {
            return getName();
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }
       
    }
    //  protected HistoStatistics histo;
    protected String name;
    protected String desc;
    protected boolean show;
    protected Parameter[] params;
    
    
    protected StatPoint statpoint;
    protected double[][] result;
    protected RasterData data;
    protected int x;
    protected int y;
    
    protected ExplorerContext cont;
    private int count;
    private double minx = Double.MIN_VALUE;
    private double maxx = Double.MAX_VALUE;
    private EvalType evalType;

    public AbstractHistoFunction(ExplorerContext cont, String name, String desc) {
        this.name = name;
        this.desc = desc;
        this.cont = cont;
        
    }

    protected abstract double compute(float[] timeseries, int framestart, int frameend, int cropleft, int cropright);

    public int getCount() {
        return count;
    }
    @Override
     public void setMinx(double min){
         this.minx = min;
     }
    @Override
    public void setMaxx(double max){
        this.maxx = max;
    }
    
    public EvalType getEvalType() {
        return evalType;
    }
    public void setEvalType(EvalType type) {
        this.evalType = type;
    }
    public EvalType[] getPossibleTypes() {
        return null;
    }

    @Override
    public boolean execute() {
        if (cont.getData() == null) {
            return false;
        }
        statpoint = new StatPoint(name, false);
        data = cont.getData();
        int size = data.getRaster_size();
        BitMask histomask = cont.getHistoMask();
        //BitMask ignoremask = cont.getIgnoreMask();
        // need start and endframe
        int start = cont.getStartframe();
        int end = cont.getEndframe();
        int cleft = cont.getCropleft();
        int cright = cont.getCropright();
        if (cleft > cright) {
            int tmp = cleft;
            cleft = cright;
            cright = tmp;
        }
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        result = new double[size][size];

        p("Computing " + (size * size) + " data points: green=" + start + "-" + end + ", cutoff=" + cleft + "-" + cright + ", function " + getClass().getName()+", minx="+minx+"maxx="+maxx);
        p("Eval type: "+this.getEvalType());
        p("Histo mask: "+histomask);
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        count = 0;
        for (int c = 0; c < size; c++) {
            for (int r = 0; r < size; r++) {
                x = c;
                y = r;
                if (ok(null, histomask, c, r)) {
                   // if (x == cont.getMaincoord().getX() && y == cont.getMaincoord().getY()) show = true;
                   // else show = false;
                    float[] ts = data.getTimeSeries(c, r, 0);
                   // if (!pinned(ts)) {
                        count++;
                        double tot = compute(ts, start, end, cleft, cright);

                        // remember  value
                        result[c][r] = tot;
                        if (tot > max) {
                            max = tot;
                        }
                        if (tot < min) {
                            min = tot;
                        }
                        if (tot >= minx && tot <= maxx) {
                            statpoint.add(tot);
                        }
                   // }
                }
                // now compute
            }
        }
        p("Computed " + count + " values, min value was: " + min + ", nax value was: " + max);
        statpoint.computeStats();

        return false;
    }
//
//    private boolean pinned(int[] ts) {
//        for (int i = 0; i < ts.length; i++) {
//            if (ts[i] > Settings.PIN_MAX || ts[i] < Settings.PIN_MIN) {
//               // p("Got pinned: @" + x + "/" + y );//+ ":" + Arrays.toString(ts));
//                return true;
//            }
//        }
//        return false;
//    }

    public boolean ok(BitMask ignoremask, BitMask take, int c, int r) {
        if (take == null && ignoremask == null) {
            return true;
        }
        else if (take == null) {
            return !ignoremask.get(c, r);
        }
        else if (ignoremask == null) {
            return take.get(c, r);
        }

        boolean b = (!ignoremask.get(c, r) && take.get(c, r));
//        if (Math.random() > 0.9999) {
//            p(c + "/" + r + ", ignmore=" + ignoremask.get(c, r) + ", take=" + take.get(c, r) + ", result: " + b);
//        }
        return b;
    }

    @Override
    public BitMask createMask(BitMask take, double left, double right) {
        int size = cont.getData().getRaster_size();
        if (left > right) {
            double tmp = left;
            left = right;
            right = tmp;
        }
        WellCoordinate relcoord = new WellCoordinate(cont.getData().getRelStartCol(), cont.getData().getRelStartRow());
        BitMask mask = new BitMask(relcoord, size, size);
        mask.setName(name);
        p("Got left/right bucket values: " + left + "-" + right);
        for (int c = 0; c < size; c++) {
            for (int r = 0; r < size; r++) {
                // remember bucket value
                /// only from currently selected take mask!
                if (take == null || take.get(c, r)) {
                    double val = result[c][r];
                    if (c % 25 == 0 && r % 25 == 0) {
                        p("got histo function value at " + c + "/" + r + "=" + val);
                        // mask.getDataPointAt(c, r);
                    }
                    if (val >= left && val <= right) {
                        mask.set(c, r, true);
                        // p("setting mask to true at "+c+"/"+r+":"+mask.get(r, c));
                    } else {
                        mask.set(c, r, false);
                    }
                }

            }
        }

        return mask;

    }
//    @Override
//    public HistoStatistics getHisto() {
//        return histo;
//    }

    public StatPoint getDataPoints() {
        return statpoint;
    }

    @Override
    public String toString() {
        return name;
    }
 
    
    public String toLongString() {        
        return toFullString()   ;
    }
    public String getHtmlDesc() {
        String s = "<html><b>"+getName() + "</b><br>" + getDescription() + "<br><br>"+toLongString()+"</html>";
        s = s.replace("\n","<br>");
        return s;
    }
     public String toFullString() {
        String s = "";//getName() + "\n" + getDescription() + "\n";
        if (params != null && params.length>0) {            
            for (Parameter p : this.getParams()) {
                s += p.toString() + "\n";
            }
        }
        return s;
    }
    @Override
    public double[][] getResult() {
        return result;
    }
 public int getNrParams() {
        return getParams().length;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return desc;
    }

    /**
     * @return the params
     */
    public Parameter[] getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(Parameter[] params) {
        this.params = params;
    }
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    protected void p(String msg) {
        //System.out.println("IntegralFunction: " + msg);
        Logger.getLogger(getClass().getName()).log(Level.INFO, msg);
    }

    void setContext(ExplorerContext maincont) {
        this.cont = maincont;
    }
}
