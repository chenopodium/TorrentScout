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
 * HistoView.java
 *
 * Created on 05.11.2011, 08:43:23
 */
package com.iontorrent.torrentscout.explorer.fit;

import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.rawdataaccess.wells.BitMask;
import com.iontorrent.torrentscout.explorer.ContextChangeAdapter;
import com.iontorrent.torrentscout.explorer.ExplorerContext;
import com.iontorrent.utils.io.FileTools;
import com.iontorrent.utils.stats.StatPoint;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.openide.util.NbPreferences;

/**
 *
 * @author Chantal Roth
 */
public class HistoView extends javax.swing.JPanel implements ActionListener {

    ExplorerContext maincont;
    FitFunctionsPanel fitpanel;
    HistoPanel panel;
    HistoPanel detail;
    AbstractHistoFunction curfunction;
    StatPoint datapoints;
    double[][] histodata;
    boolean normalize = true;
    boolean second = true;
    double maxx = Double.MAX_VALUE;
    double minx = Double.MIN_VALUE;
    int bins;
    JTextField tmin;
    JTextField tmax;

    //  BitMask selectedmask;
    /** Creates new form HistoView */
    public HistoView(ExplorerContext maincont) {
        initComponents();
        this.maincont = maincont;
        fitpanel = new FitFunctionsPanel(maincont, null);
        for (AbstractHistoFunction f : FitFunctionsFactory.getFunctions(maincont)) {
            this.boxFunc.addItem(f);
        }
        if (tmin == null) {
            tmin = new JTextField();
        }
        if (tmax == null) {
            tmax = new JTextField();
        }

        this.tmax.setText("10000");
        this.tmin.setText("-1000");
        //   this.boxFunc.addItem(new GaussFunction(maincont));
        boxFunc.addActionListener(this);
        recreateMasksDropdown();
        maincont.addListener(
                new ContextChangeAdapter() {

                    @Override
                    public void maskAdded(BitMask mask) {
                        boxuse.addItem(mask);

                    }

                    @Override
                    public void masksChanged() {
                        recreateMasksDropdown();

                    }
                });
        update(false, !add.isSelected());

    }

    private void doHintAction() {
        String msg = "<html>You can do the following things here:<ul>";
        msg += "<li>Pick a function to classify wells.<br>"
                + "Some cases (integral), it will simply compute the value based on the data as you would expect from the name of the function<br>"
                + "Where functions are used such as peak or parameterized adjustment, it will usually compute the <b>root of the sum of error squared</b><br>"
                + "where error means simply the difference between value from the function and value from the data</li>";
        msg += "<li>You can then move the scissors around to define the area you want to use to create a mask from</li>";
        msg += "<li>To create a mask with these wells, click on the mask icon. It will then use all wells that are:<br>"
                + "- in the selected mask (from the drop down)<br>"
                + "- and are in the selected range (scissors)</li>";
        msg += "<li>You can change the min and max x coordinate in the view eith the blue and red line icons</li>";
        msg += "<li>by clicking the add button, it will add the next histogram to the existing histogram,<br>"
                + "this helps to see differences between functions or between masks</li>";
        msg += "<li>You can also export the data to file with the save icon</li>";
        msg += "<li>In menu tools, options, explorer options you can change the y axis (percent or absolute value) and the nr of bins</li>";
        msg += "<li>with the tools icon you can change the parameters and shapes of some functiosn (see also process component)</li>";
        msg += "</ul></html>";
        JOptionPane.showMessageDialog(this, msg);
    }

    public void getUserPreferences() {
        if (maincont == null) {
            return;
        }
        Preferences p = NbPreferences.forModule(com.iontorrent.torrentscout.explorer.options.TorrentExplorerPanel.class);
        bins = p.getInt("bins", 50);
        second = p.getBoolean("second", true);
        normalize = p.getBoolean("normalize", true);

    }

    public void recreateMasksDropdown() {
        boxuse.removeAllItems();
        boxuse.addItem("Use all wells");
        if (maincont.getMasks() != null && maincont.getMasks().size() > 0) {
            for (BitMask m : maincont.getMasks()) {
                this.boxuse.addItem(m);

            }
            if (maincont.getHistoMask() != null) {
                boxuse.setSelectedItem(maincont.getHistoMask());
            } else {
                boxuse.setSelectedItem(0);
            }
        }

    }

