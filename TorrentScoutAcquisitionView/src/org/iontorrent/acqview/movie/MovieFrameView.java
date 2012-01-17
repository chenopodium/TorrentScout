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
package org.iontorrent.acqview.movie;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.guiutils.ColorGradient;
import com.iontorrent.guiutils.NavigableImagePanel;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.utils.stats.Stats;
import com.iontorrent.wellmodel.RasterData;
import com.iontorrent.wellmodel.WellCoordinate;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ToolTipManager;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Chantal Roth
 */
public class MovieFrameView extends NavigableImagePanel {

    private transient final InstanceContent wellCoordContent = LookupUtils.getPublisher(WellCoordinate.class);
    private RasterData data;
    private MovieState state;
    /** size of image in nr of wells per side */
    private int NR_WELLS;
    /** the offscreen image to which the density plot is drawn */
    private BufferedImage[] bimages;
    private static int BX = 30;
    private static int BY = 30;
    int offx;
    int offy;
//    private transient final InstanceContent wellContextContent = LookupUtils.getPublisher(WellContext.class);
//    private transient final InstanceContent wellSelectionContent = LookupUtils.getPublisher(WellSelection.class);
    private int IMAGE_SIZE;
    private int flow;
    private WellCoordinate coord;
    private WellCoordinate fromCoord;
    private WellCoordinate toCoord;
    private int maxcount;
    private int mincount;
    int pixperrow;
    int pixpercol;
    int maxx;
    int maxy;
    private int maxForFrame;
    private int minForFrame;
    Color[] gradientColors;
    private Stats stats;
    boolean log;
    ExperimentContext exp;
    protected static ToolTipManager tipmanager = ToolTipManager.sharedInstance();

