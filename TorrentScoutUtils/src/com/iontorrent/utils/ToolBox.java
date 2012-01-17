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
package com.iontorrent.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class ToolBox {

    private static int WIDTH = 80;

    // **********************************************************************
    // MANIPULATION OF LISTS AND STRINGS
    // **********************************************************************
    private static int getDelimPos(String desc, int start) {
        int sp = desc.indexOf(" ", start);
        if (sp < 0) {
            sp = 100000;
        }
        int dot = desc.indexOf(".", start);
        if (dot < 0) {
            dot = 100000;
        }
        int res = Math.min(sp, dot);
        int cm = desc.indexOf(",", start);
        if (cm < 0) {
            cm = 100000;
        }
        res = Math.min(cm, res);
        int min = desc.indexOf("-", start);
        if (min < 0) {
            min = 100000;
        }
        res = Math.min(min, res);
        int und = desc.indexOf("-", start);
        if (und < 0) {
            und = 100000;
        }
        res = Math.min(und, res);
        if (res >= 100000) {
            return -1;
        } else {
            return res + 1;
        }
    }

    public static String addNL(String desc, String nl, int length) {
        if (!nl.equals("\n") && !nl.equalsIgnoreCase("<br>")) {
            return desc;
        }
        StringBuffer nldesc = new StringBuffer();
        if (desc != null && desc.length() > length) {
            int start = 0;
            int pos = getDelimPos(desc, start + length);

            for (; pos > 0; pos = getDelimPos(desc, start + length)) {
                nldesc = nldesc.append(desc.substring(start, pos));
                nldesc = nldesc.append(nl);
                start = pos;
            }
            nldesc = nldesc.append(desc.substring(start));
            return nldesc.toString();
        } else {
            return desc;
        }
    }

    /**
     * change the string to html text with max line length (word wrap)
     * @param s the string
     * @param maxlinelen the max length for each line
     * @return
     */
    public static String getHtml(String s, int maxlineLen) {
        if (s == null) {
            return null;
        }
        StringBuilder strBuffer = new StringBuilder("<html>");

        if (s.toLowerCase().startsWith("<html>")) {
            s = s.substring(6);
        }
        if (s.toLowerCase().endsWith("</html>")) {
            s = s.substring(0, s.length() - 7);
        }
        s = replace(s, "<br>", "\n");

        boolean endNewLine = s.endsWith("\n");

        StringTokenizer lines = new StringTokenizer(s, "\n");
        while (lines.hasMoreElements()) {
            String line = lines.nextToken();
            if (line.length() <= maxlineLen) {
                strBuffer.append(line);
                strBuffer.append("<br>");
            } else {
                StringTokenizer st = new StringTokenizer(line, " ");
                line = "";
                while (st.hasMoreElements()) {
                    if (line.length() > maxlineLen) {
                        strBuffer.append(line);
                        strBuffer.append("<br>");
                        line = "";
                    }
                    line += st.nextToken() + " ";
                }
                strBuffer.append(line);
                strBuffer.append("<br>");
            }
        }
        if (endNewLine) {
            strBuffer.append("<br>");
        }
        String str = strBuffer.toString();
        str = str.substring(0, str.length() - 4) + "</html>";
        return str;
    }

    public static int getLineCount(String s) {
        if (s == null) {
            return 0;
        }
        s = replace(s, "<br>", "\n");
        StringTokenizer lines = new StringTokenizer(s, "\n", false);
        int count = 0;
        while (lines.hasMoreElements()) {
            count++;
            lines.nextElement();
            //	System.out.println(lines.nextElement());
        }
        return count;
    }

    /**
     * Change string to html text
     */
    public static String getHtml(String s) {
        String text = s.toLowerCase();
        if (text.toLowerCase().startsWith("<html>")) {
            return s;
        }
        s = s.replaceAll("\\n", "<br>");
        s = s.replaceAll("\n", "<br>");
        return "<html>" + s + "</html>";
    }

    public static String makeHtmlCompatible(String desc) {
        return makeHtmlCompatible(desc, true);
    }

    private static String makeHtmlCompatible(String desc, boolean donl) {
        if (desc == null) {
            desc = "";
        }
        desc = replace(desc, "&", "&amp;");
        desc = replace(desc, ">", "&gt;");
        desc = replace(desc, "<", "&lt;");
        desc = replace(desc, "%", " percent ");
        desc = replace(desc, "&lt;BR&gt;", "<br>");
        desc = replace(desc, "&lt;br&gt;", "<br>");
        desc = replace(desc, "&lt;B&gt;", "<b>");
        desc = replace(desc, "&lt;b&gt;", "<b>");
        desc = replace(desc, "&lt;/B&gt;", "</b>");
        desc = replace(desc, "&lt;/b&gt;", "</b>");
        desc = replace(desc, "&lt;I&gt;", "<i>");
        desc = replace(desc, "&lt;/I&gt;", "</i>");
        if (donl) {
            desc = replace(desc, "\\n", "<br>");
            desc = replace(desc, "\n", "<br>");
        }
        return desc;
    }

    public static String addNL(String desc, String nl) {
        if (desc == null || desc.length() < WIDTH) {
            return desc;
        }
        int pos = -1;
        int old = 0;
        int len = desc.length();
        StringBuffer newdesc = new StringBuffer(len + len / WIDTH + 1);
        int sp = desc.indexOf(" ", pos);
        for (; sp > pos;) {
            if (sp - old > WIDTH) {
                if (sp - old > 2 * WIDTH) {
                    sp = old + WIDTH;
                }
                newdesc = newdesc.append(desc.substring(old, sp));
                newdesc = newdesc.append(nl);
                old = sp;
            }
            pos = sp + 1;
            sp = desc.indexOf(" ", pos);
        }
        pos = old;
        while (len - pos > WIDTH) {
            newdesc = newdesc.append(desc.substring(pos, pos + WIDTH));
            newdesc = newdesc.append(nl);
            pos += WIDTH;
        }
        newdesc = newdesc.append(desc.substring(pos, len));
        return newdesc.toString();
    }

    public static String replace(String source, String tag, String with) {
        if (source == null || tag == null || tag.length() == 0 || with == null || tag.equals(with)) {
            return source;
        }
        if (tag.indexOf(with) >= 0) {
            String s = source;
            int tagpos = -1;
            while ((tagpos = s.indexOf(tag)) >= 0) {
                StringBuffer result = new StringBuffer();
                result.append(s.subSequence(0, tagpos));
                result.append(with);
                result.append(s.subSequence(tagpos + tag.length(), s.length()));
                s = result.toString();
            }
            return s;
        }
        StringBuilder result = new StringBuilder();
        int pos = 0;
        while (pos < source.length()) {
            int tagpos = source.indexOf(tag, pos);
            if (tagpos != -1) {
                if (tagpos > pos) {
                    result.append(source.substring(pos, tagpos));
                }
                result.append(with);
                pos = tagpos + tag.length();
            } else {
                result.append(source.substring(pos));
                break;
            }
        }
        return result.toString();
    }

    public static String getEnglishEnumeration(Vector list) {
        String result = "";
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                String element = (String) list.get(i);
                result += element;
                if (i + 2 < list.size()) {
                    result += ", ";
                }
                if (i + 2 == list.size()) {
                    result += " and ";
                }
            }
        }
        return result;
    }

    public static ArrayList<String> splitString(String line, String delim) {
        if (line == null) {
            err("splitString: line is null");
            return null;
        }
        ArrayList<String> res = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(line, delim);

        while (tokenizer.hasMoreElements()) {
            String next = tokenizer.nextToken().trim();
            if (next.startsWith("\"")) {
                next = next.substring(1, next.length());
            }
            if (next.endsWith("\"")) {
                next = next.substring(0, next.length() - 1);
            }
            res.add(next);
            //		 logger.debug("token "+res.size()+" is:"+next);
        }
        return res;
    }

    /**
     * Formats a java.util.Date object as a string according to the given
     * pattern
     * @param date the date to format
     * @param pattern the pattern in which to format the date
     * @return a formatted string representing the input date
     */
    public static String formatDate(Date date, String pattern) {
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(pattern);
        return formatter.format(date);
    }

    /** Returns the content of a file as a string.
     *  @param filename is converted to an url
     */
    public static String getFileAsString(String filename) {
        //	logger.debug("getFileAsString: File is "+filename);
        if (filename == null) {
            p("Filename is null");
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
            //		logger.debug("getFileAsString: Result is:\n"+res);
        } catch (FileNotFoundException e) {
            System.out.println("File " + filename + " not found");
        } catch (IOException e) {
            System.out.println("IO Exception");
        }
        return res.toString();
    }

    /** Returns the content of a file as a string.
     *  @param filename is converted to an url
     */
    public static byte[] getFileAsBytes(File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Get the size of the file
        long length = file.length();

        if (length > Integer.MAX_VALUE) {
            // File is too large
            Exception e = new Exception("Fils " + file + " is too large, larter than " + Integer.MAX_VALUE);
            e.printStackTrace();
            System.err.println(e.toString());
            return null;
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        try {
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            try {
                throw new IOException("Could not completely read file " + file.getName());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // Close the input stream and return bytes
        try {
            is.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * Copy the contents of <code>source</code> to the file represented by
     * <code>target</code>. 
     * @param source the file to be copied
     * @param target the destination of the copied content
     * @throws IOException if there is an error copying the file
     */
    public static void copyFile(File source, File target) throws IOException {
        BufferedInputStream contentSource = new BufferedInputStream(
                new FileInputStream(source));
        BufferedOutputStream contentTarget = new BufferedOutputStream(
                new FileOutputStream(target));
        byte[] content = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = contentSource.read(content)) != -1) {
            contentTarget.write(content, 0, bytesRead);
        }
        contentSource.close();
        contentTarget.flush();
        contentTarget.close();
    }

    public static String toFirstCharUpper(String value) {
        if (value == null || value.length() == 0) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    /**
     * Add <code>addAmount</code> to every element of <code>numbers</code>
     * and return it.
     * @throws NullPointerException if <code>numbers</code> is null.
     */
    public static void writeFile(String file, Object object) {
        byte[] result = null;
        try {
            java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream stream = new java.io.ObjectOutputStream(bout);
            stream.writeObject(object);
            result = bout.toByteArray();
            FileOutputStream fout = new FileOutputStream(new File(file));
            fout.write(result);
            fout.close();
        } catch (Exception e) {
            err(" writeFile: " + e.toString());
        }
    }

    // ************** DEBUG STUFF ******************
    public static long getObjectSize(Object obj) {
        long bytes = 0;
        try {
            java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream stream = new java.io.ObjectOutputStream(bout);
            stream.writeObject(obj);
            stream.flush();
            stream.close();
            bytes = bout.size();
        } catch (Exception e) {
            err("Response: Could not compute result size because: " + e.getMessage());
            e.printStackTrace();
        }
        return bytes;
    }

    public static void saveObject(Object o, File indexfile) {

        ObjectOutputStream outputStream = null;

        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(indexfile));
            outputStream.writeObject(o);

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            err("Could not write object", ex);
        } catch (IOException ex) {
            ex.printStackTrace();
            err("Could not write object", ex);
        } finally {
            //Close the ObjectOutputStream
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                err("Could not write object", ex);
            }
        }
    }

    public static Object readObject(File file) {

        ByteArrayInputStream fin = new ByteArrayInputStream(getFileAsBytes(file));
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(fin);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            err("Could not read object from file " + file + ":" + e);
        }
        Object res = null;
        try {
            res = in.readObject();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            err("Could not read object from file " + file + ":" + e);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            err("Could not read object from file " + file + ":" + e);
        }
        try {
            in.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            err("Could not close " + file + ":" + e);
        }
        return res;
    }

    public static Object deepClone(Object o) {
        Object res = null;
        try {
            ByteArrayOutputStream fos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(o);
            out.flush();
            out.close();
            byte[] obj = fos.toByteArray();
            fos.close();
            ByteArrayInputStream fin = new ByteArrayInputStream(obj);
            ObjectInputStream in = new ObjectInputStream(fin);
            res = in.readObject();
            in.close();
        } catch (Exception e) {
            err(e.getMessage());
            e.printStackTrace();
        }
        //p("result of deep clone:" + res);
        return res;
    }

    public static boolean writeBinaryToFile(String filename, String content) {
        BufferedWriter fout = null;
        try {
            fout = new BufferedWriter(new FileWriter(filename));

            fout.write(content, 0, content.length());
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

    public static String getGetterForField(String field) {
        return getGetterForField(field, false);
    }

    public static String getGetterForField(String field, boolean useIsPrefix) {
        StringBuilder getMethod = new StringBuilder(field);
        getMethod.setCharAt(0, Character.toUpperCase(getMethod.charAt(0)));
        getMethod.insert(0, useIsPrefix ? "is" : "get");
        return getMethod.toString();

    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(ToolBox.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        Logger.getLogger(ToolBox.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(ToolBox.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("ToolBox: " + msg);
        //Logger.getLogger( ToolBox.class.getName()).log(Level.INFO, msg, ex);
    }
}
