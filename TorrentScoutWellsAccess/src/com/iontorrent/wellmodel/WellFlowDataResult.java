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

import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.rawdataaccess.wells.BfMaskDataPoint;
import java.io.Serializable;

/**
 *
 * @author Chantal Roth
 */
public class WellFlowDataResult extends WellFlowData implements Serializable {

    private String name;
    private String description;
    private ResultType resulttype;
    private long starttime;

    public WellFlowDataResult(WellFlowDataResult d) {
        super(d.getCol(), d.getRow(), d.getFlow(), d.getType(), d.getMask());
        this.name = d.name;
        this.resulttype = d.resulttype;
        this.setData(d.getData());
        this.setTimestamps(d.getTimestamps());
        this.setShow(d.isShow());
    }
    
   
   

    public void cloneData() {
        double[] tmp = new double[data.length];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = data[i];
        }
        data = tmp;
    }

    public void append(WellFlowDataResult d) {
        int len = this.getData().length + d.getData().length;
        double newdata[] = new double[len];
        long newts[] = new long[len];
        double[] data = this.getData();
        for (int i = 0; i < data.length; i++) {
            newdata[i] = data[i];
            newts[i] = this.getTimestamps()[i];
        }
        long lastts = newts[data.length - 1];
        int off = data.length;
        data = d.getData();
        for (int i = 0; i < data.length; i++) {
            newdata[i + off] = data[i];
//           if (i ==0) {
//               newdata[i+off]=-50;
//           }
            newts[i + off] = lastts + d.getTimestamps()[i];
        }
        this.setData(newdata);
        this.setTimestamps(newts);
    }

    /**
     * @return the starttime
     */
    public long getStarttime() {
        return starttime;
    }

    /**
     * @param starttime the starttime to set
     */
    public void setStarttime(long starttime) {
        this.starttime = starttime;
    }

    public enum ResultType implements Serializable {

        RAW("Raw", false),
        ZEROMER_BULK("Zeromer bulk", false),
        ZEROMER_NOINC("Zeromer noinc", false),
        ZEROMER_BULK_NOINC("Zeromer bulk-noinc", false),
        ZEROMER_BULK_EMPTY("Zeromer bulk-empty", false),
        ZEROMER_RAW_NOINC("Zeromer raw-noinc", false),
        //   NN("Nearest neighbor bg", false),
        MEDIAN("Med NN empty", false, "Median empty signal for <b>surrounding</b> wells for this coord (same flow)"),
        EMPTYFLOWS("Med empty flows", false, "Median empty signal for this coordinate but using <b>all (cached) empty flows of this one well</b>"),
        NN_RAW_BG("Raw-nn empty", true, "Raw minus the median emtpy signal for all <b>neighboring wells (same flow)</b>"),
        RAW_BGFLOW("Raw-empty flow", false, "Raw minus the median empty signal <b>for all cached flows</b> for this <b>one well</b>");
        //String name;
        private String name;
        private boolean show;
        private String desc;
        private boolean enabled;

        ResultType(String name, boolean show) {
            this(name, show, name);
        }

        ResultType(String name, boolean show, String desc) {
            this.name = name;
            this.show = show;
            this.desc = desc;
        }

        public String toString() {
            return getName();
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the show
         */
        public boolean isShow() {
            return show;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getDesc() {
            return desc;
        }

        /**
         * @param show the show to set
         */
        public void setShow(boolean show) {
            this.show = show;
        }
    }

    public WellFlowDataResult(int col, int row, int flow, RawType filetype, BfMaskDataPoint mask) {
        super(col, row, flow, filetype, mask);
    }

    public static WellFlowDataResult createSimilarEmtpyWell(WellFlowData sample) {
        return createSimilarEmtpyWell(sample.getCol(), sample.getRow(), sample);
    }

    public WellFlowDataResult createResult(ResultType type) {
        WellFlowDataResult r = new WellFlowDataResult(getCol(), getRow(), getFlow(), getType(), getMask());
        r.setResultType(type);
        return r;
    }

    public static WellFlowDataResult createSimilarEmtpyWell(int x, int y, WellFlowData sample) {
        if (sample == null) {
            err("createSimilarEmtpyWell: No wellflow data");
            return null;
        }
        WellFlowDataResult resultwell = new WellFlowDataResult(x, y, sample.getFlow(), sample.filetype, sample.getMask());
        resultwell.setTimestamps(sample.getTimestamps());
        int nrframes = sample.getNrFrames();
        resultwell.setData(new double[nrframes]);
        return resultwell;
    }

    public String toString() {
        return resulttype + ":" + name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public void setResultType(ResultType resultType) {
        this.resulttype = resultType;
        this.setName(resultType.getName());
    }

    public ResultType getResultType() {
        return resulttype;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the show
     */
    public boolean isShow() {
        return resulttype.isShow();
    }

    /**
     * @param show the show to set
     */
    public void setShow(boolean show) {
        resulttype.setShow(show);
    }

    
}