    public MovieFrameView(ExperimentContext exp, RasterData data, MovieState state, int flow, WellCoordinate coord) {
        super();
        super.setNavigationImageEnabled(false);

        NR_WELLS = data.getRaster_size();
        IMAGE_SIZE = NR_WELLS * 4 + BX * 2;
        this.setZoomIncrement(0.25);

        // this.setBackground(Color.black);
        tipmanager.setDismissDelay(300000);
        tipmanager.setInitialDelay(1);
        this.data = data;
        this.coord = coord;
        this.exp = exp;
        this.state = state;
        this.flow = flow;

        gradientColors = new Color[]{Color.black, Color.blue, Color.green, Color.yellow, Color.orange, Color.red, Color.white};
        getMinMax(data);
        // super.setDoubleBuffered(true);

        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                setToolTipText(getToolTipText(e));
            }
        });

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                Point p = evt.getPoint();
                NavigableImagePanel.Coords coord = panelToImageCoords(p);
                double ex = coord.getDoubleX();
                double ey = coord.getDoubleY();

                ex = ex - BX;
                ey = maxy - ey;
                int x = (int) (ex / pixperrow + fromCoord.getX());
                int y = (int) (ey / pixpercol + fromCoord.getY());
                if (x < 0 || y < 0) {
                    return;
                }
                p("mouse clicked at " + x + "/" + y + ", with offset: " + (x + offx) + "?" + (y + offy) + ",  fromcoord=" + fromCoord);

                publishCoord(new WellCoordinate(x, y));
            }
        });
        update();
    }

    @Override
    public String getToolTipText(MouseEvent evt) {
        //  String res = "";
        Point p = evt.getPoint();
        NavigableImagePanel.Coords coord = panelToImageCoords(p);
        double ex = coord.getDoubleX();
        double ey = coord.getDoubleY();

        ex = ex - BX;
        ey = maxy - ey;
        int x = (int) (ex / pixperrow + fromCoord.getX());
        int y = (int) (ey / pixpercol + fromCoord.getY());
        if (x < 0 || y < 0) {
            return null;
        }
        float val = data.getValue(x, y, flow, state.getFrame());
        String s = (x + offx) + "/" + (y + offy) + ":" + val;
        //  if (log) s += " "+ getLog(val);
        return s;
    }

    protected void publishCoord(WellCoordinate coord) {
        if (coord != null) {
            this.exp.getWellContext().setCoordinate(coord);
            LookupUtils.publish(wellCoordContent, coord);
        }
    }

    private void getMinMax(RasterData data) {
        setMaxcount(Math.min((int)data.getMax(), 256000));
        setMincount(Math.min((int)data.getMin(), 0));
        stats = new Stats("Flow stats");
        stats.setMax(getMaxcount());
        stats.setMin(getMincount());
        if (getMaxcount() == 0) {
         //   err("Maximum data is " + getMaxcount() + ". Raster data probably not read correctly.");
          //  p("Data is: " + data.toString());
        }
        p("max value =" + getMaxcount());
        p("min value =" + getMincount());

    }

    public int getLog(int val) {
        return (int) (10.0 * Math.log(Math.max(1, val)));
    }

    public int getMincount() {
        return mincount;
    }

    public void redrawImages() {
        bimages = null;
        update();
    }

    public void update() {
        if (data == null) {
            p("Got no data");
            return;
        }

        p("drawing frame " + state.getFrame() + " onto panel");
        if (bimages == null) {
            // TODO for all flows, not just one
            bimages = new BufferedImage[data.getFrames_per_flow()];
        }

        int frame = state.getFrame();
        if (frame >= bimages.length) {
            err("Frame " + frame + " out of bounds: " + bimages.length);
            return;
        }

        if (bimages[frame] == null) {
            BufferedImage bimage = createImage();
            p("Created image");
            drawDensityOnImage(frame, bimage);
            bimages[frame] = bimage;
        }
        super.setImage(bimages[frame]);
        //g.clearRect(0, 0, IMAGE_SIZE, IMAGE_SIZE);
        //g.drawImage(bimages[frame], 0, 0, IMAGE_SIZE, IMAGE_SIZE, this);
        //   this.invalidate();
        // this.paintComponents(getGraphics());
        this.repaint();
    }

    public RenderedImage createImage(int frame) {
        if (frame > bimages.length) {
            return null;
        }
        return bimages[frame];
    }

    public int getMaxcount() {
        return maxcount;
    }

    /** buffer only for one flow, so clear all buffers */
    public void setFlow(int flow) {
        bimages = new BufferedImage[data.getFrames_per_flow()];
        state.stop();

    }

    public WellCoordinate getFrom() {
        return fromCoord;
    }

    public WellCoordinate getTo() {
        return toCoord;
    }

    private BufferedImage createImage() {
        int width = IMAGE_SIZE;
        int height = IMAGE_SIZE;

        if (width < 1 || height < 1) {
            err("Width/height of this panel odd. Will use a minimum value of 100");
            width = 100;
            height = 100;
        }
        // Create an image that does not support transparency
        BufferedImage bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        return bimage;
    }

    public Color[] getColors() {
        return gradientColors;
    }

    /** Draws the well density plot onto the buffered Image */
    private void drawDensityOnImage(int frame, BufferedImage bimage) {
        // Create a graphics context on the buffered image
        if (data == null || bimages == null) {
            return;
        }
        int width = bimage.getWidth();
        int cols = NR_WELLS;
        int rows = NR_WELLS;
        pixpercol = (int) Math.max(1.0, (double) (width - 2 * BX) / (double) cols);
        pixperrow = pixpercol;

        p("DRAWING frame " + frame + ": pixpercol=" + pixpercol + ", pixperrow=" + pixperrow);

        maxForFrame = Math.max((int)data.getMax(flow - flow, frame), 0);
        minForFrame = Math.min((int)data.getMin(flow - flow, frame), 0);
        p("Min/max for frame " + frame + ":" + minForFrame + "/" + maxForFrame);
        Graphics2D g = bimage.createGraphics();
        // g.setBackground(Color.black);

        // the area that was used to compute the density = this is the maximum possible value

        int delta = getMaxcount() - getMincount() + 1;
        if (delta > 10000) {
            log = true;
            delta = getLog(delta);
            p("delta is: " + delta);
        } else {
            log = false;
        }
        Color[] customGradient = ColorGradient.createMultiGradient(gradientColors, delta);
        maxy = pixperrow * rows + BY;
        maxx = pixpercol * cols + BX;


        int colinraster = coord.getCol() - data.getRelStartcoord().getCol();
        int rowinraster = coord.getRow() - data.getRelStartcoord().getRow();
        // center += half
        int startc = data.getRelStartcoord().getCol();
        int startr = data.getRelStartcoord().getRow();

        p("start row/col: " + startc + "/" + startr);
        fromCoord = new WellCoordinate(startc, startr);
        toCoord = new WellCoordinate(startc + NR_WELLS, startr + NR_WELLS);

        for (int c = startc; c < startc + NR_WELLS; c++) {
            int startx = BX + (c - startc) * pixpercol;
            for (int r = startr; r < startr + NR_WELLS; r++) {
                int starty = (r - startr) * pixperrow;
                float count = data.getValue(c, r, flow, frame) + getMincount();
                if (log) {
                    count = getLog((int)count);
                }
                count = Math.min(count, customGradient.length - 1);
                count = Math.max(count, 0);
                Color color = customGradient[(int)count];

                g.setColor(color);
                g.fillRect(startx, maxy - starty - pixperrow, pixpercol, pixperrow);
                // if (c == coord.getCol() && r == coord.getRow()) g.setColor(Color.green.darker());
                // else 
                g.setColor(Color.gray);
                g.drawRect(startx, maxy - starty - pixperrow, pixpercol, pixperrow);

            }
        }
        drawCoords(g, cols, maxy, rows, maxx);
        int startx = BX + (coord.getCol() - startc) * pixpercol;
        int starty = (coord.getRow() - startr) * pixperrow;
        g.setColor(Color.green.darker());
        g.setStroke(new BasicStroke(2));
        g.drawRect(startx, maxy - starty - pixperrow, pixpercol, pixperrow);

        g.setColor(Color.yellow);
        g.drawString("Frame " + frame, BX, 10);
        g.dispose();
    }

    public WellCoordinate getCoord(Point p) {

        // NavigableImagePanel.Coords coord = panelToImageCoords(p);
        double x = p.getX();
        double y = p.getY();
        p("Got x/y " + x + "/" + y + ", BORDER=" + BX + ", image.height=" + image.getHeight() + ", pixperrow=" + pixperrow);
        x = x - BX;
        y = maxy - y;
        int col = (int) (x / pixperrow + fromCoord.getX());
        int row = (int) (y / pixpercol + fromCoord.getY());
        return new WellCoordinate(col, row);
    }

    public boolean isLog() {
        return log;
    }

    protected void drawCoords(Graphics2D g, int cols, int maxy, int rows, int maxx) {

        int COORDDELTA = 10;
        //if (cols/BUCKET)
        g.setStroke(new BasicStroke(2));

        offx = 0;
        offy = 0;
        if (this.exp != null) {
            offx = exp.getColOffset();
            offy = exp.getRowOffset();
        }

        //     int coordscale = 1;
        g.setColor(Color.lightGray);
        for (int c = offx + data.getRelStartcoord().getCol(); c <= offx + cols + data.getRelStartcoord().getCol(); c++) {
            int startx = (int) (BX + ((c - offx - data.getRelStartcoord().getCol()) * pixpercol));
            if (c % COORDDELTA == 0) {
                g.drawLine(startx, BX + (int) pixpercol, startx, maxy + (int) pixpercol);
                int value = c;
                g.drawString("" + value, startx - 3, maxy + 20);
            }
        }
        for (int r = offy + data.getRelStartcoord().getRow(); r <= offy + rows + data.getRelStartcoord().getRow(); r++) {
            int starty = (int) ((r - offy - data.getRelStartcoord().getRow()) * pixperrow);
            if (r % COORDDELTA == 0) {
                g.drawLine(BX, maxy - starty, maxx, maxy - starty);
                int x = Math.max(1, BX - 10 - (int) (10 * (int) Math.log10(r + 1)));
                x = Math.min(x, BX - 15);
                int value = r;
                g.drawString("" + value, x, maxy - starty + 3);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(IMAGE_SIZE, IMAGE_SIZE);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(IMAGE_SIZE, IMAGE_SIZE);
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(MovieFrameView.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(MovieFrameView.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(MovieFrameView.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("MovieFrameView: " + msg);
        //Logger.getLogger( MovieFrameView.class.getName()).log(Level.INFO, msg, ex);
    }

    public Stats getStats() {
        return stats;
    }

    public void setFrame(int frame) {
        this.state.setFrame(frame);
    }

    /**
     * @param maxcount the maxcount to set
     */
    public void setMaxcount(int maxcount) {
        this.maxcount = maxcount;
    }

    /**
     * @param mincount the mincount to set
     */
    public void setMincount(int mincount) {
        this.mincount = mincount;
    }
}
