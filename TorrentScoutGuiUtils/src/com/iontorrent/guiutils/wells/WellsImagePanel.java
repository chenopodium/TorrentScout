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
package com.iontorrent.guiutils.wells;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.wellmodel.WellSelection;
import com.iontorrent.guiutils.NavigableImagePanel;
import com.iontorrent.guiutils.widgets.CoordWidget;
import com.iontorrent.guiutils.widgets.Widget;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.wellmodel.WellCoordinate;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Chantal Roth
 */
public class WellsImagePanel extends NavigableImagePanel {

    private WellSelection wellselection;
    double pixpercol;
    double pixperrow;
    int lastx = -1;
    int lasty = -1;
    boolean dragging = false;
    private int coordscale = 1;
    int bucket_size;
    private Color widgetcolors[] = {Color.yellow, Color.cyan, Color.cyan, Color.orange, Color.blue, Color.white};
    private WellModel model;
    int BORDER;
    protected static ToolTipManager tipmanager = ToolTipManager.sharedInstance();
    private WellCoordinate wellcoord;
    private String title;
    private transient final Lookup.Result<WellSelection> selectionSelection =
            LookupUtils.getSubscriber(WellSelection.class, new WellSelectionListener());
    private ExperimentContext exp;
    private int areaOffsetX;
    private int areaOffsetY;
    private ArrayList<Widget> widgets;
    private CoordWidget curwidget;
    CoordWidget mainwidget;
    CoordWidget corner1;
    CoordWidget corner2;
    private int nrwidgets;

    public WellsImagePanel(final ExperimentContext exp, final int BORDER, final BufferedImage image, final double pixpercol, final double pixperrow, final int bucket_size, WellModel model, int nrwidgets) throws IOException {
        super(image);
        this.nrwidgets = nrwidgets;
        if (this.nrwidgets == 0) {
            this.nrwidgets = 3;
        }
        p("Nr widgets: "+nrwidgets);
        // this.setSelectionSize(100/bucket_size);
        this.BORDER = BORDER;
        this.exp = exp;
        super.setNavigationImageEnabled(true);

        this.model = model;
        this.pixpercol = pixpercol;
        this.pixperrow = pixperrow;
        this.setZoomIncrement(0.25);
        this.bucket_size = bucket_size;
        // this.setBackground(Color.black);
        tipmanager.setDismissDelay(300000);
        tipmanager.setInitialDelay(1);

        if (nrwidgets >0) createWidgets(this.nrwidgets);
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                Point p = evt.getPoint();
                if (isInNavigationImage(p)) {
                    return;
                }
                NavigableImagePanel.Coords coord = panelToImageCoords(p);
                double ex = coord.getDoubleX();
                double ey = coord.getDoubleY();

                curwidget = (CoordWidget) Widget.getClosest(ex, ey, widgets);
                p("mouse clicked at " + ex + "/" + ey + ", found widget: " + curwidget);

                if (mainwidget == null) {
                    // there are no widgets! so we have to ALWAYS select
                    p("no widgets - sending well selection");
                    WellCoordinate well = getCoord(evt);
                    wellselection = new WellSelection(well, well);
                    if (exp != null) {
                        wellselection.setOffx(exp.getColOffset());
                        wellselection.setOffy(exp.getRowOffset());
                    }
                }
                else if (curwidget != null) {
                    curwidget.setSelected(true);
                    if (evt.getClickCount() > 1) {
                        widgetClicked();
                    } else {
                        wellselection = null;
                        wellcoord = null;
                    }
                } else {
                    Rectangle rect = createRect();
                    //  p("Checking if " + rect + " contains " + coord);
                    if (rect != null && rect.contains(new Point((int) ex, (int) ey))) {

                        if (evt.getClickCount() > 1) {
                            widgetClicked();
                        }
                    } else {
                        setToolTipText(getToolTipText(evt));
                        GuiUtils.showNonModelMsg("Widget", "Use the widgets to select a well or region, or drag the rectangle");
                    }
                }

            }
            
