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
import javax.swing.JOptionPane;

/**
 *
 * @author Chantal Roth
 */
public class ExpNamesNodeFilter extends NodeFilter {

    String runname;
    
    public ExpNamesNodeFilter(String status) {
        this(status, false);
    }
    public ExpNamesNodeFilter(String status, boolean not) {
        super("Filter experiments by name", "Only accept experiments whose name contains a given string", not);
        this.runname = status.trim().toLowerCase();
        if (not) {
            name = "NOT "+status+" experiments";
            description =  "Only accept experiments where the name does  NOT contain " + status;
        }
    }
    @Override
    public void askForInput() {
        // by default, do nothing
        runname = JOptionPane.showInputDialog(null, "The experiments name should contain:");
        if (runname == null) runname = "";
        runname = runname.toLowerCase().trim();
    }

      @Override
    public String getRelevantValue(Object node) {
        RundbExperiment n = (RundbExperiment) node;
        return  n.getExpName();
    }
      
    @Override
    public boolean passes(Object node) {
        RundbExperiment n = (RundbExperiment) node;
        
        String s = n.getExpName();
        if (s == null) {
            return not;
        } else {
            boolean b= s.toLowerCase().indexOf(runname)>-1;
            if (!not) return b;
            else return !b;
        }
    }
    @Override
    public Class getNodeClass() {
        return RundbExperiment.class;
    }
}
