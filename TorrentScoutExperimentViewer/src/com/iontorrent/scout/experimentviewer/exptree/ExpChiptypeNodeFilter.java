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
import org.openide.nodes.AbstractNode;

/**
 *
 * @author Chantal Roth
 */
public class ExpChiptypeNodeFilter extends NodeFilter {

    String type;

    public ExpChiptypeNodeFilter(String type) {
        this(type, false);
    }

    public ExpChiptypeNodeFilter(String type, boolean not) {
        super("Chip type " + type, "Only accept experiments that have chip type " + type + " old", not);
        this.type = type;
        if (not) {
            name = "Chip type NOT " + type;
            description = "Only accept experiments with chip type NOT " + type;
        }
    }

    @Override
    public boolean passes(Object node) {
        RundbExperiment n = (RundbExperiment) node;
        String t = n.getChipType();
        // p("Chip type  "+t);
        if (t != null && t.indexOf(type) > -1) {

            return !not;
        } else {
            return not;
        }
    }

    @Override
    public Class getNodeClass() {
        return RundbExperiment.class;
    }
}
