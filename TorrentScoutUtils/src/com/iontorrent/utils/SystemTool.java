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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/** This class gets some basic information from the system and is able to start a browser
with a given url */
public class SystemTool {

   // private static String os_name;
  //  private static String file_separator;
 //   private static String user_home;
 //   private static String os_arch;
    private static String drive = "C";
  
    // *****************************************************************
    // CONSTRUCTOR
    // *****************************************************************
    public SystemTool() {        
        
    }

    // *****************************************************************
    // GET METHODS
    // *****************************************************************
    public static String getOsName() {
        return getInfo("os.name");
    }

    public static void main(String[] args) {
        SystemTool s = new SystemTool();
        s.showSystemInfo();
    }

    public static String getFileSeparator() {
        return getInfo("file.separator");
    }

    public String getDriver() {
        return drive;
    }

    public String getUserHome() {
        return getInfo("user.home") + getFileSeparator();
    }

    public String getOsArch() {
        return getInfo("os.arch");
    }

    // *****************************************************************
    // INFO
    // *****************************************************************
    public static void showSystemInfo() {
        printInfo("java.version");
        printInfo("java.vendor");
        printInfo("java.vendor.url");
        printInfo("java.home");
        printInfo("java.vm.specification.version");
        printInfo("java.vm.specification.vendor");
        printInfo("java.vm.specification.name");
        printInfo("java.vm.version");
        printInfo("java.vm.vendor");
        printInfo("java.vm.name");
        printInfo("java.specification.version");
        printInfo("java.specification.vendor");
        printInfo("java.specification.name");
        printInfo("java.class.version");
        printInfo("java.class.path");
        printInfo("os.name");
        printInfo("os.arch");
        printInfo("os.version");
        printInfo("file.separator");
        printInfo("path.separator");
        printInfo("user.name");
        printInfo("user.home");
        printInfo("user.dir");
        printInfo("user.path");
        printInfo("test_arg");
        printInfo("run_name");
        
    }

    public static String getInfo(String prop) {
        return getInfo(prop, null);
    }
    public static String getProperty(String name) {
         String prop = getInfo("properties");
         
         String value = null;
        if (prop != null) {
            int p = prop.indexOf(name+"=");
            if (p>-1) {
                value = prop.substring(p);                
            }
        }
        if (value == null) value = SystemTool.getInfo(name);
        if (value != null) {
            int p = value.indexOf("=");
            if (p>-1) value = value.substring(p+1);
            p = value.indexOf("&");
            if (p>-1) value = value.substring(0, p);
        }
        return value;
        
    }

    public static String getInfo(String prop, String def) {
        String val = null;
        try {
            val = System.getProperty(prop, def);

        } catch (Exception e) {
            val = "<UNREADABLE>";
        }
        if (val != null) p("Got environment var "+prop+"="+val);
        return val;
    }

    public static boolean isDone(Process proc) {
        boolean done = false;
        try {
            if (proc.exitValue() >= 0) {
                return true;
            }
        } catch (Exception e) {
            err("process not done");
        }
        return false;
    }

    public static String runProgram(String exec) {
        Runtime runtime = Runtime.getRuntime();
        String res = "";
        try {
            err("Executing :" + exec);
            Process p = runtime.exec(exec);
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while (!isDone(p) || in.ready()) {
                    String line = in.readLine();
                    if (line != null) {
                        res += line.trim() + "\n";
                    }
                }
                in.close();
            } catch (IOException e) {
                err(e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            p(e.getMessage());
            e.printStackTrace();
        }
        p("result of program:" + exec + " is:" + res);
        return res;
    }

    public static int getFreeMemoryMB() {
        int freemB = (int) (Runtime.getRuntime().freeMemory() / 1000000L);

        return freemB;
    }

    public static int getTotalMemoryMB() {
        int freemB = (int) (Runtime.getRuntime().totalMemory() / 1000000L);

        return freemB;
    }

    public static String getIP() {
        String res = runProgram("ipconfig");
        int s = res.indexOf("IP Address");
        if (s < 0) {
            return null;
        }
        s = res.indexOf(":", s + 1);
        if (s < 0) {
            return null;
        }
        int e = res.indexOf("Subnet", s + 1);
        String ip = res.substring(s + 1, e).trim();
        p("ip is:" + ip);
        return ip;
    }

    /** Print a property or report a denied access. */
    private static final void printInfo(String prop) {
        System.out.print(prop);
        int i = prop.length();
        while (i < 32) {
            System.out.print(" ");
            i++;
        }
        System.out.println(getInfo(prop));
    }

    // ****************************************************************
    // *** FREE MEMORY
    // ****************************************************************
    public static boolean enoughMemoryAvailable() {
        // initialize local variables
        double minFreeMemPercent = 0.05; // 5% min free memory
        long minFreeMem = 1024 * 1024; // 1M min free
        Runtime runtime = Runtime.getRuntime();
        long totalSystemMemory = runtime.totalMemory();
        long freeSystemMemory = runtime.freeMemory();
        double freeMemPercent = (double) freeSystemMemory
                / (double) totalSystemMemory;

        return ((freeMemPercent > minFreeMemPercent) && (freeSystemMemory > minFreeMem));
    }

    public static long getFreeBytes() {
        return Runtime.getRuntime().freeMemory();
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(SystemTool.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        Logger.getLogger(SystemTool.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(SystemTool.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("SystemTool: " + msg);
        //Logger.getLogger( SystemTool.class.getName()).log(Level.INFO, msg, ex);
    }
}