            protected void widgetClicked() {
                p("DOUBLE click on widget or rect, sending event");
                if (curwidget == mainwidget) {
                    p("creating new well coord");
                    wellcoord = mainwidget.getAbsoluteCoord();
                    //                p("Image mouse clicked, got coord: " + wellcoord);
                } else {
                    p("Sending well selection");
                    wellselection = new WellSelection(corner1.getAbsoluteCoord().getX(), corner1.getAbsoluteCoord().getY(),
                            corner2.getAbsoluteCoord().getX(), corner2.getAbsoluteCoord().getY());
                    if (exp != null) {
                        wellselection.setOffx(exp.getColOffset());
                        wellselection.setOffy(exp.getRowOffset());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent evt) {
                Point p = evt.getPoint();
                if (isInNavigationImage(p)) {
                    return;
                }
                if (!SwingUtilities.isLeftMouseButton(evt)) {
                    return;
                }
                NavigableImagePanel.Coords coord = panelToImageCoords(p);
                double ex = coord.getDoubleX();
                double ey = coord.getDoubleY();
                curwidget = (CoordWidget) Widget.getClosest(ex, ey, widgets);
                if (curwidget != null) {
                    curwidget.setSelected(true);
                } else {
                    if (lastx < 0 && lasty < 0) {
                        Rectangle rect = createRect();
                        //  p("Checking if " + rect + " contains " + coord);
                        if (rect != null && rect.contains(new Point((int) ex, (int) ey))) {
                            dragging = true;
                            lastx = (int) ex;
                            lasty = (int) ey;
                            // p("yes, dragging rect");
                        } else {
                            //  p("No, not dragging");
                        }
                    } else {
                        Rectangle rect = createRect();
                        //  p("Checking if " + rect + " contains " + coord);
                        if (rect != null && rect.contains(new Point((int) ex, (int) ey))) {
//
//                            if (evt.getClickCount() > 1) {
//                                widgetClicked();
//                            }
                        } else {
                            setToolTipText(getToolTipText(evt));
                            GuiUtils.showNonModelMsg("Widget", "Use the widgets to select a well or region, or drag the rectangle");
                        }
                    }
                }
                // p("mouse pressed at " + ex + "/" + ey + ", found  widget: " + curwidget);

            }

            protected Rectangle createRect() {
                // first time - check if inside
                if (corner1 == null || corner2 == null) return null;
                int x1 = corner1.getX();
                int x2 = corner2.getX();
                int y1 = corner1.getY();
                int y2 = corner2.getY();
                Rectangle rect = new Rectangle(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
                return rect;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
                lastx = -1;
                lasty = -1;
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (curwidget != null) {
                        WellCoordinate co = getWell(curwidget);
                        //     p("mouseReleased widget " + curwidget + ", then setting to null");
                        if (curwidget.isMainWidget()) {
                            //       p("Main widget moved" + co);
                            // publishCoord(co);
                        }
                        curwidget.setSelected(false);
                        curwidget = null;
                        repaint();
                    } else {
                        // p("Left click mouse release, but no widget");
                    }
                }

            }
        });

//        addMouseListener(new MouseAdapter() {
//
//            @Override
//            public void mouseClicked(MouseEvent e) {
//
//                if (SwingUtilities.isRightMouseButton(e)) {
//                    return;
//                }
//                Point p = e.getPoint();
//                if (isInNavigationImage(p)) {
//                    return;
//                }
//                NavigableImagePanel.Coords coord = panelToImageCoords(p);
//                double x = coord.getDoubleX();
//                double y = coord.getDoubleY();
//
//                int col = imageXToChip(x);
//                int row = imageYToChip(y);
//                wellcoord = new WellCoordinate(col, row);
//                p("Image mouse clicked, got coord: " + wellcoord);
//                p("areaoffx=" + areaOffsetX);
//                if (wellselection == null) {
//                    wellselection = new WellSelection(col, row, col, row);
//                    if (exp != null) {
//                        wellselection.setOffx(exp.getColOffset());
//                        wellselection.setOffy(exp.getRowOffset());
//                    }
//
//                }
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                if (SwingUtilities.isRightMouseButton(e)) {
//                    return;
//                }
//                Selection sel = getSelection();
//                if (sel != null) {
//                    p("Got a selection in IMAGE coords: " + sel + ", pixpercol: " + pixpercol + ", bucket:" + bucket_size);
//                    // compute coords
//
//                    int col1 = imageXToChip(sel.getX1());
//                    int col2 = imageXToChip(sel.getX2());
//                    int row1 = imageYToChip(sel.getY1());
//                    int row2 = imageYToChip(sel.getY2());
//                    p("areaoffx=" + areaOffsetX);
//                    if (col1 < 0 || col1 > image.getWidth() * bucket_size) {
//                        err("Coords col " + col1 + " out of bounds, should be between 0 and " + image.getWidth() * bucket_size);
//                    }
//                    if (row1 < 0 || row1 > image.getHeight() * bucket_size) {
//                        err("Coords row " + row1 + " out of bounds, should be between 0 and " + image.getHeight() * bucket_size);
//                    }
//                    col1 = Math.max(0, col1);
//                    col2 = Math.max(0, col2);
//                    row1 = Math.max(0, row1);
//                    row2 = Math.max(0, row2);
//                    wellselection = new WellSelection(col1, row1, col2, row2);
//                    if (exp != null) {
//                        wellselection.setOffx(exp.getColOffset());
//                        wellselection.setOffy(exp.getRowOffset());
//                    }
//                    wellcoord = null;//new WellCoordinate((col1 + col2) / 2, (row1 + row2) / 2);
//                    //    p("Selected c/r:" + col1 + "/" + row1 + "-" + col2 + "/" + row2);
//
//                }
//
//            }
//        });
    }

