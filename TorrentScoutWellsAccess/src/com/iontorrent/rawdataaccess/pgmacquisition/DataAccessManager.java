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
package com.iontorrent.rawdataaccess.pgmacquisition;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.utils.ProgressListener;
import com.iontorrent.utils.log.ShortFormatter;
import com.iontorrent.wellalgorithms.WellAlgorithm;
import com.iontorrent.wellalgorithms.WellContextFilter;
import com.iontorrent.wellmodel.RasterData;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellFlowData;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class DataAccessManager {

    /** keep no more than 100 rasterdata objects... that may alrady be too much@ */
    private String errormsg;
    WellContext context;
    static DataAccessManager manager;
    private Exception ex;
    ExperimentContext exp;
    static {
        setFormatter();
    }

    public static DataAccessManager getManager(WellContext context) {
        if (manager == null || !manager.context.equals(context)) {
            manager = new DataAccessManager(context);
        }
        return manager;
    }

    public String getErrorMsg() {
        return errormsg;
    }

    private DataAccessManager(WellContext context) {
        this.context = context;
        this.exp = context.getExpContext();

    }

    public void clear() {
        WellAlgorithm.clear();
    }

   
    public boolean isCached(WellContext context, WellCoordinate coord, int flow, RawType filetype) {
        RawDataFacade io = RawDataFacade.getFacade(context.getRawDirectory(), context.getCacheDirectory().toString(), filetype);
        return io.isCached(coord, flow);
    }
    public boolean existsFile(WellContext context, WellCoordinate coord, int flow, RawType filetype) {
        RawDataFacade io = RawDataFacade.getFacade(context.getRawDirectory(), context.getCacheDirectory().toString(), filetype);
        return io.existsFile(flow);
    }

    public static boolean isConverting() {
        return RawDataFacade.isConverting();
    }

    public static void setIsConverting(boolean b) {
        RawDataFacade.setIsConverting(b);
    }

    public WellFlowData getFlowData(WellContextFilter filter, boolean useAllWellsInArea) {
        errormsg = null;
        if (filter == null) {
            err("Got no wellcontextfilter");
            return null;
        }
        RawType filetype = filter.getRawtype();
        // p("DataAccessManager.getFlowData("+useAllWellsInArea+"), filter="+filter.getKey()+", filetype="+filetype);
        RawDataFacade io = RawDataFacade.getFacade(context.getRawDirectory(), context.getCacheDirectory(), filetype);
        WellFlowData data = null;
        try {
            // if (!useAllWellsInArea) p("Computing data for "+filter.getCoord());
            WellAlgorithm alg = new WellAlgorithm(filter, 5, useAllWellsInArea);
            data = alg.getRawWell();
            if (data == null) {
                this.errormsg = io.getErrorMsg();
            }
            return data;
        } catch (Exception ex) {
            err("getFlowData, error", ex);
            this.ex = ex;
        }
        return null;

    }
//     public WellFlowData getFlowDataForEmptyFlows(WellContextFilter filter, SffRead read) {
//         ArrayList<Integer> flows = new ArrayList<Integer>();
//         
//         for (int f = 0; f < this.context.getNrFlwos(); f++) {
//             // is flow empty? Check ionogram?
//             
//         }
//         return getFlowDataForFlows(filter, flows);
//     }

    public Exception getException() {
        return ex;
    }
    public void updateContext(ExperimentContext exp) {
         RawDataFacade io = RawDataFacade.getFacade(context.getRawDirectory(), context.getCacheDirectory(), RawType.ACQ);
          PGMAcquisitionGlobalHeader h = io.getHeader(0);
        if (h == null) {
            err("Could not read header for flow  "+0);
            return ;
        }
        exp.setNrcols(h.getNrCols());
        exp.setNrrows(h.getNrRows());
    }

    public RasterData getRasterDataForArea(RasterData oldData, int rastersize, WellCoordinate relcoords, int flow, 
            RawType filetype, ProgressListener list, int startframe, int endframe) throws Exception {

        RawDataFacade io = RawDataFacade.getFacade(context.getRawDirectory(), context.getCacheDirectory(), filetype);
        PGMAcquisitionGlobalHeader h = io.getHeader(flow);
        if (h == null) {
            err("Could not read header for flow  "+flow);
            return null;
        }
        // check if we already loaded dat for one flow and coord
        RasterData data = new RasterData(this.exp, rastersize, h, relcoords, flow, flow);
        data.setFiletype(filetype);
        if (oldData != null && oldData.equals(data)) {
            p("Data same, returning");
            return oldData;
        }
        else p("Old data not the same");
        
         p("Reading raster data for flow "+flow+", span "+rastersize+", coord "+relcoords+", cache: "+context.getCacheDirectory()+", filetype:"+filetype);
       
        int cx = relcoords.getX();
        int cy = relcoords.getY();
        int x1 = Math.max(0, cx - rastersize / 2);
        int x2 = x1 + rastersize;
        int y1 = Math.max(0, cy - rastersize / 2);
        int y2 = y1 + rastersize;

        data.setStartCoord(new WellCoordinate(x1, y1));

        double inc = 100.0 / rastersize;
        double prog = 0;
        float[][][][] rdata = data.getRasterdata();
        p("Loading raster data from "+x1+"/"+y1+"-"+x2+"/"+y2+ " up to frame "+endframe);
        PGMAcquisition acq = io.readAcquisition(null, x1, y1, rastersize, rastersize, flow, 0, endframe);
        if (acq == null) {
            err("Got no result - check coordinates: "+io.getErrorMsg());
            return null;
                    
        }
        if (endframe <=0) endframe = acq.getNrFrames();
        PGMFrame first = acq.getFrames()[0];
        for (int f = 0; f < endframe; f++) {
            PGMFrame frame = acq.getFrames()[f];
            for (int x = x1; x < x2; x++) {
                if (list != null) {
                    prog += inc;
                    list.setProgressValue((int) prog);
                }
                for (int y = y1; y < y2; y++) {
                    int val = frame.getDataAt(x, y);
                     rdata[x - x1][y - y1][0][f] = val-first.getDataAt(x, y);
                   // if (f > 0) rdata[x - x1][y - y1][0][f] = val-rdata[x - x1][y - y1][0][0];
                   // else rdata[x - x1][y - y1][0][f] =0;
                }
            }
            data.setTimeStamp(0, f, frame.getTimestamp());
        }
        if (data == null) {
            err("Could not read data for " + relcoords + " and flow " + flow + ", maybe files were not converted yet? Or maybe there are not as many flows?");
        } else {
           // boolean allpinned = true;
//            for (int i =0; i < 10; i++) {
//                int x= (int) (Math.random()*data.getRaster_size());
//                int y= (int) (Math.random()*data.getRaster_size());
//                if (!data.isPinned(x, y)) allpinned = false;
//                p("Got data, example: "+ Arrays.toString(data.getTimeSeries(x, y, 0)));
//            }
//            if (allpinned) {
//                p("Seemed to get all pinned data! Samples");
//                for (int i =0; i < 10; i++) {
//                int x= (int) (Math.random()*data.getRaster_size());
//                int y= (int) (Math.random()*data.getRaster_size());
//                if (!data.isPinned(x, y)) allpinned = false;
//                p("Got data, example: "+ Arrays.toString(data.getTimeSeries(x, y, 0)));
//            }
                //acq = io.readAcquisition(null, x1, y1, rastersize, rastersize, flow, 0, endframe, true);
           // }
        }

        return data;

    }

    public RasterData getRasterDataForArea(int span, WellCoordinate coord, int flow, RawType filetype, ProgressListener list) throws Exception {

        RawDataFacade io = RawDataFacade.getFacade(context.getRawDirectory(), context.getCacheDirectory(), filetype);

        // check if we already loaded dat for one flow and coord


        RasterData data = new RasterData(exp, span, io.getHeader(flow), coord, flow, flow);

        int cx = coord.getX();
        int cy = coord.getY();

        int x1 = Math.max(0, cx - span / 2);
        int x2 = x1 + span;
        int y1 = Math.max(0, cy - span / 2);
        int y2 = y1 + span;

        data.setStartCoord(new WellCoordinate(x1, y1));

        double inc = 100.0 / span;
        double prog = 0;
        float[][][][] rdata = data.getRasterdata();
        for (int x = x1; x < x2; x++) {
            if (list != null) {
                prog += inc;
                list.setProgressValue((int) prog);
            }
            for (int y = y1; y < y2; y++) {
                try {
                    WellFlowData onewell = io.readOneWell(new WellCoordinate(x, y), flow);
                    if (onewell != null) {
                        for (int f = 0; f < onewell.getNrFrames(); f++) {
                            rdata[x - x1][y - y1][0][f] = (int) onewell.getData()[f]-rdata[x - x1][y - y1][0][0];
                        }
                    }
                    //  p("Got well data "+x+"/"+y+": "+onewell);
                } catch (Exception e) {
                    p("Got an exception :" + e.getMessage());
                    throw e;
                }
            }
        }


        if (data == null) {
            err("Could not read data for " + coord + " and flow " + flow + ", maybe files were not converted yet? Or maybe there are not as many flows?");
        } else {
            //  p("Got data " + data + ", putting it in cache");
        }

        return data;
    }

    public RasterData getRasterData(WellCoordinate coord, int flow, RawType filetype, ProgressListener list) throws Exception {

        RawDataFacade io = RawDataFacade.getFacade(context.getRawDirectory(), context.getCacheDirectory(), filetype);

        // check if we already loaded dat for one flow and coord
        if (list != null) {
            list.setProgressValue(1);
        }
        RasterData data = null;

        try {
            data = io.readFromXY(exp, coord, flow, flow, list, true);
        } catch (Exception e) {
            p("Got an exception :" + e.getMessage());
            throw e;
        }

        if (data == null) {
            err("Could not read data for " + coord + " and flow " + flow + ", maybe files were not converted yet? Or maybe there are not as many flows?");
        } else {

            p("Got data " + data + ", putting it in cache");
        }

        return data;
    }

    private static void setFormatter() {
        final ShortFormatter formatter = ShortFormatter.getFormatter();
        Logger rootLogger = Logger.getLogger(DataAccessManager.class.getName());

        while (rootLogger.getParent() != null) {
            rootLogger = rootLogger.getParent();
        }

        for (final Handler handler : rootLogger.getHandlers()) {
            handler.setFormatter(formatter);
        }
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {

        Logger.getLogger(DataAccessManager.class.getName()).log(Level.SEVERE, msg, ex);
        Logger.getLogger(DataAccessManager.class.getName()).log(Level.SEVERE, ErrorHandler.getString(ex));

    }

    private void err(String msg) {
        Logger.getLogger(DataAccessManager.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(DataAccessManager.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        //System.out.println("DataAccessManager: " + msg);
        Logger.getLogger(DataAccessManager.class.getName()).log(Level.INFO, msg, ex);
    }
}
