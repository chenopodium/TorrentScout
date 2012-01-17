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


import com.iontorrent.sff.SffRead;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.iontorrent.seq.alignment.Alignment;

/**
 *
 * @author Chantal Roth
 */
public class Read extends DNASequence{

    private SffRead sff;
    private Alignment align;
    private String fastaFile;
    private String referenceName;
    private String commandLine;
    private int alignmentStart;
    private int alignmentEnd;
    private String cigarString;
    private String md;
    
    private int flags;
    
    public Read(SffRead sff) {
        super(sff.getBases().substring(4), sff.getName());
        this.sff = sff;
        
    }
    public int getCol() {
        return sff.getCol();
    }
    public int getRow() {
        return sff.getRow();
              
    }
    public String getFlowOrder() {        
        return sff.getFlowOrder();
    }
    public String getKey() {
        return sff.getKey();
    }
    public int[] getFlowgram() {
        return sff.getFlowgram();
    }
    public float getFlowValue(int flow) {
        return sff.getFlowgram()[flow];
    }
    public ArrayList<Integer> getEmptyFlows(){
        ArrayList<Integer>  res = new ArrayList<Integer> ();
        for (int f = 0; f < getFlowgram().length; f++) {
            if (getFlowValue(f)<50) {
                res.add(f);
            }
        }
        return res;       
    }
     public ArrayList<Integer> getNonEmptyFlows(){
        ArrayList<Integer>  res = new ArrayList<Integer> ();
        for (int f = 0; f < getFlowgram().length; f++) {
            if (getFlowValue(f)>=50) {
                res.add(f);
            }
        }
        return res;       
    }
    public int[] getFlowIndex() {
        return Arrays.copyOfRange(sff.getFlow_index(), 4, sff.getFlow_index().length-4);
    }
//    public String getFlowOrder() {
//        return sff.getFlowOrder();
//    }
//    public String getKey() {
//        return sff.getKey();
//    }
    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(Read.class.getName()).log(Level.SEVERE, msg, ex);
    }
    
    private void err(String msg) {
        Logger.getLogger(Read.class.getName()).log(Level.SEVERE, msg);
    }
    
    private static void warn(String msg) {
        Logger.getLogger(Read.class.getName()).log(Level.WARNING, msg);
    }
    
    private static void p(String msg) {
        System.out.println("Read: " + msg);
        //Logger.getLogger( Read.class.getName()).log(Level.INFO, msg);
    }

    /**
     * @return the align
     */
    public Alignment getAlign() {
        return align;
    }

    /**
     * @param align the align to set
     */
    public void setAlign(Alignment align) {
        this.align = align;        
    }

    /**
     * @return the fastaFile
     */
    public String getFastaFile() {
        return fastaFile;
    }

    /**
     * @param fastaFile the fastaFile to set
     */
    public void setFastaFile(String fastaFile) {
        this.fastaFile = fastaFile;
    }

    /**
     * @return the referenceName
     */
    public String getReferenceName() {
        return referenceName;
    }

    /**
     * @param referenceName the referenceName to set
     */
    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    /**
     * @return the commandLine
     */
    public String getCommandLine() {
        return commandLine;
    }

    /**
     * @return the alignmentStart
     */
    public int getAlignmentStart() {
        return alignmentStart;
    }

    public int getPosInRead(long genomepos) {
        if (align == null) {
            warn("Got no alignment for read");
            return -1;
        }
        int basepos =  align.getPosInseq(genomepos);
        return basepos;
    }
    /**
     * @param alignmentStart the alignmentStart to set
     */
    public void setAlignmentStart(int alignmentStart) {
        this.alignmentStart = alignmentStart;
    }

    public int findFlow(int basepos) {
        if (basepos<0) return -1;
        return SeqFlowUtil.findFlow(basepos+4, sff.getBases(), sff.getFlowOrder());
    }
    public int findFlowForGenomePos(long genomepos) {
        int basepos = this.getPosInRead(genomepos);
        return findFlow(basepos);
    }
    /**
     * @return the alignmentEnd
     */
    public int getAlignmentEnd() {
        return alignmentEnd;
    }
   
    
     public SequenceIF getReverseComplement() {
        return this.complement().reverse();
    }

    /**
     * @param alignmentEnd the alignmentEnd to set
     */
    public void setAlignmentEnd(int alignmentEnd) {
        this.alignmentEnd = alignmentEnd;
    }

    /**
     * @return the cigarString
     */
    public String getCigarString() {
        return cigarString;
    }

    /**
     * @param cigarString the cigarString to set
     */
    public void setCigarString(String cigarString) {
        this.cigarString = cigarString;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }
    public boolean isReverse() {
        return flags == 16;
    }

    public int getFlags() {
        return flags;
    }

    /**
     * @return the md
     */
    public String getMd() {
        return md;
    }
    public String getHtmlFlowGramInfo() {
        return sff.getHtmlFlowInformation(200) ;
    }

    public int[] getAbsoluteFlowIndex() {
        return sff.getAbsoluteFlowIndex();
    }
    /**
     * @param md the md to set
     */
    public void setMd(String md) {
        this.md = md;
    }
   
   
}
