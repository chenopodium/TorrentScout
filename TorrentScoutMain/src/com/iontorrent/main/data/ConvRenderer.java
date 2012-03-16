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
package com.iontorrent.main.data;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Chantal Roth
 */
public class ConvRenderer extends JLabel implements TableCellRenderer {

    public static Color good = new Color(220, 255, 220);
    public static Color orange = new Color(255, 255, 220);
    public static Color bad = new Color(255, 220, 220);
    private Color notsure = Color.lightGray;
    private Color info = new Color(225, 230, 255);
    private Color nada = Color.white;

    public ConvRenderer() {
        setOpaque(true);
    }

    @Override
    public ConvRenderer getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int col) {
        /**  model.setValueAt(c.getType().getDescription(), i, 0);
        model.setValueAt(c.getMax(), i, 1);
        model.setValueAt(c.getStart(), i, 2);
        model.setValueAt(c.getMax(), i, 3); */
        setText("" + value);
        if (col < 1) {
            this.setBackground(nada);
            return this;
        }
        int max = (Integer) table.getValueAt(row, 1);
        int start = (Integer) table.getValueAt(row, 2);
        int end = (Integer) table.getValueAt(row, 3);
        if (col == 1) { // start
            if (max == 0) setBackground(bad);
            else setBackground(nada);
        } else if (col == 2) { // start
            if (start == 0) setBackground(bad);
            else if (max == start) {
                setBackground(good);            
            }
            else setBackground(info);
        } else if (col == 3) { //end
            if (end > max) {
                setForeground(Color.red.darker());
            }
            if (end - start > 10) {
                setBackground(bad);
            }
            else if (end - start > 5) {
                setBackground(orange);
            }
            else setBackground(nada);
        }
        return this;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(ConvRenderer.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(ConvRenderer.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(ConvRenderer.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
//  System.out.println("ConvRenderer: " + msg);
        //Logger.getLogger( ConvRenderer.class.getName()).log(Level.INFO, msg, ex);
    }
}
