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
package com.iontorrent.ionogram;

import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellFlowDataResult;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author Chantal Roth
 */
public class MultiFlowChartPanel extends JPanel {

    private Ionogram ionogram;
    private WellFlowDataResult[] multiflow;
    private DefaultCategoryDataset dataset;
    //   private WellContext context;
    private WellCoordinate coord;
    private String chart_type;
    private Color cg;
    private Color ca;
    private Color ct;
    private Color cc;
    static Color colors[] = {Color.black, Color.green.darker(), Color.red.darker(), Color.blue.darker()};
    static String GATC = "GATC";
    private static final int BORDER = 20;
    private static final int TOP = 30;
    private Font titleFont = new Font("Helvetica", Font.BOLD, 16);
    private Font medFont = new Font("Helvetica", Font.BOLD, 12);
    private Font labelFont = new Font("Helvetica", Font.PLAIN, 10);
    private Font gatcFont = new Font("Helvetica", Font.BOLD, 12);
    private Color cline = Color.gray;
    private Color background = Color.white;
    private boolean showBars;
    // private boolean NORM;
    private int global_max = 8;
    private BasicStroke line = new BasicStroke(1);
    boolean raw;
    boolean norm;
    float values[];
    private DecimalFormat f = new DecimalFormat("0.00");
    private BasicStroke dotted = new BasicStroke(
            1f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND,
            1f,
            new float[]{4f},
            0f);
    private int mincount;
    private int maxcount;
    private int endframe;
    private int startframe;
    private double pixpercount;
    private int minflow;
    
    int flows;
    private int subtract;
    public MultiFlowChartPanel(Ionogram ionogram, boolean raw, boolean norm, WellFlowDataResult[] multiflow, int subtract, int minflow, int maxflow) {
        //  setLayout(new BorderLayout());
        this.raw = raw;
        this.minflow = minflow;
        this.norm = norm;
        this.endframe = 90;
        this.startframe = 0;
        this.subtract = subtract;
        colors = load();
                
        flows = ionogram.nrFlows();
        if (maxflow >0) {            
            flows = Math.max(maxflow, Math.min(flows, minflow+15));
            flows = Math.min(flows, maxflow);
        }
       
        update(ionogram, multiflow, subtract);
    }

