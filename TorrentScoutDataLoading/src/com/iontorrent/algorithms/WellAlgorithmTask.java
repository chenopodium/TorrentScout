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
package com.iontorrent.algorithms;
    
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.wellalgorithms.WellAlgorithm;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellFlowDataResult;
import java.util.ArrayList;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;

/**
 *
 * @author Chantal Roth
 */
public class WellAlgorithmTask extends Task  {

    
    WellAlgorithm alg;
    ArrayList<WellFlowDataResult> results;
    
    public WellAlgorithmTask(TaskListener listener, ProgressHandle plistener, WellAlgorithm alg) {
        super(listener, plistener);   
        this.alg = alg;
        alg.setListener(this);       
    }

    public WellAlgorithm getAlgorithm() {
        return alg;
    }
    public ArrayList<WellFlowDataResult> getResults() {
        return results;
    }
    public int getNrEmpty() {
        return alg.getNrEmpty();
    }

     public boolean isSuccess() {
        return results != null;
    }
    @Override
    public Void doInBackground() {
        try {
            p("Computing "+alg.getClass().getName()+" in bg");
            results = alg.compute();            
        }
        catch (Exception e) {
            err("Well algorithm failed: "+alg.getMsg()+":"+e.getMessage());
            err(e.getMessage(), e);
        }
        p("Computing "+alg.getClass().getName()+" done");
        return null;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(WellAlgorithmTask.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(WellAlgorithmTask.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(WellAlgorithmTask.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("WellAlgorithmTask: " + msg);
        //Logger.getLogger( WellAlgorithmTask.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the flow
     */
    public int getFlow() {
        return alg.getFlow();
    }

    /**
     * @return the coord
     */
    public WellCoordinate getCoord() {
        return alg.getCoord();
    }

   
}
