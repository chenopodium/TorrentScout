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
import com.iontorrent.expmodel.FiletypeListener;
import com.iontorrent.expmodel.FlowListener;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.guiutils.flow.FiletypePanel;
import com.iontorrent.guiutils.flow.FlowNrPanel;
import com.iontorrent.guiutils.netbeans.OpenWindowAction;
import com.iontorrent.rawdataaccess.pgmacquisition.DataAccessManager;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.rawdataaccess.wells.BitMask;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.torrentscout.explorer.automate.MultiFlowPanel;
import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.utils.ProgressListener;
import com.iontorrent.utils.io.FileTools;
import com.iontorrent.wellalgorithms.NearestNeighbor;
import com.iontorrent.wellalgorithms.WellAlgorithm;
import com.iontorrent.wellmodel.RasterData;
import com.iontorrent.wellmodel.WellFlowDataResult;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import org.iontorrent.acqview.MultiAcqPanel;
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

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//com.iontorrent.torrentscout.explorer//Automate//EN",
autostore = false)
@TopComponent.Description(preferredID = "AutomateTopComponent",
iconBase = "com/iontorrent/torrentscout/explorer/system-run-3.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "right_editor_mode", openAtStartup = false)
@ActionID(category = "Window", id = "com.iontorrent.torrentscout.explorer.AutomateTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_AutomateAction",
preferredID = "AutomateTopComponent")
public final class AutomateTopComponent extends TopComponent implements TaskListener, FlowListener, FiletypeListener {

    ExplorerContext maincont;
    private FlowNrPanel flowPanel;
    private FiletypePanel typePanel;
    ArrayList<Integer> flows;
    RawType filetype;
    MultiAcqPanel cview;
    MultiFlowPanel cmulti;
    DataAccessManager manager;
    ExperimentContext expContext;
    BitMask forsignal;
    BitMask ignore;
    BitMask bg;
    RasterData maindata;
    //  ArrayList<WellFlowDataResult> results;
    WellFlowDataResult subtract;
   // int magnify;
    String baselist;
    private JTabbedPane tab;
    WellFlowDataResult multiflow;
    private WellFlowDataResult[] results;
    private int[] flownr;
    private boolean[] taskdone;
    private transient final Lookup.Result<ExperimentContext> expContextResults =
            LookupUtils.getSubscriber(ExperimentContext.class, new SubscriberListener());

    public AutomateTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(AutomateTopComponent.class, "CTL_AutomateTopComponent"));
        setToolTipText(NbBundle.getMessage(AutomateTopComponent.class, "HINT_AutomateTopComponent"));

        tab = new JTabbedPane();
        flowPanel = new FlowNrPanel(this);

        flowPanel.setText("0-7");
        flows = flowPanel.getFlows();
        typePanel = new FiletypePanel(this);

        bar.add(this.flowPanel, null, 0);
        bar.add(this.typePanel, null, 0);
      //  magnify = 100;

        addGui();
    }

    
     private void doHintAction() {
        String msg = "<html>You can do the following things here:<ul>";
        msg += "<li>it will automatically do the masked neighbor subtraction and compute a median signal for any number of flows</li>";
        msg += "<li>put in the flow numbers or ranges in the text box</li>";
        msg += "<li>pick a bg and ignore mask in the process window (and maybe check it in the mask editor!)</li>";
        msg += "<li>pick the mask of your good wells in the drop down box</li>";
        msg += "<li>if you like it can also subtract the result of any flow from the other results (subtract flow)</li>";
        msg += "<li>You can also export the data to file with the save icon</li>";
        
        
        msg += "</ul></html>";
        JOptionPane.showMessageDialog(this, msg);
    }
     
    @Override
    public void componentOpened() {
        getLatestExperimentContext();
    }

    public boolean recreateGui() {
        this.boxsignal.removeAllItems();
        // boxsignal.setModel(new DefaultComboBoxModel(maincont.getMasks()));
        boxsignal.addItem("No mask (use all wells)");
        if (maincont == null) {
            return true;
        }
        if (maincont.getMasks() != null) {
            for (BitMask m : maincont.getMasks()) {
                this.boxsignal.addItem(m);

            }

        }
        return false;
    }

    private void update(ExperimentContext result) {
         if (result == null) {
            result = GlobalContext.getContext().getExperimentContext();
        }
        if (result != null) {
            this.expContext = result;
            p("Automate: got latest exp: "+expContext.getRawDir());
            maincont = ExplorerContext.getCurContext(result);
            getUserPreferences();
            addGui();
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

    private void addGui() {

        expContext = GlobalContext.getContext().getExperimentContext();

        if (expContext == null) {
            //   GuiUtils.showNonModalMsg("Got no experiment context");
            return;
        }
        maincont = ExplorerContext.getCurContext(expContext);
        getUserPreferences();
        // boxsignal.setModel(new DefaultComboBoxModel(maincont.getMasks()));
        boxsignal.addItem("No mask (use all wells)");
        if (maincont.getMasks() != null && maincont.getMasks().size() > 0) {
            for (BitMask m : maincont.getMasks()) {
                this.boxsignal.addItem(m);

            }
            if (maincont.getSignalMask() != null) {
                boxsignal.setSelectedItem(maincont.getSignalMask());
            } else {

                boxsignal.setSelectedItem(maincont.getMasks().get(maincont.getMasks().size() - 1));
            }
        }
        info.setText(maincont.getExp().getRawDir());
//        if (maincont.getIgnoreMask() != null) {
//            boxignore.setSelectedItem(maincont.getIgnoreMask());
//        } else {
//            boxignore.setSelectedIndex(0);
//        }
//
//        if (maincont.getBgMask() != null) {
//            boxbg.setSelectedItem(maincont.getBgMask());
//        } else {
//            boxbg.setSelectedIndex(2);
//        }



        maincont.addListener(
                new ContextChangeAdapter() {

                    @Override
                    public void fileTypeChanged(RawType t) {
                        p("RawType chnged: " + t);
                        typePanel.setType(t);
                        // recomputeChart();
                    }

                    @Override
                    public void masksChanged() {
                        recreateGui();
                    }

                    @Override
                    public void maskAdded(BitMask mask) {
                        //boxignore.addItem(mask);
                        boxsignal.addItem(mask);
                        // boxbg.addItem(mask);
                    }
                });

        panel.add("Center", tab);
    }

    @Override
    public void flowChanged(ArrayList<Integer> flows) {
        this.flows = flows;
        if (maincont != null) {
            maincont.setFlow(flows.get(0));
        }
        // recomputeChart();
    }

    @Override
    public void fileTypeChanged(RawType filetype) {
        if (filetype == this.filetype) {
            return;
        }
        this.filetype = filetype;
        if (maincont != null) {
            maincont.setFiletype(filetype);
        }
        //   recomputeChart();

    }

    private void recomputeChart() {

        if (cview != null) {
            tab.remove(cview);
        }
        if (cmulti != null) {
            tab.remove(cmulti);
        }
        cview = new MultiAcqPanel(maincont.getFiletype());
        tab.add("Median Signal, Single View", cview);
        cview.setYaxis("Median count");

        cmulti = new MultiFlowPanel(maincont.getFiletype());
        tab.add("Multi flow view", cmulti);
        cmulti.setYaxis("Median count");
        flows = this.flowPanel.getFlows();

//        invalidate();
//        revalidate();
//        repaint();
//        panel.repaint();
        GuiUtils.showNonModalMsg("Computing nn etc for flows " + flows);

        this.bg = maincont.getBgMask();
        ignore = maincont.getIgnoreMask();
//        if (boxignore.getSelectedIndex() > -1) {
//            ignore = (BitMask) boxignore.getSelectedItem();
//            p("Using ignmore " + ignore.getName());
//        }

        forsignal = null;
        if (boxsignal.getSelectedIndex() > -1 && (boxsignal.getSelectedItem() instanceof BitMask)) {
            forsignal = (BitMask) boxsignal.getSelectedItem();
            p("Using mask " + forsignal.getName() + " to to compute signal");

        }
        if (ignore == null) {
            JOptionPane.showMessageDialog(this, "I see no ignore/pinned mask - you can select it in the Process component\n.Make sure you have selected a region");
            
        }
        if (ignore == forsignal) {
            JOptionPane.showMessageDialog(this, "The ignore mask = signal mask - please select the appropriate signal mask");
            return;
        }

        if (ignore == bg) {
            JOptionPane.showMessageDialog(this, "The ignore mask = bg mask, this won't work :-). You can select a bg mask in the Process windows");
            return;
        }
        if (bg == null) {
            JOptionPane.showMessageDialog(this, "You selected no BG mask - will use ALL (no pinned) wells - you can select a bg mask in the Process windows");

        }
        maincont.setSignalMask(forsignal);
        if (forsignal != null) {
            double perc = forsignal.computePercentage();
            if (perc < 1) {
                int ans = JOptionPane.showConfirmDialog(this, "<html>The signal mask only has "+perc+"% wells, do you want to still use it?"
                        +"<br><b>Did you already select a region?</b>"
                        +"<br>You might want to use the MaskEditor (and <b>refresh</b> the masks possibly) to check them</html>", "Few wells", JOptionPane.OK_CANCEL_OPTION);
                    if (ans == JOptionPane.CANCEL_OPTION) return;
            }
            else if (perc < 10) {
                JOptionPane.showMessageDialog(this, "Small percentage of flagged wells " + perc + "%  for mask " + forsignal);
            }
        }

        if (bg != null && bg.computePercentage() < 2) {
            int ans = JOptionPane.showConfirmDialog(this, "The bg mask only has " + bg.computePercentage() + "% wells, do you want to still use it?", "Few wells", JOptionPane.OK_CANCEL_OPTION);
            if (ans == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        results = new WellFlowDataResult[flows.size()];
        flownr = new int[flows.size()];
        taskdone = new boolean[flows.size()];
        int subtractflow = -1;
        subtract = null;
        multiflow = null;
        if (this.boxsub.isSelected()) {
            try {
                subtractflow = Integer.parseInt(txtSubtract.getText());
            } catch (Exception e) {
            }
        }

        if (subtractflow > -1) {
            subtract = automateFlow(subtractflow, null);
            //updateChart(task.getFlow(), res);
        }
        getUserPreferences();
        for (int i = 0; i < flows.size(); i++) {
            int flow = flows.get(i);
            flownr[i] = flow;

            if (flow != subtractflow) {

                AutomateTask task = new AutomateTask(flow, this);
                task.execute();
            } else {
                taskdone[i] = true;
            }
        }

    }

    @Override
    protected void componentActivated() {
        if (recreateGui()) {
            return;
        }

    }

    private RasterData computeNN(RasterData data, ProgressListener prog) {

        p("=========== compute NN ==========");
        int span = Math.max(1, this.maincont.getSpan());
        NearestNeighbor nn = new NearestNeighbor(span, maincont.getMedianFunction());

        //      BitMask bg = maincont.getBgMask();
//        if (boxbg.getSelectedIndex() > -1) {
//            bg = (BitMask) boxbg.getSelectedItem();
//            maincont.setBgMask(bg);
//            double perc = bg.computePercentage();
//            if (perc < 5) {
//                JOptionPane.showConfirmDialog(this, "Small percentage of flags in well " + perc + "  for mask " + bg);
//            }
//            //  GuiUtils.showNonModalMsg("Using mask " + mask.getName() + " to compute NN");
//        }

        //   RasterData nndata = nn.compute(data, mask, prog, span);
        //   p("Using ignore " + ignore + ", bg " + bg + ", span: " + span);
        RasterData nndata = nn.computeBetter(data, ignore, bg, prog, span);
//        for (int i = 0; i < 10; i++) {
//            int x = (int) (Math.random() * 50);
//            int y = (int) (Math.random() * 50);
//            //      p("Got data: " + Arrays.toString(data.getTimeSeries(x, y, 0)));
//            //        p("Got nndata: " + Arrays.toString(data.getTimeSeries(x, y, 0)));
//
//        }
        // maincont.setData(nndata);
        //    p(" ========= end NN ================");
        return nndata;
    }

    public RasterData loadData(int flow, ProgressListener list) {

        RasterData data = null;
        p(" =============================== load Data ===========");
        if (list != null) {
            list.setMessage("Loading flow " + flow + ", type: " + maincont.getFiletype() + ", coord: " + maincont.getAbsDataAreaCoord());
        }
        p("About to load data for " + maincont.getExp().getRawDir() + ", flow " + flow + ", type: " + maincont.getFiletype() + ", REL coord: " + maincont.getRelativeDataAreaCoord());
        manager = DataAccessManager.getManager(maincont.getExp().getWellContext());
        try {
            data = manager.getRasterDataForArea(data, maincont.getRasterSize(), maincont.getRelativeDataAreaCoord(), flow, maincont.getFiletype(), null, 0, -1);
            if (data != null) {
                maindata = data;
            }
        } catch (Exception e) {
            p("Got an error when loading: " + ErrorHandler.getString(e));
        }
        return data;

    }

    private void updateMultiChart() {
        // create one huge dataset for all flows, show it
        baselist = "";
        filetype = maincont.getFiletype();
        long starttime = 0;
        ArrayList<WellFlowDataResult> chartres = new ArrayList<WellFlowDataResult>();
        for (int i = 0; i < flownr.length; i++) {
            WellFlowDataResult nndata = results[i];

            int flow = flownr[i];
            if (nndata != null) {
                p("starttime for flow " + flow + "=" + starttime);
                nndata.setStarttime(starttime);
                starttime += nndata.getLastTimeStamp();

                chartres.add(nndata);
            }

 
        }
        cmulti.setResults(chartres);
        cmulti.update("Multiflow " + (maindata.getAbsStartCol()) + "/"
                + (maindata.getAbsStartRow()) + "+" + maindata.getRaster_size(),
                maincont.getExp());

        cmulti.repaint();
    }

    private void updateChart(int flow, WellFlowDataResult nndata) {
        // update(String region, ExperimentContext expContext, WellFlowDataResult nndata, ArrayList<Integer> flows) {    
        // p("Updating chart for flow " + flow);
        if (cview != null) {
            panel.remove(cview);
        }
        if (maindata == null) {
            p("No main data");
            return;
        }
        String mask = "all wells";
        if (maincont.getSignalMask() != null) {
            mask = maincont.getSignalMask().getName() + ", " + maincont.getSignalMask().computePercentage() + "% wells";
        }
        String subtitle = mask + ", signal ";
        cview.update("Area " + maindata.getAbsStartCol() + "/" + maindata.getAbsStartRow() + "+" + (maindata.getRaster_size() + maincont.getExp().getRowOffset()), subtitle, maincont.getExp(), flows);
        cview.update(nndata, 0);
        //cview.addResult(nndata, flow);
        // p("cview should now be visible!!!");
        cview.repaint();


        //panel.repaint();
        repaint();
        invalidate();
        revalidate();
    }

    @Override
    public Action[] getActions() {
        return OpenWindowAction.getActions(this);
    }

    @Override
    public void taskDone(Task t) {
        p("Task " + t + " is done");
        setCursor(null);
        // if (t.isSuccess()) {
        AutomateTask task = (AutomateTask) t;
        WellFlowDataResult res = task.getResult();
        int flow = task.getFlow();
        // store result
        int index = -1;
        for (int i = 0; i < flownr.length; i++) {
            if (flownr[i] == flow) {
                index = i;
                break;
            }
        }
        taskdone[index] = true;
        if (res != null) {
            if (subtract != null) {
                p("SUBTRACTING " + subtract);
                res = (WellFlowDataResult) res.deepClone();
                res.subtract(subtract);
            }

            results[index] = res;
            // p("Adding result " + res + " to chart");
            updateChart(flow, res);

        } else {
            p("Could not load flow  " + flow + ":" + manager.getErrorMsg());
            GuiUtils.showNonModalMsg("I was not able to get data for flow " + flow + ":" + manager.getErrorMsg());
            return;
        }
        boolean alldone = true;
        for (int i = 0; i < flows.size(); i++) {
            if (!taskdone[i]) {
                alldone = false;
            }
        }
        if (alldone) {
            p("ALL tasks are done, will now create multichart in correct flow order: " + flownr);
            if (subtract != null) {
                p("SUBTRACTING " + subtract);
                 res = (WellFlowDataResult) res.deepClone();
                res.subtract(subtract);
            }
            updateMultiChart();
        } else {
            // p("Not all done yet: " + Arrays.toString(taskdone));
        }
    }

    private class AutomateTask extends Task {

        boolean ok;
        int flow;
        WellFlowDataResult result;

        public AutomateTask(int flow, TaskListener tlistener) {

            super(tlistener, ProgressHandleFactory.createHandle("Loading data computing for flow " + flow + "..."));
            this.flow = flow;
        }

        public WellFlowDataResult getResult() {
            return result;
        }

        public int getFlow() {
            return flow;
        }

        @Override
        public Void doInBackground() {
            try {
                result = automateFlow(flow, this);
                ok = true;
            } catch (Exception e) {
                p("Error in automate task: " + ErrorHandler.getString(e));
                ok = false;
            }
            return null;
        }

        @Override
        public boolean isSuccess() {
            return ok;
        }
    }

    public void getUserPreferences() {
        if (maincont == null) {
            return;
        }
        Preferences p = NbPreferences.forModule(com.iontorrent.torrentscout.explorer.options.TorrentExplorerPanel.class);
        int span = p.getInt("span", 5);
        int size = p.getInt("masksize", 100);
        maincont.setRasterSize(size);
        maincont.setSpan(span);

        maincont.setMedianFunction("median");
        //maincont.setMedianFunction(p.get("medianfunction", "median"));
        //p("Got median function: "+maincont.getMedianFunction());
    }

    public WellFlowDataResult automateFlow(int flow, ProgressListener list) {
        RasterData data = loadData(flow, list);
        WellFlowDataResult result = null;
        if (data != null) {
            RasterData nndata = null;

            if (boxskip.isSelected()) {
                p("skipping nn, using raw data as input");
                nndata = data;
            } else {
                nndata = computeNN(data, list);
            }
            p("Got nndata for flow " + flow + ": " + nndata);
            // now compute average
            WellAlgorithm alg = new WellAlgorithm(null, maincont.getSpan(), false, maincont.getMedianFunction());
            //      p("Computing median on nndata " + nndata);


            try {
                result = alg.computeMedian(nndata, flow, maincont.getFiletype(), ignore, forsignal);
                //  p("Got result for median: " + result + "," + Arrays.toString(result.getData()));
            } catch (Exception e) {
                //  p("Got an error" + ErrorHandler.getString(e));
            }

        } else {
           
            String msg = "I got no data for flow " + flow+ ", " + maincont.getFiletype() + " in <b>" + maincont.getExp().getRawDir() + " at " + maincont.getAbsDataAreaCoord();
           GuiUtils.showNonModalMsg(msg);
        }
        return result;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bar = new javax.swing.JToolBar();
        boxskip = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        boxsignal = new javax.swing.JComboBox();
        btnNN = new javax.swing.JButton();
        boxsub = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        txtSubtract = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        btnSave = new javax.swing.JButton();
        btnimage = new javax.swing.JButton();
        hint = new javax.swing.JButton();
        info = new javax.swing.JLabel();
        panel = new javax.swing.JPanel();

        setBackground(new java.awt.Color(204, 255, 255));

        bar.setRollover(true);
        bar.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(boxskip, org.openide.util.NbBundle.getMessage(AutomateTopComponent.class, "AutomateTopComponent.boxskip.text")); // NOI18N
        boxskip.setToolTipText(org.openide.util.NbBundle.getMessage(AutomateTopComponent.class, "AutomateTopComponent.boxskip.toolTipText")); // NOI18N
        boxskip.setFocusable(false);
        boxskip.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        boxskip.setOpaque(false);
        boxskip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxskipActionPerformed(evt);
            }
        });
        bar.add(boxskip);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(AutomateTopComponent.class, "AutomateTopComponent.jLabel3.text")); // NOI18N
        bar.add(jLabel3);

        boxsignal.setToolTipText(org.openide.util.NbBundle.getMessage(AutomateTopComponent.class, "AutomateTopComponent.boxsignal.toolTipText")); // NOI18N
        boxsignal.setMaximumSize(new java.awt.Dimension(100, 20));
        boxsignal.setMinimumSize(new java.awt.Dimension(70, 18));
        boxsignal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxsignalActionPerformed(evt);
            }
        });
        bar.add(boxsignal);

        btnNN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/system-run-3.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnNN, org.openide.util.NbBundle.getMessage(AutomateTopComponent.class, "AutomateTopComponent.btnNN.text")); // NOI18N
        btnNN.setToolTipText(org.openide.util.NbBundle.getMessage(AutomateTopComponent.class, "AutomateTopComponent.btnNN.toolTipText")); // NOI18N
        btnNN.setFocusable(false);
        btnNN.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNN.setMargin(new java.awt.Insets(1, 1, 1, 1));
        btnNN.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNNActionPerformed(evt);
            }
        });
        bar.add(btnNN);

        boxsub.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(boxsub, org.openide.util.NbBundle.getMessage(AutomateTopComponent.class, "AutomateTopComponent.boxsub.text")); // NOI18N
        boxsub.setFocusable(false);
        boxsub.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        boxsub.setOpaque(false);
        bar.add(boxsub);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(AutomateTopComponent.class, "AutomateTopComponent.jLabel5.text")); // NOI18N
        jLabel5.setToolTipText(org.openide.util.NbBundle.getMessage(AutomateTopComponent.class, "AutomateTopComponent.jLabel5.toolTipText")); // NOI18N
        bar.add(jLabel5);

        txtSubtract.setColumns(4);
        txtSubtract.setText(org.openide.util.NbBundle.getMessage(AutomateTopComponent.class, "AutomateTopComponent.txtSubtract.text")); // NOI18N
        txtSubtract.setMargin(new java.awt.Insets(1, 1, 1, 1));
        txtSubtract.setMaximumSize(new java.awt.Dimension(20, 2147483647));
        txtSubtract.setMinimumSize(new java.awt.Dimension(20, 18));
        bar.add(txtSubtract);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AutomateTopComponent.class, "AutomateTopComponent.jLabel1.text")); // NOI18N
        bar.add(jLabel1);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/maskview/document-export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSave, org.openide.util.NbBundle.getMessage(AutomateTopComponent.class, "AutomateTopComponent.btnSave.text")); // NOI18N
        btnSave.setToolTipText(org.openide.util.NbBundle.getMessage(AutomateTopComponent.class, "AutomateTopComponent.btnSave.toolTipText")); // NOI18N
        btnSave.setFocusable(false);
        btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSave.setMargin(new java.awt.Insets(1, 1, 1, 1));
        btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        bar.add(btnSave);

        btnimage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/picture-save.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnimage, org.openide.util.NbBundle.getMessage(AutomateTopComponent.class, "AutomateTopComponent.btnimage.text")); // NOI18N
        btnimage.setToolTipText(org.openide.util.NbBundle.getMessage(AutomateTopComponent.class, "AutomateTopComponent.btnimage.toolTipText")); // NOI18N
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
        org.openide.awt.Mnemonics.setLocalizedText(hint, org.openide.util.NbBundle.getMessage(AutomateTopComponent.class, "AutomateTopComponent.hint.text")); // NOI18N
        hint.setToolTipText(org.openide.util.NbBundle.getMessage(AutomateTopComponent.class, "AutomateTopComponent.hint.toolTipText")); // NOI18N
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

        org.openide.awt.Mnemonics.setLocalizedText(info, org.openide.util.NbBundle.getMessage(AutomateTopComponent.class, "AutomateTopComponent.info.text")); // NOI18N
        bar.add(info);

        panel.setOpaque(false);
        panel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, 857, Short.MAX_VALUE)
            .addComponent(bar, javax.swing.GroupLayout.DEFAULT_SIZE, 857, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(bar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    public void maskIgnoreSelected() {
//        BitMask mask = (BitMask) this.boxignore.getSelectedItem();
//        if (mask != null) {
//            maincont.setIgnoreMask(mask);
//        }
    }

    public void maskSignalSelected() {
        if (maincont == null) {
            return;
        }
        if (boxsignal.getSelectedItem() instanceof BitMask) {
            BitMask mask = (BitMask) this.boxsignal.getSelectedItem();
            if (mask != maincont.getSignalMask()) {
                maincont.setSignalMask(mask);
            }
        } else {
            if (maincont.getSignalMask() != null) {
                maincont.setSignalMask(null);
            }
        }
    }
    private void btnNNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNNActionPerformed
        this.recomputeChart();
    }//GEN-LAST:event_btnNNActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        if (tab.getSelectedIndex() == 0) {
            doSaveChartAction();
        } else {
            this.doSaveMutltiChartAction();
        }
	}//GEN-LAST:event_btnSaveActionPerformed

    private void boxsignalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxsignalActionPerformed
        maskSignalSelected();
    }//GEN-LAST:event_boxsignalActionPerformed

    private void boxskipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxskipActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_boxskipActionPerformed

    private void btnimageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnimageActionPerformed
        if (tab.getSelectedIndex() == 0) {
            cview.export();
        } else {
            cmulti.export();
        }
    }//GEN-LAST:event_btnimageActionPerformed

    private void hintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hintActionPerformed
        doHintAction();
    }//GEN-LAST:event_hintActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar bar;
    private javax.swing.JComboBox boxsignal;
    private javax.swing.JCheckBox boxskip;
    private javax.swing.JCheckBox boxsub;
    private javax.swing.JButton btnNN;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnimage;
    private javax.swing.JButton hint;
    private javax.swing.JLabel info;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel panel;
    private javax.swing.JTextField txtSubtract;
    // End of variables declaration//GEN-END:variables

    public boolean doSaveChartAction() {

        String file = FileTools.getFile("Save first chart info into file", "*.csv", "", true);
        if (file == null) {
            return true;
        }
        String csv = cview.toCSV();

        FileTools.writeStringToFile(file, csv);
        JTextArea pane = new JTextArea(50, 40);
        // pane.setContentType("text");
        pane.setText(csv);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(csv), null);
        JOptionPane.showMessageDialog(this, new JScrollPane(pane), "You can copy this to Excel", JOptionPane.INFORMATION_MESSAGE);
        return false;
    }

    public boolean doSaveMutltiChartAction() {

        String file = FileTools.getFile("Save MULTI flow chart info into file", "*.csv", "", true);
        if (file == null) {
            return true;
        }
        String csv = cmulti.toCSV();
        FileTools.writeStringToFile(file, csv);
        JTextArea pane = new JTextArea(50, 40);
        // pane.setContentType("text");
        pane.setText(csv);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(csv), null);
        JOptionPane.showMessageDialog(this, new JScrollPane(pane), "You can copy this to Excel", JOptionPane.INFORMATION_MESSAGE);
        return false;
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

    private void p(String msg) {
        System.out.println("Automate: " + msg);
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
