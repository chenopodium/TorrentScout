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
package com.iontorrent.sequenceloading;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.sff.Sff;
import com.iontorrent.sff.SffRead;
import com.iontorrent.sff.index.KmerSffIndex;
import com.iontorrent.sff.index.WellToSffIndex;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.utils.ToolBox;
import com.iontorrent.utils.io.FileTools;
import com.iontorrent.utils.io.FileUtils;
import com.iontorrent.wellmodel.WellCoordinate;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.samtools.SAMRecord;
import org.iontorrent.seq.Coord;
import org.iontorrent.seq.Read;

import org.iontorrent.seq.indexing.ReadPos;

import org.iontorrent.seq.sam.SamUtils;

/**
 *
 * @author Chantal Roth
 */
public final class SequenceLoader implements TaskListener {

    private File samfile;
    private File bamfile;
    private File sffile;
    private File sfftfile;
    //  private File samindexfile;
    private File welltosffindexfile;
    private File welltosfftfindexfile;
    //private File kmertosffindexfile;
    private SamUtils sam;
    private String results_path;
    static SequenceLoader curloader;
    static WellToSffIndexTask welltosffindextask;
    static WellToSffIndexTask welltosfftfindextask;
    static KmerToSffIndexTask kmertosffindextask;
    // static SamIndexTask samindextask;
    static BamWellToLocIndexTask welltolocindextask;
    private String cache_dir;
    private String plugin_dir;
    private String msg;
    private WellToSffIndex welltosffindex;
    private WellToSffIndex welltosfftfindex;
    private KmerSffIndex kmersffindex;
    private ExperimentContext context;
    // ExperimentContext exp;

    public static SequenceLoader getSequenceLoader(ExperimentContext context) {
        return getSequenceLoader(context, true, true);
    }

    public static SequenceLoader getSequenceLoader(ExperimentContext context, boolean useCache, boolean getFiles) {
        if (context == null) {
            p("Got no experiment context");
            return null;
        }
        String results_path = context.getResultsDirectory();
        if (useCache) {
            if (curloader != null && curloader.results_path != null && curloader.results_path.equalsIgnoreCase(results_path)) {
                return curloader;
            }
        }
        curloader = new SequenceLoader(context, getFiles);
        return curloader;
    }

    private SequenceLoader(ExperimentContext context, boolean getFiles) {
        this.context = context;
        this.results_path = context.getResultsDirectory();
        this.cache_dir = context.getCacheDir();
        this.plugin_dir = context.getPluginDir();
        if (cache_dir == null) {
            err("Got no cache dir in sequence loader");
        }

        if (context.getNrcols()<1 || context.getNrrows()<1) {
            warn("unknown rows, cols");
            context.findColsRows(0, RawType.ACQ);
        }
        //   p("Locating sff " + context.getSffFileName());
        this.checkAndFindSffFile(context.getSffFileName(), getFiles);
        //   p("Locating sff tf " + context.getSfftffilename());
        this.checkAndFindSfftfFile(context.getSfftffilename(), false);
        //   p("Locating BAM " + context.getBamFileName());
        this.checkAndLocateBamFile(context.getBamFileName(), getFiles);
        //this.checkAndFindSffFile(context.getSffFileName());
        //    p("Found sff: "+this.foundSffFile());
        //    p("Found sff tf: "+this.foundSfftfFile());
        //    p("Found bam: "+this.foundBamFile());
    }

    public boolean createSamFile(String dir) {
        // convert BAM to SAM. Inefficient, but we have no BAM indexer ;-)
        String tmp = bamfile.getName();

        if (!FileUtils.canWrite(dir)) {
            err("Cannot write to dir " + dir);
            return false;
        } else {
            p("Can write to dir " + dir);
        }
        String samfilename = tmp.substring(0, tmp.length() - 4) + ".sam";
        samfile = new File(dir + samfilename);
        p("Samfile: " + samfile);
        SamUtils.convertBamToSam(bamfile, samfile);
        if (!samfile.exists()) {
            err("Could not convert bam " + bamfile + " to sam  " + samfile);
            samfile = null;
            return false;
        }
        //  getBamOrSamIndexFileName();

        return true;
    }

    public boolean maybeCreateSffIndex(TaskListener listener) {
        if (welltosffindextask != null) {
            msg = ("There is alreay an indexing task for index " + welltosffindex);
            return false;
        }
        if (listener != null) {
            msg("Staring indexing task for index file: " + welltosffindex);
            welltosffindextask = new WellToSffIndexTask(listener, this, welltosffindex);
            welltosffindextask.execute();
            GuiUtils.showNonModalMsg("I am creating an .sff index... some data won't show until this is done!");
            return false;
        } else {
            msg("Creating index file: " + welltosffindex);
            createSffIndex(welltosffindex, context.getNrcols(), context.getNrrows());
        }
        return true;
    }

