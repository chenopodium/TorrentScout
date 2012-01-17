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


import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This code is translated from R code, so I tried to use the same variable names whenever possible 
 * to be able to compare the code...(Earl)
 * @author Chantal Roth
 */
public class SeqFlowUtil {

    
    
    /** find which flow cooresponds to x  in sequence space */
    public static int findLocationFlow(int x, SeqFlow sf) {   
        if (x < 0 || x > sf.loca.length) {
            p("out of bounds: "+x);
            return 0;
        }
       return sf.loca[x];
    }

   
      
    public static int findFlow(int basepos, String sequence, String flowOrder) {
        SeqFlow    sf = seqToFlowPlus(sequence, flowOrder, 0, true);
        int flow = findLocationFlow(basepos, sf);
        return flow;
    }
    /** given a sequence with key flow at the beginning, returns the flow positions for each base call */
    private static SeqFlow seqToFlowPlus(String sequence, String flowOrder,int nFlow, boolean finishAtSeqEnd) {
        int lBases = sequence.length();
        if (nFlow <=0 ) nFlow = flowOrder.length();
     //   p("Mapping "+sequence+" to flow order: "+flowOrder);
        int[] out = new int[nFlow];
        int[] loca =  new int[sequence.length()];
        int seqpos = 0;
        for (int fIndex = 0; fIndex < nFlow; fIndex++) {
            while (seqpos < lBases && sequence.charAt(seqpos) == flowOrder.charAt(fIndex)) {
                loca[seqpos] = fIndex;
           //     p("Mapped "+sequence.charAt(seqpos)+" to "+flowOrder.charAt(fIndex)+" at flow="+fIndex+", out[fFindex]="+out[fIndex]+", seqpos="+seqpos);
                out[fIndex]++;
                seqpos++;
            }
            
        }
        if (finishAtSeqEnd) {
            // find last 0
            int lastzero = out.length;
            for (int i = out.length-1; i >-0; i--) {
                if (out[i] == 0) lastzero = i;
            }
            if (lastzero < out.length) {
                System.arraycopy(out, 0, out, 0, lastzero);
            }
        }
      SeqFlow res = new SeqFlow();
      res.out = out;
      res.loca = loca;
      
   //   p("loca:"+Arrays.toString(loca));
      
      return res;
    }
    public static class SeqFlow {
        int[] out;
        int[] loca;
      
        public int length() {
            return out.length;
        }
    }
    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(SeqFlowUtil.class.getName()).log(Level.SEVERE, msg, ex);
    }
    
    private void err(String msg) {
        
        Logger.getLogger(SeqFlowUtil.class.getName()).log(Level.SEVERE, msg);
    }
    
    private static void warn(String msg) {
        Logger.getLogger(SeqFlowUtil.class.getName()).log(Level.WARNING, msg);
    }
    
    private static void p(String msg) {
        System.out.println("SeqFlowUtil: " + msg);
        //Logger.getLogger( SeqFlowUtil.class.getName()).log(Level.INFO, msg);
    }
}
