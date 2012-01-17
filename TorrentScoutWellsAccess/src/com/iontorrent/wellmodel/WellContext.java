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
import com.iontorrent.rawdataaccess.wells.BfMask;
import com.iontorrent.rawdataaccess.wells.BfMaskReader;
import com.iontorrent.rawdataaccess.wells.WellData;
import com.iontorrent.rawdataaccess.wells.WellsReader;
import com.iontorrent.utils.io.FileTools;
import com.iontorrent.utils.io.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class WellContext {

    private WellSelection selection;
    private WellCoordinate coordinate;
    private BfMask mask;
    private File wellsfile;
    private String flowSequence;
    private int nrflows;
   
    private HashMap<String, WellData> datacache = new HashMap<String, WellData>();

    private ExperimentContext exp;
    
    public WellContext(ExperimentContext exp) {
        //results_dir = FileTools.addSlashOrBackslash(results_dir);
       this.exp = exp;
            
        this.wellsfile = new File(exp.getResultsDirectory() + "1.wells");
//        if ( !wellsfile.exists()) {
//            wellsfile = new File(exp.getCacheDir() + "1.wells");
//        }
        
    }
    public ExperimentContext getExpContext() {
        return exp;
    }
    

    public String getChipType() {
        return exp.getChipType();
    }
    public boolean is314() {
        return exp.is314();
    }
    
    public boolean is316() {
        return exp.is316();
    }
    public boolean isChipBB() {
        return exp.isChipBB();
    }
    public boolean is318() {
        return exp.is318();
    }
   
    public int getApproxSizeInMB() {
        return (int) (getNrCols() * getNrRows() / 2500000.0 * getNrFlwos());
    }

    public int esimateSecs() {
        return Math.max(60, getApproxSizeInMB() / 6);
    }

    private void openWellsFile() {
        if (wellsfile == null || !wellsfile.exists() || !wellsfile.canRead()) {
            p("Wells File " + wellsfile + " does not exist or is not readable");
        } else {
            try {
                WellsReader reader = new WellsReader(getWellsfile(), getMask());
                if (reader.getHeader() != null) {
                    flowSequence = reader.getHeader().getFlowSequence();
                    nrflows = reader.getHeader().getNrFlows();
                    reader.close();
                }
            }
            catch (Exception e) {
                err("Could not read 1.wells file "+wellsfile);
            }
        }
    }

    public String getRawDirectory() {
        return exp.getRawDir();
    }

//    public WellContext createContext(String raw_dir, String res_dir) {
//        res_dir = FileTools.addSlashOrBackslash(res_dir);
//        BfMaskReader mr = new BfMaskReader(res_dir+"bfmask.bin");
//        mr.readFile();
//        mask = mr.getMask();
//        return new WellContext(raw_dir, res_dir, mask);
//    }
    public String getCacheDirectory() {
        return exp.getCacheDir();
    }

    public String getResultsDirectory() {
        return exp.getResultsDirectory();
    }

    public String toString() {
        return "WellContext:\nexp: " + exp.toString()+ "\nwellsfile=" + wellsfile + "\nnrflows=" + nrflows + "\nselection=" + selection + "\ncoordinate=" + coordinate;
    }

    private void clearCache() {
        selection.clearCache();
        datacache.clear();
    }

    public ArrayList<WellCoordinate> getAllFilteredWells() {
        if (selection == null) {
            selection = new WellSelection(this.coordinate, this.coordinate);            
        }
        if (getMask() == null) {
            ArrayList<WellCoordinate> res= new ArrayList<WellCoordinate>();
            res.add(coordinate);
            return res;
        }
        return selection.getAllFilteredWells(getMask());

    }

    public WellData getWellData(WellCoordinate coord) {
        return getWellData(coord.getCol(), coord.getRow());
    }

    public void loadMaskData(WellCoordinate coord) {
        if (getMask() == null) {
            err("Got no mask");
        } else if (coord == null) {
            err("Coord is null");
        } else {
            coord.setMaskdata(mask.getDataPointAt(coord.getCol(), coord.getRow()));
        }
    }

    public WellData getWellData(int c, int r) {
         if (c < 0 || r < 0) {
            err("readWell: Negative coords, returning null.");
            return null;
        }
        String key = c + ":" + r;
        WellData welldata = datacache.get(key);
        if (welldata != null) {
            return welldata;
        }

        if (getWellsfile() == null || !getWellsfile().exists()) {
            err("Wells file " + getWellsfile() + " does not exist (yet)");
            return null;

        }
         p("Getting well data at "+key);
        WellsReader reader = new WellsReader(getWellsfile(), getMask());
        
        if (c - exp.getColOffset()>=0) c = c - exp.getColOffset();
        if (r - exp.getRowOffset()>=0) r = r - exp.getRowOffset();
         
        welldata = reader.readWell(c, r);
        if (datacache.size()>100) datacache.clear();
        datacache.put(key, welldata);
        if (welldata == null || welldata.getSequence() == null) {
            return null;
        }
        return welldata;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(WellContext.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(WellContext.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(WellContext.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("WellContext: " + msg);
        //Logger.getLogger( WellContext.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the selection
     */
    public WellSelection getSelection() {
        return selection;
    }

    
    public int getNrWells() {
        if (selection == null) return 0;    
        return selection.getAllWells().size();
    }
    /**
     * @param selection the selection to set
     */
    public void setSelection(WellSelection sel) {
        ArrayList<WellFilter> filters = null;
        if (selection != null) filters = selection.getFilters();
        this.selection = sel;
        if (selection != null) selection.setFilters(filters);
        clearCache();
    }

    /**
     * @return the coordinate
     */
    public WellCoordinate getCoordinate() {
        return coordinate;
    }

    public WellCoordinate getAbsoluteCoordinate() {
        if (coordinate == null) {
            if (this.getSelection() != null) {
                p("Using coor from selection");
                coordinate = this.getSelection().getCoord1();
            }
            if (coordinate == null) {
                p("Got no coordinate and no selection, using 0,0");
                coordinate = new WellCoordinate(0,0);
            }
        }
        //if (coordinate == null) return null;
        int x = coordinate.getCol()+exp.getColOffset();
        int y = coordinate.getRow()+exp.getRowOffset();
        return new WellCoordinate(x, y);
    }
    /**
     * @param coordinate the coordinate to set
     */
    public void setCoordinate(WellCoordinate coordinate) {
        this.coordinate = coordinate;
        if (selection == null) {
            selection = new WellSelection(coordinate, coordinate);
        }
    }

    /**
     * @return the mask
     */
    public BfMask getMask() {
        if (mask == null) {
            String maskfile = exp.getResultsDirectory() + "bfmask.bin";
            if (!FileUtils.exists(maskfile)) {
                p("getMask: file "+maskfile +" not found ");
            }
            else {
                BfMaskReader mr = new BfMaskReader(maskfile);
                mr.readFile();
                mask = mr.getMask();
            }
        }
        return mask;
    }

    public int getNrCols() {
        if (getMask() != null) return mask.getNrCols();
        else if (exp != null) return exp.getNrcols();
        else return 0;
    }

    public int getNrRows() {
         if (getMask() != null)return mask.getNrRows();
         else if (exp != null) return exp.getNrrows();
         else return 0;
    }

    /**
     * @return the wellsfile
     */
    public File getWellsfile() {
        return wellsfile;
    }

//    /**
//     * @param nonEmptyWells the nonEmptyWells to set
//     */
//    public void setNonEmptyWells(ArrayList<WellCoordinate> nonEmptyWells) {
//        this.nonEmptyWells = nonEmptyWells;
//    }
    /** returns base callf or this flow nr */
    public char getBase(int flow) {
        if (getFlowSequence() == null) {
            return '?';
        } else {
            return getFlowSequence().charAt(flow);
        }
    }

    public int getNrFlwos() {
        if (nrflows <= 0) {
            openWellsFile();
        }
        return nrflows;
    }

    /**
     * @return the flowSequence
     */
    public String getFlowSequence() {
        if (flowSequence == null) {
            openWellsFile();
        }
        return flowSequence;
    }

    /**
     * @return the filters
     */
    public ArrayList<WellFilter> getFilters() {
        return selection.getFilters();
    }

    /**
     * @param filters the filters to set
     */
    public void setFilters(ArrayList<WellFilter> filters) {
        if (selection != null) selection.setFilters(filters);
    }

    public void setAllWells(ArrayList<WellCoordinate> result) {
        selection.setAllWells(result);
    }

    public void clear() {
       this.datacache = null;
       this.mask = null;
       this.selection = null;
       this.flowSequence = null;
       
    }
}
