/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.rawdataaccess.transformation;

import com.iontorrent.utils.StringTools;
import com.iontorrent.utils.io.FileTools;
import com.iontorrent.utils.io.FileUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class ChannelXTCorrectionDescriptor {

    public static String FILENAME = "cross_talk_vectors.txt";
    private final static int DEFAULT_VECT_LEN = 7;
    static final double chan_xt_vect_316[] = {0.0029, -0.0632, -0.0511, 1.1114, 0.0000, 0.0000, 0.0000};
    static final double default_316_xt_vectors[][] = {chan_xt_vect_316};
    static final double chan_xt_vect_318_even[] = {0.0132, -0.1511, -0.0131, 1.1076, 0.0404, 0.0013, 0.0018};
    static final double chan_xt_vect_318_odd[] = {0.0356, -0.1787, -0.1732, 1.3311, -0.0085, -0.0066, 0.0001};
    static final double default_318_xt_vectors[][] = {
        chan_xt_vect_318_even,
        chan_xt_vect_318_even,
        chan_xt_vect_318_even,
        chan_xt_vect_318_even,
        chan_xt_vect_318_odd,
        chan_xt_vect_318_odd,
        chan_xt_vect_318_odd,
        chan_xt_vect_318_odd};
    private static final int chan_xt_column_offset[] = {-12, -8, -4, 0, 4, 8, 12};
    public static ChannelXTCorrectionDescriptor Default_316 = new ChannelXTCorrectionDescriptor("316", default_316_xt_vectors, 1, DEFAULT_VECT_LEN, chan_xt_column_offset);
    public static ChannelXTCorrectionDescriptor Default_318 = new ChannelXTCorrectionDescriptor("318", default_318_xt_vectors, 8, DEFAULT_VECT_LEN, chan_xt_column_offset);
    public static ChannelXTCorrectionDescriptor Default_Unknown = new ChannelXTCorrectionDescriptor("UNKNOWN", null, 0, 0, null);
    /**
     *  float **xt_vector_ptrs; // array of pointers to cross-talk correction vectors
    int num_vectors;        // number of vectors in xt_vector_ptrs
    int vector_len;         // length of each correction vector
    int *vector_indicies;   // relative indices for the application of each vector
     */
    private String chip_type;
    private double[][] xt_vectors;// array of pointers to cross-talk correction vectors
    private int num_vectors;        // number of vectors in xt_vector_ptrs
    private int vector_len;         // length of each correction vector
    private int[] vector_indices;   // relative indices for the application of each vector
    boolean hasFile;

    ChannelXTCorrectionDescriptor(String chip_type, double[][] xt_vectors, int num_vectors, int vector_len, int[] vector_indices) {
        this.chip_type = chip_type;
        this.xt_vectors = xt_vectors;
        this.num_vectors = num_vectors;
        this.vector_len = vector_len;
        this.vector_indices = vector_indices;

    }

    ChannelXTCorrectionDescriptor(String raw_dir) {
        parseFile(raw_dir + FILENAME);
    }

    public boolean hasFile() {
        return hasFile;
    }

    @Override
    public String toString() {
        String s = "num_vectors="+this.num_vectors+", vector_len="+this.vector_len+"\n";
        s+="offsets = [";
        for (int i = 0; i < vector_len; i++) {
            s+=vector_indices[i];
            if (i + 1 <vector_len) s+= ", ";
        }
        s = s.trim()+"]\n";
        
       for (int v = 0; v < num_vectors; v++) {          
            s+="vector["+v+"] = [";
            double[] vec = this.xt_vectors[v];
            for (int i = 0; i < vec.length; i++) {
                s+= vec[i];
                if (i + 1 < vec.length) s+= ", ";
            }
            s = s.trim()+"]\n";
       }
       return s;
            
    }
    private void parseFile(String file) {
        hasFile = FileUtils.exists(file);
        if (!hasFile) {
            return;
        }
        p("Parsing file " + file);
        ArrayList<String> lines = FileTools.getFileAsArray(file);
        if (lines == null || lines.size() < 3) {
            warn("Could not parse " + file + ":" + lines + ": not enough lines");
            return;
        }
        // first line is 2 values: vector length and number of vectors
        String line = lines.get(0);
        if (line == null || line.length() < 1) {
            return;
        }
        line = line.trim();
        int tab = line.indexOf("\t");
        if (tab < 1) {
            p("no tab on first line found: "+line);
            return;
        }
        String first = line.substring(0, tab).trim();
        int vlen = getInt(first);

        String second = line.substring(tab).trim();
        int nvec = getInt(second);
        if (nvec < 1 || vlen < 1) {
            warn("Illegal values for vlen and nvec:" + line + ":" + vlen + "/" + nvec);
            return;
        }

        line = lines.get(1);
        if (line == null || line.length() < 1) {
            return;
        }
       // p("nvec="+nvec+", vlen="+vlen);
        ArrayList<Long> offsets = StringTools.parseListToLong(line, "\t");
        if (offsets == null || offsets.size() < vlen) {
            warn("Did not get enough offsets: " + offsets + ", should have " + vlen);
            return;
        }
        // check if there are enough lines
        if (lines.size() < nvec + 2) {
            warn("Expected :" + (nvec + 2) + " lines, but got only " + lines.size() + ":" + lines);
            return;
        }
        
        this.vector_indices = new int[nvec];
        this.xt_vectors = new double[nvec][vlen];

       for (int i = 0; i < vlen; i++) {
            vector_indices[i] = offsets.get(i).intValue();
        }
       for (int v = 0; v < nvec; v++) {
          
            ArrayList<Double> values = StringTools.parseListToDouble(lines.get(v + 2), "\t");
            if (values == null || values.size() < vlen) {
                if (values == null)  warn("Error parsing vector " + v + ":" + lines.get(v + 2)+", got NO values");
                else warn("Error parsing vector " + v + ":" + lines.get(v + 2)+", got "+values.size()+" instead of "+vlen+" values");
                xt_vectors = null;
                vector_indices = null;                
                return;
            }
            for (int i = 0; i < vlen; i++) {
                xt_vectors[v][i] = values.get(i).doubleValue();
            }
         //   p("Got vector "+v+":"+Arrays.toString(xt_vectors[v]));
        }
        this.num_vectors = nvec;
        this.vector_len = vlen;
      //  p("File "+file+" parsed fine: offsets="+Arrays.toString(vector_indices));
        // second line is the offsets, subsequent lines are the vectors 1 per line

    }

    private int getInt(String s) {
        int res = Integer.MIN_VALUE;
        if (s == null || s.length() < 1) {
            return res;
        }
        try {
            res = Integer.parseInt(s);
        } catch (Exception e) {
        }
        return res;
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(ChannelXTCorrectionDescriptor.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {

        Logger.getLogger(ChannelXTCorrectionDescriptor.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(ChannelXTCorrectionDescriptor.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        //System.out.println("ChannelXTCorrectionDescriptor: " + msg);
        Logger.getLogger( ChannelXTCorrectionDescriptor.class.getName()).log(Level.INFO, msg);
    }

    /**
     * @return the xt_vector_ptrs
     */
    public double[][] getXt_vectors() {
        return xt_vectors;
    }

    /**
     * @param xt_vector_ptrs the xt_vector_ptrs to set
     */
    public void setXt_vectors(double[][] xt_vectors) {
        this.xt_vectors = xt_vectors;
    }

    /**
     * @return the num_vectors
     */
    public int getNum_vectors() {
        return num_vectors;
    }

    /**
     * @param num_vectors the num_vectors to set
     */
    public void setNum_vectors(int num_vectors) {
        this.num_vectors = num_vectors;
    }

    /**
     * @return the vector_len
     */
    public int getVector_len() {
        return vector_len;
    }

    /**
     * @param vector_len the vector_len to set
     */
    public void setVector_len(int vector_len) {
        this.vector_len = vector_len;
    }

    /**
     * @return the vector_indices
     */
    public int[] getVector_indices() {
        return vector_indices;
    }

    /**
     * @param vector_indices the vector_indices to set
     */
    public void setVector_indices(int[] vector_indices) {
        this.vector_indices = vector_indices;
    }
}
