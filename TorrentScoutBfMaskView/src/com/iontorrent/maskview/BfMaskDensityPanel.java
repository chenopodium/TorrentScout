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
package com.iontorrent.maskview;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.wellmodel.WellSelection;
import com.iontorrent.guiutils.wells.GeneralDensityPanel;
import com.iontorrent.guiutils.wells.WellsImagePanel.WellModel;
import com.iontorrent.rawdataaccess.wells.BfMask;
import com.iontorrent.rawdataaccess.wells.BfMaskFlag;
import com.iontorrent.wellmodel.BfWellDensity;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.ArrayList;
import org.openide.util.Exceptions;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Chantal Roth
 */
public class BfMaskDensityPanel extends GeneralDensityPanel implements WellModel {

    //private BfWellDensity wellDensity;
    /** the offscreen image to which the density plot is drawn */
    /** Which flag to use to draw, example LIVE or EMPTY or DUD etc */
    private BfMaskFlag bfmaskflag;
    private BfMask mask;
    private transient final InstanceContent wellSelectionContent = LookupUtils.getPublisher(WellSelection.class);
    private transient final InstanceContent wellCoordContent = LookupUtils.getPublisher(WellCoordinate.class);
    
    private int MAX_COORDS = 10000;

    int min;
    int max;
    
    public BfMaskDensityPanel(ExperimentContext exp) {
        super(exp, 2);
    }

    
    @Override
    public double getValue(int col, int row) {
        int d = mask.getMaskAt(col, row);
        if (d >= 0) {
            if (bfmaskflag.isBitSet(d)) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    public void createDefaultSelection(int col1, int row1, int col2, int row2) {
        if (imagePanel == null) {
            return;
        }
        ArrayList<WellCoordinate> coords = mask.getAllCoordsWithData(this.bfmaskflag, MAX_COORDS, col1, row1, col2, row2);
        WellSelection selection = new WellSelection(col1, row1, col2, row2, coords);
        //   p("Creating default selection");
        imagePanel.setWellSelection(selection);
        publishSelection(selection);
        repaint();
    }

    @Override
    protected int getCount(int c, int r) {

        return wellDensity.getCount(c, r);
    }

    @Override
    protected ArrayList<WellCoordinate> getCoords(WellSelection sel) {
        ArrayList<WellCoordinate> coords = mask.getAllCoordsWithData(bfmaskflag, MAX_COORDS,
                sel.getCoord1().getCol(), sel.getCoord1().getRow(), sel.getCoord2().getCol(), sel.getCoord2().getRow());
        return coords;
    }

   @Override
    protected void drawCoords(Graphics2D g, int cols, int maxy, int rows, int maxx, Color coordcolor) {
        super.drawCoords(g, cols, maxy, rows, maxx, coordcolor);
        if (this.expcontext != null && expcontext.getWellContext() != null && expcontext.getWellContext().getCoordinate()!= null) {
            WellCoordinate rel = new WellCoordinate( expcontext.getWellContext().getCoordinate());
            expcontext.makeRelative(rel);
            showWell(rel.getCol(), rel.getRow(), g, Color.yellow, true);
        }     
    }
    public void setContext(WellContext context, BfMaskFlag bfmaskflag, int bucketsize) {
        this.bfmaskflag = bfmaskflag;
        super.expcontext = context.getExpContext();
        this.wellcontext = context;
        this.mask = context.getMask();
        wellDensity = new BfWellDensity(mask, bucketsize);

        if (bfmaskflag == null) {
            p("Got no bfmaskflag");
            return;

        }
        if (wellDensity == null) {
            return;
        }
        setFlag(bfmaskflag);
    }

   
    public void setFlag(BfMaskFlag bfmaskflag) {
        this.bfmaskflag = bfmaskflag;
        
        if (wellcontext == null || bfmaskflag == null) {
            return;
        }
        wellDensity.setFlag(bfmaskflag);
        try {
            createAndDrawImage();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected void publishSelection(WellSelection sel) {
        if (sel != null) {
            p("publishSelection: Got a selection: " + sel);
            if (wellcontext.getSelection() != null) {
                wellSelectionContent.remove(wellcontext.getSelection());
            }
            wellcontext.setSelection(sel);

            LookupUtils.publish(wellSelectionContent, sel);
            if (wellcontext.getCoordinate()== null || sel.getCoord1().toString().equals(sel.getCoord2().toString())) {
                this.publishCoord(sel.getCoord1());
            }

        } else {
            p("Publish selection: got no selection");
        }
    }

    
    @Override
    protected int getMax() {
       if (max == 0) return BUCKET*BUCKET;
       else return max;
    }
     @Override
    protected int getMin() {
       return min;
    }
    
    @Override
    protected void publishCoord(WellCoordinate coord) {
        if (coord != null) {
            p("Got a coordinate: " + coord);
            if (wellcontext.getCoordinate() != null) {
                wellCoordContent.remove(wellcontext.getSelection());
            }
            wellcontext.setCoordinate(coord);
            LookupUtils.publish(wellCoordContent, coord);

        }
    }

    @Override
    protected void setMax(double max) {
        this.max = (int)max;
    }

    @Override
    protected void setMin(double min) {
        this.min = (int)min;
    }
}
