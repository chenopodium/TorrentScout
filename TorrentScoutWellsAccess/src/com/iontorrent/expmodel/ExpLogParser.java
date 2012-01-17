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

import com.iontorrent.utils.io.FileTools;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class ExpLogParser {

    private static String lastError;
    private static Exception lastException;
    private File file;
    private ArrayList<DatBlock> blocks;
    public ExpLogParser(String raw_dir) {
        
        file = new File(raw_dir+"explog.txt");        
    }
    
    public boolean hasFile() {
        return file.exists(); 
    }
    public boolean parse() {
        if (getFile() == null || !file.exists()) {
            err("File "+getFile()+" not found");
            return false;
        }
        ArrayList<String> lines = FileTools.getFileAsArray(getFile().toString());
        if (lines == null || lines.size()<1) {
            err("File "+getFile()+"  is empty: "+lines);
            return false;
        }
        blocks = new ArrayList<DatBlock>();
        for (String line: lines) {
            DatBlock b= DatBlock.parseLine(line);
            if (b != null) getBlocks().add(b);
        }            
        
        return true;
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
        Logger.getLogger(ExpLogParser.class.getName()).log(Level.SEVERE, msg, ex);
    }
    
    private static void err(String msg) {
        lastError = msg;
        Logger.getLogger(ExpLogParser.class.getName()).log(Level.SEVERE, msg);
    }
    
    private static void warn(String msg) {
        Logger.getLogger(ExpLogParser.class.getName()).log(Level.WARNING, msg);
    }
    
    private static void p(String msg) {
        //System.out.println("ExpLogParser: " + msg);
        Logger.getLogger(ExpLogParser.class.getName()).log(Level.INFO, msg);
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    public boolean hasBlocks() {
        return blocks != null && blocks.size()>0;
    }
    /**
     * @return the blocks
     */
    public ArrayList<DatBlock> getBlocks() {
        return blocks;
    }
}
