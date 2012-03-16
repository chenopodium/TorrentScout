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
package com.iontorrent.torrentscout.explorer.actions;

import com.iontorrent.main.FolderAction;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.guiutils.GuiUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(category = "Process",
id = "com.iontorrent.actions.ProcessAction")
@ActionRegistration(iconBase = "com/iontorrent/torrentscout/explorer/chart-curve-edit.png",
displayName = "Process Raw Data")
@ActionReferences({
    @ActionReference(path = "Menu/Process", position = 500),
    @ActionReference(path = "Toolbars/Process", position = 500)
})
@Messages("CTL_ProcessAction=Process")
public final class ProcessAction implements ActionListener {

    @Override
    public String toString() {
        return "View raw traces of an area, compute background subtraction and find areas with a signal in the time series";
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        GuiUtils.showNonModelMsg("Loading Components...", "Loading the Process, Fit and MaskEditor component...", false, 15);
        load();
    }

    private void load() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
               // close((TopComponent) WindowManager.getDefault().findTopComponent("TorrentScoutIonogramTopComponent"));
               // close((TopComponent) WindowManager.getDefault().findTopComponent("TorrentScoutAlignmentTopComponent"));
                //close((TopComponent) WindowManager.getDefault().findTopComponent("WellTableTopComponent"));
                close((TopComponent) WindowManager.getDefault().findTopComponent("TorrentScoutScoresViewTopComponent"));
                close((TopComponent) WindowManager.getDefault().findTopComponent("ExperimentViewerTopComponent"));
                close((TopComponent) WindowManager.getDefault().findTopComponent("OfflineTopComponent"));
                close((TopComponent) WindowManager.getDefault().findTopComponent("TorrentScoutMainPageTopComponent"));

                TopComponent tc = (TopComponent) WindowManager.getDefault().findTopComponent("FitTopComponent");
                openComponent(tc, false);
                tc = (TopComponent) WindowManager.getDefault().findTopComponent("MaskEditorTopComponent");
                openComponent(tc, false);
//                tc = (TopComponent) WindowManager.getDefault().findTopComponent("AutomateTopComponent");
//                openComponent(tc, false);

                tc = (TopComponent) WindowManager.getDefault().findTopComponent("ProcessTopComponent");
                openComponent(tc, true);
            }
        });

    }

    protected static void close(TopComponent tc) {
        if (tc != null) {
            if (tc.isOpened()) {
                tc.close();
            }
        }
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
}
