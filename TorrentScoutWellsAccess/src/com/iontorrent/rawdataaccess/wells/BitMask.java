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
package com.iontorrent.rawdataaccess.wells;

import com.iontorrent.utils.io.FileUtils;
import com.iontorrent.wellmodel.WellCoordinate;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class BitMask extends BfMask implements Serializable {
    
    private WellCoordinate relcoord;
    
    public BitMask() {
        super();
    }
    
    public BitMask(WellCoordinate coord, int c, int r) {
        super(c, r);
        this.relcoord = coord;
    }
    
    public BitMask(BitMask m) {
        super(m.getNrCols(), m.getNrRows());
        this.relcoord = m.relcoord;
    }
    public boolean get(WellCoordinate coord) {
        return get(coord.getCol(), coord.getRow()); 
    }
    public void set(WellCoordinate coord, boolean flag) {
         set(coord.getCol(), coord.getRow(), flag);
    }
    public void set(int c, int r, boolean flag) {
        super.setMaskAt(c, r, flag ? 1 : 0);
    }
    
    public void set(int c, int r, byte flag) {
        super.setMaskAt(c, r, flag > 0 ? 1 : 0);
    }
    
    public boolean get(int c, int r) {
        return super.getMaskAt(c, r) != 0 ? true : false;
    }

    /** coords are RELATIVE coords to BLOCK. So must subtract area coord */
    public ArrayList<WellCoordinate> getAllCoordsWithData(int max, int c1, int r1, int c2, int r2) {
        
        c1 = Math.max(0, c1 - this.getRelCoord().getCol());
        r1 = Math.max(0, r1 - this.getRelCoord().getRow());
        c2 = Math.min(c2- this.getRelCoord().getCol(), this.getNrCols());
        r2 = Math.min(r2- this.getRelCoord().getRow(), this.getNrRows());
        ArrayList<WellCoordinate> coords = new ArrayList<WellCoordinate>();
        
        for (int c = c1; c < c2; c++) {
            for (int r = r1; r < r2; r++) {
                if (this.get(c, r)) {
                    WellCoordinate  coord = new WellCoordinate(c+ this.getRelCoord().getCol(), r+ this.getRelCoord().getRow());
                     coords.add(coord);
                    if (coords.size() > max) {
                       // p("Found more coords than " + max + ", returning what I have now");
                        return coords;
                    }
                }           
            }
        }
        
        
        return coords;
    }
    
    public void write(String file) {
        DataOutputStream out = FileUtils.openFileToWrite(new File(file), getNrCols() * getNrRows());
        try {
            out.writeInt(relcoord.getX());
            out.writeInt(relcoord.getY());
            out.writeInt(getNrRows());
            out.writeInt(getNrCols());
            for (int r = 0; r < getNrRows(); r++) {
                for (int c = 0; c < getNrCols(); c++) {
                    out.writeByte(getMaskAt(c, r) > 0 ? 1 : 0);
                }
            }
            String name = getName();
            if (name == null) {
                name = "";
            }
            out.writeInt(name.length());
            out.writeChars(name);
            out.flush();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(BitMask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void writeAsString(String file) {
        DataOutputStream out = FileUtils.openFileToWrite(new File(file), getNrCols() * getNrRows());
        try {
            out.writeChars(relcoord.toString());
            out.writeChars("" + getNrRows());
            out.writeChars(",");
            out.writeChars("" + getNrCols());
            out.writeChars("\n");
            for (int r = 0; r < getNrRows(); r++) {
                for (int c = 0; c < getNrCols(); c++) {
                    out.writeChars("" + (getMaskAt(c, r) > 0 ? 1 : 0));
                }
                out.writeChars("\n");
            }
            out.writeChars("\n");
            out.writeChars(getName());
            out.flush();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(BitMask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static BitMask read(String file) {
        if (!FileUtils.exists(file)) {
            err("File " + file + " does not exist");
            return null;
        }
        DataInputStream in = FileUtils.openFileToRead(new File(file), 10000);
        BitMask mask = null;
        try {
            int x = in.readInt();
            int y = in.readInt();
            int rows = in.readInt();
            int cols = in.readInt();
            WellCoordinate relcoord = new WellCoordinate(x, y);
            mask = new BitMask(relcoord, cols, rows);
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    byte b = in.readByte();
                    mask.set(c, r, b);
                }
            }
        } catch (IOException ex) {
            err("Could not read mask: ", ex);
        }
        
        String name = "";
        try {
            if (mask == null) {
                err("Mask is null in readmask");
                return null;
            }
            // name
            int chars = in.readInt();
            for (int i = 0; i < chars; i++) {
                name += in.readChar();
            }
            mask.setName(name);
            in.close();
            
        } catch (IOException ex) {
            mask.setName(name);
        }
        
        
        return mask;
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(BitMask.class.getName()).log(Level.SEVERE, msg, ex);
    }
    
    private static void err(String msg) {
        Logger.getLogger(BitMask.class.getName()).log(Level.SEVERE, msg);
    }
    
    private static void warn(String msg) {
        Logger.getLogger(BitMask.class.getName()).log(Level.WARNING, msg);
    }
    
    private static void p(String msg) {
        //System.out.println("BitMask: " + msg);
        Logger.getLogger(BitMask.class.getName()).log(Level.INFO, msg);
    }
    
    public double computePercentage() {
        int tot = 0;
        long all = 0;
        for (int r = 0; r < this.getNrRows(); r++) {
            for (int c = 0; c < this.getNrCols(); c++) {
                all++;
                if (this.get(c, r)) {
                    tot++;
                }
            }
        }
        double per = (double) (tot * 100.0 / all);
        //   p("Percentage in "+getName()+":"+per);
        return per;
    }

    /**
     * @return the coord
     */
    public WellCoordinate getRelCoord() {
        return relcoord;
    }

    /**
     * @param coord the coord to set
     */
    public void setRelCoord(WellCoordinate coord) {
        this.relcoord = coord;
    }
    public void invert( WellCoordinate coord){
        this.set(coord, !this.get(coord));
    }
     public void invert( int x, int y){
        this.set(x, y, !this.get(x, y));
    }       
    public BitMask add(BitMask m1) {
        BitMask res = new BitMask(m1);
        res.add(this, m1);
        res.setName(getName() + " + " + m1.getName());
        return res;
    }
    
    public BitMask intersect(BitMask m1) {
        BitMask res = new BitMask(m1);
        res.intersect(this, m1);
        res.setName(getName() + " ^ " + m1.getName());
        return res;
    }
    
    public BitMask subtract(BitMask m1) {
        BitMask res = new BitMask(m1);
        res.subtract(this, m1);
        res.setName(getName() + " - " + m1.getName());
        return res;
    }
    
    public boolean add(BfMask m1, BfMask m2) {
        if (!compatible(m1) || !compatible(m2)) {
            return false;
        }
        p("adding " + m1 + " and " + m2 + " to " + this);
        for (int r = 0; r < m1.getNrRows(); r++) {
            for (int c = 0; c < m1.getNrCols(); c++) {
                int v1 = m1.getMaskAt(c, r);
                int v2 = m2.getMaskAt(c, r);
                this.setMaskAt(c, r, or(v1, v2));
            }
        }
        return true;
    }
    
    public boolean and(BitMask m1, BitMask m2) {
        return intersect(m1, m2);
    }
    
    public boolean intersect(BitMask m1, BitMask m2) {
        if (!compatible(m1) || !compatible(m2)) {
            return false;
        }
        for (int r = 0; r < m1.getNrRows(); r++) {
            for (int c = 0; c < m1.getNrCols(); c++) {
                int v1 = m1.getMaskAt(c, r);
                int v2 = m2.getMaskAt(c, r);
                this.setMaskAt(c, r, and(v1, v2));
            }
        }
        return true;
    }
    
    public boolean xor(BitMask m1, BitMask m2) {
        if (!compatible(m1) || !compatible(m2)) {
            return false;
        }
        for (int r = 0; r < m1.getNrRows(); r++) {
            for (int c = 0; c < m1.getNrCols(); c++) {
                int v1 = m1.getMaskAt(c, r);
                int v2 = m2.getMaskAt(c, r);
                this.setMaskAt(c, r, xor(v1, v2));
            }
        }
        return true;
    }
    /* 1 - 1 = 0; 1 - 0=1, 0 - 1 = 0, 0 - 0 =0 -> bitwise a &(!b) */
    
    public boolean subtract(BitMask m1, BitMask m2) {
        if (!compatible(m1) || !compatible(m2)) {
            return false;
        }
        p("subtracting " + m1 + " - " + m2 + " to " + this);
        for (int r = 0; r < m1.getNrRows(); r++) {
            for (int c = 0; c < m1.getNrCols(); c++) {
                int v1 = m1.getMaskAt(c, r);
                int v2 = m2.getMaskAt(c, r);
                int v3 = subtract(v1, v2);
                //  p(v1 +"- "+ v2 +"= "+v3);
                this.setMaskAt(c, r, v3);
            }
        }
        return true;
    }

    public boolean not(BitMask m1) {
        return invert(m1);
    }
    
    
    public boolean invert(BitMask m1) {
        if (!compatible(m1)) {
            return false;
        }
        for (int r = 0; r < m1.getNrRows(); r++) {
            for (int c = 0; c < m1.getNrCols(); c++) {
                int v1 = m1.getMaskAt(c, r);
                this.setMaskAt(c, r, not(v1));
            }
        }
        return true;
    }

    /** subtract individual bits */
    protected int subtract(int a, int b) {
        // 1, 1 -> 1 & 0 = 0
        // 0, 0 -> 0 & 1 = 0
        // 1, 0 -> 1 & 1 = 1
        // 0, 1 -> 0 & 0 = 0
        return is(a) && is(not(b)) ? 1 : 0;
    }

    private int not(int b) {
        return is(b) ? 0 : 1;
    }
    
    public boolean is(int a) {
        return a != 0;
    }

    /** bit xor - one or the other but not both */
    protected int xor(int a, int b) {
        if (is(a) && is(b)) {
            return 0;
        } else if (is(a) || is(b)) {
            return 1;
        } else {
            return 0;
        }
    }

    /** bit or - results in "adding"  bit values */
    protected int or(int a, int b) {
        if (is(a) || is(b)) {
            return 1;
        } else {
            return 0;
        }
    }

    /** bit and - results in "intersecting"  bit values */
    protected int and(int a, int b) {
        if (is(a) && is(b)) {
            return 1;
        } else {
            return 0;
        }
    }
}
