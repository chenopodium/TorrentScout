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

package com.iontorrent.wellmodel;


import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Chantal Roth
 */
public class FlowCoordinate {

    private WellCoordinate coord;
    private int flow;
    private int frame;
    
    public FlowCoordinate(WellCoordinate coord, int flow) {
        this(coord, flow, 0);
    }
    public FlowCoordinate(WellCoordinate coord, int flow, int frame) {
        this.coord = coord;
        this.flow = flow;
        this.frame = frame;
    }

    public int getRow() { return coord.getRow(); }
    public int getCol() { return coord.getCol(); }
    
    public String toString() {
        return "FlowCoord: "+coord.toString()+", flow="+flow+", frame="+frame;
    }
/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( FlowCoordinate.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( FlowCoordinate.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( FlowCoordinate.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("FlowCoordinate: " + msg);
        //Logger.getLogger( FlowCoordinate.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the coord
     */
    public WellCoordinate getCoord() {
        return coord;
    }

    /**
     * @param coord the coord to set
     */
    public void setCoord(WellCoordinate coord) {
        this.coord = coord;
    }

    /**
     * @return the flow
     */
    public int getFlow() {
        return flow;
    }

    /**
     * @param flow the flow to set
     */
    public void setFlow(int flow) {
        this.flow = flow;
    }

    /**
     * @return the frame
     */
    public int getFrame() {
        return frame;
    }

    /**
     * @param frame the frame to set
     */
    public void setFrame(int frame) {
        this.frame = frame;
    }
}
