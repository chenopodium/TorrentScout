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
package com.iontorrent.scout.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(category = "File",
id = "com.iontorrent.scout.actions.BrowseAction")
@ActionRegistration(iconBase = "com/iontorrent/scout/experimentviewer/database-connect.png",
displayName = "Browse DB and pick an experiment/result")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 2),
    @ActionReference(path = "Toolbars/File", position = 2),
    @ActionReference(path = "Shortcuts", name = "C-D")
})
@Messages("CTL_BrowseAction=Browse Experiment DB")
public final class BrowseAction extends SystemAction implements ActionListener {
    
    @Override
    public void actionPerformed(ActionEvent e) {
        TopComponent tc = (TopComponent) WindowManager.getDefault().findTopComponent("ExperimentViewerTopComponent");       
        openComponent(tc, true);
    }
    protected static void openComponent(TopComponent tc, boolean attention) {
        if (tc != null) {
            if( !tc.isOpened()) {
                tc.open();
            }
            tc.requestActive();
            tc.requestVisible();
            tc.toFront();
            if (attention) tc.requestAttention(true);
        }
        else {
            System.err.println("BrowseAction: Component is null");
        }
    }

    @Override
    public String toString() {
        return "Browse the database and pick a result to view";
    }
    @Override
    public String getName() {
       return "Browse";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