    protected void createSffIndexer() {
        if (welltosffindex == null) {            
            welltosffindex = new WellToSffIndex(sffile.toString(), welltosffindexfile, this.context);
        }
    }

//    private boolean checkSamToWellIndex(TaskListener listener) {
//        if (!sam.hasWellToReadIndex()) {
//            // create ndex!
//            // p("Need to create an index");
//            if (samindextask != null) {
//                msg = "I am already indexing a SAM file... ";
//                msg(msg);
//                return true;
//            }
//            if (listener != null) {
//                msg("Starting sam index creation task for file " + samindexfile);
//                samindextask = new SamIndexTask(listener);
//                samindextask.execute();
//                GuiUtils.showNonModelMsg("I am creating a SAM/BAM index " + samindexfile + "... some data won't show until this is done!");
//                return true;
//            } else {
//                msg("Creating sam index " + samindexfile);
//                try {
//                    sam.createWellToSamIndex();
//                } catch (Exception e) {
//                    msg("Got an error in index creation: " + e.getMessage());
//                }
//            }
//        } else {
//            //    p("Sam is there and has an index");
//        }
//        return false;
//    }
    private boolean checkBamWellToLocIndex(TaskListener listener) {
        if (!sam.hasWellToLocIndex()) {
            // create ndex!
            p("Need to create well to loc index");
            if (this.welltolocindextask != null) {
                msg = "I am indexing a BAM file... (well to bam index)";
                msg(msg);
                return true;
            }
            if (listener != null) {
                msg("Starting BAM well to location index creation task for file " + sam.getWellToLocIndexFile());
                welltolocindextask = new BamWellToLocIndexTask(listener);
                welltolocindextask.execute();
                GuiUtils.showNonModalMsg("I am creating a BAM index " + sam.getWellToLocIndexFile() + "... some data won't show until this is done!");
                return true;
            } else {
                msg("Creating BAM well to loc index " + sam.getWellToLocIndexFile());
                try {
                    sam.createWellToLocIndex();
                } catch (Exception e) {
                    msg("Got an error in BAM well to loc creation: " + e.getMessage());
                }
            }
        } else {
            //    p("Sam is there and has an index");
        }
        return false;
    }

    private SAMRecord getSAM(String readname, int x, int y, TaskListener listener) {
        msg = null;

        p("trying to read " + readname + ",  sam " + x + "/" + y);
        if (bamfile == null) {
            msg("Found no bam file");
            return null;
        }
        if (!checkBamFileAndIndex(bamfile.toString(), listener)) {
            p("checkBamFileAndIndex " + bamfile + " failed");
            return null;
        }
//        if (!checkSamFileAndIndex(samfile.toString(), listener)) {           
//            p("checkSamFileAndIndex " + samfile + " failed");
//            //return null;
//        }
        SAMRecord rec = null;
        if (sam.hasWellToLocIndex()) {
            if (sam.hasBai()) {
                rec = sam.getSequenceViaBai(readname);
                p("BAM has well to loc index, using BAI, got: " + rec);
            } else {
                msg("BAM file has no .bai index. Please create it using: samtools index " + this.bamfile + "(http://sourceforge.net/projects/samtools/files/)");
            }
        } else {
            //sam.createReadLocationsIndexFile();
            msg("BAM file has NO well to location index (yet) " + sam.getWellToLocIndexFile());
        }
//        if (rec == null && sam.hasWellToReadIndex()) {
//            p("Need to use SAM index file " + sam.getWellToLocIndexFile());
//            rec = sam.getSequenceByIndex(x, y);
//            return rec;
//        } else {
//            msg("SAM file has no well to read index (yet)");
//        }
        return rec;
    }

    private void msg(String s) {
        p(s);
        msg = s;
    }

//    private boolean checkSamFileAndIndex(String filename, TaskListener listener) {
//        if (!checkAndLocateSamFile(filename)) {
//            // p(" checkAndLocateSamFile " + filename + " failed ");
//            return false;
//        }
//        if (sam == null) {
//            //  p("checkSamFileAndIndex: sam is null");
//            return false;
//        }
//        if (checkSamToWellIndex(listener)) {
//            return false;
//        }
//        return true;
//    }
    private boolean checkBamFileAndIndex(String filename, TaskListener listener) {
        if (!checkAndLocateBamFile(filename, true)) {
            p(" checkBamFileAndIndex " + filename + " failed ");
            return false;
        }
        if (sam == null) {
            //  p("checkSamFileAndIndex: sam is null");
            return false;
        }
        if (this.checkBamWellToLocIndex(listener)) {
            return false;
        }
        return true;
    }

