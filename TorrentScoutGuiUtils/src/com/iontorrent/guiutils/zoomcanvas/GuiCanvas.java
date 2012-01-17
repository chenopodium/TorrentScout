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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JColorChooser;
import javax.swing.JPopupMenu;


/**
 * This is a general class that can draw a list of drawables. These drawables can be zoomable objects
 * (default), or text where only the location is adjusted automatically but not the size,
 * or static text objects that change neither location or size (like labels for instance)
 * The class uses Java 2D transformations to zoom the canvas in and out.
 * @Author Chantal Roth
 */
public class GuiCanvas
        extends BasicCanvas
        implements MouseListener, MouseMotionListener {

    private static boolean DEBUG = true;
    public static int NONE = 0;
    public static int ZOOMX = 1;
    public static int ZOOMY = 2;
    public static int ZOOMXY = 0;
    protected JPopupMenu popup = new JPopupMenu("popup");
    private int popup_x = 0;
    private int popup_y = 0;
    private Point origin = new Point(0, 0);
    private Point trans = new Point(0, 0);
    // TEST ONLY
    private Color color_fore = Color.blue;
    private Color color_back = Color.white;
//	private Image offscreenImage; // Place to hold Image
//	private Graphics offscreenGraphics;
    private BufferedImage buffer;
    private boolean BUFFERED = false;
    private double factorx = 1;
    private double factory = 1;
    private double oldfactorx = 1;
    private double oldfactory = 1;
    private ArrayList<Drawable> drawables = new ArrayList<Drawable>();
    private ArrayList<Drawable> texts = new ArrayList<Drawable>();
    private ArrayList<Drawable> statics = new ArrayList<Drawable>();
    private double width;
    private double height;
    private double startingwidth;
    private double startingheight;
    private double mx;
    private double my;
    private boolean onlyone = false;
    // private Image image;
    protected Drawable selected = null;
    protected int zoom_direction;
    protected boolean zoom_x = true;
    protected boolean zoom_y = true;
    private Image offscreen;
    private int font_height;
    private int font_width;
    private int font_base_line;
    private Font font;
    private boolean paintit = true;
    private ArrayList<Drawable> selectedlist = new ArrayList<Drawable>();
    private ArrayList<Drawable> movables = new ArrayList<Drawable>();
    private ArrayList<Drawable> selectables = new ArrayList<Drawable>();
    private ArrayList<Drawable> selectable_text = new ArrayList<Drawable>();
    private ArrayList<Drawable> simple_drawables = new ArrayList<Drawable>();
    private ArrayList<Drawable> comps = null;
    private Image backGroundImage_ = null;
    private boolean MULTIUPDATE = false;

// *****************************************************************
// CONSTRUCTOR
// *****************************************************************
    public GuiCanvas() {
        this(null, null, 0, 0, true, true);
    }

    public GuiCanvas(ArrayList<Drawable> drawables, ArrayList<Drawable> texts, double width, double height,
            boolean zoom_x, boolean zoom_y) {
        super();
        setBackground(Color.white);
        this.zoom_x = zoom_x;
        this.zoom_y = zoom_y;
        setStartingSize(width, height);
        setLayout(null);
        this.drawables = drawables;
        this.texts = texts;
        setToolTipText("dummy");
        Font font = new Font("Courier", Font.PLAIN, 12);
        setFont(font);
        setFontInfo();
        clear();

    }

    public GuiCanvas(double width, double height) {
        this(new ArrayList<Drawable>(), new ArrayList<Drawable>(), width, height, true, true);
    }

    public boolean isMultiUpdate() {
        return MULTIUPDATE;
    }

// *****************************************************************
// POPUP
// *****************************************************************
    public int getPopupX() {
        return popup_x;
    }

    public int getPopupY() {
        return popup_y;
    }

    public void setPopup(JPopupMenu popup) {
        this.popup = popup;
    }

    public PopupListener createPopupListener() {
        return new PopupListener();
    }

    public class PopupListener
            extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
            requestFocus();
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (!e.isPopupTrigger()) {
                return;
            }
            Drawable d = getDrawableAt(e);
            JPopupMenu pop = null;
            if (d != null && d instanceof GuiObject) {
                GuiObject g = (GuiObject) d;

            } else {
                pop = popup;
            }
            if (pop != null) {
                pop.show(e.getComponent(),
                        e.getX(), e.getY());
                popup_x = e.getX();
                popup_y = e.getY();
            }
        }
    }

