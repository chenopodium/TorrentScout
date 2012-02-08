/*
 * Copyright (C) 2011 Life Technologies Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.iontorrent.guiutils.widgets;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class Widget implements Serializable {

    private Color color;
    private int x;
    private int y;
    private static int MAX = 20;
    protected boolean selected;
    private boolean mainWidget = false;
    private int nr;
    private String name;
    
    public Widget(Color color, int x, int y, int nr) {
        this.color = color;
        this.x = x;
        this.y = y;
        this.nr = nr;
    }

    public void moveDx(int dx) {
        x = x + dx;
    }

    public void moveDy(int dy) {
        y = y + dy;
    }

    public String getName() {
        if (name == null) name = ""+nr;
        return name;
    }

    @Override
    public String toString() {
        return "Widget "+getName()+" at " + getX() + "/" + getY() + ", color: " + getColor();
    }

    public void paint(Graphics g, int x, int y, double scale) {
        g.setColor(getColor());
        g.drawOval(x, y, (int) (2 * scale), (int) (2 * scale));
    }

    public double getDist(int xx, int yy) {
        int dx = x - xx;
        int dy = y - yy;
        double d = Math.sqrt(dx * dx + dy * dy);
        return d;
    }

    public static Widget getClosest(double x, double y, ArrayList<Widget> list) {
        return getClosest(x, y, list, true);
    }

    public static Widget getClosest(double x, double y, ArrayList<Widget> list, boolean select) {
        if (list == null) return null;
        if (list.size() > 20) {
            p("many widgets:" + list.size());
        }
        return getClosest(x, y, list, MAX, select);
    }

    public static Widget getClosest(double x, double y, ArrayList<Widget> list, double maxdist, boolean select) {
        if (list == null) {
            return null;
        }
        double dist = Double.MAX_VALUE;

        Widget best = null;
        for (Widget w : list) {
            if (select) {
                w.setSelected(false);
            }
            double d = w.getDist((int) x, (int) y);
            if (d < dist) {
                dist = d;
                best = w;
            }
        }

        if (dist < maxdist) {
            //if (select) p("Best widgetfor "+x+"/"+y+": " + best + ", dist: " + dist + " pixels: "+best.getDist((int)x, (int)y));
            if (select) {
                best.setSelected(true);
            }
            return best;
        } else {
            return null;
        }
    }

    public void setSelected(boolean b) {
        this.selected = b;
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(Widget.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        Logger.getLogger(Widget.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(Widget.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        //System.out.println("Widget: " + msg);
        Logger.getLogger(Widget.class.getName()).log(Level.INFO, msg);
    }

    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }
    public Point getPoint() {
        return new Point(x, y);
    }
    /**
     * @param x the x to set
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * @return the mainWidget
     */
    public boolean isMainWidget() {
        return mainWidget;
    }

    /**
     * @param mainWidget the mainWidget to set
     */
    public void setMainWidget(boolean mainWidget) {
        this.mainWidget = mainWidget;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}
