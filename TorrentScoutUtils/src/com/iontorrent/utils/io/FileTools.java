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

import com.iontorrent.utils.ExtensionFileFilter;
import com.iontorrent.utils.ProgressListener;
import com.iontorrent.utils.StringTools;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Chantal Roth
 */
public class FileTools {

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 16;

    public static String addSlashOrBackslash(File fdir) {
        return addSlashOrBackslash(fdir.toString());
    }

    public static String addSlashOrBackslash(String dir) {
        if (dir == null) {
          //  err("addSlashOrBackslash: dir is null!");
            return "";
        }
        if (!dir.endsWith("/") && !dir.endsWith("\\")) {
            if (dir.indexOf("\\") > -1) {
                dir += "\\";
            } else {
                dir += "/";
            }
        }
        return dir;
    }

    public static String getFile(String title, String ext, String val) {
        return getFile(title, ext, val, false);
    }
    public static String getFile(String title, String ext, String val, boolean toSave) {
        JFileChooser cc = new JFileChooser();
        cc.setDialogTitle(title);

        
        if (val != null) {
            File f = new File(val);
           // if (!dir.isDirectory()) dir 
            cc.setSelectedFile(f);
            if (f.isDirectory()) cc.setCurrentDirectory(f);
            else if (f.getParentFile()!= null) cc.setCurrentDirectory(f.getParentFile());
        }
        cc.setVisible(true);
        String[] Ext = new String[]{ext};
        if (ext.indexOf(",") > 0) {
            Ext = StringTools.parseList(ext, ", ").toArray(Ext);

        }
        ExtensionFileFilter filter1 = new ExtensionFileFilter(Ext[0] + " files", Ext);
        cc.setFileFilter(filter1);

        String res = val;
        int ans = 0;
        if (!toSave) ans = cc.showOpenDialog(null);
        else  ans = cc.showSaveDialog(null);
        if (ans == JOptionPane.OK_OPTION) {
            File f = cc.getSelectedFile();

            
            if (!f.exists()) {
                if (!toSave) JOptionPane.showMessageDialog(new JFrame(), f + " does not exist - please select an existing file");
            }
            else {
                if (toSave) {
                    int ok= JOptionPane.showConfirmDialog(new JFrame(), "Would you want to overwrite this file?");
                    if (ok != JOptionPane.YES_OPTION) return null;
                }
            } 
            if (f.isDirectory()) {
                JOptionPane.showMessageDialog(null, f + " is a directory - please select a file");
                //f.get
            } else {
                res = f.getAbsolutePath();

            }

        }
        else res = null;
        return res;
    }

    public static String getDir(String title, File val) {
        JFileChooser cc = new JFileChooser();
        if (val != null) {
            if (!val.isDirectory()) val = val.getParentFile();
            cc.setSelectedFile(val);
            cc.setCurrentDirectory(val);

        }
        cc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        cc.setDialogTitle(title);
        cc.setAcceptAllFileFilterUsed(false);
        cc.setVisible(true);
        String res = val.toString();

        int ans = cc.showOpenDialog(null);
        if (ans == JOptionPane.OK_OPTION) {
            File dir = cc.getSelectedFile();
            if (!dir.isDirectory()) {
                JOptionPane.showMessageDialog(null, dir + " does not look like a directory - please select a directory");
                //f.get
            } else {
                res = dir.getAbsolutePath();
                if (res.endsWith("/")) {
                    res += "/";
                }
            }
        }
        return res;
    }

    public static boolean copyFile(File f1, File f2) {
        p("Copying file " + f1 + " to " + f2);
        try {

            InputStream in = new FileInputStream(f1);

            //For Append the file.
//	      OutputStream out = new FileOutputStream(f2,true);

            //For Overwrite the file.
            OutputStream out = new FileOutputStream(f2);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            p("File "+f1+" copied.");
        } catch (FileNotFoundException ex) {
            err(ex);
            return false;

        } catch (IOException e) {
            err(e);
            return false;
        }
        return true;
    }

