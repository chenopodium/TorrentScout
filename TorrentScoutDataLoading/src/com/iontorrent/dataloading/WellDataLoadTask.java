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
package com.iontorrent.dataloading;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.rawdataaccess.wells.BfMask;
import com.iontorrent.results.scores.ScoreMask;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellSelection;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;

/**
 *
 * @author Chantal Roth
 */
public class WellDataLoadTask extends Task {

    WellContext context;
    ArrayList<WellCoordinate> list;
    int max;
    ExperimentContext expcontext;
    boolean includeScoreMaskData;
    public WellDataLoadTask(TaskListener listener, ProgressHandle plistener, WellContext context, ExperimentContext expcontext, int max, boolean includeScoreMaskData) {
        super(listener, plistener);
        this.context = context;
        this.includeScoreMaskData = includeScoreMaskData;
        this.expcontext = expcontext;
        this.max = max;


    }
    public boolean isLoadScores() {
        return includeScoreMaskData;
    }
    public ArrayList<WellCoordinate> getResult() {
        return list;
    }

    public boolean isSuccess() {
        return list != null;
    }

    @Override
    public Void doInBackground() {
        if (loadData(includeScoreMaskData)) {
            return null;
        }
        return null;
    }

    public boolean loadData(boolean includeScoreMaskData) {
        setProgressValue(0);
        double progress = 0;
        WellSelection selection = context.getSelection();
        BfMask mask = context.getMask();
        if (mask == null) return false;
        ScoreMask smask = null;
        if (includeScoreMaskData) {
            smask = ScoreMask.getMask(expcontext, context);
            if (smask == null) {
                err("Got NO scoreMask for " + expcontext);
            } else {
                   p("ScoreMask: Loading all data for all flags");
                smask.readAllData();
            }
        }
        if (selection == null) {
            err("No selection");
            return false;
        }
        double totalnr = Math.abs(selection.getCoord1().getCol() - selection.getCoord2().getCol());
        double incr = 100.0d / totalnr;
        for (WellCoordinate coord : selection.getAllWells()) {
            int c = coord.getCol();
            int r = coord.getRow();
            coord.setMaskdata(mask.getDataPointAt(c, r));
            
            if (smask != null) {
                coord.setScoredata(smask.getDataPointsAt(c, r));
                //      p("loadData: Got scoredata for :"+coord+":"+coord.getScoredata());
            }


            progress += incr;
            setProgressValue((int) progress);
        }
        return true;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(WellDataLoadTask.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(WellDataLoadTask.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(WellDataLoadTask.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("WellDataLoadTask: " + msg);
        //Logger.getLogger( WellDataLoadTask.class.getName()).log(Level.INFO, msg, ex);
    }
}
