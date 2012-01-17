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

package com.iontorrent.rawdataaccess.wells;


import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Given a certain bucket size, such as 10x10 pixels, computes the number of empty, duds, beads, live etc 
 * for all properties in the @BfMaskFlag class. This data structure can be used for instance 
 * in visual display.
 * @author Chantal Roth
 */
public abstract class GeneralWellDensity {

  //  private BfMask bfmask; 
    protected int bucket_size;
  //  public static int DEFAULT_BUCKET_SIZE = 5;    
    
    private int well_density[][][];
  
    public GeneralWellDensity(int bucket_size) {                
        this.bucket_size =  bucket_size;    
         this.bucket_size = Math.max(1, bucket_size);
    }
    public int getBucketSize() {
        return bucket_size;
    }

    public abstract String computeDensityPlot();
     
    public abstract Object getFlag();
      
    public abstract void setFlag(Object flag);
    
    public abstract int getMax() ;
    
    public abstract int getMin() ;
     
    public int getNrCols() {
        return well_density.length;
    }
     public int getNrRows() {
        return well_density[0].length;
    }
     public abstract int getCount(int c, int r);
   
    private void err(String msg, Exception ex) {
        Logger.getLogger( GeneralWellDensity.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void p(String msg) {
        System.out.println("GeneralWellDensity: " + msg);
        //Logger.getLogger( GeneralWellDensity.class.getName()).log(Level.INFO, msg, ex);
    }

   
}
