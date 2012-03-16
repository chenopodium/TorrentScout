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

import com.iontorrent.dbaccess.RundbAnalysismetrics;
import com.iontorrent.dbaccess.RundbExperiment;
import com.iontorrent.dbaccess.RundbLibmetrics;
import com.iontorrent.dbaccess.RundbQualitymetrics;
import com.iontorrent.dbaccess.RundbResults;
import com.iontorrent.dbaccess.RundbTfmetrics;
import com.iontorrent.expmodel.CompositeExperiment;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.utils.ToolBox;
import com.iontorrent.utils.io.FileTools;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


/**
 *
 * @author Chantal Roth
 */
public final class MyResult  {
   
    private String resultsName;
    private String reportLink;
    private String experimentName;
    private String fastqLink;
    private String status;
    private Date date;
    private File report_directory;
    private File wellsFile;
    private File acquisitionPath;
  //  private File bfMaskFile;
    private String pgmname;
    
  
    private String bamlink;
    private String sfflink;
   
    private RundbTfmetrics tfMetrics;
    private RundbAnalysismetrics analysisMetrics;
    private RundbLibmetrics libMetrics;
    private RundbQualitymetrics qualityMetrics;
    
    private int nrFlows;
    private String flowOrder;
    private String libraryKey;
    private String tfSequence;
    private String chipType;
    
    public MyResult(RundbResults r, MyRig rig) {
      
        RundbExperiment ex = r.getRundbExperiment();
     
        this.setReportLink(r.getReportLink());
        this.setFastqLink(r.getFastqLink());
        this.setStatus(r.getStatus());
      //  this.rawPath = r.getRundbExperiment().
        this.setExperimentName((ex.getExpName()));         
        String path  = r.getReportLink();
        path = findPath(path);
        report_directory = new File(path).getParentFile();         
        this.setResultsName(report_directory.getName());
        // for testing:
        path = r.getRundbExperiment().getExpDir();
        path = findPath(path);
        acquisitionPath = new File(path);
        this.nrFlows = ex.getFlows();
        this.date = r.getRundbExperiment().getDate();
        this.libraryKey = ex.getLibraryKey();
        this.flowOrder = ex.getFlowsInOrder();
        this.chipType = ex.getChipType();
        this.chipType = chipType.replace("\"", "");
        String dir = getReport_directory().getPath();
        dir = FileTools.addSlashOrBackslash(dir);
        wellsFile = new File(dir+"1.wells");
     //   bfMaskFile = new File(dir+"bfmask.bin");
        this.pgmname = rig.getName();
        if (pgmname == null || pgmname.length()<1) {
            p("Rig has no name: "+rig);
            this.pgmname = r.getRundbExperiment().getPgmName();
        }
        if (!r.getRundbTfmetricsCollection().isEmpty()) {
       //     p("Got tfmetrics:"+ tfMetrics);
            tfMetrics = r.getRundbTfmetricsCollection().iterator().next();
            this.tfSequence = tfMetrics.getSequence();
        }
       // else p("Got no tf metrics");
        
        sfflink = r.getSffLink();        
        bamlink = sfflink.substring(0, sfflink.length()-4)+".bam";
        if (!r.getRundbAnalysismetricsCollection().isEmpty()) {
            analysisMetrics = r.getRundbAnalysismetricsCollection().iterator().next();
        }
        
        if (!r.getRundbQualitymetricsCollection().isEmpty()) {
            qualityMetrics = r.getRundbQualitymetricsCollection().iterator().next();
        }
        
        if (!r.getRundbLibmetricsCollection().isEmpty()) {
            libMetrics = r.getRundbLibmetricsCollection().iterator().next();
        }        
    }
    
    public static long getTopQ17Bases(ArrayList<MyResult> list, int top) {
        ArrayList<Long> bases = new ArrayList<Long>();
        for (MyResult res: list) {
            if (res.getQaulityMetrics() != null) {
            long val = res.getQaulityMetrics().getQ17Bases();
            bases.add(val);
            }
        }
        // from small to large, so best are at the END
        Collections.sort(bases);
        // now get the top value
        int nr = bases.size();
        if (nr == 0) return 0;
        
        if (nr > top) {
            return bases.get(nr - top);
        }
        else return bases.get(0);               
    }
     public static long getTopQ17MaxReadLen(ArrayList<MyResult> list, int top) {
        ArrayList<Long> values = new ArrayList<Long>();
        for (MyResult res: list) {
            if (res.getQaulityMetrics() != null) {
                long val = res.getQaulityMetrics().getQ17MaxReadLength();
                values.add(val);
            }
        }
        // from small to large, so best are at the END
        Collections.sort(values);
        // now get the top value
        int nr = values.size();
        if (nr == 0) return 0;
        
        if (nr > top) {
            return values.get(nr - top);
        }
        else return values.get(0);               
    }
      public static double getTopQ17MeanReadLen(ArrayList<MyResult> list, int top) {
        ArrayList<Double> values = new ArrayList<Double>();
        for (MyResult res: list) {
            if (res.getQaulityMetrics() != null) {
            double val = res.getQaulityMetrics().getQ17MeanReadLength();
            values.add(val);
            }
        }
        // from small to large, so best are at the END
        Collections.sort(values);
        // now get the top value
        int nr = values.size();
        if (nr == 0) return 0;
        
        if (nr > top) {
            return values.get(nr - top);
        }
        else return values.get(0);               
    }
    
