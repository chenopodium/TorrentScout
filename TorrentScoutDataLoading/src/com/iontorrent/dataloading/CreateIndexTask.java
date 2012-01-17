/*
 * Copyright (C) 2011 Life Technologies Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.iontorrent.dataloading;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.results.scores.ScoreMask;
import com.iontorrent.sequenceloading.SequenceLoader;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.heatmaps.ScoreMaskGenerator;
import java.awt.Frame;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.iontorrent.seq.sam.SamUtils;
import org.netbeans.api.progress.ProgressHandle;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class CreateIndexTask extends Task {

    private static String lastError;
    private static Exception lastException;
    ExperimentContext exp;
    boolean ok;
    private Frame progressframe;

    public CreateIndexTask(Frame frame, TaskListener tlistener, ExperimentContext exp, ProgressHandle progress) {
        super(tlistener, progress);
        this.exp = exp;
        this.progressframe = frame;
    }

    @Override
    public Void doInBackground() {
        try {
            ok = createIndexFiles(exp, this);
        } catch (Throwable t) {
            String s = ErrorHandler.getString(t);
            err("Got an error:" + t + ", " + s);
            JOptionPane.showMessageDialog(progressframe, "The index task failed:\n" + s);
        }

        return null;
    }

    public boolean isSuccess() {
        return ok;
    }

    private boolean createIndexFiles(ExperimentContext exp, Task task) {

        task.setProgressValue(0);
        progressframe.toFront();
        progressframe.setVisible(true);
        progressframe.setAlwaysOnTop(true);
        int prog = 0;
        p("================= creating index files for " + exp.getResultsDirectory() + " ==== =========");
        String res_dir = exp.getResultsDirectory();
        String errors = "";
        p("Got experiment context: " + exp);
        SequenceLoader loader = SequenceLoader.getSequenceLoader(exp, false, true);
        boolean ok = loader.foundBamFile();
        if (!ok) {
            errors += "Failed to locate bam file in " + res_dir + ":" + loader.getMsg();
        }

        task.setProgressValue(prog += 5);

        SamUtils utils = loader.getSamUtils();
        if (utils == null) {
            // errors += "Got no BAM file, cannot create several indices<br>";
            task.setProgressValue(prog += 20);
        } else {
            if (!utils.hasWellToLocIndex()) {
                p("Creating loctoread and well to location index file " + utils.getWellToLocIndexFile() + " on BAM file");

                progressframe.setTitle("Creating well to genome index file...(can take a few minutes)");

                utils.createReadLocationsIndexFile();
                if (utils.getErrorMsg() != null) {
                    errors += utils.getErrorMsg() + " <br>";
                }
            }
            if (!utils.hasWellToLocIndex()) {
                errors += "Failed to create well to location index file" + utils.getWellToLocIndexFile() + "<br>";
            }
            task.setProgressValue(prog += 20);
            if (!utils.hasGenomeToReadIndex()) {
                p("Creating read to location index file " + utils.getGenomeToreadIndexFile());
                progressframe.setTitle("Creating read locations index file...(can take a few minutes)");
                utils.createReadLocationsIndexFile();
                if (utils.getErrorMsg() != null) {
                    errors += utils.getErrorMsg() + "<br>";
                }
            }
            if (!utils.hasGenomeToReadIndex()) {
                errors += "Failed to create read to location index file" + utils.getGenomeToreadIndexFile() + "<br>";

            }
        }
        task.setProgressValue(prog += 20);
        progressframe.setTitle("Generating various heat maps...");
        ScoreMask mask = ScoreMask.getMask(exp, exp.getWellContext());
        String msg = null;
        if (mask != null) {
            ScoreMaskGenerator gen = new ScoreMaskGenerator(mask, exp);

            task.setProgressValue(prog += 10);

            if (!mask.hasAllSffImages()) {
                try {
                    progressframe.setTitle("Generating sff heat maps...(can take a few minutes)");
                    msg = gen.readSffFile(false);
                } catch (Exception ex) {
                    Logger.getLogger(CreateIndexTask.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (msg != null && msg.length() > 4) {
                    errors += msg + "<br>";
                }

            }

            task.setProgressValue(prog += 10);

            if (!mask.hasAllBamImages() && utils != null) {
                try {
                    progressframe.setTitle("Generating BAM heat maps...(can take a few minutes)");
                    msg = gen.processBamFile(false);
                } catch (Exception ex) {
                    Logger.getLogger(CreateIndexTask.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (msg != null && msg.length() > 4) {
                    errors += msg + " <br>";
                }
            }
        }
        task.setProgressValue(prog += 15);

        ok = loader.foundSffFile();
        if (!ok) {
            errors += "Failed to locate sff file in " + res_dir + ":" + loader.getMsg() + "<br>";
        } else {
            if (!loader.hasSffIndex()) {
                p("Creating sff genome to read index");
                progressframe.setTitle("Creating sff genome to read index file (can take a few minutes)");
                loader.createSffIndex();
                if (!loader.hasSffIndex()) {
                    errors += "Failed to create sff index: " + loader.getMsg() + "<br>";
                    ok = false;
                }
            }
        }
        if (errors != null && errors.length() > 4) {
            p("Got errors:" + errors + ".");
            JOptionPane.showMessageDialog(progressframe, "<html>" + errors + "</html>");
        }
        p("======= DONE CREATING INDICES =====");
        task.setProgressValue(100);
        progressframe.dispose();
        p("disposing progress frame");
        return ok;
    }

    /** ================== LOGGING ===================== */
    public static Exception getLastException() {
        return lastException;
    }

    public static String getLastError() {
        return lastError;
    }

    private static void err(String msg, Exception ex) {
        lastException = ex;
        lastError = msg;
        System.out.println("CreateIndexTask: " + msg);
        Logger.getLogger(CreateIndexTask.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        lastError = msg;
        System.out.println("CreateIndexTask: " + msg);
        Logger.getLogger(CreateIndexTask.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(CreateIndexTask.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("CreateIndexTask: " + msg);
        Logger.getLogger(CreateIndexTask.class.getName()).log(Level.INFO, msg);
    }
}
