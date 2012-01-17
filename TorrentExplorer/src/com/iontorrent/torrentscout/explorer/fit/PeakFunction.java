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

import com.iontorrent.torrentscout.explorer.ExplorerContext;
import com.iontorrent.torrentscout.explorer.process.PlotFunction;
import com.iontorrent.utils.system.Parameter;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class PeakFunction extends AbstractHistoFunction implements PlotFunction{
    
    double m = 0.005;
    double k = 0.009;
    double scalex = 1.0;
    double scaley = 1.0;
    public PeakFunction(ExplorerContext cont) {
        super(cont, "Peak function", "Compute how well data fits the peak function<br>[exp(-kt) - exp(-mt)]/(m-k),<br>where t0 starts at the left <b>green</b> frame.<br>The result in the histogram view is the RMS of the difference");
        params = new Parameter[4];
        params[0] = new Parameter("m", m, "m in [exp(-kt) - exp(-mt)]/(m-k)");
        params[0].setRange(0.0005, 0.01, 0.0005);
        params[1] = new Parameter("k", k, "k in [exp(-kt) - exp(-mt)]/(m-k)");
        params[1].setRange(0.0005, 0.01, 0.0005);
        params[2] = new Parameter("x-scale", scalex, "scaling factor for x (making peak smaller or larger)");
        params[2].setRange(0.1, 5.0, 0.1);
        params[3] = new Parameter("y-scale", scaley, "scaling factor for y (making peak wider or smaller)");
        params[3].setRange(0.1, 10.0, 0.1);
     
    }  

    @Override
    public void setParams(Parameter[] par) {
        super.setParams(par);
      //  p("setParams called");
        m = par[0].getDoubleValue();
        k = par[1].getDoubleValue();
        scalex = par[2].getDoubleValue();
        scaley = par[3].getDoubleValue();
    }
    @Override
    public String toLongString() {
        String res = super.toLongString();
        res += "<br>Examples for various t (ms):<br>";
        for (int t = 0; t < 2000; t += 500) {
            res += "&nbsp;&nbsp;&nbsp;f("+t+") = " +compute(t)+"<br>";
        }
        return res;
    }
    @Override
    public EvalType[] getPossibleTypes() {
        return new EvalType[]{EvalType.RMS, EvalType.DIFF};
    }
    @Override
    protected double compute(float[] ts, int start, int end, int cleft, int cright) {
        double res = 0;
        if (m == k) return 0;
        if (this.getEvalType() == null || this.getEvalType() == EvalType.RMS) {
            for (int f = Math.max(1,start); f < end; f++) {
                double t = data.getDT(start, f);
                double expect = compute(t)  ;
                double val = ts[f]  ;
                double diff = expect-val;
                res += diff*diff;
    //            if (Math.random()>0.9999) {
    //                p(" dt="+dt);
    //                p("ts[f] "+ts[f]+", tot="+res);
    //            }
            }
            res = Math.sqrt(res);
        }
        else {
            // difference
             for (int f = Math.max(1,start); f < end; f++) {
                double t = data.getDT(start, f);
                double expect = compute(t)  ;
                double val = ts[f]  ;
                double diff = val-expect;
                res += diff;
            }
             res = res / ((double)Math.max(1, data.getDT(start, end)));
        }
        return res;
    }

    @Override
    public double compute(double t) {
        if (cont.getData() == null) return 0;
        double t0 = cont.getData().getTimeStamp(0, cont.getStartframe());
        t = t - t0;
        if (t<=0 || m == k) return 0;
        t = t / scalex;
        double expect = scaley*(Math.exp(-t*k) -Math.exp(-t*m))/(m-k)  ;
        return expect;
    }
    
}
