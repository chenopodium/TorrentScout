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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 ** <p>
 * <code>NavigableImagePanel</code> is a lightweight container displaying
 * an image that can be zoomed in and out and panned with ease and simplicity,
 * using an adaptive rendering for high quality display and satisfactory performance.
 * </p>
 * <p>
 * <h3>Image</h3>
 * <p>An image is loaded either via a constructor:</p>
 * <pre>
 * NavigableImagePanel panel = new NavigableImagePanel(image);
 * </pre>
 * or using a setter:
 * <pre>
 * NavigableImagePanel panel = new NavigableImagePanel();
 * panel.setImage(image);
 * </pre>
 * When an image is set, it is initially painted centered in the component,
 * at the largest possible size, fully visible, with its aspect ratio is preserved.
 * This is defined as 100% of the image size and its corresponding zoom level is 1.0.
 * </p>
 * <h3>Zooming</h3>
 * <p>
 * Zooming can be controlled interactively, using either the mouse scroll wheel (default)
 * or the mouse two buttons, or programmatically, allowing the programmer to
 * implement other custom zooming methods. If the mouse does not have a scroll wheel,
 * set the zooming device to mouse buttons:
 * <pre>
 * panel.setZoomDevice(ZoomDevice.MOUSE_BUTTON);
 * </pre>
 * The left mouse button works as a toggle switch between zooming in and zooming out
 * modes, and the right button zooms an image by one increment (default is 20%).
 * You can change the zoom increment value by:
 * <pre>
 * panel.setZoomIncrement(newZoomIncrement);
 * </pre>
 * If you intend to provide programmatic zoom control, set the zoom device to none
 * to disable both the mouse wheel and buttons for zooming purposes:
 * <pre>
 * panel.setZoomDevice(ZoomDevice.NONE);
 * </pre>
 * and use <code>setZoom()</code> to change the zoom level.
 * </p>
 * <p>
 * Zooming is always around the point the mouse pointer is currently at, so that
 * this point (called a zooming center) remains stationary ensuring that the area
 * of an image we are zooming into does not disappear off the screen. The zooming center
 * stays at the same location on the screen and all other points move radially away from
 * it (when zooming in), or towards it (when zooming out). For programmatically
 * controlled zooming the zooming center is either specified when <code>setZoom()</code>
 * is called:
 * <pre>
 * panel.setZoom(newZoomLevel, newZoomingCenter);
 * </pre>
 * or assumed to be the point of an image which is
 * the closest to the center of the panel, if no zooming center is specified:
 * <pre>
 * panel.setZoom(newZoomLevel);
 * </pre>
 * </p>
 * <p>
 * There are no lower or upper zoom level limits.
 * </p>
 * <h3>Navigation</h3>
 * <p><code>NavigableImagePanel</code> does not use scroll bars for navigation,
 * but relies on a navigation image located in the upper left corner of the panel.
 * The navigation image is a small replica of the image displayed in the panel.
 * When you click on any point of the navigation image that part of the image
 * is displayed in the panel, centered. The navigation image can also be
 * zoomed in the same way as the main image.</p>
 * <p>In order to adjust the position of an image in the panel, it can be dragged
 * with the mouse, using the left button.
 * </p>
 * <p>For programmatic image navigation, disable the navigation image:
 * <pre>
 * panel.setNavigationImageEnabled(false)
 * </pre>
 * and use <code>getImageOrigin()</code> and
 * <code>setImageOrigin()</code> to move the image around the panel.
 * </p>
 * <h3>Rendering</h3>
 * <p><code>NavigableImagePanel</code> uses the Nearest Neighbor interpolation
 * for image rendering (default in Java).
 * When the scaled image becomes larger than the original image,
 * the Bilinear interpolation is applied, but only to the part
 * of the image which is displayed in the panel. This interpolation change threshold
 * can be controlled by adjusting the value of
 * <code>HIGH_QUALITY_RENDERING_SCALE_THRESHOLD</code>.</p>
 * @author Chantal Roth
 */
public class NavigableImagePanel extends JPanel implements MouseMotionListener {

