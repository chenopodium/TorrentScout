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
package com.iontorrent.scout.offline;

import com.iontorrent.expmodel.CompositeExperiment;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.FolderConfig;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.expmodel.LoadDataContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.guiutils.netbeans.OpenWindowAction;
import com.iontorrent.rawdataaccess.pgmacquisition.DataAccessManager;
import com.iontorrent.rawdataaccess.pgmacquisition.RawDataFacade;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.results.scores.ScoreMask;
import com.iontorrent.sequenceloading.SequenceLoader;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.utils.StringTools;
import com.iontorrent.utils.io.FileTools;
import com.iontorrent.utils.io.FileUtils;
import com.iontorrent.utils.log.ShortFormatter;
import com.iontorrent.wellmodel.WellContext;
import java.awt.Color;
import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.StatusDisplayer.Message;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//com.iontorrent.scout.offline//Offline//EN",
autostore = false)
@TopComponent.Description(preferredID = "OfflineTopComponent",
iconBase = "com/iontorrent/scout/offline/server-delete.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "top_left", openAtStartup = true)
@ActionID(category = "Window", id = "com.iontorrent.scout.offline.OfflineTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_OfflineAction",
preferredID = "OfflineTopComponent")
public final class OfflineTopComponent extends TopComponent {

    GlobalContext context;
    //  private ProgressHandle progress;
    private transient final InstanceContent expContent = LookupUtils.getPublisher(ExperimentContext.class);
    private transient final InstanceContent loadContent = LookupUtils.getPublisher(LoadDataContext.class);
    private transient final InstanceContent wellContextContent = LookupUtils.getPublisher(WellContext.class);
    private transient final Lookup.Result<GlobalContext> gContextResults =
            LookupUtils.getSubscriber(GlobalContext.class, new GSubscriberListener());
    ExperimentContext exp;
    private transient final Lookup.Result<ExperimentContext> dataClassResults =
            LookupUtils.getSubscriber(ExperimentContext.class, new ExpContextListener());
    private transient final InstanceContent compContent = LookupUtils.getPublisher(CompositeExperiment.class);

    boolean rawChanged;
    boolean cacheChanged;
    boolean resChanged;
    public OfflineTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(OfflineTopComponent.class, "CTL_OfflineTopComponent"));
        setToolTipText(NbBundle.getMessage(OfflineTopComponent.class, "HINT_OfflineTopComponent"));

        updateExpGui();
        setFormatter();

    }

    private static void setFormatter() {
        final ShortFormatter formatter = ShortFormatter.getFormatter();
        Logger rootLogger = Logger.getLogger(OfflineTopComponent.class.getName());

        while (rootLogger.getParent() != null) {
            rootLogger = rootLogger.getParent();
        }

        for (final Handler handler : rootLogger.getHandlers()) {
            handler.setFormatter(formatter);
        }
    }

      private void doHintAction() {
        String msg = "<html>You can do the following things here:<ul>";
        msg += "<li>open an experiment and specify the raw and results folders yourself</li>";
        msg += "<li>if this experiment has raw data, specify where the raw (.dat) files are stored</li>";
        msg += "<li>if there is results data, specify the folder with the results files, such as bfmask.bin</li>";
        msg += "</ul></html>";
        JOptionPane.showMessageDialog(this, msg);
    }
    
    public boolean checkAll() {
        if (exp.getCacheDir() == null) {
            btnView.setEnabled(false);
            btnView.setToolTipText("Please make sure the cache and results directory are specified..");
        }
        int nrFiles = checkFiles();
        int hasIndices = checkIndices();
        int nrdat = this.getNrCached();
        setOk(this.boxFiles, nrFiles, 5);
        setOk(this.boxIndex, hasIndices, 4);
        setOk(this.boxDat, nrdat, 1);

        if (hasIndices > 2) {
            boxIndex.setText("Found all indices");
        } else if (hasIndices > 0) {
            boxIndex.setText("Found some indices");
        } else {
            boxIndex.setText("Found no indices");
        }

        if (nrFiles > 4) {
            boxFiles.setText("Found all required files");
        } else if (nrFiles > 0) {
            boxFiles.setText("Found some required files");
        } else {
            boxFiles.setText("Found no required files");
        }

        p("Got files: " + nrFiles + ", indices: " + hasIndices + ", nrdat: " + nrdat);
        btnView.setEnabled(true);
        if (nrFiles < 1) {
            //btnView.setEnabled(false);
            btnView.setToolTipText("I found none of the required files! Please check your path...");
            this.btnView.setForeground(new Color(153, 0, 51));
            this.btnWizard.setSelected(true);
            return false;
        } else if (nrFiles > 3 && hasIndices > 2 && nrdat > 0) {
            this.btnView.setForeground(new Color(0, 153, 51));
            btnView.setToolTipText("I found all files, indices and some raw files");
            this.btnWizard.setSelected(false);
            return true;
            //   this.btnWizard.setForeground(Color.black);
        } else if (nrFiles > 3 && hasIndices > 2) {
            this.btnView.setForeground(Color.black);
            btnView.setToolTipText("I found all files and incides");
            this.btnWizard.setSelected(false);
            return true;
            //   this.btnWizard.setForeground(Color.black);
        } else {

            this.btnView.setForeground(new Color(153, 0, 51));
            btnView.setToolTipText("I haven't found all files and indices");
            this.btnWizard.setSelected(true);
            return false;
            //  this.btnWizard.setForeground(new Color(0, 153, 51));
        }



    }

    public static void parseLogFiles(String cache, ExperimentContext exp) throws NumberFormatException {
        // find explog and other files
        if (exp == null) {
            return;
        }

        boolean foundLog = false;
        if (exp.getRawDir() != null && FileUtils.exists(exp.getRawDir())) {
            String explog = exp.getRawDir() + "explog.txt";
            if (FileUtils.exists(explog)) {
                ArrayList<String> content = FileTools.getFileAsArray(explog);
                extractDataFromFile(exp, content, "=");
                foundLog = true;
            }
            if (FileUtils.exists(exp.getRawDir()+"acq_0000.dat")) {
                DataAccessManager man = DataAccessManager.getManager(exp.getWellContext());
                man.updateContext(exp);
            }
        }
        if (exp.getRawDir() != null && FileUtils.exists(exp.getResultsDirectory())) {
            String expmeta = exp.getResultsDirectory() + "expMeta.dat";
            if (FileUtils.exists(expmeta)) {
                ArrayList<String> content = FileTools.getFileAsArray(expmeta);
                extractDataFromFile(exp, content, "=");
                foundLog = true;
            }
        }
       // if (foundLog) {
            if (exp != null) {
                exp.expandCacheDir(cache);
            }
     //   }
        p("after parsing log files: expname=" + exp.getResultsName() + ", cache=" + exp.getCacheDir());
    }

    public static void extractDataFromFile(ExperimentContext exp, ArrayList<String> content, String delim) throws NumberFormatException {
        if (content != null && content.size() > 0) {
            for (String line : content) {
                ArrayList<String> items = StringTools.splitString(line, delim);
                if (items != null && items.size() > 1) {
                    String name = items.get(0);
                    String value = items.get(1);
                    name = name.replace(" ", "");
                    name = name.toLowerCase();
                    name = name.trim();
                    value = value.replace("\"", "");
                    value = value.replace(":", "=");
                    value = value.trim();
                    if (name.equals("chiptype")) {
                        exp.setChipType(value);
                    } else if (name.equals("analycsiscycles")) {
                        exp.setNrFlows(Integer.parseInt(value));
                    } else if (name.equals("runname") || name.equals("experimentname")) {
                        String res = exp.getResultsName();
                        if (res == null || res.trim().length() < 2 || res.startsWith("unknown") || res.toLowerCase().startsWith("no ")) {
                            exp.setResultsName(value);
                        }
                    }
                }
            }
        }
    }

    private boolean checkCache() {
        if (exp == null || exp.getCacheDir() == null) {
            return true;
        }
        File f = new File(exp.getCacheDir());

        String msg = "\nNote that all indices are written to the cache directory, and also the indexed .dat files.\n"
                + "If I cannot write to the cache directory, you won't be able to see much of the data...";
        if (!f.exists()) {
            f.mkdirs();
            if (!f.exists()) {
                JOptionPane.showMessageDialog(this, "I can't find the cache directory " + f + msg);
                return false;
            }
        }
        if (!f.canWrite()) {
            JOptionPane.showMessageDialog(this, "I can't write to the cache directory " + f + msg);
            return false;
        }
        return true;
    }
    public void doNewAction() {
        exp= null;
        updateExpGui();
    }

    public void checkExp() {
        if (exp == null) {
            p("exp is null!");
            exp = new ExperimentContext();
            exp.setResultsName(null);
            if (context == null) {
                context = GlobalContext.getContext();
            }
            if (!context.getManager().getRule().startsWith("offline")) {
                context.setContext("offlinepc");
            }
            // exp.setCacheDir(context.getResultsDir());
            exp.setResultsDirectory(context.getResultsDir());
            exp.setRawDir(context.getRawDir());

            Preferences pref = NbPreferences.forModule(OfflineTopComponent.class);
            if (pref != null) {
                try {

                    String cache = pref.get("offline_cache_dir", null);
                    String res = pref.get("offline_res_dir", null);
                    String raw = pref.get("offline_raw_dir", null);
                    p("Reading from preferences: res=" + res + ", raw=" + raw);
                    if (cache != null && cache.length() > 0) {
                        exp.setCacheDir(cache);
                    }

                    if (res != null) {
                        exp.setResultsDirectory(res);
                    }
                    if (raw != null) {
                        exp.setRawDir(raw);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    public int getNrCached() {
        // check raw directory
        if (exp == null || exp.getRawDir()==null || exp.getRawDir().length()<1) {
            return 0;
        }
        if (exp.doesExplogHaveBlocks()) {
           boxDat.setText("found black bird blocks");
           setOk(boxDat, 2, 2);
           return 220;
        }
        RawType rtype = RawType.ACQ;
        // count nr of flows so far
        RawDataFacade io = RawDataFacade.getFacade(exp.getRawDir(), exp.getCacheDir(), rtype);
        
        
        if (io.isRegionFormat(0)) {
            boxDat.setText("found fast .dat file format");
            setOk(boxDat, 2, 2);
            return 220;
        } else if (io.isSmall(0)) {
            boxDat.setText("found small .dat file format");
            setOk(boxDat, 2, 2);
            return 220;
        }
        if (exp.getCacheDir() == null || exp.getCacheDir().length()<1) return 0;
        int nrcached = io.getNrFlowsInCache();

        if (nrcached <= 0) {
            this.boxDat.setText("found no cached .dat files (optional)");
        } else if (nrcached == 1) {
            this.boxDat.setText("found 1 cached .dat file");
        } else {
            this.boxDat.setText("found " + nrcached + " cached .dat files");
        }
        return nrcached;
    }

    private void clearOldData(ExperimentContext newcontext) {
        final Collection<? extends ExperimentContext> items = LookupUtils.getAlIinstances(ExperimentContext.class);
        if (!items.isEmpty()) {
            ExperimentContext data = null;
            Iterator<ExperimentContext> it = (Iterator<ExperimentContext>) items.iterator();
            while (it.hasNext()) {
                data = it.next();
            }
            if (data == newcontext || data.getResultsDirectory().equalsIgnoreCase(newcontext.getResultsDirectory())) {
                p("doing nothing, same exp");
            } else {
                p("Clearing old data from  " + data);
                data.clear();
            }
        }
    }

    public void publish() {
        p("====================== PUBLISH =============");
        p("First clear all old experimental data!");
        clearOldData(exp);
        context.setExperimentContext(exp, false);

        String rule = context.getManager().getRule();
        if (rule.startsWith("offline")) {
            FolderConfig conf = context.getManager().getConfig(rule);
            conf.setCacheRule(exp.getCacheDir());
            conf.setRawRule(exp.getRawDir());
            conf.setResultsRule(exp.getResultsDirectory());
            conf.save();
        }
        Preferences pref = NbPreferences.forModule(OfflineTopComponent.class);
        if (pref != null) {
            try {
                p("Storing preferences");
                pref.put("offline_cache_dir", exp.getCacheDir());
                pref.put("offline_res_dir", exp.getResultsDirectory());
                pref.put("offline_raw_dir", exp.getRawDir());
                pref.sync();

            } catch (Exception e) {
            }
        }
        if (exp.doesExplogHaveBlocks()) {
            if (!exp.isChipBB()) exp.setChipType("900");
            GuiUtils.showNonModalMsg("Got a blackbird experiment, parsing blocks");
            p("Composite: parsing blocks");
            GuiUtils.showNonModalMsg("Got a black bird experiment");
            CompositeExperiment comp = new CompositeExperiment(exp);
            comp.maybParseBlocks();
            p("Got blocks: " + comp.getBlocks());
            p("Publishing CompositeExperiment");
            LookupUtils.publish(compContent, comp);
            // check for thumbnails            
            TopComponent tc = (TopComponent) WindowManager.getDefault().findTopComponent("TorrentScoutCompositeViewTopComponent");
            GuiUtils.showNonModalMsg("Loading whole image view for " + exp.getResultsDirectory());
            if (tc != null) {
                tc.requestActive();
                tc.requestVisible();
                tc.requestAttention(true);
            }
//            ExperimentContext thumb = comp.getThumbnailsContext();
//            if (thumb != null) {
//                LookupUtils.publish(expContent, exp);
//                p("Publishing thumbnails exp" + exp.getResultsDirectory());
//                WellContext wellcontext = exp.getWellContext();
//                if (wellcontext != null) {
//                    LookupUtils.publish(wellContextContent, wellcontext);
//                } 
//            }
        }
        else {
            LookupUtils.publish(expContent, exp);
            p("========== Publishing " + exp.getResultsDirectory());
            WellContext wellcontext = exp.getWellContext();
            if (wellcontext != null) {
                p("========== Publishing wellcontext");
                LookupUtils.publish(wellContextContent, wellcontext);
            } 
        }

    }

    public void startWizard() {
        WizardDescriptor.Iterator iterator = new OfflineWizardIterator(exp);
        final WizardDescriptor wiz = new WizardDescriptor(iterator);
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        // {1} will be replaced by WizardDescriptor.Iterator.name()
        wiz.setTitleFormat(new MessageFormat("{0} ({1})"));
        wiz.setTitle("Offline Data Viewing Wizard");
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wiz);
        dialog.setModal(false);
        dialog.setVisible(true);
        dialog.toFront();
        dialog.addPropertyChangeListener(new MyPropertyChangeListener());
        wiz.addPropertyChangeListener(new MyPropertyChangeListener());


    }

    private class MyPropertyChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            String old = "" + evt.getOldValue();
            String val = "" + evt.getNewValue();
            // GuiUtils.showNonModelMsg("Got a property change: "+evt);
            p("Got property change: " + name + ", old: " + old + ", new :" + val);
            if (name.equalsIgnoreCase(WizardDescriptor.PROP_VALUE)) {
                if (val.equalsIgnoreCase("" + WizardDescriptor.CANCEL_OPTION)) {
                    p("got cancel");
                    updateExpGui();
                } else if (val.equalsIgnoreCase("" + WizardDescriptor.OK_OPTION)) {
                    p("Got ok");
                    updateExpGui();
                    exp.setIgnoreRule(true);
                    update();
                }
            }
        }
    }

    private class ExpContextListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            getExpContext();
        }
    }

    private void getExpContext() {
        final Collection<? extends ExperimentContext> items = dataClassResults.allInstances();
        if (!items.isEmpty()) {
            ExperimentContext data = null;
            Iterator<ExperimentContext> it = (Iterator<ExperimentContext>) items.iterator();
            while (it.hasNext()) {
                data = it.next();
                //  p("Got result: " + data);
            }
            // p("SubscriberListener Got result:" + data);
            exp = data;
            this.updateExpGui();
        }
    }

    private class GSubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            updateGlobal();
        }
    }

    private void updateGlobal() {
        GlobalContext newc = GlobalContext.getContext();
        if (context == null) {
            context = newc;
            if (context != null) {
            }
        }
    }

    /** create experiment context */
    private void update() {
        if (exp == null) {
            return;
        }
        if (checkAll()) {
            publish();
        }


    }

    @Override
    public Action[] getActions() {
        return OpenWindowAction.getActions(this);
    }

    private int checkIndices() {
        if (exp == null || exp.getResultsDirectory() == null || exp.getResultsDirectory().length() < 2) {
            return -1;
        }
        if (exp.getWellContext() == null) {
            return 0;
        }
        if (exp.getWellContext().getMask() == null) {
            return 0;
        }
        SequenceLoader loader = SequenceLoader.getSequenceLoader(exp, false, false);
        int ok = 0;
        if (loader.hasSffIndex()) {
            ok++;
        }
        if (loader.hasGenomeToReadIndex()) {
            ok++;
        }
        ScoreMask mask = ScoreMask.getMask(exp, exp.getWellContext());
        if (mask == null) {
            return ok;
        }
        if (mask.hasAllBamImages()) {
            p("Got bam images heat maps");
            ok++;
        }
//        if (mask.hasAllWellImages()) {
//            ok++;
//        }
        if (mask.hasAllSffImages()) {
            ok++;
        }
        p("Got indices: " + ok);
        // -1 .. 4
        return ok;
    }

    private int checkFiles() {
        if (exp == null || exp.getResultsDirectory() == null) {
            return -1;
        }

        if (exp == null || exp.getResultsDirectory().length() < 2) {
            return -1;
        }
        WellContext well = exp.getWellContext();
        if (well == null) {
            return 0;
        }

        int ok = 0;
        if (well.getMask() != null) {
            ok++;
        }
        if (well.getWellsfile().exists()) {
            ok++;
        }

        SequenceLoader loader = SequenceLoader.getSequenceLoader(exp, false, false);
        // this.btnCacheDatFiles.setEnabled(true);
        if (loader.foundBamFile()) {
            ok++;
        }
        if (loader.foundSffFile()) {
            ok++;
        }
        if (loader.foundBai()) {
            ok++;
        }

// -1 .. 5
        return ok;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel3 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        clear = new javax.swing.JButton();
        hint = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtRaw = new javax.swing.JTextField();
        txtResults = new javax.swing.JTextField();
        btnRaw = new javax.swing.JButton();
        btnResults = new javax.swing.JButton();
        btnView = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        btnWizard = new javax.swing.JButton();
        boxFiles = new javax.swing.JCheckBox();
        boxIndex = new javax.swing.JCheckBox();
        boxDat = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        txtCache = new javax.swing.JTextField();
        btnCache = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jPanel3.setOpaque(false);
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jToolBar1.setRollover(true);
        jToolBar1.setMaximumSize(new java.awt.Dimension(300, 21));
        jToolBar1.setMinimumSize(new java.awt.Dimension(13, 21));

        clear.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/scout/experimentviewer/document-new-3.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(clear, org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.clear.text")); // NOI18N
        clear.setToolTipText(org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.clear.toolTipText")); // NOI18N
        clear.setFocusable(false);
        clear.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        clear.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearActionPerformed(evt);
            }
        });
        jToolBar1.add(clear);

        hint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/scout/experimentviewer/help-hint.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(hint, org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.hint.text")); // NOI18N
        hint.setToolTipText(org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.hint.toolTipText")); // NOI18N
        hint.setFocusable(false);
        hint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        hint.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        hint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hintActionPerformed(evt);
            }
        });
        jToolBar1.add(hint);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel3.add(jToolBar1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.jLabel10.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        jPanel3.add(jLabel10, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.jLabel11.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        jPanel3.add(jLabel11, gridBagConstraints);

        txtRaw.setText(org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.txtRaw.text")); // NOI18N
        txtRaw.setToolTipText(org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.txtRaw.toolTipText")); // NOI18N
        txtRaw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRawActionPerformed(evt);
            }
        });
        txtRaw.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtRawFocusLost(evt);
            }
        });
        txtRaw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtRawKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        jPanel3.add(txtRaw, gridBagConstraints);

        txtResults.setText(org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.txtResults.text")); // NOI18N
        txtResults.setToolTipText(org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.txtResults.toolTipText")); // NOI18N
        txtResults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtResultsActionPerformed(evt);
            }
        });
        txtResults.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtResultsFocusLost(evt);
            }
        });
        txtResults.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtResultsKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        jPanel3.add(txtResults, gridBagConstraints);

        btnRaw.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/main/options/document-open-2.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnRaw, org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.btnRaw.text")); // NOI18N
        btnRaw.setMaximumSize(new java.awt.Dimension(40, 22));
        btnRaw.setMinimumSize(new java.awt.Dimension(40, 22));
        btnRaw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRawActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        jPanel3.add(btnRaw, gridBagConstraints);

        btnResults.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/main/options/document-open-2.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnResults, org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.btnResults.text")); // NOI18N
        btnResults.setMaximumSize(new java.awt.Dimension(40, 22));
        btnResults.setMinimumSize(new java.awt.Dimension(40, 22));
        btnResults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResultsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        jPanel3.add(btnResults, gridBagConstraints);

        btnView.setForeground(new java.awt.Color(51, 153, 0));
        btnView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/scout/offline/eye.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnView, org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.btnView.text")); // NOI18N
        btnView.setToolTipText(org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.btnView.toolTipText")); // NOI18N
        btnView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel3.add(btnView, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        jPanel3.add(jSeparator1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 1;
        jPanel3.add(jSeparator2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 1;
        jPanel3.add(jSeparator3, gridBagConstraints);

        btnWizard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/scout/offline/system-run-3.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnWizard, org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.btnWizard.text")); // NOI18N
        btnWizard.setToolTipText(org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.btnWizard.toolTipText")); // NOI18N
        btnWizard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWizardActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        jPanel3.add(btnWizard, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(boxFiles, org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.boxFiles.text")); // NOI18N
        boxFiles.setToolTipText(org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.boxFiles.toolTipText")); // NOI18N
        boxFiles.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        jPanel3.add(boxFiles, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(boxIndex, org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.boxIndex.text")); // NOI18N
        boxIndex.setToolTipText(org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.boxIndex.toolTipText")); // NOI18N
        boxIndex.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        jPanel3.add(boxIndex, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(boxDat, org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.boxDat.text")); // NOI18N
        boxDat.setToolTipText(org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.boxDat.toolTipText")); // NOI18N
        boxDat.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel3.add(boxDat, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.jLabel12.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        jPanel3.add(jLabel12, gridBagConstraints);

        txtCache.setColumns(40);
        txtCache.setText(org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.txtCache.text")); // NOI18N
        txtCache.setToolTipText(org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.txtCache.toolTipText")); // NOI18N
        txtCache.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCacheActionPerformed(evt);
            }
        });
        txtCache.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtCacheFocusLost(evt);
            }
        });
        txtCache.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCacheKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        jPanel3.add(txtCache, gridBagConstraints);

        btnCache.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/main/options/document-open-2.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnCache, org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.btnCache.text")); // NOI18N
        btnCache.setMaximumSize(new java.awt.Dimension(40, 22));
        btnCache.setMinimumSize(new java.awt.Dimension(40, 22));
        btnCache.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCacheActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        jPanel3.add(btnCache, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.jButton1.text")); // NOI18N
        jButton1.setToolTipText(org.openide.util.NbBundle.getMessage(OfflineTopComponent.class, "OfflineTopComponent.jButton1.toolTipText")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 2;
        jPanel3.add(jButton1, gridBagConstraints);

        add(jPanel3, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void txtRawActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRawActionPerformed
        // TODO add your handling code here:
        
        updateExp();
        this.updateExpGui();
    }//GEN-LAST:event_txtRawActionPerformed

    private void txtRawFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtRawFocusLost
        // TODO add your handling code here:
        if (rawChanged){ 
            updateExp();        
            this.updateExpGui();
        }
    }//GEN-LAST:event_txtRawFocusLost

    private void txtResultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtResultsActionPerformed
        // TODO add your handling code here:
        updateExp();
        this.updateExpGui();
    }//GEN-LAST:event_txtResultsActionPerformed

    private void txtResultsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtResultsFocusLost
        // TODO add your handling code here:
         if(resChanged){ 
             
             updateExp();
             this.updateExpGui();
         }
        
    }//GEN-LAST:event_txtResultsFocusLost

    private void btnRawActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRawActionPerformed
        String dir = txtRaw.getText();
        if (dir == null || dir.length() < 1) {
            dir = ".";
        }
        dir = FileTools.getDir("Please select a directory for default raw data", new File(dir));
        this.txtRaw.setText(dir);
        if (exp != null && (exp.getCacheDir() == null || exp.getCacheDir().length() < 1 || !FileUtils.exists(exp.getCacheDir()))) {
            txtCache.setText(dir);
        }
        updateExp();
        this.updateExpGui();
        // setExpContextFromGui();
    }//GEN-LAST:event_btnRawActionPerformed

    private void btnResultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResultsActionPerformed
        String dir = txtResults.getText();
        if (dir == null || dir.length() < 1) {
            dir = ".";
        }
        dir = FileTools.getDir("Please select a directory for default results data", new File(dir));
        this.txtResults.setText(dir);
        if (exp.getCacheDir() == null || exp.getCacheDir().length() < 1 || !FileUtils.exists(exp.getCacheDir())) {
            txtCache.setText(dir);
        }
        updateExp();
        this.updateExpGui();

    }//GEN-LAST:event_btnResultsActionPerformed

    private void btnViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewActionPerformed

        if (exp == null) {
            return;
        }
        if (!checkCache()) {
            return;
        }
        
        if (exp.getCacheDir()==null || exp.getCacheDir().trim().length()<1) {
            JOptionPane.showMessageDialog(this, "Please specify a cache folder to store temporary data");
            return;
        }
        if (!FileUtils.exists(exp.getCacheDir())) {
            JOptionPane.showMessageDialog(this, "The cache folder does not exist, please specify a valid folder");
            return;
        }
        publish();
        TopComponent tc = (TopComponent) WindowManager.getDefault().findTopComponent("TorrentScoutMaskViewTopComponent");
        if (tc != null) {
            tc.requestActive();
            tc.requestVisible();
        }
       
        GuiUtils.showNonModalMsg("OfflineViewer: Loading data for " + exp.getResultsDirectory());
        LookupUtils.publish(loadContent, new LoadDataContext());
    }//GEN-LAST:event_btnViewActionPerformed

    private void btnWizardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWizardActionPerformed
        startWizard();
    }//GEN-LAST:event_btnWizardActionPerformed

    private void txtCacheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCacheActionPerformed
        updateExp();
        // checkCache();
}//GEN-LAST:event_txtCacheActionPerformed

    private void txtCacheFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtCacheFocusLost
       if(cacheChanged){ 
        updateExp();
       }
}//GEN-LAST:event_txtCacheFocusLost

    private void btnCacheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCacheActionPerformed
        // TODO use properties or use of course select experiment
        String dir = txtCache.getText();
        if (dir == null || dir.length() < 1) {
            dir = ".";
        }
        dir = FileTools.getDir("Please select a directory for storing cached data", new File(dir));

        this.txtCache.setText(dir);
        updateExp();
        this.updateExpGui();
        checkCache();
}//GEN-LAST:event_btnCacheActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       this.txtResults.setText(this.txtRaw.getText());
        updateExp();
        this.updateExpGui();
        checkCache();
       
    }//GEN-LAST:event_jButton1ActionPerformed

    private void hintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hintActionPerformed

        doHintAction();     }//GEN-LAST:event_hintActionPerformed

    private void clearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearActionPerformed
        doNewAction();
    }//GEN-LAST:event_clearActionPerformed

    private void txtCacheKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCacheKeyTyped
        cacheChanged = true;
    }//GEN-LAST:event_txtCacheKeyTyped

    private void txtRawKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRawKeyTyped
        rawChanged = true;
    }//GEN-LAST:event_txtRawKeyTyped

    private void txtResultsKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtResultsKeyTyped
      resChanged = true;
    }//GEN-LAST:event_txtResultsKeyTyped

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox boxDat;
    private javax.swing.JCheckBox boxFiles;
    private javax.swing.JCheckBox boxIndex;
    private javax.swing.JButton btnCache;
    private javax.swing.JButton btnRaw;
    private javax.swing.JButton btnResults;
    private javax.swing.JButton btnView;
    private javax.swing.JButton btnWizard;
    private javax.swing.JButton clear;
    private javax.swing.JButton hint;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JTextField txtCache;
    private javax.swing.JTextField txtRaw;
    private javax.swing.JTextField txtResults;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        if (exp == null) {
            this.getExpContext();
        }
        this.updateExpGui();

    }

    public void setExperimentContext(ExperimentContext exp) {
        this.exp = exp;
        updateExpGui();
    }
    public void updateExpGui() {
        p("==== update exp gui =======");
        checkExp();
        this.txtRaw.setText(exp.getRawDir());
        this.txtResults.setText(exp.getResultsDirectory());

        String cache = exp.getCacheDir();
        if (cache != null && cache.length() > 0) {
            File f = new File(cache);
            cache = f.getParent();
        }
        if (cache == null) {
            cache = "";
        }
        this.txtCache.setText(cache);
        boolean ok = checkAll();

    }

    private void updateExp() {
        rawChanged = false;
        resChanged = false;
        cacheChanged = false;
        exp = new ExperimentContext();
        exp.setRawDir(txtRaw.getText());
        exp.setCacheDir(this.txtCache.getText());
        if (txtRaw.getText()!=null && txtRaw.getText().length()>3) {
            String r = txtResults.getText();
            if (r == null || r.length()<3) {
                txtResults.setText(txtRaw.getText());
            }
            String c = txtCache.getText();
            if (c == null || c.length()<3) {
                txtCache.setText(txtRaw.getText());
            }
        }
        //exp.setCacheDir(txtCache.getText());
        exp.setResultsDirectory(txtResults.getText());
        // offset is always 0!
       

        //exp.setCacheDir(txtCache.getText());
        exp.createWellContext();
        exp.setIgnoreRule(true);
        p("=======update Exp (setting ignoreRule) =======");
        p("exp is now: " + exp);
        parseLogFiles(txtCache.getText(), exp);
    }

    // 0 = bad, 1 = middle, 2=ok
    private void setOk(JCheckBox box, int ok, int all) {
        box.setSelected(ok >= all);
        // box.setEnabled(false);
        if (ok >= all) {
            box.setForeground(Color.green.darker());
        } else if (ok > 1) {
            box.setForeground(Color.gray.darker());
        } else {
            box.setForeground(Color.red.darker());
        }

    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    private void setStatus(String msg) {
        Message message = StatusDisplayer.getDefault().setStatusText("<html>" + msg + "</html>", StatusDisplayer.IMPORTANCE_FIND_OR_REPLACE);
        p(msg);
        message.clear(30000);
    }

    private void setStatusWarning(String msg) {
        Message message = StatusDisplayer.getDefault().setStatusText("<html>" + msg + "</html>", StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        warn(msg);
        message.clear(60000);


    }

    private void err(String msg, Exception ex) {
        Logger.getLogger(OfflineTopComponent.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(OfflineTopComponent.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(OfflineTopComponent.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        //   System.out.println("ExperimentViewerTopComponent: " + msg);
        Logger.getLogger(OfflineTopComponent.class.getName()).log(Level.INFO, msg);
    }
}