    public void moveViewTo(int x, int y) {
        
        
           Point p = new Point(x, y);
           p("Moving view to: "+p);
           displayImageAtImage(p);
      
    }
    @Override
    public void mouseDragged(MouseEvent e) {
        //   p("Mouse dragged");
        if (SwingUtilities.isRightMouseButton(e)) {
            super.mouseDragged(e);
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            Point p = e.getPoint();
            if (curwidget != null) {

                NavigableImagePanel.Coords coord = panelToImageCoords(p);
                double ex = coord.getDoubleX();
                double ey = coord.getDoubleY();
//                ex = ex - BX;
//                ey = maxy - ey;
                //   p("mouseDragged widget " + curwidget);
                curwidget.setX((int) ex);
                curwidget.setY((int) ey);
                getWell(curwidget);
                // checking if corner is too far away
                CoordWidget other = null;
                if (curwidget == corner1) other = corner2;
                else if (curwidget == corner2) other = corner1;
                if (other != null) {
                    WellCoordinate c1 = curwidget.getAbsoluteCoord();
                    WellCoordinate c2 = other.getAbsoluteCoord();
                    int r = c2.getRow();
                    int c = c2.getCol();
                    int dx = c1.getCol() - c;
                    int dy = c1.getRow() - r;
                    boolean changed = false;
                    
                    if (Math.abs(dx) > 200) {
                        if (dx > 0) dx = 200;
                        else dx = -200;
                        c = Math.max(exp.getColOffset(), (c1.getCol()-dx));
                        c = Math.min(c, exp.getColOffset()+exp.getNrcols());
                        changed = true;
                    }
                    if (Math.abs(dy) > 200) {
                        if (dy > 0) dy = 200;
                        else dy = -200;
                        r = Math.max(exp.getRowOffset(), (c1.getRow()-dy));
                        r = Math.min(r, exp.getRowOffset()+exp.getNrrows());
                        changed = true;
                    }
                   // p("dxd/dy="+dx+"/"+dy+" widget coord: "+c1+", other coord is now: "+c2);
                    if (changed) {
                         c2.setCol(c);
                         c2.setRow(r);
                         Point imagep = getMiddleImageCoord(c2);
                         other.setX(imagep.x);
                         other.setY(imagep.y);
                    }
                }
                repaint();
            } else {
                //p("Left drag, but no widget - checking if inside a rectangle");
                if (dragging) {

                    NavigableImagePanel.Coords coord = panelToImageCoords(p);
                    double ex = coord.getDoubleX();
                    double ey = coord.getDoubleY();
                    int dx = -lastx + (int) ex;
                    int dy = -lasty + (int) ey;
                    corner1.moveDx(dx);
                    corner1.moveDy(dy);
                    getWell(corner1);
                    corner2.moveDx(dx);
                    corner2.moveDy(dy);
                    p("Dragging rect by " + dx + "/" + dy);
                    getWell(corner2);
                    repaint();
                    lastx = (int) ex;
                    lasty = (int) ey;
                } else {
                    // p("Not dragging anything");
                }

            }
        } else {
            //  p("drag, but right mouse");
        }
    }

