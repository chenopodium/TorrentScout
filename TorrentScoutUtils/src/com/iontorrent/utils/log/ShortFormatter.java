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
package com.iontorrent.utils.log;

import com.iontorrent.utils.ErrorHandler;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class ShortFormatter extends Formatter {

    private static LinkedList<Throwable> errors = new LinkedList<Throwable>();
    private static int MAX = 10;

    private static ShortFormatter formatter = new ShortFormatter();
    
    public static ShortFormatter getFormatter() {
        if (formatter == null) formatter = new ShortFormatter();
        return formatter;
    }
    private ShortFormatter() {
        super();

    }

    public LinkedList<Throwable> getExceptions() {
        return errors;
    }
    private void addRecord(LogRecord record) {
//        if (record == null) {
//            return;
//        }
//        if (record.getLevel() == Level.SEVERE && record.getThrown() != null) {
//            Throwable t = record.getThrown();
//            Logger.getLogger(getClass().getName(), "Adding error to logging: " + record.getMessage() + ", throwable: " + t.toString());
//            if (errors.size() > MAX) {
//                errors.removeFirst();
//            }
//            errors.add(t);
//        }
    }

    public Throwable getLastException() {
        if (errors.size() < 1) {
            return null;
        } else {
            return errors.getLast();
        }
    }

    @Override
    public String format(LogRecord record) {
        addRecord(record);
        StringBuilder sb = new StringBuilder();

        Date date = new Date(record.getMillis());

        sb.append(date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds());
        sb.append(" ");

        String cl = record.getSourceClassName();
        int dot = cl.lastIndexOf(".");
        if (dot > -1) {
            cl = cl.substring(dot + 1);
        }

        sb.append(cl);
        sb.append(" ");
        String lev = record.getLevel().getName();
        if (record.getLevel() == Level.INFO) lev = " ";
        else if (record.getLevel() == Level.SEVERE) lev = "!";
        else if (record.getLevel() == Level.WARNING) lev = "?";
        else if (record.getLevel().intValue()< Level.INFO.intValue()) lev = " (";                
        else if (lev.length() > 1) {
            lev = lev.substring(0, 1);
        }
        
        sb.append(lev);
        sb.append(" ");

        sb.append(formatMessage(record));
        if (record.getLevel().intValue()< Level.INFO.intValue()) sb.append(")");
        sb.append("\n");
        
        if ( record.getThrown() != null) {
            sb.append(ErrorHandler.getString(record.getThrown()));
            sb.append("\n");
        }

        final String ret = sb.toString();

        return ret;
    }
}