// *****************************************************************
// TOOL TIPS
// *****************************************************************
    public String getToolTipText(MouseEvent e) {
        Point p = e.getPoint();

        Drawable d = getDrawableAt(p.getX(), p.getY());

        int x = (int) (e.getX() / factorx);
        int y = (int) (e.getY() / factory);

//	 public MouseEvent(Component source, int id, long when, int modifiers,
//             int x, int y, int clickCount, boolean popupTrigger,
//             int button)
//	 
        MouseEvent evt = new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(),
                x, y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
        if (d != null) {
            return d.getToolTipText(evt);
        } // TEST XXX
        else {
            String msg = "";
//		for (int i = 0; i < selectables.size(); i++) {
//			d = (Drawable) selectables.get(i);
//			if (d instanceof GuiObject) {
//				GuiObject g= (GuiObject)d;
//				msg += g.getClass().getName()+" @ "+g.getAbsolutePosition()+", "+g.getAbsoluteSize()+"\n";			
//			}
//		}
            return "no obj@ " + x + "/" + y + msg;
        }
    }

    protected void setFontInfo() {
        FontMetrics fm = getFontMetrics(getFont());
        int font_widths[] = fm.getWidths();
        font_width = -1;
        for (int i = 0; i < font_widths.length; i++) {
            if (font_widths[i] > font_width) {
                font_width = font_widths[i];

            }
        }
        font_height = fm.getHeight();
        font_base_line = fm.getMaxAscent();
    }

// *****************************************************************
// GET / SET
// *****************************************************************
    public void setOnlyOne(boolean b) {
        this.onlyone = b;
    }

    public boolean isOnlyOne() {
        return onlyone;
    }

    public ArrayList<Drawable> getTexts() {
        return texts;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font f) {
        this.font = f;
        setFontInfo();
    }

    public void removeText(Text d) {
        if (texts != null) {
            texts.remove(d);
        }
        if (selectables != null) {
            selectables.remove(d);
        }
    }

    public int getFontWidth() {
        return font_width;
    }

    public int getFontHeight() {
        return font_height;
    }

    public int getFontBaseLine() {
        return font_base_line;
    }

    public void setOrigin(Point p) {
        this.origin = p;
    }

    public Point getOrigin() {
        return origin;
    }

    public void setTranslate(Point p) {
        this.trans = p;
    }

    public Point getTranslate() {
        return trans;
    }

    public void setZoomX(boolean zoom_x) {
        this.zoom_x = zoom_x;
    }

    public void setZoomY(boolean zoom_y) {
        this.zoom_y = zoom_y;
    }

    public void setStartingSize(double width, double height) {
        this.startingwidth = width;
        this.startingheight = height;
        mx = startingwidth / 2;
        my = startingheight / 2;
        setSize(width, height);
    }

    public double getMiddleX() {
        return mx;
    }

    public double getMiddleY() {
        return my;
    }

    public Dimension getStartingSize() {
        return new Dimension((int) startingwidth, (int) startingheight);
    }

    public double getStartingWidth() {
        return startingwidth;
    }

    public double getStartingHeight() {
        return startingheight;
    }

    public void setSize(Dimension d) {
        //	System.out.println("size is:"+getSize()+", setsize called: "+d);
        //	Exception e = new Exception();
        //	e.printStackTrace();
        super.setSize(d);
        setSize(d.getWidth(), d.getHeight());
    }

    public Dimension getSize() {
        return new Dimension((int) width, (int) height);
    }

    public void setPaint(boolean paint) {
        this.paintit = paint;
    }

    public void setSize(double width, double height) {
        // calculate zoom factor
        oldfactorx = factorx;
        oldfactory = factory;
        if (zoom_x) {
            factorx = width / startingwidth;
        }
        if (zoom_y) {
            factory = height / startingheight;
        }
        this.width = width;
        this.height = height;
        //	p("Size is "+startingwidth+"/"+startingheight);
        //	p("zoom ="+factorx+"/"+factory);
    }

    public double getFactorX() {
        return factorx;
    }

    public double getFactorY() {
        return factory;
    }

