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
package com.iontorrent.expmodel;

import com.iontorrent.rawdataaccess.pgmacquisition.PGMAcquisitionGlobalHeader;
import com.iontorrent.rawdataaccess.pgmacquisition.RawDataFacade;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.rawdataaccess.wells.BfMask;
import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.utils.ToolBox;
import com.iontorrent.utils.io.FileTools;
import com.iontorrent.utils.io.FileUtils;
import com.iontorrent.wellmodel.WellContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class ExperimentContext implements Serializable {

    private String experimentName;
    private transient Vector<ExpContextChangedListener> list;
    //  private String rawDir;
    // private String resultsDir;
    private String resultsName;
    private boolean ignoreRule;
    private String status;
    private String libraryKey;
    private int nrFlows;
    private String tfSequence;
    private String flowOrder;
    private boolean block;
    private String pgm;
    private String expDir;
    private String resDirFromDb;
    private String cacheDir;
    private String rawDir;
    private String pluginDir;
    private String resultsDir;
    private String bamfilename;
    private String sfffilename;
    private String sfftffilename;
    private String chipType;
    
    private int nrcols;
    private int nrrows;
    // for display purposes
    private int coloffset;
    private int rowoffset;
    private String reportLink;
    protected transient WellContext context;
    private RawType rawtype;
    private int flow;
    private int frame;
    private boolean thumbnails;

    public ExperimentContext() {
        this.resultsName = "unknown" + (int) (Math.random() * 100);
        this.chipType = "unknown";
        this.reportLink = "unknown";
        this.resDirFromDb = "";
        list = new Vector<ExpContextChangedListener>();
    }

    /** creates a mostly unique file key hash to be used for index files */
    public String getFileKey() {
        String h = "";
        if (this.getChipType() != null && this.getChipType().length() > 1) {
            h = this.getChipType() + "_";
        }
        if (this.getResultsName() != null && this.getResultsName().length() > 1) {
            h += this.getResultsName() + "_";
        }

        String dir = this.getRawDir();
        if (dir != null || dir.trim().length() > 1) {
            h += Math.abs(dir.hashCode());
        }
        dir = this.getResultsDirectory();
        if (dir != null || dir.trim().length() > 1) {
            h += Math.abs(dir.hashCode());
        }
        String tmp = h;
        for (int i = 0; i < tmp.length(); i++) {
            char c = tmp.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                h = h.replace(c, '_');
            }
        }
        p("Got experiment filekey: " + h);
        return h;
    }

    public void addListener(ExpContextChangedListener l) {

        if (list.contains(l)) {
            return;
        }
        Vector<ExpContextChangedListener> li = (Vector<ExpContextChangedListener>) list.clone();
        for (ExpContextChangedListener old : li) {
            if (l.getClass().equals(old.getClass())) {
                list.remove(old);
            }
        }
        list.add(l);
    }

    public void removeListener(ExpContextChangedListener l) {
        list.remove(l);
    }

    public ExperimentContext deepClone() {
        ExperimentContext res = null;
        try {
            ByteArrayOutputStream fos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(this);
            out.flush();
            out.close();
            byte[] obj = fos.toByteArray();
            fos.close();
            ByteArrayInputStream fin = new ByteArrayInputStream(obj);
            ObjectInputStream in = new ObjectInputStream(fin);
            res = (ExperimentContext) in.readObject();
            in.close();
        } catch (Exception e) {
            err(e.getMessage());
        }
        //p("result of deep clone:" + res);
        if (res != null) {
            res.list = new Vector<ExpContextChangedListener>();
        }
        return res;
    }

    public ExperimentContext(String resdir, String cachedir, String rawdir) {
        this.resultsDir = resdir;
        this.rawDir = rawdir;
        this.cacheDir = cachedir;
        list = new Vector<ExpContextChangedListener>();
    }

    public WellContext createWellContext() {
        String dir = this.getResultsDirectory();
        if (dir == null || !FileUtils.exists(dir)) {
            p("ExperimentContext has invalied results dir: " + dir);
            // return null;
        }
        context = new WellContext(this);
        if (context.getMask() != null) {
            this.nrcols = context.getNrCols();
            this.nrrows = context.getNrRows();
            this.nrFlows = context.getNrFlwos();
        }
        return context;
    }

    public WellContext getWellContext() {
        if (context == null) {
            createWellContext();
        }
        return context;
    }

    public void setCacheDir(String dir) {
        dir = FileTools.addSlashOrBackslash(dir);
        this.cacheDir = dir;
    }

    public void expandCacheDir(String cache) {

        String res = getResultsName();
        if (res == null || res.length() < 1 || res.startsWith("unknown")) {
            res = this.getResultsDirectory();
            if (res == null) {
                res = this.getRawDir();
            }
            if (res == null) {
                res = "unknown" + ((int) (Math.random() * 100));
            }
            File f = new File(res);
            res = f.getName();
            //if (res.length()>10) res = res.substring(res.length()-10);
            if (res != null) {
                setResultsName(res);
            }
        }
        if (res != null && res.length() > 0) {
            //String cache = exp.getCacheDir();
            if (cache != null && FileUtils.exists(cache) && FileUtils.canWrite(cache)) {
                if (!cache.endsWith(res)) {
                    cache = FileTools.addSlashOrBackslash(cache);
                    cache = cache + res;
                    File f = new File(cache);
                    if (!f.exists()) {
                        f.mkdirs();
                    }
                    setCacheDir(cache);
                }
            }
        }
        p("after expandCacheDir: expname=" + getResultsName() + ", cache=" + getCacheDir());
    }

    public void setRawDir(String dir) {
        dir = FileTools.addSlashOrBackslash(dir);
        this.rawDir = dir;
    }

    public String getRawDir() {
        return rawDir;
    }

    public String toString() {
        String s = "";
        s += "PGM name:   " + getPgm() + "\n";
        s += "Exp name:   " + getExperimentName() + "\n";
        s += "Exp dir:    " + getExpDir() + "\n";
        s += "Result name:" + this.getResultsName() + "\n";
        s += "Result dir: " + getResultsDirectory() + "\n";
        s += "Report link: " + getReportLink() + "\n";
        s += "Db Res dir: " + getResDirFromDb() + "\n";
        s += "Cache dir:  " + getCacheDir() + "\n";
        s += "Raw dir: " + getRawDir() + "\n";
        s += "Sff name:   " + this.getSffFileName() + "\n";
        s += "Sff tf name:   " + this.getSfftffilename() + "\n";
        s += "BAM name:   " + this.getBamFileName() + "\n";
        s += "library key:" + getLibraryKey() + "\n";
        s += "# flows:    " + getNrFlows() + "\n";
        s += "# cols  " + getNrcols() + "\n";
        s += "# rows  " + getNrrows() + "\n";
        s += "TF sequence:" + getTfSequence() + "\n";
        s += "Flow order: " + getFlowOrder() + "\n";
        return s;
    }

    public String verify() {
        String msg = "";
        if (getCacheDir() == null) {
            return "No cache directory specified";
        }
        File d = new File(getCacheDir());
        if (!d.exists()) {
            d.mkdirs();
            d.setExecutable(true);
            d.setWritable(true);
        }
        if (!d.exists()) {
            msg = "<li>The <b>cache</b> dir <b>" + d.toString() + "</b> does not seem to exist</li>";
        }
        d = new File(getRawDir());
        if (!d.exists()) {
            msg += "<li>The <b>raw</b> dir <b>" + d.toString() + "</b> does not seem to exist</li>";
        }
        d = new File(this.getResultsDirectory());
        if (!d.exists()) {
            msg += "<li>The <b>results</b> dir <b>" + d.toString() + "</b> does not seem to exist</li>";
        }
        if (msg.length() > 0) {
            msg = "There is a problem with the directories:<ul>" + msg;
            msg += "</ul><font color='aa0000'>Please check your folder rule settings</font><br>";
        }
        return msg;
    }

    public static ExperimentContext createFake(GlobalContext global) {
        ExperimentContext exp = new ExperimentContext("S:/data/beverly/results", "S:/data/beverly/cache", "S:/data/beverly/raw");
        exp.setExperimentName("Fake_experiment");
        exp.setResultsName("Fake_result");
        // exp.setChipType("316");
        exp.setPgm("Fake_pgm");
        exp.setSffFilename("fake.sff");
        exp.setSfftffilename("fake-tf.sff");
        exp.setBamFilename("fake.bam");
        exp.setExpDir("");
        exp.setResDirFromDb("s:/data/results");
        exp.setStatus("Complete");
        // exp.setFlowOrder("GATC");
        // exp.setLibraryKey("GATC");
        // exp.setNrFlows(123);
        exp.setTfSequence("GATCGATCGATCGATCGATCGATCGATCGATC");
        //  p("Got FAKE exp context: " + exp);
        global.setExperimentContext(exp, false);
        return exp;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(ExperimentContext.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(ExperimentContext.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(ExperimentContext.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("ExperimentContext: " + msg);
        //Logger.getLogger( ExperimentContext.class.getName()).log(Level.INFO, msg, ex);
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

//    /**
//     * @return the rawDir
//     */
//    public String getRawDir() {
//        return rawDir;
//    }
//
//    /**
//     * @param rawDir the rawDir to set
//     */
//    public void setRawDir(String rawDir) {
//        this.rawDir = rawDir;
//    }
//
//    /**
//     * @return the resultsDir
//     */
//    public String getResultsDir() {
//        return resultsDir;
//    }
//
//    /**
//     * @param resultsDir the resultsDir to set
//     */
//    public void setResultsDir(String resultsDir) {
//        this.resultsDir = resultsDir;
//    }
    /**
     * @return the libraryKey
     */
    public String getLibraryKey() {
        return libraryKey;
    }

    /**
     * @param libraryKey the libraryKey to set
     */
    public void setLibraryKey(String libraryKey) {
        this.libraryKey = libraryKey;
    }

    /**
     * @return the tfSequence
     */
    public String getTfSequence() {
        return tfSequence;
    }

    /**
     * @param tfSequence the tfSequence to set
     */
    public void setTfSequence(String tfSequence) {
        this.tfSequence = tfSequence;
    }

    /**
     * @return the flowOrder
     */
    public String getFlowOrder() {
        return flowOrder;
    }

    /**
     * @param flowOrder the flowOrder to set
     */
    public void setFlowOrder(String flowOrder) {
        this.flowOrder = flowOrder;
    }

//    /**
//     * @return the global
//     */
//    public GlobalContext getGlobal() {
//        return global;
//    }
    /**
     * @return the nrFlows
     */
    public int getNrFlows() {
        return nrFlows;
    }

    /**
     * @param nrFlows the nrFlows to set
     */
    public void setNrFlows(int nrFlows) {
        this.nrFlows = nrFlows;
    }

    public void setResultsName(String resultsName) {
        this.resultsName = resultsName;
    }

    public String getResultsName() {
        return resultsName;
    }

    /**
     * @return the pgm
     */
    public String getPgm() {
        return pgm;
    }

    /**
     * @param pgm the pgm to set
     */
    public void setPgm(String pgm) {
        this.pgm = pgm;
    }

    /**
     * @return the expDir
     */
    public String getExpDir() {
        return expDir;
    }

    public String getResDirFromDb() {
        return resDirFromDb;
    }

    /**
     * @param expDir the expDir to set
     */
    public void setExpDir(String d) {
        d = ToolBox.replace(d, "\\", "/");
        if (d.startsWith("/")) {
            d = d.substring(1);
        }
        this.expDir = d;
    }

    public String getResultsDirectory() {
        return this.resultsDir;
    }

    public void setResultsDirectory(String dir) {
        dir = FileTools.addSlashOrBackslash(dir);
        this.resultsDir = dir;
        char slash = '/';
        if (resultsDir.startsWith("\\")) {
            slash = '\\';
        }
        this.pluginDir = resultsDir + slash + "plugin_out" + slash + "torrentscout_out" + slash;
        // p("ExperimentContext.setResultsDir: " + resultsDir);
        //  Exception e = new Exception("tracing call");
        //  p(ErrorHandler.getString(e));
    }

    public String getPluginDir() {
        return pluginDir;
    }

    public void setResDirFromDb(String d) {
        d = ToolBox.replace(d, "\\", "/");
        if (d.startsWith("/")) {
            d = d.substring(1);
        }


        this.resDirFromDb = d;
    }

    public String getSffFileName() {
        return sfffilename;
    }

    public String getBamFileName() {
        return bamfilename;
    }

    public void setSffFilename(String name) {
        this.sfffilename = getLastPart(name);
    }

    public void setBamFilename(String name) {
        this.bamfilename = getLastPart(name);
    }

    public String getLastPart(String file) {
        file = ToolBox.replace(file, "\\", "/");
        int sl = file.lastIndexOf("/");
        if (sl > -1) {
            file = file.substring(sl + 1);
        }
        return file;
    }

    public boolean is314() {
        return chipType.startsWith("314");
    }

    public boolean isBlock() {
        if (block) {
            return true;
        }
        String d = this.getResultsDirectory();
        if (d != null && (d.endsWith("block_thumbnails") || d.endsWith("block_thumbnails"))) {
            return true;
        }
        d = this.getRawDir();
        if (d != null && (d.endsWith("thumbnails") || d.endsWith("thumbnails"))) {
            return true;
        }

        return block;
    }

    public void setBlock(boolean b) {
        this.block = b;
    }

    public boolean isChipBB() {
        boolean bb = chipType.toLowerCase().startsWith("bb")
                || chipType.toLowerCase().startsWith("9");
        return bb;
    }

    public boolean doesExplogHaveBlocks() {
        // check if explot file exists 
        ExpLogParser p = new ExpLogParser(this.getRawDir());
        if (p.hasFile()) {
            // parse it to check for blocks
            p.parse();
            return p.hasBlocks();
        }
        else p("Could not find explog file "+p.getFile());
        return false;
    }

    /**
     * @return the chipType
     */
    public String getChipType() {
        return chipType;
    }

    /**
     * @param chipType the chipType to set
     */
    public void setChipType(String chipType) {
        this.chipType = chipType;

    }

    public boolean is316() {
        return chipType.startsWith("316");
    }

    public boolean is318() {
        return chipType.startsWith("318");
    }

    /**
     * @return the nrcols
     */
    public int getNrcols() {
        return nrcols;
    }

    /**
     * @param nrcols the nrcols to set
     */
    public void setNrcols(int nrcols) {
        this.nrcols = nrcols;
    }

    /**
     * @return the nrrows
     */
    public int getNrrows() {
        return nrrows;
    }

    public void findColsRows(int flow, RawType type) {
        err("Got no cols/rows, need to read raw .dat file");
        RawDataFacade io = RawDataFacade.getFacade(getRawDir(), getCacheDir(), type);
        PGMAcquisitionGlobalHeader header = io.getHeader(flow);
        if (header != null) {
            nrcols = header.getNrCols();
            nrrows = header.getNrRows();
        } else {
            BfMask mask = this.getWellContext().getMask();
            if (mask != null) {
                nrcols = mask.getNrCols();
                nrrows = mask.getNrRows();
            }

        }
        if (nrrows < 1 || nrcols < 1) {
            err("Could not find out cols/rows from bfmask or raw file flow " + flow);
        }

    }

    /**
     * @param nrrows the nrrows to set
     */
    public void setNrrows(int nrrows) {
        this.nrrows = nrrows;
    }

    public String getCacheDir() {
        return this.cacheDir;
    }

    public void setReportLink(String reportLink) {
        this.reportLink = reportLink;
    }

    public String getReportLink() {
        return reportLink;
    }

    /**
     * @return the ignoreRule
     */
    public boolean isIgnoreRule() {
        return ignoreRule;
    }

    /**
     * @param ignoreRule the ignoreRule to set
     */
    public void setIgnoreRule(boolean ignoreRule) {
        this.ignoreRule = ignoreRule;
    }

    /**
     * @return the sfftffilename
     */
    public String getSfftffilename() {
        return sfftffilename;
    }

    /**
     * @param sfftffilename the sfftffilename to set
     */
    public void setSfftffilename(String sfftffilename) {
        this.sfftffilename = getLastPart(sfftffilename);
    }

    public void clear() {
        if (this.context != null) {
            context.clear();
        }
        context = null;
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

    public boolean isCompleted() {
        return status != null && status.equalsIgnoreCase("completed");
    }

    public boolean isStarted() {
        return status != null && status.equalsIgnoreCase("started");
    }

    public boolean isError() {
        return status != null && (status.equalsIgnoreCase("error") || status.equalsIgnoreCase("aborted"));
    }

    public void setColOffset(int offsetx) {
        this.coloffset = offsetx;
    }

    public void setRowOffset(int offsety) {
        this.rowoffset = offsety;
    }

    public int getRowOffset() {
        return rowoffset;
    }

    public int getColOffset() {
        return coloffset;
    }

    public String getBfMaskFile() {
        return this.getResultsDirectory() + "bfmask.bin";
    }

    public String getWellsFile() {
        return this.getResultsDirectory() + "1.wells";
    }

    public boolean hasBfMask() {
        return FileUtils.exists(getBfMaskFile());
    }

    public boolean hasWells() {
        return FileUtils.exists(getWellsFile());
    }

    public boolean hasSff() {
        return FileUtils.exists(this.getResultsDirectory() + this.getSffFileName());
    }

    public boolean hasDat() {
        return FileUtils.exists(this.getRawDir() + "acq_00000.dat");
    }

    public boolean hasBam() {
        return FileUtils.exists(this.getResultsDirectory() + this.getBamFileName());
    }

    public void setFileType(RawType rawType) {
        p("========= setFileType: " + rawType);
        Exception e = new Exception("test");
        p("setFleType stack grace: " + ErrorHandler.getString(e));
        if (this.rawtype != rawType) {
            this.rawtype = rawType;
            p("Got file type " + rawType);
            this.typeChanged(rawType);
        } else {
            p("Same type as before");
        }

    }

    public void typeChanged(RawType w) {
        if (list != null) {

            Vector<ExpContextChangedListener> li = (Vector<ExpContextChangedListener>) list.clone();
            for (ExpContextChangedListener l : li) {
                p("Calling type Changed: " + w);
                l.fileTypeChanged(w);
            }
        } else {
            p("typeChanged: Got no listeners");
        }

    }

    public RawType getFileType() {
        if (rawtype == null) {
            rawtype = RawType.ACQ;
        }
        return rawtype;
    }

    public int getFlow() {
        return flow;
    }

    public int getFrame() {
        return frame;
    }

    public void setFlow(int flow) {
        p("Got flow " + flow);
        if (getFlow() != flow) {
            this.flow = flow;
            if (list != null) {
                Vector<ExpContextChangedListener> li = (Vector<ExpContextChangedListener>) list.clone();
                for (ExpContextChangedListener l : li) {
                    l.flowChanged(flow);
                }
            }
        }
    }

    public void setFrame(int frame) {
        if (getFrame() != frame) {
            this.frame = frame;
            if (list != null) {
                Vector<ExpContextChangedListener> li = (Vector<ExpContextChangedListener>) list.clone();
                for (ExpContextChangedListener l : li) {
                    l.frameChanged(frame);
                }
            }
        }
    }

    public void setThumbnails(boolean b) {
        this.thumbnails = b;
    }

    public boolean isThumbnails() {
        return thumbnails;
    }
}
