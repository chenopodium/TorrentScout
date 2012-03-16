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
package org.iontorrent.seq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class DNASequence extends AbstractByteSequence implements SequenceIF {

    public DNASequence() {
        super();
    }

    public DNASequence(String sequence, String name) {
        super(sequence);
        this.setName(name);
    }

    public DNASequence(String sequence) {
        super(sequence);
    }

    public DNASequence(StringBuffer sequence) {
        super(sequence);

    }

    public DNASequence(byte[] sequence) {
        super(sequence);

    }

    public DNASequence(int length) {
        super(length);
    }
 @Override
    public ArrayList<Integer> findAll(SequenceIF seq, int start, int end) {
        return findAll(seq, start, end, false);
 }

    /** find ALL exact match of seq and return pos */
    @Override
    public ArrayList<Integer> findAll(SequenceIF seq, int start, int end, boolean debug) {
        int s = start;
        if (debug) p("findAll.Finding "+seq+"from "+s+"-"+end);
        ArrayList<Integer> res = new ArrayList<Integer>();
        boolean found = true;
        while (found) {
            int pos = -1;
            // this is to optimize the speed
            if (debug) p("Finding "+seq+"from "+s+"-"+end);
            pos = find(seq, s, end, debug);
            if (pos > -1) {
                res.add(new Integer(pos));
                if(debug) p("Found something: "+pos);
                s = pos + seq.getLength();
            } else {
                if(debug)p("Found nothing after "+s);
                found = false;
            }
        }
        return res;
    }

    /** find FIRST exact match of seq and return pos */
    @Override
    public int find(SequenceIF seq, int start, int end) {
        return find(seq, start, end, false);
    }

    public int find(SequenceIF seq, int start, int end, boolean debug) {
        int seqlen = seq.getLength();
        int sequencelen = this.getLength();
        if (end <= start) {
            end = sequencelen;
        }
        if (debug) {
            p("Finding " + seq.toSequenceString() + " between " + start + "-" + end);
        }

        for (int i = start; i <= end - seqlen && i <= sequencelen - seqlen; i++) {
            int j = 0;
            if (debug) {
                p("i:" + i);
            }
            while (j < seqlen && i + j < sequencelen
                    && (seq.getBasecharPositionCode(j) == getBaseCode(i + j))) {
                if (debug) {
            //  System.out.println(seq.getBaseChar(j) + "(" + j + ")=" + getBaseChar(i + j) + "(" + (i + j) + ")");
                }
                j++;
            }
            if (debug) {
        //  System.out.println("\nj=" + j + ">= seqlen=" + seqlen + " or sequencelen=" + sequencelen + ">=i+j=" + (i + j));
            }
            if (j >= seqlen) {
                if (debug) {
                    SubSequence test = new SubSequence(this, i, i + seq.getLength());
                    p("   Found " + seq.toSequenceString() + "@" + i + ":" + test.toSequenceString());
                }
                return i;
            }
        }
        if (debug) {
            p("Could not find " + seq + ",  between " + start + "-" + end + ":" + toSequenceString());
        }
        return -1;
    }

    @Override
    public char getBaseChar(int pos) {
        if (pos >= getLength()) {
            int qlen = 0;

            warn("Pos > seq length:" + pos + ">" + getLength());
            return 'x';
        } else if (pos < 0) {
            err("getBaseChar: Pos < 0");
        }
        return byteToChar(getBaseCode(pos));
    }

    @Override
    public DNASequence subString(int from, int to) {
        byte[] sub = Arrays.copyOfRange(getSequence(), from, to);
        DNASequence subseq = new DNASequence(sub);
        return subseq;
    }

    @Override
    public byte getBasecharPositionCode(int pos) {
        if (pos > getLength()) {
            err("pos " + pos + " outside seq len " + getLength());
        }
        byte b = getBaseCode(pos);
        if (b < 0 || b > Byte.MAX_VALUE) {
            err("getBasecharPositionCode(" + pos + "), value " + b + " > Byte.MAX_VALUE " + Byte.MAX_VALUE);
        }
        return b;
    }

    @Override
    public byte toBasecharPositionCode(char c) {
        return getCharToByte(c);
    }

    @Override
    public char byteToChar(int b) {
        return SequenceIF.BASECHARS[b];
    }

    @Override
    public String toString() {
        return toSequenceString();
    }

    @Override
    public String toSequenceString() {
        if (getLength() < 1) {
            return "";
        }
        StringBuffer buf = new StringBuffer(getLength());
        for (int i = 0; i < getLength(); i++) {
            buf = buf.append(byteToChar(getBaseCode(i)));
        }
        return buf.toString();
    }

    @Override
    public String toCsv() {
        StringBuffer buf = new StringBuffer(getLength());
        for (int i = 0; i < getLength(); i++) {
            buf = buf.append(byteToChar(getBaseCode(i)));
        }
        return buf.toString();
    }

    /** Create a new sequence that is the current object expanded by the argument seq */
    public SequenceIF concatenate(SequenceIF seq) {

        DNASequence con = new DNASequence(this.toSequenceString() + seq.toSequenceString());

        return con;
    }

    @Override
    public boolean isBase(int pos) {
        byte b = (byte) this.getBasecharPositionCode(pos);
        return (b < 5);
    }

    /** given the base nrin an alignment, find the corresponding position inthe sequence*/
    public int getSequencePosFromAlignment(int alignpos) {
        int spos = 0;
        for (int i = 0; i < getLength(); i++) {
            if (!this.isGap(i)) {
                spos++;
            }
            if (spos > alignpos) {
                return spos - 1;
            }
        }
        return -1;
    }

    @Override
    public boolean isGap(int i) {
        return getBaseCode(i) == GAP;
    }

    public static char getChar(byte b) {
        return BASECHARS[b];
    }

    @Override
    public DNASequence complement() {

        int len = getLength();
        byte[] comp = new byte[len];
        for (int i = 0; i < len; i++) {
            int curr = getBaseCode(i);
            if (curr == A) {
                comp[i] = T;
            } else if (curr == C) {
                comp[i] = (G);
            } else if (curr == G) {
                comp[i] = (C);
            } else if (curr == T) {
                comp[i] = (A);
            } else if (curr == X) {
                comp[i] = (X);
            } else if (curr == GAP) {
                comp[i] = (GAP);
            } else if (curr == POSSIBLEGAP) {
                comp[i] = (POSSIBLEGAP);
            } else if (curr == SPACE) {
                comp[i] = (SPACE);

            } else {
                p("Error in sequence, unknown character " + curr);
            }
        }
        return new DNASequence(comp);
    }

    public static byte complement(byte curr) {
        byte comp = 0;
        if (curr == A) {
            comp = T;
        } else if (curr == C) {
            comp = (G);
        } else if (curr == G) {
            comp = (C);
        } else if (curr == T) {
            comp = (A);
        } else if (curr == X) {
            comp = (X);
        } else if (curr == GAP) {
            comp = (GAP);
        } else if (curr == POSSIBLEGAP) {
            comp = (POSSIBLEGAP);
        } else if (curr == SPACE) {
            comp = (SPACE);

        } else {
            p("Error in sequence, unknown character " + curr);
        }
        return comp;
    }

    public static DNASequence random(String chars, int len, String name) {
        String pat = "";
        for (int i = 0; i < len; i++) {
            pat += chars.charAt((int) (Math.random() * chars.length()));
        }
        return new DNASequence(pat, name);
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(DNASequence.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(DNASequence.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(DNASequence.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
//  System.out.println("DNASequence: " + msg);
        //Logger.getLogger( DNASequence.class.getName()).log(Level.INFO, msg, ex);
    }

    public DNASequence reverse() {

        int len = getLength();
        byte[] comp = new byte[len];
        for (int i = 0; i < len; i++) {
            byte curr = getBaseCode(i);
            comp[len-i-1] = curr;
        }
        return new DNASequence(comp);

    }
}
