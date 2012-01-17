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

import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.rawdataaccess.wells.BitMask;
import com.iontorrent.wellmodel.RasterData;
import com.iontorrent.wellmodel.WellCoordinate;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class ContextChangeAdapter implements ContextChangedListener {

    private static void p(String msg) {
        //System.out.println("ContextChangeAdapter: " + msg);
        Logger.getLogger(ContextChangeAdapter.class.getName()).log(Level.INFO, msg);
    }

    @Override
    public void flowChanged(int flow) {
       // p("flow chnged: " + flow);
    }
   

    @Override
    public void frameChanged(int frame) {
       // p("frame chnged: " + frame);
    }

    @Override
    public void maskChanged(BitMask mask) {
   //     p("mask chnged: " + mask);
    }

    
    @Override
    public void maskAdded(BitMask mask) {
   //     p("mask added: " + mask);
    }
    
    @Override
    public void masksChanged(){
   //     p("All masks changed");
    }

    @Override
    public void maskRemoved(BitMask mask) {
    //    p("mask removed: " + mask);
    }

    @Override
    public void maskSelected(BitMask mask) {
  //      p("mask selected: " + mask);
    }

    @Override
    public void coordChanged(WellCoordinate coord) {
 //       p("Main coord changed: " + coord);
    }

    @Override
    public void dataAreaCoordChanged(WellCoordinate coord) {
    //    p("dataAreaCoordChanged changed: " + coord);
    }
    @Override
    public void widgetChanged(Widget w) {
   //     p("widget chnged: " + w);
    }

    @Override
    public void fileTypeChanged(RawType t) {
  //      p("RawType chnged: " + t);
    }

    @Override
    public void dataChanged(RasterData data, int startrow, int startcol, int startflow, int endrow, int endcol, int endflow) {
   //     p("RasterData chnged: " + startrow + "/" + startcol + "/" + startflow);
    }

    @Override
    public void flowChanged(ArrayList<Integer> flows) {
  //      p("Flows changed: "+flows);
        int flow = flows.get(0);
        flowChanged(flow);
    }
}
