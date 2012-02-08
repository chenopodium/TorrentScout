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
public class CombineFunction extends AbstractHistoFunction implements PlotFunction {

    PlotFunction fa;
    PlotFunction fb;
    /*
     * pHscale * (1-exp(-a*ta))*exp(-b*tb),  
     * where ta = max(min(t-t0,tw),0)  
     * tb = max(t-tw,0), 
     * t0 = start of ph time, 
     * tw= start of wash time, is pretty close as an approximation:  
     * tuning parameters = t0, 
     * tw (timing for region), 
     * a, b = time scale for change, 
     * phScale = (extrapolated) total size of step.
     */

    public CombineFunction(ExplorerContext cont, PlotFunction fa, PlotFunction fb) {
        super(cont, fa.getName() + "+" + fb.getName(), "Combines the " + fa.getName() + " and " + fb.getName() + " functions.<br>The histogram result is the RMS of the difference."
                + "<br>The parameters are taken from the individual functions");
        this.fa = fa;
        this.fb = fb;

    }

    @Override
    public void setParams(Parameter[] par) {
        super.setParams(par);

    }

    @Override
    public EvalType[] getPossibleTypes() {
        return fa.getPossibleTypes();
    }

    @Override
    public void setEvalType(EvalType t) {
        fa.setEvalType(t);
        fb.setEvalType(t);
    }

    @Override
    public EvalType getEvalType() {
        return fa.getEvalType();
    }

    @Override
    public String toLongString() {
        String res = super.toLongString();
        res += "<br>Examples for various t (ms):<br>";
        for (int t = 0; t < 3000; t += 500) {
            res += "&nbsp;&nbsp;&nbsp;f(" + t + ") = " + compute(t) + "<br>";
        }
        return res;
    }

    @Override
    protected double compute(float[] ts, int start, int end, int cleft, int cright) {
        double res = 0;

        //show = (Math.random()>0.999);
        String s = null;
//        if (show) {
//            s = "\nCombine function result for x=" + x + ", y = " + y;
//            s += "\nfa:" + ((AbstractHistoFunction) fa).toFullString() + "\nfb: " + ((AbstractHistoFunction) fb).toFullString();
//            s += "\nstart=" + start + ", end=" + end
//                    + "\nf, t, ts[f], va, vb, sum, diff\n";
//        }
        if (this.getEvalType() == null || this.getEvalType() == EvalType.RMS) {
            for (int f = Math.max(1, start); f < end; f++) {
                double expect = compute(data.getTimeStamp(0, f));
                double val = ts[f];
                double diff = expect - val;
                res += diff * diff;
            }

            res = Math.sqrt(res);
        } else {
            for (int f = Math.max(1, start); f < end; f++) {
                double expect = compute(data.getTimeStamp(0, f));
                double val = ts[f];
                double diff = val - expect;
                res += diff;
            }
            res = res / ((double) Math.max(1, data.getDT(start, end)));
        }
        return res;
    }

    @Override
    public double compute(double t) {
        if (t <= 0) {
            return 0;
        }
        if (fa == null || fb == null) {
            p("Got no function fa or fb");
            return 0;
        }
        double va = fa.compute(t);
        double vb = fb.compute(t);
        return va + vb;
    }
}
