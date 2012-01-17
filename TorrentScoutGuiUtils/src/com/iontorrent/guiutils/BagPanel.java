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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 *
 * @author Chantal Roth
 */
public class BagPanel extends JPanel {

    private GridBagLayout grid;
    private GridBagConstraints cons;
    public static final Insets INSETS5 = new Insets(5, 5, 5, 5);

    public BagPanel() {
        this(false);
    }

    public BagPanel(boolean smallSpace) {
        this(0, 0);
        if (smallSpace) {
            setInsets(new Insets(1, 5, 0, 5));
        }
    }

    public BagPanel(int x, int y) {
        super();
        if (x > 0 && y > 0) {
            setMinimumSize(new Dimension(x, y));
        }
        grid = new GridBagLayout();
        setLayout(grid);

        cons = new GridBagConstraints();
        cons.insets = INSETS5;
        cons.anchor = GridBagConstraints.NORTHWEST;
        cons.fill = GridBagConstraints.BOTH;
    }

    public void setAnchor(int a) {
        cons.anchor = a;
    }

    public void setFill(int a) {
        cons.fill = a;
    }

    public void setInsets(Insets insets) {
        cons.insets = insets;
    }

    public void place(int x, int y, int w, int h, Component comp) {
        place(x, y, w, h, 1, 1, comp);
    }

    public void place(int x, int y, Component comp) {
        place(x, y, 1, 1, 1, 1, comp);
    }

    public void place(int x, int y, int w, int h, double wx, double wy, Component comp) {
        cons.weightx = wx;
        cons.weighty = wy;
        cons.gridx = x;
        cons.gridy = y;
        cons.gridwidth = w;
        cons.gridheight = h;
        grid.setConstraints(comp, cons);
        add(comp);
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(BagPanel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(BagPanel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(BagPanel.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("BagPanel: " + msg);
        //Logger.getLogger( BagPanel.class.getName()).log(Level.INFO, msg, ex);
    }
}
