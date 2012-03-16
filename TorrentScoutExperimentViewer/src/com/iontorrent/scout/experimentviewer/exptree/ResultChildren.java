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

import com.iontorrent.expmodel.ExperimentContext;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author Chantal Roth
 */
public class ResultChildren extends Index.ArrayChildren {

    private MyResult res;
    ExperimentContext exp;
    public ResultChildren(MyResult res) {
        this.res = res;
        exp = res.createContext();
       
    }
 private void p(String s) {
//  System.out.println("ResultChildren: "+s);
    }
 
 
    @Override
    protected java.util.List<Node> initCollection() {
        ArrayList childrenNodes = new ArrayList();
        try {
            if (exp.isChipBB()) {
                // ADD BACK ONCE THUMBNAILS RESULTS ARE ALSO THERE
//                BeanNode node = new BeanNode("Thumbnails");            
//                node.setDisplayName("Thumbnails");
//                node.setShortDescription("Thumbnails view of whole chip");
//                childrenNodes.add(node);
            }
            if (res.getTfMetrics() != null) {
                BeanNode node = new BeanNode(res.getTfMetrics());            
                node.setDisplayName("Test Frag Metrics");
                node.setShortDescription(res.getTfMetrics().getSequence());
                childrenNodes.add(node);
            }
            
            if (res.getAnalysisMetrics() != null) {
                BeanNode node = new BeanNode(res.getAnalysisMetrics());
                node.setDisplayName("Analysis metrics");
                node.setShortDescription(res.getAnalysisMetrics().getLibLive()+" live beads");
                childrenNodes.add(node);
            }
            
            if (res.getLibMetrics() != null) {
                BeanNode node = new BeanNode(res.getLibMetrics());
                node.setDisplayName("Library metrics");
                node.setShortDescription(res.getLibMetrics().getTotalNumReads()+" total nr reads, genome "+res.getLibMetrics().getGenome());
                childrenNodes.add(node);
            }
            
            if (res.getQaulityMetrics() != null) {
                BeanNode node = new BeanNode(res.getQaulityMetrics());
                node.setDisplayName("Quality metrics");
                node.setShortDescription(res.getQaulityMetrics().getQ20Reads()+" Q20 reads");
                childrenNodes.add(node);
            }
            
        } catch (IntrospectionException ex1) {
            Exceptions.printStackTrace(ex1);
        }
        
    return childrenNodes ;
    }
}
