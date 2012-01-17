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

/**
 *
 * @author Chantal Roth
 */
public class ExpDateNodeFilter extends NodeFilter {
    int nrdays;
    
    public ExpDateNodeFilter(int nrdays) {
        super("last "+nrdays+" days", "Only accept experiments that are less than "+nrdays+" old");
        this.nrdays = nrdays;
    }
    

    @Override
    public boolean passes(Object node) {
        RundbExperiment exp = (RundbExperiment)node;
        return passes(exp);
    }

    public boolean passes(RundbExperiment exp) {
        
        if (exp.getDate() == null) return true;
        long exptime = exp.getDate().getTime();
        long now = System.currentTimeMillis();
        int deltadays = (int) ((now - exptime)/1000/86000);
        //p(deltadays+"  between now and "+ n.getExp().getDate()+", should be < "+nrdays);
        if (deltadays < nrdays) {
         //   p("Node "+n.getExp()+" passes");
            return true;
        }
        else return false;
    }

    @Override
    public Class getNodeClass() {
        return RundbExperiment.class;
    }
    
    
}
