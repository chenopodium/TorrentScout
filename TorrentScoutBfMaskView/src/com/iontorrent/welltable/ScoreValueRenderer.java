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
package com.iontorrent.welltable;

import java.awt.Color;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Chantal Roth
 */
public class ScoreValueRenderer extends JLabel implements TableCellRenderer {

     private Color good = new Color(200,250,200);
    private Color bad = new Color(250, 200, 200);
    private Color nodata = new Color(250, 230, 200);
    
    private boolean scoresLoaded;
    
    public ScoreValueRenderer() {

        setOpaque(true);
    }

    @Override
     public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (!(value instanceof Double)) {
            setText("nf: "+value.toString()+", col: "+column);
            return this;
        }
        float val = ((Double) value).floatValue();
        if (val > 0 || scoresLoaded) {
            setBackground(good);
            setText(value.toString());
        } else if (val <=0) {
            setBackground(nodata);
            setText("?");
        }
        return this;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(FlagRenderer.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(FlagRenderer.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(FlagRenderer.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("FlagRenderer: " + msg);
        //Logger.getLogger( FlagRenderer.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the scoresLoaded
     */
    public boolean isScoresLoaded() {
        return scoresLoaded;
    }

    /**
     * @param scoresLoaded the scoresLoaded to set
     */
    public void setScoresLoaded(boolean scoresLoaded) {
        this.scoresLoaded = scoresLoaded;
    }

   
}
