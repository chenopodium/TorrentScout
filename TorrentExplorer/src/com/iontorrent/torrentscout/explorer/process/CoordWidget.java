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
package com.iontorrent.torrentscout.explorer.process;

import com.iontorrent.torrentscout.explorer.Widget;
import com.iontorrent.wellmodel.WellCoordinate;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class CoordWidget extends Widget  {

    
    private WellCoordinate coord;
    
    public CoordWidget(Color color, int x, int y, int nr) {
        super(color, x, y, nr);
        
    }
    @Override
     public void paint(Graphics g, int x, int y, double scale) {
        g.setColor(getColor());
        Graphics2D gg= (Graphics2D)g;
        int fact = 4;
        if (super.isMainWidget()) {
            gg.setStroke(new BasicStroke(4));
            fact = 5;
        }
        else gg.setStroke(new BasicStroke(2));
        int w = (int)(fact*scale);
        g.drawLine(x, y-w, x, y+w);
        g.drawLine(x-w, y, x+w, y);
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
    public WellCoordinate getCoord() {
        return coord;
    }

    /**
     * @param coord the coord to set
     */
    public void setCoord(WellCoordinate coord) {
        this.coord = coord;
    }
    @Override
    public String toString() {
        return "CoordWidget "+super.getName()+"@ "+coord;
    }
}
