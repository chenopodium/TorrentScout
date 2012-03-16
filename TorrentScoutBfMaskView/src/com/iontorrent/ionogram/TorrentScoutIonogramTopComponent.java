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
package com.iontorrent.ionogram;

import com.iontorrent.guiutils.netbeans.OpenWindowAction;
import com.iontorrent.rawdataaccess.wells.WellData;
import com.iontorrent.sequenceloading.SequenceLoader;
import com.iontorrent.sff.SffRead;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.FlowListener;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.guiutils.flow.FlowNrPanel;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.utils.io.FileUtils;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
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

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//com.iontorrent.ionogram//TorrentScoutIonogram//EN",
autostore = false)
@TopComponent.Description(preferredID = "TorrentScoutIonogramTopComponent",
iconBase = "com/iontorrent/ionogram/insert-chart-bar.png",
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "ionogram_mode", openAtStartup = false)
@ActionID(category = "Window", id = "com.iontorrent.ionogram.TorrentScoutIonogramTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_TorrentScoutIonogramAction",
preferredID = "TorrentScoutIonogramTopComponent")
public final class TorrentScoutIonogramTopComponent extends TopComponent implements TaskListener, FlowListener {

    private transient final Lookup.Result<WellContext> dataClassWellSelection =
            LookupUtils.getSubscriber(WellContext.class, new WellSubscriberListener());
    private transient final Lookup.Result<WellCoordinate> dataClassWellCoordinate =
            LookupUtils.getSubscriber(WellCoordinate.class, new WellCoordinateSubscriberListener());
    private transient final Lookup.Result<ExperimentContext> expContextResults =
            LookupUtils.getSubscriber(ExperimentContext.class, new ExpSubscriberListener());
    private WellContext cur_context;
    private ExperimentContext expContext;
    private IonogramPanel ionopanel;
    private MultiFlowPanel multipanel;
    private JScrollPane ionoscroll;
    private JScrollPane multiscroll;
    private boolean DOACTIONS;
    private JTabbedPane tab;
    private FlowNrPanel flowpanel;
    private JTextField txtsub;
    //  private ProgressHandle progress;
    // XXX we we add a tab for multiple ionograms?
    private MultiFlowLoader loader;

