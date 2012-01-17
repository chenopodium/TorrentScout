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
package com.iontorrent.expmodel;

import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.wellmodel.WellCoordinate;

/**
 *
 * @author Chantal Roth
 */
public interface ExpContextChangedListener extends FrameListener, 
        FlowListener, FiletypeListener {

    public void flowChanged(int flow);

    @Override
    public void frameChanged(int frame);
   
    public void coordChanged(WellCoordinate coord);   
    
    @Override
    public void fileTypeChanged(RawType t);
}
