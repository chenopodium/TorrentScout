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
package com.iontorrent.scoreview;

import com.iontorrent.heatmaps.ScoreMaskGenerator;
import com.iontorrent.guiutils.netbeans.OpenWindowAction;
import com.iontorrent.rawdataaccess.wells.ScoreMaskFlag;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.expmodel.LoadDataContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.guiutils.MinMaxPanel;
import com.iontorrent.guiutils.wells.CoordSelectionPanel;
import com.iontorrent.heatmaps.Parameter;
import com.iontorrent.heatmaps.ScoreMaskCalculatorIF;
import com.iontorrent.results.scores.ScoreMask;
import com.iontorrent.sequenceloading.SequenceLoader;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellSelection;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.StatusDisplayer.Message;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.InstanceContent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//com.iontorrent.scoreview//TorrentScoutScoresView//EN",
autostore = false)
@TopComponent.Description(preferredID = "TorrentScoutScoresViewTopComponent",
iconBase = "com/iontorrent/scoreview/chip.png",
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "right_editor_mode", openAtStartup = false)
@ActionID(category = "Window", id = "com.iontorrent.scoreview.TorrentScoutScoresViewTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_TorrentScoutScoresViewAction",
preferredID = "TorrentScoutScoresViewTopComponent")
public final class TorrentScoutScoresViewTopComponent extends TopComponent implements TaskListener {

    int MAX_COORDS = 10000;
    private boolean DOACTIONS;
    private ExperimentContext oldContext;
    private ExperimentContext expContext;
    private WellContext wellContext;
    private GlobalContext global;
    private CustomScorePanel cust;
    private ScoreMask mask;
    private transient final Lookup.Result<LoadDataContext> loadContextResults =
            LookupUtils.getSubscriber(LoadDataContext.class, new LoadDataSubscriberListener());
    private transient final Lookup.Result<ExperimentContext> expContextResults =
            LookupUtils.getSubscriber(ExperimentContext.class, new SubscriberListener());
    private transient final Lookup.Result<WellContext> wContextResults =
            LookupUtils.getSubscriber(WellContext.class, new WSubscriberListener());
    private ScoreDensityPanel densityPanel;
    private ScoreMaskFlag currentflag;
    private String lastmsg;
    CacheFilesTask cachetask;
    ComputeHeatMapTask customtask;
    private transient final InstanceContent wellSelectionContent = LookupUtils.getPublisher(WellSelection.class);

    public TorrentScoutScoresViewTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(TorrentScoutScoresViewTopComponent.class, "CTL_TorrentScoutScoresViewTopComponent"));
        setToolTipText(NbBundle.getMessage(TorrentScoutScoresViewTopComponent.class, "HINT_TorrentScoutScoresViewTopComponent"));

        DOACTIONS = false;
        densityPanel = new ScoreDensityPanel(null);
        add("Center", densityPanel);

        ScoreMaskFlag flags[] = ScoreMaskFlag.values();
        currentflag = ScoreMaskFlag.Q47LEN;
        boxflag.setModel(new javax.swing.DefaultComboBoxModel(flags));
        boxflag.setSelectedItem(currentflag);

        DOACTIONS = true;
        //   tryToLoad();
    }

    protected void componentedActivated() {
        super.componentActivated();

    }

    private class SubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            getLatestExperimentContext();
        }
    }

    private class LoadDataSubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            if (wellContext != null) {
                if (!wellContext.is316() && !wellContext.is318()) {
                    tryToLoad();
                }
            }
        }
    }

    private class GSubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            updateGlobal();
        }
    }

    private class WSubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            getLatestWellContext();
        }
    }

    @Override
    public Action[] getActions() {
        return OpenWindowAction.getActions(this);
    }

    private void updateGlobal() {
        global = GlobalContext.getContext();
        if (global != null) {
        } else {
            p("Got no global context, it is null!");
        }
    }

    private void tryToLoad() {
//        if (global == null) {
//            GlobalContext.getContext();
//        }
//        if (global != null && global.getExperimentContext() != null && wellContext != null) {
//            mask = ScoreMask.getMask(global.getExperimentContext(), wellContext);
         //   if (mask != null ) {
                updateScoresPanel();
   //         }
    //    }
    }

    private void getLatestWellContext() {
        final Collection<? extends WellContext> items = wContextResults.allInstances();
        if (!items.isEmpty()) {
            WellContext data = null;
            Iterator<WellContext> it = (Iterator<WellContext>) items.iterator();
            while (it.hasNext()) {
                data = it.next();
            }
            update(data);
        }
    }

    private void getLatestExperimentContext() {
        final Collection<? extends ExperimentContext> items = expContextResults.allInstances();
        if (!items.isEmpty()) {
            ExperimentContext data = null;
            Iterator<ExperimentContext> it = (Iterator<ExperimentContext>) items.iterator();
            while (it.hasNext()) {
                data = it.next();
            }

            oldContext = data;
            update(oldContext);

        }
    }

    private void update(ExperimentContext result) {
        this.expContext = result;
        densityPanel.clear();
        cachetask = null;
      //  if (!result.is316() && !result.is318()) {
            tryToLoad();
      //  }

    }
    @Override
    public void repaint() {
        p("repaint called");
        super.repaint();
        if (densityPanel != null) {
            p("repaint of density panel called");
            densityPanel.repaint();
        }
    }

    private void update(WellContext context) {
        this.wellContext = context;
        //  tryToLoad();

    }

    private class CacheFilesTask extends Task {

        boolean ok = false;

        public CacheFilesTask(TaskListener tlistener) {
            super(tlistener, ProgressHandleFactory.createHandle("Score Heat Maps: Creating image files for the selected flag.."));
        }

        @Override
        public Void doInBackground() {

            ok = createImageFileFromScoreFlag();
            return null;
        }

        public boolean isSuccess() {
            return ok;
        }
    }

    private boolean createImageFileFromScoreFlag() {
        p("Creating image file for flag " + currentflag);
        if (mask == null) {
            mask = ScoreMask.getMask(global.getExperimentContext(), wellContext);
        }
        ScoreMaskGenerator gen = new ScoreMaskGenerator(mask, this.expContext);
        String msg = gen.generateImageFiles(currentflag);
        if (msg != null && msg.length() > 0) {
            if (lastmsg == null || !msg.equalsIgnoreCase(lastmsg)) {
                msg(msg);
            }
            lastmsg = msg;
            return false;
        }
        return true;
    }

    private void msg(String msg) {
        //JOptionPane.showMessageDialog(this, "<html>Scores View Heat Map:<br>"+msg+"</html>");
        GuiUtils.showNonModelMsg("Scores heat map message:", "<html>Scores View Heat Map:<br>" + msg + "</html>", true, 5);
    }

    @Override
    public void taskDone(Task t) {
        p("Task " + t + " is done");
        setCursor(null);
        //progress.stop();
        if (t instanceof ComputeHeatMapTask) {
            ComputeHeatMapTask ct = (ComputeHeatMapTask) t;
            if (t.isSuccess()) {
                customtask = null;
            } else {
                msg("The heat map creation task " + t + " failed");
            }
            this.boxflag.setSelectedItem(ct.calc.getFlag());
        } else {
            if (t.isSuccess()) {
                cachetask = null;
            } else {
                msg("The heat map creation task " + t + " failed");
            }
        }

        updateScoresPanel();


    }

    private void updateScoresPanel() {
        p("updateScoresPanel");
        global = GlobalContext.getContext();
        if (global == null) {
            p("No global context");
            this.setStatusWarning("Got no global context, won't do anything");
            return;
        }

        expContext = global.getExperimentContext();
        if (global.getExperimentContext() == null) {
            this.setStatusWarning("Please select an experiment/result first in the Experiment Component");
            p("Creating fake experiment context for testing");
            expContext = ExperimentContext.createFake(global);

        }
        wellContext = expContext.getWellContext();
        if (wellContext == null) {
            p("No well context");
            this.setStatus("Got no well context yet... load a bfmask.bin file first (need to change that:-)");
            //  p("Got no wellContext - pick and experiment first and load a mask file first");
            return;
        }
        // DEPENDS ON FLAG
        mask = ScoreMask.getMask(global.getExperimentContext(), wellContext);

        String msg = "";
        if (!mask.hasImage(currentflag)) {
            p("Don't have flag "+currentflag);
            if (currentflag.isCustom()) {
                // p("Could not find file " + mask.getImageFile(currentflag));
                if (customtask != null) {
                    GuiUtils.showNonModalMsg("The custom heat map is still being computed");
                } else {
                    GuiUtils.showNonModalMsg("To create a custom heat map, select the tools icon to compute one");
                }
            } else if (cachetask == null) {
                if ((this.expContext.hasSff() && currentflag.isIn(ScoreMaskFlag.SFF_FLAGS))
                        || (this.expContext.hasBam() && currentflag.isIn(ScoreMaskFlag.SAM_FLAGS))) {
                    msg = "The IMAGE file " + mask.getFile(currentflag) + " does NOT seem to exist, I will have to generate it first. This might take a few minutes!";
                    setStatusWarning(msg);
                    // boolean ok = createImageFileFromScoreFlag();
                    GuiUtils.showNonModalMsg("ScoreHeatMap: I have to create the scores image files first...");
                    //   this.setStatusWarning("I have to parse some files and create images for the scores...");
                    cachetask = new CacheFilesTask(this);
                    cachetask.execute();
                } else {
                    GuiUtils.showNonModalMsg("The .sff or .bam file was not found (and is needed) for this flag");
                }
            } else {
                msg = "Currently creating data for " + currentflag + " ... please wait...";
                GuiUtils.showNonModalMsg("ScoreHeatMap: creating heat map...");
                setStatusWarning(msg);
            }
           
        }
        //   this.setStatus("Updating heat map");
        this.updateDensityPanel();

    }

    private void updateDensityPanel() {
        p("updateDensityPanel with flag "+currentflag);
        // GuiUtils.showNonModalMsg("ScoreHeatMap: reading heat map for " + currentflag + "....", false, 5);
        mask.readData(currentflag);
        p("setscoremask for flag "+currentflag);
        densityPanel.setScoreMask(mask, currentflag, (Integer) spinBucket.getValue());
        //this.invalidate();
      //  this.revalidate();
                
       // p("repainting immediately");
      //  paintImmediately(0,0,1000,1000);
       // this.repaint();       
    }
    

    private void setStatus(String msg) {
        p(msg);
        Message message = StatusDisplayer.getDefault().setStatusText("<html>" + msg + "</html>", StatusDisplayer.IMPORTANCE_FIND_OR_REPLACE);
        message.clear(30000);
    }

    private void p(String msg) {
        System.out.println("ScoreHeatMapTopComp: " + msg);
    }

    private void setStatusWarning(String msg) {
        p(msg);
        Message message = StatusDisplayer.getDefault().setStatusText("<html>" + msg + "</html>", StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        message.clear(60000);
    }

    private void setStatusError(String msg) {
        p(msg);
        Message message = StatusDisplayer.getDefault().setStatusText("<html>" + msg + "</html>", StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        message.clear(120000);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        boxflag = new javax.swing.JComboBox();
        btnReload = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        spinBucket = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        btnCustom = new javax.swing.JButton();
        btnSelect = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        filter = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        boxflag.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutScoresViewTopComponent.class, "TorrentScoutScoresViewTopComponent.boxflag.toolTipText")); // NOI18N
        boxflag.setMinimumSize(new java.awt.Dimension(23, 23));
        boxflag.setPreferredSize(new java.awt.Dimension(28, 23));
        boxflag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxflagActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 13;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel1.add(boxflag, gridBagConstraints);

        btnReload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/scoreview/view-refresh-3.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnReload, org.openide.util.NbBundle.getMessage(TorrentScoutScoresViewTopComponent.class, "TorrentScoutScoresViewTopComponent.btnReload.text")); // NOI18N
        btnReload.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutScoresViewTopComponent.class, "TorrentScoutScoresViewTopComponent.btnReload.toolTipText")); // NOI18N
        btnReload.setMargin(new java.awt.Insets(1, 1, 1, 1));
        btnReload.setMaximumSize(new java.awt.Dimension(28, 28));
        btnReload.setMinimumSize(new java.awt.Dimension(28, 28));
        btnReload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReloadActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel1.add(btnReload, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(TorrentScoutScoresViewTopComponent.class, "TorrentScoutScoresViewTopComponent.jLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanel1.add(jLabel3, gridBagConstraints);

        spinBucket.setModel(new javax.swing.SpinnerNumberModel(1, 1, 200, 1));
        spinBucket.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutScoresViewTopComponent.class, "TorrentScoutScoresViewTopComponent.spinBucket.toolTipText")); // NOI18N
        spinBucket.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinBucketStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel1.add(spinBucket, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(TorrentScoutScoresViewTopComponent.class, "TorrentScoutScoresViewTopComponent.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 4, 0, 0);
        jPanel1.add(jLabel2, gridBagConstraints);

        btnCustom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/scoreview/configure-3.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnCustom, org.openide.util.NbBundle.getMessage(TorrentScoutScoresViewTopComponent.class, "TorrentScoutScoresViewTopComponent.btnCustom.text")); // NOI18N
        btnCustom.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutScoresViewTopComponent.class, "TorrentScoutScoresViewTopComponent.btnCustom.toolTipText")); // NOI18N
        btnCustom.setMargin(new java.awt.Insets(1, 1, 1, 1));
        btnCustom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCustomActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel1.add(btnCustom, gridBagConstraints);

        btnSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/select-rectangular.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSelect, org.openide.util.NbBundle.getMessage(TorrentScoutScoresViewTopComponent.class, "TorrentScoutScoresViewTopComponent.btnSelect.text")); // NOI18N
        btnSelect.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutScoresViewTopComponent.class, "TorrentScoutScoresViewTopComponent.btnSelect.toolTipText")); // NOI18N
        btnSelect.setMargin(new java.awt.Insets(1, 1, 1, 1));
        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        jPanel1.add(btnSelect, gridBagConstraints);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/scoreview/eye.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(TorrentScoutScoresViewTopComponent.class, "TorrentScoutScoresViewTopComponent.jButton1.text")); // NOI18N
        jButton1.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutScoresViewTopComponent.class, "TorrentScoutScoresViewTopComponent.jButton1.toolTipText")); // NOI18N
        jButton1.setMargin(new java.awt.Insets(1, 1, 1, 1));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new java.awt.GridBagConstraints());

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/scoreview/document-export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(TorrentScoutScoresViewTopComponent.class, "TorrentScoutScoresViewTopComponent.jButton2.text")); // NOI18N
        jButton2.setMargin(new java.awt.Insets(1, 1, 1, 1));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        jPanel1.add(jButton2, gridBagConstraints);

        filter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/scoreview/filter.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(filter, org.openide.util.NbBundle.getMessage(TorrentScoutScoresViewTopComponent.class, "TorrentScoutScoresViewTopComponent.filter.text")); // NOI18N
        filter.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutScoresViewTopComponent.class, "TorrentScoutScoresViewTopComponent.filter.toolTipText")); // NOI18N
        filter.setMargin(new java.awt.Insets(1, 1, 1, 1));
        filter.setMaximumSize(new java.awt.Dimension(20, 20));
        filter.setMinimumSize(new java.awt.Dimension(20, 20));
        filter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterActionPerformed(evt);
            }
        });
        jPanel1.add(filter, new java.awt.GridBagConstraints());

        add(jPanel1, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void boxflagActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxflagActionPerformed
        if (DOACTIONS == false) {
            //   msg("doactions is false not doing action");
            return;
        }
        DOACTIONS = false;

        currentflag = (ScoreMaskFlag) this.boxflag.getSelectedItem();
        //msg("Currentflag: "+currentflag);
        if (currentflag == null) {
            return;
        }
        p("Flag got selected:" + currentflag);
        boxflag.setToolTipText(currentflag.getDescription());
        updateScoresPanel();
        DOACTIONS = true;
}//GEN-LAST:event_boxflagActionPerformed

    private void spinBucketStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinBucketStateChanged
        updateScoresPanel();
}//GEN-LAST:event_spinBucketStateChanged

    private void btnReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReloadActionPerformed
        updateScoresPanel();
}//GEN-LAST:event_btnReloadActionPerformed

    private void btnCustomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCustomActionPerformed
        if (cust == null) {
            cust = new CustomScorePanel();
        }
        global = GlobalContext.getContext();
        if (global == null || global.getExperimentContext() == null) {
            this.getLatestExperimentContext();
        }
        if (global == null || global.getExperimentContext() == null) {
            JOptionPane.showMessageDialog(this, "No experiment context yet");
            return;
        }
        expContext = global.getExperimentContext();
        if (expContext == null) {
            GuiUtils.showNonModalMsg("No experiment loaded yet");
            return;
        }
        if (!expContext.hasBam()) {
            GuiUtils.showNonModalMsg("I need a .bam file for this");
            SequenceLoader loader = SequenceLoader.getSequenceLoader(expContext, false, true);

            if (!expContext.hasBam()) {
                return;
            }
        }
        int ans = JOptionPane.showConfirmDialog(this, cust, "Custom Heat Map", JOptionPane.OK_CANCEL_OPTION);
        if (ans != JOptionPane.OK_OPTION) {
            return;
        }


        currentflag = (ScoreMaskFlag) this.boxflag.getSelectedItem();
        ScoreMaskCalculatorIF calc = cust.getCalculator();
        if (calc.getFlag() == null) {
            JOptionPane.showMessageDialog(this, "Must associate a score mask with this custom heat map");
            return;
        }
        Parameter par[] = calc.getParams();
        if (par != null) {
            for (Parameter p : par) {
                if (p.isRequired() && p.getValue() == null) {
                    JOptionPane.showMessageDialog(this, "Must enter a value for " + p.getName());
                    return;
                }
            }
        }

        this.densityPanel.clear();
        customtask = new ComputeHeatMapTask(calc, this);
        customtask.execute();

    }//GEN-LAST:event_btnCustomActionPerformed

    private class ComputeHeatMapTask extends Task {

        ScoreMaskCalculatorIF calc;

        public ComputeHeatMapTask(ScoreMaskCalculatorIF calc, TaskListener tlistener) {
            super(tlistener, ProgressHandleFactory.createHandle("Computing heat map " + calc.getName() + "...."));
            this.calc = calc;
        }

        @Override
        public Void doInBackground() {
            try {
                if (mask == null) {
                    mask = ScoreMask.getMask(global.getExperimentContext(), global.getExperimentContext().getWellContext());
                }
                ScoreMaskGenerator gen = new ScoreMaskGenerator(mask, expContext);
                gen.processBamFileForCustomFlag(true, calc);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                err(ex.getMessage());
            }
            return null;
        }

        public boolean isSuccess() {
            return true;
        }
    }

    private void doFilterAction() {

        if (global.getExperimentContext() == null || wellContext == null) {
            return;
        }
        if (mask == null) {
            mask = ScoreMask.getMask(global.getExperimentContext(), wellContext);
        }
        if (mask == null) {
            return;
        }
        currentflag = (ScoreMaskFlag) this.boxflag.getSelectedItem();
        if (currentflag == null) {
            return;
        }

        MinMaxPanel p = new MinMaxPanel();
        double min = mask.getMin(currentflag);
        double max = mask.getMax(currentflag);
      //  p("Got min/max for " + currentflag + ":" + min + "/" + max);
        p.setMin(min);
        p.setMax(max);
        int ans = JOptionPane.showConfirmDialog(this, p, "Interval for " + this.currentflag.getName(), JOptionPane.OK_CANCEL_OPTION);
        // first find minmax

        if (ans != JOptionPane.OK_OPTION) {
            return;
        }

        min = p.getMin();
        max = p.getMax();
        if (max < min) {
            GuiUtils.showNonModalMsg("Max should be > min :-)");
            return;
        }
        p("filtering flag " + currentflag);

        // pick custom flag
        JComboBox cus = new JComboBox(ScoreMaskFlag.CUSTOM_FLAGS);
        ans = JOptionPane.showConfirmDialog(this, cus, "Store result in what map?", JOptionPane.OK_CANCEL_OPTION);
        if (ans != JOptionPane.OK_OPTION || cus.getSelectedIndex() < 0) {
            return;
        }

        ScoreMaskFlag customflag = (ScoreMaskFlag) cus.getSelectedItem();
        if (customflag == null) {
            return;
        }
        customflag.setMultiplier(currentflag.multiplier());
        ScoreMaskGenerator gen = new ScoreMaskGenerator(mask, this.expContext);
        int count = gen.filterFlag(currentflag, customflag, min, max);

        p("score mask multiplier "+customflag.getName()+"= "+customflag.multiplier());
        
        String desc = "Found "+count+" matches for "+currentflag.getName()+" between "+min+"-"+max;
        GuiUtils.showNonModalMsg(desc);
        customflag.setDescription(desc);
        currentflag = customflag;
        
        this.boxflag.setSelectedItem(customflag);
        updateScoresPanel();
       

    }

    private void selectAllWellsWithFlag(ScoreMaskFlag flag) {
        if (this.wellContext == null || mask == null) {
            GuiUtils.showNonModalMsg("Got no well context or score mask");
            return;
        }
        ArrayList<WellCoordinate> coords = mask.getAllCoordsWithData(flag, MAX_COORDS);
        if (coords.size() >= MAX_COORDS) {
            GuiUtils.showNonModalMsg("Warning: Found more than " + MAX_COORDS + ", wells, will only return first " + MAX_COORDS);
        }
        WellSelection sel = new WellSelection(coords);
        LookupUtils.publish(wellSelectionContent, sel);

    }
    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        if (this.wellContext == null || mask == null) {
            GuiUtils.showNonModalMsg("Got no well context or score mask");
            return;
        }
        CoordSelectionPanel pan = new CoordSelectionPanel();
        pan.setMaxX(this.wellContext.getNrCols());
        pan.setMaxY(this.wellContext.getNrRows());
        int ans = JOptionPane.showConfirmDialog(this, pan, "Enter a selection:", JOptionPane.OK_CANCEL_OPTION);
        if (ans == JOptionPane.CANCEL_OPTION) {
            return;
        }
        WellCoordinate c1 = pan.getCoord1();
        WellCoordinate c2 = pan.getCoord2();
        ArrayList<WellCoordinate> coords = mask.getAllCoordsWithData(this.currentflag, MAX_COORDS, c1.getCol(), c1.getRow(), c2.getCol(), c2.getRow());
        if (coords.size() >= MAX_COORDS) {
            GuiUtils.showNonModalMsg("Warning: Found more than " + MAX_COORDS + ", wells, will only return first " + MAX_COORDS);
        }
        WellSelection sel = new WellSelection(c1, c2, coords);
        LookupUtils.publish(wellSelectionContent, sel);
    }//GEN-LAST:event_btnSelectActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        selectAllWellsWithFlag(this.currentflag);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        this.densityPanel.export();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void filterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterActionPerformed
        doFilterAction();
    }//GEN-LAST:event_filterActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox boxflag;
    private javax.swing.JButton btnCustom;
    private javax.swing.JButton btnReload;
    private javax.swing.JButton btnSelect;
    private javax.swing.JButton filter;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSpinner spinBucket;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
        this.tryToLoad();
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version






    }

    private static void err(String ex) {
        Logger.getLogger(TorrentScoutScoresViewTopComponent.class.getName()).log(Level.SEVERE, ex);
    }
}
