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
package com.iontorrent.compview;

import com.iontorrent.expmodel.CompositeExperiment;
import com.iontorrent.expmodel.DatBlock;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.expmodel.LoadDataContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.guiutils.netbeans.OpenWindowAction;
import com.iontorrent.guiutils.wells.CoordSelectionPanel;
import com.iontorrent.main.options.TorrentScoutSettingsPanel;
import com.iontorrent.maskview.CompositeDensityPanel;
import com.iontorrent.rawdataaccess.wells.BfMask;
import com.iontorrent.rawdataaccess.wells.BfMaskFlag;
import com.iontorrent.expmodel.ExperimentLoader;

import com.iontorrent.guiutils.wells.SingleCoordSelectionPanel;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.utils.ProgressListener;
import com.iontorrent.wellmodel.BfHeatMap;
import com.iontorrent.wellmodel.CompositeWellDensity;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellSelection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.progress.ProgressUtils;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.StatusDisplayer.Message;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//com.iontorrent.compview//TorrentScoutCompositeView//EN",
autostore = false)
@TopComponent.Description(preferredID = "TorrentScoutCompositeViewTopComponent",
iconBase = "com/iontorrent/compview/chip_bb.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "top_left", openAtStartup = false)
@ActionID(category = "Window", id = "com.iontorrent.compview.TorrentScoutCompositeViewTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_TorrentScoutCompositeViewAction",
preferredID = "TorrentScoutCompositeViewTopComponent")
public final class TorrentScoutCompositeViewTopComponent extends TopComponent implements TaskListener {

    int MAX_COORDS = 10000;
    private String lastmsg;
    private BfMaskFlag currentflag;
    //private ProgressHandle progress;
    private BfMaskFlag default_flag;
    private boolean DOACTIONS;
    private CompositeExperiment oldContext;
    private CompositeExperiment expContext;
    private GlobalContext global;
    private CacheFilesTask cachetask;
    private WellContext context;
    private CompositeDensityPanel densityPanel;
    private BfHeatMap mask;
    private transient final Lookup.Result<CompositeExperiment> compexpresult =
            LookupUtils.getSubscriber(CompositeExperiment.class, new CompExpListener());
    private transient final Lookup.Result<ExperimentContext> expresult =
            LookupUtils.getSubscriber(ExperimentContext.class, new ExpListener());
    private transient final Lookup.Result<GlobalContext> gContextResults =
            LookupUtils.getSubscriber(GlobalContext.class, new GSubscriberListener());
    private transient final Lookup.Result<LoadDataContext> loadContextResults =
            LookupUtils.getSubscriber(LoadDataContext.class, new LoadDataSubscriberListener());
    //private transient final InstanceContent wellCoordContent = LookupUtils.getPublisher(WellCoordinate.class);
    private transient final InstanceContent wellSelectionContent = LookupUtils.getPublisher(WellSelection.class);
    private transient final InstanceContent expContent = LookupUtils.getPublisher(ExperimentContext.class);
    private transient final InstanceContent wellContextContent = LookupUtils.getPublisher(WellContext.class);
    ExperimentLoader loader;
    private int frame;
    private int flow;
    private RawType type;

    public TorrentScoutCompositeViewTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(TorrentScoutCompositeViewTopComponent.class, "CTL_TorrentScoutCompositeViewTopComponent"));
        setToolTipText(NbBundle.getMessage(TorrentScoutCompositeViewTopComponent.class, "HINT_TorrentScoutCompositeViewTopComponent"));

        DOACTIONS = false;

        BfMaskFlag flags[] = BfMaskFlag.values();
        currentflag = BfMaskFlag.RAW;
        comboFlags.setModel(new javax.swing.DefaultComboBoxModel(flags));
        comboFlags.setSelectedItem(currentflag);

        loader = (ExperimentLoader) WindowManager.getDefault().findTopComponent("ExperimentViewerTopComponent");


        densityPanel = new CompositeDensityPanel(null, loader);
        add("Center", densityPanel);
        DOACTIONS = true;
    }

    private void loadPreferences() {
        // TODO read settings and initialize GUI
        // Example:        
        // someCheckBox.setSelected(Preferences.userNodeForPackage(IonogramOptionsPanel.class).getBoolean("someFlag", false));
        // or for org.openide.util with API spec. version >= 7.4:
        // someCheckBox.setSelected(NbPreferences.forModule(IonogramOptionsPanel.class).getBoolean("someFlag", false));
        // or:
        // someTextField.setText(SomeSystemOption.getDefault().getSomeStringProperty());
        Preferences p = Preferences.userNodeForPackage(TorrentScoutSettingsPanel.class);

        if (global == null) {
            p("NO global context");
            return;
        }

        //bfmask_file = this.expContext.getResultsDirectory() + "bfmask.bin";


        int code = BfMaskFlag.RAW.getCode();
        String val = p.get("default_maskflag", "" + code);

        try {
            code = Integer.parseInt(val);
            if (code > 15) {
                code = 15;
            } else if (code < 0) {
                code = 0;
            }
        } catch (Exception e) {
        }
        default_flag = BfMaskFlag.get(code);
        if (default_flag == null) {
            default_flag = BfMaskFlag.RAW;
        }


    }

    private void savePreferences() {
        Preferences p = Preferences.userNodeForPackage(TorrentScoutSettingsPanel.class);

        if (default_flag != null) {
            p.put("default_maskflag", "" + default_flag.getCode());
        }
        try {
            p.sync();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void msg(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    private void tryToLoad() {
        p("try to load composite bfmask heatmap");

        updateScoresPanel();

    }

    private static void err(String ex) {
        Logger.getLogger(TorrentScoutCompositeViewTopComponent.class.getName()).log(Level.SEVERE, ex);
    }

    @Override
    public void taskDone(Task t) {
        p("Task " + t + " is done");
        setCursor(null);
        //progress.stop();

        if (t.isSuccess()) {
            updateScoresPanel();
        } else {
            msg("The heat map creation task failed - do the required files exist?");
        }
        cachetask = null;

    }

    private class CompExpListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            getLatestCompExp();
        }
    }

    private class ExpListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
          //  getLatestExp();
        }
    }

    private class GSubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            updateGlobal();
        }
    }

    private class LoadDataSubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            tryToLoad();
        }
    }

    private void updateGlobal() {
        global = GlobalContext.getContext();

    }

    private void getLatestCompExp() {
        //   p("Getting latest exp context");
        if (global == null) {
            updateGlobal();
        }
        final Collection<? extends CompositeExperiment> items = compexpresult.allInstances();
        if (!items.isEmpty()) {
            CompositeExperiment data = null;
            Iterator<CompositeExperiment> it = (Iterator<CompositeExperiment>) items.iterator();
            while (it.hasNext()) {
                data = it.next();
            }
            if (data == oldContext && data.getResultsDirectory(null).equalsIgnoreCase(oldContext.getResultsDirectory(null))) {
                p("doing nothing, same exp");
            } else {
                oldContext = data;
                update(oldContext);

            }
        }
    }

