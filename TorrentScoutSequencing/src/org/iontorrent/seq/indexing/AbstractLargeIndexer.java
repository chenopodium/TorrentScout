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
package org.iontorrent.seq.indexing;

import com.iontorrent.utils.StringTools;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public abstract class AbstractLargeIndexer {

    private static final int MAX_CACHE = 50000;
    protected int cur_file_nr;
    protected int dictionary_size;
    protected HashMap<Long, Long> dictmap;
    protected ArrayList<Long> dictcodes;
    protected int nrrows;
    protected RandomAccessFile index;
    protected HashMap<Long, ArrayList<String>> cache;
    protected long lastFoundPos;

    public AbstractLargeIndexer() {
    }

    public void init() {
        //	p("free memory in kB:"+Runtime.getRuntime().freeMemory()/1000);	
    }

    public void createMergedIndexFile() {

        FileWriter outfw = null;
        try {
            String file = getMergedIndex();
            	p("Creating merged index file:"+file);
            outfw = new FileWriter(file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PrintWriter outpw = new PrintWriter(outfw);
        int nr_files = cur_file_nr;
        nr_files = Math.max(nr_files, 1);


        int smallest_file = 0;
        long smallest_key = Long.MIN_VALUE;
        long[] cur_values = new long[nr_files];
        String[] cur_lines = new String[nr_files];
        // open all files
        //p("nr files: " + nr_files);
        RandomAccessFile[] files = new RandomAccessFile[nr_files];
        for (int f = 0; f < nr_files; f++) {
            String file = this.getFileName(f + 1);
            //p("subindex:" + file);
            try {
                files[f] = new RandomAccessFile(file, "r");
                String line = files[f].readLine();
               // p("read index line " + line);
                if (line != null) {
                    long pos = extractCode(line);
                    cur_lines[f] = line;
                    cur_values[f] = pos;
                } else {
                    err("First line is null in file " + file);
                }

                // read first item
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        boolean done = false;
        long count = 0;
        //   p("Cur lines 0:" + cur_lines[0]);

        //KmerFrequencyStats stats = new KmerFrequencyStats(10000);
        while (!done) {
            // find smallest
            count++;
            smallest_key = Long.MIN_VALUE;
            for (int f = 0; f < nr_files; f++) {
                if (cur_values[f] > Long.MIN_VALUE && (smallest_key == Long.MIN_VALUE || smallest_key > cur_values[f])) {
                    smallest_key = cur_values[f];
                    smallest_file = f;
                }
            }
            //p("smallest is "+smallest_key);
            if (smallest_key == Long.MIN_VALUE) {
                done = true;
            } else {
                // get the line of the smallest value

                // merge locations with any other locations from other index files!
                String merged = "" + smallest_key + "\t";

                int same = 0;
                for (int f = 0; f < nr_files; f++) {
                    if (cur_values[f] == smallest_key) {
                        same++;
                        //append locs
                        if (cur_lines[f] != null) {
                            int tab = cur_lines[f].indexOf("\t") + 1;
                            merged = merged + cur_lines[f].substring(tab) + "\t";
                        }

                        //	if (same >2) p("found same key "+same+" times. Merged line so far:\n"+merged);
                        try {
                            if (files[f].getFilePointer() < files[f].length()) {
                                String line = files[f].readLine();
                                if (line != null) {
                                    long key = extractCode(line);
                                    cur_lines[f] = line;
                                    cur_values[f] = key;
                                }
                                else {
                                    cur_values[f] = Long.MIN_VALUE;
                                }
                            } else {
                                cur_values[f] = Long.MIN_VALUE;
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                merged = merged.trim();
                if (merged.indexOf("\t") < 1) {
                    err("Merged line has no tab :'" + merged + "', count=" + count);
                }
            
                if (count % 1000000 == 0) {
                    outpw.flush();
                 //   p("Appending line :"+merged+" from file "+smallest_file);
               //     p("Got "+count+" lines");
                }
                outpw.println(merged);

            }
        }

        outpw.flush();
        outpw.close();
        //  outrep.flush();
        // outrep.close();
    //     p("Merging done");
        //writeKmerFrequencyStats(stats);
     //   System.gc();
    }

    private int countElements(String string) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getLastFoundPos() {
        return lastFoundPos;
    }

    protected long findnl(long newpos) {
        if (newpos < 0) {
            return 0;
        } else {
            try {
                if (newpos >= index.length()) {
                    return index.length();
                }
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        long pos = newpos;
        try {
            index.seek(pos);
            char c = (char) index.readByte();
            while (pos > 0 && c != '\n') {
                pos--;
                index.seek(pos);
                //		p("Got char:"+c+"/"+(int)c+" at "+pos);
                c = (char) index.readByte();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return pos;
    }

    /** read merge file and create dictionary */
    protected void createDictionary() {
        FileWriter outfw = null;
        try {
            String file = getDictFile();
            //      p("Creating  dictionary:" + file);
            outfw = new FileWriter(file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PrintWriter outpw = new PrintWriter(outfw);

        RandomAccessFile merge = null;
        try {
            merge = new RandomAccessFile(getMergedIndex(), "r");
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            long pos = 0;
            int dictionary_size = (int) (Runtime.getRuntime().freeMemory() / 1000);
            File mer = new File(this.getMergedIndex());

            int nr_keys = (int) Math.max(5, mer.length() / dictionary_size);
            //    p("keys per dict entry: " + nr_keys);
            //      p("dict size: " + dictionary_size);
            int keynr = 0;
            String line = null;
            int count = 0;
            //    p("reading merge: " + mer + " of size " + mer.length());
            String prevline = null;
            while (pos < merge.length()) {
                count++;
                pos = merge.getFilePointer();
                prevline = line;
                line = merge.readLine();
                if (line != null) {
                    keynr++;
                    if (count % 500000 == 0) {
                        //         p("processing line for dict: " + line);
                    }
                    if (keynr > nr_keys) {
                        long key = this.extractCode(line);
                        keynr = 0;
                        // write it
                        outpw.print(key + "\t" + pos);
                        //p("wrote "+key+"\t"+pos);
                        outpw.println();
                    }
                } else {
                    pos = merge.length() + 1;
                }
            }
            pos = merge.getFilePointer();
            if (line == null) {
                line = prevline;
            }
            if (line != null) {
                long key = this.extractCode(line);
                keynr = 0;
                // write it
                //	p("last key: "+key+" "+pos);
                outpw.print(key + "\t" + pos);
                outpw.println();
            }
            //else p("Got no last key, last line null");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            merge.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        outpw.flush();
        outpw.close();
        //   p("Wrote dict " + this.getDictFile());
     //   System.gc();
    }

    protected long extractCode(String line) {
        int t = line.indexOf("\t");
        if (t < 0) {
            err("No tab: " + line);
        }
        Long L = Long.parseLong(line.substring(0, t));
        //p("Got key:"+L+" from "+line);
        return L.longValue();
    }

    protected void processStatistics() {
    }

    public void writeIndex(HashMap<Long, ArrayList<String>> map) {
        this.cur_file_nr++;

        String file = getFileName(cur_file_nr);
     //   p("Writing map to file " + file);
        writeMapToFile(map, file);
        processStatistics();
        map.clear();
        nrrows = 0;
   //     System.gc();
    }

    public void writeMapToFile(HashMap<Long, ArrayList<String>> map, String file) {
        FileWriter outfw = null;
        try {
            //       p("Writing to sub-index  file:" + file);
            outfw = new FileWriter(file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //   p("Writing sub-index with" + map.size() + " to file " + file);
        PrintWriter outpw = new PrintWriter(outfw);

        Set keys = map.keySet();
        ArrayList<Long> keyList = new ArrayList<Long>(keys);
        Collections.sort(keyList);

        int nrkeys = keyList.size();
        //   p("Nr keys: " + nrkeys);
//		if (nrkeys <1) {
//			p("Found no keys in map! Map is:"+map.toString());
//		}
        for (int i = 0; i < nrkeys; i++) {
            Long key = keyList.get(i);
            ArrayList<String> vals = map.get(key);
            if (i % 100000 == 0) {
                //          p("Writing :" + key + "/" + vals);
            }
            outpw.print(key.toString());
            int nrvals = vals.size();
            for (int j = 0; j < nrvals; j++) {
                String val = vals.get(j);
                outpw.print("\t");
                outpw.print(val.toString());
            }
            //}
            outpw.println();
        }
        outpw.flush();

        outpw.close();

    }

    protected boolean loadDictionary() {

        dictmap = new HashMap<Long, Long>();
        dictcodes = new ArrayList<Long>();
        String file = getDictFile();
     //   p("Reading dictionary " + file);
        int l = 0;
        try {
            BufferedReader r = new BufferedReader(new FileReader(file));
            String line = "";
            while (r.ready() && line != null) {
                line = r.readLine();
                l++;
                if (line != null) {
                    int tab = line.indexOf("\t");
                    if (tab < 1) {
                        warn("err or in dict line, no tab on line " + l + ":" + line);
                    } else {
                        Long L = Long.parseLong(line.substring(0, tab));
                        long code = L.longValue();
                        L = Long.parseLong(line.substring(tab + 1));
                        long filepos = L.longValue();
                        //	if (l % 100000 == 0) p("Dict line:"+line+". Code: "+code+":"+filepos);
                        dictmap.put(code, filepos);
                        dictcodes.add(code);
                    }
                }

            }
            r.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            err("Could not read dictionary "+file);
            return false;
        }

        Collections.sort(dictcodes);
        return true;
        //	p("Dict loaded and sorted");
    }

    protected ArrayList<String> find(long code, boolean show) {

        if (dictcodes == null || dictcodes.size()<1) {
            err("Got no dictinary: "+dictcodes);
            return null;
        }
        if (cache == null) {
            cache = new HashMap<Long, ArrayList<String>>();
        } else {
            ArrayList<String> res = cache.get(new Long(code));
            if (res != null && res.size() > 0) {
                if (show) {
                    p("got result from cache: " + code);
                }
                return res;
            }
        }
        // check cache first

        long prevcode = 0;
        long nextcode = 0;
        int prevpos = 0;
        int nextpos = 0;
        // find closest codes in dict
        if (show) {
            p("Finding code " + code + " in dictionary..., dict size: " + dictcodes.size());
        }
        //	long t0 = System.currentTimeMillis();
        int res = Collections.binarySearch(dictcodes, new Long(code));
        if (res >= 0) {
            // dictcode was actually found
            prevpos = Math.max(res - 1, 0);
            nextpos = Math.min(res + 1, dictcodes.size() - 1);
        } else {
            // key was not found, and res= -insertion point -1 is returned
            int inspoint = -(res + 1);
            prevpos = Math.max(inspoint - 1, 0);
            nextpos = Math.min(inspoint, dictcodes.size() - 1);
        }
        prevcode = new Long(dictcodes.get(prevpos));
        nextcode = new Long(dictcodes.get(nextpos));
        if (show) {
            p("Found dictlocs for code=" + code + " in dict: prev:" + prevcode + ", next:" + nextcode);
        }
        long prevloc = dictmap.get(prevcode).longValue();
        long nextloc = dictmap.get(nextcode).longValue();
        if (prevloc == nextloc) {
            prevloc = 0;
        }
        if (show) {
            p(" dict: prev/next index file pos: " + prevloc + "/" + nextloc);
        }
        ArrayList<String> locs = find(code, prevloc, nextloc, show);
//		long dt = System.currentTimeMillis()-t0;
//		p("Search took:"+dt+" ms. 1Mio searches: "+dt*100/6+" minutes");
        // add to cache
        if (cache.size() > MAX_CACHE) {
            cache.clear();
        }
        cache.put(new Long(code), locs);
        return locs;
    }
    // binary search to find actual locations

    protected ArrayList<String> find(long code, long prevloc, long nextloc, boolean show) {
        ArrayList<String> locs = new ArrayList<String>();

        long pos = findnl(prevloc + nextloc) / 2;
        if (show) {
            p(" ===================================== ");
            p("Finding " + code + " in index file between file pos " + prevloc + "-" + nextloc + ", starting at:" + pos);
        }

        boolean done = false;
        long fcode = 0;

        String line = null;
        int count = 0;
        ArrayList<Long> old = new ArrayList<Long>();
        try {
            while (!done && count < 30 && nextloc - prevloc > 5) {
                try {
                    if (pos >= index.length()) {
                        pos = index.length() - 1;
                    }
                    count++;

                    index.seek(pos);
                    if (pos > 0) {
                        char c = (char) index.readByte();
                        if (c != '\n') {
                            String l = index.readLine();
                            pos = index.getFilePointer();
                            //warn("Don't see a newline:"+c+"/"+(int)c+" at "+pos+", reading to end of line:"+l);
                        }
                        //		else p("got a nl:"+c);
                    }
                    line = index.readLine();
                    if (show) {
                        p("Got line:" + line + " @ pos=" + pos);
                    }
                    //pos = index.getFilePointer();
                    Long P = new Long(pos);
                    if (old.contains(P)) {
                        if (show) {
                            p("already has pos " + P + ":" + old + ", done");
                        }
                        done = true;
                    } else {

                        //	old.add(P);
                        if (line == null || line.trim().length() < 1) {
                            if (show) p("line is null or has no length: "+line);
                            done = true;
                        } else {
                            int tab = line.indexOf("\t");
                            if (tab < 1) {
                                warn("err or in index line, no tab at filepos " + pos + ":\n" + line);
                                pos = index.getFilePointer();
                            } else {
                                Long L = Long.parseLong(line.substring(0, tab));
                                fcode = L.longValue();

                                if (code == fcode) {
                                    ArrayList<String> items = StringTools.splitString(line, "\t");
                                    for (int i = 1; i < items.size(); i++) {
                                        locs.add(items.get(i));
                                    }
                                    if (show) {
                                        p("Found locs at pos " + pos + " for code:" + code + "=" + fcode + "=>" + locs);
                                    }
                                    lastFoundPos = pos;
                                    return locs;
                                } else if (code < fcode) { // move left
                                    nextloc = pos;
                                    long p = pos;
                                    long newpos = (prevloc + Math.min(nextloc, pos)) / 2;
                                    long delta = Math.abs(prevloc - pos);
                                    if (delta <= 2 * line.length()) {
                                        newpos = findnl(pos - line.length() - 10);
                                        if (show) {
                                            p("short delta, getting previous line " + newpos + " old pos was:" + p);
                                        }
                                    }
                                    pos = findnl(newpos);

                                    if (show) {
                                        p(code + "<" + fcode + ", delta=" + delta + ", moving left to " + pos + ", old pos was " + p);
                                    }

                                } else { // code < fcode, move right
                                    prevloc = Math.min(nextloc - 1, pos + line.length());
                                    // next left border is end of this line
                                    // at least next line
                                    long newpos = (nextloc + Math.max(prevloc, pos + line.length() + 1)) / 2;
                                    long delta = Math.abs(nextloc - pos);
                                    if (delta <= line.length()) {
                                        newpos = pos + line.length() + 1;
                                        if (show) {
                                            p(code + ">" + fcode + ",short delta, getting next line " + newpos);
                                        }
                                    }
                                    pos = findnl(newpos);
                                    if (show) {
                                        p(code + ">" + fcode + ", delta=" + delta + ", moving right from " + newpos + " to " + pos);
                                    }
                                    // to next line!

                                }
                            }

                        }
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    err("find: Got an exception: "+e);
                }

            }
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            err("find: Got an exception: "+e1);
        }
        if (count > 29) {
            try {
                err("Not done with binary search!\ncode=" + code + " between " + prevloc + "/" + nextloc + " after " + count + " binary cycles. \nPos=" + pos + ", \nline:" + line + "\nnext line:" + index.readLine());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (show) {
            p("Could not find code " + code + " between " + prevloc + "/" + nextloc + ". Count:" + count + ";\nline=" + line);
        }
        return locs;
    }

//	public ArrayList<Long> findLocations(int readnr) {
//		return find(readnr, false);
//	}	
    protected boolean checkAndLoadFiles() {
        if (dictmap == null || dictcodes == null) {

            boolean ok = loadDictionary();
            if (!ok) return false;
            try {
                String merged = getMergedIndex();
                //       p("Loading merged index file " + merged);
                index = new RandomAccessFile(merged, "r");
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                err("Could not read merged index "+this.getMergedIndex());
                return false;
            }
        }
        return true;
    }

    protected abstract String getFileName(int filenr);

    protected abstract String getDictFile();

    protected String getStatsFileName() {
        return this.getMergedIndex() + ".csv";
    }

    protected abstract String getMergedIndex();

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(AbstractLargeIndexer.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {

        Logger.getLogger(AbstractLargeIndexer.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(AbstractLargeIndexer.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
    //    System.out.println("AbstractLargeIndexer: " + msg);
        //Logger.getLogger( AbstractLargeIndexer.class.getName()).log(Level.INFO, msg);
    }
}