    @Override
    public void repaint() {
        super.repaint();
        if (panel != null) {
            panel.repaint();
        }
    }

    private void update(boolean useminmax, boolean createNewStats) {
        if (panel == null) {
            createNewStats = true;
        }
        if (panel != null && createNewStats) {
            remove(panel);
        }
        curfunction = (AbstractHistoFunction) boxFunc.getSelectedItem();
        if (curfunction == null) {
            curfunction = (AbstractHistoFunction) boxFunc.getItemAt(0);
        }
        BitMask histomask = maincont.getHistoMask();
        if (boxuse.getSelectedItem() instanceof BitMask) {
            histomask = (BitMask) boxuse.getSelectedItem();
        } else {
            histomask = null;
        }
        maincont.setHistoMask(histomask);

        String msg = "Computing between (green) " + maincont.getStartframe() + "-" + maincont.getEndframe() + ", and end (red) frame " + maincont.getCropright();
        if (histomask != null) {
            double perc = histomask.computePercentage();

            if (perc < 10) {
                GuiUtils.showNonModalMsg("Got only " + perc + " wells for 'use' mask " + histomask);
            }
        }
        if (histomask == null) {
            GuiUtils.showNonModalMsg(msg+" using ALL wells");
        } else {
            GuiUtils.showNonModalMsg(msg + " using only wells from mask " + histomask);
        }



        getUserPreferences();
        updateFunction(curfunction, useminmax);
        //   detail = new HistoPanel(maincont, datapoints, histodata);
        if (createNewStats) {
            panel = new HistoPanel(maincont, datapoints, histodata, bins, normalize, second);
            add("Center", panel);
        } else {
            panel.addStats(datapoints);
        }
        // xxx link panel with detail

        invalidate();
        revalidate();
        //    panMain.add("South", detail);
        //  panel.paintImmediately(0, 0, 1000, 1000);
        // GuiUtils.showNonModalMsg("Computing histogram done");
        repaint();
        panel.repaint();
        paintImmediately(0, 0, 1000, 1000);
        //  addMaskSelection();
    }

    private int getInt(JTextField t) {
        if (t == null || t.getText() == null) {
            return 0;
        }
        int f = 0;
        try {
            f = Integer.parseInt(t.getText());
        } catch (Exception e) {
        }
        return f;
    }

    private double getDouble(JTextField t) {
        if (t == null || t.getText() == null) {
            return 0;
        }
        double f = 0;
        try {
            f = Double.parseDouble(t.getText());
        } catch (Exception e) {
        }
        return f;
    }

