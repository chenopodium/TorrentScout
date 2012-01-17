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

import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.guiutils.heatmap.GradientPanel;

import com.iontorrent.torrentscout.explorer.ExplorerContext;
import com.iontorrent.utils.io.FileTools;
import com.iontorrent.wellmodel.WellCoordinate;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author Chantal Roth
 */
public class RasterPanel extends JPanel{

    // private File file;
    //private WellContext context;
    private GradientPanel gradient;
    //MovieHeatMap view; 
    private SubregionView view;
    private int SIZE = 200;

    public RasterPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.black);
        this.setMinimumSize(new Dimension(SIZE, SIZE));
        // view = new MovieFrameView();
       // updateView(true);
    }

    @Override
    public void repaint() {
        super.repaint();
        if (view != null) {
           // p("Calling subregionview.repaint");
            view.repaint();
        }
      //  panMain.rep
    }
    private void updateView(boolean minmax) {
        if (view == null) {
            return;
        }
        // view.invalidate();
        view.update(minmax);
    }

    public void setFrame(int frame) {
        // view.paintImmediately(0,0,SIZE,SIZE);
        if (view == null) {
           // p("Got no subregion view");
            return;
        }
        
        view.setFrame(frame);
        updateView(false);
    }

    public RenderedImage myCreateImage(int frame) {
        return view.createImage(frame);
    }
     public boolean export() {
        
        String file = FileTools.getFile("Save image to a file", "*.*", null, true);
        return export(file);
    }

    public boolean export(String file) {
        if (file == null || file.length() < 3) {
            GuiUtils.showNonModalMsg("I need to know if it is a .png or a .jpg file");
            return false;
        }
       
        File f = new File(file);
        String ext = file.substring(file.length() - 3);
        RenderedImage image = myCreateImage(getWidth(), getHeight());
        try {
            return ImageIO.write(image, ext, f);
        } catch (IOException ex) {
            err("Could not write image to file " + f, ex);
        }
        return false;
    }
     public RenderedImage myCreateImage(int w, int h) {
        int width =Math.max(w,getWidth());
        int height = Math.max(h, getHeight());

        setSize(Math.max(w, getWidth()), Math.max(h, getHeight()));
        // Create a buffered image in which to draw
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics contents on the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();

        // Draw graphics
        view.paintComponent(g2d);

        return bufferedImage;
    }

    public WellCoordinate getWellCoordinate(int x, int y) {
        if (view == null) {
            p("Got no image panel");
            return null;
        }
        WellCoordinate coord = view.getCoord(new Point(x, y));
        return coord;
    }

    public void checkWidgets(boolean snap) {
        if (view != null) {
             view.checkWidgets(snap);
         }
    }
    public void redrawImages(boolean showignore, boolean showbg, boolean showuse) {
         if (view != null) {
             view.redrawImages(showignore, showbg, showuse, false);
         }
         repaint();
    }
   
    public String update(ExplorerContext maincont) {
        p(" Update called in RasterPanel");
        //   this.context = context;
        WellCoordinate coord = maincont.getAbsDataAreaCoord();
        if (coord == null) {
            // coord = new WellCoordinate(0,0);
            return "Cannot read acquisition file, got no region coordinates.";
        }

        if (maincont.getData() == null) {
            p("Got no image data in main cont"+ maincont);
            String s = "RasterPanel: I got no raster data for region " + coord+" from explorer context";
            if (maincont.getFiletype() == null) {
                s += "<br>because no file type was chosen";
                return s;
            }
            return s;
        }

        if (view != null) {
            p("removing view");
            remove(view);
        }
        if (gradient != null) {
            remove(gradient);
        }
         
        view = new SubregionView(maincont);
       // this.addKeyListener(view);
        p("Created SubregionView, frame "+maincont.getFrame());
        view.setFrame(maincont.getFrame());
              
        gradient = view.getGradientPanel();   
        
        //    p("adding movie frameview to center");
        add("Center", view);
        add("East", gradient);
        //    p("Adding view and gradient");
        view.update(true);
       
        return null;
    }
    public void listenToKeysFrom(JComponent comp) {
        if (view != null)comp.addKeyListener(view);
    }
    public void redoMinMax() {
         view.update(true);
         
    }
    // add popup her
    // when min/max is changed, create new colors and gradient panel

    public WellCoordinate getFrom() {
        return view.getFrom();
    }

    public WellCoordinate getTo() {
        return view.getTo();
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(RasterPanel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(RasterPanel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(RasterPanel.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("RasterPanel: " + msg);
        //Logger.getLogger( RasterPanel.class.getName()).log(Level.INFO, msg, ex);
    }

   
}
