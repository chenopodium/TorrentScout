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
package com.iontorrent.scout.fromrest;

import com.iontorrent.dbaccess.RundbAnalysismetrics;
import com.iontorrent.dbaccess.RundbExperiment;
import com.iontorrent.dbaccess.RundbLibmetrics;
import com.iontorrent.dbaccess.RundbResults;
import com.iontorrent.dbaccess.RundbRig;
import com.iontorrent.dbaccess.RundbTfmetrics;
import com.iontorrent.jason.JSONArray;
import com.iontorrent.jason.JSONException;
import com.iontorrent.jason.JSONObject;
import com.iontorrent.scout.rest.ManyExperimentsClient;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 *
 * @author Chantal Roth
 */
public class ExperimentRestClientAdapter {

    ArrayList<RundbExperiment> explist;
    public ArrayList<RundbRig> rigs;
    private String URI;

    public ExperimentRestClientAdapter(String URI) {
        this.URI = URI;
        if (URI == null || URI.length() < 5) {
            return;
        }
        p("Maybe connecting to " + URI);
        if (URI.indexOf("localhost") > -1) {
            p("Maybe not.... localhost");
            return;
        }

        if (!URI.startsWith("http://")) {
            URI = "http://" + URI;
        }
        p("Got URI:" + URI);
        ManyExperimentsClient client = new ManyExperimentsClient(URI);
        JSONObject js = client.getJSSONObject();
        explist = getExperiments(js);
        p("Got explist: " + explist.size());

    }

