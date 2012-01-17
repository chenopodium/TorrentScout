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
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see .
 */
package com.iontorrent.utils.io;

import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.IOException;

public class BufferedRandomAccessFile extends RandomAccessFile {

    public BufferedRandomAccessFile(File file, String mode) throws
            FileNotFoundException {
        super(file, mode);
        bufferlength = 65536;
        bytebuffer = new byte[bufferlength];
        maxread = 0;
        buffpos = 0;
        sb = new StringBuilder("0");
    }
    private byte[] bytebuffer;
    private int bufferlength;
    private int maxread;
    private int buffpos;
    private StringBuilder sb;

    public int getbuffpos() {
        return buffpos;
    }

    @Override
    public int read() throws IOException {
        if (buffpos >= maxread) {
            maxread = readchunk();
            if (maxread == -1) {
                return -1;
            }
        }
        buffpos++;
        return bytebuffer[buffpos - 1];
    }

    public String readLine2() throws IOException {
        sb.delete(0, sb.length());
        int c = -1;
        boolean eol = false;
        while (!eol) {
            switch (c = read()) {
                case -1:
                case '\n':
                    eol = true;
                    break;
                case '\r':
                    eol = true;
                    long cur = getFilePointer();
                    if ((read()) != '\n') {
                        seek(cur);
                    }
                    break;
                default:
                    sb.append((char) c);
                    break;
            }
        }

        if ((c == -1) && (sb.length() == 0)) {
            return null;
        }
        return sb.toString();
    }

    @Override
    public long getFilePointer() throws IOException {
        return super.getFilePointer() + buffpos;
    }

    @Override
    public void seek(long pos) throws IOException {
        if (maxread != -1 && pos < (super.getFilePointer() + maxread) && pos > super.getFilePointer()) {
            Long diff = (pos - super.getFilePointer());
            if (diff < Integer.MAX_VALUE) {
                buffpos = diff.intValue();
            } else {
                throw new IOException("something wrong w/ seek");
            }
        } else {
            buffpos = 0;
            super.seek(pos);
            maxread = readchunk();
        }
    }

    private int readchunk() throws IOException {
        long pos = super.getFilePointer() + buffpos;
        super.seek(pos);
        int read = super.read(bytebuffer);
        super.seek(pos);
        buffpos = 0;
        return read;
    }
}