// *****************************************************************
// DOING STUFF TO DRAWABLES
// *****************************************************************
    public void moveSelected(double userdx, double userdy) {
        //	pp("***** MOVING "+userdx+"/"+userdy);
        //	p("userx, usery:"+userdx+"/"+userdy);
        int dx = (int) (userdx / factorx);
        int dy = (int) (userdy / factory);
        //	p("Moving selected by "+dx +"/"+dy);
        for (int i = 0; selectedlist != null && i < selectedlist.size(); i++) {
            Drawable d = (Drawable) selectedlist.get(i);
            // pp("moving drawable " + d);
            if (!d.isSelected()) {
                err("drawable " + d + " is not really selected");
                selectedlist.remove(d);
                i--;
            } else {
                if (d.isMovable() && d.isSelectable()) {
                    // pp("moving "+d.getClass().getName()+" by "+dx+"/"+dy);
                    d.move(dx, dy);
                } else {
                    p(d.getClass().getName() + " is not movable");
                }
            }
            //draw(d);
        }
        draw();
    }

// *****************************************************************
// SELECTING DRAWABLE
// *****************************************************************
    public void toggleSelection(Drawable d) {
        toggleSelection(d, true);
    }

    public void toggleSelection(Drawable d, boolean sendevent) {
        p("toggleSelection on " + d);
        if (!d.isSelected()) {
            select(d, sendevent);
        } else {
            unselect(d, sendevent);
        }
    }

    public void select(Drawable d) {
        select(d, true);
    }

    public void select(Drawable d, boolean sendevent) {
        d.setSelected(true, sendevent);
        draw(d);
        if (!selectedlist.contains(d)) {
            selectedlist.add(d);
        }
        ArrayList<Drawable> list = d.getDrawables();
        for (int i = 0; list != null && i < list.size(); i++) {
            Drawable conn = (Drawable) list.get(i);
            conn.setSelected(true, sendevent);
            if (!selectedlist.contains(conn)) {
                selectedlist.add(conn);
            }
            draw(conn);
        }
    }

    public void unselect(Drawable d) {
        unselect(d, true);
    }

    public void unselect(Drawable d, boolean sendevent) {
        d.setSelected(false, sendevent);
        draw(d);
        selectedlist.remove(d);
        ArrayList<Drawable> list = d.getDrawables();
        for (int i = 0; list != null && i < list.size(); i++) {
            Drawable conn = (Drawable) list.get(i);
            conn.setSelected(false, sendevent);
            selectedlist.remove(conn);
            draw(conn);
        }
    }

    public Drawable getSelectedDrawableAt(double x, double y) {
        return getSelectedDrawableAt(x, y, false);
    }

    public Drawable getSelectedDrawableAt(double x, double y, boolean shift) {
        Drawable sel = getDrawableAt(x, y);
        if (sel != null && sel.isSelected()) {
            toggleSelection(sel);
            //    else p("unselectExcept: shift pressed");
        }
        return sel;
    }

    public void unselectExcept(Drawable sel, boolean shift) {
        //	p("***** unselectExcept "+sel+", got "+selectables.size()+" selectables");
        if (!shift) {
            selectedlist = new ArrayList<Drawable>();
            //		p("unselectExcept: shift NOT pressed");
            MULTIUPDATE = true;
            for (int i = 0; i < selectables.size(); i++) {
                Drawable d = (Drawable) selectables.get(i);

                if (d.isSelected() && !(d.isText())) {
                    //  p(d.toString() + " is selected");
                    if (d != sel) {
                        d.setSelected(false);
                        //	p("unselecting "+d+", sel is "+sel);
                        draw(d);
                    }
                    ArrayList<Drawable> list = d.getDrawables();
                    for (int j = 0; list != null && j < list.size(); j++) {
                        Drawable conn = (Drawable) list.get(j);
                        if (conn.isSelected()) {
                            conn.setSelected(false, false);
                            draw(d);
                            //			p("unselecting  connected one "+conn);
                        }
                    }
                }

                //	else p(d.toString()+" is NOT selected");
                if (i + 2 >= selectables.size()) {
                    MULTIUPDATE = false;
                }
            }
            MULTIUPDATE = false;
        }
        if (sel == null) {
            return;
        }
        toggleSelection(sel);
        repaint();
    }

    public void unselectAll() {
        MULTIUPDATE = true;
        for (int i = 0; selectedlist != null && i < selectedlist.size(); i++) {
            Drawable d = (Drawable) selectedlist.get(i);
            if (d.isSelected()) {
                ArrayList<Drawable> list = d.getDrawables();
                for (int j = 0; list != null && j < list.size(); j++) {
                    Drawable conn = (Drawable) list.get(i);
                    conn.setSelected(false, false);
                }
                d.setSelected(false, false);
            }
            if (i + 2 >= selectedlist.size()) {
                MULTIUPDATE = false;
            }
        }
        selectedlist = new ArrayList<Drawable>();
        MULTIUPDATE = false;
    }

    public Point userToWorld(Point p) {
        double x = p.getX();
        double y = p.getY();
        x = x - origin.getX();
        y = y - origin.getY();
        x = x / factorx;
        y = y / factory;
        x = x + origin.getX();
        y = y + origin.getY();
        return new Point((int) x, (int) y);
    }

    public Drawable getDrawableAt(MouseEvent e) {
        return getDrawableAt((double) e.getX(), (double) e.getY());
    }

    public ArrayList<Drawable> getDrawablesAt(Rectangle rect) {
        double x = Math.min(rect.getX(), rect.getX() + rect.getWidth());
        double y = Math.min(rect.getY(), rect.getY() + rect.getHeight());
        double w = Math.abs(rect.getWidth());
        double h = Math.abs(rect.getHeight());

        Point p = userToWorld(new Point((int) x, (int) y));
        Dimension dim = new Dimension((int) (w / factorx), (int) (h / factory));
        Rectangle wrect = new Rectangle(p, dim);

        ArrayList<Drawable> res = new ArrayList<Drawable>();
        for (int i = 0; i < drawables.size(); i++) {
            Drawable d = (Drawable) drawables.get(i);
            if (d.overlaps(wrect)) {
                //			p("is Drawable "+d.toString()+" overlaps with rect "+wrect.toString());
                res.add(d);
            }
        }
        for (int i = 0; i < selectables.size(); i++) {
            Drawable d = (Drawable) selectables.get(i);
            if (d instanceof Text) {
                if (d.overlaps(wrect)) {
                    //				p("is Text "+d.toString()+" overlaps with rect "+wrect.toString());
                    res.add(d);
                }
            }
        }
        return res;
    }

    public boolean overlaps(Drawable d) {

        p("No quad!");
        ArrayList<Drawable> v = this.getDrawablesAt(d.getBounds());
        if (v == null || v.size() < 1) {
            return false;
        } else {
            return true;
        }

    }

    public Drawable getDrawableAt(double x, double y) {
        return getDrawableAtExcept(x, y, null, true);
    }

    public Drawable getDrawableAtExcept(double x, double y, Class clazz,
            boolean text) {
        Point p = userToWorld(new Point((int) x, (int) y));

        ArrayList<Drawable> res = new ArrayList<Drawable>();
        if (selectables == null || selectables.size() < 1) {
            return null;
        }
        for (int i = 0; i < selectables.size(); i++) {
            Drawable d = (Drawable) selectables.get(i);
            if (d.isText() || (d instanceof Text)) {
                int x1 = d.getX();
                double h = d.getHeight() / factory * 1.5;
                int y1 = (int) (d.getY());
                y1 = (int) (y1 - h);
                int x2 = (int) (x1 + d.getWidth() / factorx);
                int y2 = (int) (y1 + h);
                //p("----------- is text "+d+" at "+p+"?");
                //	p("factory:"+factory+", new height:"+d.getHeight()/factory);

                if (p.getX() >= x1 && p.getX() <= x2 && p.getY() >= y1
                        && p.getY() <= y2) {
                    //p("Got text "+d);
                    res.add(d);
                }
            } else {
                if (clazz == null || clazz != d.getClass()) {
                    if (d.containsPoint(p)) {
                        //p("Found selectable drawable at "+p);
                        res.add(d);
                    }
                    //	else p(d+" does not contain point "+p);
                }
            }

        }
        if (res.size() == 1) {
            return (Drawable) res.get(0);
        } else if (res.size() > 1) {
            //sort by layer

            p("=====Found multiple objects, returning the one with largest layer number: "
                    + res);
            return (Drawable) res.get(0);
        }
        if (text) {
            return getTextAt(x, y);
        } else {
            return null;
        }

    }

    public Drawable getDrawableAt1(double x, double y) {
        x = x - origin.getX();
        y = y - origin.getY();
        x = x / factorx;
        y = y / factory;
        x = x + origin.getX();
        y = y + origin.getY();
        for (int i = 0; i < selectables.size(); i++) {
            Drawable d = (Drawable) selectables.get(i);
            if (d.containsPoint(new Point((int) x, (int) y))) {
                //		p("is Drawable "+d.toString()+" selected? "+d.isSelected());
                return d;
            }
        }
        return null;
    }

    public Text getTextAt(double x, double y) {
        for (int i = 0; texts != null && i < texts.size(); i++) {
            Text d = (Text) texts.get(i);
            if (d.containsPoint(new Point((int) x, (int) y))) {
                d.setSelected(true);
                return d;
            }
        }
        return null;
    }

