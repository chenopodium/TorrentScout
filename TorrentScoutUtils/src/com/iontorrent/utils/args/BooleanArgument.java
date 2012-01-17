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
package com.iontorrent.utils.args;

public class BooleanArgument extends Argument {

    public static final String TYPE = "boolean";

    public BooleanArgument(String name) {
        super(name);
    }

    public BooleanArgument(String name, Object defaultValue) {
        super(name, defaultValue);
    }
    public String getTypeDescriptor() {
        return TYPE;
    }

    /**
     * Returns a java.lang.Boolean.  If the given value is equal, ignoring case,
     * to any of "true", "t", "yes", "y", "on", then the returned boolean is
     * true, otherwise false.
     * @param value to be evaluated to true or false
     * @return a java.lang.Boolean representing the given argument
     */
    protected Object validateArgumentValue(String value) {
        return new Boolean(isTrue(value));
    }


    private boolean isTrue(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("t") ||
               value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("y") ||
               value.equalsIgnoreCase("on");
    }
}