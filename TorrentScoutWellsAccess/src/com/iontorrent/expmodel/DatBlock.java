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
package com.iontorrent.expmodel;

import com.iontorrent.utils.StringTools;
import com.iontorrent.utils.io.FileTools;
import com.iontorrent.utils.io.FileUtils;
import com.iontorrent.wellmodel.WellCoordinate;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class DatBlock {

    private static String lastError;
    private static Exception lastException;

    private WellCoordinate start;
    private WellCoordinate end;
    
    public DatBlock(WellCoordinate start, WellCoordinate end) {
        this.start = start;
        this.end = end;
    }
    public boolean contains(int x, int y) {
        return getStart().getCol() <= x && getEnd().getCol() > x && 
                 getStart().getRow() <= y && getEnd().getRow() > y;
    }
    public int getWidth() {
        return getEnd().getCol() - getStart().getCol();             
    }
    public int getHeight() {
        return getEnd().getRow() - getStart().getRow();             
    }
    public boolean contains(WellCoordinate coord) {
        return contains(coord.getCol(), coord.getRow());
    }
    public String getRawBlockDir(String raw_dir) {
        String d= raw_dir+toShortString();
        d = FileTools.addSlashOrBackslash(d);
        return d;
    }
    public String getResultsBlockDir(String dir) {
        String d= dir+"block_"+toShortString();
        d = FileTools.addSlashOrBackslash(d);
        return d;
    }
    public String getCacheBlockDir(String dir) {
        String d= dir+"cache_"+toShortString();
        d = FileTools.addSlashOrBackslash(d);
        return d;
    }
     public String getDefaultResultsBlockDir(String dir) {
        String d= dir+"block_default";
        d = FileTools.addSlashOrBackslash(d);
        if (!FileUtils.exists(d)) {
            d= dir+"block_X0_Y0";
            d = FileTools.addSlashOrBackslash(d);
        }
        return d;
    }
      public String getDefaultCacheBlockDir(String dir) {
        String d= dir+"cache_default";
        d = FileTools.addSlashOrBackslash(d);
        return d;
    }
      public String getDefaultRawBlockDir(String dir) {
        String d= dir+"default";
        d = FileTools.addSlashOrBackslash(d);
        if (!FileUtils.exists(d)) {
            d= dir+"X0_Y0";
        }
        return d;
    }
   
    public static DatBlock parseLine(String line) {
        if (line == null) return null;
        if (!line.startsWith("BlockStatus")) {
            return null;
        }
        int pos = line.indexOf(":");
        if (pos < 1) {
            p("No : found: "+line);
            return null;
        }
        line = line.substring(pos+1) ;
        ArrayList<String> items = StringTools.parseList(line, ",");                
       //BlockStatus: X0, Y0, W1280, H1344, AutoAnalyze:0, AnalyzeEarly:0, nfsCopy:,  ftpCopy:// 
        if (items == null || items.size()<4) {
            warn("Could not extract block coords from line: "+line);
            return null;
        }
        int x = getInt(items.get(0).trim().substring(1));
        int y = getInt(items.get(1).trim().substring(1));
        int w = getInt(items.get(2).trim().substring(1));
        int h = getInt(items.get(3).trim().substring(1));
        
        if (x < 0 || y < 0 || w < 0 || h < 0) {
            warn("Could not parse coords:"+x+"/"+y+"/"+w+"/"+h+":"+line);
            return null;
        }
        WellCoordinate start = new WellCoordinate(x, y);
        WellCoordinate end = new WellCoordinate(x+w, y+h);
        DatBlock block = new DatBlock(start, end);
        p("Got block:"+block.toString());
        return block;
    }
    private static int getInt(String s){ 
        int i = -1;    
        try {
          i=  Integer.parseInt(s);
        }
        catch (Exception e) {
            p("Could not parse to int: "+s);
        }
        return i;
    }
        
    private void test() {
        String line = "BlockStatus: X0, Y0, W1280, H1344, AutoAnalyze:0, AnalyzeEarly:0, nfsCopy:,  ftpCopy:// ";
        DatBlock b= parseLine(line);
        p("Line: "+line);
        p("Block: "+b);
    }
    public String toShortString() {
        return "X"+getStart().getCol()+"_Y"+getStart().getRow();
    }
    @Override
    public String toString() {
        String s = toShortString();
        int w = getEnd().getCol() - getStart().getCol();
        int h = getEnd().getRow() - getStart().getRow();
        
        s+= ", W"+w+"_H"+h;
        
        return s;
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
        Logger.getLogger(DatBlock.class.getName()).log(Level.SEVERE, msg, ex);
    }
    
    private static void err(String msg) {
        lastError = msg;
        Logger.getLogger(DatBlock.class.getName()).log(Level.SEVERE, msg);
    }
    
    private static void warn(String msg) {
        Logger.getLogger(DatBlock.class.getName()).log(Level.WARNING, msg);
    }
    
    private static void p(String msg) {
        //System.out.println("DatBlock: " + msg);
        Logger.getLogger(DatBlock.class.getName()).log(Level.INFO, msg);
    }

    /**
     * @return the start
     */
    public WellCoordinate getStart() {
        return start;
    }

    /**
     * @return the end
     */
    public WellCoordinate getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(WellCoordinate end) {
        this.end = end;
    }
}
