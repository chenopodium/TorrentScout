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

import com.iontorrent.dbaccess.RundbQualitymetrics;
import com.iontorrent.scout.experimentviewer.exptree.ExpNode;
import com.iontorrent.scout.experimentviewer.exptree.MyResult;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Chantal Roth
 */
public class DbTableModel extends DefaultTableModel {

    //   private static final int MAX_ROWS = 500;
    private static String[] HEADER = new String[]{"", "PGM", "Chip", "Run name","Date",
        "Q17 bases", "Max Q17 length", "Mean Q17 length"
    };
    private static Class[] TYPE = new Class[]{ImageIcon.class, String.class, String.class, String.class,Date.class,
        Long.class, Integer.class, Float.class
    };
    ArrayList<MyResult> res;

    @Override
    public Object getValueAt(int row, int col) {
        MyResult r = res.get(row);
        int delta = 5;
        RundbQualitymetrics q = r.getQaulityMetrics();
        if (col == 0) {
            String name = "chip.png";
            if (r.isTN()) {
                name = "zoom-out.png";
            } else if (r.isBB()) {
                name = "chip_bb.png";
            }
            return new javax.swing.ImageIcon(ExpNode.class.getResource(name));
        }//icon
        else if (col == 1) {
            return r.getPgmname();
        } else if (col == 2) {
            return r.getChipType();
        } else if (col == 3) {
            return r.getResultsName();
        }
          else if (col == 4) {
            return r.getDate();
        }
        //  else if (col == 3) return r.getStatus();
        else if (q != null) {
            if (col == delta) {
                return new Long(q.getQ17Bases());
            } else if (col == delta + 1) {
                return new Integer(q.getQ17MaxReadLength());
            } else if (col == delta + 2) {
                return new Float(q.getQ17MeanReadLength());
            }
        }
        return null;
    }

    public DbTableModel(ArrayList<MyResult> res) {
        super();
        this.res = res;

        this.setColumnIdentifiers(HEADER);
        p("Got " + res.size() + " MyResult");
    }

    @Override
    public int getRowCount() {
        if (res == null) {
            return 0;
        }
        return res.size();

    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public Class getColumnClass(int col) {
        return TYPE[col];
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(DbTableModel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(DbTableModel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(DbTableModel.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
//  System.out.println("ReadlTableModel: " + msg);
        //Logger.getLogger( ReadlTableModel.class.getName()).log(Level.INFO, msg, ex);
    }

    public MyResult getResult(int row) {
        return res.get(row);
    }
}
