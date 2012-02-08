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
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "File",
id = "com.iontorrent.scout.actions.ConfigureAction")
@ActionRegistration(iconBase = "com/iontorrent/scout/offline/system-run-3.png",
displayName = "Configure the databases, raw and results directories")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 2),
    @ActionReference(path = "Toolbars/File", position = 2),
    @ActionReference(path = "Shortcuts", name = "C-C")
})
@Messages("CTL_ConfigureAction=Configure Torrent Scout")
public final class ConfigureAction  implements ActionListener {
    
    @Override
    public void actionPerformed(ActionEvent e) {
       OptionsDisplayer.getDefault().open("TorrentScoutOptions/TorrentScoutSettings");
    }
  

    @Override
    public String toString() {
        return "Configure the database and results/raw directory paths";
    }
  
}
