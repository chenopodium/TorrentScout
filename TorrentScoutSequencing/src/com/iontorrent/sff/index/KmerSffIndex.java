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
package com.iontorrent.sff.index;

import com.iontorrent.sff.Sff;
import com.iontorrent.sff.SffRead;
import com.iontorrent.utils.io.FileTools;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.iontorrent.seq.Kmer;
import org.iontorrent.seq.indexing.AbstractLargeIndexer;

/**
 *
 * @author Chantal Roth
 */
public class KmerSffIndex extends AbstractLargeIndexer {

    private String INDEXFILENAME = "kmertosff.idx";
    static final int KMER_SIZE = 10;
    String sfffile;
    File indexfile;
    String indexpath;
    int max_kmers_in_memory;
    int nr_chunks;
    Exception ex;

    public KmerSffIndex(String sfffile, String indexpath) {
        super();
        this.sfffile = sfffile;
        this.indexpath = indexpath;
        INDEXFILENAME = "kmertosff_" + sfffile.hashCode() + "_" + KMER_SIZE + ".idx";
        indexpath = FileTools.addSlashOrBackslash(indexpath);
        this.indexfile = new File(indexpath + INDEXFILENAME);
        initminimum();
    }

    public void initminimum() {
        max_kmers_in_memory = (int) (Runtime.getRuntime().freeMemory() / 30);
        p("Max nr of kmers in memory: " + max_kmers_in_memory);
        // dont want to use all of the memory just for the index!!!
        dictionary_size = max_kmers_in_memory / 5;


    }

    public Exception getException() {
        return ex;
    }

    private ArrayList<Long> findLocationsByScanning(String sub) {
        ArrayList<Long> locs = new ArrayList<Long>();

        Sff sff = new Sff(sfffile);
        if (sff == null) {
            err("Could not open file " + sfffile);
            return null;
        }
        sff.openFile();
        long prev_pos = sff.readHeader();
        //    p("sff global header:" + sff.getGheader().toString());
        if (prev_pos <= 0) {
            err("fp after opening sff " + sfffile + " is " + prev_pos);
            return null;
        }
        int nr = 0;
        boolean done = false;
        int count = 0;
        while (!done) {

            if (nr % 50000 == 0) {
                p("Scanning read " + (nr));
            }
            nr++;
            SffRead sffread = null;
            try {
                sffread = sff.readNextRead();

            } catch (Exception e) {
                err("got an error:" + e);
                done = true;

            }

            if (sffread == null) {
                p("We got null and are DONE for scanning " + sub);
                done = true;
            } else {
                // now loop over whole sequence and add kmers!
                String seq = sffread.getBases();
                if (seq.indexOf(sub) > -1) {
                    locs.add(new Long(prev_pos));
                }
                if (prev_pos <= 0) {
                    err("read name " + sffread.getName() + " has no pos: " + prev_pos + ", read is: " + sffread.toString());
                }
                prev_pos = sff.getFilePointer();
                if (-1L == prev_pos) {
                    err("Error getting file pointer from sff");
                }
                count++;
            }
        }
        //p("Got " + count + " reads with a positive offset processed " + nr + " reads, writing it");

        return locs;
    }

    private ArrayList<Long> findLocations(Kmer kmer, boolean useindex) {
        int kmerSize = kmer.getLength();
        if (kmerSize < KMER_SIZE || useindex) {
         
            return findLocationsByScanning(kmer.toSequenceString());
        } else if (kmerSize == KMER_SIZE) {
            //p("kmer size is default size:"+defaultSize);
            ArrayList<Long> res = findLocations_r(kmer, false);
            if (res == null || res.size() < 1 && kmer.isCorrect()) {
                p("Indexer: " + this.getClass().getName() + ":Coulnd't find correct kmer! Will try again with debug on for kmer \n" + kmer);
                res = findLocations_r(kmer, true);
                err("Could not find CORRECT kmer " + kmer.toSequenceString() + ", code " + kmer.computeCode() + " in index.\nIndex file is:"
                        + this.getFileName(1) + ", \nssff file:" + sfffile);
            }
            return res;
        }
        //show = true;
        int delta = kmerSize - KMER_SIZE + 1;
      
        String str = kmer.toSequenceString();
        ArrayList<Long> locs = new ArrayList<Long>();

        for (int d = 0; d < delta; d++) {
            Kmer mer = new Kmer(str.substring(d, d + KMER_SIZE), d);
            ArrayList<Long> merlocs = findLocations_r(mer, false);

            if (merlocs == null || merlocs.size() < 1) {
                if (merlocs == null || merlocs.size() < 1 && kmer.isCorrect()) {
                    merlocs = findLocations_r(mer, true);
                    err("Could not find CORRECT kmer " + kmer.toSequenceString() + ", code " + kmer.computeCode() + " in index.\nIndex file is:" + this.getFileName(1) + ", \nsfffile=" + this.sfffile);
                }
                //	p("Could not find sub kmer "+mer and it is NOT a repeat);
                return null;

            } else {
                if (d == 0) {
                    locs.addAll(merlocs);
                } else {
                    for (int j = 0; j < merlocs.size(); j++) {
                        Long loc = new Long(merlocs.get(j).longValue() - d);
                        locs.add(loc);
                        //	if (show) p("   adding potential loc: "+loc);
                    }
                }
            }
        }
        Collections.sort(locs);    
        kmer.setFrequency(locs.size());
        // now should have a list of locs like: 11,11,11,11,11, 14, 14, 16, 19, 19, 19
        // each number should appear DELTA times
        //p("Found longer kmer "+kmer+" at:"+found);
        return locs;
    }

