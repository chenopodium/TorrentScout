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


import com.iontorrent.rawdataaccess.wells.BfMaskFlag;
import com.iontorrent.rawdataaccess.wells.ScoreMaskFlag;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Chantal Roth
 */
public class WellScoreMaskFilter extends WellFilter{

    
    ScoreMaskFlag flag;
    double minval;
    double maxval;
    
    public WellScoreMaskFilter(ScoreMaskFlag flag, double minval, double maxval) {
        super(flag.getName(), "Score filter for "+flag.getDescription());
        this.flag = flag;
        this.minval = minval;
        this.maxval = maxval;
             
    }
  public ScoreMaskFlag getFlag() {
        return flag;
    }
    @Override
    public boolean passes(WellCoordinate coord) {
       double value =  coord.getScoredata(flag);
      value = value / (double)flag.multiplier();
      p("Got "+value+"for "+flag.getName()+", min: "+minval+", max: "+maxval);
       return (value >= minval && value <= maxval);
    }
/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( WellFlagFilter.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( WellFlagFilter.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( WellFlagFilter.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
      //  System.out.println("WellFilterIF: " + msg);
        //Logger.getLogger( WellFlagFilter.class.getName()).log(Level.INFO, msg, ex);
    }
}
