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

import com.iontorrent.guiutils.widgets.Widget;
import com.iontorrent.torrentscout.explorer.process.RasterView;
import com.iontorrent.torrentscout.explorer.process.CurveView;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.FiletypeListener;
import com.iontorrent.expmodel.FlowListener;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.guiutils.flow.FiletypePanel;
import com.iontorrent.guiutils.flow.FlowNrPanel;
import com.iontorrent.guiutils.netbeans.OpenWindowAction;
import com.iontorrent.guiutils.wells.SingleCoordSelectionPanel;
import com.iontorrent.rawdataaccess.pgmacquisition.DataAccessManager;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.rawdataaccess.wells.BitMask;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.torrentscout.explorer.options.TorrentExplorerPanel;
import com.iontorrent.torrentscout.explorer.process.FlowSelection;
import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.utils.ProgressListener;
import com.iontorrent.utils.io.FileTools;
import com.iontorrent.wellalgorithms.NearestNeighbor;
import com.iontorrent.wellmodel.RasterData;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellSelection;
import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JOptionPane;
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
import org.openide.util.NbPreferences;
import org.openide.util.lookup.InstanceContent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//com.iontorrent.torrentscout.explorer//Process//EN",
autostore = false)
@TopComponent.Description(preferredID = "ProcessTopComponent",
iconBase = "com/iontorrent/torrentscout/explorer/chart-curve-edit.png",
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "table_mode", openAtStartup = false)
@ActionID(category = "Window", id = "com.iontorrent.torrentscout.explorer.ProcessTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_ProcessAction",
preferredID = "ProcessTopComponent")
public final class ProcessTopComponent extends TopComponent implements TaskListener, FlowListener, FiletypeListener {

    ExperimentContext expContext;
    private transient final Lookup.Result<ExperimentContext> expContextResults =
            LookupUtils.getSubscriber(ExperimentContext.class, new SubscriberListener());
//    private transient final Lookup.Result<WellSelection> selectionSelection =
//            LookupUtils.getSubscriber(WellSelection.class, new WellSelectionListener());
    private transient final InstanceContent wellSelectionContent = LookupUtils.getPublisher(WellSelection.class);
    ExplorerContext maincont;
    WellSelection selection;
    CurveView cview;
    private RawType subtract_type;
    private int subtract_flow;
    RasterView raster;
    private FlowNrPanel flowPanel;
    private FiletypePanel typePanel;
    private RasterData data;
    boolean DOACTION;
    private boolean automatic_nn;

