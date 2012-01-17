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
public class RSMErrorFunction extends AbstractHistoFunction{
    
    public RSMErrorFunction(ExplorerContext cont) {
        super(cont, "RSM", "Root of (sum of squared)");
    }
   
    @Override
    protected double compute(float[] ts, int start, int end, int cleft, int cright) {
        double tot = 0;
        
        int count = 0;
        for (int f = start; f < end; f++) {
            tot += ((double)(ts[f]*ts[f]));
            count++;
        }
        tot = Math.sqrt(tot)/(double)count;
        return tot;
    }
    
}
