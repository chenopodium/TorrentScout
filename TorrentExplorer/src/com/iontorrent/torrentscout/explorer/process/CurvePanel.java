/*
 * Copyright (C) 2011 Life Technologies Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * HistoPanel.java
 *
 * Created on 01.11.2011, 08:43:38
 */
package com.iontorrent.torrentscout.explorer.process;

import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.torrentscout.explorer.ContextChangeAdapter;
import com.iontorrent.torrentscout.explorer.ExplorerContext;
import com.iontorrent.torrentscout.explorer.FrameWidget;
import com.iontorrent.torrentscout.explorer.Widget;
import com.iontorrent.utils.io.FileTools;
import com.iontorrent.wellmodel.RasterData;
import com.iontorrent.wellmodel.WellCoordinate;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author Chantal Roth
 */
public class CurvePanel extends javax.swing.JPanel {

    ExplorerContext maincont;
    private PlotFunction plotfunction;
    private int w;
    private int h;
    private int cutleftframe ;
    private int cutrightframe;
    private int BORDER = 50;
    private int x0;
    private int y0;
    private int width;
    private int height;
    private int starttime;
    private RasterData data;
    private double endtime;
    private float mincount;
    private float maxcount;
    private float deltacount;
    private String title;
    private double pixpertime;
    private double pixpercount;
    private ArrayList<Widget> coordwidgets;
    private int frames;
    private int flow;
    float[][] timeseries;
    int startframe;
    int endframe;
    int cropleft;
    int cropright;
    int mainframe;
    FrameWidget framewidget;
//    private Widget wstart;
//    private Widget wend;
//    private Widget wframe;
//    private Widget wcropleft;
//    private Widget wcropright;
    private ArrayList<Widget> fwidgets;
    FrameWidget curwidget;

    /** Creates new form HistoPanel */
    public CurvePanel(ExplorerContext maincont, String title) {
        // super(false);
      //  p("Creating curvepenal");
        initComponents();
        cutleftframe = 0;
        cutrightframe = 1000;
        this.title = title;
        setLayout(new FlowLayout());
        setBackground(Color.black);
        this.maincont = maincont;
        createFrameWidgets();


        maincont.addListener(new ContextChangeAdapter() {

            @Override
            public void coordChanged(WellCoordinate coord) {
                //   p("coord chanaged: ");
              //  paintImmediately(0, 0, 1000, 1000);
                repaint();
            }
             @Override
            public void frameChanged(int frame) {
                //   p("coord chanaged: ");
              //  paintImmediately(0, 0, 1000, 1000);
                 // change the frame of the yelloe frame widget
                 framewidget.setFrame(frame);
                repaint();
            }

            @Override
            public void widgetChanged(Widget w) {
                //    p("widget chnaged: " + w);
                // repaint();
              //  paintImmediately(0, 0, 1000, 1000);
                repaint();
            }
        });

        this.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                //   p("Mouse dragged");
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (curwidget != null) {
                        Point p = e.getPoint();
                        curwidget.setX((int) p.getX());
                        curwidget.setY((int) p.getY());
                        //   p("Got a widget, setting x.y");
                        getFrame(curwidget);
                        repaint();
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                 Point p = e.getPoint();
                 if (data == null) return;
                 int frame = getFrame((int)(p.getX()));
                 double time = data.getTimeStamp(flow, frame);
                 setToolTipText("Frame "+frame+" @ "+time);
            }
        });
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                double ex = evt.getX();
                double ey = evt.getY();
