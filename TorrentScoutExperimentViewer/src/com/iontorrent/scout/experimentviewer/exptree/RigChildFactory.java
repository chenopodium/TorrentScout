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

import com.iontorrent.dbaccess.RundbExperiment;
import com.iontorrent.dbaccess.RundbResults;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author Chantal Roth
 */
public class RigChildFactory extends ChildFactory<MyRig>{
    private List<MyRig> resultList;

    static int rigcount=-1;
    
    public RigChildFactory(List<MyRig> resultList) {
        this.resultList = resultList;
        
    }
    public static int getRigCount() {
        return rigcount;
    }

    @Override
    protected boolean createKeys(List<MyRig> list) {
       // rigcount = 0;
         ArrayList<NodeFilter> filters = ExpNode.selectedFilters;
        if (resultList == null)   return true;
        
        for (MyRig rig : resultList) {
            boolean has = false;
            if (rig.isVisible()) {
                for (RundbExperiment ex : rig.getExperiments()) {                            
                    ExpNode node= null;
                  //  if (ex.getRundbResultsCollection() != null && hasOkRuns(ex, rig)) {
                        try {
                            node = new ExpNode(ex, rig);

                        } catch (IntrospectionException ex1) {
                            Exceptions.printStackTrace(ex1);
                        }
                        if (NodeFilter.passes(node.getExp(), filters) ) {
                            has = true;
                            if (node.getChildren() != null && node.getChildren().getNodesCount()>0)
                            break;
                        }
                  //  }
                }
            }
            if (has) {
                rigcount++;
                list.add(rig);
            }
        }
       
        return true;
    }

//    private boolean hasOkRuns(RundbExperiment exp, MyRig rig) {
//        // p("RundbExperiment "+exp+" has "+exp.getRundbResultsCollection().size()+" children");
//        for (RundbResults ex : exp.getRundbResultsCollection()) {
//            try {
//                ResultNode node = new ResultNode(new MyResult(ex, rig));
//                // filter?
//                if (pass(node)) return true;
//            } catch (IntrospectionException ex1) {
//                Exceptions.printStackTrace(ex1);
//            }
//        }
//        return false;
//    }
    private boolean pass(ResultNode node) {
        return NodeFilter.passes(node.getResult(), ExpNode.selectedFilters);
    }
    @Override
    protected Node createNodeForKey(MyRig c) {
        if (!c.isVisible()) return null;
        Node node = null;
        try {
            node= new RigNode(c);
        } catch (IntrospectionException ex) {
            Exceptions.printStackTrace(ex);
            Logger.getLogger(RigChildFactory.class.getName()).log(Level.SEVERE, "could not create RigNode", ex);
        }
       
        node.setDisplayName(c.getName());
       
        return node;
    }

}
