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

import javax.swing.JOptionPane;

/**
 *
 * @author Chantal Roth
 */
public class RunNamesNodeFilter extends NodeFilter {

    String runname;
    
    public RunNamesNodeFilter(String status) {
        this(status, false);
    }
    public RunNamesNodeFilter(String status, boolean not) {
        super("Filter results by name", "Only accept results whose name contains a given string", not);
        this.runname = status.trim().toLowerCase();
        if (not) {
            name = "NOT "+status+" results";
            description =  "Only accept results wher ethe name does  NOT contain " + status;
        }
    }
    @Override
    public void askForInput() {
        // by default, do nothing
        runname = JOptionPane.showInputDialog(null, "The results name should contain:");
        if (runname == null) runname = "";
        runname = runname.toLowerCase().trim();
    }

      @Override
    public String getRelevantValue(Object node) {
        MyResult n = (MyResult) node;
        return  ""+n.getResultsName();
    }
    @Override
    public boolean passes(Object node) {
        MyResult n = (MyResult) node;
        String s = n.getResultsName();
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
        return MyResult.class;
    }
}
