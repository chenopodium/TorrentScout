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
package com.iontorrent.genometochip;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.guiutils.netbeans.OpenWindowAction;
import com.iontorrent.sequenceloading.SequenceLoader;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.utils.io.FileTools;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JScrollPane;
import org.iontorrent.seq.Read;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
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
@ConvertAsProperties(dtd = "-//com.iontorrent.genometochip//TorrentScoutGenomeToChip//EN",
autostore = false)
@TopComponent.Description(preferredID = "TorrentScoutGenomeToChipTopComponent",
iconBase = "com/iontorrent/genometochip/chromo_ss.gif",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "right_editor_mode", openAtStartup = false)
@ActionID(category = "Window", id = "com.iontorrent.genometochip.TorrentScoutGenomeToChipTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_TorrentScoutGenomeToChipAction",
preferredID = "TorrentScoutGenomeToChipTopComponent")
public final class TorrentScoutGenomeToChipTopComponent extends TopComponent implements TaskListener {

    private transient final InstanceContent coordContent = LookupUtils.getPublisher(WellCoordinate.class);
    private ProgressHandle progress;
    private ExperimentContext expContext;
    private GlobalContext global;
    private WellContext wellcontext;
    private transient final Lookup.Result<ExperimentContext> expContextResults =
            LookupUtils.getSubscriber(ExperimentContext.class, new SubscriberListener());
    private boolean DOACTIONS;
    ReadlTableModel model;
    ReadTable table;
    private IndexTask task;

    public TorrentScoutGenomeToChipTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(TorrentScoutGenomeToChipTopComponent.class, "CTL_TorrentScoutGenomeToChipTopComponent"));
        setToolTipText(NbBundle.getMessage(TorrentScoutGenomeToChipTopComponent.class, "HINT_TorrentScoutGenomeToChipTopComponent"));
        table = new ReadTable();
        setOpaque(false);
        table.setOpaque(false);
        JScrollPane s = new JScrollPane(table);
        s.setOpaque(false);;
        this.add("Center", s);

        this.table.getSelectionModel().setSelectionMode(table.getSelectionModel().SINGLE_SELECTION);
        table.setDefaultRenderer(Integer.class, new ReadTableRenderer());
