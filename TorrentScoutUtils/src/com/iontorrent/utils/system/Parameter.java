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
package com.iontorrent.utils.system;

import java.util.Comparator;

public class Parameter implements Comparator {

    private String handle;
    private String value;
    private String comment;
    private String type;
    private double min;
    private double max;
    private double inc;

    public Parameter(String handle, String value, String comment) {
        this(handle, value, comment, "STRING");
    }
    public Parameter(String handle, int value, String comment) {
        this(handle, ""+value, comment, "INTEGER");
    }
     public Parameter(String handle, double value, String comment) {
        this(handle, ""+value, comment, "DOUBLE");
    }
    public Parameter(String handle, String value, String comment, String type) {
        this.handle = handle;
        this.value = value;
        this.comment = comment;
        this.type = type;
        //		if (comment != null)  p("Comment is: "+comment);
    }

    public int getIntValue()  {
        int i = Integer.MIN_VALUE;
        try {
            i = Integer.parseInt(value);
        }
        catch (Exception e) {}
       // p("parsing "+value+"="+i);
        return i;
    }
     public double getDoubleValue()  {
        double i = Double.NaN;
        try {
            i = Double.parseDouble(value);
        }
        catch (Exception e) {}
       // p("parsing "+value+"="+i);
        return i;
    }
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof Parameter) || !(o2 instanceof Parameter)) {
            System.err.println("Parameter, wrong class in compare: " + o1.getClass().getName() + ", " + o2.getClass().getName());
        }
        Parameter a = (Parameter) o1;
        Parameter b = (Parameter) o2;
        if (a == null || b == null) {
            return 0;
        }
        int t = (int) (a.getType().compareTo(b.getType())) * 100;
        t = t + a.getHandle().compareTo(b.getHandle());
        return t;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        Parameter p = (Parameter) o;
        return (p.handle.equalsIgnoreCase(handle));
    }

    public int hashCode() {
        return handle.hashCode();
    }

    public String getComment() {
        return comment;
    }

    public String getHandle() {
        return handle;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public void setComment(String string) {
        comment = string;
    }
    public String getDescription() {
        return comment;
    }
    public String getName() {
        return handle;
    }
    public void setHandle(String string) {
        handle = string;
    }

    public void setType(String string) {
        type = string;
    }

    public void setValue(String string) {
        value = string;
    }

    public String toString() {
        return handle + "=" + value;
    }

    public void setRange(double d, double d0) {
        this.setMin(d);
        this.max = d0;
    }

    /**
     * @return the min
     */
    public double getMin() {
        return min;
    }

    /**
     * @param min the min to set
     */
    public void setMin(double min) {
        this.min = min;
    }

    public void setRange(double d, double d0, double inc) {
        setRange(d, d0);
        setInc(inc);
    }

    /**
     * @return the inc
     */
    public double getInc() {
        return inc;
    }

    /**
     * @param inc the inc to set
     */
    public void setInc(double inc) {
        this.inc = inc;
    }

    public double getMax() {
        return max;
    }
    
}
