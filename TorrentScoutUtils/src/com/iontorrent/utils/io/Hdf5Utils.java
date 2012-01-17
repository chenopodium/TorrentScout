/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.utils.io;

import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5FloatReader;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import ch.systemsx.cisd.hdf5.IHDF5SimpleReader;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class Hdf5Utils {

    public void test() {
        IHDF5Reader reader = HDF5Factory.openForReading("s:\\data\\1.wells");
        HDF5DataSetInformation info = reader.getDataSetInformation("wells");
        p("info on wells: "+info);
        p("Type: "+info.getTypeInformation());
        p("dims: "+info.getDimensions());
        p("rank: "+info.getRank());
        p("size: "+info.getSize());
        p("layout: "+info.getStorageLayout());
        MDFloatArray data = reader.readFloatMDArray("wells");
        p("Dimensions: "+Arrays.toString(data.dimensions()));
        int[] dims = data.dimensions();
        
        for (int i = 0; i < 10; i++) {
            int x = (int)(Math.random()*dims[0]);
            int y = (int)(Math.random()*dims[1]);
            int f = (int)(Math.random()*(dims[2]-21));
            p("Reading: "+x+"/"+y+"/"+f);
            float value = data.get(x, y, f);
            p("Some value: "+value);

            int[] blockdims =  {1, 1, 20};
            long[] offset = { x, y, 0 }; 
            p("Trying to read a  part of wells: "+x+"/"+y+"/"+0);
            MDFloatArray part = reader.readFloatMDArrayBlockWithOffset("wells", blockdims, offset);
            p("Dimensions after reading 1 well and 20 flows: "+Arrays.toString(part.dimensions()));
            p("20 values: "+part.toString());
            p("20 values flat: "+Arrays.toString(part.getAsFlatArray()));
        }        
    }
    
    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(Hdf5Utils.class.getName()).log(Level.SEVERE, msg, ex);
    }
    
    private void err(String msg) {
     
        Logger.getLogger(Hdf5Utils.class.getName()).log(Level.SEVERE, msg);
    }
    
    private static void warn(String msg) {
        Logger.getLogger(Hdf5Utils.class.getName()).log(Level.WARNING, msg);
    }
    
    private static void p(String msg) {
        System.out.println("Hdf5Utils: " + msg);
        //Logger.getLogger( Hdf5Utils.class.getName()).log(Level.INFO, msg);
    }
}