    /**
     * <p>Identifies a change to the zoom level.</p>
     */
    public static final String ZOOM_LEVEL_CHANGED_PROPERTY = "zoomLevel";
    /**
     * <p>Identifies a change to the zoom increment.</p>
     */
    public static final String ZOOM_INCREMENT_CHANGED_PROPERTY = "zoomIncrement";
    /**
     * <p>Identifies that the image in the panel has changed.</p>
     */
    public static final String IMAGE_CHANGED_PROPERTY = "image";
    private static final double SCREEN_NAV_IMAGE_FACTOR = 0.15; // 15% of panel's width
    private static final double NAV_IMAGE_FACTOR = 0.3; // 30% of panel's width
    private static final double HIGH_QUALITY_RENDERING_SCALE_THRESHOLD = 1.0;
    private static final Object INTERPOLATION_TYPE =
            RenderingHints.VALUE_INTERPOLATION_BILINEAR;
    private double zoomIncrement = 0.2;
    private double zoomFactor = 1.0 + zoomIncrement;
    private double navZoomFactor = 1.0 + zoomIncrement;
    protected BufferedImage image;
    private BufferedImage navigationImage;
    private int navImageWidth;
    private Point zoomingCenter;
    private int navImageHeight;
    private double initialScale = 0.0;
    private double scale = 0.0;
    private double navScale = 0.0;
    private int originX = 0;
    private int originY = 0;
    private Point mousePosition;
    private Dimension previousPanelSize;
    private boolean navigationImageEnabled = true;
    private boolean draggingEnabled = true;
    private boolean selectionEnabled = true;
    private boolean highQualityRenderingEnabled = true;
    private WheelZoomDevice wheelZoomDevice = null;
    private ButtonZoomDevice buttonZoomDevice = null;
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
    private boolean wasdragged;

    public double imageToPanelX(double x) {
        return (x * scale) + originX;
    }

    public double getScale() {
        return scale;
    }

    public double imageToPanelY(double y) {
        return (y * scale) + originY;
    }

    /**
     * @return the draggingEnabled
     */
    public boolean isDraggingEnabled() {
        return draggingEnabled;
    }

    /**
     * @param draggingEnabled the draggingEnabled to set
     */
    public void setDraggingEnabled(boolean draggingEnabled) {
        this.draggingEnabled = draggingEnabled;
    }

    /**
     * @param selectionEnabled the selectionEnabled to set
     */
    public void setSelectionEnabled(boolean selectionEnabled) {
        this.selectionEnabled = selectionEnabled;
    }

