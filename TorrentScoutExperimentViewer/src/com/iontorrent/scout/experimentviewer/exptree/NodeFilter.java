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

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Chantal Roth
 */
public abstract class NodeFilter {

    protected String name;
    protected String description;
    protected boolean not = false;
    
    public NodeFilter(String name, String description) {
        this(name, description, false);
    }
    public NodeFilter(String name, String description, boolean not) {
        this.name = name;
        this.description = description;
        this.not = not;
    }
    public void askForInput() {
        // by default, do nothing
    }

    public abstract boolean passes(Object node);

    public abstract Class getNodeClass();

    public static boolean passes(Object node, ArrayList<NodeFilter> filters) {
        if (filters == null) {
            return true;
        }

        for (NodeFilter f : filters) {
            if (node.getClass().getName().equalsIgnoreCase(f.getNodeClass().getName())) {
                
                if (!f.passes(node)) {
                    return false;
                }
            }
        }
        return true;
    }

    public String toString() {
        return name;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(NodeFilter.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(NodeFilter.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(NodeFilter.class.getName()).log(Level.WARNING, msg);
    }

    protected void p(String msg) {
        System.out.println("NodeFilter: " + msg);
        //Logger.getLogger( NodeFilter.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the not
     */
    public boolean isNot() {
        return not;
    }

    /**
     * @param not the not to set
     */
    public void setNot(boolean not) {
        this.not = not;
    }
}