    private ArrayList<Long> findLocations_r(Kmer kmer, boolean show) {
        long code = kmer.computeCode();

        checkAndLoadFiles();
        //	show = true;
        if (show) {
            p("Trying to find kmer " + code);
        }
        ArrayList<String> locs = find(code, show);
        ArrayList<Long> good = new ArrayList<Long>();
        if (locs == null) {
            return null;
        }
        for (String spos : locs) {
            long pos = Long.parseLong(spos);
            // check
            good.add(pos);
        }

        return good;

    }

    public ArrayList<SffRead> findReads(String subseq, boolean useindex) {
        Kmer kmer = new Kmer(subseq, 0);
        return findReads(kmer, useindex);
    }

    private ArrayList<SffRead> findReads(Kmer kmer, boolean useindex) {
        ArrayList<Long> fps = findLocations(kmer, useindex);
        String seq = kmer.toSequenceString();
        ArrayList<SffRead> reads = new ArrayList<SffRead>();
        for (long fp : fps) {
            SffRead r = getRead(fp);
            if (r != null) {
                // checkif kmer is part of sequence
                if (r.getBases().indexOf(seq) > -1) {
                    reads.add(r);
                }
            }
        }
        return reads;
    }

    public SffRead getRead(long fp) {
        ex = null;
        try {
            if (fp <= 0) {
                p("Offset is 0 for readIndex  -> we got no data");
                return null;
            }
            //  p("Offset is " + fp);
            //      p("find Read at " + row + "/" + col+" in "+sfffile);
            Sff sff = new Sff(sfffile);
            sff.openFile();
            sff.readHeader();
            if (sff.getGheader() == null) {
                err("Could not read header for some reason...");
                return null;
            }
            //   p("Moving to  " + fp);
            sff.seek(fp);
            SffRead read = null;
            try {
                read = sff.readNextRead();
            } catch (Exception e) {
                err("find Read at " + e);


            }
            if (read == null) {
                err("Could not read at " + fp + ", got null");

                return null;
            }

            return read;
        } catch (Exception ex) {
            this.ex = ex;
            Logger.getLogger(WellToSffIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public boolean createIndex() throws Exception {
        HashMap<Long, ArrayList<String>> map = createKmerIndex(KMER_SIZE);
        writeIndex(map);
        p("Creating merged index file");
        createMergedIndexFile();
        p("crateing dictionary");
        createDictionary();
        return this.hasIndex();
    }

    public HashMap<Long, ArrayList<String>> createKmerIndex(int kmersize) throws Exception {

        p("Creating index " + indexfile + " for file " + sfffile + ", kmer size " + kmersize);

        HashMap<Long, ArrayList<String>> map = new HashMap<Long, ArrayList<String>>();
        Sff sff = new Sff(sfffile);
        if (sff == null) {
            err("Could not open file " + sfffile);
            return null;
        }
        sff.openFile();
        long prev_pos = sff.readHeader();
        //    p("sff global header:" + sff.getGheader().toString());
        if (prev_pos <= 0) {
            err("fp after opening sff " + sfffile + " is " + prev_pos);
            return null;
        }
        int nr = 0;
        boolean done = false;
        int count = 0;
        while (!done) {

            if (nr % 50000 == 0) {
                p("Processing read " + (nr));
            }
            nr++;
            SffRead sffread = null;
            try {
                sffread = sff.readNextRead();

            } catch (Exception e) {
                err("got an error:" + e);
                done = true;
            }

            if (sffread == null) {
                p("We got null and are DONE for readIndex " + nr);
                done = true;
            } else {

                // now loop over whole sequence and add kmers!
                String seq = sffread.getBases();
                int len = seq.length();

                for (int i = 0; i < len - kmersize; i++) {
                    Kmer kmer = new Kmer(seq.substring(i, i + kmersize), i);
                    createIndexForKmer(kmer, map, prev_pos);
                }
                if (prev_pos <= 0) {
                    err("read name " + sffread.getName() + " has no pos: " + prev_pos + ", read is: " + sffread.toString());
                }
                prev_pos = sff.getFilePointer();
                if (-1L == prev_pos) {
                    err("Error getting file pointer from sff");
                }
                count++;
            }
        }
        //p("Got " + count + " reads with a positive offset processed " + nr + " reads, writing it");

        return map;
    }

    public void createIndexForKmer(Kmer kmer, HashMap<Long, ArrayList<String>> map, long filepos) {
        long windowCode = kmer.computeCode();

        // add to hashmap
        Long key = new Long(windowCode);
        ArrayList<String> locs = (ArrayList<String>) map.get(key);
        if (locs == null) {
            locs = new ArrayList<String>();
        }
        locs.add("" + filepos);
        map.put(key, locs);
//				
        nrrows++;

        //if (nrrows % 1000 == 0) p("Got "+nrrows+"  kmers");
        // check if max nr kmers have been read.
        if (nrrows >= this.max_kmers_in_memory) {
            p("Got " + nrrows + "  kmers, writing!");
            //write map to next index file and sort it
            // clear map, increment file nr			
            writeIndex(map);
        }

    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(KmerSffIndex.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(KmerSffIndex.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(KmerSffIndex.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
//  System.out.println("KmerSffIndex: " + msg);
        //Logger.getLogger( KmerSffIndex.class.getName()).log(Level.INFO, msg);
    }

    @Override
    protected String getFileName(int filenr) {
        return indexpath + INDEXFILENAME + "." + filenr;
    }

    @Override
    protected String getDictFile() {
        return indexpath + INDEXFILENAME + ".dict";
    }

    @Override
    protected String getMergedIndex() {
        return indexpath + INDEXFILENAME;
    }

    public boolean hasIndex() {
        return new File(this.getMergedIndex()).exists();
    }

    public int getKmerSize() {
        return KMER_SIZE;
    }
}
