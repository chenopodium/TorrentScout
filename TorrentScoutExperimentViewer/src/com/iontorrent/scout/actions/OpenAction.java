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

import com.iontorrent.main.FolderAction;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.GlobalContext;
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
id = "com.iontorrent.scout.actions.OpenAction")
@ActionRegistration(iconBase = "com/iontorrent/scout/offline/document-open-2.png",
displayName = "Open Experiment (pick folders)")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 3),
    @ActionReference(path = "Toolbars/File", position = 3),
    @ActionReference(path = "Shortcuts", name = "C-O"),
    @ActionReference(path = "Shortcuts", name = "A-O")
})
@Messages("CTL_OpenAction=Open Experiment")
public final class OpenAction extends SystemAction implements ActionListener {
    
    @Override
    public void actionPerformed(ActionEvent e) {
        TopComponent tc = (TopComponent) WindowManager.getDefault().findTopComponent("OfflineTopComponent");       
        openComponent(tc, true);
    }
    protected static void openComponent(TopComponent tc, boolean attention) {
        if (tc != null) {
            if( !tc.isOpened()) tc.open();
            tc.requestActive();
            tc.requestVisible();
            tc.toFront();
            if (attention) tc.requestAttention(true);
        }
    }
    
    @Override
    public String toString() {
        return "Open experiment by picking folders with raw and results data";
    }
    @Override
    public String getName() {
       return "Open";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
