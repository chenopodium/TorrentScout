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
package com.iontorrent.torrentscout.explorer.automate;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;

import com.iontorrent.torrentscout.explorer.Export;
import com.iontorrent.utils.io.FileTools;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellFlowDataResult;
import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import org.jfree.chart.JFreeChart;

/**
 *
 * @author Chantal Roth
 */
public class MultiFlowPanel extends JPanel {

    private MultiFlowChartPanel chart;
    private RawType filetype;
    private String info;
    private WellContext context;
    private WellCoordinate coord;
    private String subtitle;
    private ArrayList<Integer> flows;
    private String region;
    ArrayList<WellFlowDataResult> results;

    public MultiFlowPanel(RawType filetype) {
        super(false);
        this.filetype = filetype;
        setLayout(new BorderLayout());
        chart = new MultiFlowChartPanel();
        results = new ArrayList<WellFlowDataResult>();
    }

    public MultiFlowChartPanel getChartPanel() {
        return chart;
    }

    public String getInfo() {
        return info;
    }
//    public boolean hasResult(ResultType type) {
//        return results.containsKey(type);
//    }

    public void setResults(ArrayList<WellFlowDataResult> reslist) {
        if (reslist == null) {
            p("Addresult, no data in array");
            return;
        }
        results = reslist;
    }

    public String toCSV() {
        if (chart == null) {
            return "";
        }
        return chart.toCSV();
    }

    public String update(String region, ExperimentContext exp) {
        return update(region, null, exp);
    }

    public String update(String region, String subtitle, ExperimentContext exp) {
        if (exp == null) {
            return "No experiment context";
        }
        context = exp.getWellContext();
        coord = context.getCoordinate();
//        if (getCoord() == null) {
//            return "I have no coordinates to show";
//        }
//        if (context == null) {
//            return "I got no well context";
//        }

        this.subtitle = subtitle;
        extractInfo(exp);
        this.region = region;
        if (results != null && results.size() > 0) {
            update();
        }
        return null;

    }

    public String update() {
        if (results == null || results.size() < 0) {
            String s = "I got well flow result no data for well " + getCoord() + ", data is probably still being loaded";
            if (filetype == null) {
                s += "<br>because no file type was chosen";
                return s;
            }
            return s;
        }

        if (chart != null) {
            remove(chart);
        }
        chart.update(region, subtitle, context, results);
        add("Center", chart);

        chart.invalidate();
        chart.revalidate();
        chart.paintImmediately(0, 0, 800, 800);
        return null;
    }

    public void addResult(WellFlowDataResult res) {
        results.add(res);
    }

    public void setYaxis(String name) {
        if (chart != null) {
            chart.setYaxis(name);
        }
    }
     public boolean export() {
        if (chart == null) {
            GuiUtils.showNonModalMsg("Got no image to export yet...");
            return false;
        }
        String file = Export.getFile("Save image to a file", "*.*", true);
        return export(file);
    }

    public boolean export(String file) {
        if (file == null || file.length() < 3) {
            GuiUtils.showNonModalMsg("I need to know if it is a .png or a .jpg file");
            return false;
        }
        if (chart == null) {
            return false;
        }
        File f = new File(file);
        String ext = file.substring(file.length() - 3);
        RenderedImage image = myCreateImage();
        try {
            return ImageIO.write(image, ext, f);
        } catch (IOException ex) {
            err("Could not write image to file " + f, ex);
        }
        return false;
    }

    public RenderedImage myCreateImage() {
        int width =  chart.getWidth();
        int height = chart.getHeight();

       // setSize(Math.max(w, getWidth()), Math.max(h, getHeight()));
        // Create a buffered image in which to draw
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics contents on the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();

        // Draw graphics
        chart.paint(g2d);

        return bufferedImage;
    }

    private void extractInfo(ExperimentContext expContext) {
        //String seq = context.getFlowSequence();
        //  seq = StringTools.addNL(seq, "<br>", WIDTH);
        info = "";// "Flow sequence:<br>"+seq;
        if (expContext != null) {
            // String seq = expContext.getFlowOrder();
            // seq = StringTools.addNL(seq, "<br>",WIDTH);
            info = "Library key: " + expContext.getLibraryKey() + "<br>";

        }
//        if (data == null) {
//            info += "<br>Well data is null: ";
//        }
//        else if (data.getMask() == null) {
//            info += "<br>Well mask info is null ";
//        }
//        else {
//            String mask = data.getMask().toString();
//            info += StringTools.addNL(mask, "<br>", WIDTH);
//        }


    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(MultiFlowPanel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(MultiFlowPanel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(MultiFlowPanel.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("MultiFlowPanel: " + msg);
        Logger.getLogger(MultiFlowPanel.class.getName()).log(Level.INFO, msg);
    }

    /**
     * @return the coord
     */
    public WellCoordinate getCoord() {
        return coord;
    }

    /**
     * @return the flow
     */
    public ArrayList<Integer> getFlows() {
        return flows;
    }

    public void clear() {
        results = new ArrayList<WellFlowDataResult>();

    }

    public JFreeChart getChart() {
        return chart.getChart();
    }
}
