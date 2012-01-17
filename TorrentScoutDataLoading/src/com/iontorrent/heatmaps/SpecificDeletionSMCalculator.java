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
package com.iontorrent.heatmaps;

import com.iontorrent.rawdataaccess.wells.ScoreMaskFlag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.iontorrent.seq.DNASequence;
import org.iontorrent.seq.Read;
import org.iontorrent.seq.alignment.Alignment;

/**
 *
 * @author Chantal Roth
 */
public class SpecificDeletionSMCalculator extends AbstractSMCalculator implements ScoreMaskCalculatorIF {

    String patseq;
    String patref;
    int startflow;
    int endflow;
    DNASequence seq;
    DNASequence ref;

    public SpecificDeletionSMCalculator() {
        this("CCT_C", "CCTTC");
    }

    public SpecificDeletionSMCalculator(String patseq, String patref) {
        super("Alignment pattern", "Finds wells with a certain alignment pattern", ScoreMaskFlag.CUSTOM1);
        Parameter params[] = new Parameter[4];
        params[0] = new Parameter("Alignment string of read", patseq, "The alignment string that must be present in a read");
        params[1] = new Parameter("Alignment string of reference", patref, "The alignment string that must be present in the reference");
        params[2] = new Parameter("First flow", "" + 0, "The range of flows between which the alignment pattern has to appear");
        params[3] = new Parameter("Last flow", "" + 0, "The range of flows between which the alignment pattern has to appear");
        this.setParams(params);

    }

    @Override
    public void setParams(Parameter[] par) {
        super.setParams(par);
        seq = new DNASequence(par[0].getValue());
        ref = new DNASequence(par[1].getValue());

        startflow = par[2].getIntValue();
        endflow = par[3].getIntValue();

    }

    @Override
    public double compute(Alignment al) {
        ArrayList<Integer> posr = al.getRefAlign1().findAll(ref, 0, 0);
        if (posr == null || posr.size() < 1) {
            return 0;
        }
        //   p("Got posr: "+posr);
        ArrayList<Integer> poss = al.getSeqAlign2().findAll(seq, 0, 0);

        if (poss == null || poss.size() < 1) {
            return 0;
        }
        // now check if they match :-)
        // find commeon index
        //    p("Found potential match: "+posr+"/"+poss);
        for (int r : posr) {
            for (int s : poss) {
                if (s == r) {
                    // now check flows
                    if (endflow > startflow && endflow > 0) {
                        int flow = al.findFlow(s, expContext.getFlowOrder());
                        if (flow >= startflow && flow <= endflow) {
                            return 1;
                        }

                    } else {
                        return 1;
                    }
                }
            }
        }
        // p("No match:")
        return 0;

    }

    @Override
    public boolean requiresRead() {
        return false;
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(SpecificDeletionSMCalculator.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {

        Logger.getLogger(SpecificDeletionSMCalculator.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(SpecificDeletionSMCalculator.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("SpecificDeletionSMCalculator: " + msg);
        //Logger.getLogger( SpecificDeletionSMCalculator.class.getName()).log(Level.INFO, msg);
    }
}
