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

public class LongArgument extends Argument {
    public static final String TYPE = "long integer";

    public LongArgument(String name) {
        super(name);
    }

    public LongArgument(String name, Object defaultValue) {
        super(name, defaultValue);
    }

    public String getTypeDescriptor() {
        return TYPE;
    }

   /**
    * Ensure argument is a valid long integer and return value as j
    * java.lang.Long object if so.  Range of long integer determined by
    * java language specification for type "long".
    * @param value long integer in string form
    * @return a java.lang.Long object representing the given value
    * @throws IllegalArgumentException if the given value is not a valid
    *         long integer.
    */
    protected Object validateArgumentValue(String value) {
        try {
            return new Long(value);
        } catch (NumberFormatException parseError) {
            throw new IllegalArgumentException("Could not convert value '" +
                value + "' to long integer");
        }
    }
}
