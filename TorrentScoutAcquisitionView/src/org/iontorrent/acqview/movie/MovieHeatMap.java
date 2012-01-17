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
package org.iontorrent.acqview.movie;

import com.iontorrent.guiutils.ColorGradient;
import com.iontorrent.guiutils.heatmap.HeatMap;
import com.iontorrent.wellmodel.RasterData;
import com.iontorrent.wellmodel.WellCoordinate;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 *
 * @author Chantal Roth
 */
public class MovieHeatMap extends JPanel {

    private RasterData data;
    private MovieState state;
    /** size of image in nr of wells per side */
    private static int NR_WELLS = 10;
    /** the offscreen image to which the density plot is drawn */
//    private transient final InstanceContent wellContextContent = LookupUtils.getPublisher(WellContext.class);
//    private transient final InstanceContent wellSelectionContent = LookupUtils.getPublisher(WellSelection.class);
    private static final int IMAGE_SIZE = 300;
    private int flow;
    private WellCoordinate coord;
    private WellCoordinate fromCoord;
    private WellCoordinate toCoord;
    private int maxcount;
    private int mincount;
    private int maxForFrame;
    private int minForFrame;
    private HeatMap heatmap;

    public MovieHeatMap(RasterData data, MovieState state, int flow, WellCoordinate coord) {
        this.data = data;
        this.coord = coord;
        this.state = state;
        this.flow = flow;
        maxcount = Math.max((int)data.getMax(), 0);
        mincount = Math.min((int)data.getMin(), 0);
        if (maxcount == 0) {
          //  err("Maximum data is " + maxcount + ". Raster data probably not read correctly.");
          //  p("Data is: " + data.toString());
        }
        p("max value =" + maxcount);
        p("min value =" + mincount);
        super.setDoubleBuffered(true);
        setLayout(new BorderLayout());
        update();
    }

    public int getMincount() {
        return mincount;
    }

    public int getMaxcount() {
        return maxcount;
    }

    /** buffer only for one flow, so clear all buffers */
    public void setFlow(int flow) {

        state.stop();
    }

    public WellCoordinate getFrom() {
        return fromCoord;
    }

    public WellCoordinate getTo() {
        return toCoord;
    }

    public void update() {
        createHeatMap(state.getFrame());
    }

    /** Draws the well density plot onto the buffered Image */
    private void createHeatMap(int frame) {
        if (heatmap != null) {
            remove(heatmap);
        }
        // Create a graphics context on the buffered image
        if (data == null) {
            return;
        }
        int width = IMAGE_SIZE;
        int cols = NR_WELLS;
        int rows = NR_WELLS;
        int pixpercol = (int) Math.max(1.0, (double) width / (double) cols);
        int pixperrow = pixpercol;

        p("DRAWING frame " + frame + ": pixpercol=" + pixpercol + ", pixperrow=" + pixperrow);

        maxForFrame = Math.max((int)data.getMax(flow, frame), 0);
        minForFrame = Math.min((int)data.getMin(flow, frame), 0);
        p("Min/max for frame " + frame + ":" + minForFrame + "/" + maxForFrame);


        Color[] gradientColors = new Color[]{Color.blue.darker(), Color.blue, Color.green, Color.yellow, Color.orange, Color.red, Color.white};
        // the area that was used to compute the density = this is the maximum possible value


        Color[] customGradient = ColorGradient.createMultiGradient(gradientColors, maxcount - mincount + 1);


        int colinraster = coord.getCol() - data.getRelStartcoord().getCol();
        int rowinraster = coord.getRow() - data.getRelStartcoord().getRow();
        // center += half
        int startc = Math.max(0, colinraster - NR_WELLS / 2);
        int startr = Math.max(0, rowinraster - NR_WELLS / 2);
        startc = Math.min(startc, data.getRaster_size() - NR_WELLS);
        startr = Math.min(startr, data.getRaster_size() - NR_WELLS);

        fromCoord = new WellCoordinate(startc, startr);
        toCoord = new WellCoordinate(startc + NR_WELLS, startr + NR_WELLS);

        double[][] heatdata = new double[NR_WELLS][NR_WELLS];

        for (int c = startc; c < startc + NR_WELLS; c++) {
            for (int r = startr; r < startr + NR_WELLS; r++) {
                float count = data.getValue(c, r, flow, frame) + mincount;
                count = Math.min(count, customGradient.length - 1);
                count = Math.max(count, 0);
                heatdata[c - startc][r - startr] = count;

            }
        }
        heatmap = new HeatMap(heatdata, true, customGradient);
        add("Center", heatmap);

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(IMAGE_SIZE, IMAGE_SIZE);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(IMAGE_SIZE, IMAGE_SIZE);
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(MovieHeatMap.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(MovieHeatMap.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(MovieHeatMap.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("MovieHeatMap: " + msg);
        //Logger.getLogger( MovieHeatMap.class.getName()).log(Level.INFO, msg, ex);
    }
}
