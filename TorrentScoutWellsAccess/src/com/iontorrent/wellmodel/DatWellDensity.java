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


import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.rawdataaccess.pgmacquisition.PGMAcquisition;
import com.iontorrent.rawdataaccess.pgmacquisition.PGMFrame;
import com.iontorrent.rawdataaccess.pgmacquisition.RawDataFacade;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.rawdataaccess.wells.GeneralWellDensity;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Given a certain bucket size, such as 10x10 pixels, computes the number of empty, duds, beads, live etc 
 * for all properties in the @BfMaskFlag class. This data structure can be used for instance 
 * in visual display.
 * @author Chantal Roth
 */
public class DatWellDensity extends GeneralWellDensity {

    private ExperimentContext exp; 
 //   private int bucket_size;
    int nrflags;
    int frame;
    int flow;
    RawDataFacade io ;
  //  public static int DEFAULT_BUCKET_SIZE = 5;    
    
   // private BfMaskFlag curflag;
    private int well_density[][][];
    
    RawType type;
     private int maxvalues[];
     private int minvalues[];
//     public BfWellDensity(BfMask bfmask) {        
//        this(bfmask, DEFAULT_BUCKET_SIZE);
//    }
    public DatWellDensity(ExperimentContext exp, int bucket_size, RawType type, int flow, int frame) {    
        super(bucket_size);
         this.exp  = exp;
         this.flow = flow;
         this.frame = frame;
         this.type = type;
        this.bucket_size =  bucket_size;
        io = RawDataFacade.getFacade(exp.getRawDir(), exp.getCacheDir(), type);
        
    }
  

    @Override
    public String  computeDensityPlot() {
    //    p("Computing Density for flow "+ flow+" dat in "+exp.getRawDir());
       nrflags = 1; 
        
        maxvalues = new int[nrflags];
        minvalues = new int[nrflags];
       
      
        PGMAcquisition aq = io.readAcquisition(null, 0,0,-1,-1, flow, 0, frame+1);
        if (aq == null) {
            p("Could not read dat fle in  "+exp.getRawDir());
            return "Could not read flow "+flow+" of "+this.type+" in  "+exp.getRawDir();
        }
        exp.setNrcols(aq.getNrCols());
        exp.setNrrows(aq.getNrRows());
        int nr_col_buckets = exp.getNrcols() / bucket_size+1;
        int nr_row_buckets = exp.getNrrows() / bucket_size+1;
       //  p("bucket size: "+bucket_size+", nr buckets: "+nr_col_buckets+"/"+nr_row_buckets);
        well_density = new int[nr_col_buckets][nr_row_buckets][nrflags];
      //  p("Got aq: "+aq.getFrames().length+" frames, "+aq.getNrRows()+"/"+aq.getNrCols()+" rows and cols");        
        PGMFrame frames[] = aq.getFrames();
        if (frames[frame] == null) {
            p("Could not load frame "+frame);
            return "Could not load frame "+frame;
        }
        int size = bucket_size*bucket_size;
        for (int flag = 0; flag < nrflags; flag++) {
            int max = Integer.MIN_VALUE;           
            int min = Integer.MAX_VALUE;
        //    p("Computing density for flag "+bfflag.toString());
            for (int c = 0; c < nr_col_buckets; c++) {
                for (int r = 0; r < nr_row_buckets; r++) {
                    // compute density for this area                
                    int count = computeDensity(c, r, frames)/size;
                    //int count =  aq.getFrames()[0].getDataAt(c*bucket_size, r*bucket_size);
                    if (count > max) max = count;   
                    if (count < min) min = count;   
                    well_density[c][r][flag] = count;
                }
            }
            maxvalues[flag] = max;
            minvalues[flag] = min;
        }
    //    p("Got max: "+Arrays.toString(maxvalues));
        return null;
        
    }
    @Override
    public int getNrCols() {
        if (well_density == null) return 0;
        return well_density.length;
    }
    @Override
     public int getNrRows() {
        if (well_density == null) return 0;
        return well_density[0].length;
    }
    @Override
     public int getMax() {
         return maxvalues[0];
     }
    @Override
     public int getMin() {
         return minvalues[0];
     }
    @Override
    public int getCount(int c, int r) {
        return well_density[c][r][0];
    }
    private int computeDensity(int col, int row, PGMFrame[] frames) {
        int count = 0;
        for (int c = col*bucket_size; c < Math.min((col+1)*bucket_size,exp.getNrcols()); c++) {
            for (int r =row*bucket_size; r < Math.min((row+1)*bucket_size,exp.getNrrows()); r++) {
                int value = frames[frame].getDataAt(c, r);
                if (frame > 0) value = value- frames[0].getDataAt(c, r);
                count+=value;
            }
        }
        //int count =  aq.getFrames()[0].getDataAt(col*bucket_size, row*bucket_size);
        return count;
    }
    private void err(String msg, Exception ex) {
        Logger.getLogger( DatWellDensity.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void p(String msg) {
        System.out.println("DatWellDensity: " + msg);
        Logger.getLogger( DatWellDensity.class.getName()).log(Level.INFO, msg);
    }

    @Override
    public Object getFlag() {
        return null;
    }
    public void setFlag(Object flag) {
        //curflag = (BfMaskFlag) flag;
    }
}
