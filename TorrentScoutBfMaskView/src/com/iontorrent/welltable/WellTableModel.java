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
package com.iontorrent.welltable;

import com.iontorrent.rawdataaccess.wells.BfMaskFlag;
import com.iontorrent.rawdataaccess.wells.ScoreMaskFlag;
import com.iontorrent.results.scores.ScoreMask;

import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Chantal Roth
 */
public class WellTableModel extends DefaultTableModel {

    //   private static final int MAX_ROWS = 500;
    private static String[] HEADER;
    private static final int NRFLAGS = 15;
    private static int NRSCORES = ScoreMaskFlag.getNrFlags();
    public static final int DELTA = 2;
    public static final int FLAGDELTA = 0;
    public static final int SCOREDELTA = NRFLAGS + DELTA;
    WellContext context;
    ArrayList<WellCoordinate> wellcoords;
    ScoreMask mask;

    public WellTableModel(WellContext context, ScoreMask mask) {
        super();
        this.context = context;
        this.mask = mask;
        if (mask == null) {
            NRSCORES = 0;

        } else {
            NRSCORES = ScoreMaskFlag.getNrFlags();
        }
        createHeader();
        this.setColumnIdentifiers(HEADER);
        if (context == null) {
            return;
        }
        wellcoords = context.getAllFilteredWells();
       // p("got " + wellcoords.size() + " wells for selection " + context.getSelection());
    }

    private void createHeader() {
        HEADER = new String[NRFLAGS + DELTA + NRSCORES];
        HEADER[0] = "x";
        HEADER[1] = "y";
        // HEADER[2] = "Avg flow";
        for (int i = 0; i < NRFLAGS; i++) {
            HEADER[i + DELTA] = BfMaskFlag.get(i + FLAGDELTA).getName();
        }
        for (int i = 0; i < NRSCORES; i++) {
            ScoreMaskFlag flag =  ScoreMaskFlag.get(i);
            if (flag != null) HEADER[i + SCOREDELTA] = flag.getName();
        }
     
    }

    @Override
    public int getRowCount() {
        if (wellcoords == null) {
            return 0;
        }
        return wellcoords.size();

    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public Class getColumnClass(int col) {
        // if (col  == 2) return Float.class;
        if (col < 2) {
            return Integer.class;
        } else if (col < SCOREDELTA) {
            return Boolean.class;
        } else {
            return Double.class;
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        WellCoordinate coord = wellcoords.get(row);
        if (context.getExpContext()==null || coord==null) return null;
        if (col == 0) {
            return coord.getCol()+context.getExpContext().getColOffset();
        } else if (col == 1) {
            return coord.getRow()+context.getExpContext().getRowOffset();
        } //        else if (col == 2) {  // info about flow values               
        //                WellData data = context.getWellData(coord);
        //                if (data != null) {
        //                    float average = data.getAverageFlowValue();
        //                    return new Float(average);
        //                }
        //                else return new Float(-1);
        //        }
        else if (col < SCOREDELTA) {
            int code = col - DELTA + FLAGDELTA;
            Boolean b = new Boolean(false);
            if (coord!= null && coord.getMaskdata() != null) {
                b= new Boolean(coord.getMaskdata().hasFlag(BfMaskFlag.get(code)));
            }
            return b;
        } else { // col > scoredelta          
            int code = col - SCOREDELTA;
            double d = coord.getScoredata(code);
            return d / (double)ScoreMaskFlag.get(code).multiplier();
            
        }
    }

    public WellCoordinate getWellCoordinate(int row) {
        if (wellcoords == null || row < 0 || row >= wellcoords.size()) {
            err("Row " + row + " out of bounds");
            return null;
        }
        return wellcoords.get(row);
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(WellTableModel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(WellTableModel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(WellTableModel.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("WellTableModel: " + msg);
        //Logger.getLogger( WellTableModel.class.getName()).log(Level.INFO, msg, ex);
    }

    public int findRow(WellCoordinate coord) {
        for (int r = 0; r < this.wellcoords.size(); r++) {
            WellCoordinate c = wellcoords.get(r);
            if (c.equals(coord)) {
                return r;
            }
        }
        return -1;
    }
}
