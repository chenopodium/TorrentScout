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
package com.iontorrent.rawdataaccess.pgmacquisition;

import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public enum RawType {

    ACQ("acq", "Acquisition"),
    PRERUN("prerun", "Prerun"),
    BFPRE("beadfind_pre", "Beadfind pre"),
    BFPOST("beadfind_post", "Beadfind post");
    //String name;
    private String filename;
    private String description;

    RawType(String filename, String description) {
        this.filename = filename;
        this.description = description;
    }

    public static RawType getType(String type) {
        for (RawType s : EnumSet.allOf(RawType.class)) {
            if (s.name().equalsIgnoreCase(type)) {
                return s;
            }
        }
        return null;
    }

    public String toString() {
        return description;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /** get acquisition file */
    public String getRawFileName(int flow) {

        String sflow = "" + flow;
        for (int i = sflow.length(); i < 4; i++) {
            sflow = "0" + sflow;
        }
        String file = getFilename() + "_" + sflow + ".dat";
        return file;
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(RawType.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {

        Logger.getLogger(RawType.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(RawType.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("RawType: " + msg);
        //Logger.getLogger( RawType.class.getName()).log(Level.INFO, msg);
    }
}
