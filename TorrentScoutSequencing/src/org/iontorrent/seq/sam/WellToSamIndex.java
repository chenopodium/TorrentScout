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
package org.iontorrent.seq.sam;

import java.io.File;
import java.util.HashMap;
import net.sf.samtools.SAMRecord;
import org.iontorrent.seq.Coord;

/**
 *
 * @author Chantal Roth
 */
public class WellToSamIndex extends AbstractSamIndex{

   public WellToSamIndex(File samfile, File indexfile) {
       super(samfile, indexfile);
    }
   
    protected String getKey(int x, int y) {
        return x + ":" + y;
    }

    @Override
    protected void addRecordToIndex(SAMRecord rec, long curpos) {
        Coord coord = extractWellCoord(rec.getReadName());
        if (coord != null) {
            addPosition(coord.x, coord.y, curpos);
        }
    }
    private void addPosition(int x, int y, long pos) {
        if (map == null) {
            map = new HashMap<String, Long>();
        }
        String key = getKey(x, y);
        map.put(key, pos);
        
    }}
