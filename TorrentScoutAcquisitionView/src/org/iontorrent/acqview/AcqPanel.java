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
package org.iontorrent.acqview;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.utils.StringTools;
import com.iontorrent.utils.stats.Stats;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellFlowData;
import com.iontorrent.wellmodel.WellFlowDataResult;
import com.iontorrent.wellmodel.WellFlowDataResult.ResultType;
import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.jfree.chart.JFreeChart;

/**
 *
 * @author Chantal Roth
 */
public class AcqPanel extends JPanel {

    private AcqChartPanel chart;
    private RawType filetype;
    private String info;
    private int WIDTH = 40;
//    private File file;
 //   private RasterIO io;
    private WellCoordinate coord;
    private int flow;
    private int empty;
    
    private EnumMap<ResultType, WellFlowDataResult> results; 
    
    
    public AcqPanel(RawType filetype) {
        super(false);
        this.filetype = filetype;
        setLayout(new BorderLayout());
        chart = new AcqChartPanel();
        results = new EnumMap<ResultType, WellFlowDataResult>(ResultType.class);
        // chart.setPreferredSize(new Dimension(400,400));
        //  setPreferredSize(new Dimension(400,400));
    }

    public EnumMap<ResultType, WellFlowDataResult> getResultsMap() {
        return results;
    }
    public String getInfo() {
        return info;
    }
//    public boolean hasResult(ResultType type) {
//        return results.containsKey(type);
//    }
    public void addResults(ArrayList<WellFlowDataResult> results) {
        if (results == null) return;
        for (WellFlowDataResult res: results) {
            addResult(res);
        }
    }
    public void addResult(WellFlowDataResult result) {
    //    p("Adding result "+result+":"+result.getName());
        results.put(result.getResultType(), result);
    }
    public String toCSV() {
        if (chart == null) return "";
        return chart.toCSV();
    }
    public String update(String region, ExperimentContext expContext, WellFlowData data, WellFlowDataResult nndata, WellContext context, int flow, int nrempty, boolean showRawSignal) {
        this.empty = nrempty;
        coord = context.getCoordinate();
        if (getCoord() == null) {
            return "I have no coordinates to show";
        }
        if (context == null) {
            return "I got no well context - select a bmfmask.bin file first";
        }
        this.flow = flow;
        
        if (data == null) {
            String s = "I got no well flow data for well " + getCoord()+", data is probably still being cached";
            int secs = context.esimateSecs();
            s+="<br>It takes about "+secs+" secs to cache one flow per area of 1024 x 1024 wells";
            if (filetype == null) {
                s += "<br>because no file type was chosen";
                return s;
            }
            return s;
        }

        if (chart != null) {
            remove(chart);
        }
        if (nndata != null) addResult(nndata);
        chart.update(region, context, data, results, flow, showRawSignal);

        add("Center", chart);
        extractInfo(context, expContext, data);

       // this.invalidate();
       // this.revalidate();
       // this.paintImmediately(0, 0, 800, 800);
        chart.invalidate();
        chart.revalidate();
        chart.paintImmediately(0, 0, 800, 800);
        return null;
    }
 public RenderedImage myCreateImage(int w, int h) {
        int width =Math.max(w,chart.getWidth());
        int height = Math.max(h, chart.getHeight());

        setSize(Math.max(w, getWidth()), Math.max(h, getHeight()));
        // Create a buffered image in which to draw
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics contents on the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();

        // Draw graphics
        paintComponent(g2d);

        return bufferedImage;
    }
    private void extractInfo(WellContext context, ExperimentContext expContext, WellFlowData data) {
        //String seq = context.getFlowSequence();
        //  seq = StringTools.addNL(seq, "<br>", WIDTH);
        info = "";// "Flow sequence:<br>"+seq;
        if (expContext != null) {
            // String seq = expContext.getFlowOrder();
            // seq = StringTools.addNL(seq, "<br>",WIDTH);
            info = "Library key: " + expContext.getLibraryKey() + "<br>";

        }
        if (data == null) {
            info += "<br>Well data is null: ";
        }
        else if (data.getMask() == null) {
            info += "<br>Well mask info is null ";
        }
        else {
            String mask = data.getMask().toString();
            info += StringTools.addNL(mask, "<br>", WIDTH);
        }
        

    }
    public int getNrEmpty() {
        return empty;
    }

    public Stats getStats() {
        return chart.getStats();
    }

    public Stats getNNStats() {
        return chart.getNNStats();
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(AcqPanel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(AcqPanel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(AcqPanel.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("AcqPanel: " + msg);
        Logger.getLogger( AcqPanel.class.getName()).log(Level.INFO, msg);
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
    public int getFlow() {
        return flow;
    }

    Stats getDStats() {
        return chart.getDStats();
    }

    void clear() {
        results.clear();
    }

    public JFreeChart getChart() {
       return chart.getChart();
    }
}
