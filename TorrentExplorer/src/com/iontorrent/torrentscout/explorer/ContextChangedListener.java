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
package com.iontorrent.torrentscout.explorer;

import com.iontorrent.guiutils.widgets.Widget;
import com.iontorrent.expmodel.ExpContextChangedListener;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.rawdataaccess.wells.BitMask;
import com.iontorrent.wellmodel.RasterData;
import com.iontorrent.wellmodel.WellCoordinate;

/**
 *
 * @author Chantal Roth
 */
public interface ContextChangedListener extends ExpContextChangedListener {

    @Override
    public void flowChanged(int flow);

    @Override
    public void frameChanged(int frame);

    public void maskChanged(BitMask mask);
    public void maskSelected(BitMask mask);

    public void maskAdded(BitMask mask);
    
    public void maskRemoved(BitMask mask);
    
    @Override
    public void coordChanged(WellCoordinate coord);
    
    
    public void dataAreaCoordChanged(WellCoordinate coord);
    
    public void masksChanged();
    public void widgetChanged(Widget w);
    
    @Override
    public void fileTypeChanged(RawType t);
    
    public void dataChanged(RasterData data, int startrow, int startcol, int startframe, int endrow, int endcol, int endframe);
}
