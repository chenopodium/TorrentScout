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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public abstract class AbstractOperation implements MaskOperation {

    protected String name;
   protected  String desc;

    protected int nrargs;
    
    public AbstractOperation(String name, String desc) {
        this.name = name;
        this.desc = desc;
        nrargs = 2;
    }

    public int getNrArgs() {
        return nrargs;
    }
    public String toString() {
        return name;
    }

   
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}