// *****************************************************************
// GET SET METHODS
// *****************************************************************
    /** recalculate size according to factor */
    public void setZoomFactor(double factor) {
        double height = startingheight;
        double width = startingwidth;
        if (zoom_x) {
            width = startingwidth * factor;
            factorx = factor;
        }

        if (zoom_y) {
            height = startingheight * factor;
            factory = factor;
        }

        //	p("Factor is "+factor+", Width="+width+", height="+height+", startingw: "+startingwidth+", startingh: "+startingheight);
        setSize(new Dimension((int) width, (int) height));
    }

    public double getXZoomFactor() {
        return factorx;
    }

    public double getYZoomFactor() {
        return factory;
    }

    public double getZoomFactor() {
        if (zoom_x) {
            return factorx;
        } else {
            return factory;
        }
    }

    public void setDrawables(ArrayList<Drawable> draws) {

        if (drawables == null) {
            drawables = new ArrayList<Drawable>();
        } else {
            drawables.clear();
        }
        if (movables == null) {
            movables = new ArrayList<Drawable>();
        } else {
            movables.clear();
        }
        if (selectables == null) {
            selectables = new ArrayList<Drawable>();
        } else {
            selectables.clear();
        }
        for (int i = 0; i < draws.size(); i++) {
            addDrawable((Drawable) draws.get(i));
        }
    }

