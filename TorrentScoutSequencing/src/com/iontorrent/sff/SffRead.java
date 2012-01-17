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
package com.iontorrent.sff;

import com.iontorrent.utils.io.FileUtils;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *typedef struct {
uint16_t *flowgram;   the flowgram  
uint8_t *flow_index;   the 1-based flow index for each base called 
ion_string_t *bases;   the called bases 
ion_string_t *quality;   the quality score for each base call 
} sff_read_t;

 * @author Chantal Roth
 */
public class SffRead {

    SffGlobalFileHeader gheader;
    private SffReadHeader rheader;
    /**  uint16_t *flowgram;   the flowgram   */
    private int[] flowgram;
    /**  uint8_t *flow_index;   the 1-based flow index for each base called  */
    private int[] flow_index;
    /**  ion_string_t *bases;   the called bases  */
    private String bases;
    /**  ion_string_t *quality;   the quality score for each base call */
    private String quality;
    /** well coordinates */
    int row;
    int col;

    public SffRead(SffGlobalFileHeader gheader, SffReadHeader rheader) {
        this.gheader = gheader;
        this.rheader = rheader;
        col = -1;
        row = -1;

    }

    public String getFlowOrder() {
        return gheader.flow;
    }

    public String getKey() {
        return gheader.key;
    }

    protected int read(DataInputStream in) {
        try {
            int n = 0;
            flowgram = new int[(int) gheader.flow_length];
            flow_index = new int[(int) getRheader().n_bases];
            char[] basechars = new char[(int) getRheader().n_bases];
            char[] qchars = new char[(int) getRheader().n_bases];
            for (int i = 0; i < gheader.flow_length; i++) {
                flowgram[i] = FileUtils.getUInt16(in);
                n += 2;
            }
            for (int i = 0; i < getRheader().n_bases; i++) {
                flow_index[i] = FileUtils.getUInt8(in);
                n++;
            }
            for (int i = 0; i < getRheader().n_bases; i++) {
                basechars[i] = (char) in.readByte();
                n++;
            }
            for (int i = 0; i < getRheader().n_bases; i++) {
                qchars[i] = (char) in.readByte();
                n++;
            }
            bases = new String(basechars);
            quality = new String(qchars);

            n += FileUtils.readPadding(in, n);

            //  p("Got read:" + toString() + " read \n" + n + " bytes");
            return n;

        } catch (Exception ex) {
            warn("Could not read sff header, probably EOF");
        }
        return -1;

    }

