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
package com.iontorrent.torrentscout.explorer.edit;

import com.iontorrent.rawdataaccess.wells.BitMask;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class AndOp extends AbstractOperation{
    public AndOp() {
        super("And (intersect)", "results in m1 & m2 (an intersection)");
    }

    @Override
    public boolean execute(BitMask m1, BitMask m2, BitMask m3) {
        return m3.intersect(m1, m2);
    }   
    
}
