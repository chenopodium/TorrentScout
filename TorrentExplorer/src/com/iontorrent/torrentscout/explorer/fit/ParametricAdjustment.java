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
public class ParametricAdjustment extends AbstractHistoFunction implements PlotFunction {

    double a = 0.0005;
    double b = 0.0017;
    double tw = 2700.0d;
    double phscale = -70.0;
    double zeromerdata[];

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
    public ParametricAdjustment(ExplorerContext cont) {
        super(cont, "Parametric Adjustment", "Function similar to Zeromer bulk for parametric adjustment,<br>where t0 starts at the left <b>red</b> frame"
                + "<br>The histogram result is the RMS of the difference."
                + "<br>phscale*(1-exp(-a*ta))*exp(-b*tb)"
                + "<br>ta = max(min(t-t0,tw),0); tb = max(t-tw,0)"
                + "<br>a, b = time scale for change"
                + "<br>phscale= (extrapolated) total size of step"
                + "<br><b>Use negative values for phscale to invert the function</b>");
        params = new Parameter[4];
        params[0] = new Parameter("a", a, "a (time scale for change)");
        params[0].setRange(0.0001, 0.005, 0.00005);

        params[1] = new Parameter("b", b, "b (time scale for change)");
        params[1].setRange(0.0001, 0.005, 0.00005);

        params[2] = new Parameter("tw", tw / 1000.0, "wash step time (s)");
        params[2].setRange(0, 10.0, 0.05);

        params[3] = new Parameter("phscale", phscale, "(extrapolated) total size of step (making the bulk larger/smaller)");
        params[3].setRange(-500, 500.0, 1);

    }

    @Override
    public void setParams(Parameter[] par) {
        super.setParams(par);
        a = par[0].getDoubleValue();
        b = par[1].getDoubleValue();
        tw = par[2].getDoubleValue() * 1000.0;
        phscale = par[3].getDoubleValue();
        zeromerdata = null;
    }

    @Override
    public String toLongString() {
        String res = super.toLongString();
        res += "<br>Examples for various t (ms):<br>";
        for (int t = 0; t < 2000; t += 500) {
            res += "&nbsp;&nbsp;&nbsp;f(" + t + ") = " + compute(t) + "<br>";
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

        
        if (this.getEvalType() == null || this.getEvalType() == EvalType.RMS) {
            for (int f = Math.max(1, start); f < end; f++) {
                double expect = compute(data.getTimeStamp(0, f));
                double val = ts[f];
                double diff = expect - val;
                res += diff * diff;
            }
        } else {
            // difference
            for (int f = Math.max(1, start); f < end; f++) {
                double t = data.getDT(start, f);
                double expect = compute(t);
                double val = ts[f];
                double diff = val - expect;
                res += diff;
            }
            res = res / ((double) Math.max(1, data.getDT(start, end)));
        }
        res = Math.sqrt(res);
        return res;
    }

    @Override
    public double compute(double t) {
        if (t <= 0) {
            return 0;
        }
        if (cont.getData() == null) {
            p("Got no data");
            return 0;
        }
        double t0 = cont.getData().getTimeStamp(0, cont.getCropleft());
        // double tw = cont.getData().getTimeStamp(0, cont.getCropright());
        double ta = Math.max(Math.min(t - t0, tw), 0);
        double tb = Math.max(t - tw, 0);
        double val = phscale * (1 - Math.exp(-a * ta)) * Math.exp(-b * tb);
        return val;
    }
}