     public static boolean copyUrl(URL f1, File f2) {
         return copyUrl(f1, f2, null, 1024*1024);
     }
    public static boolean copyUrl(URL f1, File f2, ProgressListener listener, int BUFF_SIZE) {
        p("Copying url " + f1 + " to file \n" + f2 + ", buf size: " + BUFF_SIZE);
        if (f1 == null || f2 == null) return false;
        try {
            URLConnection uc = f1.openConnection();
            InputStream in = uc.getInputStream();
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f2));
            copyLarge(in, out, listener, BUFF_SIZE);
            out.flush();
            out.close();
            in.close();
            p("URL "+f1+" copy done.");
        } catch (FileNotFoundException ex) {
            err(ex);
            return false;

        } catch (IOException e) {
            err(e);
            return false;
        }
        return true;
    }

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        return copyLarge(input, output, null, 1024 * 1024);
    }

    public static long copyLarge(InputStream input, OutputStream output, ProgressListener listener, int BUFF_SIZE)
            throws IOException {
        byte[] buffer = new byte[BUFF_SIZE];
        long count = 0;
        int n = 0;

        float prog = 0.0f;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
            if (listener != null) {
                if (prog >= 100) {
                    prog = 99;
                }
                listener.setProgressValue((int)(prog+=0.1));

            }
        }
        return count;
    }

    /** Returns the content of a file as a string.
    
     */
    public static String getFileAsString(String filename) {
        //	p("getFileAsString: File is "+filename);
        if (filename == null) {
            warn("Filename is null");
            return null;
        }
        StringBuilder res = new StringBuilder();

        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            while (in.ready()) {
                // res.append((char)in.read());
                res.append(in.readLine());
                res.append("\n");
            }
            in.close();
            //		p("getFileAsString: Result is:\n"+res);
        } catch (FileNotFoundException e) {
            err(e);
        } catch (IOException e) {
            err(e);
        }
        return res.toString();
    }

    public static ArrayList<String> getFileAsArray(String filename) {
        //	p("getFileAsString: File is "+filename);
        if (filename == null || !(new File(filename)).exists()) {
            warn("Filename is null or does not exist");
            return null;
        }
        ArrayList<String> res = new ArrayList<String>();

        try {

            BufferedReader in = new BufferedReader(new FileReader(filename));

            while (in.ready()) {
                // res.append((char)in.read());
                String line = in.readLine();
                if (line != null) {
                    line = line.trim();
                } else {
                    line = "";
                }
                res.add(line);

            }
            in.close();
            //		p("getFileAsString: Result is:\n"+res);
        } catch (FileNotFoundException e) {
            err(e);
        } catch (IOException e) {
            err(e);
        }
        return res;
    }

    public static ArrayList<String> getFileAsArray(URL filename) {
        //	p("getFileAsString: File is "+filename);
        if (filename == null) {
            warn("URL is null");
            return null;
        }
        ArrayList<String> res = new ArrayList<String>();

        try {
            URLConnection uc = filename.openConnection();
            InputStreamReader input = new InputStreamReader(uc.getInputStream());
            BufferedReader in = new BufferedReader(input);

            while (in.ready()) {
                // res.append((char)in.read());
                String line = in.readLine();
                if (line != null) {
                    line = line.trim();
                } else {
                    line = "";
                }
                res.add(line);

            }
            in.close();
            //		p("getFileAsString: Result is:\n"+res);
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return res;
    }

    public static boolean writeStringToFile(String filename, String content) {
        PrintWriter fout = null;
        try {
            fout = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
            fout.print(content);
            fout.flush();
            fout.close();
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("File " + filename + " not found");
        } catch (IOException e) {
            System.out.println("IO Exception");
        }
        return false;
    }

    public static boolean writeStringToFile(String filename, StringBuffer content) {
        PrintWriter fout = null;
        try {
            fout = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
            fout.print(content);
            fout.flush();
            fout.close();
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("File " + filename + " not found");
        } catch (IOException e) {
            System.out.println("IO Exception");
        }
        return false;
    }

    public static boolean writeArrayToFile(String filename, ArrayList<String> content) {
        PrintWriter fout = null;
        try {
            fout = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
            for (int i = 0; i < content.size(); i++) {
                fout.println(content.get(i));
            }
            fout.flush();
            fout.close();
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("File " + filename + " not found");
        } catch (IOException e) {
            System.out.println("IO Exception");
        }
        return false;
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(FileTools.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(Exception ex) {
        Logger.getLogger(FileTools.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
    }

    private static void err(String msg) {
        Logger.getLogger(FileTools.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(FileTools.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("FileTools: " + msg);
        //Logger.getLogger( FileTools.class.getName()).log(Level.INFO, msg, ex);
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
}