    @Override
    public void taskDone(Task t) {
        p("Task " + t + " is done");
        if (t instanceof WellToSffIndexTask) {
            if (!t.isSuccess()) {
                File f = getWellsToSffIndexFile();
                if (f.exists()) {
                    GuiUtils.showNonModalDialog("I was unable to read the file " + f + "\n"
                            + "The error message is: " + this.msg, "Unable to read file");
                } else {
                    GuiUtils.showNonModalDialog("The WellToSffIndexTask creation failed. You may have no write permission in the cache folder?\n"
                            + "The index file is: " + getWellsToSffIndexFile(), "Indexing failed");
                }
            }
            WellToSffIndexTask task = (WellToSffIndexTask) t;
            task.client.taskDone(t);
        }

    }

    public boolean foundBamFile() {
        return (bamfile != null && bamfile.exists());
    }

    public boolean foundSamFile() {
        return (samfile != null && samfile.exists());
    }

    public boolean foundSffFile() {
        return (sffile != null && sffile.exists());
    }

    public boolean foundSfftfFile() {
        return (sfftfile != null && sfftfile.exists());
    }

    public File getBamFile() {
        return bamfile;
    }

    public File getSffFile() {
        return sffile;
    }

    public File getSfftfFile() {
        return sfftfile;
    }

    private boolean checkAndLocateBamFile(String filename, boolean ask) {
        if (!checkAndMaybeCacheBamFile(filename, ask)) {
            // p(" checkAndMaybeCacheSamFile " + filename + " failed");
            return false;
        }
        if (!foundBamFile()) {
            msg("The BAM file " + bamfile + " is not there (yet)... might want to check the name");
            p(msg);
            if (ask && !FileUtils.isUrl(results_path)) {
                // xxx pick the file
                curloader = null;
                String s = FileTools.getFile("I couldn't find the right .BAM file, \nbut you can select the .BAM file yourself if you like", ".bam", results_path);

                if (s == null) {
                    return false;
                } else {
                    if (s.toLowerCase().endsWith(".sam")) {
                        // got  sam file
                        samfile = new File(s);
                    } else {
                        bamfile = new File(s);
                    }
                    msg = null;
                }
            } else {
                return true;
            }
        }
        if (samfile == null) {
            samfile = new File(bamfile.toString().substring(0, bamfile.toString().length() - 4) + ".sam");
            // getBamOrSamIndexFileName();
        }
        if (sam == null) {
            sam = getSamUtils();
        }


        return true;
    }

    public SamUtils getSamUtils() {
        if (sam != null) {
            return sam;
        }
        if (bamfile == null) {
            warn("Got no bam file, cannot create Sam Utils");
          //  Exception e = new Exception("tracing method call");
           // err(ErrorHandler.getString(e));
            return null;
        }
        sam = new SamUtils(samfile, bamfile, this.cache_dir, this.results_path, this.plugin_dir);
        return sam;
    }

    private boolean checkAndLocateSamFile(String filename) {
        if (!checkAndMaybeCacheSamFile(filename)) {
            // p(" checkAndMaybeCacheSamFile " + filename + " failed");
            return false;
        }
        if (samfile == null || !samfile.exists()) {
            msg("The SAM file " + samfile + " is not there (yet)... might want to check the name");

            if (!FileUtils.isUrl(results_path)) {
                // xxx pick the file
                curloader = null;
                String s = FileTools.getFile("I couldn't find the right .SAM file, \nbut you can select the .sam file yourself if you like", ".sam, .bam", results_path);
                if (s == null) {
                    return false;
                } else {
                    samfile = new File(s);
                    // getBamOrSamIndexFileName();
                    msg = null;
                }
            } else {
                //   getBamOrSamIndexFileName();
                return true;
            }
        }
        if (sam == null) {
            getSamUtils();
        }
        return true;
    }

    public ArrayList<WellCoordinate> findWellCoords(long genomepos) {
        if (sam == null) {
            boolean ok = checkAndLocateSamFile(null);
            if (!ok) {
                msg("I could not find a sam file");
                return null;
            }
        }
        ArrayList<WellCoordinate> coords = new ArrayList<WellCoordinate>();
        ArrayList<ReadPos> res = sam.findReadsByGenomePos(genomepos);
        for (ReadPos rp : res) {
            String name = rp.readname;
            Coord coord = sam.getCoord(name);

            // p("Got read: " + name);
            WellCoordinate w = new WellCoordinate(coord.x, coord.y);
            coords.add(w);
        }
        return coords;
    }

    public boolean hasGenomeToReadIndex() {
        this.getSamUtils();
        if (sam == null) {
            err("Got no sam object");
        }
        return (sam != null && sam.hasGenomeToReadIndex());

    }

    public void createGenomeToReadIndex() {
        this.getSamUtils();
        if (sam != null) {
            sam.createReadLocationsIndexFile();
        } else {
            err("Got no sam object");
        }
    }

    public ArrayList<Read> getReadForCoords(ArrayList<WellCoordinate> coords) {
        ArrayList<Read> reads = new ArrayList<Read>();
        for (WellCoordinate coord : coords) {

            Read read = getRead(coord.getCol(), coord.getRow(), null);
            if (read != null) {
                if (read.getAlign() == null) {
                    err("Found read " + read + " at coord " + coord + ",  but it has no alignment - should be impossible!");
                } else {
                    reads.add(read);
                    p("Found read " + read);
                }

            }
        }
        return reads;
    }

