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

import com.iontorrent.torrentscout.explorer.fit.HistoView;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.guiutils.netbeans.OpenWindowAction;
import com.iontorrent.rawdataaccess.wells.BitMask;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.utils.io.FileTools;
import com.iontorrent.wellmodel.WellCoordinate;
import java.util.Collection;
import java.util.Iterator;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JOptionPane;
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
@ConvertAsProperties(dtd = "-//com.iontorrent.torrentscout.explorer//Fit//EN",
autostore = false)
@TopComponent.Description(preferredID = "FitTopComponent",
iconBase = "com/iontorrent/torrentscout/explorer/chart.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "left_editor_mode", openAtStartup = false)
@ActionID(category = "Window", id = "com.iontorrent.torrentscout.explorer.FitTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_FitAction",
preferredID = "FitTopComponent")
public final class FitTopComponent extends TopComponent {

    ExperimentContext expContext;
    private transient final Lookup.Result<ExperimentContext> expContextResults =
            LookupUtils.getSubscriber(ExperimentContext.class, new SubscriberListener());
    ExplorerContext maincont;
    BitMask selectedmask;
    CompleteMaskPanel mp;
    HistoView histoview;

    public FitTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(FitTopComponent.class, "CTL_FitTopComponent"));
        setToolTipText(NbBundle.getMessage(FitTopComponent.class, "HINT_FitTopComponent"));

        addGui();
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
    }

    
    
    
    public void addHistoPanel() {
        if (histoview != null) {
            remove(histoview);
        }
        maincont = ExplorerContext.getCurContext(expContext);
        getUserPreferences();
        histoview = new HistoView(this.maincont);
        add("Center", histoview);

    }

//    public void changeMask() {
//        if (mp != null) {
//            panMask.remove(mp);
//        }
//        p("Got selected mask: "+selectedmask);
//        mp = new CompleteMaskPanel(expContext, selectedmask);
//        mp.setColors(new Color[]{Color.black, Color.blue, Color.green, Color.yellow, Color.orange});
//        mp.setName("Mask");
//        panMask.add("Center", mp);
//        mp.refresh();
//    }
    private void addGui() {

        //  panMask.removeAll();

        if (expContext == null) {
            expContext = GlobalContext.getContext().getExperimentContext();
        }
        if (expContext == null) {
            // GuiUtils.showNonModalMsg("Got no experiment context");
            return;
        }

        maincont = ExplorerContext.getCurContext(expContext);
        getUserPreferences();
        if (expContext.getWellContext().getCoordinate() == null) {
            expContext.getWellContext().setCoordinate(new WellCoordinate(100, 100));
        }
        addHistoPanel();


        //   addMaskSelection();
        //  rasterViewUpdate(true);
    }

//    public void rasterViewUpdate(boolean load) {
//        RasterView raster = new RasterView(maincont, load);
//        this.panImage.removeAll();
//        p("Rasterviewupdate called");
//        raster.update(load);
//        panImage.add(raster);
//    }
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
        if (result == null) {
            result = GlobalContext.getContext().getExperimentContext();
        }
        if (result != null) {
            this.expContext = result;
            maincont = ExplorerContext.getCurContext(result);
            getUserPreferences();
            addGui();
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
        // rasterViewUpdate(true);
        addGui();
    }

    @Override
    public Action[] getActions() {
        return OpenWindowAction.getActions(this);
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

    private void p(String msg) {
        System.out.println("Fit: " + msg);
    }

    private void setStatus(String msg) {
        Message message = StatusDisplayer.getDefault().setStatusText("<html>Fit: " + msg + "</html>", StatusDisplayer.IMPORTANCE_FIND_OR_REPLACE);
        message.clear(30000);
    }

    private void setStatusWarning(String msg) {
        Message message = StatusDisplayer.getDefault().setStatusText("<html>Fit: " + msg + "</html>", StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        message.clear(120000);
    }

    private void setStatusError(String msg) {
        Message message = StatusDisplayer.getDefault().setStatusText("<html>Fit: " + msg + "</html>", StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        message.clear(240000);
    }
}