    public TorrentScoutIonogramTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(TorrentScoutIonogramTopComponent.class, "CTL_TorrentScoutIonogramTopComponent"));
        setToolTipText(NbBundle.getMessage(TorrentScoutIonogramTopComponent.class, "HINT_TorrentScoutIonogramTopComponent"));
        flowpanel = new FlowNrPanel(this);
        flowpanel.setText("0-7");

        jToolBar1.add(flowpanel);
        JButton btn = new JButton("Compute");
        btn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (loader != null && multipanel != null) {
                    try {
                        multipanel.setSubtract(Integer.parseInt(txtsub.getText()));
                    } catch (Exception ex) {
                    }
                    loader.compute(flowpanel.getFlows(), multipanel);
                }
            }
        });

        txtsub = new JTextField();
        jToolBar1.add(new JLabel(" subtract flow:"));
        txtsub.setText("-1");
        txtsub.setPreferredSize(new Dimension(30, 26));
        txtsub.setMaximumSize(new Dimension(50, 26));
        txtsub.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (loader != null && multipanel != null) {
                    try {
                        multipanel.setSubtract(Integer.parseInt(txtsub.getText()));
                    } catch (Exception ex) {
                    }
                }
            }
        });
        jToolBar1.add(txtsub);
        jToolBar1.add(btn);
    }

    private class ExpSubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            getLatestExperimentContext();
        }
    }

    private void getLatestExperimentContext() {        
       expContext = GlobalContext.getContext().getExperimentContext();        
    }

    @Override
    public void flowChanged(ArrayList<Integer> flows) {
        if (loader != null && multipanel != null) {
            loader.compute(flowpanel.getFlows(), this.multipanel);
        }
    }

    @Override
    public Action[] getActions() {
        Action[] ac = OpenWindowAction.getActions(this);
        Action[] actions = new Action[ac.length + 1];
        System.arraycopy(ac, 0, actions, 0, ac.length);
        actions[actions.length - 1] = new OpenAnotherAction();
        return actions;
    }

    public class OpenAnotherAction extends AbstractAction {

        public OpenAnotherAction() {
            super("Open additional ionogram view");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            TopComponent win = new TorrentScoutIonogramTopComponent();
            win.open();
            win.requestActive();
        }
    }

    private class WellSubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            if (getLatestContext()) {
                return;
            }
        }
    }

    private boolean getLatestContext() {
        if (GlobalContext.getContext().getExperimentContext() != null) {
            cur_context = GlobalContext.getContext().getExperimentContext().getWellContext();
            update(true);
        }
        return false;
    }

    private class WellCoordinateSubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            getLatestCoordinate();

        }
    }

    private void getLatestCoordinate() {
        this.getLatestExperimentContext();
        if (GlobalContext.getContext().getExperimentContext() != null) {
            cur_context = GlobalContext.getContext().getExperimentContext().getWellContext();
            update(true);
        }
    }

    @Override
    public void taskDone(Task t) {
        p("Task " + t + " is done");

        if (t.isSuccess()) {
            update(true);
        }


    }

    private void setStatus(String msg) {
        Message message = StatusDisplayer.getDefault().setStatusText("<html>" + msg + "</html>", StatusDisplayer.IMPORTANCE_FIND_OR_REPLACE);
        p(msg);
        message.clear(30000);
    }

    private void setStatusWarning(String msg) {
        Message message = StatusDisplayer.getDefault().setStatusText("<html>" + msg + "</html>", StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        p(msg);
        message.clear(60000);
    }

    private void update(boolean multiload) {
        
        if (expContext == null) {
            this.getLatestExperimentContext();
        }
        cur_context = expContext.getWellContext();
        if (cur_context == null) {
            this.setStatusWarning("Got no WellContext for some reason...");
            return;
        }
        if (!FileUtils.exists(cur_context.getCacheDirectory())) {
            p("Cache dir does not exist");
            return;
        }
        if (loader == null || multiload == true) {
            loader = new MultiFlowLoader(expContext);
        }
        if (ionopanel != null) this.ionoscroll.remove(ionopanel);
        if (multipanel != null) this.multiscroll.remove(multipanel);
        ionopanel = new IonogramPanel(expContext);
        multipanel = new MultiFlowPanel(expContext, loader);

        if (multiload == true) {
            loader.compute(flowpanel.getFlows(), this.multipanel);
        }

        WellCoordinate coord = cur_context.getCoordinate();
        if (coord == null) {
            return;
        }
       SequenceLoader  loader = SequenceLoader.getSequenceLoader(this.expContext);
        boolean norm = radioNorm.isSelected();

        if (loader.foundSffFile()) {
            this.expContext.setFlowOrder(loader.getFlowOrder());
        }
        p("Norm is: " + norm);
        if (norm) {
            String msg = null;
            // GuiUtils.showNonModelMsg(msg, false, 60);
            //    ProgressHandle progress = ProgressHandleFactory.createHandle(msg);
            //   progress.start();
            SffRead read = loader.getSffRead(coord.getCol(), coord.getRow(), this);
            if (read == null) {
                msg = loader.getMsg();
                if (msg != null) {
                    GuiUtils.showNonModalMsg("Got no sff read: " + msg);
                    this.setStatusWarning("Got no sff read: " + msg);

                }
                norm = false;
            } else {
                WellData welldata = cur_context.getWellData(coord);
                if (welldata != null) {
                    welldata.setNormalizedValues(read.getFlowgram());
                }
            }
        }
        boolean raw = radioRaw.isSelected() || (!norm);
        DOACTIONS = false;
        radioRaw.setSelected(raw);
        DOACTIONS = true;
        int y_max = (Integer) spinMax.getValue();
        ionopanel.setMaxY(y_max);
        ionopanel.setWellContext(cur_context, raw, !raw);
        if (ionoscroll == null) ionoscroll = new JScrollPane(ionopanel);
        else ionoscroll.setViewportView(ionopanel);

        multipanel.setMaxY(y_max);
        multipanel.setWellContext(cur_context, raw, !raw);
       if (multiscroll == null) multiscroll = new JScrollPane(multipanel);
       else multiscroll.setViewportView(multipanel);

        if (tab == null) {
            tab = new JTabbedPane();
            tab.add("Ionogram", ionoscroll);
            tab.add("Ionogram with NN raw data", multiscroll);
            add("Center", tab);
        }
        repaint();
        this.paintImmediately(0, 0, 1000, 800);
        requestActive();
        //  this.requestVisible();
        // this.toFront();            
    }

    private void err(String msg) {
        Logger.getLogger(TorrentScoutIonogramTopComponent.class.getName()).log(Level.SEVERE, msg);
    }

    private void p(String s) {
//  System.out.println("IonogramTopComp:" + s);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        group = new javax.swing.ButtonGroup();
        jToolBar1 = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        spinMax = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        radioRaw = new javax.swing.JRadioButton();
        radioNorm = new javax.swing.JRadioButton();

        setLayout(new java.awt.BorderLayout());

        jToolBar1.setRollover(true);
        jToolBar1.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(TorrentScoutIonogramTopComponent.class, "TorrentScoutIonogramTopComponent.jLabel1.text")); // NOI18N
        jLabel1.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutIonogramTopComponent.class, "TorrentScoutIonogramTopComponent.jLabel1.toolTipText")); // NOI18N
        jToolBar1.add(jLabel1);

        spinMax.setModel(new javax.swing.SpinnerNumberModel(5, 2, 50, 1));
        spinMax.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutIonogramTopComponent.class, "TorrentScoutIonogramTopComponent.spinMax.toolTipText")); // NOI18N
        spinMax.setMaximumSize(new java.awt.Dimension(50, 30));
        spinMax.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinMaxStateChanged(evt);
            }
        });
        jToolBar1.add(spinMax);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(TorrentScoutIonogramTopComponent.class, "TorrentScoutIonogramTopComponent.jLabel2.text")); // NOI18N
        jToolBar1.add(jLabel2);

        group.add(radioRaw);
        org.openide.awt.Mnemonics.setLocalizedText(radioRaw, org.openide.util.NbBundle.getMessage(TorrentScoutIonogramTopComponent.class, "TorrentScoutIonogramTopComponent.radioRaw.text")); // NOI18N
        radioRaw.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutIonogramTopComponent.class, "TorrentScoutIonogramTopComponent.radioRaw.toolTipText")); // NOI18N
        radioRaw.setOpaque(false);
        radioRaw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioRawActionPerformed(evt);
            }
        });
        jToolBar1.add(radioRaw);

        group.add(radioNorm);
        radioNorm.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(radioNorm, org.openide.util.NbBundle.getMessage(TorrentScoutIonogramTopComponent.class, "TorrentScoutIonogramTopComponent.radioNorm.text")); // NOI18N
        radioNorm.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutIonogramTopComponent.class, "TorrentScoutIonogramTopComponent.radioNorm.toolTipText")); // NOI18N
        radioNorm.setOpaque(false);
        radioNorm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioNormActionPerformed(evt);
            }
        });
        jToolBar1.add(radioNorm);

        add(jToolBar1, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void radioRawActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioRawActionPerformed
        if (DOACTIONS) {
            update(false);
        }
    }//GEN-LAST:event_radioRawActionPerformed

    private void radioNormActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioNormActionPerformed
        if (DOACTIONS) {
            update(false);
        }
    }//GEN-LAST:event_radioNormActionPerformed

    private void spinMaxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinMaxStateChanged
        if (DOACTIONS) {
            update(false);
        }
    }//GEN-LAST:event_spinMaxStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup group;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JRadioButton radioNorm;
    private javax.swing.JRadioButton radioRaw;
    private javax.swing.JSpinner spinMax;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {

        this.getLatestCoordinate();
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
}
