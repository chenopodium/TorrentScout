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
package com.iontorrent.guiutils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Chantal Roth
 */
public class ImagePanel extends JPanel {

    private Selection cur_selection = null;
    private Color color_select = Color.yellow;
    private int oldmousey = -1;
    private int oldmousex = -1;
    private int mousex = -1;
    private int mousey = -1;
    private int dragx = -1;
    private int dragy = -1;
    private int endx = -1;
    private int endy = -1;
    private int originX = 0;
    private int originY = 0;
    private BufferedImage image;
    private int selectionSize;
    
    public static final String IMAGE_CHANGED_PROPERTY = "image";
    private Point mousePosition;

    public ImagePanel(BufferedImage image) {
        this();
        setImage(image);
    }

    public ImagePanel() {
        addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {

                    Point p = e.getPoint();
                //    p("MousePressed at " + p);
                    cur_selection = new Selection();
                    cur_selection.startPoint = panelToImageCoords(p);

                    oldmousex = e.getX();
                    oldmousey = e.getY();
                    dragx = oldmousex;
                    dragy = oldmousey;
                    endx = dragx;
                    endy = dragy;

                }

            }

            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {

                    Point p = e.getPoint();
            //        p("MouseReleased at " + p);

                    cur_selection.endPoint = panelToImageCoords(p);
            //        p("Got a selection now: " + cur_selection);

                    if (mousex <= 0) {
                        return;
                    }
                    endx = e.getX();
                    endy = e.getY();
                    int dx = Math.abs(endx - oldmousex);
                    int dy = Math.abs(endy - oldmousey);
                    Graphics g = getGraphics();
                    drawXorRect(g, oldmousex, oldmousey, mousex - oldmousex, mousey - oldmousey);
                    if (dx < 5 || dy < 5) {
                        mousex = -1;
                        return;
                    }
                    g.setPaintMode();
                    mousex = -1;
                    repaint();

                }

            }
        });

        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Point p = e.getPoint();
                    // record end point
                    Graphics g = getGraphics();
                    if (mousex > -1) {
                        drawXorRect(g, oldmousex, oldmousey, mousex - oldmousex, mousey - oldmousey);
                    }
                    mousex = e.getX();
                    mousey = e.getY();
                    int dx = mousex - oldmousex;
                    int dy = mousey - oldmousey;
                    drawXorRect(g, oldmousex, oldmousey, dx, dy);

                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                //we need the mouse position so that after zooming
                //that position of the image is maintained
                mousePosition = e.getPoint();
                // p("MouseMoved at "+mousePosition);
            }
        });
    }

    /**
     * <p>Sets an image for display in the panel.</p>
     *
     * @param image an image to be set in the panel
     */
    public void setImage(BufferedImage image) {
        BufferedImage oldImage = this.image;
        this.image = image;
        firePropertyChange(IMAGE_CHANGED_PROPERTY, (Image) oldImage, (Image) image);
        repaint();
    }
    //Converts this panel's coordinates into the original image coordinates

    private Coords panelToImageCoords(Point p) {
        return new Coords((p.x - originX), (p.y - originY));
    }

    //Converts the original image coordinates into this panel's coordinates
    private Coords imageToPanelCoords(Coords p) {
        return new Coords((p.x) + originX, (p.y) + originY);
    }

    private void drawXorRect(Graphics g, int sx, int sy, int dx, int dy) {
        drawXorRect(g, sx, sy, dx, dy, color_select);
    }

    private void drawXorRect(Graphics g, int sx, int sy, int dx, int dy, Color c) {

        int x1 = sx;
        int y1 = sy;

        if (selectionSize > 0) {
            dx = selectionSize;
            dy = selectionSize;
        }
        if (dx < 0) {
            x1 = sx + dx;
            dx = -dx;
        }
        if (dy < 0) {
            y1 = sy + dy;
            dy = -dy;
        }
        g.setColor(c);
        g.setXORMode(Color.black);
        // p("drawing XOR rect:" + x1 + ", " + y1 + ", dx:" + dx + ", dy:" + dy);

        g.drawRect(x1, y1, dx, dy);

    }

    public Selection getSelection() {
        if (cur_selection == null || cur_selection.endPoint == null) {
            return null;
        }
        return cur_selection;
    }

    public void clear(Graphics g) {

        if (g == null) {
            return;
        }
        //	System.out.println("basiccanvas: clear");
        g.setColor(Color.white);
        try {
            g.fillRect(0, 0, (int) getSize().getWidth(), (int) getSize().getHeight());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private int getScreenImageWidth() {
        return (int) (image.getWidth());
    }

    private int getScreenImageHeight() {
        return (int) (image.getHeight());
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Paints the background

        if (image == null) {
            return;
        }

        g.drawImage(image, originX, originY, getScreenImageWidth(),
                getScreenImageHeight(), null);


        // draw Selection
        if (cur_selection != null && cur_selection.endPoint != null) {
            Coords start = this.imageToPanelCoords(cur_selection.startPoint);
            Coords end = this.imageToPanelCoords(cur_selection.endPoint);
            g.setColor(color_select);
            g.drawRect((int) start.x, (int) start.y, (int) end.x - (int) start.x, (int) end.y - (int) start.y);
        }
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(ImagePanel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(ImagePanel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(ImagePanel.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("ImagePanel: " + msg);
        //Logger.getLogger( ImagePanel.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the selectionSize
     */
    public int getSelectionSize() {
        return selectionSize;
    }

    /**
     * @param selectionSize the selectionSize to set
     */
    public void setSelectionSize(int selectionSize) {
        this.selectionSize = selectionSize;
    }

    public class Selection {

        Coords startPoint;
        Coords endPoint;

        public String toString() {
            return startPoint.toString() + "-" + endPoint.toString();
        }

        public int getX1() {
            return startPoint.getIntX();
        }

        public int getY1() {
            return startPoint.getIntY();
        }

        public int getX2() {
            return endPoint.getIntX();
        }

        public int getY2() {
            return endPoint.getIntY();
        }
    }
    //This class is required for high precision image coordinates translation.

    private class Coords {

        public double x;
        public double y;

        public Coords(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public int getIntX() {
            return (int) Math.round(x);
        }

        public int getIntY() {
            return (int) Math.round(y);
        }

        public String toString() {
            return "[Coords: x=" + x + ",y=" + y + "]";
        }
    }
}
