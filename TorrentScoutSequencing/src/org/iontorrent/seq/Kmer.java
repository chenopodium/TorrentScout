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
public class Kmer extends DNASequence {

    private int start;
    private long kmer_code;
    private double score;
    private int repetitive = -1;
    // mutation, insertion or deletion
    private boolean correct;
    private int frequency;
    public static final int ALPHABET_SIZE = 6;

    public Kmer(Kmer kmer) {
        super(kmer.toSequenceString());
        this.start = kmer.getStart();


    }

    public Kmer(String kmer, int start) {
        super(kmer);
        this.start = start;
    }

    public Kmer(byte[] kmer, int start) {
        super(kmer);
        this.start = start;
    }

    public Kmer(StringBuffer kmer, int start) {
        super(kmer);
        this.start = start;
    }

    @Override
    public int hashCode() {
        return (int) computeCode();
    }

    public long computeCode() {
        if (kmer_code > 0) {
            return kmer_code;
        }
        int kmer_len = getLength();
        int power = kmer_len - 1;

        kmer_code = 0;

        for (int i = 0; i < kmer_len; i++) {
            int value = getBaseCode(i);
            long currValue = value * ((long) (Math.pow(ALPHABET_SIZE, power)));
            kmer_code += currValue;
            power--;

        }
        //p("Code for kmer:"+toString()+":" +kmer_code);
        //p("alphabet size: "+ALPHABET_SIZE+", maximum: "+Math.pow(ALPHABET_SIZE, kmer_len));
        return kmer_code;
    }

    public static void main(String[] args) {
    }

    @Override
    public boolean equals(Object obj) {
        Kmer k = (Kmer) obj;
        return this.toSequenceString().equals(k.toSequenceString());
    }

    @Override
    public String toString() {
        String ok = "-";
        if (isCorrect()) {
            ok = "+";
        }
        String res = ok + super.toString() + " @ " + start + " (size " + this.getLength() + "), freq: " + this.getFrequency();
        if (this.getScore() > 0) {
            res += " score=" + (int) this.getScore();
        }
        return res;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public long getKmer_code() {
        return kmer_code;
    }

    public void setKmer_code(long kmer_code) {
        this.kmer_code = kmer_code;
    }

    public String getKmer() {
        return super.toString();
    }

    public boolean hasKnownBases() {
        for (int i = 0; i < getLength(); i++) {
            int b = this.getBasecharPositionCode(i);
            if (b == G || b == A || b == T || b == C) {
                return true;
            }
        }
        return false;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public void setCorrect(boolean ok) {
        this.correct = ok;

    }

    public boolean isCorrect() {
        return correct;
    }


    public void setCode(long code) {
        this.kmer_code = code;

    }

    public void setFrequency(int freq) {
        this.frequency = freq;

    }

    public int getFrequency() {
        return frequency;
    }

    public int getRepetitiveBases() {
        if (repetitive > -1) {
            return repetitive;
        }
        repetitive = 0;
        String seq = this.toSequenceString();
        int len = seq.length();
        for (int i = 0; i + 1 < len; i++) {
            if (seq.charAt(i) == seq.charAt(i + 1)) {
                repetitive++;
            }
        }
        p(seq + " has " + repetitive + " repetitive bases");
        return repetitive;
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(Kmer.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(Kmer.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(Kmer.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
//  System.out.println("Kmer: " + msg);
        //Logger.getLogger( Kmer.class.getName()).log(Level.INFO, msg);
    }
}
