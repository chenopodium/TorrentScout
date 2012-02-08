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
package com.iontorrent.welltable;

import com.iontorrent.dataloading.WellDataLoadTask;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.guiutils.netbeans.OpenWindowAction;
import com.iontorrent.rawdataaccess.wells.BfMaskFlag;
import com.iontorrent.rawdataaccess.wells.ScoreMaskFlag;
import com.iontorrent.results.scores.ScoreMask;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.utils.io.FileTools;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellFilter;
import com.iontorrent.wellmodel.WellSelection;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.netbeans.api.progress.ProgressHandle;
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
@ConvertAsProperties(dtd = "-//com.iontorrent.welltable//WellTable//EN",
autostore = false)
@TopComponent.Description(preferredID = "WellTableTopComponent",
iconBase = "com/iontorrent/welltable/table-add.png",
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "table_mode", openAtStartup = false)
@ActionID(category = "Window", id = "com.iontorrent.welltable.WellTableTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_WellTableAction",
preferredID = "WellTableTopComponent")
public final class WellTableTopComponent extends TopComponent implements TaskListener {

    private transient final Lookup.Result<WellContext> contextSelection =
            LookupUtils.getSubscriber(WellContext.class, new WellContextListener());
    private transient final Lookup.Result<WellSelection> selectionSelection =
            LookupUtils.getSubscriber(WellSelection.class, new WellSelectionListener());
    private transient final Lookup.Result<WellCoordinate> coordSelection =
            LookupUtils.getSubscriber(WellCoordinate.class, new WellCoordListener());
    private transient final InstanceContent coordContent = LookupUtils.getPublisher(WellCoordinate.class);
    private WellContext cur_context;
    private WellTableModel model;
    private ProgressHandle progress;
    private WellCoordinate coord;
    private WellTable wellsTable;
    private ArrayList<WellFilter> filters;
    private transient final InstanceContent wellCoordContent = LookupUtils.getPublisher(WellCoordinate.class);
    private transient final InstanceContent wellSelectionContent = LookupUtils.getPublisher(WellSelection.class);
    private boolean DOACTIONS;

