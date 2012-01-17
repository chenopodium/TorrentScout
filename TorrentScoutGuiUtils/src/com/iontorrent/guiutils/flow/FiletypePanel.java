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
package com.iontorrent.guiutils.flow;

import com.iontorrent.expmodel.FiletypeListener;
import com.iontorrent.guiutils.FlowPanel;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Chantal Roth
 */
public class FiletypePanel extends JPanel {

    private FiletypeListener list;
    private RawType type;
    JComboBox combo;
    private boolean DOACTIONS;

    public FiletypePanel() {
        
    }
    public FiletypePanel(FiletypeListener list) {
        setLayout(new BorderLayout());
        this.list = list;
        DOACTIONS = false;
        type = RawType.ACQ;
        setOpaque(false);
        this.setMaximumSize(new Dimension(150, 20));
        combo = new javax.swing.JComboBox();
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (RawType t : RawType.values()) {
            model.addElement(t);
        }
        combo.setModel(model);
        combo.setMaximumSize(new Dimension(80, 20));
        combo.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboFiletypeActionPerformed(evt);
            }
        });
        combo.setSelectedItem(RawType.ACQ);
        combo.setOpaque(false);
        DOACTIONS = true;
        JLabel l = new JLabel("raw type:");
        l.setOpaque(false);
       // FlowPanel f = new FlowPanel(combo); 
       // f.setOpaque(false);
        //add("West",l );
        //add("Center",f);
        add("West", l);
        add("Center", combo);
    }

    public void setType(RawType t) {
        this.type = t;
        DOACTIONS = false;
       
        combo.setSelectedItem(t);
        DOACTIONS = true;

    }

    private void comboFiletypeActionPerformed(java.awt.event.ActionEvent evt) {
        if (!DOACTIONS) {
            DOACTIONS = true;
            return;

        }
        DOACTIONS = true;
        type = (RawType) combo.getSelectedItem();
        getList().fileTypeChanged(type);
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(FiletypePanel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(FiletypePanel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(FiletypePanel.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("FiletypePanel: " + msg);
        //Logger.getLogger( FiletypePanel.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the type
     */
    public RawType getType() {
        return type;
    }

    /**
     * @return the list
     */
    public FiletypeListener getList() {
        return list;
    }

    /**
     * @param list the list to set
     */
    public void setList(FiletypeListener list) {
        this.list = list;
    }

}
