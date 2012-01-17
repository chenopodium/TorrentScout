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
import java.util.Collections;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author Chantal Roth
 */
public class RigChildren extends Index.ArrayChildren {

    private MyRig rig;

    public RigChildren(MyRig rig) {
        this.rig = rig;
    }

    private void p(String s) {
        System.out.println("RigChildren: " + s);
    }

    @Override
    protected java.util.List<Node> initCollection() {
        ArrayList childrenNodes = new ArrayList();
        //p("Rig "+rig+" has "+rig.getExperiments().size()+" children");
       if (!rig.isVisible()) return null;
        for (RundbExperiment ex : rig.getExperiments()) {
            try {
                ExpNode node = new ExpNode(ex, rig);
                if (pass(node)) {                    
                   // Collection <RundbResults> col = ex.getRundbResultsCollection();
                   // if (col != null && col.size()>0) 
                    //if (hasOkRuns(ex, rig))  
                    childrenNodes.add(node);
                }
            } catch (IntrospectionException ex1) {
                Exceptions.printStackTrace(ex1);
            }
        }
        Collections.sort(childrenNodes, new ExpDateNodeSorter());

        return childrenNodes;
    }
//    private boolean hasOkRuns(RundbExperiment exp, MyRig rig) {
//        // p("RundbExperiment "+exp+" has "+exp.getRundbResultsCollection().size()+" children");
//        for (RundbResults ex : exp.getRundbResultsCollection()) {
//            try {
//                ResultNode node = new ResultNode(new MyResult(ex, rig));
//                // filter?
//                if (pass(node.getResult())) return true;
//            } catch (IntrospectionException ex1) {
//                Exceptions.printStackTrace(ex1);
//            }
//        }
//        return false;
//    }
    private boolean pass(Object node) {
        return NodeFilter.passes(node, ExpNode.selectedFilters);
    }
}