    private ScoreValueRenderer scoreRenderer;
    public WellTableTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(WellTableTopComponent.class, "CTL_WellTableTopComponent"));
        setToolTipText(NbBundle.getMessage(WellTableTopComponent.class, "HINT_WellTableTopComponent"));
        wellsTable = new WellTable();
        this.add("Center", new JScrollPane(wellsTable));

        this.wellsTable.getSelectionModel().setSelectionMode(wellsTable.getSelectionModel().SINGLE_SELECTION);
        wellsTable.setDefaultRenderer(Boolean.class, new FlagRenderer());
        // wellsTable.setDefaultRenderer(Float.class, new FlowValueRenderer());
        scoreRenderer = new ScoreValueRenderer();
        wellsTable.setDefaultRenderer(Double.class,scoreRenderer );


        wellsTable.addKeyListener(new MyKeyListener());
        //   this.addKeyListener(new MyKeyListener());
        wellsTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                int row = wellsTable.rowAtPoint(evt.getPoint());
                //    p("Mouse clicked in row " + row);
                rowSelected(row);
            }
        });
        DOACTIONS = true;
    }

    private class MyKeyListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            //     p("Got key pressed: " + e.getKeyCode());

            if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {

                int row = wellsTable.getSelectedRow();
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    row--;
                } else {
                    row++;
                }
                if (row < 0) {
                    row = 0;
                }
                if (row > wellsTable.getModel().getRowCount()) {
                    row--;
                }
                // wellsTable.setRowSelectionInterval(row, row);
                //     p("Selected row " + row);
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
            //      p("Got key typed: " + e.getKeyCode());
        }
    }

    @Override
    public Action[] getActions() {
        return OpenWindowAction.getActions(this);
    }

    private void selectRandomRow() {
        // find that row
        int row = (int) (Math.random() * wellsTable.getModel().getRowCount());
        this.wellsTable.getSelectionModel().setSelectionInterval(row, row);
        this.wellsTable.scrollRectToVisible(wellsTable.getCellRect(row, 0, true));

        this.rowSelected(row);
    }

    private void updateTableModel() {
        //      p("Updating table model");
        // setting filters
        //ScoreMask mask = new ScoreMask.
        ScoreMask smask = ScoreMask.getMask(GlobalContext.getContext().getExperimentContext(), cur_context);
        if (smask == null) {
            p("Got no score mask for exp: " + GlobalContext.getContext().getExperimentContext());
        }
        model = new WellTableModel(cur_context, smask);
        //  TableRowSorter sorter = new TableRowSorter(model);
        //  wellsTable.setRowSorter(sorter);
        wellsTable.setModel(model);
        wellsTable.hide(BfMaskFlag.FBADKEY.getName());
        wellsTable.hide(BfMaskFlag.FBADPPF.getName());
        wellsTable.hide(BfMaskFlag.FBADRESIDUAL.getName());
        wellsTable.hide(BfMaskFlag.FSHORT.getName());
        wellsTable.hide(BfMaskFlag.EMPTY.getName());
       // wellsTable.hide(ScoreMaskFlag.IE.getName());
       // wellsTable.hide(ScoreMaskFlag.DR.getName());
        wellsTable.hide(ScoreMaskFlag.Q10LEN.getName());
      //  wellsTable.hide(ScoreMaskFlag.Q17LEN.getName());
        wellsTable.hide(ScoreMaskFlag.Q20LEN.getName());
       // wellsTable.hide(ScoreMaskFlag.TLEN.getName());
      //  wellsTable.hide(ScoreMaskFlag.Q7LEN.getName());
       // wellsTable.hide(ScoreMaskFlag.CUSTOM2.getName());
       // wellsTable.hide(ScoreMaskFlag.CUSTOM3.getName());
       // wellsTable.hide(ScoreMaskFlag.NCALL.getName());
       // wellsTable.hide(ScoreMaskFlag.CAFIE.getName());
      // wellsTable.hide(ScoreMaskFlag.SNR.getName());
       // wellsTable.hide(ScoreMaskFlag.PPF.getName());
       // wellsTable.hide(ScoreMaskFlag.QLEN.getName());
        wellsTable.hide(BfMaskFlag.PINNED.getName());
        wellsTable.hide(BfMaskFlag.EXCLUDE.getName());
        //wellsTable.hide(BfMaskFlag..name());
        model.fireTableDataChanged();
        //    wellsTable.invalidate();
        //    wellsTable.revalidate();
        selectRandomRow();
        requestActive();
        this.requestVisible();
    }

    private class WellContextListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            getLatestContext();
        }
    }

    private class WellCoordListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            if (DOACTIONS) {
                getLatestCoord();
            }
        }
    }

    private void getLatestContext() {
        final Collection<? extends WellContext> selections = contextSelection.allInstances();
        if (!selections.isEmpty()) {
            WellContext context = null;
            Iterator<WellContext> cit = (Iterator<WellContext>) selections.iterator();
            while (cit.hasNext()) {
                context = cit.next();
                //      p("Got context: " + context);
            }
//            if (cur_context == context) {
//                return;
//            }
            cur_context = context;
            if(cur_context != null) cur_context.setFilters(filters);
           // boolean loadScores = context.getMask().getNrCols() < 1500;
           // this.btnLoadScores.setSelected(loadScores);
            //   p("SubscriberListener Got WellContext:" + cur_context);
            updateTable(cur_context);
        }

    }

    private void getLatestCoord() {
        final Collection<? extends WellCoordinate> selections = coordSelection.allInstances();
        if (!selections.isEmpty()) {
            Iterator<WellCoordinate> cit = (Iterator<WellCoordinate>) selections.iterator();
            WellCoordinate newcoord = null;
            while (cit.hasNext()) {
                newcoord = cit.next();

            }
            if (newcoord == null) {
                return;
            }
          
           
            if (cur_context == null) this.getLatestContext();
            //   p("SubscriberListener Got WellContext:" + cur_context);
            if (coord == null || !coord.equals(newcoord)) {
                coord = newcoord;
                p("Got coord: " + coord);
                if (cur_context != null) cur_context.setCoordinate(coord);
                updateTable(coord);
            }
        }

    }

    private class WellSelectionListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            getLatestSelection();
        }
    }

    private void getLatestSelection() {
        final Collection<? extends WellSelection> selections = selectionSelection.allInstances();
        if (!selections.isEmpty()) {
            //  p("Getting last selection");
            WellSelection selection = null;
            Iterator<WellSelection> it = (Iterator<WellSelection>) selections.iterator();
            while (it.hasNext()) {
                selection = it.next();
            }
            p("Got a selection: " + selection);
            if (cur_context == null) {
                getLatestContext();
            }
            if (cur_context == null) {
                return;
            }
            cur_context.setSelection(selection);
            //   p("SubscriberListener Got WellSelection:" + cur_context);
            updateTable(cur_context);
        }

    }

    private void rowSelected(int r) {
        // translted to sorted row
        // if (wellsTable.getRowSorter() != null) {
        if (cur_context == null) {
            this.getLatestContext();
        }
        int row = wellsTable.convertRowIndexToModel(r);
        //      p("rowSelected "+r +" ->"+row);
        //  }
        if (row < 0) {
            p("Row < 0:" + row);
            return;
        }
        WellCoordinate coord = model.getWellCoordinate(row);
        WellCoordinate test = model.getWellCoordinate(r);
//         p("Coord "+r+" would have been: "+test);
        cur_context.setCoordinate(coord);
        p("Sending coordinate of context: " + coord);
        DOACTIONS = false;
      //  GuiUtils.showNonModalMsg("WellTable: Loading data for coord "+cur_context.getAbsoluteCoordinate());
        if (coord != null) LookupUtils.publish(coordContent, coord);
        DOACTIONS = true;
    }

    @Override
    public void taskDone(Task t) {
        p("Task " + t + " is done");
        setCursor(null);

        WellDataLoadTask task = (WellDataLoadTask) t;
        this.wellsTable.setScoresLoaded(task.isLoadScores());
        updateTableModel();

    }

    private void updateTable(WellCoordinate coord) {
        // first find coord in table
        if (cur_context == null) {
            this.getLatestContext();
        }

        if (model == null) {
            return;
        }
        if (cur_context == null || coord == null) {
            this.setStatusError("Got no well context for some reason...");
            return;
        }
        int r = model.findRow(coord);
        if (r > -1) {
            int row = wellsTable.convertRowIndexToView(r);
            p("Found coord " + coord + " in table at row " + r + " -> in table model: " + row + ", rowindexodel would be: " + wellsTable.convertRowIndexToView(row));
            this.wellsTable.getSelectionModel().setSelectionInterval(row, row);
            this.wellsTable.scrollRectToVisible(wellsTable.getCellRect(row, 0, true));
        } else {
            p("Could not find coord " + coord + " in table. Creating new selection");
            WellSelection selection = new WellSelection(coord, coord);
            selection.createCoordsForEntireArea();
            this.cur_context.setSelection(selection);
            updateTable(cur_context);
            this.wellsTable.getSelectionModel().setSelectionInterval(0, 0);
        }
    }

    private void doExportAction() {
        String file = FileTools.getFile("Exporting table to .csv file", "*.csv", "welltable.csv", true);
        String csv = wellsTable.toCsv();
        FileTools.writeStringToFile(file, csv);
         JTextArea pane = new JTextArea(50, 40);
        // pane.setContentType("text");
        pane.setText(csv);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(csv), null);
        JOptionPane.showMessageDialog(this, new JScrollPane(pane), "You can copy this to Excel", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateTable(WellContext context) {
        cur_context = context;
        if (context == null || (context.getSelection() == null && context.getCoordinate()==null)) {
            //  this.setStatusError("Got no well context or no selection...");
            return;
        }
        //  p("Updating table - use progress bar");
        String msg = "Loading data for wells....";

        // progress = ProgressHandleFactory.createHandle(msg);

        boolean loadScores = this.btnLoadScores.isSelected();
       
        this.scoreRenderer.setScoresLoaded(loadScores);
        WellDataLoadTask task = new WellDataLoadTask(this, null, context, GlobalContext.getContext().getExperimentContext(), 5000, loadScores);
        //   p("LOADING DATA FOR SELECTED WELLS");
        if (!loadScores) {
            //    GuiUtils.showNonModelMsg("Got "+context.getMask().getNrCols()+"x"+context.getMask().getNrRows()+" wells, not loading scores heat map automatically");
            task.loadData(loadScores);
            this.wellsTable.setScoresLoaded(loadScores);
            updateTableModel();
        } else {
            //   GuiUtils.showNonModelMsg("TableComp: Loading table data, including scores heat map...");
            
            task.loadData(false);
            updateTableModel();
            if (loadScores && context.getMask().getNrCols() > 1500) {
                GuiUtils.showNonModalMsg("Also loading various scores... ", false, 10);
            }
            task.doInBackground();
        }
        // progress.progress(100);
        // progress.finish();

        //task.execute();
        //  setStatus("Select a row in the table to view ionoagrams or raw data");

    }

    private void p(String s) {
        System.out.println("WellTableTopComp:" + s);
    }

    private void setStatus(String msg) {
        Message message = StatusDisplayer.getDefault().setStatusText("<html>" + msg + "</html>", StatusDisplayer.IMPORTANCE_FIND_OR_REPLACE);
        message.clear(30000);
    }

    private void setStatusWarning(String msg) {
        Message message = StatusDisplayer.getDefault().setStatusText("<html>" + msg + "</html>", StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        message.clear(120000);
    }

    private void setStatusError(String msg) {
        Message message = StatusDisplayer.getDefault().setStatusText("<html>" + msg + "</html>", StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        message.clear(240000);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolBar = new javax.swing.JToolBar();
        btnFilter = new javax.swing.JButton();
        btnRandom = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        btnLoadScores = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        spinCol = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        spinRow = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        btnFindWell = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        toolBar.setRollover(true);
        toolBar.setOpaque(false);

        btnFilter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/welltable/filter.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnFilter, org.openide.util.NbBundle.getMessage(WellTableTopComponent.class, "WellTableTopComponent.btnFilter.text")); // NOI18N
        btnFilter.setToolTipText(org.openide.util.NbBundle.getMessage(WellTableTopComponent.class, "WellTableTopComponent.btnFilter.toolTipText")); // NOI18N
        btnFilter.setFocusable(false);
        btnFilter.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnFilter.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilterActionPerformed(evt);
            }
        });
        toolBar.add(btnFilter);

        btnRandom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/welltable/random16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnRandom, org.openide.util.NbBundle.getMessage(WellTableTopComponent.class, "WellTableTopComponent.btnRandom.text")); // NOI18N
        btnRandom.setToolTipText(org.openide.util.NbBundle.getMessage(WellTableTopComponent.class, "WellTableTopComponent.btnRandom.toolTipText")); // NOI18N
        btnRandom.setFocusable(false);
        btnRandom.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRandom.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRandom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRandomActionPerformed(evt);
            }
        });
        toolBar.add(btnRandom);

        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/welltable/document-export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnExport, org.openide.util.NbBundle.getMessage(WellTableTopComponent.class, "WellTableTopComponent.btnExport.text")); // NOI18N
        btnExport.setToolTipText(org.openide.util.NbBundle.getMessage(WellTableTopComponent.class, "WellTableTopComponent.btnExport.toolTipText")); // NOI18N
        btnExport.setFocusable(false);
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });
        toolBar.add(btnExport);

        org.openide.awt.Mnemonics.setLocalizedText(btnLoadScores, org.openide.util.NbBundle.getMessage(WellTableTopComponent.class, "WellTableTopComponent.btnLoadScores.text")); // NOI18N
        btnLoadScores.setToolTipText(org.openide.util.NbBundle.getMessage(WellTableTopComponent.class, "WellTableTopComponent.btnLoadScores.toolTipText")); // NOI18N
        btnLoadScores.setFocusable(false);
        btnLoadScores.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnLoadScores.setMargin(new java.awt.Insets(10, 2, 10, 2));
        btnLoadScores.setOpaque(false);
        btnLoadScores.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadScoresActionPerformed(evt);
            }
        });
        toolBar.add(btnLoadScores);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(WellTableTopComponent.class, "WellTableTopComponent.jLabel2.text")); // NOI18N
        toolBar.add(jLabel2);

        spinCol.setColumns(8);
        spinCol.setText(org.openide.util.NbBundle.getMessage(WellTableTopComponent.class, "WellTableTopComponent.spinCol.text")); // NOI18N
        spinCol.setMaximumSize(new java.awt.Dimension(100, 30));
        toolBar.add(spinCol);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(WellTableTopComponent.class, "WellTableTopComponent.jLabel1.text")); // NOI18N
        toolBar.add(jLabel1);

        spinRow.setColumns(8);
        spinRow.setText(org.openide.util.NbBundle.getMessage(WellTableTopComponent.class, "WellTableTopComponent.spinRow.text")); // NOI18N
        spinRow.setMaximumSize(new java.awt.Dimension(100, 30));
        toolBar.add(spinRow);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(WellTableTopComponent.class, "WellTableTopComponent.jLabel3.text")); // NOI18N
        toolBar.add(jLabel3);

        btnFindWell.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/welltable/eye.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnFindWell, org.openide.util.NbBundle.getMessage(WellTableTopComponent.class, "WellTableTopComponent.btnFindWell.text")); // NOI18N
        btnFindWell.setToolTipText(org.openide.util.NbBundle.getMessage(WellTableTopComponent.class, "WellTableTopComponent.btnFindWell.toolTipText")); // NOI18N
        btnFindWell.setFocusable(false);
        btnFindWell.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnFindWell.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnFindWell.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFindWellActionPerformed(evt);
            }
        });
        toolBar.add(btnFindWell);

        add(toolBar, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void btnFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterActionPerformed
        if (cur_context == null) {
            return;
        }
        WellFilterPanel pan = new WellFilterPanel(cur_context);
        pan.setSelected(cur_context.getFilters());
        
        
        ScoreMaskFilterPanel pan1 = new ScoreMaskFilterPanel(cur_context);
        pan1.setSelected(cur_context.getFilters());
        
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        if (this.btnLoadScores.isSelected()) {
            p.add("East", pan1);
        }
        else p.add("East", new JLabel("<html>To enable filtering by scores,<br>select the <b>'load scores'</b> checkbox first<br>(on the top of the table)</html>"));
        p.add("West", pan);
        int ans = JOptionPane.showConfirmDialog(this, p, "Select a well filter", JOptionPane.OK_CANCEL_OPTION);
        if (ans != JOptionPane.OK_OPTION) {
            return;
        }
        filters = pan.getSelectedFilters();
        ArrayList<WellFilter> filters1 = pan1.getSelectedFilters();
        if (filters == null) filters = filters1;
        else if (filters1 != null)  {
            filters.addAll(filters1);
        }
        cur_context.setFilters(filters);
        updateTable(cur_context);

    }//GEN-LAST:event_btnFilterActionPerformed

    private void btnRandomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRandomActionPerformed

        selectRandomRow();
    }//GEN-LAST:event_btnRandomActionPerformed

    private int getInt(JTextField txt) {
        if (txt.getText() == null || txt.getText().length()<1) return 0;
        String t = txt.getText().trim();
        int i = -1;
        try {
            i = Integer.parseInt(t);
        }
        catch (Exception e) {
           JOptionPane.showMessageDialog(this,"Was unable to convert "+t+" to an integer");
        }
        return i;
    }
    private void btnFindWellActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFindWellActionPerformed
        int row = getInt(spinRow);
        int col = getInt(spinCol);
        if (cur_context == null) {
            return;
        }
        if (row < 0 ||col < 0) return;
        WellCoordinate coord = new WellCoordinate(col, row);

        WellSelection sel = new WellSelection(coord, coord);
        sel.createCoordsForEntireArea();
      
        publishCoord(coord, cur_context, sel);
}//GEN-LAST:event_btnFindWellActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        doExportAction();
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnLoadScoresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadScoresActionPerformed
        if (this.btnLoadScores.isSelected()) {
            GuiUtils.showNonModalMsg("WellTable", "Loading scores for data in table...");
            this.updateTable(cur_context);
        }
    }//GEN-LAST:event_btnLoadScoresActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnFilter;
    private javax.swing.JButton btnFindWell;
    private javax.swing.JCheckBox btnLoadScores;
    private javax.swing.JButton btnRandom;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField spinCol;
    private javax.swing.JTextField spinRow;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables

    private void publishCoord(WellCoordinate coord, WellContext context, WellSelection sel) {
        if (coord != null && context != null) {
            p("Got a coordinate: " + coord);
            if (context.getCoordinate() != null) {
                wellCoordContent.remove(context.getSelection());
            }
            if (context.getSelection() != null) {
                wellSelectionContent.remove(context.getSelection());
            }
            context.setSelection(sel);
            LookupUtils.publish(wellSelectionContent, sel);
            context.setCoordinate(coord);
          //  GuiUtils.showNonModalMsg("WellTable: Loading data for coord "+context.getAbsoluteCoordinate());
            LookupUtils.publish(wellCoordContent, coord);


        }
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
        getLatestCoord();
        updateTable(cur_context);
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");

    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");

        // TODO read your settings according to their version
    }
}
