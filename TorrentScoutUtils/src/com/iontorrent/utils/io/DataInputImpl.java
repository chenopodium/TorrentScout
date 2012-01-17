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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.iontorrent.utils.io;


import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class DataInputImpl implements DataInput {

    DataInputStream in;
   
    public DataInputImpl(File file) {
       in =  FileUtils.openFile(file);
    }

     public DataInputImpl(DataInputStream in) {
       this.in =  in;
    }
/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( DataInputImpl.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( DataInputImpl.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( DataInputImpl.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("DataInputImpl: " + msg);
        //Logger.getLogger( DataInputImpl.class.getName()).log(Level.INFO, msg, ex);
    }

    @Override
    public long readLong() throws IOException {
       return in.readLong();
    }

    @Override
    public int readInt() throws IOException {
        return in.readInt();
    }
     @Override
    public void close() throws IOException {
        in.close();
    }
}