    public void createWidgets(int nrwidgets) {
        // adding main widget
        widgets = new ArrayList<Widget>();
        if (nrwidgets <= 0 ) return;
        WellCoordinate c = null;
        if (exp != null && exp.getWellContext() != null) {
            c = exp.getWellContext().getCoordinate();
        }
        if (c == null) {
            int x = 10;
            int y = 10;
            c = new WellCoordinate(x, y);
        }
        p("Creating main widget. Nr widgets: "+nrwidgets);
        mainwidget = (CoordWidget) this.addWidget(c, Color.yellow, 0);
        mainwidget.setMainWidget(true);

        for (int i = 1; i < nrwidgets; i++) {
            Color color = null;
            if (i < widgetcolors.length) {
                color = widgetcolors[i];
            } else {
                color = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
            }
            c = null;
            if (exp.getWellContext().getSelection() != null) {
                p("Got well selection, using those coordinate");
                if (i == 1) {
                    c = exp.getWellContext().getSelection().getCoord1();
                } else if (i == 2) {
                    c = exp.getWellContext().getSelection().getCoord2();
                }
            }
            if (c == null) {
                int x = 100;
                int y = 100;
                c = new WellCoordinate(x, y);
            }
            addWidget(c, color, i);
        }
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        WellCoordinate coord = getCoord(e);
        if (exp == null) {
            return "No experiment context";
        }
        Point p = e.getPoint();
        if (isInNavigationImage(p)) {
            return "This is the navigation pane";
        }
        NavigableImagePanel.Coords c = panelToImageCoords(p);
        double ex = c.getDoubleX();
        double ey = c.getDoubleY();

        curwidget = (CoordWidget) Widget.getClosest(ex, ey, widgets);
        int col = coord.getX() + exp.getColOffset();
        int row = coord.getY() + exp.getRowOffset();
        // also get value
        String msg = "";
        if (col >= 0 && row >= 0) {
            msg = "x/col=" + col + ", y/row=" + row + ", value=" + getValue(col, row);
        }
        Widget w = Widget.getClosest(ex, ey, widgets, false);
        if (w != null) {
            if (w == mainwidget) {
                msg += "<br><b>Double click</b> on me to view data for this coordinate";
            } else {
                msg += "<br><b>Double click</b> on me to select the <b>all wells</b> in this area";
            }
        } else {
            if (mainwidget == null) {
                // no widgets!
                msg += "<br><b>Double Click</b> to select a region";
            }
            else msg += "<br>Move a <b>widget</b> to select a well or region";
        }
        return "<html>" + msg + "</html>";
        //+" (im: "+coord.x+"/"+coord.y+"), chipy: "+(image.getHeight()-coord.y-BORDER)+
        //" ), bucket: "+bucket_size+", pixpercol: "+pixpercol+" BORDER="+BORDER;

    }

