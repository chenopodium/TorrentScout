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
 * RasterView.java
 *
 * Created on 01.11.2011, 09:41:11
 */
package com.iontorrent.torrentscout.explorer.process;

import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.rawdataaccess.pgmacquisition.DataAccessManager;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.torrentscout.explorer.ContextChangeAdapter;
import com.iontorrent.torrentscout.explorer.ExplorerContext;
import com.iontorrent.torrentscout.explorer.Widget;
import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.wellmodel.RasterData;
import com.iontorrent.wellmodel.WellCoordinate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Chantal Roth
 */
public class RasterView extends javax.swing.JPanel {

    private RasterPanel moviePanel;
    ExplorerContext maincont;

    /** Creates new form RasterView */
    public RasterView(ExplorerContext maincont, boolean load) {
        initComponents();
        this.maincont = maincont;
        maincont.addListener(new ContextChangeAdapter() {

            @Override
            public void frameChanged(int f) {
                if (moviePanel != null) {
                    // p("Setting frame to "+sliderFrames.getValue());
                    sliderFrames.setValue(f);
                }
            }
        });
        moviePanel = new RasterPanel();
        panMain.add("Center", moviePanel);

        // listenToKeysFrom(this);
        //  update(load);
    }

//    public void listenToKeysFrom(JComponent comp) {
//        moviePanel.listenToKeysFrom(comp);
//    }
    public void update(boolean load, ExplorerContext maincont) {
        this.maincont = maincont;

        if (maincont == null) {
            return;
        }

        p("Update called: " + load);
        if (load) {
            loadData(maincont);
        }
        String error = moviePanel.update(maincont);
        moviePanel.listenToKeysFrom(this);
        if (error != null) {
            p(error);
            GuiUtils.showNonModalMsg(error);
        }
        p("Repainting moviepanel (rasterpanel)");
        if (maincont != null && maincont.getData() != null) {
            this.sliderFrames.setMaximum(maincont.getData().getFrames_per_flow());
        } else {
            this.sliderFrames.setMaximum(250);
        }
        //   panMain.repaint();

        invalidate();
        revalidate();
        repaint();
        //    paintAll(getGraphics());
        //    moviePanel.repaint();        
        // moviePanel.paintImmediately(0,0,800,800);

    }

    @Override
    public void repaint() {
        super.repaint();
        if (panMain != null) {
            panMain.repaint();
        }
        if (moviePanel != null) {
            moviePanel.repaint();
        }
        //  panMain.rep
    }

