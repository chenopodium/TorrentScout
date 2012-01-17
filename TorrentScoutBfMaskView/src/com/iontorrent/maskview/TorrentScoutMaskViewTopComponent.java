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
package com.iontorrent.maskview;

import com.iontorrent.guiutils.netbeans.OpenWindowAction;
import com.iontorrent.main.options.TorrentScoutSettingsPanel;
import com.iontorrent.rawdataaccess.wells.BfMaskFlag;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;


import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.expmodel.LoadDataContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.guiutils.wells.CoordSelectionPanel;
import com.iontorrent.rawdataaccess.wells.BfMask;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.utils.io.FileUtils;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellSelection;
import java.awt.HeadlessException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JOptionPane;
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
@ConvertAsProperties(dtd = "-//com.iontorrent.maskview//TorrentScoutMaskView//EN",
autostore = false)
@TopComponent.Description(preferredID = "TorrentScoutMaskViewTopComponent",
iconBase = "com/iontorrent/maskview/chip.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "top_left", openAtStartup = true)
@ActionID(category = "Window", id = "com.iontorrent.maskview.TorrentScoutMaskViewTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_TorrentScoutMaskViewAction",
preferredID = "TorrentScoutMaskViewTopComponent")
public final class TorrentScoutMaskViewTopComponent extends TopComponent implements TaskListener {

    int MAX_COORDS = 10000;
    private BfMaskDensityPanel densityPanel;
    private String bfmask_file;
    private BfMaskFlag currentflag;
    //private ProgressHandle progress;
    private BfMaskFlag default_flag;
    private boolean DOACTIONS;
    private ExperimentContext oldContext;
    private ExperimentContext expContext;
    private GlobalContext global;
    private WellContext context;
    private transient final InstanceContent wellContextContent = LookupUtils.getPublisher(WellContext.class);
    private transient final Lookup.Result<ExperimentContext> expContextResults =
            LookupUtils.getSubscriber(ExperimentContext.class, new SubscriberListener());
    private transient final Lookup.Result<GlobalContext> gContextResults =
            LookupUtils.getSubscriber(GlobalContext.class, new GSubscriberListener());
    private transient final Lookup.Result<LoadDataContext> loadContextResults =
            LookupUtils.getSubscriber(LoadDataContext.class, new LoadDataSubscriberListener());
    //private transient final InstanceContent wellCoordContent = LookupUtils.getPublisher(WellCoordinate.class);
    private transient final InstanceContent wellSelectionContent = LookupUtils.getPublisher(WellSelection.class);

    public TorrentScoutMaskViewTopComponent() {
        initComponents();

        setName(NbBundle.getMessage(TorrentScoutMaskViewTopComponent.class, "CTL_TorrentScoutMaskViewTopComponent"));
        setToolTipText(NbBundle.getMessage(TorrentScoutMaskViewTopComponent.class, "HINT_TorrentScoutMaskViewTopComponent"));
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.FALSE);


        DOACTIONS = false;
        densityPanel = new BfMaskDensityPanel(null);
        add("Center", densityPanel);

        BfMaskFlag flags[] = BfMaskFlag.values();
        currentflag = BfMaskFlag.LIVE;
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(flags));
        DOACTIONS = true;

        Preferences p = Preferences.userNodeForPackage(TorrentScoutSettingsPanel.class);
        p.addPreferenceChangeListener(new PreferenceChangeListener() {

            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                loadPreferences();
            }
        });
        tryToLoad();

    }

    protected void componentedActivated() {
        super.componentActivated();
        //tryToLoad();

    }

    private boolean checkAndMaybeCacheWellsFile() throws HeadlessException {
        String res = this.expContext.getResultsDirectory();
        String cache = this.expContext.getCacheDir();
        File wellsfile = FileUtils.findAndCopyFileFromUrlTocache("1.wells", cache, res, false, false, null, 1024 * 1024);
        if (wellsfile == null) {
            this.setStatusError("Could not find wellsfile in " + res);
            return false;
        }
        if (wellsfile.length() < 10000) {
            this.setStatusError("1.wells file seems too small: " + wellsfile.length());
            return false;
        }
        return true;
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


        int code = BfMaskFlag.LIVE.getCode();
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
            default_flag = BfMaskFlag.LIVE;
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
        p("try to load");

        if (global == null) {
            //this.setStatus("Got no global context yet");
            return;
        }
        
        expContext = global.getExperimentContext();
        
        if (expContext == null) return;
        if (expContext != null) bfmask_file = this.expContext.getResultsDirectory() + "bfmask.bin";
        if (bfmask_file == null) {
            this.setStatusWarning("Got no bfmask file yet");
            return;
        }
        this.btnReload.setToolTipText("Reload the file "+bfmask_file);
        p("TRYING TO LOAD MASK:");
        p("Mask file: "+bfmask_file);
        p("Exp results dir "+expContext.getResultsDirectory());
        p("Exp raw     dir "+expContext.getRawDir());
        if (FileUtils.isUrl(bfmask_file)) {
            this.setStatusWarning(bfmask_file + " is an url, hit reload if you want to see the data");
            return;
        }

        if (!FileUtils.exists(bfmask_file)) {
            p("Got exp: "+expContext.toString());
            this.setStatusError(bfmask_file + " does not seem to exist!");
            return;
        }

        this.update();

    }

    private static void err(String ex) {
        Logger.getLogger(TorrentScoutMaskViewTopComponent.class.getName()).log(Level.SEVERE, ex);
    }

    private class LoadMaskFileTask extends Task {

        String file;
        int bucketsize;

        public LoadMaskFileTask(TaskListener tlistener, String file, int bucketsize) {
            super(tlistener, ProgressHandleFactory.createHandle("Loading bfmkas.bin file ..."));
            this.file = file;
            this.bucketsize = bucketsize;

        }

        @Override
        public Void doInBackground() {
            createWellContext(file, bucketsize);
            return null;
        }

        public boolean isSuccess() {
            return context != null;
        }
    }

    /** read a bfmask fileOrUrl and create the well density data structure */
    public void createWellContext(String fileOrUrl, int bucketsize) {
        //    p("Reading mask "+fileOrUrl);        
        context = expContext.createWellContext();
        if (context != null) {
            LookupUtils.publish(wellContextContent, context);
        } else {
            GuiUtils.showNonModalMsg("Could not read mask file " + fileOrUrl);
        }
        // p("Computing well density for mask "+fileOrUrl);

    }

    @Override
    public void taskDone(Task t) {
        p("Task " + t + " is done");
        setCursor(null);
        if (t.isSuccess()) {
            currentflag = (BfMaskFlag) this.jComboBox1.getSelectedItem();
            //msg("Currentflag: "+currentflag);
            if (currentflag == null) {
                return;
            }
           
            densityPanel.setContext(context, currentflag, (Integer) spinBucket.getValue());
            if (expContext.getNrcols()>110) densityPanel.createDefaultSelection(100, 100, 110, 110);
            else densityPanel.createDefaultSelection(50, 50, 60, 60);
        } else {
            GuiUtils.showNonModalMsg("Loading the bfmask.bin file failed");
        }

    }

    private void loadFileInThread(String fileOrUrl) {
        GuiUtils.showNonModalMsg("Loading bfmask file " + fileOrUrl);
        LoadMaskFileTask task = new LoadMaskFileTask(this, fileOrUrl, (Integer) spinBucket.getValue());
        task.execute();
    }

    private class SubscriberListener implements LookupListener {
        @Override
        public void resultChanged(LookupEvent ev) {
            getLatestExperimentContext();
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

    private void getLatestExperimentContext() {
        //   p("Getting latest exp context");
        if (global == null) {
            updateGlobal();
        }
        final Collection<? extends ExperimentContext> items = expContextResults.allInstances();
        if (!items.isEmpty()) {
            ExperimentContext data = null;
            Iterator<ExperimentContext> it = (Iterator<ExperimentContext>) items.iterator();
            while (it.hasNext()) {
                data = it.next();
            }
            if (data == oldContext && data.getResultsDirectory().equalsIgnoreCase(oldContext.getResultsDirectory())) {
                p("doing nothing, same exp");
            } else {
                oldContext = data;
                update(oldContext);
            }
        }
    }

    private void update(ExperimentContext result) {
        //   p("updating exp context "+result.getResDir());
        if (expContext == null || !expContext.getResultsDirectory().equalsIgnoreCase(result.getResultsDirectory())) {
            this.expContext = result;
             bfmask_file = expContext.getResultsDirectory() + "bfmask.bin";

             this.densityPanel.clear();
            
            String f = expContext.getResultsDirectory() + "bfmask.bin";
            //if (bfmask_file==null || !bfmask_file.equalsIgnoreCase(f)) {
            bfmask_file = f;
            tryToLoad();
            // }
        }



    }
     private void doHintAction() {
        String msg="<html><ul>";
        msg+="<li>Move the image around with the <b>right</b> mouse button </li>";
        msg+="<li>Select a coordinate by clicking the <b>left</b> mouse button </li>";
        msg+="<li>Zoom the image in and out with a <b>mouse wheel</b> </li>";        
        msg += "</ul></html>";
        JOptionPane.showMessageDialog(this, msg);    
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jButton1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox();
        btnReload = new javax.swing.JButton();
        spinBucket = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        btnSelect = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        hint = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(TorrentScoutMaskViewTopComponent.class, "TorrentScoutMaskViewTopComponent.jButton1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(TorrentScoutMaskViewTopComponent.class, "TorrentScoutMaskViewTopComponent.jLabel4.text")); // NOI18N

        setLayout(new java.awt.BorderLayout());

        jPanel2.setOpaque(false);
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jComboBox1.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutMaskViewTopComponent.class, "TorrentScoutMaskViewTopComponent.jComboBox1.toolTipText")); // NOI18N
        jComboBox1.setMinimumSize(new java.awt.Dimension(70, 18));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel2.add(jComboBox1, gridBagConstraints);

        btnReload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/maskview/view-refresh-3.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnReload, org.openide.util.NbBundle.getMessage(TorrentScoutMaskViewTopComponent.class, "TorrentScoutMaskViewTopComponent.btnReload.text")); // NOI18N
        btnReload.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutMaskViewTopComponent.class, "TorrentScoutMaskViewTopComponent.btnReload.toolTipText")); // NOI18N
        btnReload.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnReload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReloadActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel2.add(btnReload, gridBagConstraints);

        spinBucket.setModel(new javax.swing.SpinnerNumberModel(5, 1, 200, 1));
        spinBucket.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutMaskViewTopComponent.class, "TorrentScoutMaskViewTopComponent.spinBucket.toolTipText")); // NOI18N
        spinBucket.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinBucketStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel2.add(spinBucket, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(TorrentScoutMaskViewTopComponent.class, "TorrentScoutMaskViewTopComponent.jLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanel2.add(jLabel3, gridBagConstraints);

        btnSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/select-rectangular.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSelect, org.openide.util.NbBundle.getMessage(TorrentScoutMaskViewTopComponent.class, "TorrentScoutMaskViewTopComponent.btnSelect.text")); // NOI18N
        btnSelect.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutMaskViewTopComponent.class, "TorrentScoutMaskViewTopComponent.btnSelect.toolTipText")); // NOI18N
        btnSelect.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        jPanel2.add(btnSelect, gridBagConstraints);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/maskview/document-export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(TorrentScoutMaskViewTopComponent.class, "TorrentScoutMaskViewTopComponent.jButton2.text")); // NOI18N
        jButton2.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutMaskViewTopComponent.class, "TorrentScoutMaskViewTopComponent.jButton2.toolTipText")); // NOI18N
        jButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton2, new java.awt.GridBagConstraints());

        hint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/help-hint.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(hint, org.openide.util.NbBundle.getMessage(TorrentScoutMaskViewTopComponent.class, "TorrentScoutMaskViewTopComponent.hint.text")); // NOI18N
        hint.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutMaskViewTopComponent.class, "TorrentScoutMaskViewTopComponent.hint.toolTipText")); // NOI18N
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

    private void spinBucketStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinBucketStateChanged
        update();
}//GEN-LAST:event_spinBucketStateChanged

    private void btnReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReloadActionPerformed
        this.tryToLoad();
        update();
}//GEN-LAST:event_btnReloadActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        if (DOACTIONS == false) {
            //   msg("doactions is false not doing action");
            return;
        }
        DOACTIONS = false;

        currentflag = (BfMaskFlag) this.jComboBox1.getSelectedItem();
        //msg("Currentflag: "+currentflag);
        if (currentflag == null) {
            return;
        }
        //  p("Flag got selected:" + currentflag);
        jComboBox1.setToolTipText(currentflag.getDescription());
        if (densityPanel.getContext() == null) {
            update();
        } else {
            //    msg("setting flag in density panel");
            densityPanel.setFlag(currentflag);
        }
        DOACTIONS = true;
}//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        this.densityPanel.export();     }//GEN-LAST:event_jButton2ActionPerformed

    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        if (context == null || context.getMask() == null) {
            GuiUtils.showNonModalMsg("Got no well context or bfmask file");
            return;
        }
        CoordSelectionPanel pan = new CoordSelectionPanel();
        pan.setMaxX(this.context.getNrCols());
        pan.setMaxY(this.context.getNrRows());
        int ans = JOptionPane.showConfirmDialog(this, pan, "Enter a selection:", JOptionPane.OK_CANCEL_OPTION);
        if (ans == JOptionPane.CANCEL_OPTION) {
            return;
        }

        WellCoordinate c1 = pan.getCoord1();
        WellCoordinate c2 = pan.getCoord2();
        BfMask mask = this.context.getMask();
        ArrayList<WellCoordinate> coords = mask.getAllCoordsWithData(this.currentflag, MAX_COORDS, c1.getCol(), c1.getRow(), c2.getCol(), c2.getRow());
        if (coords.size() >= MAX_COORDS) {
            GuiUtils.showNonModalMsg("Warning: Found more than " + MAX_COORDS + ", wells, will only return first " + MAX_COORDS);
        }
        WellSelection sel = new WellSelection(c1, c2, coords);

        LookupUtils.publish(wellSelectionContent, sel);
    }//GEN-LAST:event_btnSelectActionPerformed

    private void hintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hintActionPerformed
        doHintAction();
    }//GEN-LAST:event_hintActionPerformed

    private void selectAllWellsWithFlag(BfMaskFlag flag) {
        if (context == null || context.getMask() == null) {
            GuiUtils.showNonModalMsg("Got no well context or bfmask file");
            return;
        }
        BfMask mask = this.context.getMask();
        ArrayList<WellCoordinate> coords = mask.getAllCoordsWithData(flag, MAX_COORDS);
        if (coords.size() >= MAX_COORDS) {
            GuiUtils.showNonModalMsg("Warning: Found more than " + MAX_COORDS + ", wells, will only return first " + MAX_COORDS);
        }
        WellSelection sel = new WellSelection(coords);
        LookupUtils.publish(wellSelectionContent, sel);

    }
    private class CacheFilesTask extends Task {

        boolean ok = false;

        public CacheFilesTask(TaskListener tlistener) {
            super(tlistener, ProgressHandleFactory.createHandle("MaskView: Caching a few files..."));
        }

        @Override
        public Void doInBackground() {
            ok = checkAndMaybeCacheWellsFile();
            return null;
        }

        public boolean isSuccess() {
            return ok;
        }
    }

    private void update() {

        global = GlobalContext.getContext();
        if (global == null) {
            p("Got no global context - loading options");
            
            return;
        }

        if (bfmask_file == null || bfmask_file.trim().length() < 8) {
            return;
        }

        if (global.getExperimentContext() == null) {
           // this.setStatusWarning("Got no experiment/result yet");
            return;
        }


        String msg = "";
        if (!FileUtils.exists(bfmask_file)) {
            msg = "The file " + bfmask_file + " not found - check symlinks etc";
            setStatusError(msg);
            GuiUtils.showNonModalMsg(msg, false, 10);
            return;
        }

        // p("loading file " + fileOrUrl + ", got flag " + currentflag);
        String res = global.getExperimentContext().getResultsDirectory();
        String cache = global.getExperimentContext().getCacheDir();
        if (FileUtils.isUrl(res) && !FileUtils.exists(cache + "1.wells")) {
            int ans = JOptionPane.showConfirmDialog(this, "I have to download the 1.wells file from " + res + ", this might take a minute...)", "Wells file", JOptionPane.OK_CANCEL_OPTION);
            if (ans == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }


        File maskfile = FileUtils.findAndCopyFileFromUrlTocache("bfmask.bin", cache, res, false, false, null, 1024 * 1024);

        //   p("Got maskfile: " + maskfile);
        if (maskfile == null) {
            this.setStatusError("Could find the bfmask.bin file in " + res);
            return;
        } else if (!FileUtils.exists(maskfile.toString())) {
            JOptionPane.showMessageDialog(this, "I cannot find the mask file " + maskfile);
            return;
        }
        this.loadFileInThread(maskfile.toString());


        if (FileUtils.isUrl(res)) {
            CacheFilesTask task = new CacheFilesTask(this);
            task.execute();
        }


        // if (!cacheBinAndWellsFIle(fileOrUrl)) return;
        //updateDensityPanel(fileOrUrl);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnReload;
    private javax.swing.JButton btnSelect;
    private javax.swing.JButton hint;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSpinner spinBucket;
    // End of variables declaration//GEN-END:variables

    @Override
    public Action[] getActions() {
        return OpenWindowAction.getActions(this);
    }

    @Override
    public void componentOpened() {

        DOACTIONS = false;
        loadPreferences();
        getLatestExperimentContext();
        if (default_flag == null) {
            default_flag = BfMaskFlag.LIVE;
        }

        jComboBox1.setSelectedItem(default_flag);
        if (this.global != null && global.getExperimentContext() != null) {
            bfmask_file = global.getExperimentContext().getResultsDirectory() + "bfmask.bin";
            tryToLoad();
        }
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
        System.out.println("BfMaskTopComp: " + msg);
    }
}
