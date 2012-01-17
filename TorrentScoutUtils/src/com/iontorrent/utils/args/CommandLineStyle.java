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

/**
 * This class represents a style for specifying command-line arguments.  It
 * is simple and simply specifies a prefix for the argument name, i.e. '-' or
 * '--', and a separator between the argument name and value.  Examples
 * of separators are:<br>
 * No separator (empty string): -Dname=value ('D' is the name, 'name=value' is the value)<br>
 * Equals separator: -file=sample.txt<br>
 * Space separator: -file sample.txt<br>
 *
 * Currently, the class is just used to produce strings for command-line
 * usage displays.
 *
 * @author Derek Guist
 */
public class CommandLineStyle {
    private String namePrefix;
    private String nameValueSeparator;

    /**
     * A CommandLineStyle object for arguments that look like:<br>
     * <code>-name &lt;value&gt;</code>
     */
    public static final CommandLineStyle NAME_SPACE_VALUE =
                            new CommandLineStyle("-", " ");

    /**
     * A CommandLineStyle object for arguments that look like:<br>
     * <code>-name=&lt;value&gt;</code>
     */
    public static final CommandLineStyle NAME_EQUALS_VALUE =
                            new CommandLineStyle("-", "=");


    public CommandLineStyle(String namePrefix, String nameValueSeparator) {
        if(namePrefix == null || nameValueSeparator == null) {
            throw new NullPointerException("namePrefix and " +
                                           "nameValueSeparator must not be " +
                                           "null");
        }
        this.namePrefix = namePrefix;
        this.nameValueSeparator = nameValueSeparator;
    }

    public String format(String name, String valueDescriptor) {
        StringBuffer argumentString = new StringBuffer();
        argumentString.append(namePrefix);
        if(name != null) argumentString.append(name);
        // Only add separator if there is actually a value to separate.
        if(valueDescriptor == null) return argumentString.toString();
        argumentString.append(nameValueSeparator);
        argumentString.append("<").append(valueDescriptor).append(">");
        return argumentString.toString();
    }
}