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

import com.iontorrent.utils.stats.Stats;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellFlowDataResult;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

/**
 *
 * @author Chantal Roth
 */
public class MultiFlowChartPanel extends JPanel {

    //  private File file;
    WellCoordinate coord;
    private String baselist;
    private String flowlist;
    ArrayList<WellFlowDataResult> results;
    WellContext context;
    String subtitle;
    ChartPanel chartPanel;
    Stats stats;
    private String region;
    Stats nnstats;
    Stats dstats;
    private String yaxis;
    int startflow;
    boolean showRawSignal;
    XYSeriesCollection dataset;
    JFreeChart chart;

    public MultiFlowChartPanel() {
        super(false);
        setLayout(new BorderLayout());
        yaxis = "count";
        //setPreferredSize(new Dimension(500, 500));
    }

    public Stats getStats() {
        return stats;
    }

    public Stats getDStats() {
        return dstats;
    }

    public Stats getNNStats() {
        return nnstats;
    }

    public void update(String region, String subtitle, WellContext context, ArrayList<WellFlowDataResult> results) {
        this.context = context;
        this.region = region;
        this.subtitle = subtitle;
        //  this.file = file;
        this.results = results;
        this.coord = context.getCoordinate();

        if (chartPanel != null) {
            remove(chartPanel);
        }


        if (context == null) {
            err("NO context!");
            return;
        }

        if (results == null || results.size() < 1) {
            p("No results, no data");
            return;
        }

        dataset = null;
        dataset = createDataset();
        chart = createChart(dataset);

        //   p("Got new dataset: flows=" + flows);
        chartPanel = new ChartPanel(chart, false);

        add("Center", chartPanel);
        //    chart.fireChartChanged();
        dataset.addChangeListener(chart.getPlot());
        //   chart.getPlot().datasetChanged(new DatasetChangeEvent(this, dataset));
        //   chart.setNotify(true);
        chartPanel.chartChanged(new ChartChangeEvent(dataset, chart));
        //  chartPanel.updateUI();
        chartPanel.repaint();

    }

    public String toCSV() {
        if (dataset == null) {
            return "";
        }
        String out = this.context.getExpContext().getRawDir() + "\n" + region + "\n";
        out += "\ntime, median value\n";
        int nr = dataset.getSeriesCount();
        
        for (int s = 0; s < nr; s++) {
            for (int f = 0; f < dataset.getItemCount(s); f++) {
                double x = dataset.getXValue(s, f);
                out += x + ",";
                out += dataset.getY(s, f)+", "+dataset.getSeriesKey(s);
                out += "\n";

            }
        }
        return out;
    }

    private XYSeriesCollection createDataset() {
        dataset = new XYSeriesCollection();
        // for (int series = 0; series < res.length; series++) {
// one for each frame
        baselist = "";
        flowlist = "";
        for (WellFlowDataResult dataresult : results) {
            String base = "";
            int flow = dataresult.getFlow();

            flowlist += flow + " ";
            if (dataresult.isPrerun()) {
                baselist = " prerun";
            } else if (dataresult.isBfPost()) {
                baselist = " bf post ";
            } else if (dataresult.isBfPre()) {
                baselist = " bf pre";
            }
            if (dataresult.isAcq()) {
                base = "" + context.getBase(flow);
                baselist += base;
            }

            //     p("showing result " + dataresult);
            double[] nframedata = dataresult.getData();
            double[] ndata = new double[nframedata.length];

            String serieskey = dataresult.getName() + " flow " + dataresult.getFlow();
            if (base != null) {
                serieskey += " " + base;
            }
            XYSeries seriesres = new XYSeries(serieskey);
            seriesres.setKey(serieskey);
            for (int f = 0; f < nframedata.length; f++) {
                long ts = dataresult.getTimestamps()[f];
                ts += dataresult.getStarttime();
                ndata[f] = nframedata[f] - nframedata[0];
                seriesres.add(ts, ndata[f]);
            }
            dataset.addSeries(seriesres);
        }

        //  else p("Not showing "+key.getName());
        //   }

        return dataset;
    }

    /**
     * Creates a chart.
     * 
     * @param dataset
     *            the data for the chart.
     * 
     * @return a chart.
     */
    private JFreeChart createChart(XYDataset dataset) {
        // create the chart...



        JFreeChart chart = ChartFactory.createXYLineChart(region + ", flows " + flowlist + " " + baselist, // chart
                // title
                "timestamp", getYaxis(), // y axis label
                dataset, // data
                PlotOrientation.VERTICAL, true, // include legend
                true, // tooltips
                false // urls
                );

        if (subtitle != null) {
            final TextTitle s = new TextTitle(subtitle);
            s.setFont(new Font("SansSerif", Font.PLAIN, 12));
            s.setPosition(RectangleEdge.TOP);
//        subtitle.setSpacer(new Spacer(Spacer.RELATIVE, 0.05, 0.05, 0.05, 0.05));
            //  s.setVerticalAlignment(VerticalAlignment.TOP);
            chart.addSubtitle(s);
        }
        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        // chart.setBackgroundPaint(Color.white);
        chart.setBackgroundImageAlpha(1.0f);
        //chart.addSubtitle(new TextTitle(file.getName()));
        // get a reference to the plot for further customisation...
        XYPlot plot = (XYPlot) chart.getPlot();

        plot.setBackgroundPaint(Color.black);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.gray);
        plot.setRangeGridlinePaint(Color.gray);
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setShapesVisible(false);
        renderer.setShapesFilled(false);

        // change the auto tick unit selection to integer units only...
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        ValueAxis va = plot.getDomainAxis();
        va.setAutoRangeMinimumSize(1000);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        // OPTIONAL CUSTOMISATION COMPLETED.
        return chart;


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
        System.out.println("MultiFlowChartPanel: " + msg);
        //Logger.getLogger( MultiFlowChartPanel.class.getName()).log(Level.INFO, msg, ex);
    }

    JFreeChart getChart() {
        return chart;
    }

    /**
     * @return the yaxis
     */
    public String getYaxis() {
        return yaxis;
    }

    /**
     * @param yaxis the yaxis to set
     */
    public void setYaxis(String yaxis) {
        this.yaxis = yaxis;
    }
}
