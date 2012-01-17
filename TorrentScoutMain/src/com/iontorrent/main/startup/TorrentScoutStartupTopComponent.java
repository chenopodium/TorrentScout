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
package com.iontorrent.main.startup;

import com.iontorrent.expmodel.CompositeExperiment;
import com.iontorrent.guiutils.netbeans.OpenWindowAction;
import com.iontorrent.main.data.ConverterTask;
import com.iontorrent.main.data.RawFileConverter;
import com.iontorrent.main.data.RawFileConverter.Conversion;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.FolderManager;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.guiutils.HtmlViewer;
import com.iontorrent.main.FolderAction;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.utils.SystemTool;

import com.iontorrent.utils.log.ShortFormatter;
import java.awt.BorderLayout;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
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
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//com.iontorrent.main.startup//TorrentScoutStartup//EN",
autostore = false)
@TopComponent.Description(preferredID = "TorrentScoutStartupTopComponent",
iconBase = "com/iontorrent/main/startup/application-form.png",
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "leftSlidingSide", openAtStartup = true)
@ActionID(category = "Window", id = "com.iontorrent.main.startup.TorrentScoutStartupTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_TorrentScoutStartupAction",
preferredID = "TorrentScoutStartupTopComponent")
public final class TorrentScoutStartupTopComponent extends TopComponent implements TaskListener {

    GlobalContext context;
    ExperimentContext expContext;
    private transient final Lookup.Result<ExperimentContext> dataClassResults =
            LookupUtils.getSubscriber(ExperimentContext.class, new SubscriberListener());
    private transient final InstanceContent globalContent = LookupUtils.getPublisher(GlobalContext.class);
    private transient final Lookup.Result<GlobalContext> gContextResults =
            LookupUtils.getSubscriber(GlobalContext.class, new GSubscriberListener());
    private transient final Lookup.Result<CompositeExperiment> compContextResults =
            LookupUtils.getSubscriber(CompositeExperiment.class, new CompSubscriberListener());
    private Conversion[] conv;
    public static ArrayList<ConverterTask> tasks = new ArrayList<ConverterTask>();
    private String lastmsg;
    private JComponent lblInfo;
    private String default_rule;
    private String skin;

    public TorrentScoutStartupTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(TorrentScoutStartupTopComponent.class, "CTL_TorrentScoutStartupTopComponent"));
        setToolTipText(NbBundle.getMessage(TorrentScoutStartupTopComponent.class, "HINT_TorrentScoutStartupTopComponent"));

        Authenticator au = new MyAuthenticator();
        Authenticator.setDefault(au);


        setLAF(skin);
