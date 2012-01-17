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

public class IntegerArgument extends Argument {

    public static final String TYPE = "integer";
    public IntegerArgument(String name) {
        super(name);
    }

    public IntegerArgument(String name, Object defaultValue) {
        super(name, defaultValue);
    }

    public String getTypeDescriptor() {
        return TYPE;
    }

    /**
     * Ensure argument is a valid integer and return value as java.lang.Integer
     * object if so.  Range of integer determined by java language specification
     * for type "long".
     * @param value integer in string form
     * @return a java.lang.Integer object representing the given value
     * @throws IllegalArgumentException if the given value is not a valid
     *         integer.
     */
    protected Object validateArgumentValue(String value) {
        try {
            return new Integer(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Value is an invalid integer: " +
                                               value);
        }
    }

}