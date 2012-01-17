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
import com.iontorrent.dbaccess.RundbRig;
import java.util.List;

/**
 *
 * @author Chantal Roth
 */
public class MyRig extends RundbRig {
   
    private List<RundbExperiment> experiments;
    private boolean visible=true;
    public MyRig(String name, String comment) {
        this.setName(name);
        this.setComments(comment);
       
       // this.setRundbLocation(rig.getRundbLocation());
    }
    public MyRig(String name) {
        this.setName(name);
        
       // this.setRundbLocation(rig.getRundbLocation());
    }
    /**
     * @return the experiments
     */
    public List<RundbExperiment> getExperiments() {
        return experiments;
    }

    /**
     * @param experiments the experiments to set
     */
    public void setExperiments(List<RundbExperiment> experiments) {
        this.experiments = experiments;
    }

    /**
     * @return the visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @param visible the visible to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    
}