    private Color[] load() {
        // TODO read settings and initialize GUI
        // Example:        
        // someCheckBox.setSelected(Preferences.userNodeForPackage(IonogramOptionsPanel.class).getBoolean("someFlag", false));
        // or for org.openide.util with API spec. version >= 7.4:
        // someCheckBox.setSelected(NbPreferences.forModule(IonogramOptionsPanel.class).getBoolean("someFlag", false));
        // or:
        // someTextField.setText(SomeSystemOption.getDefault().getSomeStringProperty());
        Preferences p = Preferences.userNodeForPackage(com.iontorrent.ionogram.options.IonogramOptionsPanel.class);
        p.addPreferenceChangeListener(new PreferenceChangeListener() {

            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                load();
            }
        });
        chart_type = p.get("chart_type", "standard bar chart");
        if (chart_type.startsWith("standard")) {
            showBars = true;
        } else {
            showBars = false;
        }
        String colT = p.get("color_T", "AA0000");
        String colA = p.get("color_A", "00AA00");
        String colC = p.get("color_C", "0000AA");
        String colG = p.get("color_G", "000000");
        cg = (GuiUtils.stringToColor(colG));
        ca = (GuiUtils.stringToColor(colA));
        ct = (GuiUtils.stringToColor(colT));
        cc = (GuiUtils.stringToColor(colC));
        Color colors[] = {cg, ca, ct, cc};
        return colors;

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension((flows-minflow) * 60, 150);
    }

    private void update(Ionogram ionogram, WellFlowDataResult[] multiflow, int subtract) {
        this.ionogram = ionogram;
        this.subtract = subtract;
        this.multiflow = multiflow;
        this.coord = ionogram.getContext().getAbsoluteCoordinate();
        
        values = ionogram.getValues(raw);
        if (values == null || (values[0] ==0 && values[1] == 0)) {
            raw = !raw;
            norm = !norm;
            values = ionogram.getValues(raw);
        }
       
        if (values == null) {
           p("Got values, neither raw nor norm");
        }
        repaint();
    }

    public void update(WellFlowDataResult[] multiflow, int subtract) {
        this.multiflow = multiflow;
         this.subtract = subtract;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D gg = (Graphics2D) g;
        int width = this.getWidth();
        int height = this.getHeight();
        int w = width - 2 * BORDER;
        int h = height - TOP - BORDER;


        int iono_y0 = height - BORDER - h / 2;
        int multi_y0 = height - BORDER;
        int x0 = BORDER;

        gg.setStroke(line);
        g.setColor(background);
        g.fillRect(x0, TOP, w, h);
        g.setColor(Color.black);
        g.drawRect(x0, TOP, w, h);
        String seq = ionogram.getSequence();
        // draw graphics rectangle
       
        float dx = (float) w / (float) (flows-minflow);

        // find max value
        float max = ionogram.getMax(raw);
        max = max + 0.5f;
        p("max y: " + max+", minflow="+minflow);
        max = Math.min(max, global_max);
        float iono_dy = (float) h / max / 2;

        g.setColor(Color.black);
        g.setFont(titleFont);

      
        g.drawString("Raw-NN and ionogram ("+minflow+"-"+flows+") @" + coord, x0 + 5, 20);

        g.setFont(labelFont);
        g.setColor(Color.black);
        gg.setStroke(line);
        g.drawLine(x0, iono_y0, x0 + w, iono_y0);
       
         for (int i = 0; i < max; i++) {
            int y = (int) (iono_y0 - i * iono_dy);
            gg.setColor(cline);
            gg.setStroke(dotted);
            g.drawLine(x0, y, x0 + w, y);
            g.drawString("" + i, x0 - 18, y + 2);
        }
        for (float i = 0.0f; i < max; i += 0.5f) {
            int y = (int) (iono_y0 - i * iono_dy);
            gg.setColor(Color.black);
            gg.setStroke(line);
            g.drawLine(x0, y, x0 + 2, y);           
        }

        g.setFont(medFont);
        g.setColor(Color.black);

        gg.setColor(cline);
        gg.setStroke(dotted);
        // draw line at ZERO
        // for each 100
        getMinMax();
        if (this.multiflow != null) {
            for (int v = mincount; v < maxcount; v++) {
                if (v % 50 == 0) {
                    gg.setStroke(line);
                    int ey = (int) (multi_y0 - (double) (v - mincount) * pixpercount);
                    g.drawLine(x0, (int) ey, x0 + w, (int) ey);
                    g.drawString("" + v, x0 - 18, ey + 2);
                } else if (v % 25 == 0) {
                    gg.setStroke(dotted);
                    int ey = (int) (multi_y0 - (double) (v - mincount) * pixpercount);
                    g.drawLine(x0, (int) ey, x0 + w, (int) ey);
                }
            }
        }
        for (int flownr = minflow; flownr < flows; flownr++) {
            g.setColor(Color.black);
            int x = (int) ((flownr-minflow) * dx) + x0;
            int mx = x + (int) (dx / 2);

            gg.setStroke(line);
            gg.setColor(Color.black);
            g.drawLine(x, multi_y0, x, multi_y0 - h);

            char base = seq.charAt(flownr % seq.length());

            // if (i % 4 == 0) {
            g.setFont(labelFont);
            g.drawString("" + flownr, mx - 4, multi_y0 + 12);
            //  }
            float value = 0;
            if (values != null) value = values[flownr];

            Color color = colors[GATC.indexOf(base)];

            // now draw mulit flow results!


            WellFlowDataResult flowres = this.multiflow[flownr];
            if (flowres != null) {
                int starttime = (int) flowres.getTimestamps()[startframe];
                int endtime = (int) flowres.getLastTimeStamp();
                if (endframe > startframe && endframe < flowres.getNrFrames()) {
                    endtime =  (int) flowres.getTimestamps()[endframe];
                }
                double pixpertime = (double) dx / ((double) endtime - (double) starttime);
                //==========================
                int startx = (int) ((flownr-minflow) * dx) + x0;
                double tx = startx;
                double ty = -1;
                double t0 = starttime;
                //  p("t0=" + t0 + ", mincount=" + mincount);
                // draw this time series


                g.setColor(color);
                for (int f = startframe; f < endframe && f < flowres.getNrFrames(); f++) {
                    float v = (float) (getValue(flowres, f) - mincount);
                    double ey = multi_y0 - (double) v * pixpercount;
                    double dt = flowres.getTimestamps()[f] - t0;
                    double ex = startx + (double) dt * pixpertime;
                    if (f > startframe) {
                        g.drawLine((int) tx, (int) ty, (int) ex, (int) ey);
                    }
                    tx = ex;
                    ty = ey;
                }
                // ========================= 
            }
            int iono_y = iono_y0 - (int) (value * iono_dy);
            if (showBars) {
                g.setColor(color);
                g.fill3DRect(mx - 2, iono_y, 3, (int) (value * iono_dy), true);
            } else {
                g.setColor(cline.brighter());
                g.drawLine(mx, iono_y, mx, iono_y0);
            }
            g.setColor(color);
            g.setFont(gatcFont);
            g.drawString("" + base, mx - 4, Math.max(iono_y - 2, 10));
        }
    }

    public void getMinMax() {
        maxcount = Integer.MIN_VALUE;
        mincount = Integer.MAX_VALUE;
        for (int i = minflow; i < this.multiflow.length; i++) {
            if (multiflow[i] != null) {
                getMinMax(multiflow[i]);
            }
        }
        if (mincount > maxcount) {
            //  p("Got no mincount, maxcount");
            mincount = -10;
            maxcount = 500;
        }
        double h = this.getHeight() - 2 * BORDER;
        double deltacount = Math.max(maxcount - mincount, 10);
        pixpercount = (double) (h-10) / (double) deltacount / 2.0;

    }
    public float getValue(WellFlowDataResult res, int f) {
        if (subtract < 0 || multiflow[subtract]==null){
            return (float) res.getData()[f];
        }
        else {
            return (float) (res.getData()[f] - multiflow[subtract].getData()[f]);
        }

    }
    public void getMinMax(WellFlowDataResult flowres) {

        for (int f = startframe; f < endframe && f < flowres.getNrFrames(); f++) {
            float v = getValue(flowres, f);
            if (v > maxcount) {
                maxcount = (int) v;
            }
            if (v < mincount) {
                mincount = (int) v;
            }
        }

    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(MultiFlowChartPanel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(MultiFlowChartPanel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(MultiFlowChartPanel.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
//  System.out.println("MultiFlowChartPanel: " + msg);
        Logger.getLogger( MultiFlowChartPanel.class.getName()).log(Level.INFO, msg);
    }

    /**
     * @return the global_max
     */
    public int getGlobal_max() {
        return global_max;
    }

    /**
     * @param global_max the global_max to set
     */
    public void setGlobal_max(int global_max) {
        this.global_max = global_max;
    }

    void setMaxy(int maxy) {
        this.global_max = maxy;
    }

    /**
     * @return the endframe
     */
    public int getEndframe() {
        return endframe;
    }

    /**
     * @param endframe the endframe to set
     */
    public void setEndframe(int endframe) {
        this.endframe = endframe;
    }

    /**
     * @return the startframe
     */
    public int getStartframe() {
        return startframe;
    }

    /**
     * @param startframe the startframe to set
     */
    public void setStartframe(int startframe) {
        this.startframe = startframe;
    }
}
