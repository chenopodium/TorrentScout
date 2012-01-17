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
package com.iontorrent.guiutils.heatmap;

import com.iontorrent.guiutils.ColorGradient;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class ColorModel {

    Color[] basecolors;
    double min;
    double max;
    double oldmin;
    double oldmax;
    Color[] customGradient;

    double factor;
    static final int MAXCOLORBUCKETS = 1000;
    int nrcolors;
    
    public ColorModel(Color[] basecolors, double min, double maxval) {
        this.min = min;
        this.oldmin = min;
        this.oldmax = maxval;
        this.max = maxval;
        this.factor = 1.0;
        setBaseColors(basecolors);
    }

    public void setBaseColors(Color[] basecolors) {

        int delta = (int) (max - min);
        if (delta < 0) {
            err("Max - min is < 0: " + getMax() + ", " + getMin());
            delta = 1;
        }
        this.basecolors = basecolors;
        int nr = basecolors.length;
        nrcolors = delta+1;
        if (delta+1 > MAXCOLORBUCKETS) {
            factor = (double)MAXCOLORBUCKETS/(double)(delta+1);
            nrcolors = MAXCOLORBUCKETS;
        }
        customGradient = ColorGradient.createMultiGradient(basecolors, nrcolors);
        customGradient[0] = basecolors[0];
        customGradient[customGradient.length - 1] = basecolors[nr - 1];

    }
    public Color getColor(double value) {
        value = value - min;
        if (factor != 1.0) value = value * factor;
         value = Math.min(value, customGradient.length - 1);
         value = Math.max(0, value);
         Color color = customGradient[(int)value];
         return color;
    }
    /** recompute colors */
    public void update() {
        setBaseColors(basecolors);
    }

    public void setMin(double min) {
        this.min = min;
    }

    public void setMax(double max) {
        this.max = max;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(ColorModel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(ColorModel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(ColorModel.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("ColorModel: " + msg);
        //Logger.getLogger( ColorModel.class.getName()).log(Level.INFO, msg, ex);
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public Color[] getColors() {
       return this.customGradient;
    }
}
