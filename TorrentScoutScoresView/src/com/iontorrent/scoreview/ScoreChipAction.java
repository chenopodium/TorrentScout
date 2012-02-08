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
package com.iontorrent.scoreview;

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

@ActionID(category = "Find",
id = "com.iontorrent.actions.ScoreChipAction")
@ActionRegistration(iconBase = "com/iontorrent/scoreview/eye.png",
displayName = "Find Reads (by nr matches, q17len, indels etc)")
@ActionReferences({
    @ActionReference(path = "Menu/Find", position = 1001),
    @ActionReference(path = "Toolbars/Find", position = 1001)
})
@Messages("CTL_ScoreChipAction=Find Reads")
public final class ScoreChipAction implements ActionListener {

    @Override
    public String toString() {
        return "Find reads based on scores such as matches, indels, Q17 values etc. Requires .bam and ideally also a .sff file";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TopComponent tc = (TopComponent) WindowManager.getDefault().findTopComponent("TorrentScoutScoresViewTopComponent");
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
            if (attention) {
                tc.requestAttention(true);
            }
        }
    }
}
