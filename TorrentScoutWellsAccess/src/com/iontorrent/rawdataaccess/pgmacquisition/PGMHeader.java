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

import java.io.DataInputStream;

/**
 *
 * @author Chantal Roth
 */
public abstract class PGMHeader {

     /** wall time	unsigned int32 	time of acquisition  */
    protected long wallTime;
    protected int nrRows;
    /** cols	cols	unsigned int16 	number of columns in the following images  */
    protected int nrCols;
    /** interlacetype	unsigned int16 	0=uninterlaced, 4=compressed  */
    protected int interlacetype;
    protected int nrFrames;
    static final long DAT_HEADER_SIGNATURE = 0xDEADBEEFL;

    protected abstract void read(DataInputStream in) ;
    
     /**
     * @return the wallTime
     */
    public long getWallTime() {
        return wallTime;
    }
    /**
     * @return the nrFrames
     */
    public int getNrFrames() {
        return nrFrames;
    }

    /**
     * @return the nrRows
     */
    public int getNrRows() {
        return nrRows;
    }

    /**
     * @return the nrCols
     */
    public int getNrCols() {
        return nrCols;
    }

    public int getInterlacetype() {
        return interlacetype;
    }
    
    
}
