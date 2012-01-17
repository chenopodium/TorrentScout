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

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.rawdataaccess.wells.ScoreMaskFlag;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.iontorrent.seq.DNASequence;
import org.iontorrent.seq.Read;
import org.iontorrent.seq.SeqFlowUtil;
import org.iontorrent.seq.alignment.Alignment;

/**
 *
 * @author Chantal Roth
 */
public class PrimerSMCalculator extends AbstractSMCalculator implements ScoreMaskCalculatorIF {

    DNASequence findseq;
    int startflow;
    int endflow;

    public PrimerSMCalculator() {
        this("ATCACCGACTGCCCATAGAGAGG");

    }

    public PrimerSMCalculator(String patseq) {
        super("sequence pattern", "Finds wells with a specific sequence", ScoreMaskFlag.CUSTOM1);
        Parameter params[] = new Parameter[3];
        params[0] = new Parameter("Sequence", patseq, "A sequence that must be present in a read");
        params[1] = new Parameter("First flow", "" + 0, "The range of flows between which the alignment pattern has to appear");
        params[2] = new Parameter("Last flow", "" + 0, "The range of flows between which the alignment pattern has to appear");

        this.setParams(params);

    }

    @Override
    public void setParams(Parameter[] par) {
        super.setParams(par);
        findseq = new DNASequence(par[0].getValue());
        startflow = par[1].getIntValue();
        endflow = par[2].getIntValue();

    }

    @Override
    public double compute(Alignment al) {

        int posr = al.getSeq2().find(findseq, 0, 0);
        if (posr < 0) {
            // if (show) p("Could not find "+findseq+" in "+al.getSeq2());
            return 0;
        } else {

            boolean show = Math.random() > 0.99;
            ArrayList<Integer> res = al.getSeq2().findAll(findseq, 0, 0);
            if (show) {
                p("Found " + findseq + " in " + al.getSeq2() + ":" + res);
            }
            // now check flows
            if (res == null || res.size() < 1) {
                err("Res should contain ints: " + res + ", seq=" + al.getSeq2() + ", to find: " + findseq);
                return 0;
            }

            if (endflow > startflow && endflow > 0) {
                String seq = al.getSeq2().toSequenceString();
                if (show) {
                    p("Checking if " + res + " are between " + startflow + "-" + endflow);
                }
                for (Integer seqpos : res) {
                    try {
                        int flow = SeqFlowUtil.findFlow(seqpos + 4, seq, expContext.getFlowOrder());
                        if (flow >= startflow && flow <= endflow) {
                            return 1;
                        }
                    } catch (Exception e) {
                        err(e.getMessage());
                    }
                }
                if (show) {
                    p("No, " + res + " are NOT between " + startflow + "-" + endflow);
                }
                return 0;

            } else {
                if (show) {
                    p("No startflow/endflow: " + startflow + ", " + endflow);
                }
                return 1;
            }
        }
    }

    @Override
    public boolean requiresRead() {
        return false;
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(PrimerSMCalculator.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {

        Logger.getLogger(PrimerSMCalculator.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(PrimerSMCalculator.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("PrimerSMCalculator: " + msg);
        //Logger.getLogger( PrimerSMCalculator.class.getName()).log(Level.INFO, msg);
    }
}