    public Read getRead(int x, int y, TaskListener listener) {

        SffRead sff = getSffRead(x, y, listener);
        if (sff == null) {
            return null;
        }
        SAMRecord rec = getSAM(sff.getName(), x, y, listener);

        Read read = new Read(sff);
        if (rec != null) {
            sam.extractData(rec, read);
            if (sam.getErrorMsg() != null) {
                if (msg == null) {
                    msg = "";
                }
                this.msg += "<br>" + sam.getErrorMsg();
            }
        }

        return read;
    }

    public boolean hasSffIndex() {
        if (!foundSffFile()) {
            return false;
        }
        createSffIndexer();
        boolean exists = welltosffindex.readIndex();
        return exists;
    }

    public boolean hasSfftfIndex() {
        if (!foundSfftfFile()) {
            return false;
        }
        if (welltosfftfindex == null) {
            welltosfftfindex = new WellToSffIndex(sfftfile.toString(), welltosfftfindexfile,  this.context);
        }
        boolean exists = welltosfftfindex.readIndex();
        return exists;
    }

    public void createSffIndex() {
        if (!foundSffFile()) {
            return;
        }
        createSffIndexer();
        boolean exists = welltosffindex.readIndex();
        if (exists) {
            return;
        }
        createSffIndex(welltosffindex, context.getNrcols(), context.getNrrows());

    }

    public void createSfftfIndex() {
        if (!foundSfftfFile()) {
            return;
        }
        if (welltosfftfindex == null) {
            welltosfftfindex = new WellToSffIndex(sfftfile.toString(), welltosfftfindexfile, this.context);
        }
        boolean exists = welltosfftfindex.readIndex();
        if (exists) {
            return;
        }
        createSffIndex(welltosfftfindex, context.getNrcols(), context.getNrrows());

    }

    public SffRead getSffRead(int x, int y, TaskListener listener) {

        
        msg = null;

        p("getSffRead x="+x+", y="+y);
        if (sffile == null || !sffile.exists()) {
            if (msg == null) {
                msg = "The SFF file " + sffile + " is not there...(might want to check the name)";
            }
            if (sffile != null && !FileUtils.isUrl(results_path)) {
                // xxx pick the file
                String s = FileTools.getFile("I couldn't find the right .SFF file (" + sffile + "), \nbut you can select one yourself if you like", ".sff", results_path);
                if (s == null) {
                    return null;
                } else {
                    sffile = new File(s);
                    this.updateSffFilename(sffile);
                    msg = null;
                }
            } else {
                return null;
            }
        }
        if (welltosffindex == null) {
            //  p("Reading sff file " + sffile + "\n indexfile:" + sffindexfile);
            if (listener != null) {
                GuiUtils.showNonModalMsg("Reading sff index " + welltosffindexfile);
            }

            this.createSffIndexer();
        }
        boolean exists = welltosffindexfile.exists();
        if (!exists) {
            if (!maybeCreateSffIndex(listener)) {
                return null;
            }
        } else {
            // file exists
            boolean ok = welltosffindex.readIndex();
            if (!ok) {
                Exception ex = welltosffindex.getException();
                msg = "Index file " + welltosffindexfile + " exists, but couldn't read it: " + ex.getMessage();
                return null;
            }
        }
        p("Calling findread x= "+x+"/y="+y);
        SffRead read = welltosffindex.findRead(y, x);
        if (read == null) {
            if (welltosffindex.getException() != null) {
                msg("Got an sff file, and an index "+ welltosffindex.getSffIndexFile()+", but got an error when reading " + x + "/" + y + ":<br>" + welltosffindex.getException().getMessage());
            } else {
                String msg = "Found an sff file "+welltosffindex.getSffFile();
                if (welltosffindex.hasIndex()) {
                    msg += "and an index "+welltosffindex.getSffIndexFile()+", but no read at " + x + "/" + y;
                } else {
                    msg += "but NO index";
                }

                if (welltosffindex.getError() != null) {
                    msg += "<br>" + welltosffindex.getError();
                }
                msg(msg);
            }
            err(msg);
        }
        //  p("Got readIndex at " + x + "/" + y + ":" + readIndex);
        return read;

    }

    public String getFlowOrder() {
        if (sffile == null || !sffile.exists()) {
            p("No sff file, don't know flow order");
            return null;
        }
        Sff sff = new Sff(sffile.toString());
        sff.openFile();
        sff.readHeader();
        sff.closeFile();
        if (sff.getGheader() == null) {
            err("Could not read sff header for some reason...");
            return null;
        }

        return sff.getFlowOrder();
    }

