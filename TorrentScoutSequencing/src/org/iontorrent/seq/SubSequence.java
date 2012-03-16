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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class SubSequence implements SequenceIF {

    private SequenceIF sequence;
    private int start_pos;
    private int end_pos;

    public SubSequence(SequenceIF seq, int start, int end) {
        this.sequence = seq;
        this.start_pos = Math.max(0, start);
        this.end_pos = Math.min(end, seq.getLength() - 1);
        start_pos = Math.min(start_pos, end_pos);
        end_pos = Math.max(start_pos, end_pos);
        // doesn't work if seq is also a subsequence... the length-start would be the correct check!
        //this.end_pos =Math.min(end, sequence.getLength());
        check();
    }

    public void check() {
        if (end_pos < start_pos) {
            err("End position < start Pos: " + end_pos + ", " + start_pos);
        }

    }

    public String getName() {
        return sequence.getName();
    }

    public boolean equals(Object obj) {
        SequenceIF s = (SequenceIF) obj;
        return s.toSequenceString().equals(this.toSequenceString());
    }

    @Override
    public char getBaseChar(int pos) {
        return sequence.getBaseChar(pos + start_pos);
    }

    @Override
    public void insertCharAt(int pos, char c) {
        sequence.insertCharAt(pos + start_pos, c);
    }

    @Override
    public void removeBaseAt(int pos) {
        sequence.removeBaseAt(pos + start_pos);
    }

    @Override
    public byte getBasecharPositionCode(int pos) {
        return sequence.getBasecharPositionCode(pos + start_pos);
    }

    @Override
    public int getLength() {
        return end_pos - start_pos;
    }
 
    @Override
    public String toString() {
        String res = start_pos + "-" + end_pos + ":" + toSequenceString();
        return res;
    }

    public String toTestString() {
        String res = "";
        int s = start_pos;
        if (sequence instanceof SubSequence) {
            SubSequence sub = ((SubSequence) (sequence));
            res += sub.toTestString();
            s += sub.getStartPos();
        } else {
            res += "\nSeq:" + sequence.toString();
        }


        res += "\nSub " + sp(s) + toSequenceString();
        return res;
    }

    public int getStartPos() {
        return start_pos;
    }

    public int getEndPos() {
        return end_pos;
    }

    public boolean isGap(int pos) {
        return sequence.isGap(pos + start_pos);
    }

    private String sp(int s) {
        StringBuffer res = new StringBuffer(getLength());
        for (int i = 0; i < s; i++) {
            res = res.append(' ');
        }
        return res.toString();

    }

    @Override
    public String toSequenceString() {
        StringBuffer res = new StringBuffer(getLength());
        for (int i = 0; i < getLength(); i++) {
            res = res.append(getBaseChar(i));
        }
        return res.toString();
    }

    public static void main(String[] args) {
        DNASequence seq = new DNASequence("GGTTAACAAAAAATTTTTTTTGGGGGGGGGGGG");
        SubSequence sub = new SubSequence(seq, 0, 5);
        p("\n\nsub 0-5:" + sub.toTestString());
        sub = new SubSequence(seq, 5, 10);
        p("\n\nsub 5-10:" + sub.toTestString());
        SubSequence sub1 = new SubSequence(sub, 1, 3);
        p("\n\nsub of sub 1-3:" + sub1.toTestString());
    }
//	public int find(SequenceIF seq) {
//		return find(seq, 0, this.getLength());
//	}

    public ArrayList<Integer> findAll(SequenceIF seq) {
        return findAll(seq, 0, this.getLength());
    }

    @Override
    public int find(SequenceIF seq, int start, int end) {
        return sequence.find(seq, start + start_pos, end + start_pos);
    }

    @Override
    public ArrayList<Integer> findAll(SequenceIF seq, int start, int end) {
        return sequence.findAll(seq, start + start_pos, end + start_pos);
    }

    @Override
    public ArrayList<Integer> findAll(SequenceIF seq, int start, int end, boolean debug) {
        return sequence.findAll(seq, start + start_pos, end + start_pos, debug);
    }
//	public void setBaseCharAt(int pos, char c) {
//		sequence.setBaseCharAt(pos+start_pos, c);
//		
//	}
    @Override
    public boolean isBase(int pos) {

        return sequence.isBase(pos + start_pos);
    }

    public SequenceIF createNew() {
        // TODO Auto-generated method stub
        return new SubSequence(sequence, start_pos, end_pos);
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(SubSequence.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {

        Logger.getLogger(SubSequence.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(SubSequence.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
//  System.out.println("SubSequence: " + msg);
        //Logger.getLogger( SubSequence.class.getName()).log(Level.INFO, msg);
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
     @Override
    public DNASequence reverse() {
          int len = getLength();
          byte[] rev = new byte[len];
          for (int i = 0; i < len; i++) {            
            rev[len-i] = sequence.getBasecharPositionCode(i+start_pos);
          }
          return new DNASequence(rev);
     }

     @Override
    public DNASequence complement() {

        int len = getLength();
        byte[] comp = new byte[len];
        for (int i = 0; i < getLength(); i++) {
            int curr = sequence.getBasecharPositionCode(i+start_pos);
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

}
