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
package com.iontorrent.seqview;

import com.iontorrent.guiutils.netbeans.OpenWindowAction;
import com.iontorrent.sequenceloading.SequenceLoader;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.utils.io.FileUtils;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.iontorrent.seq.Read;
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

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//com.iontorrent.seqview//TorrentScoutAlignment//EN",
autostore = false)
@TopComponent.Description(preferredID = "TorrentScoutAlignmentTopComponent",
iconBase = "com/iontorrent/seqview/msa.gif",
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "right_editor_mode", openAtStartup = false)
@ActionID(category = "Window", id = "com.iontorrent.seqview.TorrentScoutAlignmentTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_TorrentScoutAlignmentAction",
preferredID = "TorrentScoutAlignmentTopComponent")
public final class TorrentScoutAlignmentTopComponent extends TopComponent implements TaskListener {

    private transient final Lookup.Result<WellContext> dataClassWellSelection =
            LookupUtils.getSubscriber(WellContext.class, new WellSubscriberListener());
    private transient final Lookup.Result<WellCoordinate> dataClassWellCoordinate =
            LookupUtils.getSubscriber(WellCoordinate.class, new WellCoordinateSubscriberListener());
    private transient final Lookup.Result<ExperimentContext> expContextResults =
            LookupUtils.getSubscriber(ExperimentContext.class, new ExpSubscriberListener());
    private WellContext cur_context;
    private ExperimentContext expContext;
    private AlignmentPanel alPanel;
    private ProgressHandle progress;

    public TorrentScoutAlignmentTopComponent() {
        initComponents();
        setLayout(new BorderLayout());
        setName(NbBundle.getMessage(TorrentScoutAlignmentTopComponent.class, "CTL_TorrentScoutAlignmentTopComponent"));
    setToolTipText(NbBundle.getMessage(TorrentScoutAlignmentTopComponent.class, "HINT_TorrentScoutAlignmentTopComponent"));
        alPanel = new AlignmentPanel();
        add("Center", alPanel);
        
    }

    private class ExpSubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            getLatestExperimentContext();
        }
    }

    private void getLatestExperimentContext() {
       expContext = GlobalContext.getContext().getExperimentContext();
       if (expContext != null) {
           cur_context = expContext.getWellContext();
       
       //alPanel.clear(expContext);
       }
        //   else p("No exp context in list");
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
            TopComponent win = new TorrentScoutAlignmentTopComponent();
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
            update();
        }
        return false;
    }

    private class WellCoordinateSubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            getLatestCoordinate();
            update();

        }
    }

    private void err(String msg) {
        Logger.getLogger(TorrentScoutAlignmentTopComponent.class.getName()).log(Level.SEVERE, msg);
    }

    private void p(String s) {
//  System.out.println("TorrentScoutAlignmentTopComponent:" + s);
    }

    private void getLatestCoordinate() {
    //    p("Getting latest well coord");
        this.getLatestExperimentContext();
        
    }

    @Override
    public void taskDone(Task t) {
        p("Task " + t + " is done");
        if (t.isSuccess()) {
            update();
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

    private void update() {
        
//        if (alPanel != null) {
//            remove(alPanel);
//        }
        if (expContext == null) {
            this.getLatestExperimentContext();
        }
        
        if (expContext == null) {
            setStatusWarning("Got no experiment context, don't know where the sff and sam files are - creating dummy context");
            expContext = ExperimentContext.createFake(GlobalContext.getContext());
            
            //return;
        }
        else if (!FileUtils.exists(this.cur_context.getCacheDirectory())) {
            setStatusWarning("Cannot access cache dir "+this.cur_context.getCacheDirectory());
        }
        cur_context = expContext.getWellContext();
        this.getLatestCoordinate();
        WellCoordinate coord = cur_context.getCoordinate();
        if (coord == null) {        
            err("Got no coordinate");
            return;
        }
        SequenceLoader loader = SequenceLoader.getSequenceLoader(this.expContext);

        Read read = loader.getRead(coord.getCol(), coord.getRow(),  this);
        String error = loader.getMsg();
        if (read == null && error != null) {
            this.setStatusWarning("Got no read: " + error);
        }
        
        alPanel.update(this.expContext, cur_context, read, error);

     //   p("Updated alignment panel");
        alPanel.repaint();
        alPanel.paintImmediately(0, 0, 800, 800);
        // requestActive();
        //  this.requestVisible();
        // this.toFront();            
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        this.getLatestExperimentContext();
        this.getLatestContext();
        this.getLatestCoordinate();
        this.update();
        // TODO add custom code on component opening
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