//                ex = ex - BORDER;
//                ey = maxy - ey;

                curwidget = (FrameWidget) Widget.getClosest(ex, ey, fwidgets);
                p("mouse clicked at " + ex + "/" + ey + ", finding widget: " + curwidget);

            }

            @Override
            public void mousePressed(MouseEvent evt) {

                double ex = evt.getX();
                double ey = evt.getY();
//                ex = ex - BORDER;
//                ey = maxy - ey;
                curwidget = (FrameWidget) Widget.getClosest(ex, ey, fwidgets);

                p("mouse pressed at " + ex + "/" + ey + ", finding widget: " + curwidget);

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (curwidget != null) {
                        int frame = getFrame(curwidget);
                        p("mouseReleased widget " + curwidget + ", then setting to null");
                        curwidget = null;
                        repaint();
                    }
                }
            }
        });
    }

    public void createFrameWidgets() {
        //rameWidget(Color color, int nr, int frame, int x, int y, int y0, int y1) {
        //  p("adding frame widgets");
        fwidgets = new ArrayList<Widget>();
        startframe = maincont.getStartframe();
        endframe = maincont.getEndframe();
        cropleft = maincont.getCropleft();
        cropright = maincont.getCropright();
        mainframe = maincont.getFrame();

        framewidget =new FrameWidget("main cursor", Color.yellow, 4, mainframe);
        fwidgets.add(new FrameWidget("start frame", Color.green, 0, startframe));
        fwidgets.add(new FrameWidget("end frame", Color.green, 1, endframe));
        fwidgets.add(new FrameWidget("left crop", Color.red.darker(), 2, cropleft));
        fwidgets.add(new FrameWidget("right crop", Color.red.darker(), 3, cropright));
        fwidgets.add(framewidget);
    }

    private void p(String string) {
        System.out.println("CurvePanel: " + string);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D gg = (Graphics2D) g;
        width = getWidth();
        height = getHeight();
        gg.clearRect(0, 0, width, height);
        gg.setColor(Color.black);
        gg.fillRect(0, 0, width, height);
        data = maincont.getData();

        coordwidgets = maincont.getWidgets();
        if (data == null || coordwidgets == null) {
            g.setColor(Color.white);
            g.drawString("No data yet (select a region by clicking in the whole chip/block view or by entering coordinates somewhere)", 10, BORDER);
            return;
        }
        w = width - 2 * BORDER;
        h = height - 2 * BORDER;
        x0 = BORDER;
        y0 = height - BORDER;

        
        endtime = (int) data.getEndTime(0);//;-data.getTimeStamp(flow, 0);
         frames = data.getFrames_per_flow();
        starttime = (int) data.getTimeStamp(0, 0);//;-data.getTimeStamp(flow, 0);
        if (cutleftframe > 0) starttime = (int) data.getTimeStamp(0, cutleftframe);
        if (cutrightframe < frames) endtime = (int) data.getTimeStamp(0, cutrightframe);
        pixpertime = (double) w / ((double) endtime - (double) starttime);
        //  p("cutleft="+cutleftframe+", cutright="+cutrightframe+", starttime="+starttime+", endtime: " + endtime + ", pixpertime: " + pixpertime);
       

        timeseries = new float[coordwidgets.size()][frames];
        flow = 0;
        
        g.setColor(Color.white);
        g.drawString(title, BORDER, 20);
        getMinMax();
        drawCoords(gg);
        drawChart(gg);
        drawPlotFunction(gg);
        drawWidgets(gg);

    }

    private void drawWidgets(Graphics2D gg) {

        //  p("drawing frame widgets");

       
        gg.setStroke(new BasicStroke(2));
        for (Widget w : fwidgets) {
            FrameWidget fw = (FrameWidget) w;
            int my = y0 - h / 2;
            fw.setX((int) getXForFrame(fw.getFrame()));
            fw.setY(my);
            fw.setY0(y0);
            fw.setY1(y0 - h+1);
            w.paint(gg, w.getX(), w.getY(), 1.0);
        }
       gg.setStroke(new BasicStroke(1));
    }

    public void getMinMax() {
        int t = 0;
        maxcount = Integer.MIN_VALUE;
        mincount = Integer.MAX_VALUE;
        for (Widget wid : coordwidgets) {
            CoordWidget cw = (CoordWidget) wid;
            WellCoordinate coord = cw.getCoord();
            if (coord != null) {
                // startcol is relative to urrent sub experiment!
                int c = coord.getCol() - data.getAbsStartCol();// - maincont.getExp().getColOffset();
                int r = coord.getRow() - data.getAbsStartRow();// - maincont.getExp().getRowOffset();
                if (c < 0 || r < 0 || c >= data.getRaster_size() || r >= data.getRaster_size()) {
             //       p("Coord out of bounds: "+c+"/"+r+", abs start coord: "+data.getAbsStartCol()+"/"+data.getAbsStartRow());
                } else {
                    timeseries[t] = data.getTimeSeries(c, r, flow);
                    //  p("Got time series: " + Arrays.toString(timeseries[t]));
                    for (int f = cutleftframe; f < frames && f < cutrightframe; f++) {
                        float v = timeseries[t][f];
                        if (v > maxcount) {
                            maxcount = v;
                        }
                        if (v < mincount) {
                            mincount = v;
                        }
                    }
                }
            }
            //   else p("Min max: no coord for "+wid);
            t++;
        }
        if (mincount > maxcount) {
          //  p("Got no mincount, maxcount");
            mincount = -100;
            maxcount = 1000;
        }
        deltacount = Math.max(maxcount - mincount, 10);
        pixpercount = (double) h / (double) deltacount;
        //   p("Curvepanel: pixpercount: " + pixpercount + ", deltacount=" + deltacount + " min/max:" + mincount + "/" + maxcount);
    }

    private void drawChart(Graphics2D g) {
        //  p("w/h:" + w + "/" + h);
        if (timeseries == null) {
            p("Got no time series");
            return;
        }
     //   p("Drawing "+timeseries.length+" timeseries, "+frames+" frames, cutleft="+cutleftframe+", cutright="+cutrightframe);
        for (int t = 0; t < timeseries.length; t++) {
            CoordWidget cw = (CoordWidget) coordwidgets.get(t);
            g.setColor(cw.getColor());
            if (cw.isMainWidget()) g.setStroke(new BasicStroke(3));
            // p("Drawing widget " + cw + ", color");
            g.drawString("" + cw.getCoord(), w - 50, t * 20 + BORDER + 20);
            double x = -1;
            double y = -1;
            double t0 = data.getTimeStamp(flow, cutleftframe);
          //  p("t0=" + t0 + ", mincount=" + mincount);
            // draw this time series
            for (int f = cutleftframe; f < frames && f < cutrightframe; f++) {
                float v = timeseries[t][f] - mincount;
                double ey = y0 - (double) v * pixpercount;
                double dt = data.getTimeStamp(flow, f) - t0;
                double ex = BORDER + (double) dt * pixpertime;
                if (f > cutleftframe) {
                    g.drawLine((int) x, (int) y, (int) ex, (int) ey);
                }
                x = ex;
                y = ey;
//                if (f < 2 && t == 0) {
//                    p("Got " + data.getTimeStamp(flow, f) + "/" + v + "=> x/y: " + x + "/" + y );
//                }
            }
            if (cw.isMainWidget()) g.setStroke(new BasicStroke(1));
        }
    }
 private void drawPlotFunction(Graphics2D g) {
        //  p("w/h:" + w + "/" + h);
        if (plotfunction == null) {
          //  p("Got no plot function");
            return;
        }
     //   p("Drawing  plotfunction "+plotfunction);
            g.setColor(Color.red);
            
            g.setStroke(new BasicStroke(3));
            // p("Drawing widget " + cw + ", color");
            double x = -1;
            double y = -1;
            double t0 = data.getTimeStamp(flow, cutleftframe);
          //  p("t0=" + t0 + ", mincount=" + mincount);
            // draw this time series
            for (int f = cutleftframe; f < frames && f < cutrightframe; f++) {
                double t =data.getDT(0,f);
                float v = (float) (plotfunction.compute(t) - mincount);
                double ey = y0 - (double) v * pixpercount;
                double dt = data.getTimeStamp(flow, f) - t0;
                double ex = BORDER + (double) dt * pixpertime;
                if (f > cutleftframe) {
                    
                    g.drawLine((int) x, (int) y, (int) ex, (int) ey);
                }
                x = ex;
                y = ey;
            }
    }

    private int getFrame(Widget w) {
        FrameWidget fw = (FrameWidget) w;
        int frame = getFrame(w.getX());
    //     p("Got frame: "+frame+" for "+w.getName()+"@ "+ w.getX());
        fw.setFrame(frame);
        if (w.getColor().equals(Color.yellow)) {
            // mainframe
            maincont.widgetChanged(w);
           // maincont.frameChanged(frame);
            maincont.setFrame(frame);
        }
        String n = w.getName();
        if (n.startsWith("start")) {
            maincont.setStartframe(frame);
            // to switch if necessary
            maincont.getStartframe();
        } else if (n.startsWith("end")) {
            maincont.setEndframe(frame);
            // to switch if necessary
            maincont.getEndframe();
            
            
        } else if (n.startsWith("left")) {
            maincont.setCropleft(frame);
        } else if (n.startsWith("right")) {
            maincont.setCropright(frame);
        }
        return frame;
    }

    private int getFrame(int x) {
        double dt = (x - BORDER) / pixpertime;
        double t = data.getTimeStamp(flow, cutleftframe) + dt;
        double diff = Double.MAX_VALUE;
        int frame = 0;
        for (int f = cutleftframe; f < frames && f < cutrightframe; f++) {
            double d = Math.abs(data.getTimeStamp(flow, f) - t);
            if (d < diff) {
                diff = d;
                frame = f;
            }
        }
        return frame;
    }

    private double getXForFrame(int f) {
        if (data == null) {
            return 0;
        }
        double dt = data.getTimeStamp(flow, f) - data.getTimeStamp(flow, cutleftframe);
        double x = BORDER + (double) dt * pixpertime;
        return x;
    }

    private void drawCoords(Graphics2D g) {
        g.setColor(Color.gray);
        g.drawRect(x0, y0 - h, w, h);
        int decimals = (int) Math.ceil(Math.log10(deltacount) - 0.5);
        // 10, 100, 1000 etc
        int stepy = Math.max(10, (int) Math.pow(10, decimals - 1));
     //   g.drawString("" + stepy, BORDER, 100);

        for (int county = 0; county < maxcount; county += stepy) {
            double y = y0 - (county - mincount) * pixpercount;
            g.setColor(Color.gray.darker());
            g.drawLine(x0, (int) y, x0 + w, (int) y);
            // g.setColor(Color.black);
            g.drawString("" + county, 5, (int) y);
        }
        for (int county = 0; county > mincount; county -= stepy) {
            double y = y0 - (county - mincount) * pixpercount;
            g.setColor(Color.gray.darker());
            g.drawLine(x0, (int) y, x0 + w, (int) y);
            // g.setColor(Color.black);
            g.drawString("" + county, 5, (int) y);
        }
        double t0 = data.getTimeStamp(flow, cutleftframe);
        for (int f = 0; f < frames && f< cutrightframe; f += 1) {
            double t = data.getTimeStamp(flow, f);
            double dt = t - t0;
            double x = (double) dt * pixpertime+this.BORDER;
            if (f % 5 == 0) {
                g.setColor(Color.gray.darker());
            }
            g.drawLine((int) x, (int) y0, (int) x, (int) y0 - h);
            if (f % 10 == 0) {
                g.drawString("" + (int) t, (int) x, y0 + 20);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolbar = new javax.swing.JToolBar();
        panHisto = new javax.swing.JPanel();

        //setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(HistoPanel.class, "HistoPanel.border.title"))); // NOI18N

        toolbar.setRollover(true);

        panHisto.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE).addComponent(panHisto, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(panHisto, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)));
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel panHisto;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables

    public boolean exportData(String file) {
        String csv = "";
        String header = "Time Series data for flow " + flow + ", type " + maincont.getFiletype() + ", experiment " + maincont.getExp().getRawDir();
        header += "\n\n, , Well Coordinates (column, row) ";
        header += "\nframe, time, ";
        String values = "";
        data = maincont.getData();
        coordwidgets = maincont.getWidgets();
        if (data == null) {
            return false;
        }
        if (coordwidgets == null) {
            return false;
        }
        int nr = coordwidgets.size();
        timeseries = new float[nr][data.getFrames_per_flow()];
        int nrt = 0;
        for (Widget wid : coordwidgets) {
            CoordWidget cw = (CoordWidget) wid;
            WellCoordinate coord = cw.getCoord();
            if (coord != null) {
                int c = coord.getCol() - data.getAbsStartCol();
                int r = coord.getRow() - data.getAbsStartRow();
                if (c < 0 || r < 0 || c >= data.getRaster_size() || r >= data.getRaster_size()) {
             //      p("Coord out of bounds: "+c+"/"+r);
                } else {
                    header += "(" + c + "/" + r + "),";
                    timeseries[nrt] = data.getTimeSeries(c, r, flow);
                    nrt++;
                }
            }
        }

        double t0 = data.getTimeStamp(flow, 0);

        p("Exporting " + nrt + " time series");
        for (int f = 0; f < frames; f++) {
            for (int t = 0; t < nrt; t++) {
                if (t == 0) {
                    double dt = data.getTimeStamp(flow, f) - t0;
                    values += "\n" + f + ", " + dt+", ";
                }

                float v = timeseries[t][f] - mincount;
                values += v + ", ";
                // p("Got value "+v+" for "+t+"/"+f);

            }
        }
        csv = header + values;
        FileTools.writeStringToFile(file, csv);
          JTextArea pane = new JTextArea(50, 40);
        // pane.setContentType("text");
        pane.setText(csv);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(csv), null);
        JOptionPane.showMessageDialog(this, new JScrollPane(pane), "You can copy this to Excel", JOptionPane.INFORMATION_MESSAGE);
        return true;
    }
    
       public boolean exportImage() {
        
        String file = FileTools.getFile("Save image to a file", "*.*", null, true);
        return exportImage(file);
    }

    public boolean exportImage(String file) {
        if (file == null || file.length() < 3) {
            GuiUtils.showNonModalMsg("I need to know if it is a .png or a .jpg file");
            return false;
        }
       
        File f = new File(file);
        String ext = file.substring(file.length() - 3);
        RenderedImage image = myCreateImage(getWidth(), getHeight());
        try {
            return ImageIO.write(image, ext, f);
        } catch (IOException ex) {
           // p("Could not write image to file " + f, ex);
        }
        return false;
    }
     public RenderedImage myCreateImage(int w, int h) {
        int width =Math.max(w,getWidth());
        int height = Math.max(h, getHeight());

        setSize(Math.max(w, getWidth()), Math.max(h, getHeight()));
        // Create a buffered image in which to draw
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics contents on the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();

        // Draw graphics
        paintComponent(g2d);

        return bufferedImage;
    }

    void setCrop(boolean b) {
       if (b) {
           cutleftframe = Math.min(maincont.getCropleft(),maincont.getCropright());
           cutrightframe = Math.max(maincont.getCropleft(),maincont.getCropright());
       }
       else {
           cutleftframe = 0;
           cutrightframe = 1000;
       }
    }

    /**
     * @return the plotfunction
     */
    public PlotFunction getPlotfunction() {
        return plotfunction;
    }

    /**
     * @param plotfunction the plotfunction to set
     */
    public void setPlotfunction(PlotFunction plotfunction) {
        this.plotfunction = plotfunction;
    }
}
