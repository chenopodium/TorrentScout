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

import com.iontorrent.utils.StringTools;
import com.iontorrent.utils.io.FileTools;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class Fstab {

    String file;
    String alternative;
    HashMap<String, String> map;
    
    public Fstab(String file, String alternative) {
        this.file = file;
        this.alternative = alternative;
        parseFile();
    }

    private void parseFile() {
        map = new HashMap<String, String>();
        File f = new File(file);
        ArrayList<String> lines = null;
        if (!f.exists()) {
            p("Cannot read fstab file " + file + ". It does not exist.");
            if (alternative != null && alternative.length() > 0) {
                lines = StringTools.splitString(alternative, "\n");
                p("Parsed alternative: "+lines);
            }
        }
        else lines = FileTools.getFileAsArray(file);
        if (lines == null)  return;
        
        for (String line: lines) {
            if (line == null || line.length()<1 || line.startsWith("#") || line.indexOf(":")<1){
                //skip
            }
            else {
                line = line.trim();
                ArrayList<String> items = StringTools.parseList(line, "\t ");
                if (items == null || items.size()<2) {
                    warn("Could not parse fstab line "+line+", got: "+items);
                }
                else {
                    String dev = items.get(0);
                    int col = dev.indexOf(":");
                    if (col < 0) {
                        warn("Defice "+dev+" has no ':'");
                    }
                    else {
                        dev = dev.substring(0, col);
                        String res = items.get(1);
                        if (res.startsWith("/")) res = res.substring(1);
                        p("Mapping "+res+"->"+dev);
                        map.put(res, dev);
                    }
                }
            }
        }
    }
    public String getDevice(String res) {
        if (res.startsWith("/")) res = res.substring(1);
        if (res.startsWith("\\")) res = res.substring(1);
        int s = res.indexOf("/");
        if (s > 0) res = res.substring(0, s);
        s = res.indexOf("\\");
        if (s > 0) res = res.substring(0, s);
        if (res.endsWith("\\")) res = res.substring(0, res.length()-1);
        if (res.endsWith("/")) res = res.substring(0, res.length()-1);
        p("Finding device with "+res);
        return map.get(res);
    }
    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(Fstab.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        
        Logger.getLogger(Fstab.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(Fstab.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("Fstab: " + msg);
        //Logger.getLogger( Fstab.class.getName()).log(Level.INFO, msg);
    }
}
