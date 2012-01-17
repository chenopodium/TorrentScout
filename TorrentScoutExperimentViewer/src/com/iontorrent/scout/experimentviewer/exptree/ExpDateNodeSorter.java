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


import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.AbstractNode;
/**
 *
 * @author Chantal Roth
 */
public class ExpDateNodeSorter extends NodeSorter {
  
    public ExpDateNodeSorter() {
        super("Sort by date", "Newest experiments first");
       
    }

    @Override
    public int compare(Object o1, Object o2) {
        ExpNode e1 = (ExpNode)o1;
        ExpNode e2 = (ExpNode)o2;
        return e2.getExp().getDate().compareTo( e1.getExp().getDate());
    }


    
}