    public WellCoordinate getWell(CoordWidget w) {
        if (w == null) {
            return null;
        }
        int ex = w.getX();
        int ey = w.getY();

        WellCoordinate c = getCoord(new Point(ex, ey));
        if (exp != null)this.exp.makeAbsolute(c);
        w.setAbsoluteCoords(c);

        if (w == corner1 || w == corner2) {
            //  p("SET SELECTTION ");
            setSelection(new Coords(corner1.getX(), corner1.getY()),
                    new Coords(corner2.getX(), corner2.getY()));
        }

//        maincont.coordChanged(c);
        return c;
    }

    public Widget addWidget(WellCoordinate abscoord, Color color, int nr) {
        Point imagep = getMiddleImageCoord(abscoord);
        //   Coords nav=this.panelToImageCoords(imagep);
        //     p(coord.toString() + "-> image " + imagep);
        //  p("Creating widget  at " + imagep);
        CoordWidget w = new CoordWidget(color, imagep.x, imagep.y, nr);
        if (nr == 1) {
            corner1 = w;
        } else if (nr == 2) {
            corner2 = w;
        }
        w.setAbsoluteCoords(abscoord);
        widgets.add(w);
        return w;
    }

    public Point getMiddleImageCoord(WellCoordinate abscoord) {
        
        return this.getImagePointFromWell(abscoord);
    }

    public void setAreaOffsetX(int dx) {
        this.areaOffsetX = dx;
    }

