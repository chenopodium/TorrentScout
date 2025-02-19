/*
 * Copyright (C) 2012 Life Technologies Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * PickMaskPanek.java
 *
 * Created on 12.03.2012, 10:12:30
 */
package com.iontorrent.torrentscout.explorer.edit;

import com.iontorrent.guiutils.FlowPanel;
import com.iontorrent.rawdataaccess.wells.BitMask;
import com.iontorrent.torrentscout.explorer.ContextChangeAdapter;
import com.iontorrent.torrentscout.explorer.ExplorerContext;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Chantal Roth
 */
public class PickMaskPanel extends javax.swing.JPanel {

    private ExplorerContext maincont;
    private JComboBox boxes[];
    private JLabel labels[];
    private String tips[];
    private GridLayout grid;
    private JPanel center;

    /** Creates new form PickMaskPanek */
    public PickMaskPanel(ExplorerContext maincont) {
        initComponents();
        center = new JPanel();
        grid = new GridLayout(3, 2);
        center.setLayout(grid);
        this.setLayout(new BorderLayout());
        
        add("West", new FlowPanel(center));
        this.maincont = maincont;
        labels = new JLabel[3];
        tips = new String[3];
        labels[0] = new JLabel("Ignore mask:");        
        tips[0] = "The mask tells the software which wells to ignore, such as pinned wells";
        
        labels[1] = new JLabel("Background mask:");        
        tips[1] = "The wells used as empty wells for BG subtraction";
        
        labels[2] = new JLabel("Signal mask:");        
        tips[2] = "The wells to use to compute a (mean) signal for instance";
                
        if (maincont != null) {
            maincont.addListener(new ContextChangeAdapter() {

                @Override
                public void maskAdded(BitMask mask) {
                    createGui();
                }

                @Override
                public void maskChanged(BitMask mask) {
                    createGui();

                }

                @Override
                public void masksChanged() {
                    createGui();

                }
            });
        }
         createGui();
    }

    private void createGui() {
        center.removeAll();
        boxes = new JComboBox[3];
        for (int i = 0; i < 3; i++) {
            final JComboBox box = new JComboBox();
            box.setPreferredSize(new Dimension(150, 25));
            if (i == 0) {
                box.addItem("All wells");
            }
            if (maincont.getMasks() != null) {
                for (BitMask m : maincont.getMasks()) {
                    box.addItem(m);
                    boxes[i] = box;
                }
                //box.setSelectedIndex(i);
                center.add(labels[i]);
                box.setToolTipText(tips[i]);
                center.add(box);
                if (i == 0) box.setSelectedItem(maincont.getIgnoreMask());
                else if (i == 1)  box.setSelectedItem(maincont.getBgMask());
                else if (i == 2)  box.setSelectedItem(maincont.getSignalMask());
                box.addActionListener(new ActionListener() {
                    
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        BitMask sel = null;
                        if (box.getSelectedItem() == null || box.getSelectedItem() instanceof String) {
                            sel = null;
                        }
                        else sel = (BitMask)box.getSelectedItem();
                        if (box == boxes[0]) maincont.setIgnoreMask(sel);
                        else if (box == boxes[1]) maincont.setBgMask(sel);
                        else if (box == boxes[2]) maincont.setSignalMask(sel);
                    }
                    
                });
            }

        }
        invalidate();
        revalidate();
        repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