    public SffRead getSfftfRead(int x, int y, TaskListener listener) {

        msg = null;

        if (sfftfile == null || !sfftfile.exists()) {
            if (msg == null) {
                msg = "The SFF TF file " + sfftfile + " is not there...(might want to check the name)";
            }
            if (sfftfile != null && !FileUtils.isUrl(results_path)) {
                // xxx pick the file
                String s = FileTools.getFile("I couldn't find the right .SFF TEST FRAG file (" + sfftfile + "), \nbut you can select one yourself if you like", ".sff", results_path);
                if (s == null) {
                    return null;
                } else {
                    sfftfile = new File(s);
                    this.updateSfftfFilename(sfftfile);
                    msg = null;
                }
            } else {
                return null;
            }
        }
        if (welltosfftfindex == null) {

            //  p("Reading sff file " + sffile + "\n indexfile:" + sffindexfile);
            if (listener != null) {
                GuiUtils.showNonModalMsg("Reading sff tf index " + welltosfftfindexfile);
            }

            welltosfftfindex = new WellToSffIndex(sfftfile.toString(), welltosfftfindexfile, this.context);

            boolean exists = welltosfftfindexfile.exists();
            if (!exists) {
                if (welltosfftfindextask != null) {
                    msg = ("There is alreay an indexing task for TF index " + welltosfftfindex);
                    return null;
                }
                if (listener != null) {
                    msg("Staring indexing task for TF index file: " + welltosfftfindex);
                    welltosfftfindextask = new WellToSffIndexTask(listener, this, welltosfftfindex);
                    welltosfftfindextask.execute();
                    GuiUtils.showNonModalMsg("I am creating an .sff TF index... some data won't show until this is done!");
                    return null;
                } else {
                    msg("Creating index file: " + welltosfftfindex);
                    createSffIndex(welltosfftfindex, context.getNrcols(), context.getNrrows());
                }
            } else {
                // file exists
                boolean ok = welltosfftfindex.readIndex();
                if (!ok) {
                    Exception ex = welltosfftfindex.getException();
                    msg = "Index file TF " + welltosfftfindexfile + " exists, but coudln't read it: " + ex.getMessage();
                }
            }
        }


        SffRead read = welltosfftfindex.findRead(y, x);
        if (read == null) {
            if (welltosfftfindex.getException() != null) {
                msg("Got an sff TF file, and an index, but:" + welltosfftfindex.getException().getMessage());
            } else {
                msg("Got an sff TF file, and an index, but found no read at " + x + "/" + y);
            }
            err(msg);
        }
        //  p("Got readIndex at " + x + "/" + y + ":" + readIndex);
        return read;

    }

    public ArrayList<SffRead> getSffReads(String subseq, TaskListener listener, boolean useindex) {

        msg = null;
        if (sffile == null || !sffile.exists()) {
            if (msg == null) {
                msg = "The SFF file " + sffile + " is not there...(might want to check the name)";
            }
            if (sffile != null && !FileUtils.isUrl(results_path)) {
                // xxx pick the file
                String s = FileTools.getFile("I couldn't find the right .SFF file (" + sffile + "), \nbut you can select one yourself if you like", ".sff", results_path);
                if (s == null) {
                    return null;
                } else {
                    sffile = new File(s);
                    msg = null;
                }
            } else {
                return null;
            }
        }
        if (kmersffindex == null) {

            //  p("Reading sff file " + sffile + "\n indexfile:" + kmersffindexfile);
            if (listener != null) {
                GuiUtils.showNonModalMsg("Reading kmer to sff index in " + cache_dir);
            }

            kmersffindex = new KmerSffIndex(sffile.toString(), cache_dir);
            boolean exists = kmersffindex.hasIndex() || !useindex;
            if (!exists && subseq.length() >= kmersffindex.getKmerSize()) {
                p("No index, and " + subseq + " is >= " + kmersffindex.getKmerSize() + "  long, so need to create index");
                if (kmertosffindextask != null) {
                    msg = "There is alreay an indexing task for creating kmer index";
                    p(msg);
                    return null;
                }
                if (listener != null) {
                    msg("Staring indexing task for kmer index file");
                    kmertosffindextask = new KmerToSffIndexTask(listener, kmersffindex);
                    kmertosffindextask.execute();
                    GuiUtils.showNonModalMsg("I am creating a SFF KMER index... ");
                    return null;
                } else {
                    msg("Creating index file: " + kmersffindex);
                    createKmerSffIndex(kmersffindex);
                }
            }
        }

        ArrayList<SffRead> reads = kmersffindex.findReads(subseq, useindex);
        if (reads == null) {
            if (kmersffindex.getException() != null) {
                msg("Got an sff file, and a KMER index, but:" + kmersffindex.getException().getMessage());
            } else {
                msg("Got an sff file, and a KMER index, but found no reads with " + subseq);
            }
            err(msg);
        }
        //  p("Got readIndex at " + x + "/" + y + ":" + readIndex);
        return reads;

    }