//  public void removeDrawables() {
//	if (drawables == null) {
//	  drawables = new ArrayList<Drawable>();
//	}
//	else {
//	  drawables.removeAllElements();
//	}
//	if (movables == null) {
//	  movables = new ArrayList<Drawable>();
//	}
//	else {
//	  movables.removeAllElements();
//	}
//	if (selectables == null) {
//	  selectables = new ArrayList<Drawable>();
//	}
//	else {
//	  selectables.removeAllElements();
//	}
//  }
    public void setTexts(ArrayList<Drawable> v) {
        if (texts != null) {
            for (int i = 0; i < texts.size(); i++) {
                selectables.remove(texts.get(i));
            }
        }
        texts = new ArrayList<Drawable>();
        for (int i = 0; v != null && i < v.size(); i++) {
            addText((Drawable) v.get(i));
        }
//		p("texts:"+texts.size());
    }

    public void setSimpleDrawables(ArrayList<Drawable> simple) {
        simple_drawables = simple;
    }

    public void addSimpleDrawable(Drawable d) {
        if (simple_drawables == null) {
            simple_drawables = new ArrayList<Drawable>();
        }
        simple_drawables.add(d);
    }

    public void setStatics(ArrayList<Drawable> s) {
        statics = s;
    }

    public void addStatic(Drawable d) {
        if (statics == null) {
            statics = new ArrayList<Drawable>();
        }
        statics.add(d);
    }

    public ArrayList<Drawable> getDrawables() {
        return drawables;
    }

    public ArrayList<Drawable> getSimpleDrawables() {
        return simple_drawables;
    }

    public void clear(Drawable d) {
        Graphics2D g2 = (Graphics2D) getGraphics();
        // draw all untransformed things first
        transform(g2);
        d.clear(g2);
    }

    public void addDrawable(Drawable d, int pos) {
        if (d == null) {
            return;
        }
        //	p("adding drawable:"+d.getClass().getName());
        if (pos < 0) {
            pos = 0;
        }
        if (pos > drawables.size()) {
            pos = drawables.size() - 1;
        }
        drawables.add(pos, d);

        if (d.isSelectable()) {

            selectables.add(d);
            if (d.isMovable()) {

                movables.add(d);
            }

        }
    }

    public void addDrawable(Drawable d) {
        if (d == null) {
            return;
        }
        //	p("adding drawable:"+d.getClass().getName());

        drawables.add(d);

        if (d.isSelectable()) {
            //  		p("Got a selectable drawable "+d);
            selectables.add(d);
            if (d.isMovable()) {
                //				p("Got a movable drawable");
                movables.add(d);
            }
            if (d instanceof GuiObject) {
                GuiObject g = (GuiObject) d;
                ((GuiObject) d).setCanvas(this);
            }
            //  else p("not movable");
        }
//		else p("drawable is NOT selectable:" +d);
//
    }

    public void removeDrawable(Drawable d) {
        drawables.remove(d);
        selectables.remove(d);
        movables.remove(d);
        //	quad.remove(d);
        if (simple_drawables != null) {
            simple_drawables.remove(d);
        }
        if (statics != null) {
            statics.remove(d);
        }
        if (texts != null) {
            texts.remove(d);
        }
    }

    public void removeDrawables() {
        drawables.clear();
        selectables.clear();
        movables.clear();
        //	quad.remove(d);
        if (simple_drawables != null) {
            simple_drawables.clear();
        }
        if (statics != null) {
            statics.clear();
        }
        if (texts != null) {
            texts.clear();
        }
    }

    public void addText(Drawable t) {
        if (texts == null) {
            texts = new ArrayList<Drawable>();
        }
        texts.add(t);
        if (t.isSelectable()) {
            selectables.add(t);
        }
    }

    public void removeDrawable(Text t) {
        if (texts != null) {
            texts.remove(t);
        }
        if (statics != null) {
            statics.remove(t);
        }
    }