//        if (TSSettings.isOverwriteFolderConfig()) FolderManager.resetFolderConfig();
    }

    public static void setLAF(String skin) {
        if (skin == null || skin.length() < 1) {
            skin = "aluminium";
        }
        String OS = SystemTool.getOsName();
        //  p("OS:" + OS);
        // for Mac Lion, skins don't work as of August 2011
        if (OS != null && OS.toLowerCase().indexOf("mac") > -1) {
            skin = "java";
            p("Got MAC OS:" + OS + ", setting NO skin");
            return;
        }

        skin = skin.toLowerCase().trim();
        p("Setting skin to " + skin);

        if (skin.startsWith("mcwin")) {
            com.jtattoo.plaf.mcwin.McWinLookAndFeel.setTheme("Default");
        } else if (skin.startsWith("nimbus")) {
        } else if (skin.startsWith("aero")) {
            com.jtattoo.plaf.aero.AeroLookAndFeel.setTheme("Default");
        } else if (skin.startsWith("java")) {
        } else if (skin.startsWith("hifi")) {
            com.jtattoo.plaf.hifi.HiFiLookAndFeel.setTheme("Default");

        } else if (skin.startsWith("noire")) {
            com.jtattoo.plaf.noire.NoireLookAndFeel.setTheme("Default");

        } else if (skin.startsWith("bern")) {
            com.jtattoo.plaf.bernstein.BernsteinLookAndFeel.setTheme("Default");

        } else if (skin.startsWith("mcwin")) {
            com.jtattoo.plaf.mcwin.McWinLookAndFeel.setTheme("Default");

        } else if (skin.startsWith("acryl")) {
            com.jtattoo.plaf.acryl.AcrylLookAndFeel.setTheme("Default");
        } else {
            com.jtattoo.plaf.aluminium.AluminiumLookAndFeel.setTheme("Default");
        }

        try {

            if (skin.startsWith("mcwin")) {
                UIManager.setLookAndFeel("com.jtattoo.plaf.mcwin.McWinLookAndFeel");
            } else if (skin.startsWith("acryl")) {
                UIManager.setLookAndFeel("com.jtattoo.plaf.acryl.AcrylLookAndFeel");
            } else if (skin.startsWith("hifi")) {
                UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
            } else if (skin.startsWith("noire")) {
                UIManager.setLookAndFeel("com.jtattoo.plaf.noire.NoireLookAndFeel");
            } else if (skin.startsWith("bern")) {
                UIManager.setLookAndFeel("com.jtattoo.plaf.bernstein.BernsteinLookAndFeel");
            } else if (skin.startsWith("mcwin")) {
                UIManager.setLookAndFeel("com.jtattoo.plaf.mcwin.McWinLookAndFeel");
            } else if (skin.startsWith("aero")) {
                UIManager.setLookAndFeel("com.jtattoo.plaf.aero.AeroLookAndFeel");
            } else if (skin.equalsIgnoreCase("java")) {
                p("Setting UIManager to: " + UIManager.getCrossPlatformLookAndFeelClassName());
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } else if (skin.startsWith("nimbus")) {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            } else {
                UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
            }

        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Exceptions.printStackTrace(ex);
        }
        JFrame jFrame = (JFrame) WindowManager.getDefault().getMainWindow();
        SwingUtilities.updateComponentTreeUI(jFrame);
    }

     private class CompSubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            getLatestCompExperimentContext();
        }
    }
     private void getLatestCompExperimentContext() {
        //   p("Getting latest exp context");
        
        final Collection<? extends CompositeExperiment> items = compContextResults.allInstances();
        if (!items.isEmpty()) {
            CompositeExperiment data = null;
            Iterator<CompositeExperiment> it = (Iterator<CompositeExperiment>) items.iterator();
            while (it.hasNext()) {
                data = it.next();
            }
           update(data);
        }
    }
     private void update(CompositeExperiment result) {
        this.expContext = result.getRootContext();
        this.updateInfo();

    }
    public void setExperimentContext(ExperimentContext exp) {
        this.expContext = exp;
    }

    public static class MyAuthenticator extends Authenticator {
        // This method is called when a password-protected URL is accessed

        protected PasswordAuthentication getPasswordAuthentication() {
            // Get information about the request
            String promptString = getRequestingPrompt();
            String hostname = getRequestingHost();
            InetAddress ipaddr = getRequestingSite();
            int port = getRequestingPort();

            // Get the username from the user...
            String username = "ionuser";
            // Get the password from the user...
            String password = "ionuser";

            // Return the information
            return new PasswordAuthentication(username, password.toCharArray());
        }
    }

    private void updateInfo() {

        String msg = "<body bgcolor=\"FFFFFF\">";


        FolderAction ac = null;
        if (expContext != null) {
            ac = new FolderAction(expContext);
            msg += FolderAction.getHtmlText(expContext);
        } else {
            msg += "<font color='aa0000'>Got no experimental context</font><br>";
        }
        if (context != null) {
            msg += "<br><b>Folder access permissions:</b><br>";
            msg += context.getContextInfo(true);

        } else {
            msg = "<font color='aa0000'>Got no global context</font><br>";
        }
        // also add environmental paramters
        msg += "<br><b>Passed in and system parameters:</b><br>";
        msg += "run_name: " + SystemTool.getInfo("run_name") + "<br>";
        msg += "code base: " + SystemTool.getInfo("codebase") + "<br>";
        msg += "version: " + SystemTool.getInfo("version") + "<br>";
        if (lblInfo != null) {
            remove(lblInfo);
        }
        msg += "</body>";
        lblInfo = HtmlViewer.getComponent(msg, ac);
        add("Center", lblInfo);
        repaint();

    }

    private class GSubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            context = GlobalContext.getContext();
            if (context != null) {
                updateContext();

            }
        }
    }

    private void updateContext() {
        //  p("Got a global clontext with dir:" + context.getResultsDir());
        if (context == null || context.getDbUrl() == null || context.getDbUrl().length() < 1) {
            default_rule = FolderManager.setDefaultRule();
//            if (default_rule == null) {
//                OptionsDisplayer.getDefault().open("TorrentScoutOptions/TorrentScoutSettings");
//            } 
            if (default_rule == null || default_rule.length() < 1 || default_rule.startsWith("offline")) {
                showOfflineComponent();
            }
        } else {
            p("Got db url: " + context.getDbUrl());
            this.showExperimentViewer();
            UpdateTask t = new UpdateTask(this);
            t.execute();
            // updateInfo();
        }

    }

    public void checkAndMaybeConvertFiles() {
        checkAndMaybeConvertFiles(this);
    }

    public void checkAndMaybeConvertFiles(TaskListener listener) {
        String msg = "";
        // check for chip size

        if (context.getExperimentContext() == null) {
            this.setStatus("I got no context from the db");
            return;
        }

//        if (!context.getExperimentContext().is314() && !context.getExperimentContext().is316()) {
//            p("Chip is large: " + context.getExperimentContext().getChipType() + ", will not convert files but read them directly");
//            //return;
//        }
        p("checkAndMaybeConvertFiles: Got exp: " + this.expContext);
        if (tasks.size() > 0) {
            int ans = JOptionPane.showConfirmDialog(this, "<html>I am already running  " + tasks.size() + " caching tasks, I don't dare to start another task :-).<br>"
                    + "<b>Do you want me to cancel them?</b></html>", "Got running tasks", JOptionPane.YES_NO_OPTION);
            if (ans == JOptionPane.YES_OPTION) {
                for (Task t : tasks) {
                    t.cancel(true);
                }
                tasks.clear();
            } else {
                return;
            }
        }

        this.expContext = context.getExperimentContext();
        if (!arePathsForExpOk()) {
            return;
        }

        RawFileConverter converter = new RawFileConverter();

        msg = converter.collectConversionParams(expContext.getRawDir(), expContext.getCacheDir());
        if (msg != null && msg.length() > 0) {
            JOptionPane.showMessageDialog(this, "<html>" + msg + "</html>");
        } else {
            conv = converter.getConversion();
            if (conv != null) {
                for (Conversion con : conv) {
                    if (con.getEnd() > con.getStart() && con.getStart() >= 0) {

                        msg = "Converting " + con.getType() + " for flows " + con.getStart() + "-" + con.getEnd() + " (" + expContext.getChipType() + ", " + expContext.getNrcols() + "x" + expContext.getNrrows() + ")";
                        int secs = 80;
                        if (expContext.is316()) {
                            secs = 240;
                        } else if (expContext.is318()) {
                            secs = 600;
                        }
                        GuiUtils.showNonModalMsg(msg, false, secs);
                        ProgressHandle progress = ProgressHandleFactory.createHandle(msg);
                        // progress = new ProgressFrame(this, this.getIcon(),msg);
                        ConverterTask task = new ConverterTask(listener, progress, con, expContext.getRawDir(), expContext.getCacheDir());
                        tasks.add(task);
                        task.execute();
                    }
                }
            }
        }
    }

    public boolean arePathsForExpOk() {
        String errmsg = context.getContextInfo(false);
        if (errmsg != null && errmsg.length() > 0) {

            // GuiUtils.showNonModelMsg("<html>" + errmsg + "<html>", true);
            errmsg = "<b>There is a problem with the paths:</b><br>" + errmsg;
            //  JOptionPane.showMessageDialog(this, "<html>" + errmsg + "<html>");
            String[] options = {"Show more detailed info", "Open configuration", "Cancel"};
            int ans = JOptionPane.showOptionDialog(this, "<html>" + errmsg + "<html>", "Path problem", JOptionPane.NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, options, options[0]);
            //  p("Got answer: "+ans);
            if (ans == 1) {
                OptionsDisplayer.getDefault().open("TorrentScoutOptions/TorrentScoutSettings");
            } else if (ans == 0) {
                FolderAction act = new FolderAction(this.expContext);
                act.actionPerformed(null);
            }
            return false;


        }
        return true;
    }

    private class SubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            getLastResult();
        }
    }

    private void getLastResult() {
        final Collection<? extends ExperimentContext> items = dataClassResults.allInstances();
        if (!items.isEmpty()) {
            ExperimentContext data = null;
            Iterator<? extends ExperimentContext> it = items.iterator();
            while (it.hasNext()) {
                data = it.next();
                //  p("Got result: " + data);
            }
            // p("SubscriberListener Got result:" + data);
            update(data);
        }
    }

    /** create experiment context */
    private void update(ExperimentContext exp) {
        p("Got exp context: " + exp);
        expContext = exp;
        updateInfo();
    }

    @Override
    public Action[] getActions() {
        Action[] ac = OpenWindowAction.getActions(this);
        Action[] actions = new Action[ac.length + 1];
        System.arraycopy(ac, 0, actions, 0, ac.length);
        actions[actions.length - 1] = new OptionAction();
        return actions;
    }

    private void loadPreferences() {
        p("Loading startup preferences");
        // TODO read settings and initialize GUI
        // Example:        
        // someCheckBox.setSelected(Preferences.userNodeForPackage(IonogramOptionsPanel.class).getBoolean("someFlag", false));
        // or for org.openide.util with API spec. version >= 7.4:
        // someCheckBox.setSelected(NbPreferences.forModule(IonogramOptionsPanel.class).getBoolean("someFlag", false));
        // or:
        // someTextField.setText(SomeSystemOption.getDefault().getSomeStringProperty());
        Preferences p = Preferences.userNodeForPackage(com.iontorrent.main.options.TorrentScoutSettingsPanel.class);

        skin = p.get("skin", "aluminium");
        if (skin == null || skin.length() < 1) {
            skin = "aluminium";
        }
        context = GlobalContext.getContext();
        if (context == null) {
            context = new GlobalContext();
//            String default_context = p.get("default_context", "default");
//            context.setContext(default_context);
//            GlobalContext.setContext(context);
            LookupUtils.publish(globalContent, context);

        }

        GetDefaultSite task = new GetDefaultSite(this);
        task.execute();

    }

    private class GetDefaultSite extends Task {

        public GetDefaultSite(TaskListener tlistener) {
            super(tlistener);

        }

        @Override
        public Void doInBackground() {
            p("About to select default site");
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }

            selectDefaultSite();
            LookupUtils.publish(globalContent, context);
            setFormatter();
            return null;
        }

        public boolean isSuccess() {
            return default_rule != null;
        }
    }

    private void selectDefaultSite() {
        context = GlobalContext.getContext();
        FolderManager manager = FolderManager.getManager();
        default_rule = manager.setDefaultRule();

        if (default_rule == null || default_rule.length() < 1 || default_rule.startsWith("offline")) {
            showOfflineComponent();
        }
    }

    private void showExperimentViewer() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                TopComponent tc = (TopComponent) WindowManager.getDefault().findTopComponent("ExperimentViewerTopComponent");
                if (tc != null) {
                    tc.requestActive();
                    tc.requestVisible();
                }
            }
        });

    }

    private void showOfflineComponent() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                TopComponent tc = (TopComponent) WindowManager.getDefault().findTopComponent("OfflineTopComponent");
                if (tc != null) {
                    tc.requestActive();
                    tc.requestVisible();
                }
            }
        });

    }

    public class OptionAction extends AbstractAction {

        public OptionAction() {
            super("Options");

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OptionsDisplayer.getDefault().open("TorrentScoutOptions/TorrentScoutSettings");
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {


        btnCheck = new javax.swing.JButton(".DAT file cache");
        btnCheck.setToolTipText("This is only needed for old, non-region based .dat file formats!");
        btnOptions = new javax.swing.JButton("Server Configuration");
        //    btnFolder = new javax.swing.JButton("Show folder info");


        btnCheck.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCheckActionPerformed(evt);
            }
        });
