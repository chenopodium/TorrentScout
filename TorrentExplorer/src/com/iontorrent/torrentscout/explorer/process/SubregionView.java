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
package com.iontorrent.torrentscout.explorer.process;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.guiutils.ColorGradient;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.guiutils.NavigableImagePanel;
import com.iontorrent.guiutils.heatmap.ColorModel;
import com.iontorrent.guiutils.heatmap.GradientPanel;
import com.iontorrent.rawdataaccess.wells.BitMask;
import com.iontorrent.torrentscout.explorer.ExplorerContext;
import com.iontorrent.torrentscout.explorer.Widget;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.utils.stats.Stats;
import com.iontorrent.wellmodel.RasterData;
import com.iontorrent.wellmodel.WellCoordinate;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Chantal Roth
 */
public class SubregionView extends NavigableImagePanel implements ActionListener, KeyListener {

    private transient final InstanceContent wellCoordContent = LookupUtils.getPublisher(WellCoordinate.class);
    private RasterData data;
    /** size of image in nr of wells per side */
    private int RASTER_SIZE;
    /** the offscreen image to which the density plot is drawn */
    private BufferedImage[] bimages;
    private static int BX = 30;
    private static int BY = 30;
    boolean showignmore;
    boolean showbg;
    boolean showuse;
    private int offx;
    private int offy;
//    private transient final InstanceContent wellContextContent = LookupUtils.getPublisher(WellContext.class);
//    private transient final InstanceContent wellSelectionContent = LookupUtils.getPublisher(WellSelection.class);
    private int IMAGE_SIZE;
    private int flow;
    GradientPanel gradient;
    private WellCoordinate relcoord;
    private WellCoordinate abscoord;
    private WellCoordinate fromCoord;
    private WellCoordinate toCoord;
    private int[] maxcount;
    private int[] mincount;
    private int pixperrow;
    private int pixpercol;
    private int maxx;
    private int maxy;
    //  private int maxForFrame;
    //  private int minForFrame;
    private Color[] gradientColors;
    private ColorModel[] colormodel;
    private Stats stats;
    private boolean log;
    private int frame;
    private ExperimentContext exp;
    protected static ToolTipManager tipmanager = ToolTipManager.sharedInstance();
    private ArrayList<Widget> widgets;
    private CoordWidget curwidget;
    private Color widgetcolors[] = {Color.yellow, Color.cyan, Color.orange, Color.blue, Color.white};
    //private Color widgetcolors[] = {Color.yellow};
    ExplorerContext maincont;
    CoordWidget mainwidget;
    private boolean dominmax[];
    private int nrwidgets;

