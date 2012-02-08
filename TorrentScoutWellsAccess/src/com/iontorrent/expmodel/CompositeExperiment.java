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
package com.iontorrent.expmodel;

import com.iontorrent.utils.io.FileUtils;
import com.iontorrent.wellmodel.WellCoordinate;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class CompositeExperiment {

    private static String lastError;
    private static Exception lastException;
    private ExperimentContext rootexp;
    private ExperimentContext exptemplate;
    private ArrayList<DatBlock> blocks;
    private DatBlock curblock;
    public static final String THUMB = "thumbnail";

    public CompositeExperiment(ExperimentContext exp) {
        this.rootexp = exp;
        exptemplate = exp.deepClone();
        exptemplate.clear();
    }

    public void addMissingBlocks() {
        // FOR NOW, add missing blocks
        DatBlock b = blocks.get(0);
        int w = b.getWidth();
        int h = b.getHeight();

        int nrx = this.getNrcols() / w;
        int nry = this.getNrrows() / h;
        for (int bx = 0; bx < nrx; bx++) {
            int x = bx * w;
            for (int by = 0; by < nry; by++) {
                int y = by * h;
                b = this.findBlock(x, y);
                if (b == null) {
                    b = new DatBlock(new WellCoordinate(x, y), new WellCoordinate(x + w, y + h));
                    //p("Adding missing block for "+x+"/"+y+":"+b);
                    blocks.add(b);
                }
            }
        }
        // END ADDING MISSING BLOCKS
    }

    public ExperimentContext getRootContext() {
        return rootexp;
    }

    public ExperimentContext getContext(DatBlock block, boolean offerHelp) {
        ExperimentContext be = exptemplate.deepClone();
        if (block == null) {
            p("Got no block");
            return null;
        }
        this.curblock = block;
        be.setRawDir(this.getRawDir(block));
        be.setNrcols(block.getWidth());
        be.setNrrows(block.getHeight());
        be.setSffFilename("rawlib.sff");
        be.setBamFilename("rawlib.bam");
        be.setResultsName(exptemplate.getResultsName()+"_block_"+block.toShortString());
        be.setResultsDirectory(this.getResultsDirectory(block));
        if (!FileUtils.exists(be.getResultsDirectory())) {
            p("block results dir " + be.getResultsDirectory() + " not found, using RAW dir");
            be.setResultsDirectory(be.getRawDir());
        }
        be.setCacheDir(this.getCacheDir(block));
        int offsetx = block.getStart().getCol();
        int offsety = block.getStart().getRow();
        be.setColOffset(offsetx);
        be.setRowOffset(offsety);
        be.setBlock(true);
        be.setDatblock(block);
        p("Got exp context with block :"+block);
        p("block exp is: "+be);
        showBlockHelp(offerHelp);
        return be;
    }

    protected void showBlockHelp(boolean offerHelp) throws HeadlessException {
        // offer help
        if (offerHelp) {
            String msg = "<html>Now pick a <b>region</b> in this block to view results or raw data. You can either:<br>";
            msg += "<ul>";
            msg += "<li>Pick an area in the <b>Whole Chip/Block View</b><br>(which shows just this one <b>block</b>)</li>";
            msg += "<li>Pick an area in the <b>Mask View</b><br>(<b>if</b> bfmask.bin is available for this block!)</li>";
            msg += "<li>Enter coordinates in <b>Whole Chip/Block View</b><br>(rectangle icon in tool bar)</li>";
            msg += "<li>Enter coordinates in the <b>Process Window</b><br>(rectangle icon in tool bar)</li>";

            msg += "</ul></html>";
            //  JOptionPane.showMessageDialog(null, msg);
            
//            JFrame f = new JFrame();
//
//            final JDialog newdialog = new JDialog(f, "Viewing data from a BB block", false);
//            newdialog.setLocation(500, 300);
//            JLabel comp = new JLabel(msg);
//            newdialog.getContentPane().add(comp);
//            newdialog.pack();
//            newdialog.setAlwaysOnTop(true);
//            newdialog.setVisible(true);
//            KeyListener l = new MyEscapeListener(newdialog);
//            newdialog.addKeyListener(l);
//            comp.addKeyListener(l);
//            f.addKeyListener(l);
//            comp.setToolTipText("Click escape, space or tab to close this window (or click on the x)");
        }
    }

    private static class MyEscapeListener implements KeyListener {

        private JDialog d;

        public MyEscapeListener(JDialog d) {
            this.d = d;
        }

        @Override
        public void keyTyped(KeyEvent e) {
            int c = e.getKeyCode();
            p("Got key on dialog: " + c);
            if (c == KeyEvent.VK_ESCAPE || c == KeyEvent.VK_ENTER || c == KeyEvent.VK_SPACE) {
                //closing frame
                d.dispose();

            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            p("keyPressed on dialog");
            keyTyped(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public ExperimentContext getThumbnailsContext(boolean help) {
        ExperimentContext be = exptemplate.deepClone();
        be.setRawDir(rootexp.getRawDir() + THUMB);
        be.setThumbnails(true);
        if (!FileUtils.exists(be.getRawDir())) {
            err(THUMB + " raw dir " + be.getRawDir() + " not found");
            //return null;
        }

        be.setResultsDirectory(rootexp.getResultsDirectory() + "block_" + THUMB);
        if (!FileUtils.exists(be.getResultsDirectory())) {
            p("Thumbnails results dir " + be.getResultsDirectory() + " not found, using RAW dir");
            be.setResultsDirectory(be.getRawDir());
        }
        be.setCacheDir(rootexp.getCacheDir() + "cache_" + THUMB);
        int offsetx = 0;
        int offsety = 0;
        be.setColOffset(offsetx);
        be.setRowOffset(offsety);
        be.getWellContext();
        p("Got thumbnails experiment context: " + be);
        //p("Dirs: "+be.toString());
        showBlockHelp(help);
        return be;
    }

    public DatBlock findBlock(WellCoordinate coord) {
        return findBlock(coord.getCol(), coord.getRow());
    }

    public DatBlock findBlock(int x, int y) {
        for (DatBlock b : getBlocks()) {
            if (b.contains(x, y)) {
                return b;
            }
        }
        //   warn("No block found with coord: "+x+"/"+y);
        return null;
    }

    public void maybParseBlocks() {
        if (blocks != null && blocks.size() > 0) {
            return;
        } else {
            parseBlocks();
        }
    }

    private boolean parseBlocks() {

        ExpLogParser par = new ExpLogParser(rootexp.getRawDir());
        if (!par.parse()) {
            warn("Could not parse blocks");
            return false;
        }
        int maxrow = 0;
        int maxcol = 0;
        blocks = par.getBlocks();
        if (blocks == null || blocks.size() < 1) {
            err("No blocks found");
            return false;
        }
        for (DatBlock b : blocks) {
            int r = b.getEnd().getRow();
            int c = b.getEnd().getCol();
            if (c > maxcol) {
                maxcol = c;
            }
            if (r > maxrow) {
                maxrow = r;
            }
        }
        setNrcols(maxcol);
        setNrrows(maxrow);
        p("Adding missing blocks");
        addMissingBlocks();


        p("Total nr cols/rows: " + maxcol + "/" + maxrow);
        setCurblock(null);
        return true;
    }

    public String getResultsDirectory(DatBlock b) {
        String dir = rootexp.getResultsDirectory();
        if (b == null) {
            return dir;
        }
        String d = b.getResultsBlockDir(dir);
        if (!FileUtils.exists(d)) {
          //  warn("Dir not found: " + d + ", using default dir");
            d = b.getDefaultResultsBlockDir(dir);
        } else {
            p("Using results block dir " + d);
        }
        return d;
    }

    public String getCacheDir(DatBlock b) {
        String dir = rootexp.getCacheDir();
        if (b == null) {
            return dir;
        }
        String d = b.getCacheBlockDir(dir);
        if (!FileUtils.exists(d)) {
            File f = new File(d);
            p("Creating dir " + f);
            f.mkdir();
            if (!FileUtils.exists(d)) {
                warn("Dir not found and could not create subdir: " + d);
                d = b.getDefaultResultsBlockDir(dir);
            }
        }

        return d;
    }

    public String getRawDir(DatBlock b) {
        String dir = rootexp.getRawDir();
        if (b == null) {
            return dir;
        }
        String d = b.getRawBlockDir(dir);
        if (!FileUtils.exists(d)) {
       //     warn("Dir not found: " + d + ", using default dir");
            d = b.getDefaultRawBlockDir(dir);
        } else {
            p("Using raw block dir " + d);
        }
        return d;
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
        Logger.getLogger(CompositeExperiment.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        lastError = msg;
        Logger.getLogger(CompositeExperiment.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(CompositeExperiment.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        //System.out.println("CompositeExperiment: " + msg);
        Logger.getLogger(CompositeExperiment.class.getName()).log(Level.INFO, msg);
    }

    /**
     * @return the curblock
     */
    public DatBlock getCurblock() {
        return curblock;
    }

    /**
     * @param curblock the curblock to set
     */
    public void setCurblock(DatBlock curblock) {
        //   p("Current block: "+curblock);
        this.curblock = curblock;
    }

    /**
     * @return the blocks
     */
    public ArrayList<DatBlock> getBlocks() {
        maybParseBlocks();
        return blocks;
    }

    public int getNrBlocks() {
        if (blocks == null) {
            return 0;
        } else {
            return blocks.size();
        }
    }

    /**
     * @return the nrcols
     */
    public int getNrcols() {
        return rootexp.getNrcols();
    }

    /**
     * @param nrcols the nrcols to set
     */
    public void setNrcols(int nrcols) {
        rootexp.setNrcols(nrcols);
    }

    /**
     * @return the nrrows
     */
    public int getNrrows() {
        return rootexp.getNrrows();
    }

    /**
     * @param nrrows the nrrows to set
     */
    public void setNrrows(int nrrows) {
        this.rootexp.setNrrows(nrrows);
    }

    public ExperimentContext getContext(int i) {
        DatBlock block = this.getBlocks().get(0);
        if (block == null) {
            return null;
        }
        return getContext(block, false);
    }
}
