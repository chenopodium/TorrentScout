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

/**
 * A file list argument represents a file.  That file may itself be an input
 * file for some process or it may be a <i>list</i> of input files for some
 * process. If it is a list of input files, then the file name (minus any path
 * information) should be preceeded by a '@' symbol.
 */
public class FileListArgument extends Argument {

    public static final String TYPE = "filelist";

    public FileListArgument(String name) {
        super(name);
    }

    public FileListArgument(String name, Object defaultValue) {
        super(name, defaultValue);
    }

    public String getTypeDescriptor() {
        return TYPE;
    }

    /**
     * Check if the file or file-list exists. The file returned will be for
     * the filename as passed in, including the '@' for a file-list.
     */
    protected Object validateArgumentValue(String value) {
        File fileArgument = new File(value);
        File checkFile = fileArgument;
        if(fileArgument.getName().startsWith("@")) {
           checkFile = new File(fileArgument.getParentFile(),
                                fileArgument.getName().substring(1));
        }
        if(!checkFile.exists()) {
            throw new IllegalArgumentException("File '" + value +
                                               "' does not exist.");
        }
        return fileArgument;
    }
}