    public SubregionView(ExplorerContext maincont) {
        super();
              
        gradient = new GradientPanel(null); 
        gradient.setListener(this);
        this.nrwidgets = maincont.getPreferrednrwidgets();
        super.setNavigationImageEnabled(false);
        // super.setDraggingEnabled(false);
        super.setSelectionEnabled(false);
        this.maincont = maincont;
        data = maincont.getData();
        widgets = maincont.getWidgets();

        RASTER_SIZE = data.getRaster_size();
        IMAGE_SIZE = RASTER_SIZE * 4 + BX * 2;
        this.setZoomIncrement(0.25);
        this.frame = maincont.getFrame();
        // this.setBackground(Color.black);
        tipmanager.setDismissDelay(300000);
        tipmanager.setInitialDelay(1);
        this.data = maincont.getData();
        this.relcoord = maincont.getRelativeDataAreaCoord();
        this.abscoord = maincont.getAbsDataAreaCoord();
        this.exp = maincont.getExp();

        this.flow = maincont.getFlow();

        int startc = data.getRelStartcoord().getCol();
        int startr = data.getRelStartcoord().getRow();

        this.setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                p("Requesting focus");
                requestFocus();
            }
        });
        //   p("start row/col: " + startc + "/" + startr);
        fromCoord = new WellCoordinate(startc, startr);
        toCoord = new WellCoordinate(startc + RASTER_SIZE, startr + RASTER_SIZE);
        gradientColors = new Color[]{Color.black, Color.blue, Color.green, Color.yellow, Color.orange, Color.red, Color.white};
        
        dominmax = new boolean[data.getFrames_per_flow()];
        getMinMax(data, 0);
        // super.setDoubleBuffered(true);
        this.addKeyListener(this);

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                Point p = evt.getPoint();
                NavigableImagePanel.Coords coord = panelToImageCoords(p);
                double ex = coord.getDoubleX();
                double ey = coord.getDoubleY();
                ex = ex - BX;
                ey = maxy - ey;
                curwidget = (CoordWidget) Widget.getClosest(ex, ey, widgets);
                //   p("mouse clicked at " + ex + "/" + ey + ", found widget: " + curwidget);

            }

            @Override
            public void mousePressed(MouseEvent evt) {
                Point p = evt.getPoint();
                NavigableImagePanel.Coords coord = panelToImageCoords(p);
                double ex = coord.getDoubleX();
                double ey = coord.getDoubleY();
//                ex = ex - BX;
//                ey = maxy - ey;
                curwidget = (CoordWidget) Widget.getClosest(ex, ey, widgets);
                // p("mouse pressed at " + ex + "/" + ey + ", found  widget: " + curwidget);

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (curwidget != null) {
                        WellCoordinate co = getWell(curwidget);
                        //    p("mouseReleased widget " + curwidget + ", then setting to null");
                        if (curwidget.isMainWidget()) {

                            //      p("Main widget moved, sending coord " + co);
                            publishCoord(co);
                        }
                        curwidget = null;
                        repaint();
                    } else {
                        //   p("Left click mouse release, but no widget");
                    }
                }

            }
        });

        update(true);
        checkWidgets(false);
    }

    public void checkWidgets(boolean snap) {
        this.nrwidgets = maincont.getPreferrednrwidgets();
        addOrRemoveWidgets(nrwidgets);
        updateLocationOfWidgets();
        if (snap) {
            snapWidgets();
        }
    }

    protected BitMask getSnapMask() {
        BitMask mask = null;
        if (this.showignmore) {
            mask = maincont.getIgnoreMask();
        } else if (this.showbg) {
            mask = maincont.getBgMask();
        } else if (this.showuse) {
            mask = maincont.getSignalMask();
        }
        return mask;
    }

    private boolean snapWidgetToMask(CoordWidget w, BitMask mask) {
        WellCoordinate c = w.getCoord();
        int x = c.getX() - offx - data.getRelStartcoord().getCol();
        int y = c.getY() - offy - data.getRelStartcoord().getRow();
        int size = data.getRaster_size();
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                x++;
                if (x >= size) {
                    x = 0;
                    y++;
                    if (y >= size) {
                        y = 0;
                    }
                }
                if (mask.get(x, y)) {
                    c = new WellCoordinate(x + offx + data.getRelStartcoord().getCol(), y + offy + data.getRelStartcoord().getRow());
                    // p("Moving main widget to " + c);
                    w.setCoord(c);
                    maincont.coordChanged(c);
                    Point imagep = getMiddleImageCoord(c);
                    w.setX(imagep.x);
                    w.setY(imagep.y);
                    if (w == mainwidget) {
                        publishCoord(c);
                    }
                    repaint();
                    return true;
                }
            }
            y++;
            if (y >= size) {
                y = 0;
            }
        }
        return false;
    }

    private void snapWidgets() {

        widgets = maincont.getWidgets();
        if (widgets == null) {
            p("got no main widget");
            return;
        }

        BitMask mask = getSnapMask();

        if (mask == null) {
            return;
        }
        p("Snapping widgets to mask " + mask);
        for (Widget w : widgets) {
            snapWidgetToMask((CoordWidget) w, mask);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        setToolTipText(getToolTipText(e));
    }

    public void addOrRemoveWidgets(int nrwidgets) {
        // adding main widget


        for (Widget w : widgets) {
            if (((CoordWidget) w).isMainWidget()) {
                mainwidget = (CoordWidget) w;
            }

        }
        int curnr = widgets.size();
        if (curnr > nrwidgets) {
            for (int i = nrwidgets; i < curnr; i++) {
                widgets.remove(nrwidgets);
            }
            return;
        }
        if (curnr < 1) {
            mainwidget = (CoordWidget) this.addWidget(abscoord, Color.yellow, 0);
            mainwidget.setMainWidget(true);
            curnr++;
        }
        if (this.exp != null) {
            offx = exp.getColOffset();
            offy = exp.getRowOffset();
        }

        for (int i = curnr; i < nrwidgets; i++) {
            Color c = null;
            if (i < widgetcolors.length) {
                c = widgetcolors[i];
            } else {
                c = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
            }
            int x = (int) (Math.random() * RASTER_SIZE + offx + data.getRelStartcoord().getCol());
            int y = (int) (Math.random() * RASTER_SIZE + offy + data.getRelStartcoord().getRow());
            addWidget(new WellCoordinate(x, y), c, i);
        }
        mainwidget = (CoordWidget) widgets.get(0);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        //   p("Mouse dragged");
        if (SwingUtilities.isRightMouseButton(e)) {
            super.mouseDragged(e);
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            if (curwidget != null) {
                Point p = e.getPoint();
                NavigableImagePanel.Coords coord = panelToImageCoords(p);
                double ex = coord.getDoubleX();
                double ey = coord.getDoubleY();
//                ex = ex - BX;
//                ey = maxy - ey;
                //   p("mouseDragged widget " + curwidget);
                curwidget.setX((int) ex);
                curwidget.setY((int) ey);
                getWell(curwidget);
                repaint();
            } else {
                p("Left drag, but no widget");
            }
        } else {
            p("drag, but right mouse");
        }
    }

    public WellCoordinate getWellForPanelCoord(Point p) {
        NavigableImagePanel.Coords coord = panelToImageCoords(p);
        double ex = coord.getDoubleX();
        double ey = coord.getDoubleY();
        ex = ex - BX;
        ey = maxy - ey;
        int wellx = (int) (ex / pixperrow + fromCoord.getX());
        int welly = (int) (ey / pixpercol + fromCoord.getY());
        return new WellCoordinate(wellx, welly);
    }

    public WellCoordinate getWellForImageCoord(Point p) {
        double ex = p.getX();
        double ey = p.getY();
        int wellx = (int) (ex / pixperrow + fromCoord.getX());
        int welly = (int) (ey / pixpercol + fromCoord.getY());
        return new WellCoordinate(wellx, welly);
    }

    public void updateLocationOfWidgets() {
        if (widgets == null) {
            return;
        }
        for (Widget w : widgets) {
            CoordWidget c = (CoordWidget) w;
            Point imagep = getMiddleImageCoord(c.getCoord());
            c.setX(imagep.x);
            c.setY(imagep.y);
        }
    }

    public Widget addWidget(WellCoordinate abscoord, Color color, int nr) {
        Point imagep = getMiddleImageCoord(abscoord);
        //   Coords nav=this.panelToImageCoords(imagep);
        //     p(coord.toString() + "-> image " + imagep);
        CoordWidget w = new CoordWidget(color, imagep.x, imagep.y, nr);
        w.setCoord(abscoord);
        widgets.add(w);
        return w;
    }

    public WellCoordinate getWell(CoordWidget w) {
        if (w == null) {
            return null;
        }
        double ex = w.getX();
        double ey = w.getY();

        ex = ex - BX;
        ey = maxy - ey;

        int x = (int) (ex / pixperrow + fromCoord.getX());
        int y = (int) (ey / pixpercol + fromCoord.getY());
        if (x < 0 || y < 0) {
            return null;
        }
        WellCoordinate c = new WellCoordinate(x + offx, y + offy);
        w.setCoord(c);

        maincont.coordChanged(c);
        return c;
    }

    public void changeFlag(boolean set) {
        if (mainwidget == null) {
            p("got no main widget");
            return;
        }

        BitMask mask = getSnapMask();

        if (mask == null) {
            return;
        }

        WellCoordinate c = mainwidget.getCoord();
        int x = c.getX() - data.getAbsStartCol();
        int y = c.getY() - data.getAbsStartRow();

        p("Changing flag at " + x + "/" + y + " of mask " + mask + " to " + set);
        mask.set(x, y, set);
        maincont.maskChanged(mask);
        //  publishCoord(c);
        // need to redraw images
        this.redrawImages(false);
        //repaint();
        GuiUtils.showNonModalMsg("Changed flag at " + c.getCol() + "/" + c.getRow() + " of mask " + mask + " to " + set);
        //this.redrawImages(false);
    }

    public void findNextFlag() {
        if (mainwidget == null) {
            p("got no main widget");
            return;
        }
        BitMask mask = getSnapMask();

        if (mask == null) {
            return;
        }
        snapWidgetToMask(mainwidget, mask);


    }

    public void moveMainWidget(int dx, int dy) {
        if (mainwidget == null) {
            p("got no main widget");
            return;
        }
        if (dx == 0 && dy == 0) {
            return;
        }

        WellCoordinate c = mainwidget.getCoord();
        int x = c.getX() - offx - data.getRelStartcoord().getCol();
        int y = c.getY() - offy - data.getRelStartcoord().getRow();
        x = x + dx;
        y = y + dy;
        int size = data.getRaster_size() - 1;
        if (x < 0) {
            x = size;
            y--;
        } else if (x > size) {
            x = 0;
            y++;
        } else if (y < 0) {
            y = size;
            x--;
        } else if (y > size) {
            y = 0;
            x++;
        }
        x = Math.max(0, x);
        x = Math.min(size, x);
        c = new WellCoordinate(x + offx + data.getRelStartcoord().getCol(), y + offy + data.getRelStartcoord().getRow());
        //  p("Moving main widget to " + c);
        mainwidget.setCoord(c);
        Point imagep = getMiddleImageCoord(c);
        mainwidget.setX(imagep.x);
        mainwidget.setY(imagep.y);
        maincont.coordChanged(c);
        // maincont.widgetChanged(mainwidget);
        repaint();
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
        float val = data.getValue(x, y, flow, frame);

        String s = "Well "+(x + offx) + "/" + (y + offy) + ":  value=" + val;
        
        //  if (log) s += " "+ getLog(val);
        return s;
    }

    protected void publishCoord(WellCoordinate coord) {
        if (coord != null) {
            this.exp.getWellContext().setCoordinate(coord);
            LookupUtils.publish(wellCoordContent, coord);
        }
    }

    public void getMinMax(RasterData data, int frame) {
        if (mincount == null) {
            mincount = new int[this.data.getFrames_per_flow()];
        }
        if (maxcount == null) {
            maxcount = new int[this.data.getFrames_per_flow()];
        }
        int max = (int) data.getMax(0, frame);
        int min = (int) data.getMin(0, frame);
        setMaxcount(max, frame);
        setMincount(min, frame);
//        if (frame == 0) {
//            setMaxcount(Math.min(1000, max), frame);
//            setMincount(Math.max(-100, min), frame);
//        } else {
//            setMaxcount(Math.min(600, max), frame);
//            setMincount(Math.max(-60, min), frame);
//        }

//        stats = new Stats("Flow stats");
//        stats.setMax(getMaxcount());
//        stats.setMin(getMincount());
        if (getMaxcount(frame) == 0) {
            //   err("Maximum data for frame " + frame + "  is " + getMaxcount(frame) + ". Raster data probably not read correctly.");
            //   p("Data is: " + data.toString());
        }
        // p("max value for " + frame + " =" + getMaxcount(frame));
        //  p("min value for " + frame + " =" + getMincount(frame));

    }

    public int getLog(int val) {
        return (int) (10.0 * Math.log(Math.max(1, val)));
    }

    public int getMincount(int frame) {
        if (frame < mincount.length) {
            return mincount[frame];
        } else {
            return mincount[mincount.length - 1];
        }
    }

    public void redrawImages(boolean dominmax) {
        // save zoom factor

        bimages = null;

        update(dominmax);
    }

    public void redrawImages(boolean showignmore, boolean showbg, boolean showuse, boolean dominmax) {
        bimages = null;
        this.showignmore = showignmore;
        this.showbg = showbg;
        this.showuse = showuse;

        update(dominmax);
    }

    public void update(boolean doallminmax) {
        if (data == null) {
            p("Got no data");
            return;
        }

        //    p("drawing frame " + frame + " onto panel");
        if (bimages == null || doallminmax) {
            // TODO for all flows, not just one
            bimages = new BufferedImage[data.getFrames_per_flow()];

        }
        if (doallminmax) {
            for (int f = 0; f < data.getFrames_per_flow(); f++) {
                dominmax[f] = true;
            }
        }
        if (frame >= bimages.length) {
            // err("Frame " + frame + " out of bounds: " + bimages.length);
            return;
        }

        //   p("update, dominmax: " + doallminmax);
//        if (dominmax) {
//            for (int f = 0; f < data.getFrames_per_flow(); f++) {
//                BufferedImage bimage = createImage();
//                drawDensityOnImage(f, bimage, dominmax);
//                bimages[f] = bimage;
//            }
//        }

        if (bimages[frame] == null || dominmax[frame]) {
            BufferedImage bimage = createImage();

            drawDensityOnImage(frame, bimage, dominmax[frame]);
            bimages[frame] = bimage;
            dominmax[frame] = false;
        }
//        double zoom = this.getZoom();
//        
//        Point zoomcenter = super.getZoomingCenter();
        super.setImage(bimages[frame], false);
//        if (zoom != 0 && zoomcenter != null) {
//            p("Setting zoom to "+zoom);
//            p("Using zoom center: "+zoomcenter);
//            super.setZoom(zoom, zoomcenter);
//            
//        }
//        else p("NOT setting zoom to "+zoom+"/"+zoomcenter);

        this.repaint();
        //   this.requestFocus();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Paints the background

        if (colormodel == null || frame >= colormodel.length || colormodel[frame] == null) return;
        gradient.setColorModel(colormodel[frame]);
        gradient.repaint();
        widgets = maincont.getWidgets();
        if (widgets == null) {
            return;
        }
        for (Widget w : widgets) {
            //   p("Drawing widget: "+w);

            int x = (int) imageToPanelX(w.getX());
            int y = (int) imageToPanelY(w.getY());
            w.paint(g, x, y, getScale());
        }


    }

    public RenderedImage createImage(int frame) {
        if (frame > bimages.length) {
            return null;
        }
        return bimages[frame];
    }

    public int getMaxcount(int frame) {
        if (frame < maxcount.length) {
            return maxcount[frame];
        } else {
            return maxcount[maxcount.length - 1];
        }
    }

    /** buffer only for one flow, so clear all buffers */
    public void setFlow(int flow) {
        bimages = new BufferedImage[data.getFrames_per_flow()];


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

    
    public ColorModel getColorModel(int frame) {
        return colormodel[frame];
    }

    /** Draws the well density plot onto the buffered Image */
    private void drawDensityOnImage(int frame, BufferedImage bimage, boolean dominmax) {
        // Create a graphics context on the buffered image
        if (data == null || bimages == null) {
            return;
        }
        if (dominmax) {
            getMinMax(data, frame);
        }
        int width = bimage.getWidth();
        int cols = RASTER_SIZE;
        int rows = RASTER_SIZE;
        pixpercol = (int) Math.max(1.0, (double) (width - 2 * BX) / (double) cols);
        pixperrow = pixpercol;

        //   p("DRAWING frame " + frame + ": pixpercol=" + pixpercol + ", pixperrow=" + pixperrow);

        //maxForFrame = Math.max(data.getMax(flow - flow, frame), 0);
        // minForFrame = Math.min(data.getMin(flow - flow, frame), 0);
        //   p("Min/max for frame " + frame + ":" + minForFrame + "/" + maxForFrame);
        Graphics2D g = bimage.createGraphics();
        // g.setBackground(Color.black);

        // the area that was used to compute the density = this is the maximum possible value

     //   int delta = Math.max(getMaxcount(frame) - getMincount(frame) + 1, 1);
//        if (delta > 10000) {
//            log = true;
//            delta = getLog(delta);
//            //        p("delta is: " + delta);
//        } else {
//            log = false;
//        }
        log = false;
        if (colormodel == null) colormodel = new ColorModel[data.getFrames_per_flow()];
        
        colormodel[frame] = new ColorModel(gradientColors, getMincount(frame), getMaxcount(frame));
        
        gradient.setColorModel(colormodel[frame]);
        maxy = pixperrow * rows + BY;
        maxx = pixpercol * cols + BX;

        // center += half

        int startc = data.getRelStartcoord().getCol();
        int startr = data.getRelStartcoord().getRow();

        //   p("start row/col: " + startc + "/" + startr);
        fromCoord = new WellCoordinate(startc, startr);
        toCoord = new WellCoordinate(startc + RASTER_SIZE, startr + RASTER_SIZE);

        BitMask ignoremask = maincont.getIgnoreMask();
        BitMask bgmask = maincont.getBgMask();
        BitMask usemask = maincont.getSignalMask();
        for (int c = startc; c < startc + RASTER_SIZE; c++) {
            int x = BX + (c - startc) * pixpercol;
            for (int r = startr; r < startr + RASTER_SIZE; r++) {
                int starty = (r - startr) * pixperrow;
                float count = data.getValue(c, r, flow, frame);
                if (log) {
                    count = getLog((int) count);
                }
               
                Color color = colormodel[frame].getColor(count);

                int y = maxy - starty - pixperrow;
                g.setColor(color);
                g.fillRect(x, y, pixpercol, pixperrow);
                if (this.showignmore && ignoremask != null && ignoremask.get(c - startc, r - startr)) {
                    color = Color.red.darker();
                    g.setColor(color);
                    g.fill3DRect(x, y, pixpercol, pixperrow, true);
                    g.setColor(Color.red.darker());
                }
                if (this.showbg && bgmask != null && bgmask.get(c - startc, r - startr)) {
                    color = Color.BLUE;
                    g.setColor(color);
                    g.fill3DRect(x, y, pixpercol, pixperrow, true);
                    g.setColor(Color.red.darker());
                }
                if (this.showuse && usemask != null && usemask.get(c - startc, r - startr)) {
                    color = Color.green.darker();
                    g.setColor(color);
                    g.fill3DRect(x, y, pixpercol, pixperrow, true);
                    g.setColor(Color.red.darker());
                }
                // if (c == coord.getCol() && r == coord.getRow()) g.setColor(Color.green.darker());
                // else 
                // g.setColor(Color.gray);
                //  g.drawRect(x, maxy - starty - pixperrow, pixpercol, pixperrow);

            }
        }
        drawCoords(g, cols, maxy, rows, maxx);
        int startx = BX + (relcoord.getCol() - startc) * pixpercol;
        int starty = (relcoord.getRow() - startr) * pixperrow;
        g.setColor(Color.green.darker());
        g.setStroke(new BasicStroke(2));
        g.drawRect(startx, maxy - starty - pixperrow, pixpercol, pixperrow);

        // g.setColor(Color.yellow);
        g.setColor(Color.lightGray);


        g.drawString(maincont.getFiletype().getFilename() + " flow " + maincont.getFlow() + ", " + data.getAbsStartCol() + "/" + data.getAbsStartRow() + ", frame " + frame, BX, 15);
        g.dispose();
    }

    public Point getMiddleImageCoord(WellCoordinate abscoord) {
        if (pixperrow < 1) {
            pixpercol = Math.max(1, (int) Math.max(1.0, (double) (getWidth() - 2 * BX) / 100.0));
            pixperrow = pixpercol;
        }

        int c = abscoord.getCol() - offx - data.getRelStartcoord().getCol();

        int r = abscoord.getRow() - offy - data.getRelStartcoord().getRow();
        c = Math.max(0, c);
        r = Math.max(0, r);
        c = Math.min(c, data.getRaster_size() - 1);
        r = Math.min(r, data.getRaster_size() - 1);
        int px = BX + (c) * pixpercol + pixpercol / 2;
        int py = Math.max(BY, (r) * pixperrow + pixperrow / 2);
        return new Point(px, Math.max(BY, maxy - py));
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

        int COORDDELTA = 20;
        //if (cols/BUCKET)
        g.setStroke(new BasicStroke(1));

        offx = 0;
        offy = 0;
        if (this.exp != null) {
            offx = exp.getColOffset();
            offy = exp.getRowOffset();
        }

        //     int coordscale = 1;
        g.setColor(Color.lightGray);
        for (int c = data.getAbsStartCol(); c <= cols + data.getAbsStartCol(); c++) {
            int startx = (int) (BX + ((c - offx - data.getRelStartcoord().getCol()) * pixpercol));
            if (c % COORDDELTA == 0) {
                g.drawLine(startx, BX + (int) pixpercol, startx, maxy + (int) pixpercol);
                int value = c;
                g.drawString("" + value, startx - 3, maxy + 20);
            }
        }
        for (int r = data.getAbsStartRow(); r <= rows + data.getAbsStartRow(); r++) {
            int starty = (int) ((r - offy - data.getRelStartcoord().getRow()) * pixperrow);
            if (r % COORDDELTA == 0) {
                g.drawLine(BX, maxy - starty, maxx, maxy - starty);
                int x = Math.max(1, BX - 10 - (int) (10 * (int) Math.log10(r + 1)));
                x = Math.min(x, BX - 15);
                int value = r;
                g.drawString("" + value, x, maxy - starty + 3);
            }
        }
        //draw any other info


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
        Logger.getLogger(SubregionView.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(SubregionView.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(SubregionView.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("SubregionView: " + msg);
        //Logger.getLogger( SubregionView.class.getName()).log(Level.INFO, msg, ex);
    }

    public Stats getStats() {
        return stats;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    /**
     * @param maxcount the maxcount to set
     */
    public void setMaxcount(int maxcount, int frame) {
        this.maxcount[frame] = maxcount;
    }

    /**
     * @param mincount the mincount to set
     */
    public void setMincount(int mincount, int frame) {
        this.mincount[frame] = mincount;
    }

    public void setGradientPanel(GradientPanel gradient) {
        this.gradient = gradient;
        gradient.setListener(this);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        p("min max changed in gradient panel: " + gradient.getMin() + "/" + gradient.getMax());
        int min = (int) gradient.getMin();
        int max = (int) gradient.getMax();
        if (data != null) {
            p(" Got new min max - setting for ALL frames and redraw images");
            for (int f = 0; f < data.getFrames_per_flow(); f++) {
                setMaxcount(max, f);
                setMincount(min, f);
                dominmax[f] = false;            
            }
            p("Got mins: " + Arrays.toString(this.mincount));
            p("Got max: " + Arrays.toString(this.maxcount));
        }
        redrawImages(false);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        int c = e.getKeyCode();
        p("Got key: " + c + ", left/right etc: " + KeyEvent.VK_LEFT + "/" + KeyEvent.VK_RIGHT + "/" + KeyEvent.VK_UP + "/" + KeyEvent.VK_DOWN + "/" + KeyEvent.VK_DELETE);
        if (c == KeyEvent.VK_LEFT || c == 37) {
            this.moveMainWidget(-1, 0);
        } else if (c == KeyEvent.VK_RIGHT || c == 39) {
            this.moveMainWidget(1, 0);
        } else if (c == KeyEvent.VK_UP || c == KeyEvent.VK_PAGE_UP || c == 38) {
            this.moveMainWidget(0, 1);
        } else if (c == KeyEvent.VK_DOWN || c == KeyEvent.VK_PAGE_DOWN || c == 40) {
            this.moveMainWidget(0, -1);
        } else if (c == KeyEvent.VK_DELETE || c == KeyEvent.VK_BACK_SPACE || c == 127) {
            // delete
            this.changeFlag(false);
        } else if (c == KeyEvent.VK_INSERT || c == 155) {
            // delete
            this.changeFlag(true);
        } else if (c == KeyEvent.VK_TAB || c == 9 || c == KeyEvent.VK_SPACE) {
            // tab: find next coord with flag set
            findNextFlag();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        p("keyPressed");
        keyTyped(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public GradientPanel getGradientPanel() {
        return gradient;
    }
}