//    private void getLatestExp() {
//        p("Getting latest exp context");
//        if (global == null) {
//            updateGlobal();
//        }
//        final Collection<? extends ExperimentContext> items = expresult.allInstances();
//        if (!items.isEmpty()) {
//            ExperimentContext data = null;
//            Iterator<ExperimentContext> it = (Iterator<ExperimentContext>) items.iterator();
//            while (it.hasNext()) {
//                data = it.next();
//            }
//            // change drop down
//
//            if (expContext != null && expContext.getBlocks() != null) {
//                for (DatBlock block : expContext.getBlocks()) {
//                    ExperimentContext ex = expContext.getContext(block, false);
//                    if (ex.getRawDir().equalsIgnoreCase(data.getRawDir())) {
//                        DOACTIONS = false;
//                        blocks.setSelectedItem(block);
//                        DOACTIONS = true;
//                        return;
//                    }
//                }
//            }
//        }
//    }

    private void update(CompositeExperiment result) {
        if (result == null) {
            return;
        }
        this.expContext = result;
        this.DOACTIONS = false;
        blocks.removeAllItems();
        if (expContext.getBlocks() != null) {

            blocks.addItem("Thumbnails");
            for (DatBlock block : expContext.getBlocks()) {
                blocks.addItem(block);
            }


        }
        this.DOACTIONS = true;
        this.requestVisible();
        this.requestActive();


        if (densityPanel != null) {
            densityPanel.clear();
        }
        tryToLoad();

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        comboFlags = new javax.swing.JComboBox();
        btnReload = new javax.swing.JButton();
        spinBucket = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        btnSelect = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        blocks = new javax.swing.JComboBox();
        hint = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jPanel2.setMaximumSize(new java.awt.Dimension(21, 21));
        jPanel2.setMinimumSize(new java.awt.Dimension(21, 21));
        jPanel2.setOpaque(false);
        jPanel2.setLayout(new java.awt.GridBagLayout());

        comboFlags.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutCompositeViewTopComponent.class, "TorrentScoutCompositeViewTopComponent.comboFlags.toolTipText")); // NOI18N
        comboFlags.setMaximumSize(new java.awt.Dimension(100, 32767));
        comboFlags.setMinimumSize(new java.awt.Dimension(70, 21));
        comboFlags.setPreferredSize(new java.awt.Dimension(50, 21));
        comboFlags.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboFlagsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel2.add(comboFlags, gridBagConstraints);

        btnReload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/maskview/view-refresh-3.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnReload, org.openide.util.NbBundle.getMessage(TorrentScoutCompositeViewTopComponent.class, "TorrentScoutCompositeViewTopComponent.btnReload.text")); // NOI18N
        btnReload.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutCompositeViewTopComponent.class, "TorrentScoutCompositeViewTopComponent.btnReload.toolTipText")); // NOI18N
        btnReload.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnReload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReloadActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel2.add(btnReload, gridBagConstraints);

        spinBucket.setModel(new javax.swing.SpinnerNumberModel(4, 1, 100, 1));
        spinBucket.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutCompositeViewTopComponent.class, "TorrentScoutCompositeViewTopComponent.spinBucket.toolTipText")); // NOI18N
        spinBucket.setMaximumSize(new java.awt.Dimension(40, 32767));
        spinBucket.setMinimumSize(new java.awt.Dimension(35, 20));
        spinBucket.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinBucketStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel2.add(spinBucket, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(TorrentScoutCompositeViewTopComponent.class, "TorrentScoutCompositeViewTopComponent.jLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanel2.add(jLabel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        jPanel2.add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(TorrentScoutCompositeViewTopComponent.class, "TorrentScoutCompositeViewTopComponent.jLabel4.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel2.add(jLabel4, gridBagConstraints);

        btnSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/select-rectangular.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSelect, org.openide.util.NbBundle.getMessage(TorrentScoutCompositeViewTopComponent.class, "TorrentScoutCompositeViewTopComponent.btnSelect.text")); // NOI18N
        btnSelect.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutCompositeViewTopComponent.class, "TorrentScoutCompositeViewTopComponent.btnSelect.toolTipText")); // NOI18N
        btnSelect.setMargin(new java.awt.Insets(1, 1, 1, 1));
        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        jPanel2.add(btnSelect, gridBagConstraints);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/maskview/document-export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(TorrentScoutCompositeViewTopComponent.class, "TorrentScoutCompositeViewTopComponent.jButton2.text")); // NOI18N
        jButton2.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutCompositeViewTopComponent.class, "TorrentScoutCompositeViewTopComponent.jButton2.toolTipText")); // NOI18N
        jButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        jPanel2.add(jButton2, gridBagConstraints);

        blocks.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "no blocks found" }));
        blocks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blocksActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel2.add(blocks, gridBagConstraints);

        hint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/help-hint.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(hint, org.openide.util.NbBundle.getMessage(TorrentScoutCompositeViewTopComponent.class, "TorrentScoutCompositeViewTopComponent.hint.text")); // NOI18N
        hint.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutCompositeViewTopComponent.class, "TorrentScoutCompositeViewTopComponent.hint.toolTipText")); // NOI18N
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
        jPanel2.add(hint, gridBagConstraints);

        add(jPanel2, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void comboFlagsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboFlagsActionPerformed
        if (DOACTIONS == false) {
            //   msg("doactions is false not doing action");
            return;
        }
        DOACTIONS = false;

        currentflag = (BfMaskFlag) this.comboFlags.getSelectedItem();
        //msg("Currentflag: "+currentflag);
        if (currentflag == null) {
            return;
        }
        //  p("Flag got selected:" + currentflag);
        comboFlags.setToolTipText(currentflag.getDescription());
        if (densityPanel.getContext() == null) {
            update();
        } else {
            //    msg("setting flag in density panel");
            densityPanel.setFlag(currentflag);
        }
        DOACTIONS = true;
}//GEN-LAST:event_comboFlagsActionPerformed

    private void btnReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReloadActionPerformed
        update();
}//GEN-LAST:event_btnReloadActionPerformed

    private void spinBucketStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinBucketStateChanged
        update();
}//GEN-LAST:event_spinBucketStateChanged

    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        if (context == null || context.getMask() == null) {
            GuiUtils.showNonModalMsg("Got no well context or bfmask file");
            return;
        }
        SingleCoordSelectionPanel pan = new SingleCoordSelectionPanel();
        pan.setMaxX(this.context.getNrCols());
        pan.setMaxY(this.context.getNrRows());
        int ans = JOptionPane.showConfirmDialog(this, pan, "Enter a selection:", JOptionPane.OK_CANCEL_OPTION);
        if (ans == JOptionPane.CANCEL_OPTION) {
            return;
        }

        WellCoordinate c1 = pan.getCoord1();
        //   WellCoordinate c2 = pan.getCoord2();
        DatBlock block = expContext.findBlock(c1);
        if (block != null) {
            ExperimentContext context = expContext.getContext(block, true);
            WellCoordinate rel = c1.subtract(block.getStart());
            WellSelection sel = new WellSelection(rel, rel);
            context.getWellContext().setCoordinate(rel);
            context.getWellContext().setSelection(sel);
            //  context.getWellContext().setWellSelection(new WellSelection(block.getStart(), block.getEnd()));
            publishExpContext(context);
            LookupUtils.publish(wellSelectionContent, sel);
        }

    }//GEN-LAST:event_btnSelectActionPerformed
    protected void publishExpContext(ExperimentContext exp) {
        if (exp != null) {
            GuiUtils.showNonModalMsg("Sending ExperimentContext for selected block: " + exp.getResultsDirectory());
            p("publishExpContext: Got a ExperimentContext: " + exp);
            GlobalContext.getContext().setExperimentContext(exp, false);
            LookupUtils.publish(expContent, exp);

            if (loader != null) {
                loader.maybeLoadExperiment(exp);
            } else {
                JOptionPane.showMessageDialog(this, "Got no expreiment loader, could not load block");
            }
            WellContext wellcontext = exp.createWellContext();
            if (wellcontext != null) {
                wellcontext.setCoordinate(new WellCoordinate(0, 0));
                LookupUtils.publish(wellContextContent, wellcontext);
            } else {
                p("I was unable to crate well context");
            }
        }
    }
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        this.densityPanel.export();
}//GEN-LAST:event_jButton2ActionPerformed

    private void blocksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blocksActionPerformed
        if (!DOACTIONS) {
            return;
        }
        Object o = blocks.getSelectedItem();
        ExperimentContext exp = null;
        if (o instanceof DatBlock) {
            DatBlock block = (DatBlock) o;
            exp = this.expContext.getContext(block, true);
        } else {
            exp = this.expContext.getThumbnailsContext(true);
        }
        if (exp != null) {
            GuiUtils.showNonModalMsg("Loading data for block " + o);
            publishExpContext(exp);
        } else {
            p("Got no context fpr " + o);
        }
    }//GEN-LAST:event_blocksActionPerformed

    private void hintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hintActionPerformed

        doHintAction();     }//GEN-LAST:event_hintActionPerformed

     private void doHintAction() {
        String msg="<html><ul>";
        msg+="<li>Move the image around with the <b>right</b> mouse button </li>";
        msg+="<li>Select a coordinate by clicking the <b>left</b> mouse button </li>";
        msg+="<li>Zoom the image in and out with a <b>mouse wheel</b> </li>";        
        msg += "</ul></html>";
        JOptionPane.showMessageDialog(this, msg);    
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox blocks;
    private javax.swing.JButton btnReload;
    private javax.swing.JButton btnSelect;
    private javax.swing.JComboBox comboFlags;
    private javax.swing.JButton hint;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSpinner spinBucket;
    // End of variables declaration//GEN-END:variables

    private void update() {
        updateScoresPanel();
    }

    private void updateScoresPanel() {

        if (expContext == null) {
            this.getLatestCompExp();
        }
        if (expContext == null) {
            this.setStatusWarning("Got no composite experiment context - load a black bird data set first");
            return;
        } else {
            // p("Got composite context: " + expContext.getr);
        }

        if (expContext.getNrBlocks() < 1) {
            String msg = "I see no blocks in " + expContext.getRootContext().getRawDir() + ",\nPlease check your path settings!";
            JOptionPane.showMessageDialog(this, msg);
            this.setStatusWarning(msg);
            return;
        }

        this.currentflag = (BfMaskFlag) this.comboFlags.getSelectedItem();
        p("Got currentflag: " + currentflag);
        // DEPENDS ON FLAG
        mask = BfHeatMap.getMask(expContext.getRootContext());

        String msg = "";
        boolean has = false;
        // change flow and frame, make it user option!
        flow = 0;
        frame = 0;
        type = RawType.ACQ;
        if (currentflag == BfMaskFlag.RAW) {
            has = mask.hasImage("composite", currentflag, flow, type, frame);
            p("checking raw file: " + mask.getImageFile("composite",currentflag, flow, type, frame));
        } else {
            has = mask.hasImage(currentflag);
        }
        if (!has) {
            p("Don't have image yet for " + currentflag);
            GuiUtils.showNonModalMsg("Need to create the heat map(s) first...");
            if (cachetask == null) {
                msg = "The hat map file " + mask.getFile(currentflag) + " does not seem to exist, I will have to generate it first. This might take a few minutes!";
                setStatusWarning(msg);
                // boolean ok = createImageFileFromScoreFlag();
                //GuiUtils.showNonModalMsg("CompositeHeatMap: I have to create the composite heat maps first...", false, 10);
                //   this.setStatusWarning("I have to parse some files and create images for the scores...");
                ProgressHandle handle = ProgressHandleFactory.createHandle("Black bird: Creating composite image for total chip view for all blocks...");

                cachetask = new CacheFilesTask(this, handle);
                //  ProgressUtils.showProgressDialogAndRunLater(cachetask, "Creating custom heat maps...");
                cachetask.execute();

            } else {
                msg = "Currently creating heat map(s) for " + currentflag + " ... please wait...";
                GuiUtils.showNonModalMsg("I am already creating the heat maps, please wait...", 10);
                setStatusWarning(msg);

            }
            return;
        }
        //   this.setStatus("Updating heat map");
        this.updateDensityPanel();

    }

    private void updateDensityPanel() {
        GuiUtils.showNonModalMsg("Black Bird: reading complete block heat map for " + currentflag + "....", false, 5);
        if (currentflag == BfMaskFlag.RAW) {
            String file = mask.getImageFile("composite", BfMaskFlag.RAW, flow, type, frame);
            p("Reading RAW file: " + file);
            mask.readData(BfMaskFlag.RAW, file);
        } else {
            mask.readData(currentflag);
        }
        if (densityPanel == null) {
            if (loader == null) {
                loader = (ExperimentLoader) WindowManager.getDefault().findTopComponent("ExperimentViewerTopComponent");
            }
            densityPanel = new CompositeDensityPanel(this.expContext, loader);
            add("Center", densityPanel);
        } else {
            densityPanel.setCompExp(expContext);
        }

        // SELECT FLOW
        int flow = 0;
        densityPanel.setScoreMask(mask, currentflag, (Integer) spinBucket.getValue(), getType(), flow, frame);

    }

    private RawType getType() {
        return RawType.ACQ;
    }

    private class CacheFilesTask extends Task {

        boolean ok = false;

        public CacheFilesTask(TaskListener tlistener, ProgressHandle handle) {
            super(tlistener, handle);

        }

        @Override
        public Void doInBackground() {

            ok = createImageFileFromScoreFlag(this);
            return null;
        }

        public boolean isSuccess() {
            return ok;
        }
    }

    private boolean createImageFileFromScoreFlag(ProgressListener progress) {
        p("Creating image file for flag " + currentflag);
        if (mask == null) {
            mask = BfHeatMap.getMask(expContext.getRootContext());
        }

        /// XXX SET FRAME
        int flow = 0;
        //XXX SELECT FLOW
        CompositeWellDensity gen = new CompositeWellDensity(this.expContext, getType(), flow, frame);
        String msg = null;
        try {
            msg = gen.createCompositeImages(progress, mask.getImageFile("composite", currentflag, flow, type, frame));
        } catch (Exception e) {
            msg = e.getMessage();

            return false;
        }
        if (gen.getNrFlags() == 1) {
            p("Got just .dat files, using raw ");
            this.currentflag = BfMaskFlag.RAW;
        }
        if (msg != null && msg.length() > 0) {
            if (lastmsg == null || !msg.equalsIgnoreCase(lastmsg)) {
                msg(msg);
            }
            lastmsg = msg;
            return false;
        }


        return true;
    }

    @Override
    public Action[] getActions() {
        return OpenWindowAction.getActions(this);
    }

    @Override
    public void componentOpened() {

        DOACTIONS = false;
        loadPreferences();
        getLatestCompExp();

        //   if (default_flag == null) {
        default_flag = BfMaskFlag.RAW;
        //  }

        comboFlags.setSelectedItem(default_flag);

        DOACTIONS = true;


    }

    private void setStatus(String msg) {
        p(msg);
        Message message = StatusDisplayer.getDefault().setStatusText("<html>" + msg + "</html>", StatusDisplayer.IMPORTANCE_FIND_OR_REPLACE);
        message.clear(30000);
    }

    private void setStatusWarning(String msg) {
        p(msg);
        Message message = StatusDisplayer.getDefault().setStatusText("<html>" + msg + "</html>", StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        message.clear(60000);
    }

    private void setStatusError(String msg) {
        err(msg);
        Message message = StatusDisplayer.getDefault().setStatusText("<html>" + msg + "</html>", StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        message.clear(120000);
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
        savePreferences();
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");

        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {

        String version = p.getProperty("version");

    }

    private void p(String msg) {
        System.out.println("TorrentScoutCompositeViewTopComponent: " + msg);
    }

    private void err(String msg, Exception e) {
        Logger.getLogger(TorrentScoutCompositeViewTopComponent.class.getName()).log(Level.SEVERE, msg, e);
    }

    private void warn(String msg) {
        Logger.getLogger(TorrentScoutCompositeViewTopComponent.class.getName()).log(Level.WARNING, msg);
    }
}
