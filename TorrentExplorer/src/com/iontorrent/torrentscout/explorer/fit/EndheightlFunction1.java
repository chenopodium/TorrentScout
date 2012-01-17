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
public class EndheightlFunction1 extends AbstractHistoFunction{
    
    public EndheightlFunction1(ExplorerContext cont) {
        super(cont, "integral-end height", "integral between green cursors (normalized), minus value at right red end");
    }
   
    @Override
    protected double compute(float[] ts, int start, int end, int cleft, int cright) {
        
        double res = 0;
       
        for (int f = Math.max(1,start); f < end; f++) {
            int dt = data.getDT(f-1, f);
            res += ts[f]*dt;          
        }
        int dt = data.getDT(start, end-1);
        res =  res / dt;
        
        dt = data.getDT(cright-1, cright);
        res = res - ts[cright]*dt;
        
//        if (Math.random()>0.999) {
//                p("end height: res="+res+", ts["+cright+"]="+ts[cright]);
//         }
        return res;
    }
    
}