    private ArrayList<RundbExperiment> getExperiments(JSONObject js) {
        p("\nCreating rig/exp tree:");
        Iterator it = js.keys();
        String key = (String) it.next();

        ArrayList<RundbExperiment> explist = new ArrayList<RundbExperiment>();
        rigs = new ArrayList<RundbRig>();
        HashMap<String, RundbRig> rigmap = new HashMap<String, RundbRig>();
        for (; it.hasNext(); key = (String) it.next()) {
            try {
                //   p("Obj for key"+key+":"+js.getString(key));
                Object obj = js.get(key);
                // p("class:"+obj.getClass().getName());
                if (obj instanceof JSONArray) {
                    //  p(key + " is an array");
                    JSONArray ar = (JSONArray) obj;
                    for (int i = 0; i < ar.length(); i++) {

                        if (ar.get(i) instanceof JSONObject) {
                            RundbExperiment exp = getExpFromJSON((JSONObject) ar.get(i));
                            if (exp != null) {
                                String pgm = exp.getPgmName();
                                //         p("Exp ist at PGM " + pgm);
                                explist.add(exp);
                                RundbRig rig = rigmap.get(pgm);
                                if (rig == null) {
                                    rig = new RundbRig();
                                    rig.setName(pgm);
                                    rigmap.put(pgm, rig);
                                    //              p("Created rig:" + rig.getName());
                                    rigs.add(rig);
                                }
                            }
                        }
                    }
                } else if (obj instanceof JSONObject) {
                    //      p(key + " is an object: " + obj);
                } else {
                    if (!key.equalsIgnoreCase("log")) {
                        p(key + "=" + obj.toString() + " (" + obj.getClass().getName() + ")");
                    }
//                    if (key.equalsIgnoreCase("resource_uri")) {
//                        p("Got resource uri "+obj);
//                        ExperimentClient res = new ExperimentClient(obj.toString());
//                        res.getExperiment();
//                    }
                }
                //showContent(js.getJSONObject(key));

            } catch (JSONException ex) {
                Logger.getLogger(ManyExperimentsClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return explist;
    }

    public RundbExperiment getExpFromJSON(JSONObject js) {
        if (!js.has("expName") || !js.has("expDir")) {
            p("JSON object is not an experiment");
            return null;
        }
        RundbExperiment exp = new RundbExperiment();
        //  p("Got JS:"+js.toString());
        try {
            exp.setExpDir(js.getString("expDir"));
            exp.setExpName(js.getString("expName"));
            exp.setPgmName(js.getString("pgmName"));
            exp.setProject(js.getString("project"));
            exp.setId(js.getInt("id"));
            //2010-05-05T18:03:38
            String ds = js.getString("date");
            Date d = null;
            try {
                d = parseDate(ds);
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
                err("COuld not parse date: " + ds);
            }
            //   p("Got date: "+d);
            exp.setDate(d);
            exp.setChipType(js.getString("chipType"));
            exp.setCycles(js.getInt("cycles"));
            exp.setSample(js.getString("sample"));
        } catch (JSONException ex) {
            Logger.getLogger(ManyExperimentsClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        // p("Created res obj: " + res);

        Iterator it = js.keys();
        String key = (String) it.next();
        ArrayList<RundbResults> reslist = new ArrayList<RundbResults>();
        for (; it.hasNext(); key = (String) it.next()) {
            try {
                //   p("Obj for key"+key+":"+js.getString(key));
                Object obj = js.get(key);
                // p("class:"+obj.getClass().getName());
                if (obj instanceof JSONArray) {
                    p(key + " of exp is an array");
                    processExpChildren((JSONArray)obj, key, reslist);
                }
            } catch (JSONException ex) {
                Logger.getLogger(ManyExperimentsClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (reslist.size() > 0) {
            p("Got results:" + reslist.size());
        }
        exp.setRundbResultsCollection(reslist);
        return exp;
    }

    private void processExpChildren(JSONArray ar, String key, ArrayList<RundbResults> reslist) throws JSONException {
        
        for (int i = 0; i < ar.length(); i++) {

            if (key.equalsIgnoreCase("results")) {
                p("Got a list of results");

                if (ar.get(i) instanceof String) {
                    String resuri = (String) ar.get(i);
                    // "/rundb/api/v1/results/95/"
                    p("Got resource uri: " + resuri);
                    ManyExperimentsClient resclient = new ManyExperimentsClient(URI, resuri);
                    JSONObject resjs = resclient.getJSSONObject();
                    RundbResults res = getResultFromJSON(resjs);
                    if (res != null) {
                        reslist.add(res);
                    }
                } else if (ar.get(i) instanceof JSONObject) {
                    p("Getting result from js " + ar.get(i));
                    RundbResults res = getResultFromJSON((JSONObject)ar.get(i));
                    if (res != null) {
                        reslist.add(res);
                    }
                    //                             "resource_uri": "/rundb/api/v1/experiment/1/",
                    //      "results": [
                    //        "/rundb/api/v1/results/95/",
                    //        "/rundb/api/v1/results/94/"
                    //      ],
                }
                
            }
        }
    }

    public  RundbResults getResultFromJSON(JSONObject js) {

        if (!js.has("resultsName")) {
            p("JSON object is not an experiment");
            return null;
        }
        RundbResults res = new RundbResults();
        //  p("Got JS:"+js.toString());
        try {
            res.setResultsName(js.getString("resultsName"));
            res.setSffLink(js.getString("sffLink"));
            res.setId(js.getInt("fastqLink"));

        } catch (JSONException ex) {
            Logger.getLogger(ManyExperimentsClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Iterator it = js.keys();
        String key = (String) it.next();
        
        for (; it.hasNext(); key = (String) it.next()) {
            try {
                //   p("Obj for key"+key+":"+js.getString(key));
                Object obj = js.get(key);
                // p("class:"+obj.getClass().getName());
                if (obj instanceof JSONArray) {
                    p(key + " of result  is an array");
                    JSONArray ar = (JSONArray)obj;
                    for (int j = 0; j < ar.length(); j++) {
                        processResChildren(ar.get(j), key, res);
                    }
                }
                else if (obj instanceof JSONObject) {
                     processResChildren(obj, key, res);
                }
            } catch (JSONException ex) {
                Logger.getLogger(ManyExperimentsClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return res;
    }
    private void processResChildren(Object obj, String key, RundbResults res) {
        JSONObject js = null;
        if (obj instanceof JSONObject) {
             js = (JSONObject)obj;
        }
        if (js == null) {
            p("processResChildren, "+key+" is not a json object:"+obj);
            return;
        }
        if (key.equalsIgnoreCase("libmetrics")) {
            ArrayList<RundbLibmetrics> libs = new ArrayList<RundbLibmetrics> ();
            RundbLibmetrics lib = new RundbLibmetrics();
            res.setRundbLibmetricsCollection(libs);
            lib.setGenome(js.optString("genome"));
            lib.setTotalNumReads(js.optInt("totalNumReads"));
        }
        else if (key.equalsIgnoreCase("tfmetrics")) {
            ArrayList<RundbTfmetrics> libs = new ArrayList<RundbTfmetrics> ();
            RundbTfmetrics lib = new RundbTfmetrics();
            res.setRundbTfmetricsCollection(libs);
            lib.setName(js.optString("name"));
        }
        else if (key.equalsIgnoreCase("analysismetrics")) {
            ArrayList<RundbAnalysismetrics> libs = new ArrayList<RundbAnalysismetrics> ();
            RundbAnalysismetrics lib = new RundbAnalysismetrics();
            res.setRundbAnalysismetricsCollection(libs);
            lib.setLive(js.optInt("live"));
        }
    }
    public static Date parseDate(String input) throws java.text.ParseException {

        //  p("Trying to parse date: " + input);
        int t = input.indexOf("T");
        if (t > 0) {
            input = input.substring(0, t);
        }
        //  p("Trying to parse date: " + input);
        //NOTE: SimpleDateFormat uses GMT[-+]hh:mm for the TZ which breaks
        //things a bit.  Before we go on we have to repair this.
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        return df.parse(input);

    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(ExperimentRestClientAdapter.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        Logger.getLogger(ExperimentRestClientAdapter.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(ExperimentRestClientAdapter.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("ExperimentRestClient: " + msg);
        //Logger.getLogger( ExperimentRestClientAdapter.class.getName()).log(Level.INFO, msg, ex);
    }

    public ArrayList<RundbExperiment> getExperiments() {
        return explist;
    }

    public ArrayList<RundbRig> getRigs() {
        return rigs;
    }
}
