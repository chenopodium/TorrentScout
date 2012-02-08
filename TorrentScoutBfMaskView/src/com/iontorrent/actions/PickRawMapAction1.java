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
package com.iontorrent.actions;

import com.iontorrent.main.FolderAction;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.GlobalContext;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(category = "Pick Region",
id = "com.iontorrent.actions.PickRawMapAction1")
@ActionRegistration(iconBase = "com/iontorrent/actions/chip_16.png",
displayName = "Whole chip raw heat map")
@ActionReferences({
    @ActionReference(path = "Menu/Pick Region", position = 2),    
    @ActionReference(path = "Toolbars/Pick", position = 2),
    @ActionReference(path = "Shortcuts", name = "C-B")
})
@Messages("CTL_PickRawMapAction1=Raw Map")
public final class PickRawMapAction1 implements ActionListener {
      @Override
    public String toString() {
        return "View raw chip heat map of any frame and select wells or a region";
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        TopComponent tc = (TopComponent) WindowManager.getDefault().findTopComponent("WholeChipViewTopComponent");       
        openComponent(tc, true);
        if (GlobalContext.getContext().getExperimentContext() != null && GlobalContext.getContext().getExperimentContext().hasBfMask()) {
             tc = (TopComponent) WindowManager.getDefault().findTopComponent("WellTableTopComponent");       
             openComponent(tc, false);
        }
        else {
           tc = (TopComponent) WindowManager.getDefault().findTopComponent("ProcessTopComponent");       
           openComponent(tc, false); 
           tc = (TopComponent) WindowManager.getDefault().findTopComponent("FitTopComponent");       
           openComponent(tc, false);
        }
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
}
