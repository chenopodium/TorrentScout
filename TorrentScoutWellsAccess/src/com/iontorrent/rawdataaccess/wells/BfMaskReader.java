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

/**
 *
 * @author Chantal Roth
 */
public class BfMaskReader {

    private String maskfile;
    private BfMask bfmask;
    
    public BfMaskReader(String maskfile) {
        this.maskfile = maskfile; 
        
    }
    public void readFile() {
        openFile();       
        bfmask.readHeader();
        bfmask.readAllData();
    }
    private void openFile() {
        bfmask = new BfMask(maskfile);       
    }
    public BfMask getMask() {
        return bfmask;
    }
      
}
