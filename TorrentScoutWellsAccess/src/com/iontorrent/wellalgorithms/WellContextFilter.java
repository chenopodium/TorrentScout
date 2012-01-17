/*
*	Copyright (C) 2011 Life Technologies Inc.
*
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 2 of the License, or
*   (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.wellalgorithms;

import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.rawdataaccess.wells.BfMaskFlag;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class WellContextFilter {

    private WellContext context;
    private BfMaskFlag[] musthaveflags;
    private BfMaskFlag[] nothaveflags;
    private RawType rawtype;
    private int flow;
    private WellCoordinate coord;

    public WellContextFilter (WellContext context, 
            BfMaskFlag[] musthaveflags,BfMaskFlag[]  nothaveflags, 
            RawType rawtype, int flow, WellCoordinate coord) {
        this.context = context;
        this.musthaveflags = musthaveflags;
        this.rawtype = rawtype;
        this.flow = flow;
        this.coord = coord;
    }

    public String getKey() {
        String key= flow+":"+rawtype.name()+":r="+context.getResultsDirectory()+":s="+context.getSelection()+":#="+context.getNrWells();
        if (context.getSelection()!=null && context.getSelection().getFilters()!=null) {
           key += ":f="+ context.getSelection().getFilters();
        }
        if (musthaveflags != null) {
            key += ":+="+ Arrays.toString(musthaveflags);
        }
        if (nothaveflags != null) {
            key += ":-="+ Arrays.toString(nothaveflags);
        }
        return key;
    
    }
    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(WellContextFilter.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(WellContextFilter.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(WellContextFilter.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("WellContextFilter: " + msg);
        //Logger.getLogger( WellContextFilter.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the context
     */
    public WellContext getContext() {
        return context;
    }

    /**
     * @return the musthaveflags
     */
    public BfMaskFlag[] getMusthaveflags() {
        return musthaveflags;
    }

    /**
     * @return the nothaveflags
     */
    public BfMaskFlag[] getNothaveflags() {
        return nothaveflags;
    }

    /**
     * @return the rawtype
     */
    public RawType getRawtype() {
        return rawtype;
    }

    /**
     * @return the flow
     */
    public int getFlow() {
        return flow;
    }

    /**
     * @return the coord
     */
    public WellCoordinate getCoord() {
        return coord;
    }
}
