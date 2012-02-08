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
package com.iontorrent.scout.experimentviewer;

import com.iontorrent.expmodel.ExperimentLoader;
import com.iontorrent.dataloading.CreateIndexTask;
import com.iontorrent.dbaccess.RundbExperiment;
import com.iontorrent.dbaccess.RundbReportstorage;
import com.iontorrent.expmodel.CompositeExperiment;
import com.iontorrent.guiutils.netbeans.OpenWindowAction;
import com.iontorrent.main.options.SiteList;
import com.iontorrent.scout.experimentviewer.exptree.ExpFilterPanel;
import com.iontorrent.scout.experimentviewer.exptree.ExpNode;
import com.iontorrent.scout.experimentviewer.exptree.MyRig;
import com.iontorrent.scout.experimentviewer.exptree.RootNode;
import com.iontorrent.scout.experimentviewer.exptree.RigChildFactory;
import com.iontorrent.scout.experimentviewer.exptree.MyResult;
import com.iontorrent.scout.experimentviewer.exptree.NodeFilter;
import com.iontorrent.scout.experimentviewer.exptree.ResultNode;

import com.iontorrent.scout.experimentviewer.options.PersistenceHelper;
import com.iontorrent.scout.fromrest.ExperimentRestClientAdapter;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.FolderManager;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.expmodel.LoadDataContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.main.FolderAction;
import com.iontorrent.scout.experimentviewer.exptree.RigNode;
import com.iontorrent.scout.offline.OfflineTopComponent;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.utils.SystemTool;
import com.iontorrent.utils.io.FileUtils;
import com.iontorrent.wellmodel.WellContext;
import java.awt.Color;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.options.OptionsDisplayer;
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
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//com.iontorrent.scout.experimentviewer//ExperimentViewer//EN",
autostore = false)
@TopComponent.Description(preferredID = "ExperimentViewerTopComponent",
iconBase = "com/iontorrent/scout/experimentviewer/exptree/view-list-tree-2.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "top_left", openAtStartup = true)
@ActionID(category = "Window", id = "com.iontorrent.scout.experimentviewer.ExperimentViewerTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_ExperimentViewerAction",
preferredID = "ExperimentViewerTopComponent")
public final class ExperimentViewerTopComponent extends TopComponent implements ExplorerManager.Provider,
        TaskListener, ExperimentLoader {

    private static ExplorerManager explorermanager = new ExplorerManager();
    private EntityManager entityManager;
    private String URL;
    private MyResult oldresult;
    GlobalContext context;
    ExperimentContext exp;
    private String lastmsg;
    //  private ProgressHandle progress;
    private transient final InstanceContent wellContextContent = LookupUtils.getPublisher(WellContext.class);
    private transient final InstanceContent dataClassContent = LookupUtils.getPublisher(MyResult.class);
    private transient final InstanceContent expContent = LookupUtils.getPublisher(ExperimentContext.class);
    private transient final InstanceContent compContent = LookupUtils.getPublisher(CompositeExperiment.class);
    private transient final InstanceContent loadContent = LookupUtils.getPublisher(LoadDataContext.class);
    private transient final Lookup.Result<GlobalContext> gContextResults =
            LookupUtils.getSubscriber(GlobalContext.class, new GSubscriberListener());
    //db results
    ArrayList<MyRig> myrigs;
    ArrayList<NodeFilter> selectedFilters;
    List<RundbExperiment> experiments;
    List<RundbReportstorage> storages;
    private String run_name;
    private FindRunName findtask;
    CompositeExperiment comp;

    public ExperimentViewerTopComponent() {
        initComponents();

//        WindowManager m = WindowManager.getDefault(); 
//        TopComponent tc = this;// m.findTopComponent("<yourTopcomponentid>");         
//        m.findMode("explorer").dockInto(tc); 
        setName(NbBundle.getMessage(ExperimentViewerTopComponent.class, "CTL_ExperimentViewerTopComponent"));
        setToolTipText(NbBundle.getMessage(ExperimentViewerTopComponent.class, "HINT_ExperimentViewerTopComponent"));

        Lookup[] lookups = {ExplorerUtils.createLookup(explorermanager, getActionMap())};
        ProxyLookup lookup = new ProxyLookup(lookups);
        associateLookup(lookup);


        selectedFilters = new ArrayList<NodeFilter>();
        selectedFilters.add(ExpFilterPanel.getDefaultFilter());

        explorermanager.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                ExplorerManager em = (ExplorerManager) evt.getSource();
                Node[] sn = em.getSelectedNodes();
                if (sn != null) {
                    for (Node n : sn) {
                        if (n instanceof ResultNode) {
                            ResultNode beannode = (ResultNode) n;
                            p("Got result:" + beannode.getResult());
                            if (oldresult != null) {
                                dataClassContent.remove(oldresult);
                            }
                            oldresult = beannode.getResult();
                            lblExp.setText(oldresult.getChipType() + ": " + oldresult.getExperimentName());
                            lblRes.setText(oldresult.getResultsName());
                            update(oldresult);
                            // create new experiment context

                            // LookupUtils.publish(dataClassContent, oldresult);
                        } else if (n instanceof ExpNode) {
                            ExpNode beannode = (ExpNode) n;
                            p("Got exp:" + beannode.getExp());
                            lblExp.setText(beannode.getExp().getExpName());
//                            Iterator it = beannode.getExp().getRundbResultsCollection().iterator();
//                            if (it.hasNext()) {
//                                oldresult = new MyResult((RundbResults) it.next(), beannode.getRig());
//                                //LookupUtils.publish(dataClassContent, oldresult);
//                                update(oldresult);
//                                lblRes.setText(oldresult.getResultsName());
//                            }

                        } else if (n instanceof BeanNode) {
                            BeanNode node = (BeanNode) n;
                            if (node.getDisplayName().equalsIgnoreCase("Thumbnails")) {
                                p("Got thumbnails node");
                                ResultNode res = (ResultNode) node.getParentNode();
                                oldresult = res.getResult();
                                lblExp.setText(oldresult.getChipType() + ": " + oldresult.getExperimentName());
                                lblRes.setText(oldresult.getResultsName());
                                update(oldresult);
                                if (comp != null) {
                                    exp = comp.getThumbnailsContext(false);
                                    p("Got thumbnails context: " + exp);
                                }
                            }
                        }
                    }
                }

            }
        });
        disableButton();
        btnView.setToolTipText("Select a result to enable button. Loads data for viewing");

    }

    public boolean acceptExperimentContext() throws HeadlessException {
        // load maskview component

        String msg = "Getting information on run... (including files/paths/indices)";
        this.setStatus(msg);
        GuiUtils.showNonModalMsg(msg, true, 3);
        if (context.isComplexRule()) {
            String dirs = context.getManager().getBaseDirs();
            dirs = dirs.replace("\n", "<br>");
            dirs = dirs.replace(" ", "<br>");
            GuiUtils.showNonModelMsg("Locating data (Checking multiple drives...)", "<html>Checking:<br>" + dirs + "</html>", true, 50);
        }
        //  p("First clear all old experimental data!");
        clearOldData(exp);

        context.setExperimentContext(exp, true);
        if (!arePathsForExpOk()) {
            return false;
        }
        p("Checking for Proton: " + exp.getChipType());
        if (exp.isChipBB() && !exp.isThumbnails() && !exp.isBlock()) {
            p("Composite: parsing blocks");
            comp = new CompositeExperiment(exp);
            comp.maybParseBlocks();
            //  p("Got blocks: " + comp.getBlocks());
            p("Publishing CompositeExperiment");
            LookupUtils.publish(compContent, comp);
            // check for thumbnails            

        }
        if (exp != null) {
            justPublishExp(exp);
        }
        afterExperimentOpened(exp);
        return false;
    }

    public static void afterExperimentOpened(ExperimentContext exp) {
        TopComponent tc = null;
        if (exp.doesExplogHaveBlocks()) {
            tc = (TopComponent) WindowManager.getDefault().findTopComponent("TorrentScoutCompositeViewTopComponent");
        } else {
            if (exp.hasBam()) {
                tc = (TopComponent) WindowManager.getDefault().findTopComponent("TorrentScoutMaskViewTopComponent");
            } else {
                tc = (TopComponent) WindowManager.getDefault().findTopComponent("WholeChipViewTopComponent");
            }

        }
        openComponent(tc, true);
    }

    protected static void openComponent(TopComponent tc, boolean attention) {
        if (tc != null) {
            if (!tc.isOpened()) {
                tc.open();
            }
            tc.requestActive();
            tc.requestVisible();
            tc.toFront();
            if (attention) {
                tc.requestAttention(true);
            }
        }
    }

    protected void pickSite() throws HeadlessException {
        SiteList pan = new SiteList();
        int ans = JOptionPane.showConfirmDialog(this, pan, "Pick a site", JOptionPane.OK_CANCEL_OPTION);
        if (ans == JOptionPane.OK_OPTION) {
            String site = pan.getSelectedValue().getKey();

            if (context == null) {
                context = GlobalContext.getContext();
            }
            context.setContext(site);

            setStatus("Selecting site " + site + "/" + FolderManager.getManager().getRule() + ": " + FolderManager.getManager().getDbUrl());
            this.txtUrl.setText(context.getDbUrl());
            this.URL = context.getDbUrl();
            loadDataFromDb(false);
        }
    }

    protected void removeEmptyNodes(Children pgms) {
        ArrayList<Node> premove = new ArrayList<Node>();
        for (Node pgm : pgms.getNodes()) {
            // remove empty pgms
            //  p("Checking pgm: " + pgm.getDisplayName());
            if (pgm.isLeaf() || pgm.getChildren().getNodesCount() < 1) {
                //   pgm.setHidden(true);                
                premove.add(pgm);
            } else {
                Children exp = pgm.getChildren();
                //   p("Node has: " + exp.getNodesCount() + " experiments");
                for (Node exper : exp.getNodes()) {
                    BeanNode b = (BeanNode) exper;
                    if (exper.isLeaf() || exper.getChildren().getNodesCount() < 1) {
                        //   exper.setHidden(true);
                        // b.
                        //   p("removing " + exper.getDisplayName());
                        Node[] rm = new Node[1];
                        rm[0] = exper;
                        exp.remove(rm);
                    } else {
                        // p(exper.getDisplayName() + " has " + exper.getChildren().getNodesCount() + " results");
                    }
                }
                if (pgm.isLeaf() || pgm.getChildren().getNodesCount() < 1) {
                    //   pgm.setHidden(true);                
                    premove.add(pgm);
                }
            }
        }
        Node[] rm = new Node[premove.size()];
        if (rm.length > 0) {
            for (int i = 0; i < rm.length; i++) {
                RigNode r = (RigNode) premove.get(i);
                rm[i] = r;
                r.setVisible(false);
                p("setting to invisible " + r);
            }
            pgms.remove(rm);
            //   pgms.
        }
    }

    private void justPublishExp(ExperimentContext exp) {
        p("Publishing REGULAR expcontext " + exp.getResultsDirectory());
        LookupUtils.publish(expContent, exp);
        WellContext wellcontext = exp.createWellContext();
        if (wellcontext != null) {
            LookupUtils.publish(wellContextContent, wellcontext);
        }
    }

    public boolean arePathsForExpOk() throws HeadlessException {
        String errmsg = context.getContextInfo(false);
        if (errmsg != null && errmsg.length() > 0) {

            // GuiUtils.showNonModelMsg("<html>" + errmsg + "<html>", true);
            errmsg = "<b>There is a problem with the paths:</b><br>" + errmsg;
            //  JOptionPane.showMessageDialog(this, "<html>" + errmsg + "<html>");
            String[] options = {"Edit manually", "Show more detailed info", "Open configuration", "Cancel"};
            int ans = JOptionPane.showOptionDialog(this, "<html>" + errmsg + "<html>", "Path problem", JOptionPane.NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, options, options[0]);
            //  p("Got answer: "+ans);
            if (ans == 0) { //edit
                OfflineTopComponent tc = (OfflineTopComponent) WindowManager.getDefault().findTopComponent("OfflineTopComponent");
                if (tc != null) {
                    if (!tc.isOpened())tc.open();
                    tc.setExperimentContext(context.getExperimentContext());
                    tc.requestActive();
                    tc.requestVisible();
                    tc.toFront();
                    tc.requestAttention(true);
                }
            } else if (ans == 2) {
                OptionsDisplayer.getDefault().open("TorrentScoutOptions/TorrentScoutSettings");
            } else if (ans == 1) {
                FolderAction act = new FolderAction(exp);
                act.actionPerformed(null);
            }
            return false;


        }
        return true;
    }

    public void loadExperiment() {


        TopComponent tc = null;
        // if (!exp.isBB()) {
        tc = (TopComponent) WindowManager.getDefault().findTopComponent("TorrentScoutMaskViewTopComponent");
        GuiUtils.showNonModalMsg("Loading data for " + exp.getRawDir(), "Db Browser");
        LookupUtils.publish(loadContent, new LoadDataContext());
        afterExperimentOpened(exp);
    }

    private void doHintAction() {
        String msg = "<html>You can do the following things here:<ul>";
        msg += "<li>connect to a database (select the <b>db icon</b> to change it)</li>";
        msg += "<li>open a pgm, experiment and select a result</li>";
        msg += "<li>load the experiment to view both raw and results data<br>"
                + "(go to the <b>menu windows</b> to find viewers etc!)</li>";
        msg += "<li>click the <b>filter</b> icon to narrow down your list of results and experiments<br>"
                + "(you can filter by name, status, chip type, completion, date etc)</li>";
        msg += "<li>double click on an experiment to see experiment details</li>";
        msg += "<li>double click on a result (and other types of nodes) to see their details</li>";
        msg += "<li>you can get the same info with menu windows, properties</li>";
        msg += "</ul></html>";
        JOptionPane.showMessageDialog(this, msg);
    }

    private void addFavorite(String resultname) throws HeadlessException {
        Preferences pref = NbPreferences.forModule(ExperimentViewerTopComponent.class);


        if (exp == null) {
            JOptionPane.showMessageDialog(this, "No result is selected yet, please select one first");
        } else {
            try {
                pref.put("favorite_result_" + this.context.getServer(), resultname);
                pref.flush();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void disableButton() {
        this.btnView.setEnabled(false);
        btnView.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12)); // NOI18N
        btnView.setForeground(Color.DARK_GRAY);
        this.btnAddFavorite.setEnabled(false);

    }

    private class MyViewListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            loadExperiment();
        }
    }

    private boolean checkIndices() {

        IndexPanel pan = new IndexPanel(WindowManager.getDefault().getMainWindow(), this, this.exp, new MyViewListener());
        if (pan.checkIndices()) {
            return true;
        }
        // int ans = JOptionPane.showConfirmDialog(this, pan, "It is recommended that you create the indices first", JOptionPane.YES_NO_OPTION) ;
        JFrame msgframe = GuiUtils.showNonModalDialog(pan, "Not all index files found");
        pan.setMsgFrame(msgframe);

        return false;
    }

    @Override
    public void maybeLoadExperiment(ExperimentContext exp) {
        this.exp = exp;

        p("Maybeloadexp: " + exp.getRawDir());
        boolean ok = checkIndices();
        if (ok) {
            loadExperiment();
        }
    }

    private void enableButton() {
        this.btnView.setEnabled(true);
        btnView.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnView.setForeground(new java.awt.Color(0, 153, 51));

        this.btnAddFavorite.setEnabled(true);
        if (this.exp != null) {
            btnAddFavorite.setToolTipText("Set result " + exp.getResultsName() + " as favorite experiment");
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
        if (context == null || URL == null || !URL.equalsIgnoreCase(newc.getDbUrl())) {
            context = newc;


            if (context != null) {
                Preferences pref = NbPreferences.forModule(ExperimentViewerTopComponent.class);
                String resname = pref.get("favorite_result_" + this.context.getServer(), null);

                if (resname
                        != null) {
                    this.btnAddFavorite.setToolTipText("Remember " + resname + " as my favorite run");
                }


                if (context.getExperimentContext()
                        != null) {
                    enableButton();
                }
                URL = context.getDbUrl();

                if (URL != null && URL.length() > 0) {
                    p("Got a global clontext with dir:" + URL);
                    setStatus("Got a context with db URL " + URL);
                    this.txtUrl.setText(URL);
                  
                    loadDataFromDb(false);

                } else {
                    // showOfflineComponent();
                }
            }
        }
    }

    private void showOfflineComponent() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                TopComponent tc = (TopComponent) WindowManager.getDefault().findTopComponent("OfflineTopComponent");
                if (tc != null) {

                    tc.requestActive();
                    tc.toFront();
                    tc.requestVisible();
                }
            }
        });

    }

    private boolean buildTreeFromRigsAndExperiments(List<RundbExperiment> experiments) throws HeadlessException {
        this.experiments = experiments;
        if (experiments == null || experiments.size() < 1) {
            JOptionPane.showMessageDialog(this, "I found no experiments at DB " + URL
                    + "\nYou might want to check the settings in Tools/Options");
            createTreeModelFromData();
            return false;
        }
        //    p("Got " + experiments.size() + " experiments");
        ArrayList<MyRig> tmprigs = new ArrayList<MyRig>();
        // create map
        //    p("Creating rig map");
        HashMap<String, MyRig> map = new HashMap<String, MyRig>();
        for (RundbExperiment ex : experiments) {
            String rname = ex.getPgmName();
            MyRig rig = map.get(rname.toLowerCase());
            if (rig == null) {
                MyRig myrig = new MyRig(rname);
                tmprigs.add(myrig);
                //  p("Got rig: "+myrig.getName());
                myrig.setExperiments(new ArrayList<RundbExperiment>());
                map.put(rname.toLowerCase(), myrig);
            }
        }

        List<RundbExperiment> exp = new ArrayList();
        for (RundbExperiment ex : experiments) {
            MyRig rig = map.get(ex.getPgmName().toLowerCase());
            if (rig != null) {
                rig.getExperiments().add(ex);
                //   p("Adding exp "+ex.getExpName()+" to rig "+rig.getName());
            } else {
                p("Found no rig for exp: " + ex.getExpName() + ":" + ex.getPgmName());
            }
        }

        myrigs = new ArrayList<MyRig>();
        for (MyRig rig : tmprigs) {
            if (rig.getExperiments() != null && rig.getExperiments().size() > 0) {
                myrigs.add(rig);
            } else {
                map.remove(rig.getName().toLowerCase());
            }
        }
        createTreeModelFromData();
        return true;
    }

    /** create experiment context */
    private void update(MyResult result) {
        if (context == null) {
            context = GlobalContext.getContext();
        }

        exp = result.createContext();

        if (result.isCompleted()) {
            this.enableButton();
            btnView.setToolTipText("The experiment has completed and you can view the data");
        } else {
            // this.disableButton();
            this.enableButton();
            this.btnView.setToolTipText("The experiment has not completed and the data is not ready yet - but you can still view the data if you wish");
        }

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

    @Override
    public Action[] getActions() {
        return OpenWindowAction.getActions(this);
    }

    private void createTreeModelFromData() {
        //         for (RundbExperiment ex: experiments) {
        //             p("Exp :"+ex.getExpName()+" has "+ex.getRundbResultsCollection().size()+" results");
        //       
        //p("selected filters are:" + selectedFilters);

        ExpNode.setFilters(selectedFilters);

        ExpNode.setStorages(storages);
        Children pgms = Children.create(new RigChildFactory(myrigs), true);
        AbstractNode rootnode = new RootNode(pgms);
        explorermanager.setRootContext(rootnode);
        explorermanager.getRootContext().setDisplayName("Experiments from " + this.URL);



        if (myrigs != null && myrigs.size() > 0 && experiments != null && explorermanager.getRootContext() != null) {

            if (run_name != null && run_name.length() > 0 && !run_name.startsWith("$")) {
                if (findtask == null) {
                    findtask = new FindRunName(this, run_name);
                    findtask.execute();
                }
                //this.findAndLoadResult(run_name);

            }
            RemoveEmptyNodes remove = new RemoveEmptyNodes(this);
            remove.execute();
        }
    }

    private MyResult findResultNode(String resname) {
        if (resname == null) {
            return null;
        }
        if (explorermanager == null || explorermanager.getRootContext() == null) {
            return null;
        }
        RootNode rootnode = (RootNode) explorermanager.getRootContext();
        Children children = rootnode.getChildren();
        if (children == null) {
            p("Got no rig children");
            return null;
        }
        resname = resname.toLowerCase().trim();
        Node[] rigs = children.getNodes();
        for (Node rig : rigs) {
            Children expch = rig.getChildren();
            if (expch == null) {
                continue;
            }
            //    p("Checking rig: " + rig.getDisplayName());
            Node[] exps = expch.getNodes();
            for (Node ex : exps) {
                Children chresults = ex.getChildren();
                if (chresults == null) {
                    continue;
                }
                //  p("Checking " + ex.getDisplayName());
                Node[] results = chresults.getNodes();
                for (Node n : results) {
                    //     p("Got node :"+n.getName()+":"+n.getClass().getName());
                    if (n instanceof ResultNode) {
                        ResultNode res = (ResultNode) n;

                        String name = res.getResult().getResultsName().toLowerCase().trim();
                        if (name.startsWith(resname) || resname.startsWith(name)) {
                            p("Found result  " + res.getResult());
                            Node[] selected = new Node[1];
                            selected[0] = res;
                            MyResult r = res.getResult();
                            lblExp.setText(r.getChipType() + ": " + res.getResult().getExperimentName());
                            lblRes.setText(r.getResultsName());
                            try {
                                explorermanager.setSelectedNodes(selected);

                            } catch (PropertyVetoException ex1) {
                                Exceptions.printStackTrace(ex1);
                            }

                            return res.getResult();
                        }
                    }
                }
            }
        }
        p("Could not find ResultsName='" + resname + "' in tree");

        return null;

    }

    private void getUrlFromUser() throws HeadlessException {
        URL = JOptionPane.showInputDialog(this, "I could not connect to the db at " + URL + "."
                + "Please enter the correct URL (with or without port)", "Connection to " + URL, JOptionPane.QUESTION_MESSAGE);

        // alternative: OptionsDisplayer.getDefault().open("TorrentScoutOptions");


        if (URL != null && URL.trim().length() > 0) {

            this.txtUrl.setText(URL);
            //setAndTestURL();
        }
    }

    private void downloadMainFiles() {

        if (context.getExperimentContext() == null) {
            JOptionPane.showMessageDialog(this, "Please select an experiment and result first");
            return;
        } else if (!FileUtils.isUrl(context.getExperimentContext().getResultsDirectory())) {
            //JOptionPane.showMessageDialog(this, "The data is in a regular directory, no need to download the files");
            maybeLoadExperiment(exp);
            return;

        } else {
            String msg = context.getContextInfo(false);
            if (msg.length() > 0) {
                JOptionPane.showMessageDialog(this, "<html>I cannot download the files yet because<br>" + msg + "</html>");
                return;
            }
            int ans = JOptionPane.showConfirmDialog(this, "The download might take a few minutes (depending on where you are on the planet relative to the data :-)."
                    + "\n Would you like me to download the experiment data?", "Downloading files", JOptionPane.OK_CANCEL_OPTION);
            if (ans == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        String msg = "Downloading main files (1.wells, bfmask.bin, .sff and .sam/.bam) ....";

        ExpDownloadTask task = new ExpDownloadTask(this, null, this.context);
        ProgressHandle progress = ProgressHandleFactory.createHandle(msg, task);
        task.setProgressHandle(progress);

        task.execute();
    }

    private void loadDataFromDb(boolean pick) {
        //loadDataFromRest();
        if (this.context == null) {
            return;
        }
        URL = context.getDbUrl();
        if (URL == null || URL.trim().length() < 1) {
            p("Got no url");
            if (pick) {
                pickSite();
            } else {
                showOfflineComponent();
            }

            return;
        }

        run_name = SystemTool.getProperty("run_name");
        if ((run_name == null || run_name.length() < 1) && URL.indexOf("localhost") > -1) {
            //  run_name = "trace-fix";
            this.selectedFilters = null;
        }

        getDbContextViaThread();
    }

//    private void createCacheFiles() {
//        TorrentScoutStartupTopComponent tc = (TorrentScoutStartupTopComponent) WindowManager.getDefault().findTopComponent("TorrentScoutStartupTopComponent");
//        int tasks = 0;
//        if (!TorrentScoutStartupTopComponent.tasks.isEmpty()) {
//            tasks = TorrentScoutStartupTopComponent.tasks.size();
//        }
//        if (tasks == 0) {
//            tc.checkAndMaybeConvertFiles();
//        }
//    }
    private void getDbContextViaThread() {
        if (URL == null) {
            return;
        }

        String msg = "Trying to connect to db at " + URL + "....";
        ProgressHandle progress = ProgressHandleFactory.createHandle(msg);
        // progress = new ProgressFrame(this, this.getIcon(),msg);
        DbContextTask task = new DbContextTask(this);
        task.execute();
    }

    @Override
    public void taskDone(Task t) {
        p("Task " + t + " is done");
        setCursor(null);

        //progress.stop();
        if (t instanceof FindRunName) {
            FindRunName f = (FindRunName) t;
            MyResult result = f.getResult();
            findtask = null;
            if (result != null) {
                update(result);
                this.requestActive();
                if (acceptExperimentContext()) {
                    downloadMainFiles();
                }
            } else {
                this.setStatusWarning("Didn't see " + f.run_name + " @ " + URL + ", might want to check the filter settings and site...?");
                run_name = null;
            }
        } else if (t instanceof DbContextTask) {
            //   p("calling aftergotdb context");
            afterGotDbContext();
        } else if (t instanceof DbLoadTask) {
            //  p("calling createtreemodel from data");
            createTreeModelFromData();
        } else if (t instanceof CreateIndexTask) {
            maybeLoadExperiment(exp);
        } else if (t instanceof ExpDownloadTask) {
            ExpDownloadTask et = (ExpDownloadTask) t;

            //  JOptionPane.showMessageDialog(this, "The download task is complete. " + et.getMsg());
            maybeLoadExperiment(exp);
        }
    }

    private void afterGotDbContext() {

        //  entityManager = createDbContext();
        if (entityManager == null) {
            this.setStatusWarning("loadDataFromDb: Got no entity manager from " + URL + " after createDbContext");
            this.loadDataFromRest();
            return;
        }

        // progress = new ProgressFrame(this, this.getIcon(),msg);
        DbLoadTask task = new DbLoadTask(this);
        task.execute();
    }

    private void loadDataFromRest() {
        if (URL == null || URL.length() < 1) {
            return;
        }
        if (URL.indexOf("localhost") > -1) {
            setStatus("Not callin REST from localhost");
            return;
        }

        this.setStatus("Loading data from REST " + URL);
        ExperimentRestClientAdapter client = new ExperimentRestClientAdapter(URL);
        ArrayList<RundbExperiment> experiments = client.getExperiments();
        buildTreeFromRigsAndExperiments(experiments);
    }

    private void loadDataAfterGotContext() {
        this.setStatus("Loading data from DB " + URL);
        Query query = entityManager.createQuery("SELECT c FROM RundbExperiment c");
        experiments = query.getResultList();

        query = entityManager.createQuery("SELECT c FROM RundbReportstorage c");
        storages = query.getResultList();


        buildTreeFromRigsAndExperiments(experiments);

    }

    public void resultChanged(LookupEvent lookupEvent) {
        Lookup.Result r = (Lookup.Result) lookupEvent.getSource();
        Collection c = r.allInstances();
        if (!c.isEmpty()) {

            for (Iterator i = c.iterator(); i.hasNext();) {
                p("resultChangedL Got object: " + i.next().getClass().getName());
            }

        } else {
            p("Result changed, but got no instances");
        }
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

    private class RemoveEmptyNodes extends Task {

        public RemoveEmptyNodes(TaskListener tlistener) {
            super(tlistener);
        }

        @Override
        public Void doInBackground() {
            // p("Thread is waiting");
            try {
                Thread.currentThread().sleep(3000);
                Node root = explorermanager.getRootContext();
                Children pgms = root.getChildren();
                removeEmptyNodes(pgms);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }
    }

    private class FindRunName extends Task {

        String run_name;
        MyResult result;

        public FindRunName(TaskListener tlistener, String run_name) {
            super(tlistener, ProgressHandleFactory.createHandle("Finding run  " + run_name + "...."));
            this.run_name = run_name;
        }

        @Override
        public Void doInBackground() {
            // p("Thread is waiting");
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            GuiUtils.showNonModalMsg("Searching for run " + run_name);
            p("Trying to find " + run_name + "in tree");
            result = findResultNode(run_name);

            if (result == null) {
                try {
                    Thread.currentThread().sleep(2000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                result = findResultNode(run_name);
            }
            if (result == null) {
                setStatusWarning("Could not find " + run_name + " on " + URL + "... is this the right site :-)? Or maybe data hasn't all loaded yet");
            }
            return null;
        }

        public boolean isSuccess() {
            return result != null;
        }

        private MyResult getResult() {
            return result;
        }
    }

    private class DbContextTask extends Task {

        public DbContextTask(TaskListener tlistener) {
            super(tlistener, ProgressHandleFactory.createHandle("ExpComp: Connecting to db at  " + URL + "...."));
        }

        @Override
        public Void doInBackground() {
            createDbContext();
            return null;
        }

        public boolean isSuccess() {
            return entityManager != null;
        }
    }

    private class DbLoadTask extends Task {

        public DbLoadTask(TaskListener tlistener) {
            super(tlistener, ProgressHandleFactory.createHandle("Loading data from db at  " + URL + "...."));
        }

        @Override
        public Void doInBackground() {
            loadDataAfterGotContext();
            return null;
        }

        public boolean isSuccess() {
            return entityManager != null;
        }
    }

    private EntityManager createDbContext() {
        if (URL == null) {
            return null;
        }
        setStatus("Trying to access db " + URL + "....");
        PersistenceHelper persist = new PersistenceHelper();

        boolean ok = persist.setURL(URL);
        if (ok) {
//            ok = persist.testURL();
//            txtUrl.setText(persist.getURL());
            entityManager = persist.getEntityManager();
        }
        if (!ok) {
            this.setStatusWarning("I was unable to connect to the db at " + URL + ", please check the URL");
        }

        return entityManager;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorermanager;






    }

//    /** ================== LOGGING ===================== */
//      private static void setFormatter() {
//        final ShortFormatter formatter = new ShortFormatter();
//        Logger rootLogger = Logger.getLogger(ExperimentViewerTopComponent.class.getName());
//
//        
//        while (rootLogger.getParent() != null) {
//            rootLogger = rootLogger.getParent();
//        }
//
//        for (final Handler handler : rootLogger.getHandlers()) {
//            handler.setFormatter(formatter);
//        }
//
//    }
    private void err(String msg, Exception ex) {
        Logger.getLogger(ExperimentViewerTopComponent.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(ExperimentViewerTopComponent.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(ExperimentViewerTopComponent.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        //   System.out.println("ExperimentViewerTopComponent: " + msg);
        Logger.getLogger(ExperimentViewerTopComponent.class.getName()).log(Level.INFO, msg);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        beanTreeView1 = new org.openide.explorer.view.BeanTreeView();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblRes = new javax.swing.JLabel();
        lblExp = new javax.swing.JLabel();
        jToolBar2 = new javax.swing.JToolBar();
        btnFilter = new javax.swing.JButton();
        btnAddFavorite = new javax.swing.JButton();
        btnLoadFavorite = new javax.swing.JButton();
        hint1 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        btnView = new javax.swing.JButton();
        hint = new javax.swing.JButton();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        txtUrl = new javax.swing.JTextField();
        btnReload = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.jButton2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.jButton3.text")); // NOI18N

        setBackground(java.awt.Color.white);

        beanTreeView1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblRes, org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.lblRes.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblExp, org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.lblExp.text")); // NOI18N

        jToolBar2.setRollover(true);
        jToolBar2.setMaximumSize(new java.awt.Dimension(300, 27));
        jToolBar2.setOpaque(false);

        btnFilter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/scout/experimentviewer/filter.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnFilter, org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.btnFilter.text")); // NOI18N
        btnFilter.setToolTipText(org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.btnFilter.toolTipText")); // NOI18N
        btnFilter.setFocusable(false);
        btnFilter.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnFilter.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilterActionPerformed(evt);
            }
        });
        jToolBar2.add(btnFilter);

        btnAddFavorite.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/scout/experimentviewer/heart-add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnAddFavorite, org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.btnAddFavorite.text")); // NOI18N
        btnAddFavorite.setToolTipText(org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.btnAddFavorite.toolTipText")); // NOI18N
        btnAddFavorite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddFavoriteActionPerformed(evt);
            }
        });
        jToolBar2.add(btnAddFavorite);

        btnLoadFavorite.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/scout/experimentviewer/folder-heart.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnLoadFavorite, org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.btnLoadFavorite.text")); // NOI18N
        btnLoadFavorite.setToolTipText(org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.btnLoadFavorite.toolTipText")); // NOI18N
        btnLoadFavorite.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnLoadFavorite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadFavoriteActionPerformed(evt);
            }
        });
        jToolBar2.add(btnLoadFavorite);

        hint1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/scout/experimentviewer/help-hint.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(hint1, org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.hint1.text")); // NOI18N
        hint1.setToolTipText(org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.hint1.toolTipText")); // NOI18N
        hint1.setFocusable(false);
        hint1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        hint1.setMargin(new java.awt.Insets(2, 2, 2, 2));
        hint1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        hint1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hint1ActionPerformed(evt);
            }
        });
        jToolBar2.add(hint1);

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/scout/experimentviewer/run-build-clean.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton4, org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.jButton4.text")); // NOI18N
        jButton4.setToolTipText(org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.jButton4.toolTipText")); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jToolBar2.add(jButton4);

        jPanel2.setMaximumSize(new java.awt.Dimension(10, 20));
        jPanel2.setMinimumSize(new java.awt.Dimension(10, 20));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        jToolBar2.add(jPanel2);

        btnView.setFont(new java.awt.Font("Tahoma", 1, 12));
        btnView.setForeground(new java.awt.Color(0, 153, 51));
        org.openide.awt.Mnemonics.setLocalizedText(btnView, org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.btnView.text")); // NOI18N
        btnView.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnView.setMaximumSize(new java.awt.Dimension(150, 21));
        btnView.setMinimumSize(new java.awt.Dimension(120, 21));
        btnView.setPreferredSize(new java.awt.Dimension(120, 21));
        btnView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewActionPerformed(evt);
            }
        });
        jToolBar2.add(btnView);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(30, 30, 30)
                        .addComponent(lblRes, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblExp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(30, Short.MAX_VALUE))
            .addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lblExp))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lblRes))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.openide.awt.Mnemonics.setLocalizedText(hint, org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.hint.text")); // NOI18N
        hint.setToolTipText(org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.hint.toolTipText")); // NOI18N
        hint.setFocusable(false);
        hint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        hint.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        hint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hintActionPerformed(evt);
            }
        });

        jToolBar1.setRollover(true);
        jToolBar1.setOpaque(false);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/scout/experimentviewer/database-connect.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.jButton1.text")); // NOI18N
        jButton1.setToolTipText(org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.jButton1.toolTipText")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.jLabel1.text")); // NOI18N
        jToolBar1.add(jLabel1);

        txtUrl.setColumns(30);
        txtUrl.setText(org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.txtUrl.text")); // NOI18N
        txtUrl.setToolTipText(org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.txtUrl.toolTipText")); // NOI18N
        txtUrl.setMinimumSize(new java.awt.Dimension(20, 20));
        txtUrl.setPreferredSize(new java.awt.Dimension(100, 20));
        txtUrl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtUrlActionPerformed(evt);
            }
        });
        jToolBar1.add(txtUrl);

        btnReload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/scout/experimentviewer/view-refresh-3.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnReload, org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.btnReload.text")); // NOI18N
        btnReload.setToolTipText(org.openide.util.NbBundle.getMessage(ExperimentViewerTopComponent.class, "ExperimentViewerTopComponent.btnReload.toolTipText")); // NOI18N
        btnReload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReloadActionPerformed(evt);
            }
        });
        jToolBar1.add(btnReload);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(beanTreeView1, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 165, Short.MAX_VALUE)
                    .addComponent(hint, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 164, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(beanTreeView1, javax.swing.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 351, Short.MAX_VALUE)
                    .addComponent(hint, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 352, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtUrlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUrlActionPerformed
        String old = URL;
        URL = txtUrl.getText();
        context.setDbUrl(URL);
        Preferences pref = NbPreferences.forModule(ExperimentViewerTopComponent.class);
        if (URL != null && old != null && !old.equalsIgnoreCase(URL)) {
            loadDataFromDb(false);
        } else if (old == null && URL != null) {
            loadDataFromDb(false);
        }
    }//GEN-LAST:event_txtUrlActionPerformed

    private void btnReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReloadActionPerformed
        URL = txtUrl.getText();
        loadDataFromDb(false);
    }//GEN-LAST:event_btnReloadActionPerformed

    private void btnFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterActionPerformed
        ExpFilterPanel pan = new ExpFilterPanel();
        pan.setSelected(selectedFilters);
        int ans = JOptionPane.showConfirmDialog(this, pan, "Select a filter", JOptionPane.OK_CANCEL_OPTION);
        if (ans != JOptionPane.OK_OPTION) {
            return;
        }
        selectedFilters = pan.getSelectedFilters();
        // ask for parameters
        for (NodeFilter f : selectedFilters) {
            f.askForInput();
        }
        createTreeModelFromData();
    }//GEN-LAST:event_btnFilterActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        pickSite();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewActionPerformed
        if (!acceptExperimentContext()) {
            return;
        }
        this.downloadMainFiles();
        //createCacheFiles();
    }//GEN-LAST:event_btnViewActionPerformed

    private void btnAddFavoriteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddFavoriteActionPerformed
        if (exp != null) {
            addFavorite(this.exp.getResultsName());
        }
    }//GEN-LAST:event_btnAddFavoriteActionPerformed

    private void btnLoadFavoriteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadFavoriteActionPerformed
        Preferences pref = NbPreferences.forModule(ExperimentViewerTopComponent.class);
        String resname = pref.get("favorite_result_" + this.context.getServer(), null);
        if (resname == null) {
            JOptionPane.showMessageDialog(this, "I found no favorite result for server " + this.context.getServer());
        } else {
            findAndLoadResult(resname);
        }
    }//GEN-LAST:event_btnLoadFavoriteActionPerformed

    private void hintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hintActionPerformed
    }//GEN-LAST:event_hintActionPerformed

    private void hint1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hint1ActionPerformed

        doHintAction();     }//GEN-LAST:event_hint1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        Node root = explorermanager.getRootContext();
        Children pgms = root.getChildren();
        removeEmptyNodes(pgms);

    }//GEN-LAST:event_jButton4ActionPerformed

    private void findAndLoadResult(String resname) {
        p("Trying to find " + resname + " in tree");
        if (this.context == null) {
            JOptionPane.showMessageDialog(this, "I got no global context yet, select a site first");
            return;
        }
        if (experiments == null) {
            JOptionPane.showMessageDialog(this, "I see currently no experiments for server " + this.context.getServer() + ",\nyou may need to (re)load the data first or wait a few seconds");
            return;
        }

        // first check data in tree
        //    GuiUtils.showNonModelMsg("Searching " + resname + " in tree...");
        // //  this.addFavorite(resname);
        //   GuiUtils.showNonModalMsg("Setting " + resname + " as favorite");
        MyResult my = findResultNode(resname);
        if (my != null) {
            this.update(my);
            this.downloadMainFiles();
            return;
        }
        GuiUtils.showNonModalMsg("Searching " + resname + " in database...", false, 10);
        if (findtask == null) {
            findtask = new FindRunName(this, run_name);
            findtask.execute();
        }
//        for (RundbExperiment ex : experiments) {
//            Collection<RundbResults> results = ex.getRundbResultsCollection();
//            if (results != null) {
//                for (RundbResults res : results) {
//        String    n = res.getResultsName().toLowerCase();
//                    p("Checking " + res.getResultsName());
//                    if (n.startsWith(resname) || resname.startsWith(n)) {
//                        p("Found favorite result!");
//                        my = new MyResult(res, new MyRig(ex.getPgmName()));
//                        this.update(my);
//                        this.downloadMainFiles();
//                        return;
//                    }
//                }
//            }
//        }
//        JOptionPane.showMessageDialog(this, "I could not find your favorite result " + resname + " in the db " + context.getServer());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.openide.explorer.view.BeanTreeView beanTreeView1;
    private javax.swing.JButton btnAddFavorite;
    private javax.swing.JButton btnFilter;
    private javax.swing.JButton btnLoadFavorite;
    private javax.swing.JButton btnReload;
    private javax.swing.JButton btnView;
    private javax.swing.JButton hint;
    private javax.swing.JButton hint1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JLabel lblExp;
    private javax.swing.JLabel lblRes;
    private javax.swing.JTextField txtUrl;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        // find context
        p("ComponentOpened");

        if (context == null || context.getDbUrl() == null || context.getDbUrl().length() < 1) {
            String default_rule = FolderManager.setDefaultRule();
            if (default_rule == null) {
                OptionsDisplayer.getDefault().open("TorrentScoutOptions/TorrentScoutSettings");
                return;
            } else {
                p("Got no db url");
              
            }
        } else {
            p("Got db url: " + context.getDbUrl());
        }
       
        loadDataFromDb(true);
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
