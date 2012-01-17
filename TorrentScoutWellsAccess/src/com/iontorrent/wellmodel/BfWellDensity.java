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
import com.iontorrent.rawdataaccess.wells.BfMaskFlag;
import com.iontorrent.rawdataaccess.wells.GeneralWellDensity;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Given a certain bucket size, such as 10x10 pixels, computes the number of empty, duds, beads, live etc 
 * for all properties in the @BfMaskFlag class. This data structure can be used for instance 
 * in visual display.
 * @author Chantal Roth
 */
public class BfWellDensity extends GeneralWellDensity {

    private BfMask bfmask; 
   // private int bucket_size;
  //  public static int DEFAULT_BUCKET_SIZE = 5;    
    
    private BfMaskFlag curflag;
    private int well_density[][][];
    
     private int maxvalues[];
//     public BfWellDensity(BfMask bfmask) {        
//        this(bfmask, DEFAULT_BUCKET_SIZE);
//    }
    public BfWellDensity(BfMask bfmask, int bucket_size) {    
        super(bucket_size);
        this.bfmask = bfmask;        
        this.bucket_size =  bucket_size;
        computeDensityPlot();
    } 
  

    @Override
    public String computeDensityPlot() {
      // p("Computing Density for bfmask with "+bfmask.getNrCols()+"/"+bfmask.getNrRows()+" cols/rows");
        
        int nr_col_buckets = bfmask.getNrCols() / bucket_size+1;
        int nr_row_buckets = bfmask.getNrRows() / bucket_size+1;
        well_density = new int[nr_col_buckets][nr_row_buckets][BfMaskFlag.getNrFlags()];
        maxvalues = new int[BfMaskFlag.getNrFlags()];
        
      //  p("bucket size: "+bucket_size+", nr buckets: "+nr_col_buckets+"/"+nr_row_buckets);
        for (int flag = 0; flag < BfMaskFlag.getNrFlags(); flag++) {
            int max = 0;
            BfMaskFlag bfflag = BfMaskFlag.get(flag);
        //    p("Computing density for flag "+bfflag.toString());
            for (int c = 0; c < nr_col_buckets; c++) {
                for (int r = 0; r < nr_row_buckets; r++) {
                    // compute density for this area                
                    int count = computeDensity(c, r, bfflag);
                    if (count > max) max = count;
                   
                    well_density[c][r][flag] = count;
                }
            }
            maxvalues[flag] = max;
        }
        return null;
    }
    @Override
    public int getNrCols() {
        return well_density.length;
    }
    @Override
     public int getNrRows() {
        return well_density[0].length;
    }
    @Override
     public int getMax() {
         return maxvalues[curflag.getCode()];
     }
     @Override
     public int getMin() {
         return 0;
     }
    
    @Override
    public int getCount(int c, int r) {
        return well_density[c][r][curflag.getCode()];
    }
    private int computeDensity(int col, int row, BfMaskFlag bfflag) {
        if (bfflag == null) return -1;
        int count = 0;        
        for (int c = col*bucket_size; c < Math.min((col+1)*bucket_size,bfmask.getNrCols()); c++) {
            for (int r =row*bucket_size; r < Math.min((row+1)*bucket_size,bfmask.getNrRows()); r++) {
                if (bfflag.isBitSet(bfmask.getMaskAt(c, r))) {
                //if (bfmask.getDataPointAt(c, r).hasFlag(bfflag)){
                    count++;
                }
            }
        }
        return count;
    }
    private void err(String msg, Exception ex) {
        Logger.getLogger( BfWellDensity.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void p(String msg) {
        System.out.println("BfWellDensity: " + msg);
        //Logger.getLogger( BfWellDensity.class.getName()).log(Level.INFO, msg, ex);
    }

    @Override
    public Object getFlag() {
        return curflag;
    }
    public void setFlag(Object flag) {
        curflag = (BfMaskFlag) flag;
    }
}
