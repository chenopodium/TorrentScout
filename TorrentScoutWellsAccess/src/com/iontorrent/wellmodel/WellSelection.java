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
package com.iontorrent.wellmodel;

import com.iontorrent.rawdataaccess.wells.BfMask;
import com.iontorrent.rawdataaccess.wells.BfMaskDataPoint;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The selected rectangle in a display of wells
 * @author Chantal Roth
 */
public class WellSelection {

    private WellCoordinate coord1;
    private WellCoordinate coord2;
    private ArrayList<WellCoordinate> allWells;
    private ArrayList<WellCoordinate> filteredWells;
    private ArrayList<WellFilter> filters;
    public static final int MAX_WELLS = 10000;

    private int offx;
    private int offy;
    
    public WellSelection(int c1, int r1, int c2, int r2) {
        this(new WellCoordinate(Math.min(c1, c2), Math.min(r1, r2)), new WellCoordinate(Math.max(c1, c2), Math.max(r1, r2)), null);
    }

     public WellSelection(int c1, int r1, int c2, int r2,ArrayList<WellCoordinate> coords) {
        this(new WellCoordinate(Math.min(c1, c2), Math.min(r1, r2)), new WellCoordinate(Math.max(c1, c2), Math.max(r1, r2)), coords);
    }
     
    public WellSelection(WellCoordinate coord1, WellCoordinate coord2) {
        this(coord1, coord2, null);
    }

    public WellSelection(WellCoordinate coord1, WellCoordinate coord2, ArrayList<WellCoordinate> coords) {
        this.coord1 = coord1;
        this.coord2 = coord2;
        this.allWells = coords;
      
    }

    public WellSelection(ArrayList<WellCoordinate> coords) {
        this.allWells = coords;
        p("Finding corners");
        int c1 = 100000;
        int r1 = 100000;
        int c2 = 0;
        int r2 = 0;
        for (WellCoordinate coord : allWells) {
            int c = coord.getCol();
            int r = coord.getRow();
            if (c > c2) {
                c2 = c;
            }
            if (c < c1) {
                c1 = c;
            }
            if (r > r2) {
                r2 = r;
            }
            if (r < r1) {
                r1 = r;
            }
        }
        coord1 = new WellCoordinate(c1, r1);
        coord2 = new WellCoordinate(c2, r2);
    }
    public int getAreaSize() {
        return (coord2.getRow()-coord1.getRow())*(coord2.getCol()-coord1.getCol());
    }
    public void createCoordsForEntireArea() {
        allWells = new ArrayList<WellCoordinate>();
        int count = 0;
        for (int c = getCoord1().getCol(); c <= getCoord2().getCol(); c++) {
            for (int r = getCoord1().getRow(); r <= getCoord2().getRow(); r++) {
                //if (!mask.getDataPointAt(c, r).hasFlag(BfMaskFlag.EMPTY)) {
                WellCoordinate coord = new WellCoordinate(c, r);
                allWells.add(coord);
                if (count > MAX_WELLS) {
                    err("Got more wells than" + MAX_WELLS + " in selection, only keeping first " + MAX_WELLS);
                    return;
                }
            }
        }
    }

    public void clearCache() {
        this.filteredWells = null;

    }

    public ArrayList<WellCoordinate> getAllFilteredWells(BfMask mask) {
        if (filteredWells != null && filteredWells.size()>0) {
            return filteredWells;
        }
        if (allWells == null || allWells.size()==0) {
            p("Creating default list of ALL wells for entire area");
            createCoordsForEntireArea();
        
        }
        ArrayList<WellCoordinate> list = new ArrayList<WellCoordinate>();
        if (this.allWells != null) {
            for (WellCoordinate c : allWells) {
                if (WellFilter.passes(c, filters)) {
                    if (c.getMaskdata() == null) {
                        // load it!
                        BfMaskDataPoint dp = mask.getDataPointAt(c.getCol(), c.getRow());
                        if (dp != null) {
                            c.setMaskdata(dp);
                        }
                    }
                    list.add(c);
                }
            }
        } else {
            err("Well selection has no wells!");
        }
        filteredWells = list;
        return list;
    }

    public void loadDataForWells(BfMask mask) {
        if (allWells == null) {
            p("Creating default list of ALL wells for entire area");
            createCoordsForEntireArea();
        
        }
        for (WellCoordinate c : allWells) {
            if (c.getMaskdata() == null) {
                // load it!
                BfMaskDataPoint dp = mask.getDataPointAt(c.getCol(), c.getRow());
                if (dp != null) {
                    c.setMaskdata(dp);
                }
            }
        }

    }

    public void setAllWells(ArrayList<WellCoordinate> result) {
        this.allWells = result;
        clearCache();
    }

    @Override
    public String toString() {
        return "(x="+(getCoord1().getX()+offx)+", y="+(getCoord1().getY()+offy)+")" + "- (x=" + (getCoord2().getX()+offx)+", y="+(getCoord2().getY()+offy)+")";
    }

    /** ================== LOGGING ===================== */
    /**
     * @return the filters
     */
    public ArrayList<WellFilter> getFilters() {
        return filters;
    }

    /**
     * @param filters the filters to set
     */
    public void setFilters(ArrayList<WellFilter> filters) {
        this.filters = filters;
        this.clearCache();
    }

    private void err(String msg, Exception ex) {
        Logger.getLogger(WellSelection.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(WellSelection.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(WellSelection.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("WellSelection: " + msg);
        //Logger.getLogger( WellSelection.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the coord1
     */
    public WellCoordinate getCoord1() {
        return coord1;
    }

    /**
     * @param coord1 the coord1 to set
     */
    public void setCoord1(WellCoordinate coord1) {
        this.coord1 = coord1;
    }

    /**
     * @return the coord2
     */
    public WellCoordinate getCoord2() {
        return coord2;
    }

    /**
     * @param coord2 the coord2 to set
     */
    public void setCoord2(WellCoordinate coord2) {
        this.coord2 = coord2;
    }

    public ArrayList<WellCoordinate> getAllWells() {
        if (allWells == null) {
            p("Creating default list of ALL wells for entire area");
            createCoordsForEntireArea();
        
        }
        return allWells;
    }

    /**
     * @return the offx
     */
    public int getOffx() {
        return offx;
    }

    /**
     * @param offx the offx to set
     */
    public void setOffx(int offx) {
        this.offx = offx;
    }

    /**
     * @return the offy
     */
    public int getOffy() {
        return offy;
    }

    /**
     * @param offy the offy to set
     */
    public void setOffy(int offy) {
        this.offy = offy;
    }
}
