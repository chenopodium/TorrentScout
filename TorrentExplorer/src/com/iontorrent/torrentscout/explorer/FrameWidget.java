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
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class FrameWidget extends Widget {

    private int frame;
    private int y0;
    private int y1;
    private String name;
    
    public FrameWidget(String name, Color color, int nr, int frame) {
        super(color, 0,0, nr);
        this.name = name;
        this.frame = frame;
        
    }
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void paint(Graphics g, int x, int y, double scale) {
        g.setColor(getColor());
        Graphics2D gg = (Graphics2D) g;
        
        g.drawLine(x, getY0(), x, getY1());
        gg.fill3DRect(x-3, y-3, 6, 6, true);
    }

    @Override
    public String toString() {
        return "FrameWidget " + super.getName() + "@ " + getFrame();
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }
    public int getFrame() {
        return frame;
    }

    /**
     * @return the y0
     */
    public int getY0() {
        return y0;
    }

    /**
     * @param y0 the y0 to set
     */
    public void setY0(int y0) {
        this.y0 = y0;
    }

    /**
     * @return the y1
     */
    public int getY1() {
        return y1;
    }

    /**
     * @param y1 the y1 to set
     */
    public void setY1(int y1) {
        this.y1 = y1;
    }
}