    /**
     * @return the selectionEnabled
     */
    public boolean isSelectionEnabled() {
        return selectionEnabled;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        //p("Nav: mouse dragged");
        if (SwingUtilities.isRightMouseButton(e)
                && !isInNavigationImage(e.getPoint())) {
            Point p = e.getPoint();
            if (isDraggingEnabled()) {
                moveImage(p);
                wasdragged = true;
            }
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            Point p = e.getPoint();
            if (isInNavigationImage(p)) {
                return;
            }
            // record end point
            Graphics g = getGraphics();
            if (mousex > -1) {
                if (isSelectionEnabled()) {
                    drawXorRect(g, oldmousex, oldmousey, mousex - oldmousex, mousey - oldmousey);
                }
            }
            mousex = e.getX();
            mousey = e.getY();
            int dx = mousex - oldmousex;
            int dy = mousey - oldmousey;
            if (isSelectionEnabled()) {
                drawXorRect(g, oldmousex, oldmousey, dx, dy);
            }

        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        //we need the mouse position so that after zooming
        //that position of the image is maintained
        mousePosition = e.getPoint();
        // p("MouseMoved at "+mousePosition);
    }

    /**
     * @return the zoomingCenter
     */
    public Point getZoomingCenter() {
        return zoomingCenter;
    }

    /**
     * @param zoomingCenter the zoomingCenter to set
     */
    public void setZoomingCenter(Point zoomingCenter) {
        this.zoomingCenter = zoomingCenter;
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

        public int getMidY() {
            return (endPoint.getIntY() + startPoint.getIntY()) / 2;
        }

        public int getMidX() {
            return (endPoint.getIntX() + startPoint.getIntX()) / 2;
        }

        public int getDX() {
            return endPoint.getIntX() - startPoint.getIntX();
        }

        public int getDY() {
            return endPoint.getIntY() - startPoint.getIntY();
        }

        public Coords getMid() {
            return new Coords(getMidX(), getMidY());
        }
    }

    /**
     * <p>Defines zoom devices.</p>
     */
    public static class ZoomDevice {

        /**
         * <p>Identifies that the panel does not implement zooming,
         * but the component using the panel does (programmatic zooming method).</p>
         */
        public static final ZoomDevice NONE = new ZoomDevice("none");
        /**
         * <p>Identifies the left and right mouse buttons as the zooming device.</p>
         */
        public static final ZoomDevice MOUSE_BUTTON = new ZoomDevice("mouseButton");
        /**
         * <p>Identifies the mouse scroll wheel as the zooming device.</p>
         */
        public static final ZoomDevice MOUSE_WHEEL = new ZoomDevice("mouseWheel");
        private String zoomDevice;

        private ZoomDevice(String zoomDevice) {
            this.zoomDevice = zoomDevice;
        }

        public String toString() {
            return zoomDevice;
        }
    }

    //This class is required for high precision image coordinates translation.
    public class Coords {

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

        public double getDoubleX() {
            return x;
        }

        public double getDoubleY() {
            return y;
        }

        public String toString() {
            return "[Coords: x=" + x + ",y=" + y + "]";
        }

        private Point getPoint() {
            return new Point((int) x, (int) y);
        }
    }

    private class WheelZoomDevice implements MouseWheelListener {

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            Point p = e.getPoint();
           // p("MouseWheel moved");
            boolean zoomIn = (e.getWheelRotation() < 0);
            if (isInNavigationImage(p)) {
                if (zoomIn) {
                    navZoomFactor = 1.0 + zoomIncrement;
                } else {
                    navZoomFactor = 1.0 - zoomIncrement;
                }
                zoomNavigationImage();
            } else if (isInImage(p)) {
                if (zoomIn) {
                    zoomFactor = 1.0 + zoomIncrement;
                } else {
                    zoomFactor = 1.0 - zoomIncrement;
                }
                zoomImage();
            }
        }
    }

    private class ButtonZoomDevice extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            Point p = e.getPoint();
            if (SwingUtilities.isRightMouseButton(e)) {
                if (isInNavigationImage(p)) {
                    navZoomFactor = 1.0 - zoomIncrement;
                    zoomNavigationImage();
                } else if (isInImage(p)) {
                    zoomFactor = 1.0 - zoomIncrement;
                    zoomImage();
                }
            } else {
                if (isInNavigationImage(p)) {
                    navZoomFactor = 1.0 + zoomIncrement;
                    zoomNavigationImage();
                } else if (isInImage(p)) {
                    zoomFactor = 1.0 + zoomIncrement;
                    zoomImage();
                }
            }
        }
    }

    /**
     * <p>Creates a new navigable image panel with no default image and
     * the mouse scroll wheel as the zooming device.</p>
     */
    public NavigableImagePanel() {
        setOpaque(false);
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                if (scale > 0.0) {
                    if (isFullImageInPanel()) {
                        centerImage();
                    } else if (isImageEdgeInPanel()) {
                        scaleOrigin();
                    }
                    if (isNavigationImageEnabled()) {
                        createNavigationImage();
                    }
                    repaint();
                }
                previousPanelSize = getSize();
            }
        });

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (isInNavigationImage(e.getPoint())) {
                        Point p = e.getPoint();
                        displayImageAt(p);
                    }
