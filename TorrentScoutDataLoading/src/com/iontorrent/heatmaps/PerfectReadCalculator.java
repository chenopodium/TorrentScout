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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.iontorrent.seq.alignment.Alignment;

/**
 *
 * @author Chantal Roth
 */
public class PerfectReadCalculator extends AbstractSMCalculator implements ScoreMaskCalculatorIF {

    int len;

    String mark = "";
    public PerfectReadCalculator() {
        this("200");
    }

    public PerfectReadCalculator(String lenstr) {
        super("Perfect read pattern", "Finds wells with perfect alignments of a certain length", ScoreMaskFlag.CUSTOM1);
        Parameter params[] = new Parameter[1];
        params[0] = new Parameter("Length of read without errors", lenstr, "The (minimum) read length");
        
        this.setParams(params);

    }

    @Override
    public void setParams(Parameter[] par) {
        super.setParams(par);
        len = 200;
        try {
            len = Integer.parseInt(par[0].getValue());
        }
        catch (Exception e) {
            err("Cannot convert "+par[0].getValue()+" to an integer");
        }
        if (len <=0) mark = "x";
        else {
            StringBuffer b = new StringBuffer();
            for (int i =0; i < len; i++) {
                b=b.append("|");
            }
            mark = b.toString();
        }
    }

    @Override
    public double compute(Alignment al) {
        
        if (al.getIdentity()>=len) {
            // check stretch of perfect matches
            String check= al.getMarkupLineString();
            //check = check.replace(":", "|");
            //p("Comparing "+check+" with "+mark);
            if (check.indexOf(mark) >-1) {
                //R_2012_01_12_18_02_15_user_BEL-16-l499-
                return 1;
            }
        }
        return 0;
        
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(PerfectReadCalculator.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {

        Logger.getLogger(PerfectReadCalculator.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(PerfectReadCalculator.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("PerfectReadCalculator: " + msg);
        //Logger.getLogger( PerfectReadCalculator.class.getName()).log(Level.INFO, msg);
    }

    @Override
    public boolean requiresRead() {
        return false;
    }
}
