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
package com.iontorrent.heatmaps;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.rawdataaccess.wells.ScoreMaskFlag;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth 
 */
public abstract class AbstractSMCalculator implements ScoreMaskCalculatorIF{
    
    private ScoreMaskFlag flag;
    
    private String name;
    private String description;
    protected Parameter[] params;
    String code;
    protected ExperimentContext expContext;
   
    public AbstractSMCalculator(String name, String desc, ScoreMaskFlag flag) {
        this.name = name;
        this.description = desc;
        this.flag = flag;
        //this.flag.setName(name);
        this.flag.setDescription(description);
    }
    @Override
    public String getDesc() {
        return description;
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public void setFlag(ScoreMaskFlag flag) {
        this.flag = flag;
    }
    @Override
    public String toString() {
        return name;
    }
    
    public String toFullString() {
        String s =  name+"\n"+description+":\nParameters:\n";
        for (Parameter p: this.getParams()) {
            s += p.toString()+"\n";
        }
        return s;
    }
    public int getNrParams() {
        return params.length;
    }
    
    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(AbstractSMCalculator.class.getName()).log(Level.SEVERE, msg, ex);
    }
    
    private void err(String msg) {
        
        Logger.getLogger(AbstractSMCalculator.class.getName()).log(Level.SEVERE, msg);
    }
    
    private static void warn(String msg) {
        Logger.getLogger(AbstractSMCalculator.class.getName()).log(Level.WARNING, msg);
    }
    
    private static void p(String msg) {
        System.out.println("AbstractSMCalculator: " + msg);
        //Logger.getLogger( AbstractSMCalculator.class.getName()).log(Level.INFO, msg);
    }

   
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the flag
     */
    public ScoreMaskFlag getFlag() {
        return flag;
    }

   
    /**
     * @return the params
     */
    @Override
    public Parameter[] getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(Parameter[] params) {
        this.params = params;
    }

    /**
     * @return the expContext
     */
    public ExperimentContext getExpContext() {
        return expContext;
    }

    /**
     * @param expContext the expContext to set
     */
    public void setExpContext(ExperimentContext expContext) {
        this.expContext = expContext;
    }

   
  
}