    public float computePpf(float cutoff) {
        float values[] = getFlowValuesBetween0And1();
        if (values == null) {
            return -1;
        }
        float sum = 0;
        //int count = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] >= cutoff) {
                sum += values[i];
                //count++;
            }
        }
        return sum / values.length;
    }

    public float computePpf() {
        return computePpf(0.25f);
    }

    /** compute sum of fractional parts */
    public float computeSSQ() {
        float values[] = getFlowValuesBetween0And1();
        if (values == null) {
            return -1;
        }
        float sum = 0;

        for (int i = 0; i < values.length; i++) {
            double x = values[i] - Math.round(values[i]);
            sum += x * x;
        }
        return sum;
    }

    private float[] getFlowValuesBetween0And1() {
        if (flowgram == null) {
            return null;
        }
        float[] values = new float[flowgram.length];
        for (int i = 0; i < flowgram.length; i++) {
            values[i] = (float) (flowgram[i]) / 100.0f;
        }
        return values;
    }

    protected int read(RandomAccessFile in) {
        try {
            int n = 0;
            flowgram = new int[(int) gheader.flow_length];
            flow_index = new int[(int) getRheader().n_bases];
            char[] basechars = new char[(int) getRheader().n_bases];
            char[] qchars = new char[(int) getRheader().n_bases];
            for (int i = 0; i < gheader.flow_length; i++) {
                flowgram[i] = FileUtils.getUInt16(in);
                n += 2;
            }
            for (int i = 0; i < getRheader().n_bases; i++) {
                flow_index[i] = FileUtils.getUInt8(in);
                n++;
            }
            for (int i = 0; i < getRheader().n_bases; i++) {
                basechars[i] = (char) in.readByte();
                n++;
            }
            for (int i = 0; i < getRheader().n_bases; i++) {
                qchars[i] = (char) in.readByte();
                n++;
            }
            bases = new String(basechars);
            quality = new String(qchars);
            n += FileUtils.readPadding(in, n);

            //  p("Got read:" + toString() + " read \n" + n + " bytes");
            return n;

        } catch (Exception ex) {
            warn("Could not read sff header, probably eof ");
        }
        return -1;

    }

    public String getName() {
        return getRheader().name;
    }
    // Legacy read name format "IONPGM_XXXXX_YYYYY" where
    // YYYYY is ion_id_to_xy encoding of xy position

    public boolean isReadnameLegacy() {
        return getRheader().name.startsWith("IONPGM");
    }

    private int base36to10(String s) {
        int num = 0;
        int val;
        int len = s.length();
        int i = len;
        for (int pos = 0; pos < len; pos++) {
            char c = s.charAt(pos);
            if (c >= '0' && c <= '9') {
                val = 26 + c - '0';
            } else {
                val = c - 'a';
            }
            num += val * Math.pow(36.0, i - 1);
            i--;
        }
        p("Converting " + s + " from 36 to  base 10:" + num);
        return num;

    }

    public void extractXY() {
        extractXY(getName());
    }

    public void extractXY(String name) {
        name = name.toLowerCase();
        int num = base36to10(name);
        this.col = num / 4096;
        this.row = num % 4096;
        //   p("Converted " + name + " to col " + col + " and row " + row);
    }

    public boolean parseWellLocationFromName() {
        String name = getName();
        int len = name.length();
        if (isReadnameLegacy()) {
            // Legacy read name format "IONPGM_XXXXX_YYYYY" where
            // YYYYY is ion_id_to_xy encoding of xy position

            if (10 <= len) {
                extractXY(name.substring(len - 5));
                //  p("Got "+col+"/"+row+ " from name "+name);
                return true;
            } else {
                err("Could not read row/col from LEGACY name " + name);
                return false;
            }
        } else {
            /* states:
            0 - skipping over read name (before first colon)
            1 - reading in x value (before second colon)
            2 - reading in y value (after second colon)
             */
            int state = 0;
            int val = 0;
            for (int i = 0; i < len; i++) {
                char c = name.charAt(i);
                if (':' == c) {
                    if (1 == state) {
                        this.row = val;
                        //    p("Got row "+row+ " from name "+name);
                    }
                    state++;
                    val = 0;
                } else if ('0' <= c && c <= '9') {
                    val *= 10;
                    val += (int) (c - '0');
                }
            }
            if (2 == state) {
                this.col = val;
                //    p("Got "+col+"/"+row+ " from name "+name);
                return true;
            } else {
                err("Could not read row/col from name " + name);
                return false;
            }
        }
    }

    @Override
    public String toString() {
        // I know + is slow, but toString is hardly ever used! :-)    
        String s = rheader.toString() + "\nflowgram=" + Arrays.toString(getFlowgram()) + "\n";
        s += "flow index=" + Arrays.toString(getFlow_index()) + "\n";
        s += "bases=" + getBases() + "\n";
        s += "quality=" + getQuality() + "\n";
        s += "col=" + col + "\n";
        s += "row=" + row + "\n";
        return s;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(SffRead.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(SffRead.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(SffRead.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("SffRead: " + msg);
        //Logger.getLogger( SffRead.class.getName()).log(Level.INFO, msg, ex);
    }

    public int getRow() {
        if (row < 0) {
            parseWellLocationFromName();
        }
        return row;
    }

    public int getCol() {
        if (col < 0) {
            parseWellLocationFromName();
        }
        return col;
    }

    /**
     * @return the rheader
     */
    public SffReadHeader getRheader() {
        return rheader;
    }

    /**
     * @return the flowgram
     */
    public int[] getFlowgram() {
        return flowgram;
    }

    /**
     * @return the flow_index
     */
    public int[] getFlow_index() {
        return flow_index;
    }

    /** compute, for each base the 1-based flow index by adding all previous values */
    public int[] getAbsoluteFlowIndex() {
        int[] flownr = new int[flow_index.length];
        for (int i = 0; i < flownr.length; i++) {
            if (i == 0) {
                flownr[i] = flow_index[i];
            } else {
                flownr[i] = flownr[i - 1] + flow_index[i];
            }
        }
        return flownr;
    }

    /** return a string that prints out the flow gram values with the base from the flow order - with line breaks */
    public String getHtmlFlowInformation(int WINDOW_SIZE) {
        String res = "";
        int spacing = 5;
        String s[] = new String[2];
        s[0] = getSpacedFlowOrder(spacing).trim();
        s[1] = getSpacedFlowGram(spacing).trim();
        // G    T    A    C    G
        // 0    106  0    0    1020
        int len = s[1].length();
        int lines = len / WINDOW_SIZE + 1;
        p("nr lines: " + lines);
        p("s[0]=" + s[0]);
        p("s[1]=" + s[1]);
        for (int line = 0; line < lines; line++) {
            res += "<br>";
            for (int i = 0; i < s.length; i++) {
                len = s[i].length();
                int a = Math.min(line * WINDOW_SIZE, len);
                int b = Math.min(a + WINDOW_SIZE, len);
                if (b > a) {
                    String sub = s[i].substring(a, b);
                    for (int p = 0; p < sub.length(); p++) {
                        char c = sub.charAt(p);
                        if (c == ' ') {
                            res += "&nbsp;";
                        } else {
                            res += c;
                        }
                    }
                    res += "<br>";
                }
            }
        }

        return res.trim();
    }

    public String getSpacedFlowGram(int width) {
        int len = this.getFlowgram().length;
        StringBuffer res = new StringBuffer();
        for (int pos = 0; pos <= len * width; pos++) {
            res = res.append(" ");
        }
        for (int i = 0; i < len; i++) {
            int pos = i * width;
            String s = "" + this.getFlowgram()[i];
            res = res.replace(pos, pos + s.length() - 1, s);
        }
        return res.toString();
    }

    public String getSpacedFlowOrder(int width) {
        int len = this.getFlowOrder().length();
        StringBuffer res = new StringBuffer();
        for (int pos = 0; pos <= len * width; pos++) {
            res = res.append(" ");
        }

        for (int i = 0; i < len; i++) {
            int pos = i * width;
            res = res.replace(pos, pos, "" + getFlowOrder().charAt(i));
        }

        return res.toString();
    }

    /**
     * @return the bases
     */
    public String getBases() {
        return bases;
    }

    /**
     * @return the quality
     */
    public String getQuality() {
        return quality;
    }
}