    public void updateFunction(AbstractHistoFunction func, boolean useminmax) {
        this.curfunction = func;

        minx = getDouble(tmin);
        maxx = getDouble(tmax);
        if (panel != null && useminmax) {
            minx = Math.max(minx, panel.getLeftX());
            maxx = Math.min(panel.getRightX(), maxx);
        }
        if (minx > maxx) {
            double tmp = minx;
            minx = maxx;
            maxx = tmp;
        }
        func.setMinx(minx);
        func.setMaxx(maxx);

        func.execute();
        datapoints = func.getDataPoints();
        histodata = func.getResult();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bar = new javax.swing.JToolBar();
        boxFunc = new javax.swing.JComboBox();
        config = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        boxuse = new javax.swing.JComboBox();
        autoscale = new javax.swing.JButton();
        minmax = new javax.swing.JButton();
        add = new javax.swing.JCheckBox();
        compute = new javax.swing.JButton();
        cut = new javax.swing.JButton();
        btnMask = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnimage = new javax.swing.JButton();
        hint = new javax.swing.JButton();

        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        bar.setRollover(true);

        boxFunc.setToolTipText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.boxFunc.toolTipText")); // NOI18N
        boxFunc.setMaximumSize(new java.awt.Dimension(130, 18));
        boxFunc.setMinimumSize(new java.awt.Dimension(53, 18));
        boxFunc.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                boxFuncMouseMoved(evt);
            }
        });
        boxFunc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxFuncActionPerformed(evt);
            }
        });
        bar.add(boxFunc);

        config.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/configure-3.png"))); // NOI18N
        config.setText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.config.text")); // NOI18N
        config.setToolTipText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.config.toolTipText")); // NOI18N
        config.setFocusable(false);
        config.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        config.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        config.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configActionPerformed(evt);
            }
        });
        bar.add(config);

        jLabel3.setText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.jLabel3.text")); // NOI18N
        bar.add(jLabel3);

        boxuse.setToolTipText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.boxuse.toolTipText")); // NOI18N
        boxuse.setMaximumSize(new java.awt.Dimension(100, 20));
        boxuse.setMinimumSize(new java.awt.Dimension(70, 18));
        boxuse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxuseActionPerformed(evt);
            }
        });
        bar.add(boxuse);

        autoscale.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/process/minmax.png"))); // NOI18N
        autoscale.setText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.autoscale.text")); // NOI18N
        autoscale.setToolTipText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.autoscale.toolTipText")); // NOI18N
        autoscale.setFocusable(false);
        autoscale.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        autoscale.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        autoscale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoscaleActionPerformed(evt);
            }
        });
        bar.add(autoscale);

        minmax.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/process/snap-node.png"))); // NOI18N
        minmax.setText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.minmax.text")); // NOI18N
        minmax.setToolTipText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.minmax.toolTipText")); // NOI18N
        minmax.setFocusable(false);
        minmax.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        minmax.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        minmax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minmaxActionPerformed(evt);
            }
        });
        bar.add(minmax);

        add.setText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.add.text")); // NOI18N
        add.setToolTipText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.add.toolTipText")); // NOI18N
        add.setFocusable(false);
        add.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        bar.add(add);

        compute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/fit/insert-chart-bar.png"))); // NOI18N
        compute.setText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.compute.text")); // NOI18N
        compute.setToolTipText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.compute.toolTipText")); // NOI18N
        compute.setFocusable(false);
        compute.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        compute.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        compute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                computeActionPerformed(evt);
            }
        });
        bar.add(compute);

        cut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/fit/edit-cut-5.png"))); // NOI18N
        cut.setText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.cut.text")); // NOI18N
        cut.setToolTipText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.cut.toolTipText")); // NOI18N
        cut.setFocusable(false);
        cut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cutActionPerformed(evt);
            }
        });
        bar.add(cut);

        btnMask.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/mask.png"))); // NOI18N
        btnMask.setText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.btnMask.text")); // NOI18N
        btnMask.setToolTipText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.btnMask.toolTipText")); // NOI18N
        btnMask.setFocusable(false);
        btnMask.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnMask.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnMask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMaskActionPerformed(evt);
            }
        });
        bar.add(btnMask);

        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/view-refresh-3.png"))); // NOI18N
        btnRefresh.setText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.btnRefresh.text")); // NOI18N
        btnRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.btnRefresh.toolTipText")); // NOI18N
        btnRefresh.setFocusable(false);
        btnRefresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRefresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });
        bar.add(btnRefresh);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/maskview/document-export.png"))); // NOI18N
        btnSave.setText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.btnSave.text")); // NOI18N
        btnSave.setToolTipText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.btnSave.toolTipText")); // NOI18N
        btnSave.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        bar.add(btnSave);

        btnimage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/picture-save.png"))); // NOI18N
        btnimage.setText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.btnimage.text")); // NOI18N
        btnimage.setToolTipText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.btnimage.toolTipText")); // NOI18N
        btnimage.setFocusable(false);
        btnimage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnimage.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnimage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnimageActionPerformed(evt);
            }
        });
        bar.add(btnimage);

        hint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/help-hint.png"))); // NOI18N
        hint.setText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.hint.text")); // NOI18N
        hint.setToolTipText(org.openide.util.NbBundle.getMessage(HistoView.class, "HistoView.hint.toolTipText")); // NOI18N
        hint.setFocusable(false);
        hint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        hint.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        hint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hintActionPerformed(evt);
            }
        });
        bar.add(hint);

        add(bar, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        if (panel != null) {
            panel.repaint();
        }
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void boxFuncMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_boxFuncMouseMoved

        if (curfunction != null) {
            boxFunc.setToolTipText(curfunction.getHtmlDesc());
        }
    }//GEN-LAST:event_boxFuncMouseMoved

    private void btnMaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMaskActionPerformed
        if (panel == null) {
            return;
        }
        minx = panel.getLeftX();        
            maxx = panel.getRightX();

        if (minx > maxx) {
            double tmp = minx;
            minx = maxx;
            maxx = tmp;
        }
        p("left: " + minx + ", right: " + maxx);

        String name = JOptionPane.showInputDialog(this, "Name the new mask: ", curfunction.getName());
        if (name == null || name.trim().length() < 1) {
            return;
        }
        BitMask mask = curfunction.createMask(maincont.getHistoMask(), minx, maxx);
        DecimalFormat f = new DecimalFormat("#.###");
        String msg = "<html>Created mask <b>" + mask.getName() + "</b> with " + mask.computePercentage() + "% wells<br>using interval " + f.format(minx) + " - " + f.format(maxx) + "<br>and ";
        if (maincont.getHistoMask() != null) {
            msg += " using wells <b>only from mask " + maincont.getHistoMask() + "</b>";
        } else {
            msg += "using <b>all wells</b>";
        }
        msg += "</html>";
        JOptionPane.showMessageDialog(this, msg);
        mask.setName(name);
        maincont.addMask(mask);
    }//GEN-LAST:event_btnMaskActionPerformed

    private void computeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_computeActionPerformed
        update(false, !add.isSelected());
    }//GEN-LAST:event_computeActionPerformed

    private void boxuseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxuseActionPerformed
        maskUseeSelected();
    }

    public void maskUseeSelected() {
        if (this.boxuse.getSelectedItem() instanceof BitMask) {
            BitMask mask = (BitMask) this.boxuse.getSelectedItem();
            if (mask != maincont.getHistoMask()) {
                maincont.setHistoMask(mask);

            }
        } else {
            maincont.setHistoMask(null);
        }
    }
        // TODO add your handling code here:}//GEN-LAST:event_boxuseActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        String file = FileTools.getFile("Save histogram to file", ".csv", null, true);
        if (file != null) {
            this.export(file);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void cutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cutActionPerformed
        update(true, !add.isSelected());
    }//GEN-LAST:event_cutActionPerformed

    private void boxFuncActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxFuncActionPerformed
        update(false, !add.isSelected());
    }//GEN-LAST:event_boxFuncActionPerformed

    private void btnimageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnimageActionPerformed
        this.panel.exportImage();
    }//GEN-LAST:event_btnimageActionPerformed

    private void autoscaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoscaleActionPerformed
        this.tmax.setText("1000000");
        this.tmin.setText("-1000000");
        update(false, !add.isSelected());

    }//GEN-LAST:event_autoscaleActionPerformed

    private void minmaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minmaxActionPerformed
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel("Minimum X:"));
        p.add(tmin);
        p.add(new JLabel("Maximum X:"));
        p.add(tmax);
        int ans = JOptionPane.showConfirmDialog(this, p, "Select min/max values", JOptionPane.OK_CANCEL_OPTION);
        if (ans == JOptionPane.CANCEL_OPTION) {
            return;
        }
        update(false, !add.isSelected());

    }//GEN-LAST:event_minmaxActionPerformed

    private void configActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configActionPerformed

        if (curfunction != null) {
            fitpanel.select(curfunction);
        }
        JOptionPane.showMessageDialog(this, fitpanel, "Select the parameters for the fit functions", JOptionPane.QUESTION_MESSAGE);
        fitpanel.parseParameters();

    }//GEN-LAST:event_configActionPerformed

    private void hintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hintActionPerformed
        doHintAction();
    }//GEN-LAST:event_hintActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox add;
    private javax.swing.JButton autoscale;
    private javax.swing.JToolBar bar;
    private javax.swing.JComboBox boxFunc;
    private javax.swing.JComboBox boxuse;
    private javax.swing.JButton btnMask;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnimage;
    private javax.swing.JButton compute;
    private javax.swing.JButton config;
    private javax.swing.JButton cut;
    private javax.swing.JButton hint;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton minmax;
    // End of variables declaration//GEN-END:variables

    @Override
    public void actionPerformed(ActionEvent e) {
        curfunction = (AbstractHistoFunction) boxFunc.getSelectedItem();
        if (curfunction != null) {
            boxFunc.setToolTipText(curfunction.getHtmlDesc());
        }
    }

    public boolean export(String file) {
        return this.panel.export(file);
    }

    private void p(String string) {
        System.out.println("HistoView: " + string);
    }
}
