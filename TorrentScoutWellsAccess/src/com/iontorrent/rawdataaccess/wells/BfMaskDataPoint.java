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

import com.iontorrent.utils.io.FileUtils;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
     The data section consists of a single array of nRow * nCol uint16 values,
     * one per well stored in column-major order. 
     * In other words, the offset for the value for row i and column j 
     * (where i and j are 0-based) is row*nCol+col. 
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
public class BfMaskDataPoint  implements Serializable{

         
        /** Each bit in the integer is used as a boolean flag to indicate the properties outlined in the table below. */
        int mask;

    BfMaskDataPoint(int mask) {
        this.mask = mask;
    }

        /**
      empty	1<<0	Indicates if the well does not contain a bead
         */
        public boolean isEmpty() {
            return BfMaskFlag.EMPTY.isBitSet(mask);
        }

        /** bead	1<<1	Indicates if the well contains a bead */
        public boolean isBead() {
            return BfMaskFlag.BEAD.isBitSet(mask);
        }

        /** live	1<<2	Indicates if the well contains a bead with live template*/
        public boolean isLive() {
            return BfMaskFlag.LIVE.isBitSet(mask);
        }

        /** dud	1<<3	Indicates if the well contains a bead with no live template */
        public boolean isDud() {
            return BfMaskFlag.DUD.isBitSet(mask);
        }

        /**ambiguous	1<<4	Indicates if the well contains a bead with live template that may be either library or test fragment */
        public boolean isAmbiguous() {
            return BfMaskFlag.AMBIGUOUS.isBitSet(mask);
        }

        /** testFrag	1<<5	Indicates if the well contains a bead with live test fragment template */
        public boolean isTestFrag() {
            return BfMaskFlag.TESTFRAG.isBitSet(mask);
        }

        /** library	1<<6	Indicates if the well contains a bead with live library template */
        public boolean isLibrary() {
            return BfMaskFlag.LIBRARY.isBitSet(mask);
        }

        /** pinned	1<<7	Indicates if the well went out of the system dynamic range at some point during the run */
        public boolean isPinned() {
            return BfMaskFlag.PINNED.isBitSet(mask);
        }

        /** ignore	1<<8	Indicates if the well should be ignored in various calculations such as background signal derivation from empty wells*/
        public boolean isIgnore() {
            return BfMaskFlag.IGNORE.isBitSet(mask);
        }

        /** washout	1<<9	Indicates if the well contained a bead that washed out at some point during the run*/
        public boolean isWashout() {
           return BfMaskFlag.WASHOUT.isBitSet(mask);
        }

        /** exclude	1<<10	Indicates if the well should be excluded from all analysis as it is not fluidically addressable */
        public boolean isExclude() {
            return BfMaskFlag.EXCLUDE.isBitSet(mask);
        }

        /** ekeypass	1<<11	Indicates if the well generated a valid nucleotide sequence in the key flows */
        public boolean isKeypass() {
            return BfMaskFlag.KEYPASS.isBitSet(mask);
        }

        /** filteredBadKey	1<<12	Indicates if the well was filtered out due to generating an invalid nucleotide sequence in the key */
        public boolean isFilteredBadKey() {
            return BfMaskFlag.FBADKEY.isBitSet(mask);
        }

        /** filteredShort	1<<13	Indicates if the well was filtered out due to generating too short a nucleotide sequence */
        public boolean isFilteredShort() {
            return BfMaskFlag.FSHORT.isBitSet(mask);
        }

        /**filteredBadPPF	1<<14	Indicates if the well was filtered out due to too many incorporating flows */
        public boolean isFilteredBadPPF() {
            return BfMaskFlag.FBADPPF.isBitSet(mask);
        }

        /** filteredBadResidual	1<<15	Indicates if the well was filtered out due to the signal values fitting poorly */
        public boolean isFilteredBadResidual() {
            return BfMaskFlag.FBADRESIDUAL.isBitSet(mask);
        }
        
        protected void read(DataInputStream in) {
            try {
                mask = FileUtils.getUInt16Little(in);

            } catch (IOException ex) {
                err("Could not read mask", ex);
            }
        }
        public static int readNext(DataInputStream in) {
            int mask = Integer.MIN_VALUE;
            try {
               mask = FileUtils.getUInt16Little(in);

            } catch (IOException ex) {
                err("Could not read mask", ex);
            }
            return mask;
        }
        public boolean hasFlag(BfMaskFlag flag) {
            return flag.isBitSet(mask);
        }

        public String toString() {
            String s = "Mask: ";
            if (!isBead()) s += "empty, ";
            if (!isLive()) s += "dead, ";
            if (isDud()) s += "dud, ";
            if (isTestFrag()) s += "test frag, ";
            if (isLibrary()) s += "library frag, ";
            if (isAmbiguous()) s += "ambig, ";
            if (isWashout()) s += "washout, ";
            if (isIgnore()) s += "ignore, ";
            if (isFilteredShort()) s += "filtered bad short, ";
            if (isFilteredBadPPF()) s += "filtered bad ppf, ";
            if (isFilteredBadResidual()) s += "filtered bad res, ";
            if (isExclude()) s += "exclude, ";
            if (s.endsWith(", ")) s = s.substring(0, s.length()-2);
            return s;
        }
         private static void err(String msg, Exception ex) {
        Logger.getLogger(BfMaskDataPoint.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void p(String msg) {
        System.out.println("BfMaskDataPoint: " + msg);

    }

    public boolean hasFlags(BfMaskFlag[] emptyflags) {
        for (BfMaskFlag flag: emptyflags) {
            if (!hasFlag(flag)) return false;
        }
        return true;
    }
    public boolean hasFlags(BfMaskFlag[] haveflags,BfMaskFlag[] nothaveflags) {
        if (haveflags != null) {
            for (BfMaskFlag flag: haveflags) {
                if (!hasFlag(flag)) return false;
            }
        }
        if (nothaveflags != null) {
            for (BfMaskFlag flag: nothaveflags) {
                if (hasFlag(flag)) return false;
            }
        }
        return true;
    }

     public static boolean hasFlags(BfMaskFlag[] haveflags,BfMaskFlag[] nothaveflags, int mask) {
        if (haveflags != null) {
            for (BfMaskFlag flag: haveflags) {
                if (!flag.isBitSet(mask)) return false;
            }
        }
        if (nothaveflags != null) {
            for (BfMaskFlag flag: nothaveflags) {
                if (flag.isBitSet(mask)) return false;
            }
        }
        return true;
    }
}
