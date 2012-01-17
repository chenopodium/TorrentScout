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
package com.iontorrent.main.data;

import com.iontorrent.main.data.RawFileConverter.Conversion;
import com.iontorrent.rawdataaccess.pgmacquisition.RasterIO;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;

/**
 *
 * @author Chantal Roth
 */
public class ConverterTask extends Task implements Cancellable{

    Conversion con;
    String msg;
    String raw_dir;
    String cache_dir;
    Throwable ex;        
    
    public ConverterTask(TaskListener listener, ProgressHandle plistener, Conversion conv, String raw, String cache) {
        super(listener, plistener);
        this.con = conv;
        this.raw_dir = raw;
        this.cache_dir = cache;

    }
    public String getMsg() {
        return msg;
    }
    public Throwable getThrowable() {
        return ex;
    }

    public boolean isSuccess() {
        return msg == null || msg.length()<1;
    }
    @Override
    public Void doInBackground() {
        doConversion();
        return null;
    }

    private void doConversion() {
        msg = "";
        try {
            setProgressValue(0);
            int start = con.getStart();
            int end = con.getEnd();
            if (start < 0) {
                // ignore
             //   msg += "Start (" + start + ") for " + con.getType() + " must be >= 0<br>";
            } else if (end < start || end < 0) {
                msg += "Start (" + start + ") for " + con.getType() + " must be < end (" + end + ")<br>";
            } else if (end > con.getMax()) {
                msg += "There are only " + con.getMax() + " files of type " + con.getType() + "*.dat, but startflow=" + start + " and end=" + end + "<br>";
            } else {
                try {
                    String info = "Converting flows "+start +"-"+end+" for files of type " + con.getType();
                     setText(info);
                     p(info);
                    String res = RasterIO.convertFlows(con.getType(), raw_dir, cache_dir, start, end-1, super.getProgListener(), null);
                    if (res != null || res.length()>0) {
                        msg += res;
                    }
                } catch (Exception e) {
                    err("Got an error: ", e);
                    ex = e;

                }

            }
            setProgressValue(100);
        }
        catch (Throwable e){
            err("Got a  Throwable in ConverterTask: "+e.getMessage()+", cause: "+e.getCause());
            this.ex = e;
            Exceptions.printStackTrace(e);
        }
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(ConverterTask.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(ConverterTask.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(ConverterTask.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("ConverterTask: " + msg);
        //Logger.getLogger( ConverterTask.class.getName()).log(Level.INFO, msg, ex);
    }

    public Conversion getConversion() {
        return con;
    }

    @Override
    public boolean cancel() {
       this.cancel(true);
       this.getTaskListener().taskDone(this);
       return true;
    }
}
