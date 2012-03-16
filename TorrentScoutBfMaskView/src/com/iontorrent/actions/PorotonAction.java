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
import com.iontorrent.guiutils.GuiUtils;
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
id = "com.iontorrent.actions.PorotonAction")
@ActionRegistration(iconBase = "com/iontorrent/actions/chip_bb_16.png",
displayName = "Pick a Proton Block")
@ActionReferences({
    @ActionReference(path = "Menu/Pick Region", position = 1),    
    @ActionReference(path = "Toolbars/Pick", position = 1),
    @ActionReference(path = "Shortcuts", name = "A-P")
})
@Messages("CTL_PorotonAction=Proton Block")
public final class PorotonAction implements ActionListener {
    
     @Override
    public String toString() {
        return "Pick a block in a Proton experiment";
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        
        TopComponent tc = (TopComponent) WindowManager.getDefault().findTopComponent("TorrentScoutCompositeViewTopComponent");       
        openComponent(tc, true);
        
        if (tc == null) GuiUtils.showNonModalMsg("I could not find this component");
        tc = (TopComponent) WindowManager.getDefault().findTopComponent("WholeChipViewTopComponent");       
        openComponent(tc, false);
    }
    protected static void openComponent(TopComponent tc, boolean attention) {
        if (tc != null) {
            if( !tc.isOpened()) tc.open();
         //   else p("CompView is already open");
            tc.requestActive();
            tc.requestVisible();
            tc.toFront();
            if (attention) tc.requestAttention(true);
        }
        
    }
    private static void p(String msg) {
//  System.out.println("ProtonAction: "+msg);
       
    }
}