//        // wellsTable.setDefaultRenderer(Float.class, new FlowValueRenderer());
//        table.setDefaultRenderer(Double.class, new ScoreValueRenderer());


        table.addKeyListener(new MyKeyListener());
        //   this.addKeyListener(new MyKeyListener());
        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                int row = table.rowAtPoint(evt.getPoint());
                p("Mouse clicked in row " + row);
                rowSelected(row);
            }
        });
        DOACTIONS = true;
    }

    private class MyKeyListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            p("Got key pressed: " + e.getKeyCode());

            if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {

                int row = table.getSelectedRow();
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    row--;
                } else {
                    row++;
                }
                if (row < 0) {
                    row = 0;
                }
                if (row > table.getModel().getRowCount()) {
                    row--;
                }
                // wellsTable.setRowSelectionInterval(row, row);
                p("Selected row " + row);
                rowSelected(row);
            } else {
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            //   p("Got key released: " + e.getKeyCode());
        }

        @Override
        public void keyTyped(KeyEvent e) {
            p("Got key typed: " + e.getKeyCode());
        }
    }

    private void rowSelected(int r) {
        // translted to sorted row
        // if (wellsTable.getRowSorter() != null) {

        int row = table.convertRowIndexToModel(r);
        p("rowSelected " + r + " ->" + row);
        //  }
        if (row < 0) {
            p("Row < 0:" + row);
            return;
        }
        Read read = model.getRead(row);
        WellCoordinate coord = new WellCoordinate(read.getCol(), read.getRow());

        wellcontext.setCoordinate(coord);
        wellcontext.loadMaskData(coord);
        p("Sending coordinate of context: " + coord);
        DOACTIONS = false;
        LookupUtils.publish(coordContent, coord);
        DOACTIONS = true;
    }

    private void update() {
        if (global == null) {
            return;
        }

        if (expContext == null) {
            this.getLatestExperimentContext();
        }

        if (expContext == null) {
            setStatusWarning("Got no experiment context, don't know where the sff and sam files are - creating dummy context");
            expContext = ExperimentContext.createFake(GlobalContext.getContext());
            update(expContext);         
        }
        if (expContext == null) {
            return;
        }

        if (wellcontext == null) {
            wellcontext = this.expContext.createWellContext();
        }
        if (wellcontext == null) {
            wellcontext = this.expContext.createWellContext();
            GuiUtils.showNonModalMsg("I have no well context for some reason...");
            return;
        }

        SequenceLoader loader = SequenceLoader.getSequenceLoader(this.expContext);

        int genomepos = (Integer) this.spinGenome.getValue();
        if (!loader.hasGenomeToReadIndex()) {
            if (task != null) {
                GuiUtils.showNonModalMsg("Please wait, I am already creating a genome-> read index...");
            } else {
                task = new IndexTask(this);

                task.execute();
            }
            return;
        } else {
            String msg = "GenomeToChip: finding reads for position" + genomepos + "....";
            //    progress = ProgressHandleFactory.createHandle(msg);
            GuiUtils.showNonModalMsg(msg);
        }
        ArrayList<WellCoordinate> coords = loader.findWellCoords(genomepos);
        p("FindCoords at " + genomepos + ":" + coords);
        String error = loader.getMsg();
        if (error != null) {
            this.setStatusError(error);
        }
        // ERR MOVE THIS TO EXP PANEL
        WellSelection sel = new WellSelection(coords);

        GuiUtils.showNonModalMsg("Loading mask flags for found wells...");
        sel.loadDataForWells(wellcontext.getMask());
        p("Created well selection " + sel + " with " + coords.size() + "  coords");
        ArrayList<Read> reads = loader.getReadForCoords(coords);

        error = loader.getMsg();
        if (error != null) {
            this.setStatusError(error);
        }

        updateTableModel(reads, genomepos);
    }

    private class IndexTask extends Task {

        public IndexTask(TaskListener tlistener) {
            super(tlistener, ProgressHandleFactory.createHandle("GenomeToChip: creating genome to read index..."));
            progress = this.getProgressHandle();

        }

        @Override
        public Void doInBackground() {
            String msg = "GenomeToChip: creating genome -> read index ....";
            progress = ProgressHandleFactory.createHandle(msg);
            int time = 60;
            if (expContext.is316()) {
                time = 120;
            } else if (expContext.is318()) {
                time = 240;
            }
            GuiUtils.showNonModalMsg(msg, false, time);
            SequenceLoader loader = SequenceLoader.getSequenceLoader(expContext);
            loader.createGenomeToReadIndex();
            return null;
        }

        public boolean isSuccess() {
            SequenceLoader loader = SequenceLoader.getSequenceLoader(expContext);
            return loader.hasGenomeToReadIndex();
        }
    }

    @Override
    public void taskDone(Task t) {
        p("Task " + t + " is done");
        setCursor(null);
        task = null;
        if (t.isSuccess()) {
            update();
        } else {
            GuiUtils.showNonModalMsg("Genome to read indexing task failed");
            p("Task not successful");
        }

    }

    private void updateTableModel(ArrayList<Read> reads, long genomepos) {
        p("Updating table model with " + reads.size() + "  reads for genome pos " + genomepos);
        model = new ReadlTableModel(reads, genomepos);
        //  TableRowSorter sorter = new TableRowSorter(model);
        //  wellsTable.setRowSorter(sorter);
        table.setModel(model);

        //wellsTable.hide(BfMaskFlag..name());
        model.fireTableDataChanged();
        //    wellsTable.invalidate();
        //    wellsTable.revalidate();
        //     selectRandomRow();
        requestActive();
        this.requestVisible();
    }

    private class SubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            getLatestExperimentContext();
        }
    }

    private void updateGlobal() {
        global = GlobalContext.getContext();
        if (global != null) {
        //    p("Got a global clontext with dir:" + global.getResultsDir());
           // setStatus("Got a context with results dir " + global.getResultsDir());

        } else {
            p("Got no global context, it is null!");
        }
    }

    private void doExportAction() {
        String file = FileTools.getFile("Exporting table to .csv file", "*.csv", "genometochip.csv", true);
        String csv = this.table.toCsv();
        FileTools.writeStringToFile(file, csv);
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
            update(data);

        }
    }

    private void update(ExperimentContext result) {
        //   p("updating exp context "+result.getResDir());
        this.expContext = result;
        if (global == null) {
            global = GlobalContext.getContext();
        }


    }

    private class GSubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            updateGlobal();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        btnExport = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        spinGenome = new javax.swing.JSpinner();
        jButton1 = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jToolBar1.setRollover(true);
        jToolBar1.setOpaque(false);

        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/document-export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnExport, org.openide.util.NbBundle.getMessage(TorrentScoutGenomeToChipTopComponent.class, "TorrentScoutGenomeToChipTopComponent.btnExport.text")); // NOI18N
        btnExport.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutGenomeToChipTopComponent.class, "TorrentScoutGenomeToChipTopComponent.btnExport.toolTipText")); // NOI18N
        btnExport.setFocusable(false);
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });
        jToolBar1.add(btnExport);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(TorrentScoutGenomeToChipTopComponent.class, "TorrentScoutGenomeToChipTopComponent.jLabel1.text")); // NOI18N
        jToolBar1.add(jLabel1);

        spinGenome.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(7984), Integer.valueOf(0), null, Integer.valueOf(1000)));
        spinGenome.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinGenomeStateChanged(evt);
            }
        });
        jToolBar1.add(spinGenome);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(TorrentScoutGenomeToChipTopComponent.class, "TorrentScoutGenomeToChipTopComponent.jButton1.text")); // NOI18N
        jButton1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        add(jToolBar1, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void spinGenomeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinGenomeStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_spinGenomeStateChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       
        this.update();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        doExportAction();
    }//GEN-LAST:event_btnExportActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExport;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JSpinner spinGenome;
    // End of variables declaration//GEN-END:variables

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

        DOACTIONS = false;

        getLatestExperimentContext();

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
    }

    private static void err(String ex) {
        Logger.getLogger(TorrentScoutGenomeToChipTopComponent.class.getName()).log(Level.SEVERE, ex);
    }

    private void p(String msg) {
        System.out.println("TorrentScoutGenomeToChipTopComponent: " + msg);
    }
}
