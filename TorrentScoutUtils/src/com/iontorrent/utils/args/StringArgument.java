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

public class StringArgument extends Argument {
    public static final String TYPE = "string";

    public StringArgument(String name) {
        super(name);
    }

    public StringArgument(String name, Object defaultValue) {
        super(name, defaultValue);
    }

    public String getTypeDescriptor() {
        return TYPE;
    }

    /**
 * Return the given value as is.  If the value is null, throw an
 * <code>IllegalArgumentException</code>.
 * @param value
 * @return
 * @throws IllegalArgumentException if given value is null.
 */
    protected Object validateArgumentValue(String value) {
        return value;
    }
}
