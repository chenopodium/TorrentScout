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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public abstract class PGMFrame {

    /** timestamp (ms)	unsigned int32 	Relative time from the start of acquisition for this frame  */
    protected long timestamp; // unused, uint32
    /** Compressed (0)	unsigned int32 	If this is a compressed frame or not.  
     * If not compressed, the data follows immediately. */
    protected long compressed;

    public PGMFrame() {
    }

    public abstract int getDataAt(int x, int y);

    public abstract boolean contains(int x, int y);
    
    protected void err(String msg, Exception ex) {
        Logger.getLogger(PGMFrame.class.getName()).log(Level.SEVERE, msg, ex);
    }

    protected void err(String msg) {
        Logger.getLogger(PGMFrame.class.getName()).log(Level.SEVERE, msg);
    }

    protected void p(String msg) {
        Logger.getLogger(PGMFrame.class.getName()).log(Level.INFO, msg);
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    public abstract void setDataAt(int x, int y, int value) ;
}
