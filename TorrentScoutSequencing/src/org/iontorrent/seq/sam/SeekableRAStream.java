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

package org.iontorrent.seq.sam;


import com.iontorrent.utils.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.util.LineReader;
import net.sf.samtools.util.SeekableStream;
import org.openide.util.Exceptions;
/**
 *
 * @author Chantal Roth
 */
public class SeekableRAStream extends SeekableStream implements LineReader {

     RandomAccessFile in;
     File file;
     
     char peekchar= Character.UNASSIGNED;
     
    public SeekableRAStream(File file) {
        this.file = file;
        in = FileUtils.openRAFile(file);
        
    }
    public long getFilePointer() {
        try {
            return in.getFilePointer();
        } catch (IOException ex) {
            Logger.getLogger(SeekableRAStream.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( SeekableRAStream.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( SeekableRAStream.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( SeekableRAStream.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
//  System.out.println("SeekableRAStream: " + msg);
        //Logger.getLogger( SeekableRAStream.class.getName()).log(Level.INFO, msg, ex);
    }

    @Override
    public long length() {
        return file.length();
    }

    @Override
    public void seek(long l) throws IOException {
        peekchar = Character.UNASSIGNED;
        in.seek(l);
    }

    @Override
    public int read(byte[] bytes, int offset, int len) throws IOException {
        
       return in.read(bytes, offset, len);
    }

    @Override
    public void close()  {
        try {
            in.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public boolean eof() throws IOException {
        return in.getFilePointer()>= length();
    }

    @Override
    public String getSource() {
        return file.toString();
    }

    @Override
    public int read() throws IOException {
         int res = -1;
         if (Character.UNASSIGNED != peekchar) res =  in.read();
         else res =  peekchar;
         peekchar = Character.UNASSIGNED;
         return res;
    }

    @Override
    public String readLine() {
        try {
            
            String line= in.readLine();
           // p("Read:"+line);
            if (Character.UNASSIGNED != peekchar) line = peekchar+line;
            peekchar = Character.UNASSIGNED;
            return line;
        } catch (IOException ex) {
            err(ex.getMessage(), ex);
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public int getLineNumber() {
       return 0;
    }

    @Override
    public int peek() {
        try {
            int peek =  in.read();
            peekchar = (char)peek;
        //    p("Got peek char:"+peekchar);
            return peek;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return -1;
    }
}
