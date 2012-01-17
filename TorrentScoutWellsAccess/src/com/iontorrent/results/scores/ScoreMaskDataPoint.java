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
package com.iontorrent.results.scores;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
The data section consists of a single array of nRow * nCol uint16 values,
 * one per well stored in column-major order. 
 * In other words, the offset for the value for row i and column j    
 * @author Chantal Roth
 */
public class ScoreMaskDataPoint {

    /** Each bit in the integer is used as a boolean flag to indicate the properties outlined in the table below. */
    double score;

    public ScoreMaskDataPoint() {
     
    }
  public ScoreMaskDataPoint(double val) {
       this.score = val;
    }
  
    public double getValue() {
        return score;
    }
    public void setValue(double val) {
        this.score = val;
    }
    public String toString() {
       
       return ""+score;
    }

    private void err(String msg, Exception ex) {
        Logger.getLogger(ScoreMaskDataPoint.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void p(String msg) {
        System.out.println("ScoreMaskDataPoint: " + msg);

    }
}
