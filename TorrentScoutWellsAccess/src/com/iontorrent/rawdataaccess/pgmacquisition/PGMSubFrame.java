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
package com.iontorrent.rawdataaccess.pgmacquisition;

/**
 *
 * @author Chantal Roth
 */
public class PGMSubFrame extends PGMFrame{

   
    private int[][] imageData;
    private int startx;
    private int starty;
    private int dx;
    private int dy;
    private int nrcols;
    private int endx;
    private int endy;

    public PGMSubFrame(PGMFrame frame, PGMAcquisitionGlobalHeader header, int startx, int starty, int dx, int dy) {
        super();
        this.startx = startx;
        this.starty = starty;
        this.dx = dx;
        this.dy = dy;
        this.nrcols = header.getNrCols();
        this.timestamp = frame.getTimestamp();
        this.endx = Math.min(startx + dx, header.getNrCols());
        this.endy = Math.min(starty + dy, header.getNrRows());
        imageData = new int[dx][dy];
        for (int x = startx; x < endx; x++) {
            int i = x - startx;
            for (int y = starty; y < endy; y++) {
                int j = y - starty;
                imageData[i][j] = frame.getDataAt(x, y);
            }
        }
    }

    public boolean contains(int x, int y) {
        return x >= startx && x < (endx) && y >= starty && y < (endy);
    }
//    /** row major order */

    @Override
    public int getDataAt(int x, int y) {
        if (!contains(x, y)) {
            //  err(x + "/" + y + "  not part of subframe " + toString());
            return 0;
        }
        return imageData[x - getStartx()][y - getStarty()];
    }

    @Override
    public void setDataAt(int x, int y, int value) {
        if (!contains(x, y))  return;        
        imageData[x-getStartx()][y-getStarty()] = value;
    }
    public String toString() {
        return "SubFrame: " + getStartx() + "/" + getStarty() + " - " + endx + "/" + endy;
    }

  
    /**
     * @return the imageData
     */
    public int[][] getImageData() {
        return imageData;
    }

    /**
     * @return the startx
     */
    public int getStartx() {
        return startx;
    }

    /**
     * @param startx the startx to set
     */
    public void setStartx(int startx) {
        this.startx = startx;
    }

    /**
     * @return the starty
     */
    public int getStarty() {
        return starty;
    }

    /**
     * @return the dx
     */
    public int getDx() {
        return dx;
    }

    /**
     * @return the dy
     */
    public int getDy() {
        return dy;
    }
}
