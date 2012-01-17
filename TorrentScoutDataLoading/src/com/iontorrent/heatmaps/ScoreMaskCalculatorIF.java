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
package com.iontorrent.heatmaps;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.rawdataaccess.wells.ScoreMaskFlag;
import org.iontorrent.seq.alignment.Alignment;

/**
 *
 * @author Chantal Roth
 */
public interface ScoreMaskCalculatorIF {
    public double compute(Alignment al) ;
    
    public void setExpContext(ExperimentContext exp);
    
    public ScoreMaskFlag getFlag();

    public void setFlag(ScoreMaskFlag scoreMaskFlag);
    
    public Parameter[] getParams();
    
    public void setParams(Parameter[] par);

    public String getDesc();
    
    public String getName();

    public boolean requiresRead();
}
