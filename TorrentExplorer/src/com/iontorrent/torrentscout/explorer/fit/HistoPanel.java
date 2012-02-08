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
 * along with this program.  Iff not, see <http://www.gnu.org/licenses/>.
 */

/*
 * HistoPanel.java
 *
 * Created on 01.11.2011, 08:43:38
 */
package com.iontorrent.torrentscout.explorer.fit;

import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.torrentscout.explorer.ContextChangeAdapter;
import com.iontorrent.torrentscout.explorer.ExplorerContext;
import com.iontorrent.guiutils.widgets.Widget;
import com.iontorrent.guiutils.widgets.CoordWidget;
import com.iontorrent.torrentscout.explorer.Export;
import com.iontorrent.torrentscout.explorer.options.TorrentExplorerPanel;
import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.utils.io.FileTools;
import com.iontorrent.utils.stats.HistoStatistics;
import com.iontorrent.utils.stats.StatPoint;
import com.iontorrent.utils.stats.XYStats;
import com.iontorrent.wellmodel.RasterData;
import com.iontorrent.wellmodel.WellCoordinate;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.openide.util.NbPreferences;

/**
 *
 * @author Chantal Roth
 */
public class HistoPanel extends javax.swing.JPanel implements SelectionListener {

    ExplorerContext maincont;
    private boolean drawScissors;
    
    private int w;
    private int h;
    private int BORDER = 30;
    private int x0;
    private int y0;
    private int width;
    private int height;
    private double CX;
    private RasterData data;
    private double pixpertbin;
    private double pixpercount;
    // private ArrayList<Widget> widgets;
    private ArrayList<Widget> hwidgets;
    int[][] timeseries;
    StatPoint stat;
    double interval;
    boolean normalize;
    boolean second;
    double[][] histodata;
    HistoStatistics histo;
    HistoStatistics histo1;
    Color col = Color.red.darker();
    private int leftpos;
    private int rightpos;
    
    private int leftvalue;
    private int rightvalue;
    
    
    private double leftx;
    private double rightx;
    private int buckets;
    HistoWidget wleft;
    HistoWidget wright;
    HistoWidget curwidget;
    double cutleft;
    double maxy;
    boolean hasdragged;
    double cutright;
    SelectionListener list;
    int NRBUCKETS;
    DecimalFormat small = new DecimalFormat("#.###");
    DecimalFormat large = new DecimalFormat("###");
    DecimalFormat per = new DecimalFormat("0.##%");
    DecimalFormat form = small;
    double minx;
    double maxx;
    double[] bincoord;
//int bins

    /** Creates new form HistoPanel */
    public HistoPanel(ExplorerContext maincont, StatPoint stat, double[][] histodata, int nrbuckets, boolean normalize, boolean second) {
        this(maincont, stat, histodata, null, nrbuckets, normalize, second);
    }

    public HistoStatistics getHisto() {
        return this.histo;
    }
    public HistoPanel(final ExplorerContext maincont, StatPoint stat, double[][] histodata, final SelectionListener list, int nrbuckets, final boolean normalize, boolean second) {
        initComponents();
        drawScissors = true;
        this.second = second;
        this.normalize = normalize;
        this.setDoubleBuffered(false);
        this.NRBUCKETS = nrbuckets;
        setLayout(new FlowLayout());
        this.stat = stat;
        this.list = list;
        this.histodata = histodata;
        //  p("creating histopanel");
        //super(false);
        this.maincont = maincont;
        //   this.func = func;
        //setLayout(new FlowLayout());
        setBackground(Color.black);
        setMinimumSize(new Dimension(100, 100));
        setPreferredSize(new Dimension(300, 200));
        leftpos = 100;
        rightpos = 300;
        hwidgets = new ArrayList<Widget>();
        wleft = new HistoWidget(Color.gray, getLeftpos(), BORDER, 1);
        wright = new HistoWidget(Color.gray, getRightpos(), BORDER, 2);
        hwidgets.add(wleft);
        hwidgets.add(wright);

        computeHisto();

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                double ex = evt.getX();
                //curwidget = (HistoWidget) Widget.getClosest(ex, 0, hwidgets, 50);
                if (histo == null) return;
                int bin = getBin((int) ex);
                WellCoordinate abs = findCoordForBin(bin);
                
                p("Got bin " + bin + " with coord " + abs + ", changing main widget");
                if (abs != null) {
                    for (Widget w : maincont.getWidgets()) {
                        CoordWidget cw = (CoordWidget) (w);
                        if (cw.isMainWidget()) {
                            cw.setAbsoluteCoords(abs);
                            maincont.widgetChanged(w);
                            repaint();
                        }
                    }
                }
                hasdragged = false;
            }

