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
package com.iontorrent.torrentscout.explorer;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Panel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class WidgetLabel extends Panel {

    Widget w;
    public WidgetLabel(Widget w) {
        this.w = w;
        setLayout(new FlowLayout());
        setSize(50, 20);
    }
    protected void paintComponent(Graphics g) {
        super.paintComponents(g);
        g.setColor(w.getColor());
        
        g.fillRect(0,0,getWidth(), getHeight());
        g.setColor(Color.black);
        g.drawString(w.toString(), 1, 15);
    }
    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(WidgetLabel.class.getName()).log(Level.SEVERE, msg, ex);
    }
    
    private static void err(String msg) {
        Logger.getLogger(WidgetLabel.class.getName()).log(Level.SEVERE, msg);
    }
    
    private static void warn(String msg) {
        Logger.getLogger(WidgetLabel.class.getName()).log(Level.WARNING, msg);
    }
    
    private static void p(String msg) {
        //System.out.println("WidgetLabel: " + msg);
        Logger.getLogger(WidgetLabel.class.getName()).log(Level.INFO, msg);
    }
}
