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
package com.iontorrent.guiutils.zoomcanvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public abstract class GuiObject implements Drawable, MouseListener, MouseMotionListener {

    private static boolean DEBUG = true;
    protected Object object = null;
    private boolean isstatic = false;
    protected boolean movable = false;
    //private ArrayList<Drawable> actions;
    protected boolean collapsed = false;
    protected static String htmlstart = "<html><font color=blue size=-1>";
    protected static String htmlend = "</font></html>";
    protected static String nl = "<br>";
    private Rectangle bounds = null;
    //protected FeatureInfo info = null;
    protected boolean error = false;
    private Object source = null;
    protected boolean visible = true;
    protected int zoomfactor = 100;
    private int layer = 0;
    protected Color backgroundColor = Color.white;
    protected Color foregroundColor = Color.white;
    protected Color highlightColor = Color.black;
    protected Dimension absoluteSize = new Dimension(0, 0);
    protected Dimension viewSize = new Dimension(0, 0);
    protected Point absolutePosition = new Point(0, 0);
    private String name;
    protected boolean selectable = true;
    protected Rectangle viewrect = null;
    protected boolean selected = false;
    protected Point origin = new Point(0, 0);
    protected boolean highlighted = false;
    //protected DrawingCanvas canvas;
    private Drawable d;
    protected ArrayList<Drawable> ds;
    private boolean invalidpos = true;
    private boolean invalidsize = true;
    private int x = -1;
    private int y = -1;
    private int w = -1;
    private int h = -1;
    private static HashMap visible_map = new HashMap();
    private GuiCanvas canvas;

    // ***************************************************************************
    // CONSTRUCTOR
    // ***************************************************************************
    public GuiCanvas getCanvas() {
        return canvas;
    }

    public void setCanvas(GuiCanvas canvas) {
        this.canvas = canvas;
    }

    public GuiObject(Point origin) {
        this.origin = origin;
    }

    // ***************************************************************************
    //GET SET
    // ***************************************************************************
    /** sometimes you need to remember where a gui object came from, for instance
     *  from a certain chromosome slot, or a canvas etc
     */
    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public void setDrawable(Drawable d) {
        setLink(d);
    }

    public Drawable getDrawable() {
        if (ds == null || ds.size() < 1) {
            return null;
        } else {
            return (Drawable) ds.get(0);
        }

    }

    public void addDrawable(Drawable d) {
        addLink(d);
    }

    public void addLink(Drawable d) {
        if (ds == null) {
            ds = new ArrayList<Drawable>();
        }
        ds.add(d);
    }

    public void setLink(Drawable d) {
        ds = new ArrayList<Drawable>();
        addLink(d);
    }

    public double getCurrentXZoomFactor() {
        if (canvas == null) {
            return 1.0;
        }
        return canvas.getFactorX();
    }

    public double getCurrentYZoomFactor() {
        if (canvas == null) {
            return 1.0;
        }
        return canvas.getFactorY();
    }

    public boolean isError() {
        return error;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean c) {
        this.collapsed = c;
    }

    public void setBaseObject(Object object) {
        this.object = object;
    }

    public Object getBaseObject() {
        return object;
    }

    public void setStatic(boolean s) {
        this.isstatic = s;
    }

    public boolean isStatic() {
        return isstatic;
    }

    // ***************************************************************************
    // ABSTRACT; FROM DRAWABLE
    // ***************************************************************************
    /** draw the graphics component
     */
    public abstract void draw(Graphics g);

    public void clear(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(getX(), getY(), getWidth(), getHeight());
    }

    public String getToolTipText(MouseEvent evt) {
        //Added by Rita
        if (!isVisible()) {
            return null;
        }

        return htmlstart + toHtml(evt) + "@" + evt.getX() + "/" + evt.getY() + htmlend;
    }

    public String toHtml(MouseEvent evt) {
        return toHtml();
    }

    public String toHtml() {
        return "not implemented in " + this.getClass().getName();
    }

    public boolean isText() {
        return false;
    }

    public boolean drawAbove() {
        return isForward();
    }

    public String getId() {
        return null;
    }

    public void drawBounds(Graphics g2) {
        g2.setColor(Color.green);
        Rectangle r = getBounds();
        g2.drawRect(r.x, r.y, r.width, r.height);
    }

    // ***************************************************************************
    // MOUSE STUFF
    // ***************************************************************************
    public void mouseClicked(MouseEvent e) {
        //p("mouse clicked");
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    // ***************************************************************************
    // IMPLEMENTED; FROM DRAWABLE
    // ***************************************************************************
    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean high) {
        this.highlighted = high;
    }

    public boolean isMovable() {
        return movable;
    }

    public void setMovable(boolean b) {
        this.movable = b;
    }

    public void setSelectable(boolean b) {
        this.selectable = b;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public boolean isForward() {
        return true;
    }

    public boolean isClassVisible() {
        Boolean B = (Boolean) visible_map.get(getClass().getName());
        if (B == null || B.booleanValue()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isClassVisible(Class clazz) {
        Boolean B = (Boolean) visible_map.get(clazz.getName());
        if (B == null || B.booleanValue()) {
            return true;
        } else {
            return false;
        }
    }

    public void setClassVisible(boolean b) {
        if (visible_map == null) {
            visible_map = new HashMap();
        }
        visible_map.put(getClass().getName(), new Boolean(b));
    }

    public static void setClassVisible(Class c, boolean b) {
        if (visible_map == null) {
            visible_map = new HashMap();
        }
        visible_map.put(c.getName(), new Boolean(b));
    }

    public boolean isVisible() {
        return visible && isClassVisible();
    }

    public void setVisible(boolean vis) {

        this.visible = vis;

    }

    /** returns true, if the point p (a absolute position) is contained in this object (determinded
     * using the absolute position and absolute size
     */
    public boolean containsPoint(Point p) {
        if (getBounds() == null) {
            return false;
        }
        return getBounds().contains(p);
    }

    /** returns true, if the point p (a absolute position) is contained in this object (determinded
     * using the absolute position and absolute size
     */
    public boolean contains(int px, int py) {
        if (getBounds() == null) {
            return false;
        }
        return getBounds().contains(px, py);
    }

    private boolean contains(Point p) {
        return containsPoint(p);
    }

    /** returns true, if the drawable draw overlaps with this drawable.
     */
    public boolean overlaps(Drawable draw) {
        if (getBounds() == null) {
            return false;
        } else {
            return getBounds().intersects(draw.getBounds());
        }
    }

    /** returns true, if the drawable draw overlaps with this drawable.
     */
    public boolean overlaps(Rectangle rect) {
        return getBounds().intersects(rect);
    }
    // ***************************************************************************
    // GET/SET
    // ***************************************************************************

    /** set the rectangle that is currently viewed */
    protected void setViewRect(Rectangle rect) {
        this.viewrect = rect;
    }

    /** get the currently viewed rectangle */
    public Rectangle getViewRect() {
        return viewrect;
    }

    public void setSelected(boolean b) {
        setSelected(b, true);
    }

    /** returs true if the current object is selected */
    public boolean isSelected() {
        return selected;
    }

    public int getX() {
        if (invalidpos) {
            updateBounds();
        }
        return (int) getAbsolutePosition().getX();
    }

    public int getY() {
        if (invalidpos) {
            updateBounds();
        }
        return (int) getAbsolutePosition().getY();
    }

    public void setHeight(int h) {
        int w = getWidth();
        setAbsoluteSize(new Dimension(w, h));
    }

    public void setWidth(int w) {
        int h = getHeight();
        setAbsoluteSize(new Dimension(w, h));
    }

    public Dimension getAbsoluteSize() {
        return absoluteSize;
    }

    /** Calculates the actual viewing size as a function of the zoomfactor and
     * absoluteSize and sets the viewSize accordingly */
    public void setAbsoluteSize(Dimension size) {
        absoluteSize = size;
        bounds = null;
        invalidsize = true;

    }

    public Point getAbsolutePosition() {
        return absolutePosition;
    }

    public void rotate90(Point m) {
        rotate90(true, m);
    }

    public void rotate180(Point m) {
        double x = 2 * m.getX() - getX();
        double y = 2 * m.getY() - getY();
        setAbsolutePosition(new Point((int) x, (int) y));
    }

    public void rotate90(boolean plus, Point m) {
        setAbsoluteSize(new Dimension(getHeight(), getWidth()));

        double x = 0;
        double y = 0;
        if (plus) {
            x = 2 * m.getY() - getY();
            y = getX();
        } else {
            x = getY();
            y = 2 * m.getX() - getX();
        }

        setAbsolutePosition(new Point((int) x, (int) y));
    }

    // ***************************************************************************
    // NEW MOVEMENT/POSITIONS
    // ***************************************************************************
    public void setPosition(Point p) {
        setAbsolutePosition(p);
    }

    public Point getPosition() {
        return getAbsolutePosition();
    }

    public Dimension getSize() {
        return getAbsoluteSize();
    }

    public void setSize(Dimension d) {
        setAbsoluteSize(d);
    }

    public Rectangle getBounds() {
        if (bounds == null) {
            updateBounds();
        }
        return bounds;
    }

    public void updateBounds() {
        if (getPosition() == null) {
            err("No position for guiobject " + toString());
            bounds = null;;
            invalidpos = true;
        } else if (getSize() == null) {
            err("No size for guiobject " + toString());
            bounds = null;
            invalidsize = true;
        } else {
            bounds = new Rectangle(getPosition(), getSize());
            x = (int) bounds.getX();
            y = (int) bounds.getY();
            w = (int) bounds.getWidth();
            h = (int) bounds.getHeight();
            invalidpos = false;
            invalidsize = false;
        }
    }

    // ***************************************************************************
    // MOVEMENT/POSITIONS
    // ***************************************************************************
    public void move(int dx, int dy) {
        setAbsolutePosition(new Point(getX() + dx, getY() + dy));
    }

    public int getWidth() {
        if (invalidsize) {
            updateBounds();
        }
        return w;
    }

    public int getHeight() {
        if (invalidsize) {
            updateBounds();
        }
        return h;
    }

    public void moveTo(int x, int y) {

        setAbsolutePosition(new Point(x, y));
    }

    public void setLocation(Point p) {
        setAbsolutePosition(p);
    }

    /** Calculates the actual viewing position */
    public void setAbsolutePosition(Point p) {
        this.absolutePosition = p;
        bounds = null;
        invalidpos = true;
    }

    public void setOrigin(Point p) {
        origin = p;
    }

    public Point getOrigin() {
        return origin;
    }

    public Color getForeground() {
        return foregroundColor;
    }

    public Color getHighlight() {
        return highlightColor;
    }

    public Color getBackground() {
        return backgroundColor;
    }

    public void setForeground(Color c) {
        foregroundColor = c;
    }

    public void setBackground(Color c) {
        backgroundColor = c;
    }

    public Dimension getViewSize() {
        return viewSize;
    }

    // *****************************************************************
    // HELPER METHODS
    // *****************************************************************
    /** This method should be overwritten for any specific instance */
    public String toString() {
        return name;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int i) {
        layer = i;
    }

    @Override
    public void setSelected(boolean b, boolean sendevent) {
        this.selected = b;

    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(GuiObject.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(GuiObject.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(GuiObject.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("GuiObject: " + msg);
        //Logger.getLogger( GuiObject.class.getName()).log(Level.INFO, msg, ex);
    }
    public ArrayList<Drawable> getDrawables() {
        return null;
    }
}
