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
package com.iontorrent.genometochip;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(category = "Find",
id = "com.iontorrent.actions.GenomeToChipAction")
@ActionRegistration(iconBase = "com/iontorrent/genometochip/chromo_ss.gif",
displayName = "Genome to Chip (locate reads on genome)")
@ActionReferences({
    @ActionReference(path = "Menu/Find", position = 1000),    
    @ActionReference(path = "Toolbars/Find", position = 1000)
})
@Messages("CTL_GenomeToChipAction=Genome to Chip")
public final class GenomeToChipAction implements ActionListener {
    
     @Override
    public String toString() {
        return "Find reads mapped to a particular location on the genome. Requires a .bam file";
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        TopComponent tc = (TopComponent) WindowManager.getDefault().findTopComponent("TorrentScoutGenomeToChipTopComponent");       
        openComponent(tc, true);
         tc = (TopComponent) WindowManager.getDefault().findTopComponent("WellTableTopComponent");       
        openComponent(tc, false);
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
