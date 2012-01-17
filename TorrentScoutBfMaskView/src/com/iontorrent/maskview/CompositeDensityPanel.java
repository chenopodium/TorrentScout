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

import com.iontorrent.expmodel.CompositeExperiment;
import com.iontorrent.expmodel.DatBlock;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.guiutils.wells.GeneralDensityPanel;
import com.iontorrent.guiutils.wells.WellsImagePanel.WellModel;
import com.iontorrent.rawdataaccess.wells.BfMaskFlag;
import com.iontorrent.expmodel.ExperimentLoader;

import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.wellmodel.BfHeatMap;
import com.iontorrent.wellmodel.CompositeWellDensity;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellSelection;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.openide.util.Exceptions;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Chantal Roth
 */
public class CompositeDensityPanel extends GeneralDensityPanel implements WellModel {

    // private ScoreWellDensity wellDensity;
    /** the offscreen image to which the density plot is drawn */
    /** Which flag to use to draw, example LIVE or EMPTY or DUD etc */
    private BfMaskFlag scoremaskflag;
    private BfHeatMap mask;
    private String fileOrUrl;
    private transient final InstanceContent wellSelectionContent = LookupUtils.getPublisher(WellSelection.class);
    private transient final InstanceContent wellCoordContent = LookupUtils.getPublisher(WellCoordinate.class);
    private transient final InstanceContent expContent = LookupUtils.getPublisher(ExperimentContext.class);
    private transient final InstanceContent wellContextContent = LookupUtils.getPublisher(WellContext.class);
    private int MAX_COORDS = 10000;
    private CompositeExperiment compexp;
    private Font fblock = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    ExperimentLoader loader;
    private DatBlock curblock;
    int min;
    int max;

    public CompositeDensityPanel(CompositeExperiment exp, ExperimentLoader loader) {
        super(null);
        this.loader = loader;

        this.compexp = exp;
        setBorder(50);
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        WellCoordinate coord = imagePanel.getCoord(e);
        int col = coord.getX();
        int row = coord.getY();
        DatBlock b = compexp.findBlock(coord);
        // also get value
        if (b == null) {
            return "<html>col=" + col + ",row=" + row + "<br>value=" + imagePanel.getValue(col, row) + "</html>";
        } else {
            return "<html>col=" + col + ",row=" + row + "<br>block=" + b.toString() + "<br>dir=" + compexp.getResultsDirectory(b) + ", <br>value=" + imagePanel.getValue(col, row) + "</html>";
        }
        //+" (im: "+coord.x+"/"+coord.y+"), chipy: "+(image.getHeight()-coord.y-BORDER)+
        //" ), bucket: "+bucket_size+", pixpercol: "+pixpercol+" BORDER="+BORDER;

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
        if (sel == null) {
            return null;
        }
        ArrayList<WellCoordinate> coords = mask.getAllCoordsWithData(scoremaskflag, MAX_COORDS,
                sel.getCoord1().getCol(), sel.getCoord1().getRow(), sel.getCoord2().getCol(), sel.getCoord2().getRow());
        return coords;
    }

    public void setScoreMask(BfHeatMap mask, BfMaskFlag scoremaskflag, int bucketsize, RawType filetype, int flow, int frame) {
        this.scoremaskflag = scoremaskflag;
         mask.updateInfo();
        this.setCoordscale(mask.GRID);
        //  context = mask.getWellContext();
        //    this.expcontext = expcontext;
        this.mask = mask;
        if (scoremaskflag == null || mask == null) {
            p("Got no bfmaskflag or mask");
            return;

        }
        //  wellDensity = new CompositeWellDensity(mask, bucketsize, scoremaskflag);
        wellDensity = new CompositeWellDensity(getCompExp(), filetype, flow, frame);
        ((CompositeWellDensity) wellDensity).setMask(mask);
//        if (nrflags == 1) scoremaskflag = BfMaskFlag.RAW;
        wellDensity.setFlag(scoremaskflag);
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
        int maxcount = Math.max(1, wellDensity.getMax());
        
        return maxcount;
    }

