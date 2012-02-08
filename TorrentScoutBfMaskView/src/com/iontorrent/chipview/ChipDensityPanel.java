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
package com.iontorrent.chipview;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.Settings;
import com.iontorrent.guiutils.wells.GeneralDensityPanel;
import com.iontorrent.guiutils.wells.WellsImagePanel.WellModel;
import com.iontorrent.rawdataaccess.wells.BfMaskFlag;

import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.wellmodel.BfHeatMap;
import com.iontorrent.wellmodel.ChipWellDensity;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellSelection;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import org.openide.util.Exceptions;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Chantal Roth
 */
public class ChipDensityPanel extends GeneralDensityPanel implements WellModel {

    // private ScoreWellDensity wellDensity;
    /** the offscreen image to which the density plot is drawn */
    /** Which flag to use to draw, example LIVE or EMPTY or DUD etc */
    private BfMaskFlag scoremaskflag;
    private BfHeatMap mask;
    private String info;
    private String fileOrUrl;
    private transient final InstanceContent wellSelectionContent = LookupUtils.getPublisher(WellSelection.class);
    private transient final InstanceContent wellCoordContent = LookupUtils.getPublisher(WellCoordinate.class);
    private int MAX_COORDS = 10000;
    private int frame;
    int min;
    int max;

    public ChipDensityPanel(ExperimentContext exp) {
        super(exp);
        scoremaskflag = BfMaskFlag.RAW;
       // this.expcontext = exp;
        //this.setSendEventOnClick(false);
        setBorder(30);
        this.setNrWidgets(3);
    }

   
    @Override
    public String getToolTipText(MouseEvent e) {
        WellCoordinate coord = imagePanel.getCoord(e);
        int col = coord.getX();
        int row = coord.getY();
        // also get value
        return "<html>col=" + (col+this.expcontext.getColOffset()) + ",row=" + (row +this.expcontext.getRowOffset())+ "<br>value=" + imagePanel.getValue(col, row) + "</html>";
    }

    @Override
    public double getValue(int col, int row) {
        int p = mask.getDataPointAt(scoremaskflag, col, row, false);
        return p;
    }

    @Override
    protected int getCount(int c, int r) {
        return wellDensity.getCount(c, r);
    }

    @Override
    protected ArrayList<WellCoordinate> getCoords(WellSelection sel) {
        if (mask == null) {
            return null;
        }
        ArrayList<WellCoordinate> coords = mask.getAllCoordsWithData(scoremaskflag, MAX_COORDS,
                sel.getCoord1().getCol(), sel.getCoord1().getRow(), sel.getCoord2().getCol(), sel.getCoord2().getRow());
        return coords;
    }

    public void setScoreMask(BfHeatMap mask, int bucketsize, int flow, RawType type, int frame) {
      //  this.setCoordscale(4);
        //  context = mask.getWellContext();
        //    this.expcontext = expcontext;
        mask.updateInfo();
        this.setCoordscale(mask.GRID);
        this.mask = mask;
        this.frame = frame;
        if (mask == null) {
            p("Got no bfmaskflag or mask");
            return; 
        }    
        this.info = expcontext.getRawDir()+type.getFilename()+", flow "+flow+" frame "+frame;
        wellDensity = new ChipWellDensity (getExp(), flow, type, frame);
       
        ((ChipWellDensity) wellDensity).setMask(mask);

        wellDensity.setFlag(BfMaskFlag.RAW);
        //   }

        try {
            createAndDrawImage();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected int getMax() {
        if (max != 0) {
            return max;
        }
       // int size = wellDensity.getBucketSize()*wellDensity.getBucketSize();
        int maxcount = Math.max(1, wellDensity.getMax());
        if (frame > 0)  maxcount = Math.min(maxcount, 500);
        else  maxcount = Math.min(maxcount, Settings.PIN_MAX);
        return  Math.max(0, maxcount);
    }
    @Override
    protected int getMin() {
        if (min != 0) {
            return min;
        }
        int mincount = wellDensity.getMin();
        mincount = Math.max(mincount, Settings.PIN_MIN);
        return mincount;
    }

    public String getFile() {
        return fileOrUrl;
    }

    public void setFlag(BfMaskFlag scoremaskflag) {
        this.scoremaskflag = scoremaskflag;

        if (fileOrUrl == null || scoremaskflag == null) {
            return;
        }
        wellDensity.setFlag(scoremaskflag);

        try {
            createAndDrawImage();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected void drawCoords(Graphics2D g, int cols, int maxy, int rows, int maxx) {
        super.drawCoords(g, cols, maxy, rows, maxx, Color.gray);

        // also draw current block;
        g.setColor(Color.white);
        g.drawString(info, super.BORDER, 15);
    }

//    @Override
//    protected void publishSelection(WellSelection sel) {
//        if (sel != null) {
//            p("publishSelection: Got a selection: " + sel);//           
//            imagePanel.setWellSelection(sel);
//            LookupUtils.publish(wellSelectionContent, sel);
//        } else {
//            p("Publish selection: got no selection");
//        }
//    }
 @Override
    protected void publishSelection(WellSelection sel) {
        if (sel != null) {
            p("publishSelection: Got a selection: " + sel);
            if (wellcontext == null) {
                wellcontext = this.expcontext.getWellContext();
            }
            if (wellcontext != null) {
                if (wellcontext.getSelection() != null) {
                    wellSelectionContent.remove(wellcontext.getSelection());
                }
                wellcontext.setSelection(sel);
            }

            LookupUtils.publish(wellSelectionContent, sel);
            if (sel.getCoord1().toString().equals(sel.getCoord2().toString())) {
                this.publishCoord(sel.getCoord1());
            }

        } else {
            p("Publish selection: got no selection");
        }
    }
    @Override
    protected void publishCoord(WellCoordinate coord) {
        if (coord != null) {
            p("Got a coordinate: " + coord);
            LookupUtils.publish(wellCoordContent, coord);
        }
    }

    

    @Override
    protected void setMax(double max) {
        this.max = (int) max;
    }

    @Override
    protected void setMin(double min) {
        this.min = (int) min;
    }
}
