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

import com.iontorrent.rawdataaccess.wells.WellData;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author Chantal Roth
 */
public class IonogramPanel extends JPanel {

    private WellContext context;
    private ExperimentContext expContext;
    private IonogramChartPanel chart;
    private JScrollPane scroll;
    boolean raw;
    boolean norm;
    int maxy = 10;

    public IonogramPanel(ExperimentContext expContext) {
        setLayout(new BorderLayout());
        this.expContext = expContext;
    }

    private void updateChart(WellData welldata) {
        if (scroll != null) {
            remove(scroll);
        }
        if (welldata == null) {
            return;
        }

       
        //  if (welldata.getAverageFlowValue() > 0) {
        chart = new IonogramChartPanel(new Ionogram(welldata, context), raw, norm);
        chart.setMaxy(maxy);
        scroll = new JScrollPane(chart);
        String msg = "";
        String flow = context.getFlowSequence();

        if (flow != null && flow.length() > 3) {
            flow = flow.substring(0, 4);
            msg += "Flow order: " + flow + ". ";
        } else {
            msg = "Got no flow order. ";
        }

        if (welldata.getSequence() != null) {
            msg += "Sequence: " + welldata.getSequence();
        }
        if (expContext != null) {
            String xflow = expContext.getFlowOrder();
            if (xflow != null && xflow.length() > 3) {
                xflow = flow.substring(0, 4);
                msg += "Exp flow order: " + flow + ". ";
            }
            msg = "Flow order: " + xflow + ", Library key: " + expContext.getLibraryKey();
            if (xflow != null && flow != null && !xflow.equals(flow)) {
                msg += "<br><b><font color='AA0000'>Warning: flow sequence " + flow + " does not agree between 1.wells file and experiment info " + xflow + "from db</font></b>";

            }
            if (welldata.getMaskdata().isTestFrag()) {
                msg += "<br> Test frag sequence=" + expContext.getTfSequence();
            }

        } else {
            msg += "Got no info about experiment from db.";
        }
        add("South", new JLabel("<html>" + msg + "</html>"));
        add("Center", scroll);
//        } else {
//            JLabel lbl = new JLabel("No flow values found at well location " + context.getCoordinate());
//            lbl.setBackground(Color.white);
//            lbl.setForeground(Color.red.darker());
//
//            add("Center", lbl);
//        }
       
    }
    // Returns a generated image.

    public RenderedImage myCreateImage(int w, int h) {
        int width = Math.max(w, chart.getWidth());
        int height = Math.max(h, chart.getHeight());

        chart.setSize(Math.max(w, getWidth()), Math.max(h, getHeight()));
        // Create a buffered image in which to draw
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics contents on the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();

        // Draw graphics
        chart.paintComponent(g2d);

        return bufferedImage;
    }

    public void setWellContext(WellContext context, boolean raw, boolean norm) {
        this.context = context;

        this.raw = raw;
        this.norm = norm;
        if (scroll != null) {
            remove(scroll);
        }
       
        WellCoordinate coord = context.getCoordinate();
        if (coord == null) {
            err("Cannot read ionogram, got no coordinates. Context is: " + context, true);
            return;
        }
        WellData welldata = context.getWellData(coord);
        if (welldata == null || welldata.getSequence() == null) {
            err("got no welldata from WellContext at "+coord, true);
            return;
        }

     //   p("Got data for one well: " + welldata);

        if (coord.getCol() != welldata.getX() || coord.getRow() != welldata.getY()) {
            err("Coordinates do not match: " + welldata + " vs " + coord, true);
        }

        updateChart(welldata);

    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(IonogramPanel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg, boolean lbl) {
        if (lbl)  add("Center", new JLabel(msg));

        Logger.getLogger(IonogramPanel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(IonogramPanel.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
//  System.out.println("IonogramPanel: " + msg);
        //Logger.getLogger( IonogramPanel.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the expContext
     */
    public ExperimentContext getExpContext() {
        return expContext;
    }

    void setMaxY(int y_max) {
        this.maxy = y_max;
    }
}