     @Override
    protected int getMin() {
        if (min != 0) {
            return min;
        }
        int mincount = wellDensity.getMin();
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
        // super.drawCoords(g, cols, maxy, rows, maxx, Color.gray);
        if (getCompExp().getBlocks() == null || getCompExp().getBlocks().size() < 1) {
            p("NO blocks!");
            return;
        }
        DatBlock block = getCompExp().getBlocks().get(0);

        int blockwidth = block.getWidth();
        int blockheight = block.getHeight();

        g.setStroke(new BasicStroke(3));
        g.setFont(fblock);

        g.setColor(Color.red.darker());
        for (int realc = 0; realc <= getCompExp().getNrcols(); realc += blockwidth) {
            int startx = (int) (realc / getCoordscale() / BUCKET * pixpercol + BORDER);
            //int startx = (int) (BORDER + (c * pixpercol));

            g.drawLine(startx, BORDER + (int) pixpercol, startx, maxy + (int) pixpercol);
            int ziffern = (int) Math.log10(realc + 1);
            int value = realc;
            g.drawString("" + value, startx - fblock.getSize() * ziffern / 2, maxy + fblock.getSize() + 20);

        }
        for (int realr = 0; realr <= getCompExp().getNrrows(); realr += blockheight) {
            int starty = (int) (realr / BUCKET / getCoordscale() * pixperrow);
            g.drawLine(BORDER, maxy - starty, maxx, maxy - starty);
            int ziffern = (int) Math.log10(realr + 1);
            int x = Math.max(1, BORDER - 30 - (int) (fblock.getSize() * ziffern));
            x = Math.min(x, BORDER - 30);
            int value = realr;
            g.drawString("" + value, x, maxy - starty + 3);

        }


    }

//    public void createDefaultSelection(int col1, int row1, int col2, int row2) {
//        if (imagePanel == null) {
//            return;
//        }
//        DatBlock block = compexp.findBlock(col1, row1);
//        if (block == null) {
//            return;
//        }
//        ExperimentContext context = compexp.getContext(block);
//        if (context != null) {
//            publishExpContext(context);
//        }
//    }
//    
    @Override
    protected void publishSelection(WellSelection sel) {
        if (sel != null) {
            p("publishSelection: Got a selection: " + sel);
//            if (wellcontext.getSelection() != null) {
//                wellSelectionContent.remove(wellcontext.getSelection());
//            }
//            wellcontext.setSelection(sel);
//            LookupUtils.publish(wellSelectionContent, sel);
            p("Got sel: " + sel);
            DatBlock block = compexp.findBlock(sel.getCoord1());
            if (block != null) {
                ExperimentContext context = compexp.getContext(block, true);
                curblock = block;
                imagePanel.setWellSelection(new WellSelection(curblock.getStart(), curblock.getEnd()));
                p("got block with raw dir "+context.getRawDir());
                publishExpContext(context, block);
            }

        } else {
            p("Publish selection: got no selection");
        }
    }

    @Override
    protected void publishCoord(WellCoordinate coord) {
        if (coord != null) {
            p("Got a coordinate: " + coord);
            DatBlock block = compexp.findBlock(coord);
            if (block != null) {
                ExperimentContext context = compexp.getContext(block, true);
                 p("got block with raw dir "+context.getRawDir());
                publishExpContext(context, block);
                curblock = block;
                imagePanel.setWellSelection(new WellSelection(curblock.getStart(), curblock.getEnd()));
            }
            //     LookupUtils.publish(wellCoordContent, coord);

        }
    }

    protected void publishExpContext(ExperimentContext exp, DatBlock block) {
        if (exp != null) {
            int ans = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), "<html>Would you like to load block <b>"+block.toShortString()+"</b>?"
                    + "<br>Note you will <b>lose any currently edited masks</b></html>", "Load block", JOptionPane.OK_CANCEL_OPTION);
            if (ans != JOptionPane.OK_OPTION) return;
            
            GuiUtils.showNonModalMsg("Sending ExperimentContext for selected block: " + exp.getResultsDirectory());
            p("Got a ExperimentContext: " + exp);
            GlobalContext.getContext().setExperimentContext(exp, false);
            LookupUtils.publish(expContent, exp);
            WellContext wellcontext = exp.createWellContext();
           
            if (loader != null) {
                loader.maybeLoadExperiment(exp);
            } else {
                JOptionPane.showMessageDialog(this, "Got no expreiment loader, could not load block");
            }
             if (wellcontext != null) {
                wellcontext.setCoordinate(new WellCoordinate(0, 0));
                LookupUtils.publish(wellContextContent, wellcontext);
            } else {
                p("I was unable to crate well context");
            }

        }
    }

    /**
     * @return the exp
     */
    public CompositeExperiment getCompExp() {
        return compexp;
    }

    /**
     * @param exp the exp to set
     */
    public void setCompExp(CompositeExperiment exp) {
        this.compexp = exp;
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
