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

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * This class represents a date argument.  Validation is done using pattern
 * strings as described in the documentation for
 * <code>java.text.SimpleDateFormat</code>.  The default pattern is dd-MMM-yyyy.
 * @author Derek Guist
 */
public class DateArgument extends Argument {

    private String datePattern = DEFAULT_PATTERN;
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_PATTERN);

    public static final String DEFAULT_PATTERN ="dd-MMM-yyyy";


    public DateArgument(String name) {
        super(name);
    }

    public DateArgument(String name, Object defaultValue) {
        super(name, defaultValue);
    }

    /**
     * Set the pattern by which this object validates date arguments.
     * @see java.text.SimpleDateFormat
     * @param pattern a date pattern as described in java.text.SimpleDateFormat
     * @throws IllegalArgumentException if pattern is not valid as described
     *         in java.text.SimpleDateFormat
     * @throws NullPointerException if pattern is null
     */
    public void setDatePattern(String pattern) {
        dateFormat.applyPattern(pattern);
        this.datePattern = pattern;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public String getTypeDescriptor() {
        return datePattern;
    }

    /**
     * Ensure string is a valid date according to the date pattern
     * associated with this object.  Return a java.util.Date object if it is,
     * otherwise throw an exception.
     * @param value
     * @return java.util.Date representing given value
     * @throws IllegalArgumentException if given value is not a date matching
     *         this object's date pattern
     */
    protected Object validateArgumentValue(String value) {
        try {
            return dateFormat.parse(value);
        } catch (ParseException parseError) {
            throw new IllegalArgumentException("Date value '" + value +
                                              "' not valid, pattern must be '" +
                                              datePattern + "'");
        }
    }
}