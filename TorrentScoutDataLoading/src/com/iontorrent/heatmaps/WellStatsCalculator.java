/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.heatmaps;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.sequenceloading.SequenceLoader;
import com.iontorrent.sff.SffRead;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class WellStatsCalculator {

    private static final double minSD = 0.01;
    private ExperimentContext exp;
    private SequenceLoader loader;
    private int nrKeyFlows;// such as 8
    private int[] keyVec; // suchas 0 1 0 0 1 0 1 1

    public WellStatsCalculator(ExperimentContext exp) {
        this.exp = exp;
        loader = SequenceLoader.getSequenceLoader(exp);
        // compute key Flows and keyvec!

        String key = exp.getLibraryKey(); // such as GATC
        String floworder = exp.getFlowOrder();// suchas CATGCATC
        if (key != null && key.length()>0 && floworder != null && floworder.length()>0) {
            int keypos = 0;
            // just some value large enough!
            keyVec = new int[20];
            p("lib key="+key+", floworder="+floworder);
            for (int keynr = 0; keynr < key.length(); keynr++) {
                char keybase = key.charAt(keynr);
                // such as G
                while (keybase != floworder.charAt(keypos)) {
                    keyVec[keypos] = 0;
                    keypos++;
                }
                // now we know that keybase = base in flow order
                keyVec[keypos] = 1;
                keypos++;
            }
            nrKeyFlows = keypos - 1;
            p("WellStatsCalculator: keyVec=" + Arrays.toString(keyVec) + ", nrKeyFlows=" + keypos);
        }
        else {
            p("Exp has no flow order or library key, not computing all heat maps");
        }
    }

 
    public double computeKeySNR(SffRead sff) {
        
        if (sff == null) return 0.0;
        
        int[] flowgram = sff.getFlowgram();
        if (flowgram == null) return 0.0;
        double measured[] = new double[flowgram.length];
        for (int i = 0; i < measured.length; i++) {
            measured[i] = (double) ((double)flowgram[i] / 100.0);
        }
        double snr = computeKeySNR(measured);
        return snr;
    }

    private double computeKeySNR(double[] measured) {
        int nZeroMer = 0;
        int nOneMer = 0;
        double zeroMer[] = new double[nrKeyFlows];
        double oneMer[] = new double[nrKeyFlows];
        for (int i = 0; i < nrKeyFlows; i++) {
            if (keyVec[i] == 0) {
                zeroMer[nZeroMer++] = measured[i];
            } else if (keyVec[i] == 1) {
                oneMer[nOneMer++] = measured[i];
            }
        }
        zeroMer = Arrays.copyOf(zeroMer, nZeroMer);
        oneMer = Arrays.copyOf(oneMer, nOneMer);
        double zeroMerSig = median(zeroMer);
        double zeroMerSD = Math.max(minSD, sd(zeroMer));

        double oneMerSig = median(oneMer);
        double oneMerSD = Math.max(minSD, sd(oneMer));

        double keySig = oneMerSig - zeroMerSig;
        double keySD = Math.sqrt(Math.pow(zeroMerSD, 2) + Math.pow(oneMerSD, 2));
        return keySig / keySD;
    }

    private double sd(double[] x) {
        int n = x.length;
        if (n <1) return 0;
        double mean = 0.0;
        for (int i = 0; i < n; i++) {
            mean += x[i];
        }
        mean /= n;

        double sd = 0;
        for (int i = 0; i < n; i++) {
            sd += Math.pow(x[i] - mean, 2);
        }
        sd /= (n - 1);

        return (Math.sqrt(sd));

    }

    public static double median(double[] values) {
        if (values == null || values.length<1) return 0;
        Arrays.sort(values);
        int n = values.length;
        if (n % 2 == 1) {
            return values[(n + 1) / 2 - 1];
        }
        else {
            double lower = values[n / 2 - 1];
            double upper = values[n / 2];
            return (double) ((lower + upper) / 2.0);
        }
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(WellStatsCalculator.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {

        Logger.getLogger(WellStatsCalculator.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(WellStatsCalculator.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("WellStatsCalculator: " + msg);
        Logger.getLogger( WellStatsCalculator.class.getName()).log(Level.INFO, msg);
    }
}
