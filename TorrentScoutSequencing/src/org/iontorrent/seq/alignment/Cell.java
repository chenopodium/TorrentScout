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
package org.iontorrent.seq.alignment;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class Cell {

    /**
     * Row of the cell
     */
    private int row;
    /**
     * Column of the cell
     */
    private int col;
    /**
     * Alignment score at this cell
     */
    private int alignmentpos;
    private float score;
    private byte direction;
    /** nr of best scores in other parts of the matrix */
    private int nrbest;
    private ArrayList<Cell> others;

    /**
     * Constructor
     */
    public Cell() {
        super();
        this.row = 0;
        this.col = 0;
        alignmentpos = -1;
        this.score = Float.NEGATIVE_INFINITY;
    }

    public int getNrbest() {
        return nrbest;
    }

    public void setNrbest(int count) {
        this.nrbest = count;
    }

    /**
     * @return Returns the col.
     */
    public int getCol() {
        return this.col;
    }

    /**
     * @param col The col to set.
     */
    public void setCol(int col) {
        this.col = col;
    }

    /**
     * @return Returns the row.
     */
    public int getRow() {
        return this.row;
    }

    /**
     * @param row The row to set.
     */
    public void setRow(int row) {
        this.row = row;
    }

    /**
     * @return Returns the score.
     */
    public float getScore() {
        return this.score;
    }

    /**
     * @param score The score to set.
     */
    public void setScore(float score) {
        this.score = score;
    }

    /**
     * Sets the row, column and score of the cell.
     * @param row The row to set.
     * @param col The col to set.
     * @param score The score to set.
     */
    public void set(int row, int col, float score) {
        this.row = row;
        this.col = col;
        this.score = score;
    }

    public String toString() {
        String s = "Cell " + row + "/" + col + ":" + score;
        if (this.alignmentpos > -1) {
            s += " @ " + alignmentpos;
        }
        return s;
    }

    public static void main(String[] args) {
    }

    public void setOthers(ArrayList<Cell> others) {
        this.others = others;
    }

    public ArrayList<Cell> getOthers() {
        return others;
    }

    public void setDirection(byte b) {
        this.direction = b;

    }

    public boolean isDiagonal() {
        return direction == Directions.DIAGONAL;
    }

    public byte getDirection() {
        return direction;
    }

    public int getAlignmentpos() {
        return alignmentpos;
    }

    public void setAlignmentpos(int alignmentpos) {
        this.alignmentpos = alignmentpos;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(Cell.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(Cell.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(Cell.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
//  System.out.println("Cell: " + msg);
        //Logger.getLogger( Cell.class.getName()).log(Level.INFO, msg, ex);
    }
}
