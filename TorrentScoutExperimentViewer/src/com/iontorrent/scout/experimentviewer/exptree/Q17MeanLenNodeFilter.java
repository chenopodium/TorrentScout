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
public class Q17MeanLenNodeFilter extends NodeFilter {

    double cutoff;
    
    public Q17MeanLenNodeFilter(double cutoff) {
        this(cutoff, false);
    }
    public Q17MeanLenNodeFilter(double cutoff, boolean not) {
        super("Q17 mean read len", "Only accept results with Q17 mean read len of at least  " + cutoff, not);
        this.cutoff = cutoff;
        if (not) {
            name = "NOT "+cutoff+" results";
            description =  "Only accept results with Q17 mean read len of at most " + cutoff;
        }
    }
@Override
    public String getRelevantValue(Object node) {
        MyResult n = (MyResult) node;
        if (n.getQaulityMetrics() == null) return "?";
        return  ""+n.getQaulityMetrics().getQ17MeanReadLength();
    }
    @Override
    public boolean passes(Object node) {
        MyResult n = (MyResult) node;
        if (n.getQaulityMetrics() == null) return not;
        
        double value = n.getQaulityMetrics().getQ17MeanReadLength();
        boolean b= value >= cutoff;
        if (!not) return b;
        else return !b;
        
    }
    @Override
    public Class getNodeClass() {
        return MyResult.class;
    }
}