    public void setAreaOffsetY(int dy) {
        this.areaOffsetY = dy;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the coordscale
     */
    public int getCoordscale() {
        return coordscale;
    }

    public double getValue(int col, int row) {
        return model.getValue(col, row);
    }

    /**
     * @param coordscale the coordscale to set
     */
    public void setCoordscale(int coordscale) {
        this.coordscale = coordscale;
    }

    public int getAreaOffsetX() {
        return this.areaOffsetX;
    }

    public int getAreaOffsetY() {
        return this.areaOffsetY;
    }

    public Point getImagePointFromWell(WellCoordinate coord) {
        WellCoordinate c = new WellCoordinate(coord);
        exp.makeRelative(c);
        int x = (int) this.chipToImageXForRelCoord(c.getCol());
        int y = (int) this.chipToImageYForRelCoord(c.getRow());
        return new Point(x, y);
    }

    public interface WellModel {

        public double getValue(int col, int row);
    }

    private void getLatestSelection() {
        final Collection<? extends WellSelection> selections = selectionSelection.allInstances();
        if (!selections.isEmpty()) {
            //  p("Getting last selection");
            WellSelection selection = null;
            Iterator<WellSelection> it = (Iterator<WellSelection>) selections.iterator();
            while (it.hasNext()) {
                selection = it.next();
            }
            if (selection != null) {
              //  p("getLatestSelection: Got new well selection: " + selection);
                setWellSelection(selection);
                repaint();
            }

        }

    }

    private class WellSelectionListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            getLatestSelection();
        }
    }

    public WellCoordinate getWellCoordinate() {
        return wellcoord;
    }

    private double chipToImageXForRelCoord(double c) {

        double x = (c * ((double) pixpercol / (double) getCoordscale() / (double) bucket_size) + BORDER);
        return x;

    }

    private double chipToImageYForRelCoord(double r) {

        double y = (r * ((double) pixperrow / (double) getCoordscale() / (double) bucket_size) + BORDER - image.getHeight());
        return -y;

    }

    public WellCoordinate getCoord(MouseEvent e) {
        Point p = e.getPoint();
        NavigableImagePanel.Coords coord = panelToImageCoords(p);
        double x = coord.getDoubleX();
        double y = coord.getDoubleY();

        int col = imageXToChip(x);
        int row = imageYToChip(y);
        return new WellCoordinate(col, row);
    }
    

    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        setToolTipText(getToolTipText(e));
    }

    public WellCoordinate getCoord(Point p) {

        // NavigableImagePanel.Coords coord = panelToImageCoords(p);
        double x = p.getX();
        double y = p.getY();
        //  p("Got x/y " + x + "/" + y + ", BORDER=" + BORDER + ", image.height=" + image.getHeight() + ", pixperrow=" + pixperrow + ", bucket_size=" + bucket_size);
        int col = imageXToChip(x);
        int row = imageYToChip(y);
        //    p("-> col/row: " + col + "/" + row);
        return new WellCoordinate(col, row);
    }

    public int imageXToChip(double x) {
        x = (x - BORDER - 0.5);
        int c = (int) Math.round(x / pixpercol) * bucket_size;
        c = Math.max(0, c);
        c = c + this.areaOffsetX;
        //   p("getImageXToChip: " + c + ", areaoffx=" + areaOffsetX);
        //   p("x "+x+"->  col "+c+",   pixpercol="+pixpercol+", bucketsize="+bucket_size);
        return c * getCoordscale();
    }

    public int imageYToChip(double y) {
        y = (image.getHeight() - y - BORDER);
        int r = (int) Math.round(y / pixperrow) * bucket_size;
        r = Math.max(0, r);
        r = r + this.areaOffsetY;
        //  p("getImageYToChip: " + r + ", areaoffy=" + areaOffsetY);
        //  p("y "+y+"->  row "+r+",   pixperrow="+pixperrow);        

        return r * getCoordscale();
    }

    private void showSelection(Graphics g) {
        g.setPaintMode();
        g.setColor(Color.white);
        int x = (int) (getWidth() * 0.2);
        int y = (int) (30);

        if (this.curwidget != null) {
            g.drawString(curwidget.toString(), x, 15);
        }
        if (corner1 != null) {
            g.drawString(corner1.getAbsoluteCoord() + "-" + corner2.getAbsoluteCoord(), x, y);
        }
        //   p("Drawing selection string at " + x + "/" + y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        showSelection(g);
        if (title != null) {
            int x = (int) (getWidth() * 0.2);
            int y = (int) (12);
            g.drawString(title, x, y);
        }
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

    /**
     * @return the selection
     */
    public WellSelection getWellSelection() {

        return wellselection;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(WellsImagePanel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(WellsImagePanel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(WellsImagePanel.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("WellsImagePanel: " + msg);
        Logger.getLogger( WellsImagePanel.class.getName()).log(Level.INFO, msg);
    }

    public void setWellSelection(WellSelection selection) {
      //  p("SETWELLSELECTION CALLED");
        // this.wellselection = selection;

        // also zoom to selection
        WellCoordinate c1 = selection.getCoord1();
        WellCoordinate c2 = selection.getCoord2();
        double x0 = this.chipToImageXForRelCoord(c1.getCol());
        double y0 = this.chipToImageYForRelCoord(c1.getRow());
        Coords start = new Coords(x0, y0);
        double x = this.chipToImageXForRelCoord(c2.getCol());
        double y = this.chipToImageYForRelCoord(c2.getRow());
        Coords end = new Coords(x + pixpercol, y + pixperrow);
        if (corner1 != null) {
            corner1.setAbsoluteCoords(c1);
            corner2.setAbsoluteCoords(c2);
           
            if (exp != null) {
                if (exp.getWellContext().getCoordinate() != null) {
                    WellCoordinate c = new WellCoordinate(exp.getWellContext().getCoordinate());
                    exp.makeAbsolute(c);
                    mainwidget.setAbsoluteCoords(c);
                }
                else mainwidget.setAbsoluteCoords(c1);
            }            
        }

        setSelection(start, end);
    }

    public void setPixPerCol(double pixpercol) {
        this.pixpercol = pixpercol;
    }

    public void setPixPerRow(double pixperrow) {

        this.pixperrow = pixpercol;
    }

    public void setBucketSize(int bucketSize) {
        this.bucket_size = bucketSize;
    }
}