    private String findFile(String ex, boolean ask) {
        return findFile(ex, ask, null);
    }

    private String findFile(String ex, boolean ask, String not) {
        String f = findFile(ex, results_path, ask, not);
        if (f != null) {
            return f;
        }
        f = findFile(ex, cache_dir, ask, not);
        if (f != null) {
            return f;
        }
        f = findFile(ex, plugin_dir, ask, not);
        return f;

    }

    private String findFile(String ex, String path, boolean ask) {
        return findFile(ex, path, ask, null);
    }

    private String findFile(String ex, String path, boolean ask, String not) {
        if (path == null || FileUtils.isUrl(path)) {
            return null;
        }
        String file = null;
        File dir = new File(path);
        int nr = 0;
        if (dir.exists()) {
            File files[] = dir.listFiles();
            if (files != null) {
                for (int f = 0; f < files.length; f++) {
                    if (files[f].toString().endsWith(ex)) {
                        if (not == null || files[f].toString().indexOf(not) < 0) {
                            nr++;
                            file = files[f].toString();
                        }
                    }
                }
            }
        }
        if (nr > 1) {
            if (ask) {
                file = FileTools.getFile("I don't know which the right " + ex + " file is, please tell me", ex, path);
            } else {
                file = null;
            }
        } else if (nr == 1) {
            //if (ask) file = FileTools.getFile("I assume this is the right " + ex + " file?", ex, file);
        }
        return file;

    }

    private boolean checkAndFindSffFile(String file, boolean askFiles) {
        if (foundSffFile()) {
            return true;
        }

        if (file == null) {
            file = findFile("rawltrimmed.sff", askFiles, "untrimmed.rawlib.sff");
            if (file == null) file = findFile("rawlib.sff", askFiles, "untrimmed.rawlib.sff");
            if (file == null) file = findFile(".sff", askFiles, "tf.sff");
            if (file == null) {
                return false;
            } else {
                updateSffFilename(new File(file));
                return true;
            }
        }
        //   p("Checking if we can find sff file " + file);
        File f = this.findFileSomewhere(file);
        if (f == null || !f.exists()) {
            f = FileUtils.findAndCopyFileFromUrlTocache(file, this.cache_dir, this.results_path, false, false, null, 1024 * 1024);
        }
        if (f == null || !f.exists()) {
            file = findFile(".sff", askFiles, "tf.sff");
            f = this.findFileSomewhere(file);
        }
        if (f == null || !f.exists()) {
            if (askFiles) {
                file = FileTools.getFile("I couldn't find the right .SFF file, \nplease select it yourself", ".sff", results_path);
                f = this.findFileSomewhere(file);
            }

        }

        if (f == null || !f.exists()) {
            msg("Found no .sff file " + file + " in " + results_path + " or in cache " + cache_dir);
            return false;
        } else {
            updateSffFilename(f);
            return true;
        }
    }

    private boolean checkAndFindSfftfFile(String file, boolean askFiles) {
        if (foundSfftfFile()) {
            return true;
        }

        if (file == null) {
            file = findFile("tf.sff", askFiles);
            if (file == null) {
                return false;
            } else {
                updateSfftfFilename(new File(file));
                return true;
            }
        }
        //   p("Checking if we can find sff file " + file);
        File f = this.findFileSomewhere(file);
        if (f == null || !f.exists()) {
            f = FileUtils.findAndCopyFileFromUrlTocache(file, this.cache_dir, this.results_path, false, false, null, 1024 * 1024);
        }
        if (f == null || !f.exists()) {
            file = findFile("tf.sff", askFiles);
            f = this.findFileSomewhere(file);
        }
        if (f == null || !f.exists()) {
            if (askFiles) {
                file = FileTools.getFile("I couldn't find the right .SFF TEST FRAG file, \nplease select it yourself", ".sff", results_path);
                f = this.findFileSomewhere(file);
            }

        }

        if (f == null || !f.exists()) {
            msg("Found no .sff test frag file " + file + " in " + results_path + " or in cache " + cache_dir);
            return false;
        } else {
            updateSfftfFilename(f);
            return true;
        }
    }

    private void updateSffFilename(File f) {
        // p("Sff file is: " + f);
        sffile = f;
        String key = this.context.getFileKey();       
        String filename = sffile.getName() + key+".idx";
       welltosffindexfile = findFileSomewhere(filename);
        //  p("after findFileSomewhere: welltosffindexfile: " + welltosffindexfile);
        // p("welltosffindexfile exists? " + welltosffindexfile.exists());
    }

    private void updateSfftfFilename(File f) {
        // p("Sff file is: " + f);
        sfftfile = f;
        String filename = sfftfile.getName() + ".idx";
        welltosfftfindexfile = findFileSomewhere(filename);
        p("TF after findFileSomewhere: welltosffindexfile: " + welltosfftfindexfile);
        p("TF welltosffindexfile exists? " + welltosfftfindexfile.exists());
    }

