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
package com.iontorrent.wellalgorithms;

import com.iontorrent.wellmodel.WellFlowData;
import com.iontorrent.wellmodel.WellFlowDataResult;
import com.iontorrent.wellmodel.WellFlowDataResult.ResultType;
import java.util.ArrayList;

/**
 *
 * @author Chantal Roth
 */
public class ZeromerTrace extends WellAlgorithm {

    /** relationship of empty to inferred bulk*/
//    private static final int default_tauEmpty = 4;
//    /** relationship of inferred bulk to well with bead */
//    private static final int default_tauBulk = 10;
//    /** Frames over which to operate */
//    private static final int default_frameStart = 5;
//    private static final int default_frameEnd = 100;
    private float mtauEmpty;
    private float mtauBulk;
    private int frameStart;
    private int frameEnd;

    /** results */
    public ZeromerTrace(WellContextFilter filter, int span, float tauEmpty, float tauBulk, int start, int end, boolean allWells) {
        super(filter, span, allWells);
        this.mtauBulk = tauBulk;
        this.mtauEmpty = tauEmpty;
        this.frameStart = start;
        this.frameEnd = end;
    }

    public String getName() {
        return "zeromer trace algorithm";
    }

    private WellFlowDataResult computeZeromer(WellFlowData medianEmpty, float tauEmpty, float tauBulk, long[] tt) {

        String thiskey = getKey() + ":" + tauEmpty + ":" + tauBulk;
        WellFlowDataResult zeromer = (WellFlowDataResult) this.getCachedResult(thiskey);
        if (zeromer != null) {
            return zeromer;
        }
        zeromer = WellFlowDataResult.createSimilarEmtpyWell(medianEmpty);

        double[] data = zeromer.getData();
        double[] zeromerdata = new double[data.length];
        System.arraycopy(data, 0, zeromerdata, 0, data.length);
        data = medianEmpty.getData();
        double[] emptydata = new double[data.length];
        System.arraycopy(data, 0, emptydata, 0, data.length);

        int start = Math.max(1, frameStart);
        int end = Math.min(frameEnd, zeromer.getNrFrames());
        int nrframes = end - start;
        double[] idelta = new double[nrframes + start];
        double[] cdelta = new double[nrframes + start];

        for (int f = start; f < end; f++) {
            //    deltat<-tt[j]-tt[j-1]
            double dt = (double) (tt[f] - tt[f - 1]);
            double dempty = (emptydata[f] - emptydata[0]);
            //    sB[j]<-(sE[j]*(tauEmpty+deltat) + cdelta[j-1])/(tauBulk+deltat)
            zeromerdata[f] = (dempty * (tauEmpty + dt) + cdelta[f - 1]) / (tauBulk + dt);
            //    idelta[j]<-sE[j]-sB[j]
            idelta[f] = (dempty - zeromerdata[f]) * dt;
            //    cdelta[j]<-cdelta[j-1]+idelta[j]
            cdelta[f] = cdelta[f - 1] + idelta[f];
        }
        zeromer.setData(zeromerdata);
        this.cacheResult(thiskey, zeromer);
        return zeromer;

    }

    @Override
    public ArrayList<WellFlowDataResult> compute() {
        //     p("Computing ZeromerTrace for " + getCoord() + " and flow " + getFlow());

        ArrayList<WellFlowData> contextwells = this.getWellsInContext();
        if (contextwells == null) {
            return null;
        }

        WellFlowDataResult resdata = createResultWell();

        WellFlowDataResult medianEmpty = computeMedian(contextwells, resdata);
        medianEmpty.setResultType(ResultType.MEDIAN);
        results.add(medianEmpty);

        long[] tt = medianEmpty.getTimestamps();
       
        //zeromer.t(sE,tauEmpty,0,tt)  #what the inferred bulk looks like given the empty
        //zeromer.t(sE,tauEmpty,tauBulk,tt) #generates zero-mer trace without incorporation
        WellFlowDataResult zeromerBulk = computeZeromer(medianEmpty, mtauEmpty, 0, tt);
        zeromerBulk.setName("Inferred bulk given empty, span " + getSpan());
        zeromerBulk.setResultType(ResultType.ZEROMER_BULK);
        zeromerBulk.setDescription("Found " + this.getNrEmpty() + " wells within " + getSpan() + " wells in each direction");
        //p("zeromerBulk: "+zeromerBulk+":"+Arrays.toString(zeromerBulk.getData()));

        WellFlowDataResult zeromerNoInc = computeZeromer(medianEmpty, mtauEmpty, mtauBulk, tt);
        zeromerNoInc.setName("Zeromer trace wo incorporation, span " + getSpan());
        zeromerNoInc.setResultType(ResultType.ZEROMER_NOINC);
        zeromerNoInc.setDescription("Found " + this.getNrEmpty() + " wells within " + getSpan() + " wells in each direction");
        //p("zeromerNoInc: "+zeromerNoInc+":"+Arrays.toString(zeromerNoInc.getData()));
        results.add(zeromerBulk);
        results.add(zeromerNoInc);

        // now also compute difference
        WellFlowData rawData = getRawWell();
        int nrframes = rawData.getNrFrames();

        WellFlowDataResult dBulk_NoInc = WellFlowDataResult.createSimilarEmtpyWell(resdata);
        for (int f = 0; f < nrframes; f++) {
            dBulk_NoInc.getData()[f] = zeromerBulk.getData()[f] - zeromerNoInc.getData()[f];
        }
        dBulk_NoInc.setResultType(ResultType.ZEROMER_BULK_NOINC);
        results.add(dBulk_NoInc);


        WellFlowDataResult dBulk_Empty = WellFlowDataResult.createSimilarEmtpyWell(resdata);
        for (int f = 0; f < nrframes; f++) {
            dBulk_Empty.getData()[f] = zeromerBulk.getData()[f] - medianEmpty.getData()[f];
        }
        dBulk_Empty.setResultType(ResultType.ZEROMER_BULK_EMPTY);
        results.add(dBulk_Empty);

        WellFlowDataResult draw_noinc = WellFlowDataResult.createSimilarEmtpyWell(resdata);
        for (int f = 0; f < nrframes; f++) {
            draw_noinc.getData()[f] = rawData.getData()[f] - zeromerNoInc.getData()[f];
        }
        draw_noinc.setResultType(ResultType.ZEROMER_RAW_NOINC);
        results.add(draw_noinc);

        return results;
    }
}
