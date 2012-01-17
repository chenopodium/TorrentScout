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
package com.iontorrent.rawdataaccess.wells;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class WellsReader {

    private File wellsfile;
    private Wells wells;
    private BfMask mask;

    public WellsReader(File wellsfile, BfMask mask) {
        this.wellsfile = wellsfile;
        this.mask = mask;
        openFile();
    }

    public WellHeader getHeader() {
        return wells.getHeader();
    }
    private void openFile() {
        wells = new Wells(wellsfile, mask);
        if (wells != null) wells.readHeader();
    }

    
    public WellData readWell(int col, int row) {
        return wells.readWell(col, row);
    }

    public Wells getWells() {
        return wells;
    }

    private void p(String msg) {
        Logger.getLogger(WellsReader.class.getName()).log(Level.INFO, msg);
    }

    public void close() {
        wells.closeFile();
    }
}
