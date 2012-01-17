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
package org.iontorrent.seq.sam;

import com.iontorrent.utils.ToolBox;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.util.CloseableIterator;
import org.iontorrent.seq.Coord;
import org.openide.util.Exceptions;

/**
 *
 * @author Chantal Roth
 */
public abstract class AbstractSamIndex {

    HashMap<String, Long> map;
    File samfile;
    File indexfile;
    SeekableRAStream in;

    public AbstractSamIndex(File samfile, File indexfile) {
        this.samfile = samfile;
        this.indexfile = indexfile;
    }

    public boolean hasIndex() {
        if (map == null || map.isEmpty()) {
            boolean ok = load();
            return ok;
        }
        return true;
    }
    protected void addKey(String key, long pos) {
        if (map == null) {
            map = new HashMap<String, Long>();
        }
        
        map.put(key, pos);
    }
    private boolean checkAndCreateIndex() {
        if (map == null) {
            boolean ok = load();
            if (!ok) {
                p("Will create sam index first");
                createIndex();
                ok = load();
                if (!ok) {
                    err("Could not create index "+this.indexfile);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkAndCreateInputStream() {
        if (in == null) {
            if (!samfile.exists()) {
                err("File " + samfile + " does not exist");
                return false;
            }
            in = null;
            try {
                in = new SeekableRAStream(samfile);
            } catch (Exception ex) {
                err("Could not open file " + samfile, ex);
                return false;
            }

        }
        return true;
    }

    private long findPosition(int x, int y) {
        if (map == null) {
            err("NO map, read index first");
            return -1;
        }
        String key = getKey(x, y);
        
        Long Pos = map.get(key);
        //p("Got map: "+map.size()+" and key: "+key+"->"+Pos);
        if (Pos == null) return -1;
        else return Pos.longValue();
        
    }

    public SAMRecord findSequence(int x, int y) {
        if (!checkAndCreateIndex()) {
            return null;
        }

        long pos = findPosition(x, y);
        if (pos < 0) {
            err("Could not find a sequeence with key " + getKey(x, y));
            return null;
        } else {
       //     p("findSequence Found pos " + pos + " for " + x + "/" + y+" and key "+getKey(x,y));
        }
        if (!checkAndCreateInputStream()) {
            return null;
        }
        try {
            in.seek(0);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
     //   p("findSequence Moving to pos: " + pos);
        MySamTextReader reader = new MySamTextReader(in, samfile);
       // p("SAM header: "+reader.getFileHeader().getTextHeader());


        try {
            in.seek(pos);
        } catch (IOException ex) {
            err("Could not move to pos " + pos, ex);
            return null;
        }
     //   p("Am now at " + pos + ", getting iterator");
        CloseableIterator<SAMRecord> it = reader.getIterator();


      //  p("Getting next record");
        SAMRecord rec = it.next();


        return rec;
    }

    protected boolean createIndex() {
        if (!samfile.exists()) {
            err("File " + samfile + " does not exist");
            return false;
        }
        in = null;
        try {
            in = new SeekableRAStream(samfile);
        } catch (Exception ex) {
            err("Could not open file " + samfile, ex);
            return false;
        }

      //  p("Creating new sam reader with seekable input for " + samfile);
        MySamTextReader reader = new MySamTextReader(in, samfile);
      //  p("Before reading header, fp="+in.getFilePointer());
      //  reader.readHeader();
                        
        CloseableIterator<SAMRecord> it = reader.getIterator();

        long curpos = in.getFilePointer();
        for (int i = 0; it.hasNext(); i++) {
            long pos = in.getFilePointer();
           
            SAMRecord rec = it.next();
            addRecordToIndex(rec, curpos);
            curpos = pos;
        }
        return store();

    }
 
    protected abstract void addRecordToIndex(SAMRecord rec, long curpos) ;
   
    public static Coord extractWellCoord(String name) throws NumberFormatException {
        //p("Got name:"+name);
        name = name.trim();
        int col = name.indexOf(":");
        try {
            if (col > 0) {
                name = name.substring(col + 1);
                // get x and y
                if (name.startsWith(":")) {
                    name = name.substring(1);
                }
                col = name.indexOf(":");

                if (col > 0) {
                    String sx = name.substring(0, col);
                    String sy = name.substring(col + 1);

                    int x = Integer.parseInt(sx);
                    int y = Integer.parseInt(sy);
                    // CHECK IF THIS ORDER IS RIGHT!!!
                    // XXX, yes THIS is right for the sam file (something wrong with default.sam.parsed!)
                    // as whoever creates the read names
                    Coord coord = new Coord(y, x);
                    return coord;

                } else {
                    p("Name has no second colunm: " + name);
                }
            } else {
                p("Name has no column: " + name);
            }
        }
        catch (NumberFormatException e) {
            err("Cannot extract coords from "+name);
        }
        return null;
    }

    private boolean store() {
        if (map == null) {
            err("NO map - we have no index to save");
            return false;
        }
      
        ToolBox.saveObject(map, indexfile);
        p("Saved index :" + map.size() + " to index file " + indexfile);
        return true;
    }

    private boolean load() {
        if (indexfile == null) {
            err("Got no index file, it is null");
            return false;
        }
        if (!indexfile.exists()) {
            p("AbstractSamIndex.load: no index file '" + indexfile+"'");
            return false;
        }
    //    else p(indexfile+" exists");
        map = (HashMap<String, Long>) ToolBox.readObject(indexfile);
        if (map == null) {
            err("Error reading index " + indexfile);
            return false;
        }
    //    p("Loaded index :" + map.size() + " from " + indexfile);
        return true;
    }

    protected abstract String getKey(int x, int y);

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(AbstractSamIndex.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        Logger.getLogger(AbstractSamIndex.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(AbstractSamIndex.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("AbstractSamIndex: " + msg);
       Logger.getLogger(AbstractSamIndex.class.getName()).log(Level.INFO, msg);
    }
}
