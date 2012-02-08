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
package com.iontorrent.torrentscout.explorer.fit;

import com.iontorrent.guiutils.widgets.Widget;
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
public class HistoWidget extends Widget {

    private int bin;
    int ch = 14;
    int cx = 5;
    int d = 4;

    public HistoWidget(Color color, int x, int y, int nr) {
        super(color, x, y, nr);

    }

    @Override
    public void paint(Graphics g, int x, int y, double scale) {
        g.setColor(getColor());
        Graphics2D gg = (Graphics2D) g;
        gg.setStroke(new BasicStroke(3));
        int h = 70;
        int cy0 = y - h - 5;
        int cy1 = cy0 - ch;

        g.drawLine(x, y - h, x, y-1);
        
        gg.fill3DRect(x - 2, (y - h / 2), 6, 6, true);
        
        if (selected) gg.setStroke(new BasicStroke(2));
        else gg.setStroke(new BasicStroke(1));
        // craw cisssor
        g.drawLine(x - cx, cy0, x + cx, cy1);
        g.drawLine(x - cx, cy1, x + cx, cy0);
       // g.setColor(new Color(100, 0, 0));
        g.drawOval(x - cx - d/2, cy1 - d, d, d);
        g.drawOval(x + cx-d/2, cy1 - d, d, d);
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(HistoWidget.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        Logger.getLogger(HistoWidget.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(HistoWidget.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        //System.out.println("CoordWidget: " + msg);
        Logger.getLogger(HistoWidget.class.getName()).log(Level.INFO, msg);
    }

    /**
     * @return the bin
     */
    public int getBin() {
        return bin;
    }

    /**
     * @param coord the bin to set
     */
    public void setBin(int bin) {
        this.bin = bin;
    }

    @Override
    public String toString() {
        return "HistoWidget " + super.getName() + "@ bin " + bin;
    }
}
