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
package com.iontorrent.wellalgorithms;

import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellFlowData;
import com.iontorrent.wellmodel.WellFlowDataResult;
import com.iontorrent.wellmodel.WellFlowDataResult.ResultType;
import java.util.ArrayList;

/**
 *
 * @author Chantal Roth
 */
public class FlowBgAlg extends WellAlgorithm {

    ArrayList<Integer> flows;
    public FlowBgAlg(WellContextFilter filter, ArrayList<Integer> flows, boolean allWells) {
        super(filter, 5, allWells);
        this.flows = flows;
    }

    @Override
    public String getName() {
        return "flow bg algorithm";
    }

    @Override
    public ArrayList<WellFlowDataResult> compute() {
        p("Computing flow bg for " + getCoord() + " and flow " + getFlow());
        
        WellFlowDataResult bg = (WellFlowDataResult) this.getCachedResult(getKey() + ":flowbg");
        if (bg == null) {
            bg = createResultWell();
            if (bg == null) {
                err("flowbg.compute: Got no result well!");
                return null;
            }           
            WellFlowData data =getFlowDataForFlows(flows);
            bg.setData(data.getData());
        }
        
        WellFlowDataResult diffdata = WellFlowDataResult.createSimilarEmtpyWell(bg);
        
        int nrframes = bg.getNrFrames();
        // now also compute difference
        WellFlowData welldata = getRawWell();
        for (int f = 0; f < nrframes; f++) {
            diffdata.getData()[f] = welldata.getData()[f] - bg.getData()[f];
        }
        //now we have the sum of all value. Can compute average now...
        // or mean?
        bg.setName("Flow bg for empty flows");
        bg.setResultType(ResultType.EMPTYFLOWS);
        bg.setDescription("BG computation using empty wells for this coordinate using all flows (mean).");

        diffdata.setName("Raw- flow BG");
        diffdata.setResultType(ResultType.RAW_BGFLOW);
        diffdata.setDescription("Raw - BG subtraction using empty flow for this coord.");
        results.add(bg);
        results.add(diffdata);
        
        this.cacheResult(getKey()+":flowbg", bg);
        return results;
    }
    public WellFlowData getFlowDataForFlows(ArrayList<Integer> flows) {
        p("FlowBgAlg.getFlowDataForFlows("+flows+")");
       
        ArrayList<WellFlowData> data = new ArrayList<WellFlowData>();
        WellCoordinate coord = filter.getCoord();
        int nr = 0;
        for (int flow: flows) {
            if (io.isCached(coord, flow)) {
                WellFlowData d = io.readOneWellAndTransform(coord, flow, filter.getContext());
                //WellFlowData d = io.readOneWell(coord, flow);
                if (d != null) {
                    nr++;
                    data.add(d);
                }
            }
           // else err("Not reading data for flow "+flow+", flow is not cached");
        }      
        WellFlowData res = computeMedian(data);                
        this.nrEmpty = nr;
        return res;

    }
}