// *****************************************************************
// CALCULATIONS
// *****************************************************************
    /** calculate the position of the point p,
     *  where p is a coordinate within the zoomed canvas.
     *  zoom it back to the
     *  startingwidth and startingheight
     */
    public Point getCanvasToAbs(Point cp) {
        double cx = cp.getX();
        double cy = cp.getY();

        // now scale it!
        // for this we need the scale factors
        double ax = cx / factorx;
        double ay = cy / factory;

        Point absp = new Point((int) ax, (int) ay);
        //	p("VP Point "+vp.toString()+"-> abs "+absp.toString());
        return absp;
    }

    public Dimension getCanvasToAbs(Dimension d) {
        double w = d.getWidth();
        double h = d.getHeight();
        Point p = getCanvasToAbs(new Point((int) w, (int) h));
        return new Dimension((int) p.getX(), (int) p.getY());
    }

    /** calculate the position of the point p,
     *  where p is an absolute coordinate within the canvas.
     *  The result is the position in the zoomed canvas
     *  but NOT relative to the viewport
     */
    public Dimension getAbsToCanvas(Dimension d) {
        double w = d.getWidth();
        double h = d.getHeight();
        Point p = getAbsToCanvas(new Point((int) w, (int) h));
        return new Dimension((int) p.getX(), (int) p.getY());
    }

    /** calculate the position of the point p,
     *  where p is an absolute coordinate within the canvas.
     *  The result is the position in the zoomed canvas
     *  but NOT relative to the viewport
     */
    public Point getAbsToCanvas(Point absp) {
        double ax = absp.getX();
        double ay = absp.getY();

        // now scale it!
        // for this we need the scale factors
        double cx = ax * factorx;
        double cy = ay * factory;

        Point cp = new Point((int) cx, (int) cy);
        return cp;
    }

