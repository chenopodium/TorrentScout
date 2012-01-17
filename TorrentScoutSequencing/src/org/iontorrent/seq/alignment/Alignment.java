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
package org.iontorrent.seq.alignment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.iontorrent.seq.DNASequence;
import org.iontorrent.seq.Read;
import org.iontorrent.seq.SeqFlowUtil;
import org.iontorrent.seq.SequenceIF;

/**
 *
 * @author Chantal Roth
 */
public class Alignment {

    private static final int WINDOW_SIZE = 60;
    protected SequenceIF refseq1;
    protected SequenceIF seq2;
    private SequenceIF refalign1;
    private SequenceIF align2;
    protected int refstart1;
    protected int start2;
    private char[] markupLine;
    private int[][] pathscore;
    private String flowstring;
    private int genome_startpos;
    private int genome_endpos;
    private boolean seqReverse;
    /**
     * Count of identical locations
     */
    private int identity;
    /**
     * Count of similar locations
     */
    private int similarity;
    /**
     * Count of gap locations
     */
    private int[] gaps;
    private int[] endgaps;
    /**
     * Scoring matrix
     */
    // private Matrix matrix;
    /**
     * Alignment score
     */
    protected float score;
    private Cell[] celllist;

    public Alignment(SequenceIF seq1, SequenceIF seq2) {
        this.refseq1 = seq1;
        this.seq2 = seq2;
    }

    /**
     * @return the seqReverse
     */
    public boolean isSeqReverse() {
        return seqReverse;
    }

    /**
     * @param seqReverse the seqReverse to set
     */
    public void setSeqReverse(boolean seqReverse) {
        this.seqReverse = seqReverse;
    }

    public int getPosInAl(int basepos) {
        if (seqReverse) {
            basepos = this.seq2.getLength() - basepos;
        }
        //
        int seqpos = this.getSeqStart2();
        String al = this.getSeqAlign2().toSequenceString();

        for (int alpos = 0; alpos < this.getLength() && seqpos <= basepos; alpos++) {
            if (seqpos == basepos) {
                return alpos;
            }
            char c = al.charAt(alpos);
            if (c != ' ' && c != '-' && c != '_') {
                seqpos++;
            }
        }
        return -1;
    }

    public static enum SCORE_TYPE {

        IDENTITY("percent identity"),
        LOGODDS("log odds"),
        TEMPLATEREADLEN("template read length"),
        IDENTITYLEN("percent identity x template read length"),
        IDENTITYLOGLEN("percent identity x log(template read length)"),
        LONGESTKMER("longest kmer");
        private final String name;

