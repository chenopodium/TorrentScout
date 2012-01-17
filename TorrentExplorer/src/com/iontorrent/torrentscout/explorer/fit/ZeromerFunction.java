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
public class ZeromerFunction extends AbstractHistoFunction implements PlotFunction {

    double tauEmpty = 10;
    double tauBulk = 4;
    double scalex = 1.0;
    double scaley = 1.0;

    double zeromerdata[];
    
    public ZeromerFunction(ExplorerContext cont) {
        super(cont, "Zeromer", "Zeromer bulk (no inc), where t0 starts at the left green frame.<br>The result is the RMS of the difference."
                + "<br>for (int f = start ; f < end ; f++) {<br>             &nbsp;&nbsp;&nbsp;&nbsp;	 int dt = (int) (tt[f] - tt[f-1]);  <br> &nbsp;&nbsp;&nbsp;&nbsp;	 int dempty = (emptydata[f]-emptydata[0]);<br> &nbsp;&nbsp;&nbsp;&nbsp;	zeromerdata[f] = (dempty * (<b>tauEmpty</b> + dt) + cdelta[f-1]) / (<b>tauBulk</b> + dt);             <br> &nbsp;&nbsp;&nbsp;&nbsp;	idelta[f] =(dempty - zeromerdata[f])*dt;<br> &nbsp;&nbsp;&nbsp;&nbsp;	cdelta[f] = cdelta[f-1] + idelta[f];<br>  }");
        params = new Parameter[4];
        params[0] = new Parameter("tauEmpty", tauEmpty, "tau empty");
        params[2].setRange(1, 10, 1.0);
        params[1] = new Parameter("tauBulk", tauBulk, "tau bulk");
        params[2].setRange(1, 10, 1.0);
        params[2] = new Parameter("x-scale", scalex, "scaling factor for x (making peak smaller or larger)");
        params[2].setRange(0.25, 10.0, 0.25);
        params[3] = new Parameter("y-scale", scaley, "scaling factor for y (making peak wider or smaller)");
        params[3].setRange(0.25, 10.0, 0.25);

    }

    @Override
    public void setParams(Parameter[] par) {
        super.setParams(par);
        tauEmpty = par[0].getDoubleValue();
        tauBulk = par[1].getDoubleValue();
        scalex = par[2].getDoubleValue();
        scaley = par[3].getDoubleValue();
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
    protected double compute(float[] ts, int start, int end, int cleft, int cright) {
        double res = 0;
        
        zeromerdata = precompute(ts, start, cright);
        
        for (int f = Math.max(1, start); f < end; f++) {
            double expect = zeromerdata[f];
            double val = ts[f];
            double diff = expect - val;
            res += diff * diff;
//            if (Math.random()>0.9999) {
//                p(" dt="+dt);
//                p("ts[f] "+ts[f]+", tot="+res);
//            }
        }
        res = Math.sqrt(res);
        return res;
    }

    public double[] precompute(float emptyts[], int frameStart, int frameEnd) {
        int len = emptyts.length;
        zeromerdata = new double[len];
        
        int start = Math.max(1, frameStart);
        int end = Math.min(frameEnd, len);
        int nrframes = end - start;
        double[] idelta = new double[nrframes + start];
        double[] cdelta = new double[nrframes + start];

        System.arraycopy(emptyts, 0, zeromerdata, 0, len);
        
        for (int f = start; f < end; f++) {
            //    deltat<-tt[j]-tt[j-1]
            double dt = (double)data.getDT(f-1, f);
            double dempty = (emptyts[f] - emptyts[0]);
            //    sB[j]<-(sE[j]*(tauEmpty+deltat) + cdelta[j-1])/(tauBulk+deltat)
            zeromerdata[f] = (dempty * (tauEmpty + dt) + cdelta[f - 1]) / (tauBulk + dt);
            //    idelta[j]<-sE[j]-sB[j]
            idelta[f] = (dempty - zeromerdata[f]) * dt;
            //    cdelta[j]<-cdelta[j-1]+idelta[j]
            cdelta[f] = cdelta[f - 1] + idelta[f];
        }
        return zeromerdata;
    }

    @Override
    public double compute(double t) {
        if (t <= 0) {
            return 0;
        }
        t = t / scalex;
        double expect = scaley * (Math.exp(-t * tauBulk) - Math.exp(-t * tauEmpty)) / (tauEmpty - tauBulk);
        return expect;
    }
}
