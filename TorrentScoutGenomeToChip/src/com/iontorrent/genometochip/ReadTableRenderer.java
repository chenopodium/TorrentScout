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
package com.iontorrent.genometochip;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Chantal Roth
 */
public class ReadTableRenderer extends JLabel implements TableCellRenderer {

    public static Color good = new Color(220, 255, 220);
    public static Color bad = new Color(255, 220, 220);
    private Color notsure = Color.lightGray;
    private Color info = new Color(225, 230, 255);
    private Color nada = Color.white;

    public ReadTableRenderer() {

        setOpaque(true);
    }

    @Override
    public ReadTableRenderer getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int col) {

        String headername = "" + table.getTableHeader().getColumnModel().getColumn(col).getHeaderValue();
        if (headername.startsWith("flow")) {
            this.setBackground(good);
        } else {
            this.setBackground(Color.white);
        }

        setText("" + value);
        return this;
    }
}
