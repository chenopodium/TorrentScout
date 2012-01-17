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


import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.rawdataaccess.wells.BfMaskDataPoint;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Chantal Roth
 */
public class WellFlowData implements Serializable{
    private int flow;
    private int col;
    private int row;
    protected double[] data;
    private long[] timestamps;
    RawType filetype;
    BfMaskDataPoint mask; 
    
    public WellFlowData(int x, int y, int flow, RawType filetype, BfMaskDataPoint mask) {
       this.flow = flow;
       this.col = x;
       this.row = y;
       this.filetype = filetype;
       this.mask = mask;
    }
     
    public WellFlowData createSimilarEmtpyWell(int x, int y) {        
        WellFlowData resultwell = new WellFlowData(x, y, getFlow(), filetype, getMask());
        resultwell.setTimestamps(getTimestamps());
        int nrframes = getNrFrames();
        resultwell.setData(new double[nrframes]);
        return resultwell;
    
    }
     public  Object deepClone() {
        Object o = this;
        Object res = null;
        try {
            ByteArrayOutputStream fos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(o);
            out.flush();
            out.close();
            byte[] obj = fos.toByteArray();
            fos.close();
            ByteArrayInputStream fin = new ByteArrayInputStream(obj);
            ObjectInputStream in = new ObjectInputStream(fin);
            res = in.readObject();
            in.close();
        } catch (Exception e) {
            err(e.getMessage(),e);
            
        }
        //p("result of deep clone:" + res);
        return res;
    }
     public void subtract(WellFlowData subtract) {
        
        double[] tmp = new double[data.length];
        double[] sub = subtract.getData();
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = data[i] - sub[i] ;
        }
        data = tmp;
    }
    public BfMaskDataPoint getMask() {
        return mask;
    }
    public RawType getType() {
        return filetype;
    }
    public int getNrFrames() {
        return data.length;
    }
    public boolean isPrerun() {
        return filetype == RawType.PRERUN;
    }
    public boolean isAcq() {
        return filetype == RawType.ACQ;
    }
    public boolean isBfPre() {
        return filetype == RawType.BFPRE;
    }
    public boolean isBfPost() {
        return filetype == RawType.BFPOST;
    }
    public String toString() {
        String s =  "Raw data for flow :"+getFlow()+ " and type "+filetype.getDescription()+
                "\ndata="+Arrays.toString(data)+
                "\nts="+Arrays.toString(timestamps);
        return s;
    }
/** ================== LOGGING ===================== */
    protected static void err(String msg, Exception ex) {
        Logger.getLogger( WellFlowData.class.getName()).log(Level.SEVERE, msg, ex);
    }

    protected static void err(String msg) {
        Logger.getLogger( WellFlowData.class.getName()).log(Level.SEVERE, msg);
    }

     protected void warn(String msg) {
        Logger.getLogger( WellFlowData.class.getName()).log(Level.WARNING, msg);
    }

    protected void p(String msg) {
        System.out.println("WellFlowData: " + msg);
        //Logger.getLogger( WellFlowData.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the flow
     */
    public int getFlow() {
        return flow;
    }

    /**
     * @return the data
     */
    public double[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(double[] data) {
        this.data = data;
    }
     public void setDataAt(int frame, double value) {
        this.data[frame] = value;
    }
     public void setData(int[] idata) {
         
        this.data = new double[idata.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = idata[i];
        }
    }

    /**
     * @return the timestamps
     */
    public long[] getTimestamps() {
        return timestamps;
    }
    public long getLastTimeStamp() {
        return timestamps[timestamps.length-1];
    }

    /**
     * @param timestamps the timestamps to set
     */
    public void setTimestamps(long[] timestamps) {
        this.timestamps = timestamps;
    }

    /**
     * @return the col
     */
    public int getCol() {
        return col;
    }

    /**
     * @return the row
     */
    public int getRow() {
        return row;
    }
}