    private String getLastPart(String file) {
        file = ToolBox.replace(file, "\\", "/");
        int sl = file.lastIndexOf("/");
        if (sl > -1) {
            file = file.substring(sl + 1);
        }
        return file;
    }

    private File findFileSomewhere(String filename) {
        if (filename == null || filename.length() < 1) {
            return null;
        }
        File f = new File(filename);
        if (f.exists()) {
            return f;
        }

        filename = getLastPart(filename);
        f = new File(this.results_path + filename);
        if (f.exists()) {
            //   p("Found " + filename + " in results path");
            return f;
        }

        f = new File(this.plugin_dir + filename);
        if (f.exists()) {
            //    p("Found " + filename + " in plugin path");
            return f;
        }

        f = new File(this.cache_dir + filename);
        if (f.exists()) {
            //      p("Found " + filename + " in cache");
            return f;
        }

     //   p("Could NOT find file: " + filename + " in resultspath, plugin dir or cache dir");
        if (FileUtils.canWrite(results_path)) {
            //   p("Not found, but can write in results path");
            f = new File(results_path + filename);

        } else if (FileUtils.canWrite(plugin_dir)) {
            //  p("Not found, but can write in plugin path");
            f = new File(plugin_dir + filename);
        } else {
            //   p("Not found, using cache");
            f = new File(this.cache_dir + filename);
        }
        //  p("findFileSomewhere:" + filename + "->" + f);
        //   p("cache would be: " + cache_dir);
        return f;
    }

    public File getWellsToSffIndexFile() {
        return welltosffindexfile;
    }

//    public File getSamIndexFile() {
//        return this.samindexfile;
//    }
    private boolean checkAndMaybeCacheSamFile(String file) {
        if (samfile != null && samfile.exists()) {
            return true;
        }

        if (samfile != null && !samfile.exists()) {
            samfile = this.findFileSomewhere(samfile.getName());
        }

        if (file == null) {
            file = findFile(".sam", false);
            if (file == null) {
                msg("Could not find any sam file");
                return false;
            } else {
                // p("Found file " + file);
                samfile = new File(file);
                // getBamOrSamIndexFileName();
                return true;
            }
        }

        File f = this.findFileSomewhere(file);
        //  p("Checking if we can find sam file " + file);
        if (f == null || !f.exists()) {
            f = FileUtils.findAndCopyFileFromUrlTocache(file, this.cache_dir, this.results_path, false, false, null, 1024 * 1024);
        }

        if (f != null) {
            samfile = f;
        }
        //   p("sam file is: " + samfile);
        if (samfile == null) {
            createSamFile(cache_dir);
        }
        //p("sam file is: " + samfile);
        if (samfile != null) {
            //  getBamOrSamIndexFileName();
            return true;
        } else {
            msg("I found no sam/bam file");
            return false;
        }
    }

    private boolean checkAndMaybeCacheBamFile(String file, boolean ask) {
        if (bamfile != null) {
            return true;
        }

        if (file == null && ask) {
            file = findFile(".bam", ask);
            if (file == null) {
                msg("Cold not find any BAM file");
                return false;
            } else {
                // p("Found file " + file);
                bamfile = new File(file);

                return true;
            }
        }
        //  p("Checking if we can find sam file " + file);
        File f = findFileSomewhere(file);

        if (f == null || !f.exists()) {
            f = FileUtils.findAndCopyFileFromUrlTocache(file, this.cache_dir, this.results_path, false, false, null, 1024 * 1024);
        }

        if (f == null || !f.exists()) {
            file = findFile(".bam", ask);
            f = this.findFileSomewhere(file);
        }
        if (f != null) {
            bamfile = f;
        }
        //   p("sam file is: " + samfile);
        if (bamfile == null && file != null && file.length() > 4) {
            // now check for .bam files
            String samfile = file.substring(0, file.length() - 4) + ".sam";
            f = findFileSomewhere(samfile);
            if (f == null || !f.exists()) {
                f = FileUtils.findAndCopyFileFromUrlTocache(samfile, this.cache_dir, this.results_path, false, true, null, 1024 * 1024);
            }
            if (f == null) {
                msg("Found no .bam or .sam file in " + results_path + " or in cache " + cache_dir);
                return false;
            } else {
                // convert BAM to SAM. Inefficient, but we have no BAM indexer ;-)
                p("Converting SAM to BAM");
                File newbamfile = new File(cache_dir + file);
                sam.convertSamToBam(newbamfile, new File(samfile));
                if (!newbamfile.exists()) {
                    err("Could not convert sam " + samfile + " to bam  " + newbamfile);
                    return false;
                } else {
                    bamfile = newbamfile;
                }
            }
        }
        //p("sam file is: " + samfile);
        if (bamfile != null) {
            return true;
        } else {
            msg("I found no bam file");
            return false;
        }
    }