    public ProcessTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(ProcessTopComponent.class, "CTL_ProcessTopComponent"));
        setToolTipText(NbBundle.getMessage(ProcessTopComponent.class, "HINT_ProcessTopComponent"));

        flowPanel = new FlowNrPanel(this);


        if (maincont == null) {
            flowPanel.setFlow(0);
        } else {
            flowPanel.setFlow(maincont.getFlow());
            typePanel.setType(maincont.getFiletype());
        }
        typePanel = new FiletypePanel(this);


        this.toolbar.add(this.flowPanel, null, 0);
        toolbar.add(this.typePanel, null, 0);


    }

    @Override
    public void flowChanged(ArrayList<Integer> flows) {
        int f = flows.get(0);
        if (maincont == null) {
            return;
        }
        if (f == maincont.getFlow()) {
            return;
        }
        p("flow changed");
        maincont.setFlow(f);
        //this.rasterViewCreate(true, null);
        // recomputeChart();
    }

    @Override
    public void fileTypeChanged(RawType filetype) {
        if (maincont == null) {
            return;
        }

        if (maincont != null) {
            maincont.setFiletype(filetype);
        }
        p("filetypechanged");


        //    this.rasterViewCreate(true, null);

    }

    public boolean doSaveChartAction() {
        
        String file = Export.getFile("Save chart data into file", "*.csv", true);
        if (file == null) {
            return true;
        }
        boolean ok = cview.export(file);
        //mask.write(file);
        if (!ok) {
            GuiUtils.showNonModalMsg("Problem writing chart to file...");
        } else {
            GuiUtils.showNonModalMsg("Wrote chart to file " + file);
        }
        return false;
    }

    public boolean doSelectCoord() throws HeadlessException {

        SingleCoordSelectionPanel pan = new SingleCoordSelectionPanel();
        // pan.setMaxX(this.expContext.getNrcols());
        // pan.setMaxY(this.expContext.getNrrows());
        if (maincont.getAbsDataAreaCoord() != null) {
            pan.setCoord1(maincont.getAbsDataAreaCoord());
        }
        int ans = JOptionPane.showConfirmDialog(this, pan, "Enter a selection:", JOptionPane.OK_CANCEL_OPTION);
        if (ans == JOptionPane.CANCEL_OPTION) {
            return true;
        }
        // RELATIVE to current sub experiment
        WellCoordinate c1 = new WellCoordinate(pan.getCoord1().getCol() - maincont.getExp().getColOffset(), pan.getCoord1().getRow() - maincont.getExp().getRowOffset());
        WellCoordinate c2 = c1.add(100, 100);
        WellSelection sel = new WellSelection(c1, c2);
        WellCoordinate abs = pan.getCoord1();
        if (maincont.getAbsDataAreaCoord() != null && abs.equals(maincont.getAbsDataAreaCoord())) {
            GuiUtils.showNonModalMsg("Same coordinates, I won't to anything", "Process");
            return true;
        }
        maincont.setAbsDataAreaCoord(abs);

        LookupUtils.publish(wellSelectionContent, sel);

        return false;
    }

    public void getUserPreferences() {
        if (maincont == null) {
            return;
        }
        Preferences p = NbPreferences.forModule(com.iontorrent.torrentscout.explorer.options.TorrentExplorerPanel.class);
        int span = p.getInt("span", 8);
        int size = p.getInt("masksize", 100);
        automatic_nn = p.getBoolean("automatic_nn", true);
        maincont.setRasterSize(size);
        maincont.setSpan(span);
        maincont.setMedianFunction("median");
    }

   
   
    protected boolean getExpContext() {
        //  Exception e = new Exception("showing stack trace");
        //   p(ErrorHandler.getString(e));
        if (expContext == null) {
            expContext = GlobalContext.getContext().getExperimentContext();
        }
        if (expContext == null) {
            GuiUtils.showNonModalMsg("Got no experiment context from global contexet");
            return false;
        }
        maincont = ExplorerContext.getCurContext(expContext);
        info.setText(expContext.getRawDir());
        this.btnreload.setToolTipText("Reload data from dir " + expContext.getRawDir() + " at " + maincont.getAbsDataAreaCoord());
        return true;
    }

