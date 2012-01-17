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
import com.iontorrent.utils.stats.StatPoint;

/**
 *
 * @author Chantal Roth
 */
public interface HistoFunction {
    
    public String getName();
    public String getDesc();
    public double[][] getResult();
    public BitMask createMask(BitMask take, double left, double right);
    public boolean execute();
    public void setMinx(double min);
    public void setMaxx(double max);
   // public HistoStatistics getHisto();
    public StatPoint getDataPoints();
}
