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

import com.iontorrent.guiutils.widgets.Widget;
import com.iontorrent.wellmodel.WellCoordinate;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class CoordWidget extends Widget {

    private WellCoordinate abscoord;
  
    public CoordWidget(Color color, int x, int y, int nr) {
        super(color, x, y, nr);

    }

    @Override
    public void paint(Graphics g, int x, int y, double scale) {
       
        Graphics2D gg = (Graphics2D) g;
        int armlen = 8;
        int thickness = 5;
        if (selected) {
            gg.setStroke(new BasicStroke(3));
            g.setColor(getColor().brighter());
        }
        else {
            g.setColor(getColor());
            gg.setStroke(new BasicStroke(2));
        }
        if (super.isMainWidget()) {
            armlen = 10;
        }
        
        Polygon p = createCross(x-thickness/2, y- armlen-thickness/2, thickness, armlen);
        g.fillPolygon(p);
      //  if (selected) g.setColor(getColor().darker());
        g.setColor(Color.black);
        g.drawPolygon(p);
    }

    private Polygon createCross(int x, int y, int d, int s) {

        Point[] points = new Point[]{new Point(x, y), new Point(x + d, y), new Point(x + d, y + s), new Point(x + d + s, y + s), new Point(x + d + s, y + d + s), new Point(x + d, y + d + s), new Point(x + d, y + s + d + s), new Point(x, y + s + d + s),
            new Point(x, y + d + s), new Point(x - s, y + d + s), new Point(x - s, y + s), new Point(x, y + s), new Point(x, y)};
        int l = points.length;
        int[] xp = new int[l];
        int[] yp = new int[l];
        for (int i = 0; i < l; i++) {
            xp[i] = (int) points[i].getX();
            yp[i] = (int) points[i].getY();
        }
        Polygon p = new Polygon(xp, yp, l);
        return p;

    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(CoordWidget.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        Logger.getLogger(CoordWidget.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(CoordWidget.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        //System.out.println("CoordWidget: " + msg);
        Logger.getLogger(CoordWidget.class.getName()).log(Level.INFO, msg);
    }

    /**
     * @return the coord
     */
    public WellCoordinate getAbsoluteCoord() {
        return abscoord;
    }

    /**
     * @param coord the coord to set
     */
    public void setAbsoluteCoords(WellCoordinate coord) {
        this.abscoord = new WellCoordinate(coord);
    }

    @Override
    public String toString() {
        return "Coordinate " + super.getName() + "@ " + abscoord;
    }

  

   
}
