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
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author Chantal Roth
 */
public class ExperimentChildren extends Index.ArrayChildren {

    private RundbExperiment exp;
    private MyRig rig;

    public ExperimentChildren(RundbExperiment exp, MyRig rig) {
        this.exp = exp;
        this.rig = rig;

    }

    private void p(String s) {
       // System.out.println("RundbExperiment: " + s);
    }

    @Override
    protected java.util.List<Node> initCollection() {
        ArrayList childrenNodes = new ArrayList();
        if (exp.getRundbResultsCollection() == null) {
            p("RundbExperiment " + exp + " has no children");
            return null;
        }

        // p("RundbExperiment "+exp+" has "+exp.getRundbResultsCollection().size()+" children");
        for (RundbResults ex : exp.getRundbResultsCollection()) {
            try {
                ResultNode node = new ResultNode(new MyResult(ex, rig));
                // filter?
                if (pass(node)) {
                    childrenNodes.add(node);
                }
            } catch (IntrospectionException ex1) {
                Exceptions.printStackTrace(ex1);
            }
        }


        return childrenNodes;
    }

    private boolean pass(ResultNode node) {
        return NodeFilter.passes(node.getResult(), ExpNode.selectedFilters);
    }
}