// *****************************************************************
// COLOR
// *****************************************************************
    public Color selectColor(Drawable d) {
        if (d == null) {
            return null;
        }
        Color color = JColorChooser.showDialog(this, "Please select a color",
                d.getForeground());
        return color;
    }

// *****************************************************************
// DRAWING
// *****************************************************************
    public void draw() {
        //	paintComponent(getGraphics());
        repaint();

    }

    public Dimension getPreferredSize() {
        return new Dimension((int) width, (int) height);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    private void drawList(Graphics2D g, ArrayList<Drawable> list) {
// sort by layer
        //if (qtlFilterIDs != null) System.out.print(" FILTER IDS =" + qtlFilterIDs);
        for (int i = 0; list != null && i < list.size(); i++) {
            Drawable d = (Drawable) list.get(i);

            if (d.isVisible()) {
                if (d.isStatic()) {
                    drawStatic(g, d);
                } else if (d.isText()) {
                    drawText(g, d);
                } else {
                    d.draw(g);
                }
            }
        }
    }

    public void draw(Graphics2D g) {
        if (g == null) {
            return;
        }
        //    Log.info("GuiCanvs: drawing statics drawables: "+statics);
        drawList(g, simple_drawables);
        drawList(g, statics);
        drawList(g, drawables);
        drawList(g, texts);
        //	if (guiquad != null && usequad)guiquad.draw(g);
    }

    public void clear() {
        Graphics g = getGraphics();
        clear(g);
    }

    public void setBackGroundImage(Image image) {
        backGroundImage_ = image;
    }

    public void clear(Graphics g) {
        if (g == null) {
            return;
        }
        g.setColor(Color.white);
        //	p("clearing to :"+ startingwidth+" /"+startingheight);
        if (startingheight > 300000) {
            startingheight = 300000;
        }
        try {
            g.fillRect(0, 0, (int) startingwidth, (int) startingheight);
            if (backGroundImage_ != null) {
                g.drawImage(backGroundImage_, 0, 0, null);
            }
        } catch (Throwable t) {
            err("could not clear to :" + startingwidth + "/" + startingheight);
        }
    }

    public void transform(Graphics2D g2) {

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(origin.getX(), origin.getY());
        g2.scale(factorx, factory);
        g2.translate(-origin.getX(), -origin.getY());
        g2.translate(trans.getX(), trans.getY());

    }

    public void untransform(Graphics2D g2) {
        g2.translate(-trans.getX(), -trans.getY());
        g2.translate(origin.getX(), origin.getY());
        g2.scale(1.0 / factorx, 1.0 / factory);
        g2.translate(-origin.getX(), -origin.getY());

    }

    public void drawText(Graphics2D g2, Drawable text) {
        double dx = (text.getX() - origin.getX()) * (1.0 - factorx);
        double dy = (text.getY() - origin.getY()) * (1.0 - factory);
        untransform(g2);
        g2.translate(-dx, -dy);
        text.draw(g2);
        g2.translate(dx, dy);
        transform(g2);
    }

    public void drawVerticalText(Graphics2D g, String t, int x, int y) {
        double dx = (x - origin.getX()) * (1.0 - factorx);
        double dy = (y - origin.getY()) * (1.0 - factory);
        untransform(g);

        AffineTransform r = new AffineTransform();
        r.setToRotation(-Math.PI / 2.0, dx, dy);
        g.setTransform(r);
        g.drawString(t, -y, x);
        //	 g.translate(dx, dy);
        transform(g);
    }

    private void drawBounds(Drawable d, Graphics2D g2) {
        if (DEBUG) {
            g2.setColor(Color.green);
            Rectangle r = d.getBounds();
            g2.drawRect(r.x, r.y, r.width, r.height);
        }
    }

    public void drawText(Graphics2D g2, String t, int x, int y) {
        double dx = (x - origin.getX()) * (1.0 - factorx);
        double dy = (y - origin.getY()) * (1.0 - factory);
        untransform(g2);
        g2.translate(-dx, -dy);
        g2.drawString(t, x, y);
        g2.translate(dx, dy);
        transform(g2);
    }

    public void drawStatic(Graphics2D g2, Drawable text) {
        //    Log.info("GuiCanvas: drawing static "+text);
        untransform(g2);
        text.draw(g2);
        transform(g2);
    }

    public void draw(Drawable d) {
        Graphics2D g2 = (Graphics2D) getGraphics();
        if (g2 == null || d == null) {
            return;
        }
        // draw all untransformed things first
        transform(g2);
        if (d.isVisible()) {
            if (d.isStatic()) {
                drawStatic(g2, d);
            } else if (d.isText()) {
                drawText(g2, d);
            } else {
                //	if (d.getX()<=0 || d.getY()<=0) {
                //	p("not drawing "+d+", coord is < 0");
                //	}
                //	else 
                d.draw(g2);
            }
        } else {
            d.clear(g2);
            p("drawable is invisible, not drawing");
        }
        drawBounds(d, g2);
    }

    public void paintComponent(Graphics g) {

        int nr = 0;
        if (this.getDrawables() != null) {
            nr = getDrawables().size();
            //	Log.info("GuiCanvas: -------------------  drawing "+nr+" drawables");
            //Exception e = new Exception("tracing paint calls");
            //e.printStackTrace();
        }
        if (paintit) {
            super.paintComponent(g);

            int canvas_width = getSize().width;
            int canvas_height = getSize().height;
            if (canvas_height <= 0 || canvas_width <= 0) {
                return;
            }

            //   if(offscreen == null) offscreen = createImage(canvas_width, canvas_height);
            //   Graphics og = offscreen.getGraphics();
            //   og.setClip(0, 0, canvas_width, canvas_height);

            Graphics2D g2 = (Graphics2D) g;
            clear(g2);
            paintContent(g2);
            transform(g2);


            draw(g2);


            untransform(g2);

        }

    }

    private void paintContent(Graphics2D g) {
        //	p("painting contents");
        Component[] comps = this.getComponents();
        if (comps == null || g == null) {
            return;
        }
        for (int i = 0; comps != null && i < comps.length; i++) {
            //  if (comps[i].getX() > 10) {
            comps[i].paint(g);
            // }
        }
    }

    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        Drawable d = getDrawableAt(e);
        if (d != null && d instanceof MouseListener) {
            ((MouseListener) d).mouseClicked(e);
        }
    }

    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        Drawable d = getDrawableAt(e);
        if (d != null && d instanceof MouseListener) {
            ((MouseListener) d).mousePressed(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        Drawable d = getDrawableAt(e);
        if (d != null && d instanceof MouseListener) {
            ((MouseListener) d).mouseReleased(e);
        }
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(GuiCanvas.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(GuiCanvas.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(GuiCanvas.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("GuiCanvas: " + msg);
        //Logger.getLogger( GuiCanvas.class.getName()).log(Level.INFO, msg, ex);
    }
}
