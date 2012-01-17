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

import java.io.File;

public class FileArgument extends Argument {

    public static final String TYPE = "file";

    public FileArgument(String name) {
        super(name);
    }

    public FileArgument(String name, Object defaultValue) {
        super(name, defaultValue);
    }

    public String getTypeDescriptor() {
        return TYPE;
    }

    protected Object validateArgumentValue(String value) {
        File fileArgument = new File(value);
        if(!fileArgument.exists()) {
            throw new IllegalArgumentException("File '" + value +
                                               "' does not exist.");
        }
        return fileArgument;
    }
}