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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public abstract class AbstractByteSequence implements SequenceIF {

    private byte[] sequence;
    private String name;

    public AbstractByteSequence() {
    }

    public AbstractByteSequence(int length) {
        sequence = new byte[length];
    }

    public void setName(String n) {
        this.name = n;
    }

    public String getName() {
        return name;
    }

    public byte getBaseCode(int i) {
        if (i >= sequence.length) {
            err("Sequence length:" + sequence.length + ", attempting to access base " + i);
            return 0;
        }
        return sequence[i];
    }

    public AbstractByteSequence(byte[] sequence) {
        this.sequence = sequence;

    }

    public AbstractByteSequence(String sequence) {
        setSequence(sequence);
    }

    public AbstractByteSequence(StringBuffer sequence) {
        setSequence(sequence);
    }

    public AbstractByteSequence(String seq, int[] quality) {
        super();
        setSequence(seq);

    }

    @Override
    public int getLength() {
        if (sequence == null) {
            return 0;
        } else {
            return sequence.length;
        }
    }

    public void setBaseCode(int pos, byte b) {
        sequence[pos] = b;
    }

    public void insertCharAt(int pos, char c) {
        byte[] tmp = sequence;
        //p("Inserting "+c+" @ "+pos);
        sequence = new byte[getLength() + 1];
        for (int i = 0; i < getLength(); i++) {
            if (i < pos) {
                sequence[i] = tmp[i];
            } else if (i == pos) {
                sequence[i] = toBasecharPositionCode(c);
            } else {
                sequence[i] = tmp[i - 1];
            }
        }
        //p("    seq len is now:"+getLength()+"/"+this.getLength());
        if (sequence.length != this.getLength()) {
            err("sequence.length " + sequence.length + " <> getLength() " + this.getLength());
        }
//		if (this.getBaseChar(pos) != c) {
//			err("Insert error, should be "+c+", but is "+this.getBaseChar(pos));
//		}
        //	p("Sequence is now: "+toSequenceString());
    }

    public void setBaseCharAt(int pos, char c) {
        if (pos < 0 || pos >= getLength()) {
            warn("Trying to write char out of range:" + pos + ", seq length:" + getLength());
        } else {
            setBaseCode(pos, toBasecharPositionCode(c));
        }
    }

    public void setBasesAt(int pos, String str) {
        for (int i = pos; i < pos + str.length(); i++) {
            setBaseCharAt(i, str.charAt(i));
        }
    }

    public void removeBaseAt(int pos) {
        byte[] tmp = sequence;
        //	p("Removing char @ "+pos);
        sequence = new byte[getLength() - 1];
        for (int i = 0; i < tmp.length; i++) {
            if (i < pos) {
                sequence[i] = tmp[i];
            } else if (i > pos) {
                sequence[i - 1] = tmp[i];
            }
        }
        //	p("Sequence is now: "+toSequenceString());
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

    public String toCsv() {
        StringBuffer buf = new StringBuffer(getLength());
        for (int i = 0; i < getLength(); i++) {
            buf = buf.append(byteToChar(getBaseCode(i)));
            if (i + 1 < getLength()) {
                buf = buf.append(", ");
            }
        }
        return buf.toString();
    }

    /** check consistency of sequence and quality */
    protected void check() {
        if (sequence == null) {
            err("No sequence specified");
        } else {
            for (int i = 0; i < getLength(); i++) {
                this.getBasecharPositionCode(i);
            }
        }
    }

    public byte getBasecharPositionCode(int pos) {

        return getBaseCode(pos);
    }

    public byte[] getSequence() {
        return sequence;
    }

    public void assignSequence(AbstractByteSequence seq) {
        this.sequence = seq.sequence;
    }

    public abstract AbstractByteSequence subString(int from, int to);

    /** convert string to byte array */
    private void setSequence(String seq) {
        sequence = new byte[seq.length()];
        for (int i = 0; i < getLength(); i++) {
            char c = seq.charAt(i);
            byte base = toBasecharPositionCode(c);
            sequence[i] = base;
        }
    }

    /** convert string to byte array */
    private void setSequence(StringBuffer seq) {
        if (seq == null) {
            warn("Sequence StringBuffer is null");
            return;
        }
        sequence = new byte[seq.length()];
        for (int i = 0; i < getLength(); i++) {
            char c = seq.charAt(i);
            byte base = toBasecharPositionCode(c);
            sequence[i] = base;
        }
    }

    public abstract byte toBasecharPositionCode(char c);

    public abstract char byteToChar(int b);

    protected byte[] getByteComplement() {
        int len = getLength();
        byte[] comp = new byte[len];
        for (int i = 0; i < len; i++) {
            int curr = getBaseCode(i);
            byte com = getByteComplement(curr);
            comp[i] = com;
        }
        return comp;
    }

    public static byte getByteComplement(int curr) {
        byte com = 0;
        if (curr == A) {
            com = T;
        } else if (curr == C) {
            com = (G);
        } else if (curr == G) {
            com = (C);
        } else if (curr == T) {
            com = (A);
        } else if (curr == X) {
            com = (X);
        } else if (curr == GAP) {
            com = (GAP);
        } else if (curr == POSSIBLEGAP) {
            com = (POSSIBLEGAP);
        } else if (curr == SPACE) {
            com = (SPACE);

        } else {
            p("Error in sequence, unknown character " + curr);
        }
        return com;
    }

    /** Create a new sequence that is the current object expanded by the argument seq */
    public byte[] concat(AbstractByteSequence seq) {

        byte[] res = new byte[this.getLength() + seq.getLength()];
        int s = getLength();
        for (int i = 0; i < s; i++) {
            res[i] = sequence[i];
        }

        for (int i = 0; i < seq.getLength(); i++) {
            res[i + s] = seq.getBaseCode(i);
        }
        return res;
    }

    public static byte getCharToByte(char c) {
        byte base = 0;
        if (c == 'g' || c == 'G') {
            base = G;
        } else if (c == 'a' || c == 'A') {
            base = A;
        } else if (c == 't' || c == 'T') {
            base = T;
        } else if (c == 'c' || c == 'C') {
            base = C;
        } else if (c == '_' || c == '=' || c == GAPCHAR) {
            base = GAP;
        } else if (c == ' ' || c == SPACECHAR) {
            base = SPACE;
        } else if (c == '?' || c == POSSGAPCHAR) {
            base = POSSIBLEGAP;
        } else if (c == 'x' || c == 'X' || c == 'N' || c == 'n') {
            base = X;
        } else if (c == ' ') {
            base = SPACE;
        } else {
            base = X;
            //err("Unknown base "+c+" in sequence string");			
        }
        return base;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(AbstractByteSequence.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(AbstractByteSequence.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(AbstractByteSequence.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
     //   System.out.println("AbstractByteSequence: " + msg);
        //Logger.getLogger( AbstractByteSequence.class.getName()).log(Level.INFO, msg, ex);
    }
}
