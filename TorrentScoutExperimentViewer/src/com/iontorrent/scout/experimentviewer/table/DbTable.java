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

import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.TableColumn;
import org.netbeans.swing.etable.ETable;

/**
 *
 * @author Chantal Roth
 */
public class DbTable extends ETable {

    public DbTable() {
        super();
        p("Created db table");
        // ETableColumnModel columnModel = (ETableColumnModel)this.getColumnModel();
        //columnModel.getc
    }

    public void setWidths() {
        int nrcols = columnModel.getColumnCount();
        for (int i = 0; i < nrcols; i++) {
            TableColumn column = columnModel.getColumn(i);
            if (i == 0) {
                column.setWidth(18);
                column.setMaxWidth(18);
            }
            else if (i == 1) {//pgm
                column.setWidth(50);
                column.setMaxWidth(70);
            }
            else if (i == 2) {//type
                column.setWidth(40);
                column.setMaxWidth(40);
            }
            else if (i == 4) {//date
                column.setWidth(80);
                column.setMaxWidth(80);
            }
           
        }
    }
    public void hide(String colname) {
        // ETableColumnModel columnModel = (ETableColumnModel) this.getColumnModel();
        int nrcols = columnModel.getColumnCount();
        for (int i = 0; i < nrcols; i++) {
            TableColumn column = columnModel.getColumn(i);
            if (column.getHeaderValue().equals(colname)) {
                removeColumn(column);
            }
        }
    }

    @Override
    public String getToolTipText(MouseEvent ev) {
        int row = this.rowAtPoint(ev.getPoint());
        row = convertRowIndexToModel(row);
        int col = this.columnAtPoint(ev.getPoint());
        String res = "row " + row;
        if (row > 0) {
            Object val = this.getModel().getValueAt(row, col);
            res = this.getColumnName(col) + ": " + val;
        }
        return res;
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(DbTable.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {

        Logger.getLogger(DbTable.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(DbTable.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("DbTable: " + msg);
        Logger.getLogger(DbTable.class.getName()).log(Level.INFO, msg);
    }

    public String toCsv() {
        int rows = this.getRowCount();
        int cols = this.getColumnCount();
        String s = "";
        for (int c = 0; c < cols; c++) {
            TableColumn column = columnModel.getColumn(c);
            s += column.getHeaderValue();
            if (c + 1 < cols) {
                s += ", ";
            } else {
                s += "\n";
            }
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                s += this.getValueAt(r, c);
                if (c + 1 < cols) {
                    s += ", ";
                } else {
                    s += "\n";
                }
            }
        }
        return s;
    }
}
