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

/**
 *
 * @author Chantal Roth
 */
public class RunStatusNodeFilter extends NodeFilter {

    String status;
    
    public RunStatusNodeFilter(String status) {
        this(status, false);
    }
    public RunStatusNodeFilter(String status, boolean not) {
        super(status + " results", "Only accept results with status " + status, not);
        this.status = status.trim().toLowerCase();
        if (not) {
            name = "NOT "+status+" results";
            description =  "Only accept results with status that are NOT " + status;
        }
    }
      @Override
    public String getRelevantValue(Object node) {
        MyResult n = (MyResult) node;
        return  n.getStatus();
    }

    @Override
    public boolean passes(Object node) {
        MyResult n = (MyResult) node;
        String s = n.getStatus();
        if (s == null) {
            return not;
        } else {
            boolean b= s.toLowerCase().startsWith(status);
            if (!not) return b;
            else return !b;
        }
    }
    @Override
    public Class getNodeClass() {
        return MyResult.class;
    }
}
