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
 * FramePanel.java
 *
 * Created on 14.11.2011, 16:40:07
 */
package com.iontorrent.guiutils.flow;

import com.iontorrent.expmodel.FrameListener;
import javax.swing.JTextField;

/**
 *
 * @author Chantal Roth
 */
public class FramePanel extends javax.swing.JPanel {

    FrameListener list;
    
    /** Creates new form FramePanel */
    public FramePanel(FrameListener list) {
        initComponents();
        this.list = list;
    }

    
    public int getFrame() {
        return getInt(txtFrame);
    }
       
    public void setFrame(int f) {
        txtFrame.setText(""+f);       
    }
    
     private int getInt(JTextField txt) {
        if (txt.getText()==null) return 0;
        int i = 0;
        try {
            i = Integer.parseInt(txt.getText());
        }
        catch (Exception e) {}
        return i;
    }
 
   

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txtFrame = new javax.swing.JTextField();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(FramePanel.class, "FramePanel.jLabel1.text")); // NOI18N

        txtFrame.setColumns(5);
        txtFrame.setText(org.openide.util.NbBundle.getMessage(FramePanel.class, "FramePanel.txtFrame.text")); // NOI18N
        txtFrame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFrameActionPerformed(evt);
            }
        });
        txtFrame.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtFrameFocusLost(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtFrame, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel1)
                .addComponent(txtFrame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtFrameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFrameActionPerformed
        if (list != null) {
            list.frameChanged(getFrame());
        }
    }//GEN-LAST:event_txtFrameActionPerformed

    private void txtFrameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtFrameFocusLost
         if (list != null) {
            list.frameChanged(getFrame());
        }
    }//GEN-LAST:event_txtFrameFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField txtFrame;
    // End of variables declaration//GEN-END:variables
}