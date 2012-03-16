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
package com.iontorrent.scout.experimentviewer.table;

import com.iontorrent.guiutils.ColorGradient;
import com.iontorrent.scout.experimentviewer.exptree.ExpNode;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Chantal Roth
 */
public class DbTableRenderer extends JLabel implements TableCellRenderer {

    public static Color good = new Color(220, 255, 220);
    public static Color bad = new Color(255, 220, 220);
    private Color notsure = Color.lightGray;
    private Color info = new Color(225, 230, 255);
    private Color nada = Color.white;
    private static int MAX = 600;
    DecimalFormat dec = new DecimalFormat("#.#");
    public final static Color[] gradient = ColorGradient.createMultiGradient(new Color[]{
                new Color(255, 200, 200), //red
                Color.white, //white
                Color.white,
                new Color(200, 255, 200) //green
            }, MAX);

    public DbTableRenderer() {

        setOpaque(true);
    }

    public static Color getColor(double value) {

        value = Math.min(value, gradient.length - 1);
        value = Math.max(0, value);
        Color color = gradient[(int) value];
        return color;
    }

    public static Color getColor(long value) {

        value = Math.min(value, gradient.length - 1);
        value = Math.max(0, value);
        Color color = gradient[(int) value];
        return color;
    }

    private static void p(String msg) {
        System.out.println("DbTableRenderer: " + msg);
        Logger.getLogger(DbTableRenderer.class.getName()).log(Level.INFO, msg);
    }

    @Override
    public DbTableRenderer getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int col) {

        String val = "";
        if (col == 0) { //icon             
            this.setIcon((ImageIcon)value);
            this.setBackground(Color.white);
        } else {
            String headername = ("" + table.getTableHeader().getColumnModel().getColumn(col).getHeaderValue()).toLowerCase();
           // p("Got headername=" + headername);
            if (headername.indexOf("bases") > -1) {
                long bases = (Long) table.getValueAt(row, col);
                // scale to at most MAX. We want giga bases, so t
                long g = 1000000000/MAX;
                this.setBackground(getColor(bases / g));
                val = getNice(bases);
            } else if (headername.indexOf("max") > -1) {
                int bases = (Integer) table.getValueAt(row, col);
                this.setBackground(getColor(bases));
                 val = getNice(bases);
            } else if (headername.indexOf("mean") > -1) {
                float bases = (Float) table.getValueAt(row, col);
                // 400 is already quite good for mean
                this.setBackground(getColor(bases /4*6));
                 val = ""+dec.format(bases);
            } else {
                val = ""+value;
                this.setBackground(Color.white);
            }
            setText(val);
        }

        return this;
    }

    private String getNice(long nr) {
        String val = "";

        if (nr > 1000000000) {
            val = "" + dec.format((double) (nr / 1000000000.0d)) + " GB";
        } else if (nr > 1000000) {
            val = "" + dec.format((double) (nr / 1000000.0d)) + " MB";
         } else if (nr > 1000) {
            val = "" + dec.format((double) (nr / 1000.0d)) + " kB";
        } else {
            val = ""+nr;
        }
        return val;
    }
}