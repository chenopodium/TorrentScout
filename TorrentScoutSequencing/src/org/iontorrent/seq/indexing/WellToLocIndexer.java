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
import com.iontorrent.utils.io.FileTools;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class WellToLocIndexer extends AbstractLargeIndexer {

    // a, b, c,d,e,f, g, h, i, j, k, l, m,n, o, p, q ,r,s,t, u, v, w, x, y, z
    // the path that the output is written to
    protected String indexpath;
    protected static long BUCKET_SIZE = 8000;
    private String INDEXFILENAME = "welltoloc.idx";
    private String READLOCFILENAME = "readlocations.txt";
    private int max_locs_in_memory;
    private int nr_chunks;
    private long totalFileSize;
    //private int nr_readlocs;

    //String locToReadFile;
    public WellToLocIndexer(String indexpath, int code) {
        super();
        this.indexpath = indexpath;
        code = Math.abs(code);
        INDEXFILENAME = "welltoloc_"+code+".idx";
        READLOCFILENAME = "readlocations_"+code+".txt";
        init();
        this.indexpath = FileTools.addSlashOrBackslash(indexpath);
        

    }

    public void init() {
        max_locs_in_memory = (int) (Runtime.getRuntime().freeMemory() / 300);
        //	p("Max locs in memory: "+max_locs_in_memory);

        //	p("free memory in kB:"+Runtime.getRuntime().freeMemory()/1000);	


    }

    public static void main(String[] args) {
        // get start time of indexing
        LocToReadIndexer ind = new LocToReadIndexer("S:/data/beverly/cache", 0);
        String prog = "ALL";
        if (args == null || args.length < 1) {
            p("No arguments passed. Valid arguments are:");

            p("   -prog [all|merge|dict]");
        } else {
            for (int i = 0; i < args.length; i += 2) {
                String name = args[i];
                if (i + 1 >= args.length) {
                    warn("You need to pass an even nr of arguments in the form '-name value -name value' etc");
                    return;
                }
                String value = args[i + 1];
                int ival = 0;
                try {
                    ival = Integer.parseInt(value);
                } catch (Exception e) {
                    p("Couldn't parse " + value);
                }
                if (name.startsWith("-")) {
                    name = name.substring(1);
                }
                name = name.toLowerCase();
                if (name.startsWith("prog")) {
                    prog = value.toUpperCase();
                }
            }
        }

        ind.createLocToReadIndex();

    }


    private void updateLine(HashMap<Long, ArrayList<String>> map, String line) {
        int tab = line.indexOf("\t");
        if (tab < 1) {
            warn("err or in readloc, no tab on line " + nrrows + ":" + line);
        } else {
            String a = line.substring(0, tab).trim();

            Long pos = Long.parseLong(a);
            // bunch of reads...
            line = line.substring(tab + 1).trim();
            ArrayList<String> readposlist = StringTools.parseList(line, "\t");
            for (String rpline : readposlist) {
                ReadPos rp = ReadPos.fromString(rpline);
                if (rp != null) update(pos, rp, map);
            }
        }
    }

    public boolean createWellToLocIndex() {
       // long begin = System.currentTimeMillis();

        RandomAccessFile readloc_file = null;
       p("createWellToLocIndex: processing getReadLocationsFileName "+getReadLocationsFileName());
        try {
            readloc_file = new RandomAccessFile(getReadLocationsFileName(), "r");        
            totalFileSize = readloc_file.length();
        } catch (Exception e) {
            // TODO Auto-generated catch block
           err("Could not open file "+getReadLocationsFileName()+":"+e);
           return false;
        }

        nr_chunks = (int) (this.totalFileSize / max_locs_in_memory);
       // p("Nr chunks: " + nr_chunks);
        // dont want to use all of the memory just for the index!!!
        dictionary_size = max_locs_in_memory / 5;

        HashMap<Long, ArrayList<String>> map = new HashMap<Long, ArrayList<String>>();
        try {
            while (readloc_file.getFilePointer() < readloc_file.length()) {
                nrrows++;

                String line = readloc_file.readLine();
               // if (nrrows % 10000 == 0) p("Processing line: " + line);
                updateLine(map, line);

                if (nrrows > this.max_locs_in_memory) {
                    writeIndex(map);
                }
            }
        } catch (Exception e) {
            err("Could not create well to loc index: "+e);
        }
        writeIndex(map);
        createMergedIndexFile();
        createDictionary();

        // record time at end of indexing and output total time
      //  long end = System.currentTimeMillis();
       // long time = (end - begin) / 100 / 6;
    //    p("Total indexing took " + time + " minutes");
        return true;
    }

    public Long getHashCode(String name) {
       return new Long(Math.abs(name.trim().toUpperCase().hashCode()));
    }
    public void update(Long pos, Object readpos, HashMap<Long, ArrayList<String>> map) {
        // USE BUECKTS
        ReadPos rp = (ReadPos)readpos;
       
        Long hashcode = getHashCode(rp.readname);
        ArrayList<String> reads = map.get(hashcode);
        if (reads == null) {
            reads = new ArrayList<String>();
        }
        // also add read START and END to the list
        String spos = rp.toString();

        if (!reads.contains(spos)) {
            reads.add(spos);
           // p("Adding rp  "+spos+" to bucket "+Buck);
        }
        map.put(hashcode, reads);
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

    public String getReadLocationsFileName() {
        return indexpath + READLOCFILENAME;
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(WellToLocIndexer.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {

        Logger.getLogger(WellToLocIndexer.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(WellToLocIndexer.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("WellToLocIndexer: " + msg);
        //Logger.getLogger( WellToLocIndexer.class.getName()).log(Level.INFO, msg);
    }
}
