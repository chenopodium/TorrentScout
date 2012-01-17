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
package com.iontorrent.guiutils;

import java.awt.FlowLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author Chantal Roth
 */
public class FlowPanel extends JPanel {

    FlowLayout l;

    public FlowPanel(JComponent comp) {
        this(comp, null, null);
        
    }

    public FlowPanel(JComponent comp, JComponent comp1) {
        this(comp, comp1, null);
    }

    public FlowPanel(JComponent comp, JComponent comp1, JComponent comp2) {
        this.setOpaque(false);
        l = new FlowLayout(FlowLayout.LEFT);
        l.setHgap(0);
        l.setVgap(0);

        setLayout(l);
        if (comp != null) {
            add(comp);
        }
        if (comp1 != null) {
            add(comp1);
        }
        if (comp2 != null) {
            add(comp2);
        }
    }

    public void setCenter() {
        l.setAlignment(FlowLayout.CENTER);
    }

    public void setLeft() {
        l.setAlignment(FlowLayout.LEFT);
    }

    public void setRight() {
        l.setAlignment(FlowLayout.RIGHT);
    }

    public void setLeading() {
        l.setAlignment(FlowLayout.LEADING);
    }

    public void setTrailing() {
        l.setAlignment(FlowLayout.TRAILING);
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(FlowPanel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(FlowPanel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(FlowPanel.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("FlowPanel: " + msg);
        //Logger.getLogger( FlowPanel.class.getName()).log(Level.INFO, msg, ex);
    }
}
