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

import com.iontorrent.dbaccess.RundbLibmetrics;
import com.iontorrent.main.FolderAction;
import java.beans.IntrospectionException;
import javax.swing.Action;
import org.openide.actions.PropertiesAction;

import org.openide.nodes.BeanNode;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Chantal Roth
 */
public class ResultNode extends BeanNode {

    /** Creates a new instance of CategoryNode */
    MyResult result;

    public ResultNode(MyResult exp) throws IntrospectionException {
        super(exp, new ResultChildren(exp), Lookups.singleton(exp));
        this.result = exp;
        setDisplayName(exp.getResultsName());

        if (exp.getLibMetrics()!= null) {
            RundbLibmetrics lib = exp.getLibMetrics();
            int qb = lib.getQ17MappedBases();
            int la = lib.getQ17LongestAlignment();
            int q200 = lib.getI100Q17reads();
            
            setShortDescription(exp.getStatus() +", 200bp Q17 reads="+q200+",  Q17 mapped bases ="+qb+", Q17 longest al="+la);
        }
        else {
            setShortDescription(exp.getStatus() + ", dir="+exp.getReport_directory());
        }
        // this.set
        if (exp.getResultsName() != null && exp.getResultsName().indexOf("_tn_")>0) {
            // thumbnails!
            setShortDescription("Thumbnails: "+this.getShortDescription());
            setIconBaseWithExtension("com/iontorrent/scout/experimentviewer/exptree/zoom-out.png");
        }
        else setIconBaseWithExtension("com/iontorrent/scout/experimentviewer/exptree/view-list-icons-2.png");
    }

    @Override
    public String getHtmlDisplayName() {
        return getHtmlStatus()+this.getDisplayName();
    }
    public String getHtmlStatus() {
        
        if (result.isCompleted()) {
            return "";
        } else if (result.isError()) {
            return "(<font color='880000'>error</font>) ";
        } else if (result.isStarted()) {
            return "(<font color='999999'>started</font>) ";            
        } else {
            return "(<font color='000099'>"+result.getStatus()+"</font>) ";
        }
    }
    public MyResult getResult() {
        return result;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
                    SystemAction.get(PropertiesAction.class),
                    new FolderAction(result.createContext())};
    }
}
