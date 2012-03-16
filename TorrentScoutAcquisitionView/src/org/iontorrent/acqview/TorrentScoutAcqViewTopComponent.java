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
package org.iontorrent.acqview;

import com.iontorrent.algorithms.WellAlgorithmTask;
import com.iontorrent.guiutils.FlowPanel;
import com.iontorrent.guiutils.netbeans.OpenWindowAction;
import com.iontorrent.main.data.ConverterTask;
import com.iontorrent.main.data.RawFileConverter.Conversion;
import com.iontorrent.main.startup.TorrentScoutStartupTopComponent;
import com.iontorrent.rawdataaccess.pgmacquisition.DataAccessManager;
import com.iontorrent.rawdataaccess.wells.BfMaskFlag;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.guiutils.wells.CoordSelectionPanel;
import com.iontorrent.guiutils.wells.SingleCoordSelectionPanel;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.rawdataaccess.wells.BfMask;
import com.iontorrent.sequenceloading.SequenceLoader;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.utils.stats.Stats;
import com.iontorrent.wellalgorithms.FlowBgAlg;
import com.iontorrent.wellalgorithms.NearestNeighbor;
import com.iontorrent.wellalgorithms.WellAlgorithm;
import com.iontorrent.wellalgorithms.WellContextFilter;
import com.iontorrent.wellalgorithms.ZeromerTrace;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellFlowData;
import com.iontorrent.wellmodel.WellFlowDataResult;
import com.iontorrent.wellmodel.WellFlowDataResult.ResultType;
import com.iontorrent.wellmodel.WellSelection;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import org.iontorrent.acqview.options.RawOptionsPanel;
import org.iontorrent.acqview.utils.AlgParamPanel;
import com.iontorrent.expmodel.FiletypeListener;
import com.iontorrent.guiutils.flow.FiletypePanel;
import com.iontorrent.expmodel.FlowListener;
import com.iontorrent.guiutils.flow.FlowNrPanel;
import com.iontorrent.guiutils.flow.SubtractPanel;
import com.iontorrent.utils.ToolBox;
import java.util.Arrays;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;
import org.iontorrent.acqview.utils.ParamChangeListener;
import org.iontorrent.seq.Read;

