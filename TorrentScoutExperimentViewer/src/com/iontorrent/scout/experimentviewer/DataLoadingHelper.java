/*
 * Copyright (C) 2011 Life Technologies Inc.
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
package com.iontorrent.scout.experimentviewer;

import com.iontorrent.dbaccess.RundbExperiment;
import com.iontorrent.scout.experimentviewer.exptree.MyRig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class DataLoadingHelper {

    private static String lastError;
    private static Exception lastException;

    public static ArrayList<MyRig> buildTreeFromRigsAndExperiments(List<RundbExperiment> experiments)  {       
        //    p("Got " + experiments.size() + " experiments");
        ArrayList<MyRig> tmprigs = new ArrayList<MyRig>();
        // create map
        //    p("Creating rig map");
        HashMap<String, MyRig> map = new HashMap<String, MyRig>();
        for (RundbExperiment ex : experiments) {
            String rname = ex.getPgmName();
            MyRig rig = map.get(rname.toLowerCase());
            if (rig == null) {
                MyRig myrig = new MyRig(rname);
                tmprigs.add(myrig);
                //  p("Got rig: "+myrig.getName());
                myrig.setExperiments(new ArrayList<RundbExperiment>());
                map.put(rname.toLowerCase(), myrig);
            }
        }

        //List<RundbExperiment> exp = new ArrayList();
        
        for (RundbExperiment ex : experiments) {
            MyRig rig = map.get(ex.getPgmName().toLowerCase());
            if (rig != null) {
                rig.getExperiments().add(ex);
                //   p("Adding exp "+ex.getExpName()+" to rig "+rig.getName());
            } else {
                p("Found no rig for exp: " + ex.getExpName() + ":" + ex.getPgmName());
            }
        }

        ArrayList<MyRig>myrigs = new ArrayList<MyRig>();
        for (MyRig rig : tmprigs) {
            if (rig.getExperiments() != null && rig.getExperiments().size() > 0) {
                myrigs.add(rig);
            } else {
                map.remove(rig.getName().toLowerCase());
            }
        }
       
        return myrigs;
    }
    /** ================== LOGGING ===================== */
    public static Exception getLastException() {
        return lastException;
    }

    public static String getLastError() {
        return lastError;
    }

    private static void err(String msg, Exception ex) {
        lastException = ex;
        lastError = msg;
        Logger.getLogger(DataLoadingHelper.class.getName()).log(Level.SEVERE, msg, ex);
    }
    
    private static void err(String msg) {
        lastError = msg;
        Logger.getLogger(DataLoadingHelper.class.getName()).log(Level.SEVERE, msg);
    }
    
    private static void warn(String msg) {
        Logger.getLogger(DataLoadingHelper.class.getName()).log(Level.WARNING, msg);
    }
    
    private static void p(String msg) {
        //System.out.println("DataLoadingHelper: " + msg);
        Logger.getLogger(DataLoadingHelper.class.getName()).log(Level.INFO, msg);
    }
}
