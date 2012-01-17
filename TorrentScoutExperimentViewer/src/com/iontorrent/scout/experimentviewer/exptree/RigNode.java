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

package com.iontorrent.scout.experimentviewer.exptree;

import java.beans.IntrospectionException;
import javax.swing.Action;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.BeanNode;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Chantal Roth
 */
public class RigNode extends BeanNode{
 /** Creates a new instance of CategoryNode */
    MyRig rig;
    public RigNode( MyRig rig ) throws IntrospectionException {
        super( rig, new RigChildren(rig), Lookups.singleton(rig) );
        setDisplayName(rig.getName());
        this.rig = rig;
        setShortDescription(rig.getName());
        if (rig.getLocationId() != null) setShortDescription(rig.getName()+", "+rig.getLocationId().getName());
        else if (rig.getComments()!= null) setShortDescription(rig.getName()+", "+rig.getComments());
        
        setIconBaseWithExtension("com/iontorrent/scout/experimentviewer/exptree/server-chart.png");
    }
  
      
    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
                    SystemAction.get(PropertiesAction.class)};
    }

    public void setVisible(boolean b) {
         rig.setVisible(b);
    }
   
}
