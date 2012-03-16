/*
 * Copyright (C) 2012 Life Technologies Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.iontorrent.ionogram;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.rawdataaccess.pgmacquisition.DataAccessManager;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.rawdataaccess.wells.BfMaskFlag;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.utils.ProgressListener;
import com.iontorrent.wellalgorithms.NearestNeighbor;
import com.iontorrent.wellalgorithms.WellContextFilter;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellFlowData;
import com.iontorrent.wellmodel.WellFlowDataResult;
import com.iontorrent.wellmodel.WellFlowDataResult.ResultType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class MultiFlowLoader implements TaskListener {

    private ArrayList<Integer> flowlist;
    private DataAccessManager manager;
    
    private RawType type;
    private WellFlowDataResult[] results;
    private int[] flownr;
    private boolean[] taskdone;
    private ExperimentContext exp;
    private WellCoordinate coord;
    private ProgressListener progress;
    private int curflowpos;

    private double progperflow;
    public MultiFlowLoader(ExperimentContext exp) {
        this.exp = exp;
        this.coord = exp.getWellContext().getCoordinate();
        type = RawType.ACQ;
        results = new WellFlowDataResult[exp.getNrFlows()];
    }

    public void compute(ArrayList<Integer> flows, ProgressListener progress) {
        if (flows == null || flows.size()<1) {
            this.flowlist = new ArrayList<Integer> ();
            for (int f = 0; f < exp.getNrFlows(); f++) {
                this.flowlist.add(f);
            }
        }
        else this.flowlist = flows;
        
        this.progress = progress;
        results = new WellFlowDataResult[exp.getNrFlows()];
        flownr = new int[flowlist.size()];
        taskdone = new boolean[flowlist.size()];
        progperflow = 100.0/flowlist.size();
        for (int i = 0; i < flowlist.size(); i++) {
            int flow = flowlist.get(i);
            flownr[i] = flow;
        }
        curflowpos = 0;
        int flow = flownr[curflowpos];
        AutomateTask task = new AutomateTask(flow, this);
        task.execute();

    }

    private WellFlowDataResult automateFlow(int flow) {
        
        this.coord = exp.getWellContext().getCoordinate();
        p("autoomateflow "+flow+", coord="+coord);
        manager = DataAccessManager.getManager(exp.getWellContext());
        BfMaskFlag[] haveflags = new BfMaskFlag[]{BfMaskFlag.EMPTY};
       // BfMaskFlag[] notflags = new BfMaskFlag[]{BfMaskFlag.PINNED};
        boolean bg = true;

        WellFlowDataResult nnresult = null;
        WellContextFilter filter = new WellContextFilter(exp.getWellContext(), haveflags, null, type, flow, coord);
        if (filter.getCoord() == null) {
            p("Got no coordinate");
            return null;
        }
        WellFlowData data = manager.getFlowData(filter, false);

        if (data == null) {
            err("Could not get data for flow " + flow + " and type " + type + ":" + manager.getErrorMsg());
            return null;
        }

        WellFlowDataResult raw = new WellFlowDataResult(coord.getCol(), coord.getRow(), flow, type, coord.getMaskdata());
        raw.setResultType(ResultType.RAW);
        raw.setData(data.getData());
        raw.setTimestamps(data.getTimestamps());

        if (bg) {
            NearestNeighbor alg = new NearestNeighbor(filter, 10, false);
            ArrayList<WellFlowDataResult> res = alg.compute();
            if (res != null && res.size() > 1) {
                nnresult = res.get(1);
                if (nnresult != null) {
                    //   p("Adding nn");
                    nnresult.setName("Raw - nn " + flow);
                }
            }
        } else {
            nnresult = raw;
        }

        return nnresult;
    }

    @Override
    public void taskDone(Task t) {
        p("Task " + t + " is done");
        // if (t.isSuccess()) {
        AutomateTask task = (AutomateTask) t;
        WellFlowDataResult res = task.getResult();
        int flow = task.getFlow();
        // start next task
        curflowpos++;
        if (curflowpos < flownr.length) {
            p("Starting task for flow "+ flownr[curflowpos]);
            AutomateTask nexttask = new AutomateTask(flownr[curflowpos], this);
            nexttask.execute();
        }

        // store result
        int index = -1;
        for (int i = 0; i < flownr.length; i++) {
            if (flownr[i] == flow) {
                index = i;
                break;
            }
        }
        taskdone[index] = true;
        if (res != null) {
            p("Got result for flow " + flow);
            results[flow] = res;
            if(progress != null) progress.setProgressValue((int)(progperflow*flow));
        } else {
            p("Could not load flow  " + flow + ":" + manager.getErrorMsg());
            p("I was not able to get data for flow " + flow + ":" + manager.getErrorMsg());
           // return;
        }
        boolean alldone = true;
        for (int i = 0; i < flowlist.size(); i++) {
            if (!taskdone[i]) {
                alldone = false;
            }
        }
        if (alldone || curflowpos >= flownr.length) {
            p("ALL tasks are done");
            if (progress != null)progress.setProgressValue(100);
            
        } else {
            if (progress != null)progress.setProgressValue(curflowpos*100/flownr.length);
            p("Not all done yet: " + Arrays.toString(taskdone)+", curflowpos="+curflowpos+", flownr.length="+flownr.length);
        }
    }

    
    /**
     * @return the multiflowresults
     */
    public WellFlowDataResult[] getMultiflowresults() {
        return results;
    }

    private class AutomateTask extends Task {

        boolean ok;
        int flow;
        WellFlowDataResult result;

        public AutomateTask(int flow, TaskListener tlistener) {
            super(tlistener);
            this.flow = flow;
           
        }

        public WellFlowDataResult getResult() {
            return result;
        }

        public int getFlow() {
            return flow;
        }

        @Override
        public Void doInBackground() {
            try {
                if (results[flow] == null) result = automateFlow(flow);
                else {
                    p("No need to compute, already got result for flow "+flow);
                    result = results[flow];
                }
                ok = true;
            } catch (Exception e) {
                p("Error in automate task: " + ErrorHandler.getString(e));
                ok = false;
            }
            return null;
        }

        @Override
        public boolean isSuccess() {
            return ok;
        }
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(MultiFlowLoader.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        Logger.getLogger(MultiFlowLoader.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(MultiFlowLoader.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        //System.out.println("MultiFlowLoader: " + msg);
        Logger.getLogger(MultiFlowLoader.class.getName()).log(Level.INFO, msg);
    }
}
