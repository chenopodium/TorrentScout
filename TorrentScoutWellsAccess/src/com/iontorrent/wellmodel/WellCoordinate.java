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


import com.iontorrent.rawdataaccess.wells.BfMaskDataPoint;
import com.iontorrent.rawdataaccess.wells.BfMaskFlag;
import com.iontorrent.rawdataaccess.wells.ScoreMaskFlag;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * The selected rectangle in a display of wells
 * @author Chantal Roth
 */
public class WellCoordinate  implements Serializable{

    private int col;
    private int row;
    private BfMaskDataPoint maskdata;
    private double[] scoredata;
    
    public WellCoordinate(){}
    public WellCoordinate(int col, int row) {
        this.col = col;
        this.row = row;
        
    }

    @Override
    public String toString() {
        return "x="+col+", y="+row;
    }
    @Override
    public int hashCode() {
        return col*4096+row;
    }
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof WellCoordinate)) return false;
        WellCoordinate c = (WellCoordinate)o;
        return c.col == col && c.row == row;
    }
/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( WellCoordinate.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( WellCoordinate.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( WellCoordinate.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("WellCoordinate: " + msg);
        //Logger.getLogger( WellCoordinate.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the col
     */
    public int getCol() {
        return col;
    }
    public int getX() {
        return col;
    }
    public int getY() {
        return row;
    }
       /**
     * @return the row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return the maskdata
     */
    public BfMaskDataPoint getMaskdata() {
        return maskdata;
    }

    public boolean hasFlag(int code) {
        if (maskdata == null) return false;
        return getMaskdata().hasFlag(BfMaskFlag.get(code));
    }
     public boolean hasFlag(BfMaskFlag flag) {
         if (maskdata == null) return false;
        return getMaskdata().hasFlag(flag);
    }
    /**
     * @param maskdata the maskdata to set
     */
    public void setMaskdata(BfMaskDataPoint maskdata) {
        this.maskdata = maskdata;
    }

    /**
     * @return the scoredata
     */
    public double[] getScoredata() {
        return scoredata;
    }

     public double getScoredata(ScoreMaskFlag flag) {
        if (scoredata == null) return 0;
        else return scoredata[flag.getCode()];
    }
      public double getScoredata(int flag) {
        if (scoredata == null) {
           // if (flag < 2) p("scoredata is null. Got no scoredata for flag "+flag +" at "+this.toString());
            return 0;
        }
        else return scoredata[flag];
    }
    /**
     * @param scoredata the scoredata to set
     */
    public void setScoredata(double[] scoredata) {
        this.scoredata = scoredata;
    }

    public boolean isValid() {
        return col>=0 && row >=0;
    }

    public WellCoordinate subtract(WellCoordinate start) {
        int x = Math.max(0, col - start.getCol());
        int y = Math.max(0,row - start.getRow());
        return new WellCoordinate(x, y);
    }

    public WellCoordinate add(int dx, int dy) {
        return new WellCoordinate(col+dx, row+dy);
    }

    
  
}