//    private class WellSelectionListener implements LookupListener {
//
//        @Override
//        public void resultChanged(LookupEvent ev) {
//            //ask
//            int ans = JOptionPane.showConfirmDialog(ProcessTopComponent.this, "Would you like to reload the data with the updated coordinate?");
//            if (ans == JOptionPane.OK_OPTION) getLatestSelection();
//        }
//    }
//    private void getLatestSelection() {
//        final Collection<? extends WellSelection> selections = selectionSelection.allInstances();
//        if (!selections.isEmpty()) {
//            //  p("Getting last selection");
//            selection = null;
//            Iterator<WellSelection> it = (Iterator<WellSelection>) selections.iterator();
//            while (it.hasNext()) {
//                selection = it.next();
//            }
//            p("Got a selection: " + selection);
//            maincont.setDataAreaCoord(selection.getCoord1());
//            maincont.getExp().getWellContext().setSelection(selection);
//
//            //   p("SubscriberListener Got WellSelection:" + cur_context);
//            GuiUtils.showNonModalMsg("Process: Got a well selection: " + selection);
//            // rasterViewUpdate(true);
//        }
//
//    }
    private void addGui() {

        if (expContext == null) {
            expContext = GlobalContext.getContext().getExperimentContext();
        }
        if (expContext == null) {
            //  GuiUtils.showNonModalMsg("Got no experiment context");
            return;
        }
        maincont = ExplorerContext.getCurContext(expContext);
        getUserPreferences();        

        rasterViewCreate(true, null);

        maincont.addListener(new ContextChangeAdapter() {

            @Override
            public void fileTypeChanged(RawType t) {
                p("RawType chnaged: " + t);
                typePanel.setType(t);
                // rasterViewCreate(true, null);
            }

          
            @Override
            public void flowChanged(int flow) {
                p("flow changed: " + flow);
                rasterViewCreate(true, "Raw data");
            }

            @Override
            public void dataChanged(RasterData data, int startrow, int startcol, int startflow, int endrow, int endcol, int endflow) {
                p("RasterData chnged: " + startrow + "/" + startcol + "/" + startflow + ", NOT reloading automatically");
                //rasterViewUpdate();
            }

            @Override
            public void coordChanged(WellCoordinate coord) {
                // p("coord chnged: " + coord);
                chartViewUpdate();
            }

            @Override
            public void dataAreaCoordChanged(WellCoordinate coord) {
                p("data area coord changed: " + coord);
                btnreload.setToolTipText("Reload data from at " + maincont.getAbsDataAreaCoord());
                // rebuildMaskComboBoxes();
                rasterViewCreate(true, "Raw data");
            }

            @Override
            public void widgetChanged(Widget w) {
                p("widget chnged: " + w);
                chartViewUpdate();
            }
        });
        if (expContext.getWellContext().getCoordinate() == null) {
            expContext.getWellContext().setCoordinate(new WellCoordinate(100, 100));
        }


    }

    private RasterData computeNN(ProgressListener prog) {
        return computeNN(prog, maincont.getData());
    }
    private RasterData computeNN(ProgressListener prog, RasterData rawdata) {
     //   p("computeNN. Maincont is: " + maincont);
        if (rawdata == null) {
            GuiUtils.showNonModalDialog("<html>I see no data yet - did you already pick a region?<br>"
                    + "(Even if you see something somewhere, if you didn't actually select a region, it might just show some sample data)</html>", "No data - region selected?");
            return null;
        }
        RasterData nndata = null;
        try {
            if (maincont == null) {
                maincont = ExplorerContext.getCurContext(expContext);
            }
            int span = Math.max(1, this.maincont.getSpan());
            NearestNeighbor nn = new NearestNeighbor(span, maincont.getMedianFunction());
            BitMask ignore = maincont.getIgnoreMask();
            BitMask take = maincont.getBgMask();
           
            if (take != null && take == ignore) {
                JOptionPane.showMessageDialog(this, "You select the same mask for ignore and bg :-). \nYou should select another mask for the bg (or you get a null result. I will just return the old data.");
                return rawdata;
            }
            if (take != null && take.computePercentage() < 1) {
                int ans = JOptionPane.showConfirmDialog(this, "<html>The bg mask only has " + take.computePercentage() + "% wells, do you want to still use it?"
                        + "<br><b>Did you already select a region?</b>"
                        + "<br>You might want to use the MaskEditor (and <b>refresh</b> the masks possibly) to check them</html>", "Few wells", JOptionPane.OK_CANCEL_OPTION);
                if (ans == JOptionPane.CANCEL_OPTION) {
                    return rawdata;
                }
            }
            maincont.setBgMask(take);
            maincont.setIgnoreMask(ignore);
            if (prog != null) prog.setMessage("Masked neighbor subtraction: ignore mask " + ignore + " and empty mask " + take);
            // RasterData nndata = nn.compute(rawdata, mask, prog, span);


         //   p("calling computebetter");
            // if (boxslow.isSelected()) nndata =nn.computeSlow(rawdata, ignore, take, prog, span);
            nndata = nn.computeBetter(rawdata, ignore, take, prog, span);


        } catch (Exception e) {
            p("Error with nn: " + ErrorHandler.getString(e));
            JOptionPane.showMessageDialog(this, "I was not able to do the masked neighbor subtraction:\n" + ErrorHandler.getString(e));
            return null;
        }
        if (nndata == null) {
            JOptionPane.showMessageDialog(this, "I was not able to do the masked neighbor subtraction - I got no error but also no result :-) ");
        }
        return nndata;
    }

    private void doHintAction() {
        String msg = "<html>You can do the following things here:<ul>";
         msg += "<li><b>Pick the masks for NN subtraction in the Mask Editor Component</b></li>";
         msg += "<li>You can change the automatic NN subtraction in the <b>Options/Explorer options tab</b></li>";
        msg += "<li>drag the cursors around in the left view with the <b>left</b> mouse button </li>";
        msg += "<li>overlay masks by selecting them in the drop down box and checking one of the 3 check boxes </li>";
        msg += "<li>move the yellow cursor (main cursor) around:";
        msg += "<ul><li>First, click on it (or the image)</li>";
        msg += "<li>the <b>cursor keys</b> will move it around</li>";
        msg += "<li>the <b>space or tab key</b> will move it to the next flagged coordinate (if a mask is overlayed)</li>";
        msg += "<li>the <b>delete key</b> will delete a flag (if a mask is overlayed)</li>";
        msg += "<li>the <b>insert key</b> will add a flag (if a mask is overlayed)</li>";
        msg += "</ul></li>";
        msg += "<li>change the number of cursors (text field) </li>";
        msg += "<li>do a <b>masked neighbor subtraction</b> by picking a bg and ingore mask and then clicking the wheels icon"
                + "<br>(it will compute the average signal using bg wells, excluding ignore wells, with the given radius around a well (span))"
                + "<br>(the span can ge changed in Tools, Options, Explorer Options)</li>";
        msg += "<li>move the image around with the right mouse button</li>";
        msg += "<li>zoom in or out of the image  with a mouse wheel</li>";
        msg += "<li>to change the size (100x100 is default), go to Tools, Options, Explorer Options</li>";
        msg += "</ul></html>";
        JOptionPane.showMessageDialog(this, msg);
    }

    @Override
    public void taskDone(Task t) {
        p("Task " + t + " is done");
        setCursor(null);
        // if (t.isSuccess()) {
        this.data = ((ComputeNN) t).getData();

        p("Got nndata " + data);
        if (data == null) {
            JOptionPane.showMessageDialog(this, "There was a problem in nn compute, I got no result");
        } else {
            maincont.setData(data);
            rasterViewCreate(false, "After masked neighbor subtraction, span=" + maincont.getSpan() + ", BG mask=" + maincont.getBgMask());
        }


    }

    private class ComputeNN extends Task {

        boolean ok;

        public ComputeNN(TaskListener tlistener) {

            super(tlistener, ProgressHandleFactory.createHandle("Computing NN..."));
        }

        public RasterData getData() {
            return data;
        }

        @Override
        public Void doInBackground() {
            data = computeNN(this);
            return null;
        }

        public boolean isSuccess() {
            return ok;
        }
    }

    private class SubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            getLatestExperimentContext();
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

            expContext = data;
            update(expContext);

        }
    }

    @Override
    public Action[] getActions() {
        return OpenWindowAction.getActions(this);
    }

    @Override
    public void componentOpened() {

        getLatestExperimentContext();

    }

    private void p(String msg) {
        System.out.println("Process: " + msg);
    }

    private void update(ExperimentContext result) {
        if (result == null) {
            result = GlobalContext.getContext().getExperimentContext();
        }
        if (result == null) {
            // GuiUtils.showNonModalMsg("Got no experiment context");
            return;
        }
        if (result != null) {
            this.expContext = result;
            maincont = ExplorerContext.getCurContext(result);
            getUserPreferences();
            addGui();
        }
    }

    public void rasterViewUpdate() {
        p("rasterviewupdate called");
        raster.update(false, maincont);

        cview.repaint();
    }

    public void rasterViewCreate(boolean load, String title) {
        p("=========rasterViewCreate called: load=" + load);
       // if (load) {
          //  Exception e = new Exception("Tracing call");
         //   p(ErrorHandler.getString(e));
      //  }
        if (!getExpContext()) {
            return;
        }
        if (load) {
            if (selection != null) GuiUtils.showNonModalMsg("Loading " + selection + " for " + maincont.getFiletype() + ", flow " + maincont.getFlow(), "Process");
        }


        String base = "?";
        if (maincont.getFiletype() == RawType.ACQ) {
            base = "" + maincont.getExp().getWellContext().getBase(maincont.getFlow());
        }
        this.flowPanel.setToolTipText("<html>Base for flow " + maincont.getFlow() + ":" + base + "<br>Flow order: " + maincont.getExp().getFlowOrder() + "</html>");

        raster = new RasterView(maincont, load);

        this.panImage.removeAll();
        panImage.add(raster);
        // repaint();
        //  p("adding raster view");
        if (load) {
            this.getUserPreferences();
        }
        raster.update(load, maincont);

        
        // paintImmediately(0,0,1000,1000);
        panImage.repaint();
        raster.repaint();

        if (load) {
            title = "Raw data";
        }     
        title += " flow "+maincont.getFlow()+"="+base;
        chartViewCreate(title);

        invalidate();
        revalidate();
        //this.paintAll(getGraphics());
        repaint();
        if (load) {
            if (this.automatic_nn) {
                p("also doing nn - disable nn");
                this.btnNN.setEnabled(false);
                btnNN.setText("");
                btnNN.setToolTipText("Automatically computing NN - check Explorer Options if you wish to change it!");            
                RasterData data = computeNN(null);
                maincont.setData(data);
                rasterViewCreate(false,"After masked neighbor subtraction, span=" + maincont.getSpan() + ", BG mask=" + maincont.getBgMask() );
            }
            else {
                this.btnNN.setEnabled(true);
                btnNN.setText("Compute NN");
                btnNN.setToolTipText("Click to compute NN bg subtraction - check Explorer Options if you wish <b>automate</b> this!");            
            }
        }

    }

    public void chartViewCreate(String title) {
        cview = new CurveView(maincont, title);
        this.panChart.removeAll();
        this.panChart.add("Center", cview);
        panChart.repaint();
        cview.repaint();

    }

    public void chartViewUpdate() {

        cview.repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolbar = new javax.swing.JToolBar();
        btnNN = new javax.swing.JButton();
        btnreload = new javax.swing.JButton();
        subtract = new javax.swing.JButton();
        btnSelect = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        hint = new javax.swing.JButton();
        refresh = new javax.swing.JButton();
        info = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        panChart = new javax.swing.JPanel();
        panImage = new javax.swing.JPanel();

        toolbar.setRollover(true);
        toolbar.setOpaque(false);

        btnNN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/system-run-3.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnNN, org.openide.util.NbBundle.getMessage(ProcessTopComponent.class, "ProcessTopComponent.btnNN.text")); // NOI18N
        btnNN.setToolTipText(org.openide.util.NbBundle.getMessage(ProcessTopComponent.class, "ProcessTopComponent.btnNN.toolTipText")); // NOI18N
        btnNN.setFocusable(false);
        btnNN.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnNN.setMargin(new java.awt.Insets(1, 1, 1, 1));
        btnNN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNNActionPerformed(evt);
            }
        });
        toolbar.add(btnNN);

        btnreload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/database-refresh.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnreload, org.openide.util.NbBundle.getMessage(ProcessTopComponent.class, "ProcessTopComponent.btnreload.text")); // NOI18N
        btnreload.setToolTipText(org.openide.util.NbBundle.getMessage(ProcessTopComponent.class, "ProcessTopComponent.btnreload.toolTipText")); // NOI18N
        btnreload.setFocusable(false);
        btnreload.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnreload.setMargin(new java.awt.Insets(1, 1, 1, 1));
        btnreload.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnreload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnreloadActionPerformed(evt);
            }
        });
        toolbar.add(btnreload);

        subtract.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/minus.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(subtract, org.openide.util.NbBundle.getMessage(ProcessTopComponent.class, "ProcessTopComponent.subtract.text")); // NOI18N
        subtract.setToolTipText(org.openide.util.NbBundle.getMessage(ProcessTopComponent.class, "ProcessTopComponent.subtract.toolTipText")); // NOI18N
        subtract.setFocusable(false);
        subtract.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        subtract.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        subtract.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subtractActionPerformed(evt);
            }
        });
        toolbar.add(subtract);

        btnSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/select-rectangular.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSelect, org.openide.util.NbBundle.getMessage(ProcessTopComponent.class, "ProcessTopComponent.btnSelect.text")); // NOI18N
        btnSelect.setToolTipText(org.openide.util.NbBundle.getMessage(ProcessTopComponent.class, "ProcessTopComponent.btnSelect.toolTipText")); // NOI18N
        btnSelect.setFocusable(false);
        btnSelect.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnSelect.setMargin(new java.awt.Insets(1, 1, 1, 1));
        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });
        toolbar.add(btnSelect);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/maskview/document-export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSave, org.openide.util.NbBundle.getMessage(ProcessTopComponent.class, "ProcessTopComponent.btnSave.text")); // NOI18N
        btnSave.setToolTipText(org.openide.util.NbBundle.getMessage(ProcessTopComponent.class, "ProcessTopComponent.btnSave.toolTipText")); // NOI18N
        btnSave.setMargin(new java.awt.Insets(1, 1, 1, 1));
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        toolbar.add(btnSave);

        hint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/help-hint.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(hint, org.openide.util.NbBundle.getMessage(ProcessTopComponent.class, "ProcessTopComponent.hint.text")); // NOI18N
        hint.setToolTipText(org.openide.util.NbBundle.getMessage(ProcessTopComponent.class, "ProcessTopComponent.hint.toolTipText")); // NOI18N
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

        refresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/view-refresh-3.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(refresh, org.openide.util.NbBundle.getMessage(ProcessTopComponent.class, "ProcessTopComponent.refresh.text")); // NOI18N
        refresh.setToolTipText(org.openide.util.NbBundle.getMessage(ProcessTopComponent.class, "ProcessTopComponent.refresh.toolTipText")); // NOI18N
        refresh.setFocusable(false);
        refresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshActionPerformed(evt);
            }
        });
        toolbar.add(refresh);

        org.openide.awt.Mnemonics.setLocalizedText(info, org.openide.util.NbBundle.getMessage(ProcessTopComponent.class, "ProcessTopComponent.info.text")); // NOI18N
        toolbar.add(info);

        panChart.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panChart.setOpaque(false);
        panChart.setLayout(new java.awt.BorderLayout());

        panImage.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panImage.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 1090, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panImage, javax.swing.GroupLayout.PREFERRED_SIZE, 546, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panChart, javax.swing.GroupLayout.DEFAULT_SIZE, 528, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 544, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 544, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panChart, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                    .addComponent(panImage, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 208, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 203, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnNNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNNActionPerformed
        p(" about to compute nn. Maincont is: " + maincont + ", data is: " + maincont.getData());
        ComputeNN task = new ComputeNN(this);
        task.execute();

    }//GEN-LAST:event_btnNNActionPerformed

    private void btnreloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnreloadActionPerformed
        rasterViewCreate(true, "Raw data");

    }//GEN-LAST:event_btnreloadActionPerformed

    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        doSelectCoord();

     }//GEN-LAST:event_btnSelectActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        doSaveChartAction();

    }
		//GEN-LAST:event_btnSaveActionPerformed

    private void refreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshActionPerformed
        
        this.raster.redrawImages();

    }//GEN-LAST:event_refreshActionPerformed

    private void subtractActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subtractActionPerformed

        RasterData data = maincont.getData();
        if (data == null) {
            JOptionPane.showMessageDialog(this, "I have no data yet to subtract anything from :-)");
            return;
        }
        if (subtract_type == null) {
            subtract_type = maincont.getFiletype();
        }
        FlowSelection pan = new FlowSelection("Select the flow and file type you wish to subtract from the currently selected data");
        pan.setFlow(subtract_flow);
        pan.setFiletype(subtract_type);
        int ans = JOptionPane.showConfirmDialog(this, pan, "Data Subtraction", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ans != JOptionPane.OK_OPTION) {
            return;
        }

        subtract_flow = pan.getFlow();
        subtract_type = pan.getFiletype();
        // otherwise load the data and then subtract
        RasterData sub = null;
        DataAccessManager manager = DataAccessManager.getManager(maincont.getExp().getWellContext());
        try {
            p("Loading subregion, RELATIVE coord " + maincont.getRelativeDataAreaCoord());
            sub = manager.getRasterDataForArea(null, maincont.getRasterSize(), maincont.getRelativeDataAreaCoord(), pan.getFlow(), pan.getFiletype(), null, 0, -1);
            // now subtract first frame
        } catch (Exception ex) {
            p("Error when loading: " + ErrorHandler.getString(ex));
        }
        if (sub == null) {
            JOptionPane.showMessageDialog(this, "I could not load flow " + pan.getFlow() + ", " + pan.getFiletype() + ", @ " + maincont.getAbsDataAreaCoord());
            return;
        }
        String what = "raw";
        if (this.automatic_nn) {
            GuiUtils.showNonModalMsg("Computing NN before subtraction");
            sub = computeNN(null,sub);
            what = "NN subtracted";
        }
            
        data.subtract(sub);
        JOptionPane.showMessageDialog(this, "I subtracted the "+what+" data from " + pan.getFlow() + ", " + pan.getFiletype() + ", @ " + maincont.getAbsDataAreaCoord());
        this.rasterViewUpdate();

    }//GEN-LAST:event_subtractActionPerformed

    private void hintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hintActionPerformed

        doHintAction();     }//GEN-LAST:event_hintActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnNN;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSelect;
    private javax.swing.JButton btnreload;
    private javax.swing.JButton hint;
    private javax.swing.JLabel info;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel panChart;
    private javax.swing.JPanel panImage;
    private javax.swing.JButton refresh;
    private javax.swing.JButton subtract;
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

    private void setStatus(String msg) {
        Message message = StatusDisplayer.getDefault().setStatusText("<html>Process: " + msg + "</html>", StatusDisplayer.IMPORTANCE_FIND_OR_REPLACE);
        message.clear(30000);
    }

    private void setStatusWarning(String msg) {
        Message message = StatusDisplayer.getDefault().setStatusText("<html>Process: " + msg + "</html>", StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        message.clear(120000);
    }

    private void setStatusError(String msg) {
        Message message = StatusDisplayer.getDefault().setStatusText("<html>Process: " + msg + "</html>", StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        message.clear(240000);
    }
}