//        btnFolder.addActionListener(new java.awt.event.ActionListener() {
//
//            @Override
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                FolderAction ac = new FolderAction(expContext);
//                ac.actionPerformed(evt);
//            }
//        });

        btnOptions.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOptionsActionPerformed(evt);
            }
        });


        this.setLayout(new BorderLayout());
        JPanel top = new JPanel();
        top.setLayout(new GridLayout(2, 1));
        top.add(btnCheck);
        top.add(btnOptions);
        //  top.add(btnFolder);
        this.add("North", top);
        //this.add("Center", new );

    }// </editor-fold>                        

    private void setStatus(String msg) {
        Message message = StatusDisplayer.getDefault().setStatusText("<html>" + msg + "</html>", StatusDisplayer.IMPORTANCE_FIND_OR_REPLACE);
        message.clear(30000);
    }

    private void setStatusWarning(String msg) {
        Message message = StatusDisplayer.getDefault().setStatusText("<html>" + msg + "</html>", StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        message.clear(60000);
    }

    private void btnCheckActionPerformed(java.awt.event.ActionEvent evt) {
        checkAndMaybeConvertFiles(this);
    }

    private class UpdateTask extends Task {

        public UpdateTask(TaskListener tlistener) {
            super(tlistener);
        }

        @Override
        public Void doInBackground() {
            updateInfo();
            return null;
        }

        public boolean isSuccess() {
            return true;
        }
    }

    @Override
    public void taskDone(Task t) {
        p("Task " + t + " is done");
        setCursor(null);

        //progress.stop();
        if (t instanceof GetDefaultSite) {
            GetDefaultSite f = (GetDefaultSite) t;
            if (!t.isSuccess()) {
            } else {
                context.setContext(default_rule);

                if (context.getExperimentContext() != null) {
                    String errmsg = context.getContextInfo(false);

                    if (errmsg != null && errmsg.length() > 0) {
                        if (lastmsg == null || !lastmsg.equalsIgnoreCase(errmsg)) {
                            JOptionPane.showMessageDialog(this, "<html>OptionsPanel<br>" + errmsg + "<html>");
                        }
                        lastmsg = errmsg;
                    }
                }
                GlobalContext.setContext(context);
                globalContent.remove(context);
                LookupUtils.publish(globalContent, context);

            }
            updateInfo();
        } else if (t instanceof ConverterTask) {
            tasks.remove(t);
            //progress.stop();
            ConverterTask task = (ConverterTask) t;
            Conversion conv = task.getConversion();
            String msg = task.getMsg();
            if (task.getThrowable() != null) {
                JOptionPane.showMessageDialog(this, "Got an exception from the conversion task:\n" + task.getThrowable() + "\nmsg: " + msg);
            } else if (msg != null && msg.trim().length() > 0) {
                JOptionPane.showMessageDialog(this, "<html>Msg from conversion task: " + msg + "</html>");
            } else {
//                RawFileConverter converter = new RawFileConverter();
//                msg = converter.showConversion(expContext.getRawDir(), expContext.getCacheDir());
//                if (msg != null && msg.length() > 0) {
//                    JOptionPane.showMessageDialog(this, "<html>" + msg + "</html>");
//                }
            }

        }

    }

    private void btnOptionsActionPerformed(java.awt.event.ActionEvent evt) {
        OptionsDisplayer.getDefault().open("TorrentScoutOptions");
    }
    // Variables declaration - do not modify                     
    private javax.swing.JButton btnCheck;
    private javax.swing.JButton btnOptions;
    private javax.swing.JButton btnFolder;

    // End of variables declaration                   
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
        loadPreferences();
        setLAF(skin);
        setFormatter();
    }

    private static void setFormatter() {
        final ShortFormatter formatter = ShortFormatter.getFormatter();
        Logger rootLogger = Logger.getLogger(TorrentScoutStartupTopComponent.class.getName());

        while (rootLogger.getParent() != null) {
            rootLogger = rootLogger.getParent();
        }

        for (final Handler handler : rootLogger.getHandlers()) {
            handler.setFormatter(formatter);
        }
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

    private static void p(String msg) {
        Logger.getLogger(TorrentScoutStartupTopComponent.class.getName()).log(Level.INFO, msg);
    }
}
