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

import com.iontorrent.utils.stats.Stats;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellFlowData;
import com.iontorrent.wellmodel.WellFlowDataResult;
import com.iontorrent.wellmodel.WellFlowDataResult.ResultType;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.EnumMap;
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
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 *
 * @author Chantal Roth
 */
public class AcqChartPanel extends JPanel {

    //  private File file;
    WellCoordinate coord;
    int flow;
    WellFlowData rawdata;
    EnumMap<ResultType, WellFlowDataResult> results;
    WellContext context;
    ChartPanel chartPanel;
    Stats stats;
    private String region;
    Stats nnstats;
    Stats dstats;
    int startflow;
    boolean showRawSignal;
    XYSeriesCollection dataset;
    JFreeChart chart;

    public AcqChartPanel() {
        super(false);
        setLayout(new BorderLayout());
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

    public void update(String region, WellContext context, WellFlowData wfdata, EnumMap<ResultType, WellFlowDataResult> results, int startflow, boolean showRawSignal) {
        this.context = context;
        this.region = region;
        //  this.file = file;
        this.results = results;
        this.startflow = startflow;
        this.rawdata = wfdata;
        this.showRawSignal = showRawSignal;
        this.coord = context.getCoordinate();

        this.flow = startflow;
        if (chartPanel != null) {
            remove(chartPanel);
        }
        if (wfdata == null) {
            err("NO framedata!");
            return;
        }

        if (context == null) {
            err("NO context!");
            return;
        }

        dataset = null;
        dataset = createDataset();
        chart = createChart(dataset);

       p("Got new dataset: flow=" + rawdata.getFlow() +", "+rawdata.getData()[0] +", "+rawdata.getData()[1] +", delta1=" + (rawdata.getData()[1] - rawdata.getData()[0]));
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
        String out = "Experiment name: "+this.context.getExpContext().getExperimentName() + "\nRaw data dir: "+this.context.getExpContext().getRawDir() + "\n" + region + "\n";
        out += "\ntime, ";
        int nr = dataset.getSeriesCount();
        for (int s = 0; s < nr; s++) {
            out += dataset.getSeriesKey(s);

            if (s + 1 < nr) {
                out += ", ";
            } else {
                out += "\n";
            }
        }
        for (int f = 0; f < dataset.getItemCount(0); f++) {
            double x = dataset.getXValue(0, f);
            out += x + ",";
            for (int s = 0; s < nr; s++) {
                out += dataset.getY(s, f);
                if (s + 1 < nr) {
                    out += ", ";
                } else {
                    out += "\n";
                }
            }
        }
        return out;
    }

    private XYSeriesCollection createDataset() {
        dataset = new XYSeriesCollection();
        // for (int series = 0; series < res.length; series++) {
// one for each frame
        
        double[] framedata = rawdata.getData();
        XYSeries seriesraw = new XYSeries("Raw data");
        double[] fdata = new double[framedata.length];
        for (int f = 0; f < framedata.length; f++) {
            long ts = rawdata.getTimestamps()[f];
            fdata[f] = framedata[f] - framedata[0];
            seriesraw.add(ts, fdata[f]);
        }
        stats = new Stats("WellFlowData");
        stats.computeStats(fdata);
        if (showRawSignal) {
            dataset.addSeries(seriesraw);
        }

        if (results != null) {

            for (ResultType key : results.keySet()) {
                //   p("got "+key.getName());
                if (key.isShow()) {
                    WellFlowDataResult dataresult = results.get(key);
                    //     p("showing result " + dataresult);
                    double[] nframedata = dataresult.getData();
                    double[] ndata = new double[nframedata.length];

                    XYSeries seriesres = new XYSeries(dataresult.getName());
                    seriesres.setKey(dataresult.getResultType());
                    for (int f = 0; f < nframedata.length; f++) {
                        long ts = dataresult.getTimestamps()[f];
                        ndata[f] = nframedata[f] - nframedata[0];
                        seriesres.add(ts, ndata[f]);
                    }
                    dataset.addSeries(seriesres);
                }
                //  else p("Not showing "+key.getName());
            }
        }


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

        String base = "";
        if (rawdata.isAcq()) {
            base = ", " + context.getBase(flow);
        } else if (rawdata.isPrerun()) {
            base = ", prerun";
        } else if (rawdata.isBfPost()) {
            base = ", bf post ";
        } else if (rawdata.isBfPre()) {
            base = ", bf pre";
        }
       // ChartFactory.
        JFreeChart chart = ChartFactory.createXYLineChart(region + ", flow " + flow + base, // chart
                // title
                "timestamp", // x axis label
                "count", // y axis label
                dataset, // data
                PlotOrientation.VERTICAL, true, // include legend
                true, // tooltips
                false // urls
                );
        
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
        Logger.getLogger(AcqChartPanel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(AcqChartPanel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(AcqChartPanel.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("AcqChartPanel: " + msg);
        //Logger.getLogger( AcqChartPanel.class.getName()).log(Level.INFO, msg, ex);
    }

    JFreeChart getChart() {
        return chart;
    }
}
