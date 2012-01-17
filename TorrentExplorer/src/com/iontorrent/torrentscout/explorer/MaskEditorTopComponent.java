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
package com.iontorrent.torrentscout.explorer;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.guiutils.netbeans.OpenWindowAction;
import com.iontorrent.guiutils.wells.SingleCoordSelectionPanel;
import com.iontorrent.rawdataaccess.wells.BitMask;
import com.iontorrent.torrentscout.explorer.edit.MaskCalcPanel;
import com.iontorrent.torrentscout.explorer.edit.MaskCommandPanel;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.utils.io.FileTools;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellSelection;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
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
import org.openide.util.NbPreferences;
import org.openide.util.lookup.InstanceContent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//com.iontorrent.torrentscout.explorer//MaskEditor//EN",
autostore = false)
@TopComponent.Description(preferredID = "MaskEditorTopComponent",
iconBase = "com/iontorrent/torrentscout/explorer/mask.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "table_mode", openAtStartup = false)
@ActionID(category = "Window", id = "com.iontorrent.torrentscout.explorer.MaskEditorTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_MaskEditorAction",
preferredID = "MaskEditorTopComponent")
public final class MaskEditorTopComponent extends TopComponent implements ActionListener {

    HashMap<String, CompleteMaskPanel> maskmap;
    int MAXPANELS = 4 * 50;
    //  CompleteMaskPanel[] maskpanels;
    ExperimentContext expContext;
    ExplorerContext maincontext;
    ArrayList<BitMask> masks;
    MaskCommandPanel cmd;
    JTabbedPane tab;
    MaskCalcPanel calc;
    String savefile;
    private transient final InstanceContent wellSelectionContent = LookupUtils.getPublisher(WellSelection.class);
    private transient final Lookup.Result<ExperimentContext> expContextResults =
            LookupUtils.getSubscriber(ExperimentContext.class, new SubscriberListener());

    public MaskEditorTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(MaskEditorTopComponent.class, "CTL_MaskEditorTopComponent"));
        setToolTipText(NbBundle.getMessage(MaskEditorTopComponent.class, "HINT_MaskEditorTopComponent"));

        createDefaultMasks();
        calc = new MaskCalcPanel(maincontext);
        tab = new JTabbedPane();
        //this.panTop.add("North", calc);
        cmd = new MaskCommandPanel(maincontext, this);
        //panTop.add("Center", cmd);
        tab.add("Drop Down Calculator", calc);
        tab.add("Command Line Editor", cmd);
        panTop.add("Center", tab);
    }

    public void refreshAllMasks() {
        p("refreshing all masks");
        panMasks.removeAll();
        maskmap = new HashMap<String, CompleteMaskPanel>();
        masks = maincontext.getMasks();
        info.setText("   " + maincontext.getExp().getBfMaskFile());
        int nr = 0;
        if (masks != null && masks.size() > 0) {
            for (int i = 0; i < masks.size(); i++) {
                BitMask mask = masks.get(i);
                if (mask != null) {
                    nr++;
                    addMask(mask);
                }
            }
        } else {
            GuiUtils.showNonModalMsg("I see no data and hence cannot create any masks. Load some data first (select a region on the chip)");
        }

        // addEmpty();
        invalidate();
        revalidate();
        repaint();
        panMasks.repaint();

        //BitMask last = new BitMask();
    }

    protected void recreateAllMasks() {
        if (maincontext == null) {
            return;
        }
        p("====recreate all masks, calling maincontext. createMasks");
        this.maincontext.createMasks();
        this.createDefaultMasks();
        // also create drop down calculator
//        if (calc != null) {
//            tab.remove(calc);
//            calc = new MaskCalcPanel(maincontext);
//            tab.add("Drop Down Calculator", calc);
//            tab.setSelectedIndex(1);
//            tab.setSelectedIndex(0);
//            
//        }


    }

    protected boolean saveAs(boolean ask) throws HeadlessException {
        if (ask || savefile == null) {
            savefile = FileTools.getFile("Save all masks, settings, coordinates to file", "*.*", savefile, true);
        }
        if (savefile == null) {

            return true;
        }
        String res = maincontext.storeContext(savefile);
        if (res != null) {
            JOptionPane.showMessageDialog(this, res);
        } else {
            GuiUtils.showNonModalMsg("Masks were saved in " + savefile);
        }
        return false;
    }

    private void selectCoord() {
        SingleCoordSelectionPanel pan = new SingleCoordSelectionPanel();
        pan.setMaxX(this.expContext.getNrcols());
        pan.setMaxY(this.expContext.getNrrows());

        int ans = JOptionPane.showConfirmDialog(this, pan, "Enter a selection:", JOptionPane.OK_CANCEL_OPTION);
        if (ans == JOptionPane.CANCEL_OPTION) {
            return;
        }
        WellCoordinate c1 = new WellCoordinate(pan.getCoord1().getCol() - this.maincontext.getExp().getColOffset(), pan.getCoord1().getRow() - maincontext.getExp().getRowOffset());
        WellCoordinate c2 = c1.add(100, 100);
        WellCoordinate abs = pan.getCoord1();

        WellSelection sel = new WellSelection(c1, c2);
        if (maincontext.getAbsDataAreaCoord() != null && abs.equals(maincontext.getAbsDataAreaCoord())) {
            GuiUtils.showNonModalMsg("Same coordinates, I won'd to anything");
            return ;
        }
        this.maincontext.setAbsDataAreaCoord(abs);
        LookupUtils.publish(wellSelectionContent, sel);

    }

    public void addEmpty() {
        if (masks.size() < 1) {
            GuiUtils.showNonModalMsg("Got no masks as template");
            return;
        }

        BitMask first = masks.get(0);
        BitMask mask = new BitMask(first);
        mask.setName("" + masks.size());
        addMask(mask);

    }

    public void getUserPreferences() {
        if (maincontext == null) {
            return;
        }
        Preferences p = NbPreferences.forModule(com.iontorrent.torrentscout.explorer.options.TorrentExplorerPanel.class);
        int span = p.getInt("span", 5);
        int size = p.getInt("masksize", 100);
        maincontext.setRasterSize(size);
        maincontext.setSpan(span);
    }

    private void createDefaultMasks() {
        if (expContext == null) {
            expContext = GlobalContext.getContext().getExperimentContext();
        }
        if (expContext == null) {
            //  GuiUtils.showNonModalMsg("Got no experiment context");
            return;
        }


        maincontext = ExplorerContext.getCurContext(expContext);
        getUserPreferences();
        if (maincontext.getAbsDataAreaCoord() == null) {
            GuiUtils.showNonModalMsg("Got no main coordinate - but must still clear masks!");
           // return;
        }

        //    maskpanels = new CompleteMaskPanel[MAXPANELS];

        maincontext.addListener(new ContextChangeAdapter() {

            @Override
            public void maskAdded(BitMask mask) {
                addMask(mask);
            }

            @Override
            public void maskChanged(BitMask mask) {
                updateMask(mask);

            }

           

            @Override
            public void dataAreaCoordChanged(WellCoordinate coord) {
                p("Recreating all masks");
                recreateAllMasks();

            }

            @Override
            public void masksChanged() {
                p("masks changed: ");
                refreshAllMasks();
            }
        });
        refreshAllMasks();
        //BitMask last = new BitMask();


    }

    public void updateMask(BitMask mask) {
        p("mask changed: " + mask);
        CompleteMaskPanel mp = maskmap.get(mask.getName());
        if (mp == null) {
            p("got no mask panel with name " + mask.getName() + ", adding it");
            addMask(mask);
        } else {
            p("refreshing mask " + mask);
            mp.setMask(mask);
            mp.refresh();
        }
    }

    public void addMask(BitMask mask) {
        // mask.setSelectedWells...
        CompleteMaskPanel mp = new CompleteMaskPanel(this.maincontext, mask);
        maskmap.put(mask.getName(), mp);
        mp.setName(mask.getName());

        panMasks.add(mp);
        mp.repaint();
        if (!maincontext.getMasks().contains(mask)) {
            maincontext.addMask(mask);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        BitMask res = cmd.getResult();
        if (res != null) {
            p("Updating gui with result " + res);
            updateMask(res);
        }
    }

    private class SubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            getLatestExperimentContext();
        }
    }

    private void getLatestExperimentContext() {
        p("Getting latest exp context");

        final Collection<? extends ExperimentContext> items = expContextResults.allInstances();
        if (!items.isEmpty()) {
            ExperimentContext data = null;
            Iterator<ExperimentContext> it = (Iterator<ExperimentContext>) items.iterator();
            while (it.hasNext()) {
                data = it.next();
            }

            expContext = data;
            update(expContext);

        }
    }

    private void update(ExperimentContext result) {
        p("updating exp context "+result.getResultsDirectory());
        if (result == null) {
            result = GlobalContext.getContext().getExperimentContext();
        }
        if (result != null) {
            this.expContext = result;
            maincontext = ExplorerContext.getCurContext(result);
            getUserPreferences();
            createDefaultMasks();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolbar = new javax.swing.JToolBar();
        btnClear = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnSelect = new javax.swing.JButton();
        load = new javax.swing.JButton();
        saveas = new javax.swing.JButton();
        save = new javax.swing.JButton();
        hint = new javax.swing.JButton();
        info = new javax.swing.JLabel();
        panTop = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        panMasks = new javax.swing.JPanel();

        toolbar.setRollover(true);
        toolbar.setOpaque(false);

        btnClear.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/view-refresh-3.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnClear, org.openide.util.NbBundle.getMessage(MaskEditorTopComponent.class, "MaskEditorTopComponent.btnClear.text")); // NOI18N
        btnClear.setToolTipText(org.openide.util.NbBundle.getMessage(MaskEditorTopComponent.class, "MaskEditorTopComponent.btnClear.toolTipText")); // NOI18N
        btnClear.setFocusable(false);
        btnClear.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnClear.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });
        toolbar.add(btnClear);

        btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/list-add-5.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnAdd, org.openide.util.NbBundle.getMessage(MaskEditorTopComponent.class, "MaskEditorTopComponent.btnAdd.text")); // NOI18N
        btnAdd.setToolTipText(org.openide.util.NbBundle.getMessage(MaskEditorTopComponent.class, "MaskEditorTopComponent.btnAdd.toolTipText")); // NOI18N
        btnAdd.setFocusable(false);
        btnAdd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAdd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });
        toolbar.add(btnAdd);

        btnSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/select-rectangular.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSelect, org.openide.util.NbBundle.getMessage(MaskEditorTopComponent.class, "MaskEditorTopComponent.btnSelect.text")); // NOI18N
        btnSelect.setToolTipText(org.openide.util.NbBundle.getMessage(MaskEditorTopComponent.class, "MaskEditorTopComponent.btnSelect.toolTipText")); // NOI18N
        btnSelect.setFocusable(false);
        btnSelect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSelect.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnSelect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });
        toolbar.add(btnSelect);

        load.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/document-open-4.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(load, org.openide.util.NbBundle.getMessage(MaskEditorTopComponent.class, "MaskEditorTopComponent.load.text")); // NOI18N
        load.setToolTipText(org.openide.util.NbBundle.getMessage(MaskEditorTopComponent.class, "MaskEditorTopComponent.load.toolTipText")); // NOI18N
        load.setFocusable(false);
        load.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        load.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        load.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadActionPerformed(evt);
            }
        });
        toolbar.add(load);

        saveas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/document-save-as-5.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(saveas, org.openide.util.NbBundle.getMessage(MaskEditorTopComponent.class, "MaskEditorTopComponent.saveas.text")); // NOI18N
        saveas.setToolTipText(org.openide.util.NbBundle.getMessage(MaskEditorTopComponent.class, "MaskEditorTopComponent.saveas.toolTipText")); // NOI18N
        saveas.setFocusable(false);
        saveas.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveas.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        saveas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveasActionPerformed(evt);
            }
        });
        toolbar.add(saveas);

        save.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/document-save-5.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(save, org.openide.util.NbBundle.getMessage(MaskEditorTopComponent.class, "MaskEditorTopComponent.save.text")); // NOI18N
        save.setToolTipText(org.openide.util.NbBundle.getMessage(MaskEditorTopComponent.class, "MaskEditorTopComponent.save.toolTipText")); // NOI18N
        save.setFocusable(false);
        save.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        save.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });
        toolbar.add(save);

        hint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/help-hint.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(hint, org.openide.util.NbBundle.getMessage(MaskEditorTopComponent.class, "MaskEditorTopComponent.hint.text")); // NOI18N
        hint.setToolTipText(org.openide.util.NbBundle.getMessage(MaskEditorTopComponent.class, "MaskEditorTopComponent.hint.toolTipText")); // NOI18N
        hint.setFocusable(false);
        hint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        hint.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        hint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hintActionPerformed(evt);
            }
        });
        toolbar.add(hint);

        org.openide.awt.Mnemonics.setLocalizedText(info, org.openide.util.NbBundle.getMessage(MaskEditorTopComponent.class, "MaskEditorTopComponent.info.text")); // NOI18N
        toolbar.add(info);

        panTop.setLayout(new java.awt.BorderLayout());

        panMasks.setLayout(new java.awt.GridLayout(5, 4));
        jScrollPane1.setViewportView(panMasks);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 827, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 827, Short.MAX_VALUE)
            .addComponent(panTop, javax.swing.GroupLayout.DEFAULT_SIZE, 827, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        recreateAllMasks();

    }//GEN-LAST:event_btnClearActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        addEmpty();

    }//GEN-LAST:event_btnAddActionPerformed

    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        selectCoord();
		}//GEN-LAST:event_btnSelectActionPerformed

    private void loadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadActionPerformed
        String file = FileTools.getFile("Load masks, settings, coordinates from file", "*.*", null, false);
        if (file == null) {
            return;
        }
        GuiUtils.showNonModalMsg("Loading masks and refreshing all views...");
        String res = maincontext.loadContext(file);
        if (res != null) {
            JOptionPane.showMessageDialog(this, res);
        }

        JOptionPane.showMessageDialog(this, "The masks and coordinates have been loaded");
        // this.refreshAllMasks();
    }//GEN-LAST:event_loadActionPerformed

    private void saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveActionPerformed
        saveAs(false);
    }//GEN-LAST:event_saveActionPerformed

    private void saveasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveasActionPerformed
        saveAs(true);
    }//GEN-LAST:event_saveasActionPerformed

    private void hintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hintActionPerformed

        doHintAction();     }//GEN-LAST:event_hintActionPerformed

    private void doHintAction() {
        String msg = "<html>You can do the following things here:<ul>";
        msg += "<li>Compute with masks: add, subtract, xor, shift, invert etc</li>";
        msg += "<li>Clear all masks and start again fresh from the masks based on bfmask.bin (if it exists), or else just pinned and not pinned</li>";
        msg += "<li>Create new masks to be used in mask based neighbor subtraction, histogram view and automate (to compute median signal)</li>";
        msg += "</ul></html>";
        JOptionPane.showMessageDialog(this, msg);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnSelect;
    private javax.swing.JButton hint;
    private javax.swing.JLabel info;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton load;
    private javax.swing.JPanel panMasks;
    private javax.swing.JPanel panTop;
    private javax.swing.JButton save;
    private javax.swing.JButton saveas;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables

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

    @Override
    public Action[] getActions() {
        return OpenWindowAction.getActions(this);
    }

    @Override
    public void componentOpened() {

        //   DOACTIONS = false;
        //   loadPreferences();
        getLatestExperimentContext();
        this.createDefaultMasks();


    }

    private void p(String msg) {
        System.out.println("MaskEditor: " + msg);
    }

    private void setStatus(String msg) {
        Message message = StatusDisplayer.getDefault().setStatusText("<html>MaskEditor: " + msg + "</html>", StatusDisplayer.IMPORTANCE_FIND_OR_REPLACE);
        message.clear(30000);
    }

    private void setStatusWarning(String msg) {
        Message message = StatusDisplayer.getDefault().setStatusText("<html>MaskEditor: " + msg + "</html>", StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        message.clear(120000);
    }

    private void setStatusError(String msg) {
        Message message = StatusDisplayer.getDefault().setStatusText("<html>MaskEditor: " + msg + "</html>", StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        message.clear(240000);
    }
}