    public String getMsg() {
        return msg;
    }

    public File getSamFile() {
        return samfile;
    }

    public boolean foundBai() {
        if (sam == null) {
            sam = getSamUtils();
        }
        if (sam == null) {
            p("foundBai: no sam object");
            return false;
        }
        boolean b = sam.hasBai();
        p("foundBai: " + b);
        return b;
    }

//    private class SamIndexTask extends Task {
//
//        public SamIndexTask(TaskListener tlistener) {
//            super(tlistener);
//        }
//
//        @Override
//        public Void doInBackground() {
//            //p("Starting to create sam index");
//            try {
//                sam.createWellToSamIndex();
//            } catch (Exception e) {
//                msg("Got an error in index creation: " + e.getMessage());
//            }
//            // p("Done with sam index");
//            GuiUtils.showNonModelMsg("SAM/BAM index creation done...");
//            samindextask = null;
//            return null;
//        }
//
//        public boolean isSuccess() {
//            return sam.hasWellToReadIndex();
//        }
//    }
    private class BamWellToLocIndexTask extends Task {

        public BamWellToLocIndexTask(TaskListener tlistener) {
            super(tlistener);
        }

        @Override
        public Void doInBackground() {
            //p("Starting to create sam index");
            try {
                sam.createWellToLocIndex();
            } catch (Exception e) {
                msg("Got an error in createWellToLocIndex creation: " + e.getMessage());
            }
            // p("Done with sam index");
            GuiUtils.showNonModalMsg("BAM createWellToLocIndex creation done...");
            welltolocindextask = null;
            return null;
        }

        public boolean isSuccess() {
            return sam.hasWellToLocIndex();
        }
    }

    private class WellToSffIndexTask extends Task {

        WellToSffIndex index;
        TaskListener client;

        public WellToSffIndexTask(TaskListener client, TaskListener tlistener, WellToSffIndex index) {
            super(tlistener);
            this.index = index;
            this.client = client;

        }

        @Override
        public Void doInBackground() {
            if (index == null) {
                err("Got no SffIndex object in SffIndexTask");
            }
            createSffIndex(index, context.getNrcols(), context.getNrrows());
            GuiUtils.showNonModalMsg("SFF index creation done, data will now show!");
            return null;
        }

        public boolean isSuccess() {
            return index.readIndex();
        }
    }

    private class KmerToSffIndexTask extends Task {

        KmerSffIndex index;
        boolean ok;

        public KmerToSffIndexTask(TaskListener tlistener, KmerSffIndex index) {
            super(tlistener);
            this.index = index;

        }

        @Override
        public Void doInBackground() {
            if (index == null) {
                err("Got no KmerSffIndex object in SffIndexTask");
            }
            ok = createKmerSffIndex(index);
            GuiUtils.showNonModalMsg("SFF KmerSffIndex creation done, data will now show!");
            return null;
        }

        public boolean isSuccess() {
            return ok;
        }
    }

    private boolean createKmerSffIndex(KmerSffIndex index) {
        if (index == null) {
            err("createKmerSffIndex: No SffIndx object...");
        }
        try {
            p("Creating KmerSffIndex index on file " + index.toString());
            boolean ok = index.createIndex();
            return ok;
        } catch (Exception ex) {
            ex.printStackTrace();
            msg("Got an error while creating KMER index for sff file:" + ex.getMessage());
            return false;


        }

    }

    private boolean createSffIndex(WellToSffIndex index, int nrcols, int nrrows) {
        if (index == null) {
            err("createSffIndex: No SffIndx object...");
        }
        try {
            p("Creating sff index on file " + index.toString()+ " for "+nrcols+"  cols and "+nrrows+" rows");
            boolean ok = index.createIndex(nrrows, nrcols);
           
            if (!ok) {
                p("Failed to create sffindex, will try cache folder");
                File sff = new File(index.getSffFile());
                
                 String key = this.context.getFileKey();               
                String filename = sff.getName() + key+ ".idx";
                File indexfile = new File(cache_dir + filename);
                index = new WellToSffIndex(sff.toString(), indexfile, this.context);
                ok = index.createIndex(nrrows, nrcols);
                // try cache folder                
            }
            if (!ok) {
                msg("failed to create " + welltosffindexfile);
            }
            else {
                 p("Testing just newly created index");
                 index.testIndex(10);
            }
            return ok;
        } catch (Exception ex) {
            p(ErrorHandler.getString(ex));
            msg("Got an error while indexing sff file:" + ex.getMessage());
            return false;
        }
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(SequenceLoader.class.getName()).log(Level.WARNING, msg, ex);
    }

    private void err(String msg) {
        this.msg = msg;
        Logger.getLogger(SequenceLoader.class.getName()).log(Level.WARNING, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(SequenceLoader.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("SequenceLoader: " + msg);
        Logger.getLogger( SequenceLoader.class.getName()).log(Level.INFO, msg);
    }
}
