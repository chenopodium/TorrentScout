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
public class IonogramChartPanel extends JPanel {

    private Ionogram ionogram;
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
    private DecimalFormat f = new DecimalFormat("0.00");
    private BasicStroke dotted = new BasicStroke(
            1f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND,
            1f,
            new float[]{4f},
            0f);

    public IonogramChartPanel(Ionogram ionogram, boolean raw, boolean norm) {
        //  setLayout(new BorderLayout());
        this.raw = raw;
        this.norm = norm;
        colors = load();

        update(ionogram);
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
        return new Dimension(ionogram.nrFlows() * 15, 150);
    }

    private void update(Ionogram ionogram) {
        this.ionogram = ionogram;

        this.coord = ionogram.getContext().getAbsoluteCoordinate();

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


        int y0 = height - BORDER;
        int x0 = BORDER;

        gg.setStroke(line);
        g.setColor(background);
        g.fillRect(x0, TOP, w, h);
        g.setColor(Color.black);
        g.drawRect(x0, TOP, w, h);
        String seq = ionogram.getSequence();
        // draw graphics rectangle
        int flows = ionogram.nrFlows();
        float dx = (float) w / (float) flows;

        // find max value
        float max = ionogram.getMax(raw);
        max = max + 0.5f;
        p("max: " + max);
        max = Math.min(max, global_max);
        float dy = (float) h / max;

        g.setColor(Color.black);
        g.setFont(titleFont);
        String s = "";
        if (ionogram.getExpContext() != null) {
            s += " of " + ionogram.getExpContext().getResultsName();
        }
        float values[] = ionogram.getValues(raw);
        if (values == null) {
//            raw= !raw;
//            norm = !norm;
//            values =ionogram.getValues(raw);
//        }
//        
//        if (values == null) {
//            p("Got no flow values!");
            return;
        }
        if (norm && raw) {
            g.drawString("Ionogram from sff and 1.wells file " + coord + s, x0 + 5, 20);
        } else if (norm) {
            g.drawString("Ionogram from sff file at " + coord + s, x0 + 5, 20);
        } else {
            g.drawString("Raw Ionogram from 1.wells file at " + coord + s, x0 + 5, 20);
        }



        g.setFont(labelFont);
        g.setColor(Color.black);

        for (int i = 0; i < max; i++) {
            int y = (int) (y0 - i * dy);
            gg.setColor(cline);
            gg.setStroke(dotted);
            g.drawLine(x0, y, x0 + w, y);
            g.drawString("" + i, x0 - 18, y + 2);
        }
        for (float i = 0.0f; i < max; i += 0.5f) {
            int y = (int) (y0 - i * dy);
            gg.setColor(Color.black);
            gg.setStroke(line);
            g.drawLine(x0, y, x0 + 2, y);           
        }

        g.setFont(medFont);
        g.setColor(Color.black);
        g.drawString("PPF: " + f.format(ionogram.computePpf(raw)), x0 + 5, BORDER + 25);
        g.drawString("SSQ: " + f.format(ionogram.computeSSQ(raw)), x0 + 5, BORDER + 40);


        for (int i = 0; i < ionogram.nrFlows(); i++) {
            g.setColor(Color.black);
            int x = (int) (i * dx) + x0;
            int mx = x + (int) (dx / 2);
            if (i % 4 == 0) {
                gg.setStroke(dotted);
                gg.setColor(cline);
                g.drawLine(x, y0, x, y0 - h);
                gg.setStroke(line);
                gg.setColor(Color.black);
            } else {
                g.drawLine(mx, y0, mx, y0 + 2);
            }
            if (i % 20 == 0) {
                gg.setStroke(line);
                gg.setColor(Color.black);
                g.drawLine(x, y0, x, y0 - h);

            }

            char base = seq.charAt(i % seq.length());

            if (i % 4 == 0) {
                g.setFont(labelFont);
                g.drawString("" + i, mx - 4, y0 + 12);
            }
            float value = values[i];
            int y = y0 - (int) (value * dy);

            Color color = colors[GATC.indexOf(base)];


            if (showBars) {
                g.setColor(color);
                g.fill3DRect(mx - 2, y, 3, (int) (value * dy), true);
            } else {
                g.setColor(cline.brighter());
                g.drawLine(mx, y, mx, y0);
            }
            g.setColor(color);
            g.setFont(gatcFont);
            g.drawString("" + base, mx - 4, Math.max(y - 2, 10));

        }


    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(IonogramChartPanel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(IonogramChartPanel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(IonogramChartPanel.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
//  System.out.println("IonogramChartPanel: " + msg);
        //Logger.getLogger( IonogramChartPanel.class.getName()).log(Level.INFO, msg, ex);
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
}
