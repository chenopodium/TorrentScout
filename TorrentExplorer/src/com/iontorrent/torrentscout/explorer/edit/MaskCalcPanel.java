/*
 * Copyright (C) 2011 Life Technologies Inc.
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
 * MaskCalcPanel.java
 *
 * Created on 03.11.2011, 08:19:30
 */
package com.iontorrent.torrentscout.explorer.edit;

import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.rawdataaccess.wells.BitMask;
import com.iontorrent.torrentscout.explorer.ContextChangeAdapter;
import com.iontorrent.torrentscout.explorer.ExplorerContext;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author Chantal Roth
 */
public class MaskCalcPanel extends javax.swing.JPanel implements ActionListener {

    ExplorerContext cont;
    JComboBox[] boxes;
    JComboBox ops;

    /** Creates new form MaskCalcPanel */
    public MaskCalcPanel(ExplorerContext cont) {
        initComponents();
        this.cont = cont;
        if (cont != null) {
            cont.addListener(new ContextChangeAdapter() {

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

    public boolean compute() throws HeadlessException {
        //p("Computing");
        MaskOperation op = (MaskOperation) ops.getSelectedItem();
        BitMask m1 = (BitMask) boxes[0].getSelectedItem();
        if (op == null) {
            JOptionPane.showMessageDialog(this, "Please select an operation");
            return false;
        } else if (m1 == null) {
            JOptionPane.showMessageDialog(this, "Please select a mask for input and output");
            return false;
        }
        BitMask m2 = (BitMask) boxes[1].getSelectedItem();
        BitMask m3 = null;
        if (boxes[2].getSelectedItem() instanceof String) {
            m3 = new BitMask(m1);
            String name = JOptionPane.showInputDialog(this, "Please name the result mask", "result");
            if (name == null || name.length()<1) return false;
            if (name != null) {
                m3.setName(name);
            } else {
                m3.setName("result");
            }
            cont.masAdded(m3);
            
        } else {
            m3 = (BitMask) boxes[2].getSelectedItem();
        }
        if (m3 == null) {
            JOptionPane.showMessageDialog(this, "Please select a mask for the result");
            return false;
        }
        if (op.getNrArgs() > 1) {
            if (m2 == null) {
                JOptionPane.showMessageDialog(this, "Operation " + op + " requires " + op.getNrArgs() + " arguments");
                return false;
            }
        }
        boolean ok = op.execute(m1, m2, m3);
        if (ok) {
            if (op.getNrArgs() == 2) {
                GuiUtils.showNonModalMsg("Operation " + m1 + " " + op.getName() + m2 + " -> " + m3 + " done");
            } else {
                GuiUtils.showNonModalMsg("Operation " + m1 + " " + op.getName() + " -> " + m3 + " done");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Operation failed");
        }
        cont.maskChanged(m3);
        return true;
    }

    private void createGui() {
        this.removeAll();
        boxes = new JComboBox[3];
        for (int i = 0; i < 3; i++) {
            JComboBox box = new JComboBox();
            box.setPreferredSize(new Dimension(150, 25));
            if (i == 2) {
                box.addItem("New mask");
            }
            if (cont.getMasks() != null) {
                for (BitMask m : cont.getMasks()) {
                    box.addItem(m);
                    boxes[i] = box;
                }
                //box.setSelectedIndex(i);
                add(box);
            }
            if (i == 0) {
                // op: m1 op m2 = m3 ok
                ops = new JComboBox();
                for (AbstractOperation op: OperationFactory.getOps()) {
                    ops.addItem(op);
                }
                add(ops);
                ops.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        AbstractOperation op = (AbstractOperation) ops.getSelectedItem();
                        int nr = op.getNrArgs();
                        if (nr == 1) {
                            boxes[1].setEnabled(false);
                        } else {
                            boxes[1].setEnabled(true);
                        }
                    }
                });
            } else if (i == 1) {
                add(new JLabel("->"));
            } else if (i == 2) {
                JButton btn = new JButton("Execute");
                btn.addActionListener(this);
                add(btn);
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
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    @Override
    public void actionPerformed(ActionEvent e) {
        compute();
            
    }
}
