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
 
 * @author Chantal Roth
 */
public enum ScoreMaskFlag {

    // sff flags    
    SNR(0, "SNR", "Signal to noise ratio", 10000),
    PPF(1, "PPF", "ppf", 10000),
    SSQ(2, "SSQ", "ssq", 10000),
    
    // wellstats flags    
//    CAFIE(3, "CAFIE", "regional CAFIE", 10000),
//    IE(4, "IE", "ie", 10000),
//    DR(5, "DR", "dr", 100),

    // default.samp.parsed flags  
    Q7LEN(3, "Q7 len" ),
    QLEN(4, "qlen"),        
    MATCH(5, "matches", "Nr matches"),    
    TLEN(6, "tlen"),
    IDENTITY(7, "identity"),
    Q10LEN(8, "Q10 len" ),
    Q17LEN(9, "Q17 len"),
    Q20LEN(10, "Q20 len"),
    Q47LEN(11, "Q47 len"),
    INDEL(12, "indels", "Nr of indels"),
    
    CUSTOM1(13, "Custom 1" ),
    CUSTOM2(14, "Custom 2" ),
    CUSTOM3(15, "Custom 3" );
    
   // INDEL(12, "indels", "Nr of indels"),
    
    public static final int SAM_START = 3;
 //   public static final int WELL_START = 3;
  //  public static final int CUSTOM_START = 16;
    
    private static final Map<Integer, ScoreMaskFlag> lookup = new HashMap<Integer, ScoreMaskFlag>();

    public static  ScoreMaskFlag SFF_FLAGS[] = {  SNR, PPF, SSQ};
  // public static  ScoreMaskFlag WELLS_FLAGS[] = { CAFIE, IE, DR};
    public static  ScoreMaskFlag SAM_FLAGS[] = { TLEN, IDENTITY, QLEN, INDEL, Q7LEN, Q10LEN, Q17LEN,Q20LEN, Q47LEN, MATCH};
    public static  ScoreMaskFlag CUSTOM_FLAGS[] = { CUSTOM1, CUSTOM2, CUSTOM3};
    
    static {
        for (ScoreMaskFlag s : EnumSet.allOf(ScoreMaskFlag.class)) {
            lookup.put(s.getCode(), s);
        }
    }
   
    private int code;
    private String name;
    private String description;
    private int mult;
    
    private ScoreMaskFlag(int code, String name) {
        this(code, name, name, 128);
    }
    private ScoreMaskFlag(int code, String name, String description) {
        this(code, name, description, 1);
    }
     private ScoreMaskFlag(int code, String name, String description, int mult) {
        this.code = code;
        this.mult = mult;
        this.name = name;
        this.description = description;
    }
 /* If double, multiply x 1000, for instance, to get integer values . If integer, return the value as it is */
    public int multiplier() {
        return mult;
    }
    public int getIntValue(double value) {
        return (int) (mult*value);
    }
     public double getRealValue(int value) {
        return (double)value/(double)mult;
    }
    public static int getNrFlags() {
        return EnumSet.allOf(ScoreMaskFlag.class).size();
    }

    public boolean isBitSet(int value) {
        if ((value & (1 << code)) != 0) {
            return true;
        } else {
            return false;
        }
        
    }
    public String getName() {
        return name;
    }

    public String getImageName() {        
        return name() + ".bmp";
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

    public static ScoreMaskFlag get(int code) {
        return lookup.get(code);
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
 
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCustom() {
      return this == CUSTOM1 ||  this == CUSTOM2 ||  this == CUSTOM3;
    }

    public boolean isIn(ScoreMaskFlag[] flags) {
       for (int i = 0; i < flags.length; i++ ) {
           if (this.code == flags[i].code) return true;
       }
       return false;
    }

}