//                    } else {
//                        Point p = e.getPoint();
//                        //      p("MousePressed at " + p);
//                        cur_selection = new Selection();
//                        cur_selection.startPoint = panelToImageCoords(p);
//
//                        oldmousex = e.getX();
//                        oldmousey = e.getY();
//                        dragx = oldmousex;
//                        dragy = oldmousey;
//                        endx = dragx;
//                        endy = dragy;
//
//                    }
                }

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (isInNavigationImage(e.getPoint())) {
                        Point p = e.getPoint();
                        displayImageAt(p);
                    } else {
//                        Point p = e.getPoint();
//                        //        p("MouseReleased at " + p);
//
//                        if (cur_selection == null) {
//                            return;
//                        }
//                        cur_selection.endPoint = panelToImageCoords(p);
//                        //       p("Got a selection now: " + cur_selection);
//
//                        if (mousex <= 0) {
//                            return;
//                        }
//                        endx = e.getX();
//                        endy = e.getY();
//                        int dx = Math.abs(endx - oldmousex);
//                        int dy = Math.abs(endy - oldmousey);
//                        if (isSelectionEnabled()) {
//                            Graphics g = getGraphics();
//                            drawXorRect(g, oldmousex, oldmousey, mousex - oldmousex, mousey - oldmousey);
//                            if (dx < 5 || dy < 5) {
//                                mousex = -1;
//                                return;
//                            }
//                            g.setPaintMode();
//                        }
//                        mousex = -1;
//                        repaint();
                    }
                }

            }
        });

        addMouseMotionListener(this);

        setZoomDevice(ZoomDevice.MOUSE_WHEEL);
      //  ZoomDevice.
    }

    private void drawXorRect(Graphics g, int sx, int sy, int dx, int dy) {
        drawXorRect(g, sx, sy, dx, dy, color_select);
    }

    private void drawXorRect(Graphics g, int sx, int sy, int dx, int dy, Color c) {

        int x1 = sx;
        int y1 = sy;

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

    private void p(String msg) {
        System.out.println("Navigable Image Panel: " + msg);
    }

    /**
     * <p>Creates a new navigable image panel with the specified image
     * and the mouse scroll wheel as the zooming device.</p>
     */
    public NavigableImagePanel(BufferedImage image) throws IOException {
        this();
        setImage(image);
    }

    private void addWheelZoomDevice() {
        if (wheelZoomDevice == null) {
            wheelZoomDevice = new WheelZoomDevice();
            addMouseWheelListener(wheelZoomDevice);
        }
    }

    private void addButtonZoomDevice() {
        if (buttonZoomDevice == null) {
            buttonZoomDevice = new ButtonZoomDevice();
            addMouseListener(buttonZoomDevice);
        }
    }

    private void removeWheelZoomDevice() {
        if (wheelZoomDevice != null) {
            removeMouseWheelListener(wheelZoomDevice);
            wheelZoomDevice = null;
        }
    }

    private void removeButtonZoomDevice() {
        if (buttonZoomDevice != null) {
            removeMouseListener(buttonZoomDevice);
            buttonZoomDevice = null;
        }
    }

    /**
     * <p>Sets a new zoom device.</p>
     *
     * @param newZoomDevice specifies the type of a new zoom device.
     */
    public void setZoomDevice(ZoomDevice newZoomDevice) {
        if (newZoomDevice == ZoomDevice.NONE) {
            removeWheelZoomDevice();
            removeButtonZoomDevice();
        } else if (newZoomDevice == ZoomDevice.MOUSE_BUTTON) {
            removeWheelZoomDevice();
            addButtonZoomDevice();
        } else if (newZoomDevice == ZoomDevice.MOUSE_WHEEL) {
            removeButtonZoomDevice();
            addWheelZoomDevice();
        }
    }

    /**
     * <p>Gets the current zoom device.</p>
     */
    public ZoomDevice getZoomDevice() {
        if (buttonZoomDevice != null) {
            return ZoomDevice.MOUSE_BUTTON;
        } else if (wheelZoomDevice != null) {
            return ZoomDevice.MOUSE_WHEEL;
        } else {
            return ZoomDevice.NONE;
        }
    }

    //Called from paintComponent() when a new image is set.
    private void initializeParams() {
        double xScale = (double) getWidth() / image.getWidth();
        double yScale = (double) getHeight() / image.getHeight();
        initialScale = Math.min(xScale, yScale);
        scale = initialScale;
        scale = 1.0;
        //An image is initially centered
        centerImage();
        if (isNavigationImageEnabled()) {
            createNavigationImage();
        }
    }

    //Centers the current image in the panel.
    private void centerImage() {
        originX = (getWidth() - getScreenImageWidth()) / 2;
        originY = (getHeight() - getScreenImageHeight()) / 2;
    }

    //Creates and renders the navigation image in the upper let corner of the panel.
    private void createNavigationImage() {
        //We keep the original navigation image larger than initially
        //displayed to allow for zooming into it without pixellation effect.
        navImageWidth = (int) (getWidth() * NAV_IMAGE_FACTOR);
        navImageHeight = navImageWidth * image.getHeight() / image.getWidth();
        int scrNavImageWidth = (int) (getWidth() * SCREEN_NAV_IMAGE_FACTOR);
        int scrNavImageHeight = scrNavImageWidth * image.getHeight() / image.getWidth();
        navScale = (double) scrNavImageWidth / navImageWidth;
        navigationImage = new BufferedImage(navImageWidth, navImageHeight,
                image.getType());
        Graphics g = navigationImage.getGraphics();
        g.drawImage(image, 0, 0, navImageWidth, navImageHeight, null);
    }

    /**
     * <p>Sets an image for display in the panel.</p>
     *
     * @param image an image to be set in the panel
     */
    public void setImage(BufferedImage image) {
        setImage(image, true);
    }

    public void setImage(BufferedImage image, boolean reset) {
        BufferedImage oldImage = this.image;
        this.image = image;
        //Reset scale so that initializeParameters() is called in paintComponent()
        //for the new image.
        if (reset) {
            scale = 0.0;
        }

        firePropertyChange(IMAGE_CHANGED_PROPERTY, (Image) oldImage, (Image) image);
        repaint();
    }

    /**
     * <p>Tests whether an image uses the standard RGB color space.</p>
     */
    public static boolean isStandardRGBImage(BufferedImage bImage) {
        return bImage.getColorModel().getColorSpace().isCS_sRGB();
    }

    //Converts this panel's coordinates into the original image coordinates
    public Coords panelToImageCoords(Point p) {
        if (p == null) {
            return null;
        }
        return new Coords((p.x - originX) / scale, (p.y - originY) / scale);
    }

    //Converts the original image coordinates into this panel's coordinates
    public Coords imageToPanelCoords(Coords p) {
        if (p == null) {
            return null;
        }
        return new Coords(imageToPanelX(p.x), imageToPanelY(p.y));
    }

    //Converts the navigation image coordinates into the zoomed image coordinates
    private Point navToZoomedImageCoords(Point p) {
        int x = p.x * getScreenImageWidth() / getScreenNavImageWidth();
        int y = p.y * getScreenImageHeight() / getScreenNavImageHeight();
        return new Point(x, y);
    }

    //The user clicked within the navigation image and this part of the image
    //is displayed in the panel.
    //The clicked point of the image is centered in the panel.
    private void displayImageAt(Point p) {
        Point scrImagePoint = navToZoomedImageCoords(p);
        originX = -(scrImagePoint.x - getWidth() / 2);
        originY = -(scrImagePoint.y - getHeight() / 2);
        repaint();
    }

     public void displayImageAtImage(Point scrImagePoint) {
        
        originX = -(scrImagePoint.x - getWidth() / 2);
        originY = -(scrImagePoint.y - getHeight() / 2);
        repaint();
    }
    //Tests whether a given point in the panel falls within the image boundaries.
    private boolean isInImage(Point p) {
        Coords coords = panelToImageCoords(p);
        int x = coords.getIntX();
        int y = coords.getIntY();
        return (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight());
    }

    //Tests whether a given point in the panel falls within the navigation image
    //boundaries.
    protected boolean isInNavigationImage(Point p) {
        return (isNavigationImageEnabled() && p.x < getScreenNavImageWidth()
                && p.y < getScreenNavImageHeight());
    }

    //Used when the image is resized.
    private boolean isImageEdgeInPanel() {
        if (previousPanelSize == null) {
            return false;
        }

        return (originX > 0 && originX < previousPanelSize.width
                || originY > 0 && originY < previousPanelSize.height);
    }

    //Tests whether the image is displayed in its entirety in the panel.
    private boolean isFullImageInPanel() {
        return (originX >= 0 && (originX + getScreenImageWidth()) < getWidth()
                && originY >= 0 && (originY + getScreenImageHeight()) < getHeight());
    }

    /**
     * <p>Indicates whether the high quality rendering feature is enabled.</p>
     *
     * @return true if high quality rendering is enabled, false otherwise.
     */
    public boolean isHighQualityRenderingEnabled() {
        return highQualityRenderingEnabled;
    }

    /**
     * <p>Enables/disables high quality rendering.</p>
     *
     * @param enabled enables/disables high quality rendering
     */
    public void setHighQualityRenderingEnabled(boolean enabled) {
        highQualityRenderingEnabled = enabled;
    }

    //High quality rendering kicks in when when a scaled image is larger
    //than the original image. In other words,
    //when image decimation stops and interpolation starts.
    private boolean isHighQualityRendering() {
        return (highQualityRenderingEnabled
                && scale > HIGH_QUALITY_RENDERING_SCALE_THRESHOLD);
    }

    /**
     * <p>Indicates whether navigation image is enabled.<p>
     *
     * @return true when navigation image is enabled, false otherwise.
     */
    public boolean isNavigationImageEnabled() {
        return navigationImageEnabled;
    }

    /**
     * <p>Enables/disables navigation with the navigation image.</p>
     * <p>Navigation image should be disabled when custom, programmatic navigation
     * is implemented.</p>
     *
     * @param enabled true when navigation image is enabled, false otherwise.
     */
    public void setNavigationImageEnabled(boolean enabled) {
        navigationImageEnabled = enabled;
        repaint();
    }

    //Used when the panel is resized
    private void scaleOrigin() {
        originX = originX * getWidth() / previousPanelSize.width;
        originY = originY * getHeight() / previousPanelSize.height;
        repaint();
    }

    //Converts the specified zoom level	to scale.
    private double zoomToScale(double zoom) {
        return initialScale * zoom;
    }

    /**
     * <p>Gets the current zoom level.</p>
     *
     * @return the current zoom level
     */
    public double getZoom() {
        return scale / initialScale;
    }

    /**
     * <p>Sets the zoom level used to display the image.</p>
     * <p>This method is used in programmatic zooming. The zooming center is
     * the point of the image closest to the center of the panel.
     * After a new zoom level is set the image is repainted.</p>
     *
     * @param newZoom the zoom level used to display this panel's image.
     */
    public void setZoom(double newZoom) {
        zoomingCenter = new Point(getWidth() / 2, getHeight() / 2);
        setZoom(newZoom, zoomingCenter);
    }

    /**
     * <p>Sets the zoom level used to display the image, and the zooming center,
     * around which zooming is done.</p>
     * <p>This method is used in programmatic zooming.
     * After a new zoom level is set the image is repainted.</p>
     *
     * @param newZoom the zoom level used to display this panel's image.
     */
    public void setZoom(double newZoom, Point zoomingCenter) {
        this.setZoomingCenter(zoomingCenter);
        Coords imageP = panelToImageCoords(zoomingCenter);
        if (imageP.x < 0.0) {
            imageP.x = 0.0;
        }
        if (imageP.y < 0.0) {
            imageP.y = 0.0;
        }
        if (imageP.x >= image.getWidth()) {
            imageP.x = image.getWidth() - 1.0;
        }
        if (imageP.y >= image.getHeight()) {
            imageP.y = image.getHeight() - 1.0;
        }

        Coords correctedP = imageToPanelCoords(imageP);
        double oldZoom = getZoom();
        scale = zoomToScale(newZoom);
        Coords panelP = imageToPanelCoords(imageP);

        originX += (correctedP.getIntX() - (int) panelP.x);
        originY += (correctedP.getIntY() - (int) panelP.y);

        firePropertyChange(ZOOM_LEVEL_CHANGED_PROPERTY, new Double(oldZoom),
                new Double(getZoom()));

        repaint();
    }

    /**
     * <p>Gets the current zoom increment.</p>
     *
     * @return the current zoom increment
     */
    public double getZoomIncrement() {
        return zoomIncrement;
    }

    /**
     * <p>Sets a new zoom increment value.</p>
     *
     * @param newZoomIncrement new zoom increment value
     */
    public void setZoomIncrement(double newZoomIncrement) {
        double oldZoomIncrement = zoomIncrement;
        zoomIncrement = newZoomIncrement;
        firePropertyChange(ZOOM_INCREMENT_CHANGED_PROPERTY,
                new Double(oldZoomIncrement), new Double(zoomIncrement));
    }

    //Zooms an image in the panel by repainting it at the new zoom level.
    //The current mouse position is the zooming center.
    private void zoomImage() {
        Coords imageP = panelToImageCoords(mousePosition);
        if (mousePosition == null) {
            return;
        }
        double oldZoom = getZoom();
        scale *= zoomFactor;
        Coords panelP = imageToPanelCoords(imageP);
        if (panelP == null) {
            return;
        }

        originX += (mousePosition.x - (int) panelP.x);
        originY += (mousePosition.y - (int) panelP.y);

        firePropertyChange(ZOOM_LEVEL_CHANGED_PROPERTY, new Double(oldZoom),
                new Double(getZoom()));

        repaint();
    }

    //Zooms the navigation image
    private void zoomNavigationImage() {
        navScale *= navZoomFactor;
        repaint();
    }

    /**
     * <p>Gets the image origin.</p>
     * <p>Image origin is defined as the upper, left corner of the image in
     * the panel's coordinate system.</p>
     * @return the point of the upper, left corner of the image in the panel's coordinates
     * system.
     */
    public Point getImageOrigin() {
        return new Point(originX, originY);
    }

    /**
     * <p>Sets the image origin.</p>
     * <p>Image origin is defined as the upper, left corner of the image in
     * the panel's coordinate system. After a new origin is set, the image is repainted.
     * This method is used for programmatic image navigation.</p>
     * @param x the x coordinate of the new image origin
     * @param y the y coordinate of the new image origin
     */
    public void setImageOrigin(int x, int y) {
        setImageOrigin(new Point(x, y));
    }

    /**
     * <p>Sets the image origin.</p>
     * <p>Image origin is defined as the upper, left corner of the image in
     * the panel's coordinate system. After a new origin is set, the image is repainted.
     * This method is used for programmatic image navigation.</p>
     * @param newOrigin the value of a new image origin
     */
    public void setImageOrigin(Point newOrigin) {
        originX = newOrigin.x;
        originY = newOrigin.y;
        repaint();
    }

    //Moves te image (by dragging with the mouse) to a new mouse position p.
    private void moveImage(Point p) {
        if (p == null || mousePosition == null) {
            return;
        }
        int xDelta = p.x - mousePosition.x;
        int yDelta = p.y - mousePosition.y;
        originX += xDelta;
        originY += yDelta;
        mousePosition = p;
        repaint();
    }

//Moves te image (by dragging with the mouse) to a new mouse position p.
    private void centerImageAround(Coords imagep) {
        originX = (int) (getWidth() / 2 - imagep.getIntX() * scale);
        originY = (int) (getHeight() / 2 - imagep.getIntY() * scale);

        repaint();
    }

    //Gets the bounds of the image area currently displayed in the panel (in image
    //coordinates).
    private Rectangle getImageClipBounds() {
        Coords startCoords = panelToImageCoords(new Point(0, 0));
        Coords endCoords = panelToImageCoords(new Point(getWidth() - 1, getHeight() - 1));
        int panelX1 = startCoords.getIntX();
        int panelY1 = startCoords.getIntY();
        int panelX2 = endCoords.getIntX();
        int panelY2 = endCoords.getIntY();
        //No intersection?
        if (panelX1 >= image.getWidth() || panelX2 < 0 || panelY1 >= image.getHeight() || panelY2 < 0) {
            return null;
        }

        int x1 = (panelX1 < 0) ? 0 : panelX1;
        int y1 = (panelY1 < 0) ? 0 : panelY1;
        int x2 = (panelX2 >= image.getWidth()) ? image.getWidth() - 1 : panelX2;
        int y2 = (panelY2 >= image.getHeight()) ? image.getHeight() - 1 : panelY2;
        return new Rectangle(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
    }

    /**
     * Paints the panel and its image at the current zoom level, location, and
     * interpolation method dependent on the image scale.</p>
     *
     * @param g the <code>Graphics</code> context for painting
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Paints the background

        if (image == null) {
            return;
        }

        if (scale == 0.0) {
            initializeParams();
        }

        if (isHighQualityRendering()) {
            Rectangle rect = getImageClipBounds();
            if (rect == null || rect.width == 0 || rect.height == 0) { // no part of image is displayed in the panel
                return;
            }

            BufferedImage subimage = image.getSubimage(rect.x, rect.y, rect.width,
                    rect.height);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, INTERPOLATION_TYPE);
            g2.drawImage(subimage, Math.max(0, originX), Math.max(0, originY),
                    Math.min((int) (subimage.getWidth() * scale), getWidth()),
                    Math.min((int) (subimage.getHeight() * scale), getHeight()), null);
        } else {
            g.drawImage(image, originX, originY, getScreenImageWidth(),
                    getScreenImageHeight(), null);
        }

        //Draw navigation image
        if (isNavigationImageEnabled()) {
            g.drawImage(navigationImage, 0, 0, getScreenNavImageWidth(),
                    getScreenNavImageHeight(), null);
            drawZoomAreaOutline(g);
        }

        // draw Selection
        if (isSelectionEnabled()) {
            if (cur_selection != null && cur_selection.endPoint != null) {
                Coords start = this.imageToPanelCoords(cur_selection.startPoint);
                Coords end = this.imageToPanelCoords(cur_selection.endPoint);
                g.setColor(color_select);
                int dy = (int) end.y - (int) start.y;
                int dx = (int) end.x - (int) start.x;

                for (int i = -1; i < 1; i++) {
                    this.drawXorRect(g, (int) start.x - i, (int) start.y - i, dx + 2 * i, dy + 2 * i);
                }
                // g.drawRect((int) start.x, (int) start.y, (int) end.x - (int) start.x, (int) end.y - (int) start.y);
            }
        }
    }

    public void setSelection(Coords start, Coords end) {
        cur_selection = new Selection();
        cur_selection.startPoint = start;
        cur_selection.endPoint = end;
       // p("Got selection "+start+"/"+end);
        showSelection();
    }

    public void showSelection() {
        if (!this.isSelectionEnabled()) {
            return;
        }
        if (cur_selection != null && cur_selection.endPoint != null && this.scale > 0) {

            this.repaint();
        }
    }
    //Paints a white outline over the navigation image indicating
    //the area of the image currently displayed in the panel.

    private void drawZoomAreaOutline(Graphics g) {
        if (isFullImageInPanel()) {
            return;
        }
        if (getScreenImageWidth() <= 0) {
            return;
        }
        if (getScreenImageHeight() <= 0) {
            return;
        }
        int x = -originX * getScreenNavImageWidth() / getScreenImageWidth();
        int y = -originY * getScreenNavImageHeight() / getScreenImageHeight();
        int width = getWidth() * getScreenNavImageWidth() / getScreenImageWidth();
        int height = getHeight() * getScreenNavImageHeight() / getScreenImageHeight();
        g.setColor(Color.white);
        g.drawRect(x, y, width, height);
    }

    private int getScreenImageWidth() {
        return (int) (scale * image.getWidth());
    }

    private int getScreenImageHeight() {
        return (int) (scale * image.getHeight());
    }

    private int getScreenNavImageWidth() {
        return (int) (navScale * navImageWidth);
    }

    private int getScreenNavImageHeight() {
        return (int) (navScale * navImageHeight);
    }

    private static String[] getImageFormatExtensions() {
        String[] names = ImageIO.getReaderFormatNames();
        for (int i = 0; i < names.length; i++) {
            names[i] = names[i].toLowerCase();
        }
        Arrays.sort(names);
        return names;
    }

    private static boolean endsWithImageFormatExtension(String name) {
        int dotIndex = name.lastIndexOf(".");
        if (dotIndex == -1) {
            return false;
        }

        String extension = name.substring(dotIndex + 1).toLowerCase();
        return (Arrays.binarySearch(getImageFormatExtensions(), extension) >= 0);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java NavigableImagePanel imageFilename");

        }

        final String filename = args[0];

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                final JFrame frame = new JFrame("Navigable Image Panel");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                NavigableImagePanel panel = new NavigableImagePanel();
                try {
                    final BufferedImage image = ImageIO.read(new File(filename));
                    panel.setImage(image);
                } catch (IOException e) {
                    //  JOptionPane.showMessageDialog(null, e.getMessage(), "",
                    //        JOptionPane.ERROR_MESSAGE);
                }

                frame.getContentPane().add(panel, BorderLayout.CENTER);

                GraphicsEnvironment ge =
                        GraphicsEnvironment.getLocalGraphicsEnvironment();
                Rectangle bounds = ge.getMaximumWindowBounds();
                frame.setSize(new Dimension(bounds.width, bounds.height));
                frame.setVisible(true);
            }
        });
    }

    private void err(String msg, Exception ex) {
        Logger.getLogger(NavigableImagePanel.class.getName()).log(Level.SEVERE, msg, ex);
    }
}
