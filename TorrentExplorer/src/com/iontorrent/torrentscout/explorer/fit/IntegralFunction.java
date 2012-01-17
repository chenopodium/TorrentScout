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

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class IntegralFunction extends AbstractHistoFunction{
    
    public IntegralFunction(ExplorerContext cont) {
        super(cont, "Integral", "Compute integral between specified frames for each well");
    }
   
    @Override
    protected double compute(float[] ts, int start, int end, int cleft, int cright) {
        double res = 0;
        
        for (int f = Math.max(1,start); f < end; f++) {
              int dt = data.getDT(f-1, f);
            res += ts[f]*dt;
//            if (Math.random()>0.9999) {
//                p(" dt="+dt);
//                p("ts[f] "+ts[f]+", tot="+res);
//            }
        }
       int dt = data.getDT(start, end-1);
        res =  res / dt;
                
//        if (Math.random()>0.999) {
//                p("final integral="+res);
//         }
        return res;
    }
    
}
