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
package com.iontorrent.scoreview;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.guiutils.wells.GeneralDensityPanel;
import com.iontorrent.guiutils.wells.WellsImagePanel.WellModel;
import com.iontorrent.rawdataaccess.wells.ScoreMaskFlag;
import com.iontorrent.results.scores.ScoreMask;

import com.iontorrent.utils.LookupUtils;
import com.iontorrent.wellmodel.ScoreWellDensity;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellSelection;
import java.io.IOException;
import java.util.ArrayList;
import org.openide.util.Exceptions;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Chantal Roth
 */
public class ScoreDensityPanel extends GeneralDensityPanel implements WellModel {

    // private ScoreWellDensity wellDensity;
    /** the offscreen image to which the density plot is drawn */
    /** Which flag to use to draw, example LIVE or EMPTY or DUD etc */
    private ScoreMaskFlag scoremaskflag;
    private ScoreMask mask;
    private String fileOrUrl;
    private transient final InstanceContent wellSelectionContent = LookupUtils.getPublisher(WellSelection.class);
    private transient final InstanceContent wellCoordContent = LookupUtils.getPublisher(WellCoordinate.class);
    private int MAX_COORDS = 10000;
  
    int min;
    int max;
    public ScoreDensityPanel( ExperimentContext exp) {
        super(exp);
        this.setNrWidgets(3);
    }

    @Override
    public double getValue(int col, int row) {
        double p = mask.getDataPointAt(scoremaskflag, col, row);
        return p / (double) scoremaskflag.multiplier();
    }

    @Override
    protected int getCount(int c, int r) {
        return wellDensity.getCount(c, r);
    }

    @Override
    protected ArrayList<WellCoordinate> getCoords(WellSelection sel) {
        ArrayList<WellCoordinate> coords = mask.getAllCoordsWithData(scoremaskflag, MAX_COORDS,
                sel.getCoord1().getCol(), sel.getCoord1().getRow(), sel.getCoord2().getCol(), sel.getCoord2().getRow());
        return coords;
    }


    public void setScoreMask(ScoreMask mask, ScoreMaskFlag scoremaskflag, int bucketsize) {
        this.scoremaskflag = scoremaskflag;
        wellcontext = mask.getWellContext();
        super.expcontext = wellcontext.getExpContext();
        //    this.expcontext = expcontext;
        this.mask = mask;
        if (scoremaskflag == null || mask == null) {
            p("Got no scoremaskflag or mask");
            return;

        }
        // if (wellDensity != null) {
        //           wellDensity.setFlag(scoremaskflag);
//            if (!scoremaskflag.isCustom() && mask == wellDensity.getMask() && bucketsize == wellDensity.getBucketSize()) {
//                p("SAME MASK AND SAME BUCKET and NOT custom flag - reusing");
//                wellDensity.update(scoremaskflag);
//            } else {
//                p("new well density object");
//                wellDensity = new ScoreWellDensity(mask, bucketsize, scoremaskflag);
//            }
        //     } else {
        wellDensity = new ScoreWellDensity(mask, bucketsize, scoremaskflag);

        //   }

        try {
            p("Create and draw image");
            createAndDrawImage();
            p("Create and draw image done");
        } catch (IOException ex) {
            p("got an error: "+ex.getMessage());
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected int getMax() {
       if (max != 0) return max;
        int maxcount = Math.max(1, wellDensity.getMax());
        return maxcount;
    }
    @Override
    protected int getMin() {
        if (min != 0) return min;
      return wellDensity.getMin();
    }
 @Override
    protected void setMax(double max) {
        this.max = (int)max;
    }

    @Override
    protected void setMin(double min) {
        this.min = (int)min;
    }
    public String getFile() {
        return fileOrUrl;
    }


    public void setFlag(ScoreMaskFlag scoremaskflag) {
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
    protected void publishSelection(WellSelection sel) {
        if (sel != null) {
            p("publishSelection: Got a selection: " + sel);
            if (wellcontext.getSelection() != null) {
                wellSelectionContent.remove(wellcontext.getSelection());
            }
            wellcontext.setSelection(sel);
            LookupUtils.publish(wellSelectionContent, sel);

        } else {
            p("Publish selection: got no selection");
        }
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
}