        private SCORE_TYPE(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public Alignment getReverseAlignment() {
        Alignment al = new Alignment(refseq1.complement().reverse(), seq2.complement().reverse());
        al.setRefAlign1(this.refalign1.complement().reverse());
        al.setSeqAlign2(this.align2.complement().reverse());

        char[] m = this.getMarkupLine();
        char[] revm = new char[m.length];
        for (int i = 0; i < m.length; i++) {
            revm[i] = m[m.length - i - 1];
        }
        al.setMarkupLine(revm);
        al.setGenomeEndpos(this.getGenomeEndpos());
        al.setGenomeStartpos(this.getGenomeStartpos());

        return al;
    }

    public String toString(boolean full) {
        String res = "";

        SequenceIF seqs[] = new SequenceIF[4];
        if (refalign1 == null || align2 == null) {
            res += "\nseq1: " + refseq1.toString();
            res += "\nseq2: " + seq2.toString();
        } else if (refalign1 != null && align2 != null) {

            String[] s = new String[4];
            s[0] = "";//getFlowNrString();
            s[1] = refalign1.toString();
            s[2] = this.getMarkupLineString();
            s[3] = align2.toString();
            seqs[0] = null;
            seqs[1] = refseq1;
            seqs[2] = null;
            seqs[3] = seq2;
            int len = s[1].length();
            int lines = len / WINDOW_SIZE + 1;
            for (int line = 0; line < lines; line++) {
                res += "\n";
                for (int i = 0; i < s.length; i++) {
                    len = s[i].length();
                    int a = Math.min(line * WINDOW_SIZE, len);
                    int b = Math.min(a + WINDOW_SIZE, len);
                    if (b > a) {
                        res += s[i].substring(a, b);
                        if (seqs[i] != null) {
                            if (seqs[i] instanceof SequenceIF) {
                                SequenceIF r = (SequenceIF) seqs[i];
                                if (r.getName() != null) {
                                    res += " " + r.getName();


                                }
                            }
                        }

                        res += "\n";
                    }
                }
            }
        }
        res += getSummary(full);


        // if (full) {
        // if (this.best != null) {
        // res += best.toString(full);
        // }
        // if (this.random != null) {
        // res += random.toString(full);
        // }
        // }

        return res;
    }

    public int getPosInseq(long genomepos) {
        /**
         * seq: ggggaaaattttcccc
         * gen:  start    x
         * al:   rrrrrrrrrrrrrrrrrrr
         * aleq: aaaattttcccc
         * start2 = 4
        
         */
        if (!seqReverse) {
            return (int) (genomepos - this.getGenomeStartpos() + this.getSeqStart2());
        } else {
            /**
             * seq:        ggggttttccccaaaa, rev=true
             *  rev comp:  ttttggggaaaacccc
             * gen:  start    x    end
             * al:   rrrrrrrrrrrrrrrr
             * aleq:     ggggaaaacccc
             * start2 = 4
             * a) x-start+4 = pos in revcomp
             * b) len - posinrev
             */
            int inrevcomp = (int) (genomepos - this.getGenomeStartpos() + this.getSeqStart2());
            return this.getSeq2().getLength() - inrevcomp;

        }
    }

    public SequenceIF getRefAlign1() {
        return refalign1;
    }

    public void setRefAlign1(SequenceIF align1) {
        this.refalign1 = align1;
    }

    public SequenceIF getSeqAlign2() {
        return align2;
    }

    public int getMismatches() {
        return getSimilarity() - getIdentity();
    }

    public void setSeqAlign2(SequenceIF align2) {
        this.align2 = align2;
    }

    public SequenceIF getRefSeq1() {
        return refseq1;
    }

    public int getLength() {
        return refalign1.getLength();
    }

    public SequenceIF getSeq2() {
        return seq2;
    }

    /**
     * Returns a summary for alignment
     * 
     * @return alignment summary
     */
    public String getSummary() {
        return getSummary(false, "\n");
    }

    public String getSummary(boolean full) {
        return getSummary(full, "\n");
    }

    public String getSummary(boolean all, String nl) {
        StringBuffer buf = new StringBuffer();
        DecimalFormat f1 = new DecimalFormat("0.0");
        DecimalFormat fperc = new DecimalFormat("0.0%");

        buf = buf.append(nl);
        if (refalign1 == null || align2 == null) {
            return "no alignment";

        }
        int length = refalign1.getLength();
        if (all) {
            buf.append("Alignment length:            " + length);
            buf.append(nl);
        }

        buf.append("Identity:                    ").append(this.getIdentity()).append(" = ").append(fperc.format(getIdentityPerc() / 100.0)).append(nl);
        // buf.append("Template read length:        ").append(this.getTemplateReadLength()).append("bp").append(nl);
        // if (all) {
        if (getRefStart1() > 0) {
            buf.append("Start in reference:          ").append(getRefStart1());
            buf.append(nl);
        }
        if (getSeqStart2() > 0) {
            buf.append("Start in seq:                ").append(getSeqStart2());
            buf.append(nl);
        }
        // }
        // buf.append("Rel.identity (vs best,rand): " + fperc.format(Math.max(0,
        // getRelativeIdentity()))+"\n");

        // }
        //    buf.append("Similarity:                  ").append(this.getSimilarity()).append(" = ").append(fperc.format(getSimilarityPerc() / 100.0));
        //   buf.append(nl);
        if (this.getMismatches() > 0) {
            buf.append("Mismatches:                  ").append(this.getMismatches()).append(" = ").append(fperc.format(getMismatchesPerc() / 100.0));
            buf.append(nl);
        }
        if (this.getRefGaps1() > 0) {
            buf.append("Gaps in ref:                 ").append(this.getRefGaps1()).append(" = ").append(fperc.format(this.getRefGapsPercent1() / 100.0));
            buf.append(nl);
        }
        if (this.getSeqGaps2() > 0) {
            buf.append("Gaps in seq:                 ").append(this.getSeqGaps2()).append(" = ").append(fperc.format(this.getSeqGapsPercent2() / 100.0));
            buf.append(nl);
        }
        int[] Qvalues = {7, 10, 17, 20, 47};
        int[] qlens = computeQlengths(Qvalues);
        buf.append(nl).append("<b>Q lengths</b>: ").append(nl);
        DecimalFormat f = new DecimalFormat("0.0%");
        for (int i = 0; i < Qvalues.length; i++) {
            double q = Qvalues[i];
            double err = Math.pow(10.0, (q / (-10.0)));
            if (i + 1 == Qvalues.length) {
                f = new DecimalFormat("0.000%");
            }
            buf.append("Q" + Qvalues[i] + " (" + f.format(err) + ")=" + qlens[i]);
            if (i + 1 < Qvalues.length) {
                buf.append(",  ");
            }

        }
        buf.append(nl);
        buf.append("Qlen=" + this.getSeq2().getLength() + ", Tlen=" + this.getRefSeq1().getLength());
        buf.append(nl);
        String flowproblems = this.getFlowNrForErrors(nl);
        if (flowproblems != null && flowproblems.length() > 0) {
            buf.append(nl).append("<b>Mapping errors -> flow #</b>:").append(nl).append(flowproblems);
        }
        return buf.toString();
    }

    public double getRelativeIdentity() {
        double r = 50;// random.getIdentityPerc();
        double b = 100.0;//
        double s = this.getIdentityPerc();
        double frac = (s - r) / (b - r);
        return Math.max(0, frac);
    }

    public void calculateStats() {
        calculateStats(false);
    }

    public void calculateStats(boolean debug) {
        // score = this.();
        identity = 0;

        gaps = new int[2];

        // length = align1.getLength();
        similarity = 0;
        char c1, c2; // the next character
        for (int i = 0, n = refalign1.getLength(); i < n; i++) {
            c1 = refalign1.getBaseChar(i);
            c2 = align2.getBaseChar(i);
            // the next character in the first sequence is a gap

            if (c1 == DNASequence.GAPCHAR) {
                if (c2 != DNASequence.GAPCHAR) {
                    gaps[0]++;

                }
            } else if (c2 == DNASequence.GAPCHAR) {
                if (c1 != DNASequence.GAPCHAR) {
                    gaps[1]++;

                }
            } else if (Character.toUpperCase(c1) == Character.toUpperCase(c2) && this.getMarkupLine()[i] == Markups.IDENTITY) {
                identity++;
                if (debug) {
                    p("ident: " + c1 + "=" + c2);

                }
                similarity++;

            } // the next character in the second sequence is a gap
            // the next characters in boths sequences are not gaps
            else if (c1 != DNASequence.SPACECHAR && c2 != DNASequence.SPACECHAR) {
                similarity++;
                // p("sim: "+c1+"="+c2);
            }
        }
        endgaps = new int[2];
        for (int i = 0; i < refalign1.getLength() && refalign1.isGap(i); i++) {
            endgaps[0]++;
        }
        for (int i = refalign1.getLength() - 1; i > 0 && refalign1.isGap(i); i--) {
            endgaps[0]++;
        }
        for (int i = 0; i < refalign1.getLength() && align2.isGap(i); i++) {
            endgaps[1]++;
        }
        for (int i = refalign1.getLength() - 1; i > 0 && align2.isGap(i); i--) {
            endgaps[1]++;
        }

        // don't count END gaps

    }

    public int getEndgaps() {
        return endgaps[0] + endgaps[1];
    }

    public int getEndgaps1() {
        return endgaps[0];
    }

    public int getEndgaps2() {
        return endgaps[1];
    }

    /**
     * Check if the calculated score matches the field score.
     * 
     * @return true if equal, else false. (By: Bram Minnaert)
     */
    public int getIdentity() {
        return identity;
    }

    public void setIdentity(int identity) {
        this.identity = identity;
    }

    public int getSimilarity() {
        return similarity;
    }

    public float getSimilarityPerc() {
        if (refalign1.getLength() < 1) {
            // p("Strange alignment:"+align1+"/"+align2);
            return 0;
        }
        return similarity * 100 / refalign1.getLength();
    }

    public float getMismatchesPerc() {
        if (getSimilarity() == 0) {
            // warn("No similarity... ");
            return 0;
        }
        return this.getMismatches() * 100 / getSimilarity();
    }

    public double getScore(SCORE_TYPE type) {
        if (type == SCORE_TYPE.IDENTITY) {
            return this.getIdentityPerc();
        } else if (type == SCORE_TYPE.TEMPLATEREADLEN) {
            return this.getTemplateReadLength();
        } else if (type == SCORE_TYPE.LOGODDS) {
            return this.getScore();
        } else if (type == SCORE_TYPE.IDENTITYLOGLEN) {
            return this.getIdentityPerc() * Math.log(this.getTemplateReadLength());
        } else if (type == SCORE_TYPE.IDENTITYLEN) {
            return this.getIdentityPerc() * this.getTemplateReadLength();
        } else if (type == SCORE_TYPE.LONGESTKMER) {
            return this.getLongestIdentityStretch().getScore();
        } else {
            err("Unknown score type:" + type);

        }
        return -1;
    }

    public double getIdentityPerc() {
        if (refalign1.getLength() < 1) {
            // p("Strange alignment:"+align1+"/"+align2);
            return 0;
        }
        // if (identity <=0) {
        // this.calculateScore();
        // }
        return (double) identity * 100.0 / (double) refalign1.getLength();
    }

    public void setSimilarity(int similarity) {
        this.similarity = similarity;
    }

    public int getGaps() {
        if (gaps == null) {
            this.calculateStats();
        }
        return gaps[0] + gaps[1];
    }

    public int getRefGaps1() {
        if (gaps == null) {
            this.calculateStats();
        }
        return gaps[0];
    }

    public int getSeqGaps2() {
        if (gaps == null) {
            this.calculateStats();
        }
        return gaps[1];
    }

    public double getGapsPercent() {
        return 100.0 * (double) (getGaps() - getEndgaps()) / (double) this.getLength();
    }

    public double getRefGapsPercent1() {
        return 100.0 * (double) (getRefGaps1() - getEndgaps1()) / (double) this.getLength();
    }

    public double getSeqGapsPercent2() {
        return 100.0 * (double) (getSeqGaps2() - getEndgaps2()) / (double) this.getLength();
    }

    public float getScore() {
        // if (score==0) {
        // this.calculateScore();
        // }
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public void setRefSeq1(SequenceIF seq1) {
        this.refseq1 = seq1;
    }

    public void setSeq2(SequenceIF seq2) {
        this.seq2 = seq2;
    }

    public int getRefStart1() {
        return refstart1;
    }

    public void setRefStart1(int start1) {
        this.refstart1 = start1;
    }

    public int getSeqStart2() {
        return start2;
    }

    public void setSeqStart2(int start2) {
        this.start2 = start2;
    }

    public char[] getMarkupLine() {
        return markupLine;
    }

    public boolean isMatch(int pos) {
        return markupLine[pos] == Markups.IDENTITY;
    }

    public boolean isGap(int pos) {
        return markupLine[pos] == Markups.GAP;
    }

    public boolean isMisMatch(int pos) {
        return markupLine[pos] == Markups.MISMATCH;
    }

    public String getMarkupLineString() {
        return new String(markupLine);
    }

    public String getCoordString() {
        int len = this.getLength();
        StringBuffer res = new StringBuffer();
        for (int pos = 0; pos <= len; pos++) {
            res = res.append(" ");
        }

        for (int pos = 10; pos <= len; pos += 10) {
            String nr = "" + pos;
            int l = nr.length();
            res.insert(pos - l, nr);
        }

        return res.toString();
    }

    public String getFlowNrForErrors(String nl) {
        if (flowstring != null) {
            return flowstring;

        }
        if (!(seq2 instanceof Read)) {
            return "";
        }
        Read read = (Read) seq2;

        StringBuffer res = new StringBuffer();
        int seqpos = this.getSeqStart2();
        String al = this.getSeqAlign2().toSequenceString();
        String ref = this.getRefAlign1().toSequenceString();
        String mark = this.getMarkupLineString();
        res = res.append("al pos, read pos, flow nr, alignment").append(nl);

        String spaces = "     ";
        if (nl.startsWith("<")) {
            spaces = ",&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
        }
        for (int pos = 0; pos < this.getLength(); pos++) {
            char c = al.charAt(pos);
            char m = mark.charAt(pos);
            if (m != '|' && seqpos < this.getSeq2().getLength()) {
                int sp = seqpos;
                if (this.isSeqReverse()) {
                    sp = (this.getSeq2().getLength() - sp);
                }
                int flow = read.findFlow(sp);

                res = res.append(pos).append(spaces);
                res = res.append(sp).append(spaces);

                if (nl.startsWith("<")) {
                    res = res.append("<font color='aa0000'><b>" + flow + "</b></font>");
                } else {
                    res = res.append(flow);
                }
                res = res.append(spaces).append(c).append(m).append(ref.charAt(pos)).append(nl);
            }
            if (c != ' ' && c != '-' && c != '_') {
                seqpos++;
            }

        }
        flowstring = res.toString();

        return flowstring;
    }

    public int findSequencPosition(int alignmentpos) {
        int seqpos = this.getSeqStart2();
        String al = this.getSeqAlign2().toSequenceString();

        int sp = -1;
        for (int pos = 0; pos <= alignmentpos && pos < this.getLength(); pos++) {
            char c = al.charAt(pos);
            if (seqpos < this.getSeq2().getLength()) {
                sp = seqpos;
                if (this.isSeqReverse()) {
                    sp = (this.getSeq2().getLength() - sp);
                }
            }
            if (c != ' ' && c != '-' && c != '_') {
                seqpos++;
            }

        }
        return sp;

    }

    public int findFlow(int alignmentpos, String floworder) {

        int seqpos = this.findSequencPosition(alignmentpos);
        if (seqpos < 0) {
            return -1;
        }

        int flow = SeqFlowUtil.findFlow(seqpos + 4, this.seq2.toSequenceString(), floworder);
        return flow;
    }

    public void setMarkupLine(char[] markupLine) {
        this.markupLine = markupLine;
    }

    public void setCelllist(ArrayList<Cell> cells) {
        this.celllist = cells.toArray(new Cell[0]);
    }

    public void setCelllist(Cell[] celllist) {
        this.celllist = celllist;
    }

    public Cell getCellAt(int posinalignment) {
        if (celllist == null) {
            warn("No celllist");
            return null;
        }
        if (celllist == null || posinalignment >= celllist.length) {
            return null;

        }
        return celllist[posinalignment];
    }

    public int getTemplateReadLength() {
        Cell cell = getLastCell();
        int len_in_template = cell.getRow() - getRefStart1();
        return len_in_template;
    }

    public Cell getLastCell() {
        if (celllist == null) {
            warn("No celllist");
            return null;
        }
        return celllist[celllist.length - 1];
    }

    public int getBasePositionInSequence(int seqnr, int posinalignment) {
        Cell cell = getCellAt(posinalignment);
        if (cell == null) {
            return 0;

        }
        if (seqnr == 2) {
            return Math.min(cell.getCol(), this.seq2.getLength() - 1);

        } else if (seqnr == 1) {
            return Math.min(cell.getRow(), this.refseq1.getLength() - 1);

        } else {
            err("There are only 2 sequences. Seqnr is: " + seqnr);

        }
        return 0;
    }

    public int getLongestKmer() {
        return (int) getLongestIdentityStretch().getAlignmentpos();
    }

    public Cell getLongestIdentityStretch() {
        Cell best = new Cell();
        for (int pos = 0; pos < this.getLength();) {
            int i = pos;
            int samecount = 0;
            while (i < this.getLength() && getRefAlign1().getBaseChar(i) == getSeqAlign2().getBaseChar(i)
                    && getRefAlign1().isBase(i)) {
                i++;
                samecount++;
            }
            if (samecount > best.getScore()) {
                Cell cell = getCellAt(pos);
                if (cell != null) {
                    best.setScore(samecount - 1);
                    best.setAlignmentpos(pos);
                    best.setRow(cell.getRow());
                    best.setCol(cell.getCol());
                }
            }
            pos = i + 1;
        }
        // if (best != null)
        // p("Longest strech of identity at cell :"+best+", nr same: "+best.getScore());

        return best;
    }

    public ArrayList<Cell> getIdentityStretchesLongerThan(int kmer_size) {

        ArrayList<Cell> cells = new ArrayList<Cell>();
        for (int pos = 0; pos < this.getLength();) {
            int i = pos;
            int samecount = 0;
            while (i < this.getLength() && getRefAlign1().getBaseChar(i) == getSeqAlign2().getBaseChar(i)
                    && getRefAlign1().isBase(i)) {
                i++;
                samecount++;
            }
            if (samecount > kmer_size) {
                Cell cell = getCellAt(pos);
                if (cell != null) {
                    for (int size = kmer_size; size < samecount; size++) {
                        Cell best = new Cell();
                        best.setScore(size);
                        best.setAlignmentpos(pos);
                        best.setRow(cell.getRow());
                        best.setCol(cell.getCol());
                        cells.add(best);
                    }
                }
            }
            pos = i + 1;
        }
        // if (best != null)
        // p("Longest strech of identity at cell :"+best+", nr same: "+best.getScore());

        return cells;
    }

    public Cell[] getCelllist() {
        return celllist;
    }

    public int getPathCount(Cell cell) {
        if (pathscore != null && cell != null) {

            return pathscore[cell.getCol()][cell.getRow()];
        } else {
            return 0;

        }
    }

    private int getPathCount(int pos) {
        if (celllist == null || pos >= celllist.length) {
            return 0;

        }
        return getPathCount(celllist[pos]);
    }

    public String toHtml() {
        return toHtml(false);
    }

    public String toHtml(boolean full) {
        String res = "<font face=\"Courier\">";
        res = res + toHtml_r(full);
        res = res + "</font>";
        return res;
    }

    /** uses the numbers on top as indication of what color to use */
    public String toHtml_r(boolean full) {
        String res = "";


        SequenceIF seqs[] = new SequenceIF[4];
        if (refalign1 == null || align2 == null) {
            res += "<br>seq1: " + refseq1.toString();
            res += "<br>seq2: " + seq2.toString();
        } else if (refalign1 != null && align2 != null) {
            //  res += "<br>\n";
            String[] s = new String[4];
            s[0] = getCoordString();
            s[3] = refalign1.toString();
            s[2] = this.getMarkupLineString();
            s[1] = align2.toString();
            seqs[0] = null;
            seqs[3] = refseq1;
            seqs[2] = null;
            seqs[1] = seq2;
            int len = s[1].length();
            int lines = len / WINDOW_SIZE + 1;
            for (int line = 0; line < lines; line++) {
                res += "\n<br>";
                for (int i = 0; i < s.length; i++) {
                    len = s[i].length();
                    int a = Math.min(line * WINDOW_SIZE, len);
                    int b = Math.min(a + WINDOW_SIZE, len);
                    if (i == 0 || i == 2) {
                        res += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
                    } else if (i == 1) {
                        res += "seq:&nbsp;";
                    } else if (i == 3) {
                        res += "ref:&nbsp;";
                    }

                    if (b > a) {
                        String sub = s[i].substring(a, b);
                        if (i == 0) { // coord! replace spaces with
                            // something else or else it
                            // won't show
                            for (int p = 0; p < sub.length(); p++) {
                                char c = sub.charAt(p);
                                if (c == ' ') {
                                    res += "&nbsp;";

                                } else {
                                    res += c;

                                }
                            }
                        } else if (i == 1 || i == 3) { // seq! color it based
                            // on match or mismatch                            
                            int oldcount = -1;
                            for (int p = 0; p < sub.length(); p++) {
                                int count = 0;
                                char mark = s[2].charAt(p + a);
                                if (mark != Markups.IDENTITY) {
                                    count = 9;
                                }
                                char base = sub.charAt(p);
                                if (count != oldcount) {
                                    if (oldcount > -1) {
                                        res += "</font>";
                                        // add color!
                                    }
                                    res += "<font color=\"" + getFontColor(count) + "\">" + base;
                                } else { // same color!
                                    res += base;
                                }
                                oldcount = count;
                            }

                            res += "</font><font color=\"000000\">";
                        } else if (i == 2) { // markup! replace spaces with
                            // something else or else it
                            // won't show
                            for (int p = 0; p < sub.length(); p++) {
                                char c = sub.charAt(p);
                                if (c == ' ') {
                                    res += "&nbsp;";

                                } else {
                                    res += c;

                                }
                            }
                        } else {
                            res = res + sub;

                        }
                        if (seqs[i] != null) {
                            if (seqs[i] instanceof SequenceIF) {
                                SequenceIF r = (SequenceIF) seqs[i];
                                if (r.getName() != null) {
                                    res += "&nbsp;" + r.getName();
                                }
                            }
                        }

                        res += "\n<br>";
                    }
                }
            }
        }
        String end = getSummary(full, "<br>\n");
        end = end.replace(" ", "&nbsp;");
        res += end;



        return res;
    }

    private String getFontColor(int count) {
        if (count == 9) {
            return "AA0000"; // red

        } else if (count == 8) {
            return "FF6600"; // red-orange

        } else if (count == 7) {
            return "FF9900"; // orange

        } else if (count == 6) {
            return "DDCC00"; // yellow-brownish

        } else if (count == 6) {
            return "AADD00"; // yellow-green

        } else if (count == 5) {
            return "00EEFF"; // cyan

        } else if (count == 4) {
            return "00AAFF"; // light blue

        } else if (count == 3) {
            return "0000FF"; // blue

        } else if (count == 2) {
            return "0000AA"; // dark blue

        } else {
            return "000000"; // black

        }
    }

    public int findPosInSequence(SequenceIF al, SequenceIF seq, int posinal) {
        int sp = 0;

        if (al.getBasecharPositionCode(posinal) == DNASequence.SPACE) {
            p("Char in alignment is SPACE.. cannot find sequence - returning -1");
            return -1;
        } else if (al.getBasecharPositionCode(posinal) == DNASequence.GAP) {
            p("Char in alignment is GAP.. cannot find sequence - returning -1");
            return -1;
        }
        // find postion of alignment string in sequence... very inefficient.
        StringBuffer alb = new StringBuffer();
        // int spaces=0;
        for (int i = 0; i < al.getLength(); i++) {
            if (al.isBase(i)) {
                alb = alb.append(al.getBaseChar(i));
            }
            // else if (al.getBaseChar(i)=='.' || al.getBaseChar(i)==' ')
            // spaces++;
        }

        int startpos = seq.toSequenceString().toUpperCase().indexOf(alb.toString().toUpperCase());
        if (startpos < 0) {
            err("Could not find alignment string (NO GAPS!) " + alb + ", " + al.getClass().getName() + " " + al + ", in seq " + seq.toSequenceString());
        }
        if (al.getBasecharPositionCode(posinal) == DNASequence.GAP) {
            warn("Char in alignmentis gap.. cannot find sequence");
        }
        sp = startpos;
        for (int i = 0; i < posinal; i++) {
            if (al.isBase(i)) {
                sp++;
            }
        }
        if (sp > seq.getLength()) {
            p("seq pos out of range:" + sp);
            err("Could not find correct position in seq. al(" + posinal + ")=" + al.getBaseChar(posinal) + "<>seq(" + sp + ")=" + seq.getBaseChar(sp) + "\nal w gaps: " + al.toSequenceString() + "\nAl wo gaps: " + alb + "\nseq: " + seq.toSequenceString() + "\nGot posinal: " + posinal + ", and pos in seq: " + sp + ", startpos: " + startpos);
        }
        if (al.getBaseChar(posinal) != seq.getBaseChar(sp)) {
            err("Could not find correct position in seq. al(" + posinal + ")=" + al.getBaseChar(posinal) + "<>seq(" + sp + ")=" + seq.getBaseChar(sp) + "\nal w gaps: " + al.toSequenceString() + "\nAl wo gaps: " + alb + "\nseq: " + seq.toSequenceString() + "\nGot posinal: " + posinal + ", and pos in seq: " + sp + ", startpos: " + startpos);
        }

        return sp;
    }

    /**
     * @return the genome_startpos
     */
    public int getGenomeStartpos() {
        return genome_startpos;
    }

    /**
     * @param genome_startpos the genome_startpos to set
     */
    public void setGenomeStartpos(int genome_startpos) {
        this.genome_startpos = genome_startpos;
    }

    /**
     * @return the genome_endpos
     */
    public int getGenomeEndpos() {
        return genome_endpos;
    }

    /**
     * @param genome_endpos the genome_endpos to set
     */
    public void setGenomeEndpos(int genome_endpos) {
        this.genome_endpos = genome_endpos;
    }

    public int[] computeQlengths(int[] errorValues) {
        Alignment al = this;
        int[] Qvalues = {7, 10, 17, 20, 47};
        if (errorValues != null) {
            Qvalues = errorValues;
        }
        int nrvals = Qvalues.length;
        // int[] phred_lens = new int[nrvals];
        int[] q_len_vec = new int[nrvals];

        if (al == null || al.getLength() < 1) {
            return q_len_vec;
        }
        double[] max_error_value = new double[nrvals];

        int t_diff = 0;
        int match_base = 0;
        int seq_length = 0;
        int ref_seq_length = 0;

        int[] length_of_ref_sequence_per_al_pos = new int[al.getLength() + 1];

        //int[] q_score_vec = new int[nrvals];
        for (int i = 0; i < nrvals; i++) {
            double q = (double) Qvalues[i];
            double err = Math.pow(10.0, (q / (-10.0)));
            max_error_value[i] = err;
        }
        int prev_nr_mismatches = 0;
        int prev_loc_len = 0;

        /*
        tDna => pad_source 
        match => pad_match
        qdna => pad_target
         */
        //coord_t equiv_counter = 0;

        String ref_alignment = al.getRefAlign1().toSequenceString();
        String seq_alignment = al.getSeqAlign2().toSequenceString();
        String match_string = al.getMarkupLineString();

        for (int i = 0; i < al.getLength(); i++) {
            length_of_ref_sequence_per_al_pos[i] = ref_seq_length;
            if (ref_alignment.charAt(i) != '-' && ref_alignment.charAt(i) != '_') {
                ref_seq_length++;
            }
            if (match_string.charAt(i) != '|') {
                t_diff++;
            } else {
                match_base++;
            }

            if (seq_alignment.charAt(i) != '-' && seq_alignment.charAt(i) != '_') {
                seq_length++;
            }
        }

        int pos_in_alignment = al.getLength();

        int num_slop = 0; // nr of base to ignore from start

        for (int k = 0; k < max_error_value.length; k++) {
            //long loc_len = n_qlen;
            //long loc_err = t_diff;
            int loc_len = al.getLength();
            int nr_of_mismatches = al.getMismatches() + al.getGaps();
            if (nr_of_mismatches == 0) {
                // perfect alignment
                q_len_vec[k] = al.getLength();
            } else {
                if (k > 0) {
                    loc_len = prev_loc_len;
                    nr_of_mismatches = prev_nr_mismatches;
                }

                while ((loc_len > 0) && (pos_in_alignment >= num_slop) && pos_in_alignment > 0) {

                    double err = (double) (nr_of_mismatches) / (double) (loc_len);
                    if (err <= max_error_value[k] && length_of_ref_sequence_per_al_pos[loc_len] != 0) {
                        q_len_vec[k] = length_of_ref_sequence_per_al_pos[loc_len];
                        prev_nr_mismatches = nr_of_mismatches;
                        prev_loc_len = loc_len;
                        break;
                    }

                    if (match_string.charAt(pos_in_alignment - 1) != '|') {
                        nr_of_mismatches--;
                    }
                    if (seq_alignment.charAt(pos_in_alignment - 1) != '-' && seq_alignment.charAt(pos_in_alignment - 1) != '_') {
                        loc_len--;
                    }
                    pos_in_alignment--;

                }
            }

        }
//        for (int k = 0; k < Q.length; k++) {
//            p("Q="+Qvalues[k]+"-> "+Q[k]+", len="+q_len_vec[k]);
//        }
        return q_len_vec;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(Alignment.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(Alignment.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(Alignment.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("Alignment: " + msg);
        //Logger.getLogger( Alignment.class.getName()).log(Level.INFO, msg, ex);
    }
}
