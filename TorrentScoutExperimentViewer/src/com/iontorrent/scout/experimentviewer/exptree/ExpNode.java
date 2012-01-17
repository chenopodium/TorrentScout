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
import com.iontorrent.dbaccess.RundbReportstorage;
import com.iontorrent.expmodel.ExperimentContext;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.BeanNode;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Chantal Roth
 */
public class ExpNode extends BeanNode {

    static ArrayList<NodeFilter> selectedFilters;
    static List<RundbReportstorage> storages;
    /** Creates a new instance of CategoryNode */
    RundbExperiment exp;
    MyRig rig;

    public ExpNode(RundbExperiment exp, MyRig rig) throws IntrospectionException {
        super(exp, new ExperimentChildren(exp, rig), Lookups.singleton(exp));
        this.exp = exp;
        this.rig = rig;
        
         String type = exp.getChipType();
        type = type.replaceAll("\"", "");
       
        setDisplayName(type+": "+exp.getExpName());

        setShortDescription(exp.getFtpStatus()+", project "+exp.getProject()+",  "+ exp.getFlows() + " flows, dir=" + exp.getExpDir());
        
        if (isBB()) setIconBaseWithExtension("com/iontorrent/scout/experimentviewer/exptree/chip_bb.png");
        else setIconBaseWithExtension("com/iontorrent/scout/experimentviewer/exptree/chip.png");
        
    }

    public boolean isBB() {
        String t = exp.getChipType();
        if (t == null) t = "";
        t = t.replaceAll("\"", "");
        
        return (t.toLowerCase().startsWith("bb") ||  t.toLowerCase().startsWith("9") ||
                (exp.getExpName()!= null && exp.getExpName().indexOf("block")>-1));
    }
    @Override
    public String getHtmlDisplayName() {
        return getHtmlStatus() + this.getDisplayName();
    }

    public String getHtmlStatus() {
        String s = exp.getFtpStatus();
        if (s == null) {
            s = "";
        }
        s = s.toLowerCase().trim();
        if (s.startsWith("complete")) {
            return "";
        } else if (s.length() > 0) {
            int p = 0;
            try {
                p = (Integer.parseInt(s));
            } catch (Exception e) {
                return "(<font color='990000'>" + s + "</font>) ";
            }
            if (p > 70) {
                return "(<font color='008800'>" + p + "%</font>) ";
            } else if (p > 50) {
                return "(<font color='000088'>" + p + "%</font>) ";
            } else if (p > 20) {
                return "(<font color='000044'>" + p + "%</font>) ";
            } else {
                return "(<font color='884400'>" + p + "%</font>) ";
            }
        } else {
            return "<font color='990000'>?</font>";
        }
    }

    public RundbExperiment getExp() {
        return exp;
    }

    public MyRig getRig() {
        return rig;
    }

    public static void setFilters(ArrayList<NodeFilter> filters) {
        selectedFilters = filters;
    }

    public static ArrayList<NodeFilter> getFilters() {
        return selectedFilters;
    }

    public List<RundbReportstorage> getStorages() {
        return storages;
    }

    public static void setStorages(List<RundbReportstorage> st) {
        storages = st;
    }

    public ExperimentContext createContext() {
        ExperimentContext context = new ExperimentContext();
        context.setExperimentName(exp.getExpName());
        context.setResultsName("");
        context.setChipType(exp.getChipType());
        context.setPgm(exp.getPgmName());
        context.setExpDir(exp.getExpDir());
        return context;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
                    SystemAction.get(PropertiesAction.class)};
    }
}
