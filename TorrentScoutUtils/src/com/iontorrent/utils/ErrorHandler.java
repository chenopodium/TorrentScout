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
package com.iontorrent.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorHandler {

	private static Throwable exception;

	public static void log(Exception e) {
		   log("Error", e);
	}

	public static void log(Object obj, Exception e) {
		   String trace  = getString(e);
		   String name = obj.getClass().getName();
		   int dot = Math.max(0, name.lastIndexOf("."));
		   name = name.substring(dot);
		   err(name+": "+trace);
	}
	public static void log(String name, Exception e) {
		   String trace  = getString(e);
		   err(name+": "+trace);
	} 
// *****************************************************************
// OUTPUT STUFF
// *****************************************************************

	public static Throwable getLastException() {
		return exception;
	}
// *****************************************************************
// HELPER
// *****************************************************************

	public static String getString(Throwable e) {
		exception = e;
		if (e == null) return "No exception";
		StringWriter w = new StringWriter();
		PrintWriter p = new PrintWriter(w, true);
		e.printStackTrace(p);
		String res = w.getBuffer().toString();
		return res;
	}

// *****************************************************************
// LOG
// *****************************************************************
 private void err(String msg, Exception ex) {
        Logger.getLogger(ErrorHandler.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        Logger.getLogger(ErrorHandler.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(ErrorHandler.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("ErrorHandler: " + msg);
        //Logger.getLogger( ErrorHandler.class.getName()).log(Level.INFO, msg, ex);
    }

}