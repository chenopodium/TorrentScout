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

import com.iontorrent.utils.ProgressListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class FileUtils {

    /** number of bytes for each datatype */
    public static final short UINT8 = 1;
    public static final short UINT16 = 2;
    public static final short UINT32 = 4;
    public static final short UINT64 = 8;
    public static final short INT8 = 1;
    public static final short INT16 = 2;
    public static final short INT32 = 4;
    public static final short INT64 = 8;
    public static final short LONG = 8;
    public static final short CHAR = 1;

    public static File findAndCopyFileFromUrlTocache(String file, String cache_dir, String source_dir, 
            boolean copyAlsoIfSourceIsFile, boolean copyAlsoIfCannotWriteInSource, ProgressListener listener, int BUFF_SIZE) {
        cache_dir = FileTools.addSlashOrBackslash(cache_dir);
        source_dir = FileTools.addSlashOrBackslash(source_dir);
        if (!FileTools.isUrl(source_dir) && !copyAlsoIfSourceIsFile) {
            File f = new File(source_dir);
            if (!copyAlsoIfCannotWriteInSource || f.canWrite() ){                                          
                f = new File(source_dir + file);
            //    p("findAndCopyFileFromUrlTocache: Dir " + source_dir + " is NOT a url, so won't have to copy file. Will return " + f);
                return f;
            }
        }
        File f = new File(cache_dir + file);
        if (f.exists()) {
            p(file + " is already in cache");
            return f;
        }
        if (FileUtils.isUrl(source_dir + file)) {
            try {
                // find matching url, then copy to cache dir!
                URL url = new URL(source_dir + file);
                p("Checking  url " + url);
                if (FileUtils.exists(url.toString())) {
                    //p("Copying " + url + " to cache " + f+", bufsize="+BUFF_SIZE);
                    
                    boolean ok = FileTools.copyUrl(url, f, listener, BUFF_SIZE);
                    if (ok && f.exists()) {
                        p("copy ok, returning " + f);
                        return f;
                    } else {
                        err("Copy not ok, returning null");
                        return null;
                    }
                } else {
                    err(" url " + url + " does not seem to exist");
                    return null;
                }
            } catch (Exception ex) {
                err("Was not able to open url " + (source_dir + file), ex);
                
                return null;
            }
        } else {
            p("path is a a file, not a url, but file " + f + " does not exist");
        }
        return null;
    }

    /** open a binary file */
    public static DataInputStream openFile(File file) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(file), 1024 * 256));
        } catch (Exception ex) {
           warn("Could not open file " + file+":"+ex.getMessage());
        }
        return in;
    }
    /** open a binary file */
    public static DataInputStream openFileToRead(File file, int approxSize) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(file),Math.max(1024, approxSize)));
        } catch (Exception ex) {
           warn("Could not open file to read " + file+":"+ex.getMessage());
        }
        return in;
    }
     /** open a binary file */
    public static DataOutputStream openFileToWrite(File file, int approxSize) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file), Math.max(1024, approxSize)));
        } catch (Exception ex) {
           warn("Could not open file to write " + file+":"+ex.getMessage());
        }
        return out;
    }

    public static DataInputStream openFileOrUrl(String path) {
        if (isUrl(path)) {
            try {
                return openUrl(new URL(path));
            } catch (MalformedURLException ex) {
                err("Malformed url: "+path+":"+ex.getMessage());
                return null;
            }
        } else {
            return openFile(new File(path));
        }
    }

    public static long getSize(String fileorUrl) {

        if (FileTools.isUrl(fileorUrl)) {
            try {
                HttpURLConnection.setFollowRedirects(false);
                // note : you may also need
                //        HttpURLConnection.setInstanceFollowRedirects(false)
                HttpURLConnection con =
                        (HttpURLConnection) new URL(fileorUrl).openConnection();
                con.setRequestMethod("HEAD");
                return con.getContentLength();              
            } catch (Exception e) {
                p(e.getMessage());
                return -1;
            }
        } else {
            return new File(fileorUrl).length();
        }
    }

    public static DataInputStream openHttpUrl(URL url) {
        HttpURLConnection.setFollowRedirects(false);
        // note : you may also need
        //        HttpURLConnection.setInstanceFollowRedirects(false)

        DataInputStream in = null;
        try {
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            //  uc.setRequestMethod("GET");
            uc.setDoInput(true);
           // p("Opened url " + url + ", authority: " + url.getAuthority() + ", user: " + url.getUserInfo());

            in = new DataInputStream(new BufferedInputStream(uc.getInputStream(), 1024 * 256));

        } catch (IOException ex) {
            err("Error reading "+url+":", ex);
        }


        return in;
    }

    public static DataInputStream openUrl(URL url) {
        if (url.toString().startsWith("http")) {
            return openHttpUrl(url);
        }
        DataInputStream in = null;

        try {
            URLConnection uc = url.openConnection();
            uc.connect();
           // p("Opened url " + url + ", authority: " + url.getAuthority() + ", user: " + url.getUserInfo());
            in = new DataInputStream(new BufferedInputStream(uc.getInputStream(), 4096));
        } catch (Exception ex) {
           err("Could not open url " + url+":"+ex.getMessage());
        }
        return in;
    }

    static boolean existsHttp(String URLName) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con =
                    (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            con.setConnectTimeout(10000);
            int code = con.getResponseCode();
            
            boolean ex= (code == HttpURLConnection.HTTP_OK);
            p("Exists url " + URLName + ", code: " + code+"= "+ ex);
            return ex;
        } catch (Exception e) {
            p("URL does not exist: "+e.getMessage());
            return false;
        }
    }

    public static boolean exists(String fileorUrl) {
        // p("Checking if " + fileorUrl + " exists");
        if (isUrl(fileorUrl)) {
            if (fileorUrl.startsWith("http")) {
                return existsHttp(fileorUrl);
            }
            else {
                try {
                    URL url = new URL(fileorUrl);
                    //  p(url + " looks ok");
                    try {
                        URLConnection uc = url.openConnection();
                        uc.setDoInput(true);;
                        uc.connect();
                        //p("Exists: url " + url );
                        return true;
                    } catch (Exception ex) {
                        p("Could not open url " + url + ":" + ex.getMessage());
                    }
                    return false;
                } catch (Exception ex) {
                    p(fileorUrl + " is not a valid  url");
                    return false;
                }
            }
        } else {
            File f = new File(fileorUrl);
            return f.exists();
        }
    }

    public static boolean isUrl(String path) {
        if (path == null || path.length() < 1) {
            return false;
        }
        int pos = path.indexOf("://");
        if (pos > 0 && pos < 10) {
            return true;
        } else {
            return false;
        }
    }

    /** open a binary file for writing */
    public static DataOutputStream openOutputStream(File file) {
        DataOutputStream s = null;

        try {
            s = new DataOutputStream(new FileOutputStream(file));

        } catch (FileNotFoundException ex) {
            err("Could not open file to write " + file, ex);
        }
        return s;
    }

    /** open a binary file for reading */
    public static BufferedInputStream openFile(File file, int buf) {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file), buf);


        } catch (FileNotFoundException ex) {
            err("Could not open file " + file, ex);
        }
        return in;
    }

    /** open a binary file */
    public static BufferedRandomAccessFile openBRAFile(File file) {
        BufferedRandomAccessFile in = null;
        try {
            in = new BufferedRandomAccessFile(file, "r");
        } catch (FileNotFoundException ex) {
            err("Could not open file " + file, ex);
        }
        return in;
    }

    public static float getFloatLittle(DataInputStream in) {
        byte[] b = new byte[4];
        ByteBuffer bb = ByteBuffer.allocate(4); // aus der java.nio
        try {
            in.read(b, 0, 4);


        } catch (IOException ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

        bb.put(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.flip();
        float f = bb.getFloat();
        return f;
    }

    public static float getFloatLittle(RandomAccessFile in) {
        byte[] b = new byte[4];
        ByteBuffer bb = ByteBuffer.allocate(4); // aus der java.nio
        try {
            in.read(b, 0, 4);
        } catch (IOException ex) {
            err("Cannot read float", ex);
        }

        bb.put(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.flip();
        float f = bb.getFloat();
        return f;
    }

    /** open a binary file */
    public static RandomAccessFile openRAFile(File file) {
        RandomAccessFile in = null;
        try {
            in = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException ex) {
            err( "Could not open file " + file, ex);
        }
        return in;
    }

    public static long getUInt32(DataInputStream is) throws IOException {
        long l = is.readInt() & 0xFFFFFFFFL; // mask with 32 one-bits
       // System.out.println("getUInt32: "+Long.toHexString(l));
        return l;
    }

    public static long getUInt64(DataInputStream is) throws IOException {
        return is.readLong() & 0xFFFFFFFFFFFFFFFFL; // mask with 64 one-bits
    }

    public static long getUInt64(RandomAccessFile is) throws IOException {
        return is.readLong() & 0xFFFFFFFFFFFFFFFFL; // mask with 64 one-bits
    }

    public static int getUInt8(DataInputStream is) throws IOException {
        return is.readUnsignedByte();
    }

    public static int getUInt8(RandomAccessFile is) throws IOException {
        return is.readUnsignedByte();
    }

    public static int getInt8(DataInputStream is) throws IOException {
        byte a = is.readByte();
        byte b = is.readByte();
        return (short) ((a << 8) | (b & 0xff));
    }

    public static int getUInt8(short val) {
        byte a = (byte) val;
        byte b = (byte) (val >> 8);
        return toUnsignedShort(a, b);
    }

    public static long getUInt32(RandomAccessFile is) throws IOException {
        return is.readInt() & 0xFFFFFFFFL; // mask with 32 one-bits
    }

    // 2-byte number 16 bit
    public static int SHORT_little_endian_TO_big_endian(int i) {
        return ((i >> 8) & 0xff) + ((i << 8) & 0xff00);
    }

    // 4-byte number, 32 bit
    public static int INT_little_endian_TO_big_endian(int i) {
        return ((i & 0xff) << 24) + ((i & 0xff00) << 8) + ((i & 0xff0000) >> 8) + ((i >> 24) & 0xff);
    }

    public static int getUInt16(DataInputStream is) throws IOException {
        return is.readUnsignedShort();
    }
   

    public static int getUInt16(RandomAccessFile is) throws IOException {
        return is.readUnsignedShort();
    }

    public static char getChar(DataInputStream is) throws IOException {
        return is.readChar();
    }

    public static char getCharLittle(DataInputStream is) throws IOException {
        return (char) getUInt16Little(is);
    }

    public static char getChar(RandomAccessFile is) throws IOException {
        return is.readChar();
    }

    public static char getCharLittle(RandomAccessFile is) throws IOException {
        return (char) getUInt16Little(is);
    }

    public static int getUInt32Little(DataInputStream is) throws IOException {
        return INT_little_endian_TO_big_endian((int) getUInt32(is));
    }

    public static int getUInt32Little(RandomAccessFile is) throws IOException {
        return INT_little_endian_TO_big_endian((int) getUInt32(is));
    }

    public static int getUInt16Little(DataInputStream is) throws IOException {
        // return (char) SHORT_little_endian_TO_big_endian(getUInt16(is));
        byte b1 = is.readByte();
        byte b2 = is.readByte();
        // now reverse and convert to 
        return toUnsignedShort(b1, b2);
    }

    public static int getUInt16Little(RandomAccessFile is) throws IOException {
        // return (char) SHORT_little_endian_TO_big_endian(getUInt16(is));
        byte b1 = is.readByte();
        byte b2 = is.readByte();
        // now reverse and convert to 
        return toUnsignedShort(b1, b2);
    }

    public static short toShort(byte b1, byte b2) {
        return (short) ((b1 & 0xFF) | ((b2 & 0xFF) << 8));
    }

    public static int toUnsignedShort(byte b1, byte b2) {
        return (b1 & 0xFF) | ((b2 & 0xFF) << 8);
    }

    private static void p(String string) {
        System.out.println("FileUtils:" + string);
    }

    public static boolean canWrite(String dir) {
        if (dir == null) return false;
        if (isUrl(dir)) return false;
                
       File d =  new File(dir);
       boolean ok =  d.exists() && d.canWrite();
       // now try to write somethingd
       dir = FileTools.addSlashOrBackslash(dir);
       File f = new File(dir+"test"+(int)(Math.random()*1000)+".xxx");
        try {
            ok = f.createNewFile();
        } catch (IOException ex) {
            ok = false;
           // err("Cannot write to "+dir, ex);
        }
       f.delete();
       return ok;
    }

    public char[] getUnsignedBytes(byte[] b) {
        int length = b.length;
        char[] res = new char[length];
        for (int step = 0; step < length; step++) {
            int byteValue = new Integer(b[step]).intValue();
            if (byteValue < 0) {
                byteValue = b[step] & 0x80;
                byteValue += b[step] & 0x7F;
            }
            res[step] = (char) byteValue;
        }
        return res;
    }

    public static int toInt(byte b1, byte b2, byte b3, byte b4) {
        return (b1 & 0xFF) | ((b2 & 0xFF) << 8) | ((b3 & 0xFF) << 16) | ((b4 & 0xFF) << 24);
    }

    public static long toUnsignedInt(byte b1, byte b2, byte b3, byte b4) {
        return ((b1 & 0xFF) | ((b2 & 0xFF) << 8) | ((b3 & 0xFF) << 16) | ((b4 & 0xFF) << 24)) & 0xFFFFFFFFl;
    }

    /** 
    ion_read_padding(FILE *fp, uint32_t n)
    {
    char padding[8]="\0";
    n = (n & 7); // (n % 8)
    if(0 != n) {
    n = 8 - n; // number of bytes of padding
    if(NULL != fp) {
    if(n != fread(padding, sizeof(char), n, fp)) {
    ion_error(__func__, "fread", Exit, ReadFileError);
    }
    }
    }
    return n;
    }*/
    public static int readPadding(DataInputStream in, int n) {

        n = n % 8;
        if (n == 0) {
            return 0;
        }
        int left = 8 - n; // number of bytes of padding
        //   p("read "+n+" bytes, need additional "+left);
        for (int i = 0; i < left; i++) {
            try {
                in.readByte();


            } catch (IOException ex) {
                Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return left;
    }

    /** REFACTOR */
    public static int readPadding(RandomAccessFile in, int n) {

        n = n % 8;
        if (n == 0) {
            return 0;
        }
        int left = 8 - n; // number of bytes of padding
        //   p("read "+n+" bytes, need additional "+left);
        for (int i = 0; i < left; i++) {
            try {
                in.readByte();

            } catch (IOException ex) {
                //err(, ex);
            }
        }
        return left;
    }

    private static void err(String string) {
        Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, string);
    }
      private static void err(String string, Exception ex) {
        Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, string, ex);
    }
       private static void warn(String string) {
        Logger.getLogger(FileUtils.class.getName()).log(Level.WARNING, string);
    }
}
