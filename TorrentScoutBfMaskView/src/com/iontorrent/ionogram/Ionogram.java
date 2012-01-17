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
package com.iontorrent.ionogram;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.rawdataaccess.wells.WellData;
import com.iontorrent.wellmodel.WellContext;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class Ionogram {

    WellData data;
    WellContext context;

    public Ionogram(WellData data, WellContext context) {
        this.data = data;
        this.context = context;

    }

    public String getSequence() {
        return data.getSequence();
    }

    public int nrFlows() {
        return data.getNrFlows();
    }

    public int getX() {
        return data.getX();
    }

    public int getY() {
        return data.getY();
    }

    public float getFlowValue(int i) {
        return data.getFlowValue(i);
    }

    public int getNormalizedValue(int i) {
        if (data.getNormalizedValues() == null) {
            return -1;
        }
        return data.getNormalizedValues()[i];
    }

    public ExperimentContext getExpContext() {
        return context.getExpContext();
    }
    public float computePpf(boolean raw, float cutoff) {
        float values[] = getValues(raw);
        if (values == null) {
            return -1;
        }
        float sum = 0;
        //int count = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] >= cutoff) {
                sum += values[i];
                //count++;
            }
        }
        return sum / values.length;
    }
     public float computePpf(boolean raw) {
         return computePpf(raw, 0.25f);
     }
     /** compute sum of fractional parts */
     public float computeSSQ(boolean raw) {
        float values[] = getValues(raw);
        if (values == null) {
            return -1;
        }
        float sum = 0;
        
        for (int i = 0; i < values.length; i++) {
            double x  =  values[i] - Math.round(values[i]);
            sum += x*x;
        }
        return sum;
    }
    public float getMax(boolean raw) {
        float values[] = getValues(raw);
        if (values == null) {
            return -1;
        }
        float max = 0;
        for (int i = 0; i < values.length; i++) {
           if (max < values[i]) max = values[i];
        }
        return max;
    }
    public float compute(boolean raw) {
        float values[] = getValues(raw);
        if (values == null) {
            return -1;
        }
        float sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }
        return sum / values.length;
    }

    public float[] getValues(boolean raw) {
        float values[] = null;
        if (raw) {
            values = data.getFlowValues();
        } else {
            int norm[] = data.getNormalizedValues();
            if (norm == null) {
                return null;
            }
            values = new float[norm.length];
            for (int i = 0; i < norm.length; i++) {
                values[i] = (float)(norm[i]) / 100.0f;
            }
        }
        return values;
    }

    public String toString() {
        return "Ionogram at " + getX() + "/" + getY();
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(Ionogram.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(Ionogram.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(Ionogram.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("Ionogram: " + msg);
        //Logger.getLogger( Ionogram.class.getName()).log(Level.INFO, msg, ex);
    }

    public WellContext getContext() {
        return context;
    }
}
