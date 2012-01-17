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
package com.iontorrent.utils.system;

import java.io.*;

public class Executer {
    private static boolean DEBUG = false;

    private StringBuffer out = new StringBuffer();
    private StringBuffer err = new StringBuffer();
    private Process process;
    private int exitval;
    
    String env[] = null;
    //{"PATH=.;C:\\java\\jdk142\\bin",
     //       "CLASSPATH=E:\\Tools\\eclipse3\\workspace\\BioSphere\\classes;C:\\computehost\\lib\\xml.jar;C:\\computehost\\lib\\log4j.jar;"
    //};
    class InputThread extends Thread {

        InputStream is;
        String type;

        InputThread(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        public void run() {
            readOutput(is, type);
        }

    }

    private String readOutput(InputStream is, String type) {
      
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
          //  p("reading..");
            while ((line = br.readLine()) != null) {
               if (type.equalsIgnoreCase("ERR")) err = err.append(line+"\n");
               else out = out.append(line+"\n");
                
           //     p("line is: "+line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        if (type.equalsIgnoreCase("ERR")) return err.toString();
        else return out.toString();
    }

    public static void execute(String ex) {
        String[] args = new String[1];
        args[0] = ex;
        Executer e = new Executer(args);
    }
    private String checkCmd(String cmd) {
        String osName = System.getProperty("os.name");
      
//        if ((!cmd.startsWith("java") &&  !cmd.startsWith("echo"))
//                || cmd.startsWith("cmd")) {
//            p("hack: '"+cmd+"' is not java or has a cmd, not adding cmd");
//            return cmd;
//        }
        if (cmd.startsWith("cmd") || cmd.startsWith("hostname")) {
            p("'"+cmd+"' has a cmd/hostname, not adding cmd");
            return cmd;
        }
        if (osName.equals("Windows NT") || osName.equals("Windows 2000")) {
            cmd = "cmd.exe /C "+cmd;
        } else if (osName.indexOf("Windows") > 0) {
            cmd = "command.com /C "+cmd;
        } 
        return cmd;
    }
    public String exec(String[] args) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
            b = b.append(args[i]+" ");
        }
        return exec(b.toString().trim());
    }
    public String exec(String arg) {
        String res = null;
        try {
            String cmd = checkCmd(arg);
            Runtime rt = Runtime.getRuntime();
           
            System.out.println("executing: \n"+cmd);   
           //  p("env: "+env.toString());
            process = rt.exec(cmd, env);
         
            InputThread errorGobbler = new InputThread(process.getErrorStream(), "ERR");
            InputThread outputGobbler = new  InputThread(process.getInputStream(), "");
            errorGobbler.start();
            outputGobbler.start();

            exitval = process.waitFor();
            p("ExitValue: " + exitval);
         //   p("proc: " + proc);
         //   proc.wait(2000);
           // err = readOutput(proc.getErrorStream());
          //  out = readOutput(proc.getInputStream());

        } catch (Throwable t) {
            t.printStackTrace();
        }
        p("err: "+err);
        p("out: "+out);
        return out.toString();
    }

    public Executer(String[] args, String[] env) {
        this.env = env;
        exec(args);
    }

    public Executer(String[] args) {
        exec(args);
    }
    // *****************************************************************
    // MAIN (for testing)
    // *****************************************************************

    public Executer(String cmdline) {
        exec(cmdline);
    }

    public static void main(String args[]) {

        String name = "Executer";
        String cmd = "java div.utils.prot.LsfDoneTest -f xyz -o test.txt";
       

        if (args.length < 1) {
            System.out.println("USAGE: java Executer <cmd>");
            Executer ex = new Executer(cmd);
        }
        else {
            Executer ex = new Executer(args);
        }
        System.exit(0);

    }

    // *****************************************************************
    // DEBUG
    // *****************************************************************

    protected static void p(String s) {
        if (DEBUG) System.out.println("Executer:" + s);
    }

    protected static void err(String s) {
        System.out.println("ERROR: Executer:" + s);
    }

    public String getErr() {
        return err.toString();
    }

    public String getOut() {
        return out.toString().trim();
    }
    public Process getProcess() {
        return process;
    }
    public int getExitval() {
        return exitval;
    }
   
}