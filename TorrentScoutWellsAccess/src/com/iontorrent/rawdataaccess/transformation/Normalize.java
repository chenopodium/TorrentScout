/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.rawdataaccess.transformation;

import com.iontorrent.rawdataaccess.pgmacquisition.PGMAcquisition;
import com.iontorrent.rawdataaccess.pgmacquisition.PGMFrame;
import com.iontorrent.rawdataaccess.pgmacquisition.RawDataFacade;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;

import com.iontorrent.utils.io.FileUtils;
import com.iontorrent.utils.system.Parameter;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellFlowData;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *

 * @author Chantal Roth
 */
public class Normalize extends DataTransformation {

    /**
     * struct ChannelXTCorrectionDescriptor {
    float **xt_vector_ptrs; // array of pointers to cross-talk correction vectors
    int num_vectors;        // number of vectors in xt_vector_ptrs
    int vector_len;         // length of each correction vector
    int *vector_indicies;   // relative indices for the application of each vector
    };
     */
    RawDataFacade io;
    int start = 5;
    int end = 20;

    public Normalize() {
        super("Normalize", "Subtract average of a range of frames from all frames");
        params = new Parameter[2];
        params[0] = new Parameter("Start frame", start, "First frame to use");
        params[1] = new Parameter("End frame", end, "Last frame to use");
     
    }
   

    @Override
    public void setContext(WellContext newc, RawType t) {
        if (context != null && newc != null && t == super.type && context.getCacheDirectory().equalsIgnoreCase(newc.getCacheDirectory())) return;
        super.setContext(newc, t);
        io = RawDataFacade.getFacade(context.getRawDirectory(), context.getCacheDirectory(), t);
    }
    @Override
    public void setParams(Parameter[] par) {
        super.setParams(par);
        start = par[0].getIntValue();
        end = par[1].getIntValue();

    }

    /** Do the xy channel correction. Returns error message to be reported back to user */
    @Override
    public String transform(WellFlowData data, WellCoordinate coord, int flow) {

        if (data == null) return null;
        // WellFlowData data = io.readOneWell(coord, flow);
        PGMAcquisition acq = io.getCachedData(coord, flow);
        // acq MUST be non null at this point!
        if (acq == null) {
            String error = "Cached data must not be null after reading well flow at " + coord;
            err(error);
            return error;
        }
      //  if (acq.getFrames() == null) return;
        
        int nrframes = Math.min(acq.getNrFrames(), acq.getFrames().length);

        int row = coord.getY();
        int col = coord.getX();

        end = Math.min(nrframes-1, Math.max(start, end));
        double tot = 0.0;
        for (int f = start; f < end && f < nrframes; f++) {
           
            tot += acq.getFrames()[f].getDataAt(col, row);
        }
        tot = tot / (end - start + 1);
        for (int frame = 0; frame < nrframes; frame++) {
            PGMFrame image = acq.getFrames()[frame];
            double val = image.getDataAt(col, row);
            data.setDataAt(frame, val - tot);
        }

        return null;
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(XTChannelCorrect.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {

        Logger.getLogger(XTChannelCorrect.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(XTChannelCorrect.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("XTChannelCorrect: " + msg);
        //Logger.getLogger( XTChannelCorrect.class.getName()).log(Level.INFO, msg);
    }
}
