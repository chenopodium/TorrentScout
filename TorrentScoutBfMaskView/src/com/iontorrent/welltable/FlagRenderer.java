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


import com.iontorrent.rawdataaccess.wells.BfMaskFlag;
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
public class FlagRenderer extends JLabel implements TableCellRenderer {


    public static Color good = new Color(220,255,220);
    public static Color bad = new Color(255, 220, 220);
    private Color notsure = Color.lightGray;
    private Color info = new Color(225,230, 255);
    private Color nada = Color.white;
    public FlagRenderer() {
        
        setOpaque(true); 
    }

    
    @Override
    public FlagRenderer getTableCellRendererComponent(
                            JTable table, Object value,
                            boolean isSelected, boolean hasFocus,
                            int row, int col) {
        if (col <2 || value == null) {
            setText(""+value);
        }
        else {
            String headername = ""+table.getTableHeader().getColumnModel().getColumn(col).getHeaderValue();
            BfMaskFlag flag = BfMaskFlag.get(headername);
            boolean isTrue = value.toString().equalsIgnoreCase("true");
            if (flag.equals(flag.AMBIGUOUS)) {
                if (isTrue) setBackground(notsure);
                else  setBackground(good);     
            }
            else if (flag.equals(flag.IGNORE) || flag.equals(flag.PINNED)) {
                if (isTrue) setBackground(notsure);
                else  setBackground(good);     
            }
            else if (flag.equals(flag.DUD) || 
                    flag.equals(flag.EMPTY) || flag.equals(flag.WASHOUT)
                    || flag.equals(flag.EXCLUDE) ||
                    flag.equals(flag.FBADKEY) ||
                    flag.equals(flag.FBADPPF) || flag.equals(flag.FSHORT) ||
                    flag.equals(flag.FBADRESIDUAL)) {
                if (isTrue) setBackground(bad);
                else  setBackground(good);     
            }
            else if (flag.equals(flag.TESTFRAG) || flag.equals(flag.LIBRARY)) {
                if (isTrue) setBackground(info);
                else  setBackground(nada);     
            }
          
            else {
                if (isTrue) setBackground(good);
                else  setBackground(bad);                
            }
            if (isTrue) {
                setText("+");
            }
            else {
                setText("-");
            }
        }
        return this;
    }

/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( FlagRenderer.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( FlagRenderer.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( FlagRenderer.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("FlagRenderer: " + msg);
        //Logger.getLogger( FlagRenderer.class.getName()).log(Level.INFO, msg, ex);
    }
}
