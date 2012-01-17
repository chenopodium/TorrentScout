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
package com.iontorrent.scout.experimentviewer;

import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.utils.io.FileUtils;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Cancellable;

/**
 *
 * @author Chantal Roth
 */
public class ExpDownloadTask extends Task implements Cancellable {

    String msg;
    ExperimentContext exp;
    GlobalContext context;
    int prog;

    public ExpDownloadTask(TaskListener tlistener, ProgressHandle progress, GlobalContext context) {
        super(tlistener, progress);
        this.exp = context.getExperimentContext();
        this.context = context;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public Void doInBackground() {
// public static File findAndCopyFileFromUrlTocache(String file, String cache_dir, String source_dir, 
        //          boolean copyAlsoIfSourceIsFile, boolean copyAlsoIfCannotWriteInSource) {
        String cache = exp.getCacheDir();
        String results = exp.getResultsDirectory();
        //String raw = context.getRawDir();
        msg = "";
        if (!FileUtils.isUrl(results)) {
            msg = "Results folder is not a url, no need to copy data!";

            return null;
        }
        prog = 0;

        File f = process("bfmask.bin", results, cache, 1, 2048);
        f = process("1.wells", results, cache, 20, 1024*1024);
        f = process(exp.getBamFileName(), results, cache, 20, 1024*1024);
        if (f == null || !f.exists()) {
            f = process(exp.getBamFileName(), results, cache, 20, 1024*1024);
        }
        f = process(exp.getSffFileName(), results, cache, 20, 1024*1024);

        return null;
    }

    private File process(String name, String results, String cache, int inc, int BUFF_SIZE) {
        String msg = "Downloading " + name + " file from " + results + " to " + cache;
        p(msg);
        File f = null;
        try {
            this.setMessage(msg);
            f = FileUtils.findAndCopyFileFromUrlTocache(name, cache, results, false, false, null, BUFF_SIZE);
            if (f == null) {
                msg += "<br>I was unable to download " + name;
            } else if (!f.exists()) {
                msg += "<br>The file " + f + " is not there for some reason...";
            }
            this.setProgress(prog += inc);
        } catch (Exception e) {
            err("Got an exception while doing " + msg + ":" + e.getMessage(), e);
        }
        return f;
    }

    public boolean isSuccess() {
        return msg == null || msg.length() < 1;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(ExpDownloadTask.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(ExpDownloadTask.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(ExpDownloadTask.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("ExpDownloadTask: " + msg);
        Logger.getLogger(ExpDownloadTask.class.getName()).log(Level.INFO, msg);
    }

    @Override
    public boolean cancel() {
        msg = "Task was aborted";
        this.cancel(true);

        this.getTaskListener().taskDone(this);
        return true;

    }
}