            @Override
            public void mousePressed(MouseEvent evt) {
                double ex = evt.getX();
                curwidget = (HistoWidget) Widget.getClosest(ex, 0, hwidgets, 50, true);
                hasdragged = false;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (curwidget != null && hasdragged) {
                        double ex = e.getX();
                        int bin = getBin((int) ex);
                        curwidget.setBin(bin);
                        curwidget.setX((int) (ex));
                        setLeftpos(Math.min(wleft.getX(), wright.getX()));
                        setRightpos(Math.max(wleft.getX(), wright.getX()));
                        //         p("mouseReleased widget " + curwidget + ", then setting to null");
                        if (list != null) {
                            list.leftChanged(getLeftpos());
                            list.rightChanged(getRightpos());
                        }
                        curwidget = null;
                        repaint();
                    } else {
                        //       p("Left click mouse release, but no widget");
                        curwidget = null;
                    }
                }
                hasdragged = false;
            }
        });
        this.addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                double ex = e.getX();
                int bin = getBin((int) ex);

                if (histo != null && bin >= 0 && bin <= buckets) {
                    String s = "";
                    if (normalize) {
                        s = per.format(histo.getCount(bin));
                    } else {
                        s = "" + histo.getCount(bin);
                    }
                    setToolTipText("x=" + small.format(histo.getBucketXValue(bin)) + ", y=" + s);
                } else {
                    setToolTipText("Bin out of bounds");
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                //  p("Mouse dragged");
                if (SwingUtilities.isRightMouseButton(e)) {
                    super.mouseDragged(e);
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    hasdragged = true;
                    if (curwidget != null) {
                        double ex = e.getX();
                        curwidget.setX((int) ex);
                        // curwidget.setY((int) ey);
                        int bin = getBin((int) ex);
                        curwidget.setBin(bin);
                        setLeftpos(Math.min(wleft.getX(), wright.getX()));
                        setRightpos(Math.max(wleft.getX(), wright.getX()));
                        repaint();
                        //paintImmediately(0,0,1000,1000);
                        // paintAll(getGraphics());
                        //p("dragging widget bin "+ curwidget.getBin());
                    } else {
                        // p("Left drag, but no widget");
                    }
                } else {
                    //  p("drag, but right mouse");
                }
            }
        });
        maincont.addListener(new ContextChangeAdapter() {

            @Override
            public void coordChanged(WellCoordinate coord) {
                //   p("coord chanaged: ");
                repaint();
            }

            @Override
            public void widgetChanged(Widget w) {
                //    p("widget chnaged: " + w);
                repaint();
            }
        });
    }

    protected void drawHisto(Graphics2D g, HistoStatistics histo) {
        for (int i = 0; i < buckets; i++) {
            double yvalue = histo.getCount(i);
            yvalue = Math.min(yvalue, maxy * 1.05);
            if (yvalue > 0) {
                int x = getXForBucket(i);
                int x1 = getXForBucket(i + 1);
                int ycount = getY(yvalue);

                g.fill3DRect(x, ycount, x1 - x, Math.abs(ycount - y0), true);
            }
            // g.setColor(Color.black);
            //  g.drawRect(x, y1, x1-x, Math.abs(y1-0));
        }
    }

    private void p(String string) {
        System.out.println("HistoPanel: " + string);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D gg = (Graphics2D) g;
        width = getWidth();
        height = getHeight();
        p("Painting histopanel "+width+"/"+height);
        gg.clearRect(0, 0, width, height);
        //  gg.setColor(this.getParent().getBackground());
        //    gg.fillRect(0, 0, width, height);
        data = maincont.getData();

        // widgets = maincont.getWidgets();
        if (data == null || stat == null) {
            g.setColor(Color.white);
            g.drawString("No data yet", 10, BORDER);
            return;
        }
        w = width - 2 * BORDER;
        h = height - 2 * BORDER;
        x0 = BORDER;
        y0 = height - BORDER;
        this.pixpercount = (double) h / (double) maxy;
        // p("pix per percentage: " + pixpercount);
        this.pixpertbin = (double) w / (double) buckets;
        // p("Pix per bin=" + pixpertbin);
         if (leftvalue > 0) {
            this.setLeftX_r(leftvalue);
            leftvalue = 0;
        }
        if (rightvalue > 0) {
            this.setRightX_r(rightvalue);
            rightvalue = 0;
        }
        drawCoords(gg);
        drawChart(gg);
        drawSep(gg);

    }
    public void getPixPerBin() {
         width = getWidth();
         w = width - 2 * BORDER;
         if (w < 0) {
             p("No width yet: "+width);
         }
         this.pixpertbin = (double) w / (double) buckets;
    }

    public void update() {
        computeHisto();
        repaint();
    }

    public void addStats(StatPoint stat1) {
        XYStats stats1 = XYStats.createStats(stat1, interval, (int) minx, (int) maxx);
        histo1 = stats1.createStatistics();
        if (normalize) {
            histo1.normalize();
        }
        p("Computed second his");
    }

    private void computeHisto() {
        if (stat == null) {
            p("Got no stat object");
            return;
        }
        p("Got stats:" + stat.toString());
        minx = stat.getMin();
        maxx = stat.getMax();
        if (cutleft == cutright) {
            cutleft = minx;
            cutright = maxx;
        }

        p("max function value: " + maxx + ", min function value: " + minx);
        interval = Math.max(0.001, (double) (cutright - cutleft) / (double) NRBUCKETS);
        //   p("delta bucket interval: " + interval);
        XYStats stats = XYStats.createStats(stat, interval, (int) minx, (int) maxx);
        histo = stats.createStatistics();
        // p("Got histo bfore norm: " + histo.toCsv());
        // normalizing
        if (normalize) {
            histo.normalize();
        }
        // p("Got histo: " + histo.toCsv());
        //FileTools.writeStringToFile("histo.cvs", histo.toCsv());

        buckets = (int) histo.getNrBuckets();
        if (second) {
            maxy = histo.getSecondLargestY();
            p("Got SECOND LARGEST maxy:" + maxy);
        } else {
            maxy = histo.getMaxy();
        }


    }

    private void drawChart(Graphics2D g) {
        g.setColor(col);
        // g.drawString("maxy=" + maxy, BORDER, BORDER);
        drawHisto(g, histo);

        if (histo1 != null) {
            g.setColor(Color.blue);
            drawHisto(g, histo1);
        }
    }

    private void drawSep(Graphics2D g) {
        int delta = 50;
//        g.setColor(Color.green.darker());
//        g.fill3DRect(leftpos, y0 - h + delta, 2, h - delta, true);
//        g.drawString("left", leftpos - 10, y0 - h + 20);
//
//        g.setColor(Color.green.darker());
//        g.fill3DRect(rightpos, y0 - h + delta, 2, h - delta, true);
//        g.drawString("right", rightpos - 10, y0 - h + 20);


        if (drawScissors) {
            wleft.paint(g, getLeftpos(), height - BORDER, 1.0);
            wright.paint(g, getRightpos(), height - BORDER, 1.0);
        }

        // draw main cursor
        //   maincont.getWidcoords()

        ArrayList<Widget> widgets = maincont.getWidgets();
        int size = data.getRaster_size();
        int cstart = data.getAbsStartCol();// - maincont.getExp().getColOffset();
        int rstart = data.getAbsStartRow();//- maincont.getExp().getRowOffset();

        for (Widget w : widgets) {
            CoordWidget cw = (CoordWidget) (w);
            WellCoordinate coord = cw.getAbsoluteCoord();


            if (coord != null) {
                int relx = coord.getCol() - cstart;
                int rely = coord.getRow() - rstart;

                if (relx >= 0 && rely >= 0 && relx < size && rely < size) {
                    double value = histodata[relx][rely];
                    int x = this.getXForXval(value);
                    g.setColor(cw.getColor());
                    if (cw.isMainWidget()) {
                        g.setStroke(new BasicStroke(3));
                    } else {
                        g.setStroke(new BasicStroke(1));
                    }
                    g.fill3DRect(x, y0 - h + delta, 2, h - delta, true);
                    g.drawString(coord.getX() + "/" + coord.getY(), Math.max(BORDER, x - 30), y0 - h + 20);
                    g.drawString("v=" + (int) value, Math.max(this.BORDER, x - 30), y0 - h + 35);


                }
            }
        }
        g.setStroke(new BasicStroke(1));

    }

    public WellCoordinate findCoordForBin(int bin) {
        if (histo == null) return null;
        double mid = histo.getBucketXValue(bin);

        double count = histo.getCount(bin);
        if (count <= 0.0) {
            GuiUtils.showNonModalMsg("Found nothing in bucket for bin "+bin+" value " + (mid+histo.getBucketDelta()/2));
        }

        double delta = count/10.0;
        
        if (this.bincoord == null|| bin >= bincoord.length) bincoord = new double[(int) histo.getNrBuckets()];
        double bincount = bincoord[bin]+delta;
        
        if (bincount+delta >= count) bincount = delta;
        
        bincoord[bin] = bincount;
        
        p("Finding random coord "+bincount+" for bin "+bin+", there are "+count+" possible values");
        double a = mid;
        double b = mid + histo.getBucketDelta();
        double found = 0;
        for (int x = 0; x < histodata.length; x++) {
            for (int y = 0; y < histodata[0].length; y++) {
                double value = histodata[x][y];
                if (value >= a && value <= b) {
                    // check with bincount
                    found+=delta;
                    
                    if (found >=bincount) {
                        p("Found coord: " + x + "/" + y + " with an approx. value of " + bin);
                        WellCoordinate coord = new WellCoordinate(x + data.getAbsStartCol(), y + data.getAbsStartRow());
                        return coord;
                    }
                    
                }
            }
        }
        // we should NOT be here!
        p("For some reason, could not find "+bincount+"th coordinate for bin "+bin+"even though there should be "+count);
        return null;
    }

    public int getBin(int x) {
        return (int) ((x - x0) / pixpertbin);
    }

    private void drawCoords(Graphics2D g) {
        g.setColor(Color.black);
        g.fillRect(x0, y0 - h, w, h);
        g.setColor(Color.gray);
        g.drawRect(x0, y0 - h, w, h);

        double COORDY = 0.0001;

        int pow = (int) Math.floor(Math.log10(maxy / 2));
        if (pow > 0) {
            COORDY = (int) Math.pow(10, pow);
        } else {
            if (maxy > 0.30) {
                COORDY = 0.1;
            } else if (maxy > 0.03) {
                COORDY = 0.01;
            } else if (maxy > 0.003) {
                COORDY = 0.001;
            } else if (maxy > 0.0003) {
                COORDY = 0.0001;
            }
        }
        //      p("maxy: "+maxy+", COORDY="+COORDY);

        int count = (int) histo.getNrSamples();
        //double avg = histo.getAverage();


        int binleft = this.getBin(getLeftpos());
        int binright = this.getBin(getRightpos())+1;
            
        double betweenLeftRight = histo.computeCumulativeProbabilityForBuckets(Math.min(binleft, binright), Math.max(binleft, binright));
        // compute values between scissors
        
        g.drawString("" + count + " data points, mode="
                + form.format(histo.getXValueForMaxY()) + ", between frames " + maincont.getStartframe() + "-" + maincont.getEndframe() + ", right red frame=" + maincont.getCropright(), this.BORDER, 20);
        
        g.setColor(Color.white);
        if (normalize) {            
            g.drawString("Integral between scissors: "+per.format(betweenLeftRight), this.BORDER, this.BORDER+20);
        }
        else g.drawString("Between scissors: "+small.format(betweenLeftRight), this.BORDER, BORDER+20);
        
         g.setColor(Color.gray);
        for (double y = 0; y < maxy; y += COORDY) {
            int yy = this.getY(y);
            if (normalize) {
                g.drawString("" + per.format(y), 2, yy + 5);
            } else {
                g.drawString("" + small.format(y), 2, yy + 5);
            }
            g.drawLine(BORDER - 4, yy, BORDER, yy);
        }

        CX = 1;
//        int maxx = (int) histo.getBucketXValue(buckets - 1);
//        int minx = (int) histo.getBucketXValue(0);
        double dx = maxx - minx;
        pow = (int) Math.floor(Math.log10(dx / 2));
        if (pow > 0) {
            CX = (int) Math.pow(10, pow);
        } else {
            if (dx > 3) {
                CX = 1;
            } else if (dx > 0.30) {
                CX = 0.1;
            } else if (dx > 0.03) {
                CX = 0.01;
            } else if (dx > 0.003) {
                CX = 0.001;
            } else if (dx > 0.0003) {
                CX = 0.0001;
            } else {
                CX = 0.00001;
            }
        }
        if (CX >= 1) {
            form = large;
        } else {
            form = small;
        }

        double startx = Math.floor(minx / CX) * CX;
        //    p("minx-maxx=" + minx + "-" + maxx + ", dx:" + dx + ", CX=" + CX + ", startx=" + startx);

        for (double xval = startx; xval <= maxx; xval += CX) {
            int x = getXForXval(xval);
            g.drawLine(x, y0, x, y0 + 5);
            int x1 = getXForXval(xval + CX / 2.0);
            g.drawLine(x1, y0, x1, y0 + 3);
            g.drawString("" + form.format(xval), x - 10, y0 + 20);
        }

//        for (int b = 0; b < buckets; b += 10) {
//            int xval = (int) histo.getMidPoint(b);
//            //  if (count > 0) {
//
//            int x = getXForBucket(b);
//            g.drawString("" + xval, x + 10, y0 + 15);
//            // }
//        }


    }

    public int getXForXval(double x) {
        double b = histo.computeFloatingBucket(x);
        //p("Bucket for xvalue "+x+" is="+b);
        return getXForBucket(b);
    }

    public int getXForBucket(double b) {
        if (pixpertbin <0.5) getPixPerBin();
        return (int) (x0 + pixpertbin * b);
    }

    public int getY(double percent) {
        return (int) (y0 - pixpercount * percent);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 492, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 270, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    public double getLeftX() {
        int bin = getBin(getLeftpos());
        double value = histo.getBucketXValue(bin);
        p("Got bin: " + bin + ", and LEFT bucket value: " + value + " for leftpos " + getLeftpos());
        leftx = value;
        return leftx;
    }

    public void setLeftX(double value) {
        this.leftvalue = (int) value;
    }
    public void setLeftX_r(double value) {
       
        int pos  = getXForXval(value);
         p("Setting left to value "+value+"="+ pos);
         if (pos <= 0) {
            int b = histo.computeBucket(value);
            p("Pos for value "+value+" is 0. Bucket for value is "+b  +   ", nr buckets="+
                    histo.getNrBuckets()+", valuex at bucket is "+histo.getBucketXValue(b)+", bin="+getBin((int)value)+"pixperbin="+this.pixpertbin+", x="+getXForBucket(b));
            pos = getXForBucket(b);
        }
         pos = Math.max(BORDER, pos);
        setLeftpos(pos);
        wleft.setX(getLeftpos());
        
    }
    public void setRightX(double value) {
        this.rightvalue = (int)value;
    }
     public void setRightX_r(double value) {
        int pos = getXForXval(value);
        p("Setting right to value "+value+"="+ pos);
        if (pos == 0) {
            int b = histo.computeBucket(value);
            p("Pos for value "+value+" is 0. Bucket for value is "+b  +   ", nr buckets="+
                    histo.getNrBuckets()+", valuex at bucket is "+histo.getBucketXValue(b)+", bin="+getBin((int)value)+"pixperbin="+this.pixpertbin+", x="+getXForBucket(b));
            pos = getXForBucket(b);
        }
        if (pos <= getLeftpos()) pos = this.getWidth()-BORDER;
        setRightpos(pos);
        wright.setX(getRightpos());
        
    }
    public double getRightX() {
        int bin = getBin(getRightpos());
        double value = histo.getBucketXValue(bin);
        p("Got bin: " + bin + ", and RIGHT bucket value: " + value + "  for rightpos " + getRightpos());
        rightx = value;
        return rightx;
    }

    @Override
    public void leftChanged(double value) {
        cutleft = value;
        update();
    }

    @Override
    public void rightChanged(double value) {
        cutright = value;
        update();
    }

    public boolean exportImage() {
        if (histo == null) {
            GuiUtils.showNonModalMsg("Got no image to export yet...");
            return false;
        }
       
        String file = Export.getFile("Save image to a file", "*.png", true);
        
        return exportImage(file);
    }

    public boolean exportImage(String file) {
        if (file == null || file.length() < 3) {
            GuiUtils.showNonModalMsg("I need to know if it is a .png or a .jpg file");
            return false;
        }

        File f = new File(file);
        String ext = file.substring(file.length() - 3);
        RenderedImage image = myCreateImage();
        if (image == null) {
            p("Got no image");
            return false;
        }
        try {
            return ImageIO.write(image, ext, f);
        } catch (Exception ex) {
            p(ErrorHandler.getString(ex));
        }
        return false;
    }

    public RenderedImage myCreateImage() {
        if (this.getWidth() < 800) {
            this.setSize(800, 400);
        }
        p("Mycreateimage, size is:"+getSize());
        int w = Math.max(800, getWidth());
        int h = Math.max(400,getHeight());

        
        // setSize(Math.max(w, getWidth()), Math.max(h, getHeight()));
        // Create a buffered image in which to draw
        BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        // Create a graphics contents on the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();

        
        // Draw graphics
        paintComponent(g2d);

        return bufferedImage;
    }

    public boolean export(String file) {
        if (histo == null) {
            return false;
        }
        String out = "Experiment name: " + this.maincont.getExp().getExperimentName() + "\nRaw data dir: " + this.maincont.getExp().getRawDir() + "\nCoordinates" + maincont.getAbsDataAreaCoord() + "\n";
        out += "Left/Right frames: " + maincont.getStartframe() + "-" + maincont.getEndframe() + "\n";
        out += "Left/Right cut off frames: " + maincont.getCropleft() + "-" + maincont.getCropright() + "\n";
        String csv = out + histo.toCsv();
        FileTools.writeStringToFile(file, csv);
        JTextArea pane = new JTextArea(50, 40);
        // pane.setContentType("text");
        pane.setText(csv);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(csv), null);
        JOptionPane.showMessageDialog(this, new JScrollPane(pane), "You can copy this to Excel", JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    /**
     * @return the drawScissors
     */
    public boolean isDrawScissors() {
        return drawScissors;
    }

    /**
     * @param drawScissors the drawScissors to set
     */
    public void setDrawScissors(boolean drawScissors) {
        this.drawScissors = drawScissors;
    }

    /**
     * @return the leftpos
     */
    public int getLeftpos() {
        return leftpos;
    }

    /**
     * @param leftpos the leftpos to set
     */
    public void setLeftpos(int leftpos) {
        this.leftpos = leftpos;
    }

    /**
     * @return the rightpos
     */
    public int getRightpos() {
        return rightpos;
    }

    /**
     * @param rightpos the rightpos to set
     */
    public void setRightpos(int rightpos) {
        this.rightpos = rightpos;
    }
}
