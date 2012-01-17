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
import com.iontorrent.wellmodel.WellSelection;
import com.iontorrent.guiutils.NavigableImagePanel;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.wellmodel.WellCoordinate;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
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
    private int coordscale = 1;
    int bucket_size;
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

    
    public WellsImagePanel(final ExperimentContext exp, final int BORDER, final BufferedImage image, final double pixpercol, final double pixperrow, final int bucket_size, WellModel model) throws IOException {
        super(image);
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

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (SwingUtilities.isRightMouseButton(e)) {
                    return;
                }
                Point p = e.getPoint();
                if (isInNavigationImage(p)) {
                    return;
                }
                NavigableImagePanel.Coords coord = panelToImageCoords(p);
                double x = coord.getDoubleX();
                double y = coord.getDoubleY();

                int col = imageXToChip(x);
                int row = imageYToChip(y);
                wellcoord = new WellCoordinate(col, row);
                p("Image mouse clicked, got coord: " + wellcoord);
                p("areaoffx=" + areaOffsetX);
                if (wellselection == null) {
                    wellselection = new WellSelection(col, row, col, row);
                    if (exp != null) {
                        wellselection.setOffx(exp.getColOffset());
                        wellselection.setOffy(exp.getRowOffset());
                    }

                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    return;
                }
                Selection sel = getSelection();
                if (sel != null) {
                    p("Got a selection in IMAGE coords: " + sel + ", pixpercol: " + pixpercol + ", bucket:" + bucket_size);
                    // compute coords

                    int col1 = imageXToChip(sel.getX1());
                    int col2 = imageXToChip(sel.getX2());
                    int row1 = imageYToChip(sel.getY1());
                    int row2 = imageYToChip(sel.getY2());
                    p("areaoffx=" + areaOffsetX);
                    if (col1 < 0 || col1 > image.getWidth() * bucket_size) {
                        err("Coords col " + col1 + " out of bounds, should be between 0 and " + image.getWidth() * bucket_size);
                    }
                    if (row1 < 0 || row1 > image.getHeight() * bucket_size) {
                        err("Coords row " + row1 + " out of bounds, should be between 0 and " + image.getHeight() * bucket_size);
                    }
                    col1 = Math.max(0, col1);
                    col2 = Math.max(0, col2);
                    row1 = Math.max(0, row1);
                    row2 = Math.max(0, row2);
                    wellselection = new WellSelection(col1, row1, col2, row2);
                    if (exp != null) {
                        wellselection.setOffx(exp.getColOffset());
                        wellselection.setOffy(exp.getRowOffset());
                    }
                    wellcoord = null;//new WellCoordinate((col1 + col2) / 2, (row1 + row2) / 2);
                    //    p("Selected c/r:" + col1 + "/" + row1 + "-" + col2 + "/" + row2);

                }

            }
        });
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
                p("getLatestSelection: Got new well selection: " + selection);
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

    public WellCoordinate getCoord(Point p) {

        // NavigableImagePanel.Coords coord = panelToImageCoords(p);
        double x = p.getX();
        double y = p.getY();
        p("Got x/y " + x + "/" + y + ", BORDER=" + BORDER + ", image.height=" + image.getHeight() + ", pixperrow=" + pixperrow + ", bucket_size=" + bucket_size);
        int col = imageXToChip(x);
        int row = imageYToChip(y);
        p("-> col/row: " + col + "/" + row);
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

        g.drawString("Selected wells: " + wellselection, x, y);
        //   p("Drawing selection string at " + x + "/" + y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (wellselection != null) {
            showSelection(g);
        }
        if (title != null) {
            int x = (int) (getWidth() * 0.2);
            int y = (int) (12);
            g.drawString(title, x, y);
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
        //Logger.getLogger( WellsImagePanel.class.getName()).log(Level.INFO, msg, ex);
    }

    public void setWellSelection(WellSelection selection) {
        this.wellselection = selection;

        // also zoom to selection
        WellCoordinate c1 = selection.getCoord1();
        WellCoordinate c2 = selection.getCoord2();
        double x0 = this.chipToImageXForRelCoord(c1.getCol());
        double y0 = this.chipToImageYForRelCoord(c1.getRow());
        Coords start = new Coords(x0, y0);
        double x = this.chipToImageXForRelCoord(c2.getCol());
        double y = this.chipToImageYForRelCoord(c2.getRow());
        Coords end = new Coords(x + pixpercol, y + pixperrow);
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
