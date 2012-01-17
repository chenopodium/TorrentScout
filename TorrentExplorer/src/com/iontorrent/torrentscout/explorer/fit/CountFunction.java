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
import java.util.Arrays;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class CountFunction extends AbstractHistoFunction{
    
    public CountFunction(ExplorerContext cont) {
        super(cont, "count", "just takes the value at the left green cursor");
    }
   
    @Override
    protected double compute(float[] ts, int start, int end, int cleft, int cright) {
//        if (Math.random()>0.999) {
//                p("count["+start+"]="+ts[start]);
//         }
//        
        return ts[start];
    }
    
}
