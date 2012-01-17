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
package com.iontorrent.rawdataaccess.wells;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * empty	1<<0	Indicates if the well does not contain a bead
bead	1<<1	Indicates if the well contains a bead
live	1<<2	Indicates if the well contains a bead with live template
dud	1<<3	Indicates if the well contains a bead with no live template
ambiguous	1<<4	Indicates if the well contains a bead with live template that may be either library or test fragment
testFrag	1<<5	Indicates if the well contains a bead with live test fragment template
library	1<<6	Indicates if the well contains a bead with live library template
pinned	1<<7	Indicates if the well went out of the system dynamic range at some point during the run
ignore	1<<8	Indicates if the well should be ignored in various calculations such as background signal derivation from empty wells
washout	1<<9	Indicates if the well contained a bead that washed out at some point during the run
exclude	1<<10	Indicates if the well should be excluded from all analysis as it is not fluidically addressable
keypass	1<<11	Indicates if the well generated a valid nucleotide sequence in the key flows
filteredBadKey	1<<12	Indicates if the well was filtered out due to generating an invalid nucleotide sequence in the key
filteredShort	1<<13	Indicates if the well was filtered out due to generating too short a nucleotide sequence
filteredBadPPF	1<<14	Indicates if the well was filtered out due to too many incorporating flows
filteredBadResidual	1<<15	Indicates if the well was filtered out due to the signal values fitting poorly         

 * @author Chantal Roth
 */
public enum BfMaskFlag {

    EMPTY(0, "empty", "Indicates if the well does not contain a bead"),
    BEAD(1, "bead", "Indicates if the well contains a bead"),
    LIVE(2, "live", "Indicates if the well contains a bead with live template"),
    DUD(3, "dud", "Indicates if the well contains a bead with no live template"),
    AMBIGUOUS(4, "ambiguous", "Indicates if the well contains a bead with live template that may be either library or test fragment"),
    TESTFRAG(5, "testfrag", "Indicates if the well contains a bead with live test fragment template"),
    LIBRARY(6, "library", "Indicates if the well contains a bead with live library template"),
    PINNED(7, "pinned", "Indicates if the well went out of the system dynamic range at some point during the run"),
    IGNORE(8, "ignore", "Indicates if the well should be ignored in various calculations such as background signal derivation from empty wells"),
    WASHOUT(9, "washout", "Indicates if the well contained a bead that washed out at some point during the run"),
    EXCLUDE(10, "exclude", "Indicates if the well should be excluded from all analysis as it is not fluidically addressable"),
    KEYPASS(11, "keypass", "Indicates if the well generated a valid nucleotide sequence in the key flows"),
    FBADKEY(12, "filtered bad key", "Indicates if the well was filtered out due to generating an invalid nucleotide sequence in the key"),
    FSHORT(13, "filtered short", "Indicates if the well was filtered out due to generating too short a nucleotide sequence"),
    FBADPPF(14, "filtered bad ppf", "Indicates if the well was filtered out due to too many incorporating flows"),
    FBADRESIDUAL(15, "filtered bad residual", "Indicates if the well was filtered out due to the signal values fitting poorly"),
    RAW(16, "raw signal summary", "Used for processing .dat files when no bfmask.bin files are present");
    
    public static BfMaskFlag[] DEFAULT_MASKS={PINNED, EMPTY, IGNORE , BEAD, LIVE, DUD, AMBIGUOUS, WASHOUT, EXCLUDE, KEYPASS, TESTFRAG, LIBRARY};
    
    private static final Map<Integer, BfMaskFlag> lookup = new HashMap<Integer, BfMaskFlag>();
    private static final Map<String, BfMaskFlag> namemap = new HashMap<String, BfMaskFlag>();
    static {
        for (BfMaskFlag s : EnumSet.allOf(BfMaskFlag.class)) {
            lookup.put(s.getCode(), s);
            namemap.put(s.getName().toLowerCase().trim(), s);
        }
    }

    public static int getNrFlags() {
        return EnumSet.allOf(BfMaskFlag.class).size();
    }

    public boolean isBitSet(int value) {
        if ((value & (1 << code)) != 0) {
            return true;
        } else {
            return false;
        }
    }
    private int code;
    private String name;
    private String description;

    private BfMaskFlag(int code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }

    public String toString() {
        return getName();
    }

    public static BfMaskFlag get(int code) {
        return lookup.get(code);
    }
      public static BfMaskFlag get(String name) {
        return namemap.get(name.toLowerCase().trim());
    }

    public String getImageName() {        
        return name() + ".bmp";
    
    }
}
