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
import com.iontorrent.rawdataaccess.transformation.DataTransformation;
import com.iontorrent.rawdataaccess.transformation.TransformFactory;
import com.iontorrent.utils.ProgressListener;
import com.iontorrent.utils.io.FileUtils;
import com.iontorrent.wellmodel.RasterData;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellFlowData;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class RawDataFacade {

    private PGMAcquisitionGlobalHeader header;
    // cache multiple regions - at least 4
    private LinkedList<PGMAcquisition> cachedDataList = new LinkedList<PGMAcquisition>();
    private String raw_dir;
    private String cache_dir;
    private RawType filetype;
    private String errormsg;
    private RasterIO raster;
    private int DX = 64;
    private int DY = 64;
    public static boolean ISCONVERTING;
    /** reuse the same for he same raw_dir */
    private static RawDataFacade facade;

    public static RawDataFacade getFacade(String raw, String cache_dir, RawType t) {
        if (t == null) {
            Logger.getLogger(RawDataFacade.class.getName()).log(Level.SEVERE, "Got no file type in getFacade");
            return null;
        }
        if (facade == null || facade.raw_dir == null || facade.filetype == null || facade.cache_dir==null) {
            facade = new RawDataFacade(raw, cache_dir, t);
        } 
        else if (facade.cache_dir!=null &&  raw != null && t != null && facade.raw_dir.equalsIgnoreCase(raw) && facade.filetype.equals(t)) {
            //   p("same RawDataFacade: t="+t+", filetype="+facade.filetype);
        } else {
            //  p("NEW RawDataFacade: t="+t);
            facade = new RawDataFacade(raw, cache_dir, t);
        }
        return facade;
    }

    private RawDataFacade(String raw_dir, String cache_dir, RawType type) {
        this.raw_dir = raw_dir;
        this.cache_dir = cache_dir;
        this.filetype = type;

        raster = new RasterIO(raw_dir, cache_dir, filetype);
    }
 
    
    public boolean isCached(WellCoordinate coord, int flow) {
        String file = RasterIO.getRawFilePath(filetype, raw_dir, flow);
        if (getCachedData(coord, flow) != null) {
            return true;
        } else {
            
            if (this.isRegionFormat(file, flow)) {
                //  p("Got raster format, fast!");
                return true;
            }
            if (raster.existsRasterFile(coord, flow)) {
                return true;
            } else {
                if (header != null && header.getNrCols() < 1000) {
                    return true;
                }
                File f = new File(file);
                // < 10 MB is small... cropped data set for instance
                if (f.length() / 1000000 < 10) {
                    return true;
                } else {
                    return false;
                }
            }
        }

    }

    public PGMAcquisition getCachedData(WellCoordinate coord, int flow) {
        String path = RasterIO.getRawFilePath(filetype, raw_dir, flow);
        for (PGMAcquisition acq : cachedDataList) {
            if (acq != null) {
                if (acq.contains(coord.getX(), coord.getY()) && acq.getFlow() == flow && acq.getPath().equals(path)) {
                    return acq;
                }
                else if (acq.getFlow() == flow && acq.getPath().equals(path)) {
                    p("Got same path and flow: "+flow+"/"+path+", but different coords: "+coord+" vs "+acq.contains(coord.getX(), coord.getY()));
                }
                //  else p("Got cached PGM data "+acq+", but not coord "+coord+"  or not flow "+flow);
            }

        }
        return null;
    }

    public PGMAcquisition getCachedData(int x, int y, int flow) {
        for (PGMAcquisition acq : cachedDataList) {
            if (acq != null && acq.contains(x, y) && acq.getFlow() == flow) {
                return acq;
            }
        }
        return null;
    }

    private void addCachedData(PGMAcquisition data) {
        if (cachedDataList.size() > 4) {
            cachedDataList.removeFirst();
        }
        cachedDataList.add(data);
    }

    public WellFlowData readOneWellFromAcq(int x, int y, int startflow) {
        PGMAcquisition cachedData = getCachedData(x, y, startflow);
        if (cachedData == null) {
            // p("Reading directly from acquisition .dat file, DX/DY=" + DX + "/" + DY);
            String file = RasterIO.getRawFilePath(filetype, raw_dir, startflow);
            int startx = Math.max(0, x - DX / 2);
            int starty = Math.max(0, y - DY / 2);
            cachedData = this.readAcquisition(file, startx, starty, DX, DY, startflow, 0, -1);
            this.addCachedData(cachedData);
        }
        // else p("Reading from cache");
        WellFlowData flowdata = new WellFlowData(x, y, startflow, filetype, null);
        if (flowdata == null || cachedData == null) return null;
        flowdata.setData(cachedData.getDataForPos(x, y));
        flowdata.setTimestamps(cachedData.getTimeStamp());
        return flowdata;
    }

    public static boolean isConverting() {
        return ISCONVERTING;
    }

    public static void setIsConverting(boolean b) {
        ISCONVERTING = b;
    }

    public ArrayList<WellFlowData> readAllWellsAndTransform(ArrayList<WellCoordinate> coords, int startflow, WellContext context) {
        ArrayList<WellFlowData> res = new ArrayList<WellFlowData>();
        for (WellCoordinate coord : coords) {
            WellFlowData data = readOneWellAndTransform(coord, startflow, context);
            if (data != null) {
                res.add(data);
            }
        }

        return res;

    }
//    public ArrayList<WellFlowData> readAllWells(ArrayList<WellCoordinate> coords, int startflow) {
//        ArrayList<WellFlowData> res = new ArrayList<WellFlowData>();
//        for (WellCoordinate coord : coords) {
//            WellFlowData data = readOneWell(coord, startflow);
//            if (data != null) {
//                res.add(data);
//            }
//        }
//
//        return res;
//    }

    public WellFlowData readOneWellAndTransform(WellCoordinate coord, int startflow, WellContext context) {
        ArrayList<DataTransformation> transforms = TransformFactory.getTransformations();
        WellFlowData data = readOneWell(coord, startflow);
      //  p("Data before transformations: " + data);
        for (DataTransformation trans : transforms) {
            if (trans.isEnabled()) {
                trans.setContext(context, this.filetype);
                //    p("Applying data trans "+trans.getName());
                trans.transform(data, coord, startflow);
            }
            //  else p("NOT Applying data trans "+trans.getName());
        }
    //    p("Data after transformations: " + data);
        return data;
    }

 
    public WellFlowData readOneWell(WellCoordinate coord, int startflow) {
        int x = coord.getCol();
        int y = coord.getRow();
        WellFlowData data = null;
        
        
        if (this.getCachedData(coord, startflow) != null) {
            //  p("reading "+x+"/"+y+" and flow "+startflow+" from cached acq file, filetype="+this.filetype);
            data = readOneWellFromAcq(x, y, startflow);
        } else {
            String file = RasterIO.getRawFilePath(filetype, raw_dir, startflow);
            if (FileUtils.exists(file) && isRegionFormat(file, startflow)) {
                //  p("File is REGION based. Just read it");
                data = readOneWellFromAcq(x, y, startflow);
            } else {
                File rasterfile = raster.getRasterFileName(coord, startflow, true);
                if (!rasterfile.exists()) {
                    if (header != null && header.getNrCols() > 1000) {
                        p("No region format and large file, no cache for " + coord + " :" + rasterfile + ". Creating cached raster first, flow=" + startflow);
                        // read acq file and cache
                        ISCONVERTING = true;
                        raster.convertOneFileToXY(startflow, coord);
                        ISCONVERTING = false;
                    } else {
                        p("Reading SMALL old style file");
                        data = readOneWellFromAcq(x, y, startflow);
                    }
                } else {
                    p("Reading from old cached xy file");
                    data = readOneWellFromXY(coord, startflow);
                }
            }
            if (data != null) {
                return data;
            }
        }
        if (data == null) {
            err("readOneWell: Got no data for " + coord + "  and flow " + startflow);
        }

        return data;
    }

    public boolean isSmall() {
        String file = RasterIO.getRawFilePath(filetype, raw_dir, 0);
        return isSmall(file, 0);
    }

    public boolean isSmall(int flow) {
        String file = RasterIO.getRawFilePath(filetype, raw_dir, flow);
        return isSmall(file, flow);
    }

    public boolean isSmall(String file, int flow) {
        if (!FileUtils.exists(file)) {
            return false;
        }
        File f = new File(file);
        if (f.length() < 10000000) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isRegionFormat() {
        String file = RasterIO.getRawFilePath(filetype, raw_dir, 0);
        return isRegionFormat(file, 0);
    }

    public boolean isRegionFormat(int flow) {
        String file = RasterIO.getRawFilePath(filetype, raw_dir, flow);
        return isRegionFormat(file, flow);
    }

     public boolean existsFile(int flow) {
        String file = RasterIO.getRawFilePath(filetype, raw_dir, flow);
        return FileUtils.exists(file);
    }
    public boolean isRegionFormat(String file, int flow) {
        if (!FileUtils.exists(file)) {
            return false;
        }
        header = PGMAcquisition.getHeader(file);
        return header.isRegionFormat();
    }

    public PGMAcquisitionGlobalHeader getHeader(int flow) {
        if (filetype == null) {
            err("Got no filetype!");
            return null;
        }
        String file = getRawFilePath(filetype, raw_dir, flow);
        
        header = PGMAcquisition.getHeader(file);
        return header;

    }

    public PGMAcquisition readAcquisition(String file, int startx, int starty, int dx, int dy, int flow, int startframe, int endframe) {
        return readAcquisition(file, startx, starty, dx, dy, flow, startframe, endframe, false);
    }
    public PGMAcquisition readAcquisition(String file, int startx, int starty, int dx, int dy, int flow, int startframe, int endframe, boolean debug) {
        if (file == null) file = RasterIO.getRawFilePath(filetype, raw_dir, flow);
        if (!FileUtils.exists(file)) {
            err("File does not exist :"+file+", flow="+flow);
            return null;
        }
        PGMAcquisition data = new PGMAcquisition(file, flow);
        header = PGMAcquisition.getHeader(file);
        if (header == null) {
            err("Got no header of file "+file);
            return null;
        }
        p(file + ": " + header.getNrCols() + "x" + header.getNrRows() + ", reading " + startx + "/" + starty + "+" + dx + "/" + dy);
        if (startx > header.getNrCols() || starty > header.getNrRows()) {
            err("Coordinates out of bounds: "+startx+"/"+starty+", max is :"+header.getNrCols()+"/"+header.getNrRows());
            return null;
        }
        try {
            //data.readFile(startx, starty, dx, dy);
            data.readFile(startx, starty, dx, dy, startframe, endframe, debug);
            //   p("read SUB file " + file);
        } catch (Exception e) {
            String msg = "Got an exception while reading " + file + ":" + e.getMessage();
            err(msg, e);
            this.errormsg = msg;
            return null;
        }

        return data;
    }

    /** Read the data for one particular XY raster, for ALL frames one flow... (?)
     * @param raster_size 
     */
    private WellFlowData readOneWellFromXY(WellCoordinate coord, int startflow) {
        errormsg = null;
        WellFlowData data = raster.readOneWellFromXY(coord, startflow);
        if (data == null) {
            //       p("readOneWellFromXY: Got no data for " + coord + "  and flow " + startflow + "  from raster file");
        }
        //    else p("Got data from rasterfile: "+data);
        return data;
    }

    public String getErrorMsg() {
        return errormsg;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        this.errormsg = msg + ":" + ex.getMessage();
        // System.out.println("RawDataFacade: " + msg + ", " + ex.getMessage());

        Logger.getLogger(RawDataFacade.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        this.errormsg = msg;
        //  System.out.println("RawDataFacade: " + msg);
        Logger.getLogger(RawDataFacade.class.getName()).log(Level.WARNING, msg);
    }

    private void warn(String msg) {
        // System.out.println("RawDataFacade: " + msg);
        Logger.getLogger(RawDataFacade.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        //  System.out.println("RawDataFacade: " + msg);
        Logger.getLogger(RawDataFacade.class.getName()).log(Level.INFO, msg);
    }

    public int getNrFlowsInCache() {
        return raster.getNrFlowsInCache();
    }

    public static String getRawFilePath(RawType rawType, String rtype, int f) {
        return RasterIO.getRawFilePath(rawType, rtype, f);
    }

    public WellCoordinate computeRasterStartCoord(WellCoordinate coord) {
        return raster.computeRasterStartCoord(coord);
    }

    public RasterData readFromXY(ExperimentContext exp, WellCoordinate coord, int flow, int flow0, ProgressListener list, boolean b) {
        // p("change to use SubFrames");
        try {
            return raster.readFromXY(exp, coord, flow, flow0, list, b);

        } catch (Exception ex) {
            Logger.getLogger(RawDataFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
