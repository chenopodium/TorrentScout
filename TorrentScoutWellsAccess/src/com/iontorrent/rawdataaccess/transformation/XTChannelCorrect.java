/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.rawdataaccess.transformation;

import com.iontorrent.rawdataaccess.pgmacquisition.PGMAcquisition;
import com.iontorrent.rawdataaccess.pgmacquisition.PGMFrame;
import com.iontorrent.rawdataaccess.pgmacquisition.RawDataFacade;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;

import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellFlowData;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
// XTChannelCorrect:
// For the 316 and 318, corrects cross-talk due to incomplete analog settling within the 316 and 318, and also
// residual uncorrected incomplete setting at the output of the devices.  
// works along each row of the image and corrects cross talk that occurs
// within a single acquisition channel (every fourth pixel is the same channel on the 316/318)
// This method has no effect on the 314.
// NOTE:  A side-effect of this method is that the data for all pinned pixels will be replaced with
// the average of the surrounding neighbor pixels.  This helps limit the spread of invalid data in the pinned
// pixels to neighboring wells
 * @author Chantal Roth
 */
public class XTChannelCorrect extends DataTransformation {

    /**
     * struct ChannelXTCorrectionDescriptor {
    float **xt_vector_ptrs; // array of pointers to cross-talk correction vectors
    int num_vectors;        // number of vectors in xt_vector_ptrs
    int vector_len;         // length of each correction vector
    int *vector_indicies;   // relative indices for the application of each vector
    };
     */
    //BfMask mask;
    ChannelXTCorrectionDescriptor correction;
    RawDataFacade io;

    public XTChannelCorrect() {
        super("XT channel correct", "Correct cross talk from neighboring wells.\nIf available, uses the file " + ChannelXTCorrectionDescriptor.FILENAME + ",\notherwise uses default values for 316 and 318 chips");
        //  this.setEnabled(false);
    }

    @Override
    public void setContext(WellContext newc, RawType t) {
        if (context != null && newc != null && t == super.type && context.getCacheDirectory().equalsIgnoreCase(newc.getCacheDirectory())) {
            return;
        }
        super.setContext(newc, t);
        io = RawDataFacade.getFacade(context.getRawDirectory(), context.getCacheDirectory(), t);
        correction = getDescriptor();
    }

    private ChannelXTCorrectionDescriptor getDescriptor() {
        correction = new ChannelXTCorrectionDescriptor(context.getRawDirectory());

        if (correction == null || correction.getXt_vectors() == null) {
           // p("Got no ChannelXTCorrectionDescriptor file, will get default for chip type " + context.getChipType());
            if (context.is316()) {
                correction = ChannelXTCorrectionDescriptor.Default_316;
            } else if (context.is318()) {
                correction = ChannelXTCorrectionDescriptor.Default_318;
            }
        }
        if (correction == null || correction.getXt_vectors() == null) {
           // p("Got no correction descriptor");
        } else {
          //  p("Got descriptor: " + correction.getNum_vectors() + "/" + correction.getVector_len());
        }
        return correction;
    }

    @Override
    public String toLongString() {
        if (correction == null || correction.getXt_vectors() == null) {
            if (context == null) {
                return "Got no chip context yet";
            } else {
                return "Got no XTCorrection parameters for this chip " + context.getChipType();
            }
        } else {
            return "XTCorrection parameters for this chip " + context.getChipType() + "\n" + correction.toString();
        }
    }

    /** Do the xy channel correction. Returns error message to be reported back to user */
    @Override
    public String transform(WellFlowData data, WellCoordinate coord, int flow) {

        if (data == null) {
            return null;
        }

        if (correction == null || correction.getXt_vectors() == null) {
          //  p("Got no ChannelXTCorrectionDescriptor for chip " + context.getChipType());
            return null;
        }

        // fill in pinned pixels with average of surrounding valid wells
        // p("BG step for pinned pixels not done");
        //  BackgroundCorrect(mask,        MaskPinned, (MaskAll & ~MaskPinned & ~MaskExclude),  0, 0,  5,       5,       false,         false,          true);
        //BackgroundCorrect(Mask *mask, MaskType these, MaskType usingThese,        innerx, innery, outerx, outery,  bool saveBkg, bool onlyBkg, bool replaceWBkg)

        PGMAcquisition acq = io.getCachedData(coord, flow);
        // acq MUST NOT be null at this point as we have already read it (WellFlowData)!
        if (acq == null) {
            io.readOneWellFromAcq(coord.getX(), coord.getY(), flow);
            acq = io.getCachedData(coord, flow);
        }
        if (acq == null) {
            String error = "Cached data must not be null after reading well flow at " + coord + ", data is: " + data;
            err(error);
            return error;
        }
        int nrframes = acq.getNrFrames();
        int cols = acq.getNrCols();

        double[][] vects = correction.getXt_vectors();
        int nvects = correction.getNum_vectors();
        int[] col_offset = correction.getVector_indices();
        int vector_len = correction.getVector_len();

        int row = coord.getY();
        int col = coord.getX();
        int vndx = (col % nvects);
        double[] vect = vects[vndx];


        for (int frame = 0; frame < nrframes; frame++) {
            PGMFrame image = acq.getFrames()[frame];
            double sum = 0.0;
            for (int vn = 0; vn < vector_len; vn++) {
                int ndx = col + col_offset[vn];
                if ((ndx >= 0) && (ndx < cols)) {
                    if (!image.contains(ndx, row)) {
                        io.readOneWellFromAcq(ndx, row, flow);
                        // we also need to make sure we have the data for the area around this well!
                        acq = io.getCachedData(ndx, row, flow);
                        image = acq.getFrames()[frame];
                    }
                    sum += image.getDataAt(ndx, row) * vect[vn];
                }
            }
            data.setDataAt(frame, sum);
            // we are not changing the cached data! We only change the data for this ONE data point
            //do NOT do this: image.setDataAt(col, row, (int)sum);
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
        //   System.out.println("XTChannelCorrect: " + msg);
        Logger.getLogger(XTChannelCorrect.class.getName()).log(Level.INFO, msg);
    }
}