   public ExperimentContext createContext() {
       MyResult result = this;
        ExperimentContext exp = new ExperimentContext();
        
     //   p("Creating experiment context with result " + result);
        // check for bb        
        exp.setExperimentName(result.getExperimentName());
        exp.setResultsName(result.getResultsName());
        exp.setChipType(result.getChipType());
        exp.setPgm(result.getPgmname());
        exp.setReportLink(result.getReportLink());
        String name = exp.getLastPart(result.getFastqLink());
        name = name.substring(0, name.length() - 6);
        exp.setStatus(result.getStatus());
        exp.setBamFilename(name + ".bam");
        exp.setSffFilename(name + ".sff");
        exp.setSfftffilename(name + "-tf.sff");
        exp.setExpDir(result.getAcquisitionPath().toString());
        
        String report = result.getReportLink();
        report = ToolBox.replace(report, "\\", "/");
        int s = report.lastIndexOf("/");

        report = report.substring(0, s);
        exp.setResDirFromDb(report);
        exp.setFlowOrder(result.getFlowOrder());
        exp.setLibraryKey(result.getLibraryKey());
        exp.setNrFlows(result.getNrFlows());
        exp.setTfSequence(result.getTfSequence());
        //   p("Created exp context: " + exp);
        exp.setRawDir("/"+exp.getExpDir());
        exp.setResultsDirectory(exp.getResDirFromDb());
        exp.setCacheDir(exp.getResultsDirectory()+"/plugin_out/torrentscout/");
        if (exp.isThumbnails()) {
            
            exp.setThumbnailsRaw();
            p("Experiment is thumbnails, using thumbnails raw: "+exp.getRawDir());
        }
        return exp;
   }
    private String findPath(String url) {
        return url;
    }
    /**
     * @return the resultsName
     */
    public String getResultsName() {
        return resultsName;
    }

    /**
     * @param resultsName the resultsName to set
     */
    public void setResultsName(String resultsName) {
        this.resultsName = resultsName;
    }

    /**
     * @return the reportLink
     */
    public String getReportLink() {
        return reportLink;
    }

    /**
     * @param reportLink the reportLink to set
     */
    public void setReportLink(String reportLink) {
        this.reportLink = reportLink;
    }

    /**
     * @return the fastqLink
     */
    public String getFastqLink() {
        return fastqLink;
    }

    /**
     * @param fastqLink the fastqLink to set
     */
    public void setFastqLink(String fastqLink) {
        this.fastqLink = fastqLink;
    }

    public boolean isCompleted() {
        return status != null && status.equalsIgnoreCase("completed");
    }
    public boolean isStarted() {
        return status != null && status.equalsIgnoreCase("started");
    }
    public boolean isError() {
        return status != null && (status.indexOf("error")>-1 || status.equalsIgnoreCase("aborted"));
    }
    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the report_directory
     */
    public File getReport_directory() {
        return report_directory;
    }

    /**
     * @return the wellsFile
     */
    public File getWellsFile() {
        return wellsFile;
    }

    /**
     * @return the acquisitionPath
     */
    public File getAcquisitionPath() {
        return acquisitionPath;
    }

//    /**
//     * @return the bfMaskFile
//     */
//    public File getBfMaskFile() {
//        return bfMaskFile;
//    }

    /**
     * @return the tfMetrics
     */
    public RundbTfmetrics getTfMetrics() {
        return tfMetrics;
    }

    /**
     * @return the analysisMetrics
     */
    public RundbAnalysismetrics getAnalysisMetrics() {
        return analysisMetrics;
    }

    /**
     * @return the libMetrics
     */
    public RundbLibmetrics getLibMetrics() {
        return libMetrics;
    }

    /**
     * @return the qualityMetrics
     */
    public RundbQualitymetrics getQaulityMetrics() {
        return qualityMetrics;
    }

    private void p(String string) {
//  System.out.println("MyResults: "+string);
    }

    /**
     * @return the experimentName
     */
    public String getExperimentName() {
        return experimentName;
    }

    /**
     * @param experimentName the experimentName to set
     */
    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    /**
     * @return the nrFlows
     */
    public int getNrFlows() {
        return nrFlows;
    }

    /**
     * @return the flowOrder
     */
    public String getFlowOrder() {
        return flowOrder;
    }

    /**
     * @return the libraryKey
     */
    public String getLibraryKey() {
        return libraryKey;
    }

    /**
     * @return the tfSequence
     */
    public String getTfSequence() {
        return tfSequence;
    }

    /**
     * @return the pgmname
     */
    public String getPgmname() {
        return pgmname;
    }

    /**
     * @return the samlink
     */
    public String getBamlink() {
        return bamlink;
    }

    /**
     * @return the sfflink
     */
    public String getSfflink() {
        return sfflink;
    }

    /**
     * @return the chipType
     */
    public String getChipType() {
        return chipType;
    }
    public boolean isBB() {
        String t = getChipType();
        if (t == null) t = "";
        t = t.replaceAll("\"", "");
        
        return (t.toLowerCase().startsWith("bb") ||  t.toLowerCase().startsWith("9"));
    }
    public boolean isTN() {
        String t = this.getResultsName();
        if (t == null) t = "";
       
        return (t.toLowerCase().indexOf("_tn_")>0);
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }
      
}