    public void loadData(ExplorerContext maincont) {

        WellCoordinate coord = maincont.getAbsDataAreaCoord();
        if (coord == null) {
            return;
        }
        GuiUtils.showNonModalMsg("Loading flow " + maincont.getFlow() + ", type: " + maincont.getFiletype() + ", coord: " + maincont.getAbsDataAreaCoord() + ", rel coord: " + maincont.getRelativeDataAreaCoord());

        p("About to load data for " + maincont.getExp().getRawDir() + ", flow " + maincont.getFlow() + ", type: " + maincont.getFiletype() + ", REL coord: " + maincont.getRelativeDataAreaCoord());
        DataAccessManager manager = DataAccessManager.getManager(maincont.getExp().getWellContext());
        try {
            WellCoordinate rel = maincont.getRelativeDataAreaCoord();

            RasterData data = manager.getRasterDataForArea(maincont.getData(), maincont.getRasterSize(), rel, maincont.getFlow(), maincont.getFiletype(), null, 0, -1);
            // now subtract first frame
            //  p("maincont is: "+maincont);
            p("Result of data loading is " + data);
            maincont.setData(data);
            p("Data is: " + maincont.getData());
            if (data == null) {
               maincont.tellUserWhyDataNotThere();               
            }

        } catch (Exception e) {
            p("Got an error when loading: " + ErrorHandler.getString(e));
        }


    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jButton1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        sliderFrames = new javax.swing.JSlider();
        panMain = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        btnimage = new javax.swing.JButton();
        showbg = new javax.swing.JCheckBox();
        cursors = new javax.swing.JTextField();
        snap = new javax.swing.JButton();
        showuse = new javax.swing.JCheckBox();
        showignore = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        random = new javax.swing.JButton();
        hint = new javax.swing.JButton();

        jButton1.setText(org.openide.util.NbBundle.getMessage(RasterView.class, "RasterView.jButton1.text")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(RasterView.class, "RasterView.jLabel4.text")); // NOI18N

        sliderFrames.setMajorTickSpacing(25);
        sliderFrames.setMaximum(200);
        sliderFrames.setMinorTickSpacing(5);
        sliderFrames.setPaintLabels(true);
        sliderFrames.setPaintTicks(true);
        sliderFrames.setValue(15);
        sliderFrames.setOpaque(false);
        sliderFrames.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderFramesStateChanged(evt);
            }
        });

        panMain.setBackground(new java.awt.Color(102, 102, 102));
        panMain.setMaximumSize(new java.awt.Dimension(400, 400));
        panMain.setMinimumSize(new java.awt.Dimension(300, 300));
        panMain.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        btnimage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/picture-save.png"))); // NOI18N
        btnimage.setText(org.openide.util.NbBundle.getMessage(RasterView.class, "RasterView.btnimage.text")); // NOI18N
        btnimage.setMargin(new java.awt.Insets(1, 1, 1, 1));
        btnimage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnimageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(btnimage, gridBagConstraints);

        showbg.setForeground(new java.awt.Color(0, 0, 204));
        showbg.setText(org.openide.util.NbBundle.getMessage(RasterView.class, "RasterView.showbg.text")); // NOI18N
        showbg.setToolTipText(org.openide.util.NbBundle.getMessage(RasterView.class, "RasterView.showbg.toolTipText")); // NOI18N
        showbg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showbgActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(showbg, gridBagConstraints);

        cursors.setColumns(2);
        cursors.setText(org.openide.util.NbBundle.getMessage(RasterView.class, "RasterView.cursors.text")); // NOI18N
        cursors.setMaximumSize(new java.awt.Dimension(20, 20));
        cursors.setPreferredSize(new java.awt.Dimension(15, 20));
        cursors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cursorsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 6, 0, 0);
        jPanel1.add(cursors, gridBagConstraints);

        snap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/view-grid.png"))); // NOI18N
        snap.setText(org.openide.util.NbBundle.getMessage(RasterView.class, "RasterView.snap.text")); // NOI18N
        snap.setToolTipText(org.openide.util.NbBundle.getMessage(RasterView.class, "RasterView.snap.toolTipText")); // NOI18N
        snap.setMargin(new java.awt.Insets(1, 1, 1, 1));
        snap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                snapActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(snap, gridBagConstraints);

        showuse.setForeground(new java.awt.Color(0, 153, 51));
        showuse.setText(org.openide.util.NbBundle.getMessage(RasterView.class, "RasterView.showuse.text")); // NOI18N
        showuse.setToolTipText(org.openide.util.NbBundle.getMessage(RasterView.class, "RasterView.showuse.toolTipText")); // NOI18N
        showuse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showuseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(showuse, gridBagConstraints);

        showignore.setForeground(new java.awt.Color(153, 0, 0));
        showignore.setText(org.openide.util.NbBundle.getMessage(RasterView.class, "RasterView.showignore.text")); // NOI18N
        showignore.setToolTipText(org.openide.util.NbBundle.getMessage(RasterView.class, "RasterView.showignore.toolTipText")); // NOI18N
        showignore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showignoreActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(showignore, gridBagConstraints);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(RasterView.class, "RasterView.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 0, 0);
        jPanel1.add(jLabel1, gridBagConstraints);

        random.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/process/random16.png"))); // NOI18N
        random.setText(org.openide.util.NbBundle.getMessage(RasterView.class, "RasterView.random.text")); // NOI18N
        random.setToolTipText(org.openide.util.NbBundle.getMessage(RasterView.class, "RasterView.random.toolTipText")); // NOI18N
        random.setMargin(new java.awt.Insets(1, 1, 1, 1));
        random.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                randomActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(random, gridBagConstraints);

        hint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/help-hint.png"))); // NOI18N
        hint.setText(org.openide.util.NbBundle.getMessage(RasterView.class, "RasterView.hint.text")); // NOI18N
        hint.setToolTipText(org.openide.util.NbBundle.getMessage(RasterView.class, "RasterView.hint.toolTipText")); // NOI18N
        hint.setFocusable(false);
        hint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        hint.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        hint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hintActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 11;
        gridBagConstraints.gridy = 0;
        jPanel1.add(hint, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sliderFrames, javax.swing.GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE)
                .addGap(20, 20, 20))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panMain, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(sliderFrames, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(panMain, javax.swing.GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void err(String msg) {
        Logger.getLogger(RasterView.class.getName()).log(Level.SEVERE, msg);
    }

    private void p(String s) {
        System.out.println("RasterView:" + s);
    }
    private void sliderFramesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderFramesStateChanged
        //  if (DOACTIONS) {
        if (moviePanel != null) {
            // p("Setting frame to "+sliderFrames.getValue());
            maincont.setFrame(sliderFrames.getValue());
            moviePanel.setFrame(sliderFrames.getValue());
        }

    }//GEN-LAST:event_sliderFramesStateChanged

    public void redrawImages() {
        moviePanel.redrawImages(showignore.isSelected(), showbg.isSelected(), showuse.isSelected());
        requestFocus();
        moviePanel.repaint();
    }

    private void updateCursors(boolean snap) {
        if (moviePanel == null) {
            return;
        }
        if (maincont == null) {
            return;
        }
        int nr = 1;
        if (this.cursors != null) {
            try {
                nr = Integer.parseInt(cursors.getText());
            } catch (Exception e) {
            }
        }
        maincont.setPreferrednrwidgets(nr);
        moviePanel.checkWidgets(snap);
        moviePanel.repaint();

    }
    private void showignoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showignoreActionPerformed
        redrawImages();
    }//GEN-LAST:event_showignoreActionPerformed

    private void showbgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showbgActionPerformed
        redrawImages();
    }//GEN-LAST:event_showbgActionPerformed

    private void showuseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showuseActionPerformed
        redrawImages();
    }//GEN-LAST:event_showuseActionPerformed

    private void cursorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cursorsActionPerformed
        updateCursors(false);
    }//GEN-LAST:event_cursorsActionPerformed

    private void snapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_snapActionPerformed
        boolean sel = showignore.isSelected() || showbg.isSelected() || showuse.isSelected();
        if (!sel) {
            JOptionPane.showMessageDialog(this, "To snap coordinates to a mask, enable one of the masks (ignmore, bg or use).\nIt will snap them to the first selected mask");
            return;
        }
        updateCursors(true);
    }//GEN-LAST:event_snapActionPerformed

    private void btnimageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnimageActionPerformed
        this.moviePanel.export();
    }//GEN-LAST:event_btnimageActionPerformed

    private void randomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_randomActionPerformed
        if (maincont.getWidgets() == null) {
            JOptionPane.showMessageDialog(this, "Found no widgets");
            return;
        }
        for (Widget w : maincont.getWidgets()) {
            CoordWidget c = (CoordWidget) w;
            int x = (int) (Math.random() * (double)maincont.getRasterSize());
            int y = (int) (Math.random() * (double)maincont.getRasterSize());
            p("Random x/y: "+x+"/"+y);
            WellCoordinate abs = maincont.getAbsDataAreaCoord();
            WellCoordinate rand = new WellCoordinate(abs.getCol() + x, abs.getRow() + y);
            p("rand coord: "+rand);
            c.setCoord(rand);
            maincont.widgetChanged(w);

        }
        this.updateCursors(false);

    }//GEN-LAST:event_randomActionPerformed

    private void hintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hintActionPerformed
        doHintAction();
    }//GEN-LAST:event_hintActionPerformed

    private void doHintAction() {
        String msg = "<html>You can do the following things here:<ul>";
        msg += "<li>drag the cursors around in the left view eith the <b>left</b> mouse button </li>";
        msg += "<li>overaly masks by selecting them in the drop down box above and checking one of the 3 check boxes </li>";
        msg += "<li>move the yellow cursor (main cursor) around:";
        msg += "<ul><li>First, click on it (or the image)</li>";
        msg += "<li>The <b>cursor keys</b> will move it around</li>";
        msg += "<li>The <b>space or tab key</b> will move it to the next flagged coordinate (if a mask is overlayed)</li>";
        msg += "<li>The <b>delete key</b> will delete a flag (if a mask is overlayed)</li>";
        msg += "<li>The <b>insert key</b> will add a flag (if a mask is overlayed)</li>";
        msg += "</ul></li>";
        msg += "<li>change the number of cursors (text field) </li>";        
        msg += "<li>move the image around with the right mouse button</li>";
        msg += "<li>zoom in or out of the image  with a mouse wheel</li>";
        msg += "<li>to change the size (100x100 is default), go to Tools, Options, Explorer Options</li>";
        msg += "</ul></html>";
        JOptionPane.showMessageDialog(this, msg);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnimage;
    private javax.swing.JTextField cursors;
    private javax.swing.JButton hint;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel panMain;
    private javax.swing.JButton random;
    private javax.swing.JCheckBox showbg;
    private javax.swing.JCheckBox showignore;
    private javax.swing.JCheckBox showuse;
    private javax.swing.JSlider sliderFrames;
    private javax.swing.JButton snap;
    // End of variables declaration//GEN-END:variables
}