import org.netbeans.api.options.OptionsDisplayer;
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
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.iontorrent.acqview//TorrentScoutAcqView//EN",
autostore = false)
@TopComponent.Description(preferredID = "TorrentScoutAcqViewTopComponent",
iconBase = "org/iontorrent/acqview/office-chart-line.png",
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "right_editor_mode", openAtStartup = false)
@ActionID(category = "Window", id = "org.iontorrent.acqview.TorrentScoutAcqViewTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_TorrentScoutAcqViewAction",
preferredID = "TorrentScoutAcqViewTopComponent")
public final class TorrentScoutAcqViewTopComponent extends TopComponent
        implements FlowListener, FiletypeListener, TaskListener, PreferenceChangeListener, ParamChangeListener {

    private transient final Lookup.Result<WellContext> dataClassWellSelection =
            LookupUtils.getSubscriber(WellContext.class, new WellSubscriberListener());
    private transient final Lookup.Result<WellCoordinate> dataClassWellCoordinate =
            LookupUtils.getSubscriber(WellCoordinate.class, new WellCoordinateSubscriberListener());
    private transient final InstanceContent wellCoordContent = LookupUtils.getPublisher(WellCoordinate.class);
    private WellContext cur_context;
    private MultiAcqPanel acqPanel;
    private MultiFlowPanel cmulti;
    
    // private JScrollPane scroll;
    private JLabel lblMsg;
    private RawType filetype;
    private JPanel centerPanel;
    private JTabbedPane charttab;
    private CurveSelectionPanel curvePanel;
    //  private JLabel lblMin;
    //   WellFlowData data;
    ArrayList<Integer> curflows;
    private FlowNrPanel flowPanel;
    private SubtractPanel subPanel;
    private AlgParamPanel algPanel;
    private FiletypePanel typePanel;
    //   private ExperimentContext oldContext;
    private ExperimentContext expContext;
    private WellContextFilter filter;
    private ProgressHandle progress;
    private WellFlowDataResult nnresult;
    private int curtab;
    private transient final Lookup.Result<WellSelection> selectionSelection =
            LookupUtils.getSubscriber(WellSelection.class, new WellSelectionListener());
    private transient final Lookup.Result<ExperimentContext> expContextResults =
            LookupUtils.getSubscriber(ExperimentContext.class, new ExSubscriberListener());
    private JPanel panNorth;
    JPanel msgpanel;
    BfMaskFlag[] haveflags;
    BfMaskFlag[] nothaveflags;
    private int span;
    private int subtract = -1;
    LoadRawDataTask loadrawdatatask;
    WellAlgorithmTask zeromertask;
    WellAlgorithmTask nntask;
    private DataTransPanel transPanel;    
    private WellFlowDataResult[] results;
    private int[] flownr;
    private boolean[] taskdone;

    public TorrentScoutAcqViewTopComponent() {
        initComponents();

        setName(NbBundle.getMessage(TorrentScoutAcqViewTopComponent.class, "CTL_TorrentScoutAcqViewTopComponent"));
        setToolTipText(NbBundle.getMessage(TorrentScoutAcqViewTopComponent.class, "HINT_TorrentScoutAcqViewTopComponent"));


        initPanNorth();
        initMainPanel();
        add("Center", centerPanel);
        charttab = new JTabbedPane();
        centerPanel.add("Center", charttab);
        charttab.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                curtab = charttab.getSelectedIndex();
            }
            
        });
        loadPreferences();
        this.setOpaque(false);
    }

    private void initPanNorth() {
        panNorth = new JPanel();
        panNorth.setLayout(new BorderLayout());
        panNorth.setOpaque(false);
        typePanel = new FiletypePanel(this);
        typePanel.setType(RawType.ACQ);
        typePanel.setOpaque(false);


        curvePanel = new CurveSelectionPanel(this);
        transPanel = new DataTransPanel();

        JToolBar bar = new JToolBar();
        bar.setOpaque(false);
        panNorth.add("North", bar);

        final JButton btnTrans = new javax.swing.JButton();
        btnTrans.setIcon(new ImageIcon(this.getClass().getResource("configure-3.png")));
        btnTrans.setToolTipText("Configure data transformations");

        btnTrans.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(btnTrans, transPanel, "Select the parameters for the data transformations", JOptionPane.QUESTION_MESSAGE);

            }
        });
        final JButton btnCurve = new javax.swing.JButton();
        //btnCurve.setIcon(new ImageIcon(this.getClass().getResource("filter.png")));
        btnCurve.setIcon(new ImageIcon(this.getClass().getResource("chart-line-edit.png")));
        btnCurve.setToolTipText("Select data to view");


        final JButton btnBulb = new javax.swing.JButton();
        //btnCurve.setIcon(new ImageIcon(this.getClass().getResource("filter.png")));
        btnBulb.setIcon(new ImageIcon(this.getClass().getResource("help-hint.png")));
        btnBulb.setToolTipText("Give me a hint...");
        btnBulb.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doHintAction();
            }
        });

        bar.add(btnCurve);
        bar.add(btnTrans);
        btnCurve.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(btnCurve, curvePanel, "Select the type of data to view", JOptionPane.QUESTION_MESSAGE);
            }
        });
        JButton btnExport = new javax.swing.JButton();
        btnExport.setIcon(new ImageIcon(this.getClass().getResource("document-export.png")));
        btnExport.setToolTipText("Export data to Excel");
        bar.add(btnExport);

        btnExport.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                toCsv();
            }
        });

        final JButton btnCoord = new javax.swing.JButton();
        btnCoord.setIcon(new ImageIcon(this.getClass().getResource("select-rectangular.png")));
        btnCoord.setToolTipText("Enter a well coordinate (absolute)");

        btnCoord.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectCoordinate();
            }
        });
        bar.add(btnCoord);

        final JButton btnRefresh = new javax.swing.JButton();
        btnRefresh.setIcon(new ImageIcon(this.getClass().getResource("refresh.png")));
        btnRefresh.setToolTipText("Configure data transformations");

        btnRefresh.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //  GuiUtils.showNonModalMsg("Refreshing view...", 1);
                doRefreshAction();

            }
        });
        bar.add(btnRefresh);
        bar.add(btnBulb);
        algPanel = new AlgParamPanel(this);
        JButton btnAlg = new JButton("Zeromer...");
        btnAlg.setToolTipText("Configure parameters for zeromer calculation");
        btnAlg.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(centerPanel, algPanel, "Select the parameters", JOptionPane.QUESTION_MESSAGE);

                //  update();
            }
        });

        //  bar.add(btnAlg);
        add("North", panNorth);

        flowPanel = new FlowNrPanel(this);
        flowPanel.setText("0");
        //flows = flowPanel.getFlows();


        subPanel = new SubtractPanel(new FlowListener() {

            @Override
            public void flowChanged(ArrayList<Integer> flows) {
                subtract = subPanel.getFlow();
                //acqPanel.clear();
                doRefreshAction();
            }
        });
        subPanel.setText("");
        //flows = flowPanel.getFlows();
        bar.add(this.flowPanel);

        bar.add(this.typePanel);
        bar.add(this.subPanel);
    }

    private void selectCoordinate() {
        SingleCoordSelectionPanel pan = new SingleCoordSelectionPanel();

        if (expContext ==null) expContext = GlobalContext.getContext().getExperimentContext();
        if (expContext.getRowOffset() > 0 || expContext.getColOffset() > 0) {
            pan.setCoord1(new WellCoordinate(expContext.getColOffset(), expContext.getRowOffset()));
        }
        // pan.setMaxX(this.expContext.getNrCols());
        //pan.setMaxY(this.expContext.getNrRows());
        int ans = JOptionPane.showConfirmDialog(this, pan, "Enter a selection:", JOptionPane.OK_CANCEL_OPTION);
        if (ans == JOptionPane.CANCEL_OPTION) {
            return;
        }
        WellCoordinate c1 = pan.getCoord1();
        // subtract offsets!
        int x = c1.getX() - this.expContext.getColOffset();
        int y = c1.getY() - this.expContext.getRowOffset();
        if (x < 0 || y < 0) {
            JOptionPane.showMessageDialog(this, "Note that coordinates are absolute. It looks like you entered relative coords");
        } else {
            c1 = new WellCoordinate(x, y);
        }
        publishCoord(c1);
        // update();
    }

    protected void publishCoord(WellCoordinate coord) {
        if (coord != null) {
            if (expContext == null) {
                this.getLatestExperimentContext();
            }
            if (expContext != null) {
                if (cur_context == null) {
                    cur_context = expContext.createWellContext();
                }
            }
            if (expContext == null || cur_context == null) {
                // this.setStatusWarning("Got no experiment context yet");
                return;
            }
            cur_context.setCoordinate(coord);
            p("Got a coordinate: " + coord);
            LookupUtils.publish(wellCoordContent, coord);
        }
    }

    public void doRefreshAction() {

        if (acqPanel != null) {
            acqPanel.clear();
        }
        if (cmulti != null) {
            cmulti.clear();
        }
        if (cur_context != null) {
            DataAccessManager manager = DataAccessManager.getManager(cur_context);
            manager.clear();
        }
        update();
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        loadPreferences();

    }

    private void addMsg(String txt) {
        lblMsg = new JLabel("<html>" + txt + "</html>");
        centerPanel.add("North", lblMsg);
        centerPanel.repaint();
        this.invalidate();
        this.revalidate();
        centerPanel.paintImmediately(0, 0, 800, 500);
        this.paintImmediately(0, 0, 800, 500);
    }

    private void addBtn(String txt) {
        if (msgpanel != null) {
            centerPanel.remove(msgpanel);
        }
        msgpanel = new JPanel();
        msgpanel.setLayout(new BorderLayout());
        lblMsg = new JLabel("<html><h3>" + txt + "</h3></html>");

        msgpanel.add("North", lblMsg);
        final JButton btn = new JButton("Read raw .dat file");
        btn.setToolTipText("<html>" + txt + "</html>");
        btn.setForeground(new Color(0, 153, 51));
        btn.setFont(new Font("Tahoma", Font.BOLD, 14));
        btn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (loadrawdatatask != null) {
                    addCancelBtn(loadrawdatatask.getFlow());
                } else {
                    for (int flow : curflows) {
                        startDataLoadThread(flow);
                        addCancelBtn(flow);
                    }
                }
            }
        });
        msgpanel.add("Center", new FlowPanel(btn));
        centerPanel.add("North", msgpanel);
        centerPanel.repaint();
        this.invalidate();
        this.revalidate();
        msgpanel.paintImmediately(0, 0, 800, 800);
        centerPanel.paintImmediately(0, 0, 800, 800);
        this.paintImmediately(0, 0, 800, 800);
    }

    private void addCancelBtn(final int flow) {
        if (msgpanel != null) {
            centerPanel.remove(msgpanel);
        }
        msgpanel = new JPanel();
        msgpanel.setLayout(new BorderLayout());
        String txt = "Cancel reading/caching of raw .dat file for flow " + flow;
        lblMsg = new JLabel("<html><h3><font color='aa0000'>" + txt + "</font></h></html>");

        msgpanel.add("North", lblMsg);
        final JButton btn = new JButton("Cancel reading raw .dat file");
        btn.setToolTipText(txt);
        btn.setForeground(new Color(0, 153, 51));
        btn.setFont(new Font("Tahoma", Font.BOLD, 14));
        btn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (loadrawdatatask != null) {
                    GuiUtils.showNonModalMsg("Trying to cancel caching task... (may take a few seconds...)");
                    loadrawdatatask.cancel(true);
                    DataAccessManager.setIsConverting(false);
                    if (loadrawdatatask != null && !loadrawdatatask.isCancelled()) {
                        GuiUtils.showNonModalMsg("Failed to cancel task for flow " + flow + "...");
                    } else {
                        loadrawdatatask = null;
                        DataAccessManager.setIsConverting(false);
                        addBtn("Caching cancelled for flow " + flow + ". Restart reading/caching raw data file for " + curflows);
                    }

                } else {
                    GuiUtils.showNonModalMsg("I see no data caching task...");
                    addBtn("Caching cancelled. Restart reading/caching raw data file for current selected flow");
                }
            }
        });
        msgpanel.add("Center", new FlowPanel(btn));
        centerPanel.add("North", msgpanel);
        centerPanel.repaint();
        this.invalidate();
        this.revalidate();
        msgpanel.paintImmediately(0, 0, 800, 800);
        centerPanel.paintImmediately(0, 0, 800, 800);
        this.paintImmediately(0, 0, 800, 800);
    }

    private void computeFlowBg(int flow) throws HeadlessException {
        SequenceLoader loader = SequenceLoader.getSequenceLoader(this.expContext);
        WellCoordinate coord = this.cur_context.getCoordinate();
        Read read = loader.getRead(coord.getCol(), coord.getRow(), this);

        if (read == null) {
            GuiUtils.showNonModalMsg("Found no read at " + coord);
        } else {
            ArrayList<Integer> empty = read.getEmptyFlows();
            FlowBgAlg alg = new FlowBgAlg(filter, empty, curvePanel.getBoxAllWells().isSelected());
            if (curvePanel.getBoxAllWells().isSelected()) {
                GuiUtils.showNonModalMsg("Flow bg: Only using this ONE well at " + filter.getCoord(), false, 5);
            }
            ArrayList<WellFlowDataResult> res = alg.compute();
            int nrempty = alg.getNrEmpty();
            if (nrempty < 10) {
                GuiUtils.showNonModalMsg("Computed bg with " + nrempty + " empty cached flows."
                        + "Use the plugin to cache more flows so that I can use more data for the bg computation", true, 10);
            } else {
                GuiUtils.showNonModalMsg("Computed bg with " + nrempty + " empty cached flows");
            }
            addFinalResultToChart(res, flow);
        }

    }

    private void updateMultiChart() {
        // create one huge dataset for all flows, show it
        long starttime = 0;
        ArrayList<WellFlowDataResult> chartres = new ArrayList<WellFlowDataResult>();
        for (int i = 0; i < flownr.length; i++) {
            WellFlowDataResult nndata = results[i];

            int flow = flownr[i];
            if (nndata != null) {
                p("updateMultiChart: starttime for flow " + flow + "=" + starttime);
                nndata.setStarttime(starttime);
                starttime += nndata.getLastTimeStamp();

                chartres.add(nndata);
            }
            else p("updateMultiChart: Got no result for flow "+flow+" and i "+i);


        }
        cmulti.setResults(chartres);
        cmulti.update("NN subtracted traces at " + (this.expContext.getWellContext().getAbsoluteCoordinate()),
                expContext);

        cmulti.repaint();
        
    }

    private void computeNN(WellCoordinate a, WellCoordinate b, WellCoordinate c, WellCoordinate d, int flow) throws HeadlessException {
        // not in thread      
        // cache data again first for each coord!
        if (isCachedOrElseAddButton(a, flow) && isCachedOrElseAddButton(b, flow) && isCachedOrElseAddButton(c, flow) && isCachedOrElseAddButton(d, flow)) {
            p("NN: good, all Data is cached for flow " + flow);
            NearestNeighbor alg = new NearestNeighbor(filter, span, curvePanel.getBoxAllWells().isSelected());
            int nr = 0;
            if (cur_context.getSelection() != null) {
                nr = cur_context.getSelection().getAreaSize();
            }
            if (curvePanel.getBoxAllWells().isSelected() && nr > 200) {
                boolean doit = true;
                if (nr > 2000) {
                    int ok = JOptionPane.showConfirmDialog(this, "<html>Your selection contains " + nr + " wells. The NN algorithm could take quite some time.<br>"
                            + "I would recommend to make a smaller selection :-).<br>"
                            + "Do you still want to continue?</html>", nr + " wells", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (ok == JOptionPane.NO_OPTION) {
                        doit = false;
                    }
                }
                if (doit) {
                    GuiUtils.showNonModalMsg("Computing NN for all empty wells in area with " + nr + " wells");
                    nntask = new WellAlgorithmTask((TaskListener) this, ProgressHandleFactory.createHandle("Computing nearest neighbor for " + nr + " wells"), (WellAlgorithm) alg);
                    // nntask.
                    nntask.execute();
                }
            } else {
                ArrayList<WellFlowDataResult> res = alg.compute();
                p("adding nn result for flow " + flow + ":" + res);
                addFinalResultToChart(res, flow);
            }
        } else {
            GuiUtils.showNonModalMsg("Need to wait with NN, need to cache more data...");
        }
    }

    private void computeZeromer(WellCoordinate a, WellCoordinate b, WellCoordinate c, WellCoordinate d, int flow) throws HeadlessException {
        if (isCachedOrElseAddButton(a, flow) && isCachedOrElseAddButton(b, flow) && isCachedOrElseAddButton(c, flow) && isCachedOrElseAddButton(d, flow)) {
            p("ZERO: All Data is cached");
            float tauBulk = algPanel.getTauBulk();
            float tauEmpty = algPanel.getTauEmpty();
            int frameStart = algPanel.getFrameStart();
            int frameEnd = algPanel.getFrameEnd();

            // GuiUtils.showNonModelMsg("Computing Zeromer trace with "+tauBulk+"/"+tauEmpty+", "+frameStart+"-"+frameEnd);
            ZeromerTrace alg = new ZeromerTrace(filter, span, tauBulk, tauEmpty, frameStart, frameEnd, curvePanel.getBoxAllWells().isSelected());
            int nr = 0;
            if (cur_context.getSelection() != null) {
                nr = cur_context.getSelection().getAreaSize();
            }
            if (curvePanel.getBoxAllWells().isSelected() && nr > 200) {
                boolean doit = true;
                if (nr > 2000) {
                    int ok = JOptionPane.showConfirmDialog(this, "<html>Your selection contains " + nr + " wells. The Zeromer algorithm could take quite some time.<br>"
                            + "I would recommend to make a smaller selection :-).<br>"
                            + "Do you still want to continue?</html>", nr + " wells", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (ok == JOptionPane.NO_OPTION) {
                        doit = false;
                    }
                }
                if (doit) {
                    GuiUtils.showNonModalMsg("Computing zeromer trace for entire area with " + nr + " wells");
                    zeromertask = new WellAlgorithmTask(this, ProgressHandleFactory.createHandle("Computing zeromer for " + nr + " wells"), alg);
                    zeromertask.execute();
                }
            } else {
                ArrayList<WellFlowDataResult> res = alg.compute();
                addFinalResultToChart(res, flow);
            }

        } else {
            GuiUtils.showNonModalMsg("Need to wait with zeromer calculation, need to cache more data...");
        }
    }

    private void addFinalResultToChart(ArrayList<WellFlowDataResult> res, int flow) {
        if (res != null) acqPanel.addResults(res, flow);
        // only add CERTAIN results to the MULTI FLOW chart... just the bg subtracted
        WellFlowDataResult toadd = null;
        if (res != null) {
            for (WellFlowDataResult re: res) {
                p("Got result of type: "+re.getResultType());
                if (re.getResultType().equals(ResultType.NN_RAW_BG)) {
                    toadd = re;
                    p("Found a result to add to multiflowchart: "+re.getResultType());
                }
            }
        }
        if (res != null && toadd == null) {
            p("Nothing for multiflowchart");
            return;
        }
        int index = -1;
        for (int i = 0; i < flownr.length; i++) {
            if (flownr[i] == flow) {
                index = i;
                break;
            }
        }
        results[index] = toadd;
        taskdone[index] = true;
        boolean alldone = true;
        for (int i = 0; i < curflows.size(); i++) {
            if (!taskdone[i]) {
                alldone = false;
            }
        }
        if (alldone) {
            p("ALL tasks are done, will now create multichart in correct flow order: " + flownr);            
            updateMultiChart();
        } else {
            p("Not all done yet: " + Arrays.toString(taskdone));
        }
    }

    private void loadPreferences() {
        Preferences p = Preferences.userNodeForPackage(RawOptionsPanel.class);
        try {
            p.removePreferenceChangeListener(this);

        } catch (Exception e) {
        }

        String bg_wells = p.get("background_wells", "empty_wells");
        if (bg_wells != null && bg_wells.equalsIgnoreCase("empty_wells")) {
            haveflags = new BfMaskFlag[]{BfMaskFlag.EMPTY};
            nothaveflags = null;
        } else {
            haveflags = new BfMaskFlag[]{BfMaskFlag.BEAD};
            nothaveflags = new BfMaskFlag[]{BfMaskFlag.EMPTY, BfMaskFlag.LIVE};
        }

        span = p.getInt("nn_span", 5);


        p.addPreferenceChangeListener(this);
    }

    private void initMainPanel() {
        centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        filetype = RawType.ACQ;


        acqPanel = new MultiAcqPanel(filetype);
        // lblMin = new JLabel("Stats: ");
        centerPanel.setLayout(new BorderLayout());

        curvePanel.getBoxAllWells().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (acqPanel != null) {
                    acqPanel.clear();
                }
                update();
            }
        });
        // pn.add("East", btnAlg);

        // centerPanel.add("South", lblMin);
    }

    @Override
    public void parameterChanged(Object source, String name, double value) {
        //update();
    }

    private void repaintPanel() {
        updateInfo();
        //scroll = new JScrollPane(acqPanel);
        centerPanel.setToolTipText("<html>" + acqPanel.getInfo() + "</html>");
        //   p("got info: " + acqPanel.getInfo());
        acqPanel.setToolTipText(centerPanel.getToolTipText());
        //    txtInfo.setText("<html>"+acqPanel.getInfo()+"</html>");
        centerPanel.repaint();
        invalidate();
        revalidate();
        // requestActive();
        //   this.requestVisible();
        centerPanel.paintImmediately(0, 0, 1000, 1000);
    }

    private void startDataLoadThread(int flow) {
        DataAccessManager manager = DataAccessManager.getManager(cur_context);
        loadrawdatatask = new LoadRawDataTask(this, manager, flow);
        int secs = cur_context.esimateSecs();
        GuiUtils.showNonModelMsg("Need to read raw a .dat file", "<html>Need to read/cache raw .dat file for flow " + flow + ", ca " + secs + " secs"
                + "<br><font color='bb0000'>(if possible, use the <b>plugin</b>)</font> ... </html>", true, secs + 10);
        loadrawdatatask.execute();
    }

    private void toCsv() {
        if (acqPanel == null) {
            return;
        }
        String cvs = acqPanel.toCSV();
        JTextArea pane = new JTextArea(50, 40);
        // pane.setContentType("text");
        pane.setText(cvs);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(cvs), null);
        JOptionPane.showMessageDialog(this, new JScrollPane(pane), "You can copy this to Excel", JOptionPane.INFORMATION_MESSAGE);
        return;
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
            p("Got a selection: " + selection + ", coord: " + selection.getCoord1());
            if (cur_context == null) {
                getLatestContext();
            }
            if (cur_context == null) {
                return;
            }
            cur_context.setSelection(selection);
            this.getLatestCoordinate();
            if (acqPanel != null) {
                this.acqPanel.clear();
            }
            //   p("SubscriberListener Got WellSelection:" + cur_context);
            if (acqPanel != null) {
                acqPanel.clear();
            }
            this.update();
        }

    }

    private class ExSubscriberListener implements LookupListener {

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

            if (acqPanel != null) {
                acqPanel.clear();
            }
            this.update();
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
            super("Open additional view");

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            TopComponent win = new TorrentScoutAcqViewTopComponent();
            win.open();
            win.requestActive();
        }
    }

    private void updateInfo() {
        Stats stat = acqPanel.getStats();
        String s = "";
        if (stat != null) {
            s += "Raw: min=" + stat.getMin() + ", max=" + stat.getMax() + ", mean=" + (int) stat.getMean() + ", var=" + (int) stat.getVar();
        }
        stat = acqPanel.getNNStats();
        if (stat != null) {
            s = "NN: min=" + stat.getMin() + ", max=" + stat.getMax() + ", mean=" + (int) stat.getMean() + ", var=" + (int) stat.getVar();
            s += "<br>Nr bg wells for nn: " + acqPanel.getNrEmpty() + ", flags: ";

            if (haveflags != null) {
                for (BfMaskFlag flag : haveflags) {
                    s += flag.getName() + ", ";
                }
            }
            if (nothaveflags != null) {
                for (BfMaskFlag flag : nothaveflags) {
                    s += "not " + flag.getName() + ", ";
                }
            }
            if (s.endsWith(", ")) {
                s = s.substring(0, s.length() - 2);
            }
        }
        stat = acqPanel.getDStats();
        if (stat != null) {
            s += "<br>Signal: min=" + stat.getMin() + ", max=" + stat.getMax() + ", mean=" + (int) stat.getMean() + ", var=" + (int) stat.getVar();
        }


        // s += "<br>" + acqPanel.getInfo();
        // lblMin.setText("<html>" + s + "</html>");

    }

    @Override
    public void flowChanged(ArrayList<Integer> flows) {
        curflows = flows;
        this.doRefreshAction();
    }

    @Override
    public void fileTypeChanged(RawType filetype) {
        if (filetype == this.filetype) {
            return;
        }
        this.filetype = filetype;
        //flowPanel.setFlow(0);

        flowPanel.setText("0-4");
        if (this.expContext != null) {
            expContext.setFileType(filetype);
        }
        this.doRefreshAction();

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
        final Collection<? extends WellContext> contexts = dataClassWellSelection.allInstances();
        if (!contexts.isEmpty()) {
            Iterator<WellContext> cit = (Iterator<WellContext>) contexts.iterator();
            while (cit.hasNext()) {
                cur_context = cit.next();
                //       p("Got context: " + cur_context);
            }
            //   p("SubscriberListener Got WellContext:" + cur_context + ", out of " + contexts.size());
            // lbl_coords.setText(cur_selection.toString());
            WellCoordinate coord = cur_context.getCoordinate();
            if (coord == cur_context.getCoordinate()) {
                //    p("Same coordinate, returning");
                return true;
            } else {
                cur_context.setCoordinate(coord);
            }
            if (acqPanel != null) {
                acqPanel.clear();
            }
            update();
        }
        return false;
    }

    private class WellCoordinateSubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            if (getLatestCoordinate()) {
                return;
            }
        }
    }

    private boolean getLatestCoordinate() {
        final Collection<? extends WellCoordinate> selections = dataClassWellCoordinate.allInstances();
        if (!selections.isEmpty()) {
            WellCoordinate coord = null;
            Iterator<WellCoordinate> cit = (Iterator<WellCoordinate>) selections.iterator();
            while (cit.hasNext()) {
                coord = cit.next();
                //        p("Got coord: " + coord);
            }
            // p("SubscriberListener Got WellCoordinate:" + coord + ", out of " + selections.size());
            // lbl_coords.setText(cur_selection.toString());
            if (cur_context == null) {
                this.getLatestExperimentContext();
                if (this.expContext != null) {
                    cur_context = expContext.getWellContext();
                }

            }
            if (cur_context != null) {
                cur_context.setCoordinate(coord);
            }
            if (acqPanel != null) {
                acqPanel.clear();
            }
            update();
        }
        return false;
    }

    public void doHintAction() {
        String msg = "<html>You can do the following things here:<ul>";
        msg += "<li>Enter one or more flows (sepearated by comma), such as: 0,1,2,3<br>(You can also enter a range like 0-4)</li>";
        msg += "<li>Select the raw type to view in the drop down box</li>";
        msg += "<li>Select if you wish to see the NN subtracted data or raw data in the first options panel (button with lines and pencil) </li>";
        msg += "<li>Select if you wish to apply data transformations (channel x-talk correction for instance) in the second panel - using the button with the tools icon</li>";
        msg += "<li>You can also directly enter coordinates here using the icon with the rectangle</li>";
        msg += "<li>If you wish to subtract data from some flow (such as a flow where you know nothing was incorporated)<br>"
                + "(you can enter the flow nr in the subtract flow text field)</li>";


        msg += "</ul></html>";
        JOptionPane.showMessageDialog(this, msg);

    }

    protected void update() {
        update(true);
    }

    private void update(boolean mayStartThread) {
        //  p("Updating acquisition panel");
       
        if (lblMsg != null) {
            centerPanel.remove(lblMsg);
        }
        if (msgpanel != null) {
            centerPanel.remove(msgpanel);
        }

        String msg = "";

        GlobalContext global = GlobalContext.getContext();
        this.expContext = global.getExperimentContext();
        if (global == null) {
            addMsg("I have no global context or any data yet");
            return;
        }


        if (cur_context == null) {
            addMsg("Select a region and well first (in the well table or one of the heat maps)");
            return;
        } else if (cur_context.getCoordinate() == null && cur_context.getSelection() == null) {
            addMsg("Got no selection or coordinate yet - select a well in a heat map or table");
            return;
        } else if (cur_context.getCoordinate() == null) {
            cur_context.setCoordinate(cur_context.getSelection().getCoord1());
        } else if (!cur_context.getCoordinate().isValid()) {
            addMsg("The coordinate " + cur_context.getCoordinate() + " is not valid");
            return;
        }
        String msg1 = global.verifyCacheDir();
        msg += msg1;
        if (msg.length() > 0) {
            addMsg(msg);
            if (msg1.length() > 0) {
                OptionsDisplayer.getDefault().open("TorrentScoutOptions/TorrentScoutSettings");
            }
            return;
        }
        // check if enouch cached data
        curflows = flowPanel.getFlows();

        //this.expContext.setFileType(filetype);
        this.expContext.setFlow(curflows.get(0));

        if (cur_context.getWellData(cur_context.getCoordinate()) == null) {
            p("Got no well data yet - maybe 1.wells file is still being transferred to cache?");

        } else {
            // nrflows = cur_context.getWellData(cur_context.getCoordinate()).getNrFlows();
        }

        if (acqPanel != null) charttab.remove(acqPanel);
        else {
            acqPanel = new MultiAcqPanel(filetype);
        }
        p("SETTING SUBTRACT TO :" + subtract);
        acqPanel.setSubtract(subtract);
        
        int nr = cur_context.getNrWells();
        DataAccessManager manager = DataAccessManager.getManager(cur_context);

        if (subtract > -1 && !curflows.contains(subtract)) {
            curflows.add(subtract);
        }


        results = new WellFlowDataResult[curflows.size()];
        flownr = new int[curflows.size()];
        taskdone = new boolean[curflows.size()];
        if (cmulti != null) charttab.remove(cmulti);
        cmulti = new MultiFlowPanel(filetype);   
        cmulti.setSubtract(subtract);
        cmulti.setYaxis("Raw count");
        
        charttab.add("Single flows", acqPanel);        
        charttab.add("Multi flow", cmulti);        
        if (curtab > 0) charttab.setSelectedIndex(curtab);
        for (int i = 0; i < curflows.size(); i++) {
            int flow = curflows.get(i);
            flownr[i] = flow;

            filter = new WellContextFilter(cur_context, haveflags, nothaveflags, filetype, flow, cur_context.getCoordinate());
            WellFlowData data = null;
            if (isCachedOrElseAddButton(cur_context.getCoordinate(), flow)) {
                //    p("Data is cached");
                if (curvePanel.getBoxAllWells().isSelected() && nr > 100) {
                    GuiUtils.showNonModalMsg("Computing average raw signal for all " + nr + " wells");
                    LoadRawDataTask task = new LoadRawDataTask(this, manager, flow);
                    task.execute();
                } else {
                    data = manager.getFlowData(filter, curvePanel.getBoxAllWells().isSelected());
                    if (data == null) {
                        if (manager.getException() != null) {
                            err("Got an exception: " + manager.getException());
                        } else {
                            p("Got no data");
                        }

                    }

                    afterLoadedRawData(data, flow);
                }
            }
        }
    }

    public boolean isCachedOrElseAddButton(WellCoordinate coord, int flow) {
        DataAccessManager manager = DataAccessManager.getManager(cur_context);
//        if (!manager.isCached(cur_context, coord, flow, filetype)) {
//            if (loadrawdatatask != null) {
//                this.setStatus("I am already reading a raw .data file for flow " + loadrawdatatask.getFlow() + "...");
//                addCancelBtn(loadrawdatatask.getFlow());
//                return false;
//            } else {
//                int secs = cur_context.esimateSecs();
//                this.addBtn("Need to read/cache raw .dat file for flow " + flow + " (ca " + secs + " secs)<br>"
//                        + "<font color='bb0000'>Please use the TorrentScout plugin to do that</font>");
//                return false;
//            }
//        } else {
//            //   p("Data is cached for "+coord+":"+curflow);
//            return true;
//        }
        return true;
    }

    private class LoadRawDataTask extends Task {

        WellFlowData data;
        DataAccessManager manager;
        int flow;

        public LoadRawDataTask(TaskListener tlistener, DataAccessManager manager, int flow) {
            super(tlistener, ProgressHandleFactory.createHandle("Reading entire acqusition file for flow " + flow));
            this.flow = flow;
            this.manager = manager;
        }

        public int getFlow() {
            return flow;
        }

        public WellFlowData getData() {
            return data;
        }

        @Override
        public Void doInBackground() {
            data = manager.getFlowData(filter, curvePanel.getBoxAllWells().isSelected());

            return null;
        }

        public boolean isSuccess() {
            return data != null;
        }
    }

    private void afterLoadedRawData(WellFlowData data, int flow) {
        p("========= afterLoadedRawData, flow " + flow + "=====================");
        if (lblMsg != null) {
            centerPanel.remove(lblMsg);
        }
        if (msgpanel != null) {
            centerPanel.remove(msgpanel);
        }
        DataAccessManager manager = DataAccessManager.getManager(cur_context);

        String msg = null;
        if (data == null) {
            if (manager.getException() != null) {
                msg = "Got an error when loading data:" + manager.getException().getMessage();
            } else if (manager.getErrorMsg() != null) {
                msg = manager.getErrorMsg();
            }
            if (msg == null) {
                msg = "Was not able to load the data at coord " + cur_context.getCoordinate() + ", no data found";
            }
            if (msg != null) {
                this.addMsg(msg);
                return;
            }
        } else {
            //p("Got well data for coord " + cur_context.getCoordinate() + ":" + data);
        }

        nnresult = null;
        int nrempty = 0;
        // now cmpute nn 
        // TODO: user input
        span = Math.max(span, 4);
        if (haveflags == null) {
            // haveflags = new BfMaskFlag[]{BfMaskFlag.BEAD};
            // nothaveflags = new BfMaskFlag[]{BfMaskFlag.EMPTY, BfMaskFlag.LIVE};
            haveflags = new BfMaskFlag[]{BfMaskFlag.EMPTY};
            nothaveflags = new BfMaskFlag[]{BfMaskFlag.PINNED, BfMaskFlag.EXCLUDE};
        }
        filter = new WellContextFilter(cur_context, haveflags, nothaveflags, filetype, flow, cur_context.getCoordinate());

        boolean nn = false;
        boolean zero = false;
        boolean flowbg = false;
        boolean useCache = true;
        EnumMap<ResultType, WellFlowDataResult> map = null;
        if (acqPanel != null) {
            map = acqPanel.getResultsMap(flow);
        }

        if (acqPanel.getCoord() == cur_context.getCoordinate()) {
            useCache = true;
        } else {
            if (map != null) {
                map.clear();
            }
        }

        for (ResultBox box : curvePanel.getBoxResults()) {
            if (box.isSelected()) {
                if (box.type == ResultType.MEDIAN) {
                    if (!useCache || map == null || !map.containsKey(box.type)) {
                        nn = true;
                    }
                } else if (box.type == ResultType.NN_RAW_BG) {
                    if (!useCache || map == null || !map.containsKey(box.type)) {
                        nn = true;
                        if (nntask != null) {
                            nn = false;
                            GuiUtils.showNonModalMsg("Already computing nn, won't start another thread");
                        }
                    }
                } else if (box.type == ResultType.EMPTYFLOWS || box.type == ResultType.RAW_BGFLOW) {
                    if (!useCache || map == null || !map.containsKey(box.type)) {
                        flowbg = true;
                    }

                } else {
                    if (!useCache || map == null || !map.containsKey(box.type)) {
                        zero = true;
                        if (zeromertask != null) {
                            zero = false;
                            GuiUtils.showNonModalMsg("Already computing zeromer, won't start another thread");
                        }
                    }
                }
            }
        }
        WellCoordinate m = cur_context.getCoordinate();
        WellCoordinate a = new WellCoordinate(m.getCol() - span, m.getRow() - span);
        WellCoordinate b = new WellCoordinate(m.getCol() + span, m.getRow() - span);
        WellCoordinate c = new WellCoordinate(m.getCol() + span, m.getRow() + span);
        WellCoordinate d = new WellCoordinate(m.getCol() - span, m.getRow() + span);

        if (nn) {
            computeNN(a, b, c, d, flow);
        }
        if (zero) {
            computeZeromer(a, b, c, d, flow);
        }
        if (flowbg) {
            computeFlowBg(flow);
        }


        String region = "" + cur_context.getAbsoluteCoordinate();

        if (curvePanel.getBoxAllWells().isSelected()) {
            region = cur_context.getSelection().getAllWells().size() + " wells";
        }
        WellFlowDataResult raw = new WellFlowDataResult(m.getCol(), m.getRow(), flow, this.filetype, m.getMaskdata());
        raw.setResultType(ResultType.RAW);
        raw.setData(data.getData());
        raw.setTimestamps(data.getTimestamps());
        ResultType.RAW.setShow(curvePanel.getBoxRaw().isSelected());


        msg = acqPanel.update(region, expContext, curflows);

        if (msg != null) {
            lblMsg = new JLabel("<html>" + msg + "</html>");
            centerPanel.add("Center", lblMsg);
            this.paintImmediately(0, 0, 500, 500);
        } else if (raw != null) {
            msg = acqPanel.update(raw, nrempty);
        }

        repaintPanel();
    }

    @Override
    public void taskDone(Task t) {
        p("Task " + t + " is done");
        setCursor(null);
        if (progress != null) {
            progress.progress(100);
            progress.finish();
        }

        if (t instanceof LoadRawDataTask) {

            this.setStatus("Raw data loading done");
            afterLoadedRawData(loadrawdatatask.getData(), loadrawdatatask.getFlow());
            if (loadrawdatatask.getData() == null) {
                GuiUtils.showNonModalMsg("Could not load flow "+loadrawdatatask.getFlow());
                addFinalResultToChart(null, loadrawdatatask.getFlow());
            }
            loadrawdatatask = null;
        } else if (t instanceof WellAlgorithmTask) {
            WellAlgorithmTask task = (WellAlgorithmTask) t;

            ArrayList<WellFlowDataResult> res = task.getResults();
            if (task == zeromertask) {
                zeromertask = null;
            } else if (task == nntask) {
                nntask = null;
            }
            if (res == null) {
                err("Got no result from " + task.getAlgorithm());
                
                return;
            } else {
                p("Got result for " + task.getAlgorithm().getName());
            }
            if (acqPanel != null && acqPanel.getCoord() == task.getCoord()) {
                addFinalResultToChart(res, task.getFlow());
                p("AFTER WELL ALG TASK: Adding results " + res);
                GuiUtils.showNonModalMsg("Added result of " + task.getAlgorithm().getName() + " to panel");
                //  p("repaingint panel");
                String region = "" + cur_context.getCoordinate();
                if (curvePanel.getBoxAllWells().isSelected()) {
                    region = cur_context.getSelection().getAllWells().size() + " wells";
                }

                String msg = acqPanel.update(region, expContext, curflows);
                if (msg != null) {
                    lblMsg = new JLabel("<html>" + msg + "</html>");
                    centerPanel.add("Center", lblMsg);
                    this.paintImmediately(0, 0, 500, 500);
                } else {

                    acqPanel.update(nnresult, task.getNrEmpty());
                }
                repaintPanel();

            } else {
                err("Data has changed in the meantime, different flow or coord");
            }
        } else {
            // probably data loading task
            ConverterTask task = (ConverterTask) t;
            TorrentScoutStartupTopComponent tc = (TorrentScoutStartupTopComponent) WindowManager.getDefault().findTopComponent("TorrentScoutStartupTopComponent");
            if (tc.tasks != null) {
                tc.tasks.remove(t);
            }
            //progress.stop();
            Conversion conv = task.getConversion();
            String msg = task.getMsg();
            if (task.getThrowable() != null) {
                JOptionPane.showMessageDialog(this, "Acquisition View:\nGot an exception from the conversion task:\n" + task.getThrowable() + "\nmsg: " + msg);
            } else if (msg != null && msg.trim().length() > 0) {
                JOptionPane.showMessageDialog(this, "<html>Acquisition View:<br>Msg from conversion task: " + msg + "</html>");
            } else {
                if (conv.getType() == this.filetype) {
                    update(false);
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        if (filetype == null) {
            filetype = RawType.ACQ;
            typePanel.setType(filetype);
        }
        this.getLatestContext();
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

    private void err(String msg) {
        Logger.getLogger(TorrentScoutAcqViewTopComponent.class.getName()).log(Level.SEVERE, msg);
    }

    private void p(String s) {
        System.out.println("TorrentScoutAcqViewTopComponent:" + s);
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
}
