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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(category = "Process",
id = "com.iontorrent.actions.MaskAction")
@ActionRegistration(iconBase = "com/iontorrent/torrentscout/explorer/mask.png",
displayName = "Edit Masks")
@ActionReferences({
    @ActionReference(path = "Menu/Process", position = 700),    
    @ActionReference(path = "Toolbars/Process", position = 700)
})
@Messages("CTL_MaskAction=Edit Masks")
public final class MaskAction implements ActionListener {
      @Override
    public String toString() {
        return "Compute and edit with masks (addition, subtraction, xor, shift etc)";
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        TopComponent tc = (TopComponent) WindowManager.getDefault().findTopComponent("MaskEditorTopComponent");       
        openComponent(tc, true);
    }
    protected static void openComponent(TopComponent tc, boolean attention) {
        if (tc != null) {
            if( !tc.isOpened()) tc.open();
            tc.requestActive();
            tc.requestVisible();
            if (attention) tc.requestAttention(true);
        }
    }
}
