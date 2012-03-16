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
import com.iontorrent.rawdataaccess.wells.GeneralWellDensity;
import com.iontorrent.wellmodel.WellSelection;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.guiutils.heatmap.ColorModel;
import com.iontorrent.guiutils.heatmap.GradientPanel;
import com.iontorrent.guiutils.wells.WellsImagePanel.WellModel;
import com.iontorrent.utils.io.FileTools;

import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author Chantal Roth
 */
public abstract class GeneralDensityPanel extends JPanel implements WellModel, ActionListener {

    protected int coordscale = 1;
    protected WellsImagePanel imagePanel;
    protected WellsImagePanel oldimagePanel;
    protected GeneralWellDensity wellDensity;
    /** the offscreen image to which the density plot is drawn */
    protected BufferedImage bimage;
    /** Which flag to use to draw, example LIVE or EMPTY or DUD etc */
    protected double pixpercol;
    protected double pixperrow;
    protected WellContext wellcontext;
    private ColorModel colormodel;
    private Color[] gradientColors;
    private GradientPanel gradient;
    private int IMAGE_SIZE = 400;
    private int MIN_IMAGE_SIZE = 256;
    protected int BORDER = 35;
    private int MAX_IMAGE_SIZE = 2048 + 2 * BORDER;
    protected int BUCKET;
    protected int fontsize = 14;
    //private int MAX_COORDS = 10000;
    protected Font fcoord = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    //   private Font fcoord1 = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    protected ExperimentContext expcontext;
    private int nrWidgets;
    private JPopupMenu rightMenu;
    private MouseAdapter popAdapter;
    private WellCoordinate popcoord;

    protected boolean useMainWidget;
    protected boolean useCornerWidgets;
    
    public GeneralDensityPanel(ExperimentContext exp, int nrwidgets) {
        setLayout(new BorderLayout());
        this.expcontext = exp;
        this.nrWidgets = nrwidgets;
        this.setBackground(Color.black);
        makePopup();
    }

    protected void addPopup(String cmd) {
        rightMenu.add(makeMenuItem(cmd));
    }

    private void makePopup() {
        rightMenu = new JPopupMenu("Actions");
        popAdapter = new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }

            // And on other platforms, mousePressed sets PopupTrigger.
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }

            // Get the component over which the right button click
            // occurred and show the menu there.
            public void showPopupMenu(MouseEvent e) {
                popcoord = imagePanel.getCoord(e);
                p("Got popupcoord="+popcoord);
                rightMenu.show(GeneralDensityPanel.this, e.getX(), e.getY());
            }
        }; // anonymous MouseAdapter subclass

    } // makePopup

    /** A utility method for making menu items. **/
    private JMenuItem makeMenuItem(String label) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand().toLowerCase().trim();
                WellCoordinate coord = popcoord;
                p("Got popup command:" + cmd + " at " + coord);
                processPopupCommand(cmd, coord);
            }
        });
        return item;
    } // 

    protected void processPopupCommand(String cmd, WellCoordinate coord) {
        p("Got popup command: " + cmd + ", no handler implemented");
    }

    /**
     * @return the exp
     */
    public ExperimentContext getExp() {
        return expcontext;
    }

    /**
     * @param exp the exp to set
     */
    public void setExp(ExperimentContext exp) {
        this.expcontext = exp;
    }

    public void setBorder(int border) {
        this.BORDER = border;
        MAX_IMAGE_SIZE = 2048 + 2 * BORDER;
    }

    public void showNavigationImage(boolean b) {
        if (imagePanel != null) {
            imagePanel.setNavigationImageEnabled(b);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        double max = gradient.getMax();
        double min = gradient.getMin();
        p(" Got new min max - redrawing image");
        setMax(max);
        setMin(min);
        this.redrawImage();
    }

    protected void drawCoords(Graphics2D g, Color coordcolor, int offx, int cols, int COORDDELTA, int maxy, int offy, int rows, int maxx) {
        g.setColor(coordcolor);
        for (int c = offx; c <= offx + cols * BUCKET; c++) {
            int startx = (int) (BORDER + ((c - offx) / BUCKET * pixpercol));
            if (c % COORDDELTA == 0) {

                g.drawLine(startx, BORDER + (int) pixpercol, startx, maxy + (int) pixpercol);
                int value = c * coordscale;
                g.drawString("" + value, startx - 3, maxy + this.fontsize + 6);
            }
        }
        for (int r = offy; r <= offy + rows * BUCKET; r++) {
            int starty = (int) ((((r - offy) / BUCKET - 1) * pixperrow));
            if (r % COORDDELTA == 0) {
                g.drawLine(BORDER, maxy - starty, maxx, maxy - starty);
                int x = Math.max(1, BORDER - 10 - (int) (10 * (int) Math.log10(r + 1)));
                x = Math.min(x, BORDER - 15);
                int value = r * coordscale;
                g.drawString("" + value, x, maxy - starty + 3);
            }
        }
    }

    protected abstract void setMax(double max);

    protected abstract void setMin(double min);

    public WellCoordinate getWellCoordinate(int x, int y) {
        if (imagePanel == null) {
            p("Got no image panel");
            return null;
        }
        WellCoordinate coord = imagePanel.getCoord(new Point(x, y));
        return coord;
    }

    public Point getImagePointFromWell(WellCoordinate coord) {
        return imagePanel.getImagePointFromWell(coord);
    }

    public boolean export() {
        if (imagePanel == null) {
            GuiUtils.showNonModalMsg("Got no image to export yet...");
            return false;
        }
        String file = FileTools.getFile("Save image to a file", "*.*", null, true);
        return export(file);
    }

    public boolean export(String file) {
        if (file == null || file.length() < 3) {
            GuiUtils.showNonModalMsg("I need to know if it is a .png or a .jpg file");
            return false;
        }
        if (imagePanel == null) {
            return false;
        }
        File f = new File(file);
        String ext = file.substring(file.length() - 3);
        RenderedImage image = myCreateImage();
        try {
            return ImageIO.write(image, ext, f);
        } catch (IOException ex) {
            err("Could not write image to file " + f, ex);
        }
        return false;
    }
    // Returns a generated image.

    public RenderedImage myCreateImage() {
        return myCreateImage(450, 450);
    }

    public RenderedImage myCreateImage(int minw, int minh) {
        if (bimage == null) {
            try {
                this.setSize(minw, minh);
                createAndDrawImage();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
        if (bimage != null) {
            p("Got image:" + bimage + " of size " + bimage.getWidth() + "/" + bimage.getHeight());
        }
//        imagePanel.setSize(Math.max(minw, bimage.getWidth()), Math.max(minh, bimage.getHeight()));
//        
//        int width =Math.max(minw,imagePanel.getWidth());
//        int height = Math.max(minh, imagePanel.getHeight());
//
//        
//        // Create a buffered image in which to draw
//        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//
//        // Create a graphics contents on the buffered image
//        Graphics2D g2d = bufferedImage.createGraphics();
//
//        this.b
        // Draw graphics


        return this.bimage;
    }

    protected void drawCoords(Graphics2D g, int cols, int maxy, int rows, int maxx) {
        drawCoords(g, cols, maxy, rows, maxx, Color.white);
    }

    protected void drawCoords(Graphics2D g, int cols, int maxy, int rows, int maxx, Color coordcolor) {
        // p("Drawing coords");

        int COORDDELTA = 100;
        //if (cols/BUCKET)
        if (this.BUCKET > 39) {
            COORDDELTA = 500;
        } else if (this.BUCKET > 9) {
            COORDDELTA = 200;
        } else if (this.BUCKET == 2) {
            COORDDELTA = 50;
        } else if (this.BUCKET == 1) {
            COORDDELTA = 20;
        }
        if (COORDDELTA * pixperrow / BUCKET < fontsize * 4) {
            // too narrow
            COORDDELTA = COORDDELTA * 2;
        }
        g.setStroke(new BasicStroke(2));
        g.setFont(fcoord);


        int offx = 0;
        int offy = 0;
        if (expcontext != null) {
            offx = expcontext.getColOffset();
            offy = expcontext.getRowOffset();
            //  p(" Got offset:"+offx+"/"+offy);
        }
        drawCoords(g, coordcolor, offx, cols, COORDDELTA, maxy, offy, rows, maxx);
    }

    public WellContext getContext() {
        return wellcontext;
    }

    public void clear() {
        if (imagePanel != null) {
            remove(imagePanel);
            imagePanel = null;
            this.repaint();
        }
    }

    public void setContext(WellContext context, int bucketsize) {
        this.wellcontext = context;
        // wellDensity = new GeneralWellDensity(mask, bucketsize);

        if (wellDensity == null) {
            return;
        }
        try {
            createAndDrawImage();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /** draw the well density to an offscreen image */
    private void createImage() {
        //  GraphicsDevice gs = ge.getDefaultScreenDevice();
        //   GraphicsConfiguration gc = gs.getDefaultConfiguration();
        BUCKET = wellDensity.getBucketSize();
        //  if (BUCKET == 1) IMAGE_SIZE = Math.max(MIN_IMAGE_SIZE,this.mask.getNrCols())+2*BORDER;
        int width = Math.min(MAX_IMAGE_SIZE, Math.max(MIN_IMAGE_SIZE, wellDensity.getNrCols() * 2)) + 2 * BORDER;
        int height = Math.min(MAX_IMAGE_SIZE, Math.max(MIN_IMAGE_SIZE, wellDensity.getNrRows() * 2)) + 2 * BORDER;

        int cols = wellDensity.getNrCols();
        int rows = wellDensity.getNrRows();
        int w = height - 2 * BORDER;
        pixpercol = Math.round(Math.max(1.0, (double) w / (double) (cols)));
        pixperrow = pixpercol;//= Math.round(Math.max(1.0, (double) h / (double) (rows)));
        height = (int) (pixperrow * rows + 2 * BORDER);
        width = (int) (pixpercol * cols + 2 * BORDER);
        BUCKET = wellDensity.getBucketSize();

        double pixpercoord = width * 100.0 / cols;
        // nr of pixels per coordinate
        fontsize = 14;
        if (pixpercoord - 1 < 14) {
            fontsize = (int) pixpercoord - 1;
        } else if (pixpercoord > 200) {
            pixpercoord = 20;
        } else if (pixpercoord > 100) {
            pixpercoord = 18;
        } else if (pixpercoord > 50) {
            pixpercoord = 16;
        }
        //    p("Creating offscreen image of size " + width + "/" + height + ", pixpercoord=" + pixpercoord + ", font size=" + fontsize);
        fcoord = new Font(Font.SANS_SERIF, Font.BOLD, fontsize);
        // Create an image that does not support transparency
        // bimage = gc.createCompatibleImage(width, height, Transparency.OPAQUE);
        bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics contents on the buffered image
        // Graphics2D g2d = bufferedImage.createGraphics();

        //   p("Creating image done");
    }

    protected abstract int getCount(int c, int r);
    //int count = wellDensity.getCount(c, r, bfmaskflag);

    protected abstract int getMax();

    protected abstract int getMin();

    public void setColors(Color[] gradientColors) {
        this.gradientColors = gradientColors;
        colormodel = new ColorModel(gradientColors, getMin(), getMax());
        if (gradient == null) {
            gradient = new GradientPanel(colormodel);
        } else {
            gradient.setColorModel(colormodel);
            gradient.setMax(getMax());
        }
        gradient.setListener(this);
    }

    /** Draws the well density plot onto the buffered Image */
    protected void drawDensityOnImage() {
        // Create a graphics context on the buffered image
        if (wellDensity == null || bimage == null) {
            return;
        }
//        int width = bimage.getWidth();
//        int height = bimage.getHeight();
        int cols = wellDensity.getNrCols();
        int rows = wellDensity.getNrRows();
        int w = (int) (cols * pixpercol);
        int h = (int) (rows * pixperrow);

        BUCKET = wellDensity.getBucketSize();

        Graphics2D g = bimage.createGraphics();
        g.setBackground(Color.black);
        if (gradientColors == null || gradientColors.length < 1) {
            gradientColors = new Color[]{Color.black, Color.blue, Color.green, Color.yellow, Color.orange, Color.red, Color.white};
        }
        setColors(gradientColors);

        // p("Drawing image, densityrows=" + rows + ", densitycols=" + cols);

        int maxy = BORDER + h;
        int maxx = BORDER + w;
        g.setStroke(new BasicStroke(1));
        for (int c = 0; c < cols; c++) {
            int startx = (int) (BORDER + (c * pixpercol));
            for (int r = 0; r < rows; r++) {
                int starty = (int) ((r * pixperrow));
                int count = getCount(c, r);
                Color color = colormodel.getColor(count);
                g.setColor(color);
                g.fillRect(startx, maxy - starty, (int) pixpercol, (int) pixperrow);
            }
        }
        drawCoords(g, cols, maxy, rows, maxx);
        g.dispose();
    }

    public void redrawImage() {
        bimage = null;
        try {
            createAndDrawImage();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected void createAndDrawImage() throws IOException {
        if (gradient != null) {
            remove(gradient);
        }
        createImage();
        drawDensityOnImage();
        if (imagePanel != null) {

            imagePanel.setPixPerCol(pixpercol);
            imagePanel.setPixPerRow(pixperrow);
            imagePanel.setBucketSize(wellDensity.getBucketSize());
            imagePanel.setImage(bimage);
            imagePanel.setCoordscale(coordscale);

        } else {

            imagePanel = new WellsImagePanel(expcontext, BORDER, bimage, pixpercol, pixperrow, wellDensity.getBucketSize(), this, nrWidgets);

            imagePanel.setCoordscale(coordscale);
            //   p("WellsImagePanel created");
            add("Center", imagePanel);

            imagePanel.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {

                    processClick(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {

                    processClick(e);
                }
            });
            imagePanel.addMouseListener(popAdapter);
        }

        afterImageCreated();
        add("East", gradient);
        gradient.repaint();
        imagePanel.repaint();
        this.repaint();
    }

    protected void processClick(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) {
            popcoord = imagePanel.getCoord(e.getPoint());
        }
        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
            p("GeneralDensityPanel.processClick");
            WellSelection sel = imagePanel.getWellSelection();
            if (sel == null && imagePanel.getWellCoordinate() == null) {
                p("GeneralDensityPanel.processClick. Got no well selection or coordinate");
                return;
            }
            p("Got selection from image: " + ", sel is: " + sel + ", area offset=" + imagePanel.getAreaOffsetX() + "/" + imagePanel.getAreaOffsetY());
            if (sel != null) {
                ArrayList<WellCoordinate> coords = getCoords(sel);
                sel.setAllWells(coords);
                p("publishing SELECTION");
                publishSelection(sel);
            }

            WellCoordinate coord = imagePanel.getWellCoordinate();


            if (wellcontext == null && expcontext != null) {
                wellcontext = expcontext.getWellContext();
            }
            if (coord != null && wellcontext != null) {
                wellcontext.setCoordinate(coord);
                wellcontext.loadMaskData(coord);
                p("publishing relative coord: " + coord);
                publishCoord(coord);
            }

        }
        return;
    }

    protected void afterImageCreated() {
    }

    @Override
    public void repaint() {
        super.repaint();
        if (imagePanel != null) {
            imagePanel.repaint();
        }
        if (gradient != null) {
            gradient.repaint();
        }
    }

    /** make abstract */
    protected abstract void publishSelection(WellSelection sel);

    protected abstract void publishCoord(WellCoordinate coord);

    protected abstract ArrayList<WellCoordinate> getCoords(WellSelection sel);
    // ArrayList<WellCoordinate> coords = mask.getAllCoordsWithData(bfmaskflag, MAX_COORDS,
    //                       sel.getCoord1().getCol(), sel.getCoord1().getRow(), sel.getCoord2().getCol(), sel.getCoord2().getRow());

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(IMAGE_SIZE, IMAGE_SIZE);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(IMAGE_SIZE, IMAGE_SIZE);
    }

    protected void err(String msg, Exception ex) {
        Logger.getLogger(getClass().getName() + ": ").log(Level.SEVERE, msg, ex);
    }

    protected void err(String msg) {
        Logger.getLogger(getClass().getName() + ": ").log(Level.SEVERE, msg);
    }

    protected void p(String msg) {
        System.out.println(getClass().getName() + ": " + msg);
        Logger.getLogger(GeneralDensityPanel.class.getName()).log(Level.INFO, msg);
    }

    /**
     * @return the coordscale
     */
    public int getCoordscale() {
        return coordscale;
    }

    /**
     * @param coordscale the coordscale to set
     */
    public void setCoordscale(int coordscale) {
        //   p("SETTING COORDSCALE TO: " + coordscale);
        this.coordscale = coordscale;
    }

    /**
     * @return the nrWidgets
     */
    public int getNrWidgets() {
        return nrWidgets;
    }

    /**
     * @param nrWidgets the nrWidgets to set
     */
    public void setNrWidgets(int nrWidgets) {
        this.nrWidgets = nrWidgets;
    }
   
     protected void showWell(int c, int r, Graphics2D g, Color color, boolean fill) {        
        int y = (int) (((r  / BUCKET - 1) * pixperrow));                        
        int x = (int) (BORDER + (c  / BUCKET * pixpercol));
        g.setColor(color);
        if (fill) {
            g.fill3DRect(x-1, y-1 , (int)pixpercol+2, (int)pixperrow+2, true);
        }
        else {
            g.draw3DRect(x-1, y-1 , (int)pixpercol+2, (int)pixperrow+2, true);
        }
        
       // g.drawString("" + value, x, maxy - starty + 3);
    }
}
