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
package com.iontorrent.torrentscout.explorer;

import com.iontorrent.expmodel.CompositeExperiment;
import com.iontorrent.expmodel.DatBlock;
import com.iontorrent.guiutils.widgets.Widget;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.rawdataaccess.pgmacquisition.DataAccessManager;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.rawdataaccess.wells.BfMask;
import com.iontorrent.rawdataaccess.wells.BfMaskFlag;
import com.iontorrent.rawdataaccess.wells.BitMask;
import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.utils.ToolBox;
import com.iontorrent.wellmodel.RasterData;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellSelection;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class ExplorerContext implements Serializable {

    private ArrayList<BitMask> masks;
    private ExperimentContext exp;
    private int frame = 10;
    private ArrayList<Widget> widgets;
    private int preferrednrwidgets;
    private ArrayList<WellCoordinate> widcoords;
    private WellCoordinate absdataAreaCoord;
    private transient static ExplorerContext context;
    private transient Vector<ContextChangedListener> list;
    private transient RasterData data;
    private int rasterSize;
    private BitMask ignoreMask;
    private BitMask bgMask;
    private BitMask signalMask;
    private BitMask histoMask;
    private int startframe;
    private int endframe;
    private int mainframe;
    private int cropleft = -1;
    private int cropright = -1;
    private boolean maskOrOtherDataChanged;
    private transient final Lookup.Result<WellSelection> selectionSelection =
            LookupUtils.getSubscriber(WellSelection.class, new WellSelectionListener());
    private transient final Lookup.Result<ExperimentContext> expContextResults =
            LookupUtils.getSubscriber(ExperimentContext.class, new ExpSubscriberListener());
    private transient final Lookup.Result<CompositeExperiment> compContextResults =
            LookupUtils.getSubscriber(CompositeExperiment.class, new CompSubscriberListener());
    private transient final InstanceContent expContent = LookupUtils.getPublisher(ExperimentContext.class);
    private int span;
    private transient CompositeExperiment compexp;
    private String medianfunction;

    public String storeContext(String file) {

        p("=============== STORING CONTEXT ==============");
        p("Exp is:" + exp);
        p("Cols, rows=" + exp.getNrcols() + "/" + exp.getNrrows());
        try {
            OutputStream fout = new FileOutputStream(file);
            OutputStream buffer = new BufferedOutputStream(fout);
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(this);
            out.flush();
            out.close();

        } catch (Exception e) {
            err(ErrorHandler.getString(e));
            return "Could not save context to " + file + ":\n" + ErrorHandler.getString(e);
        }
        p("Wrote explorer context into " + file);
        return null;
    }

    /**
     * @return the maskOrOtherDataChanged
     */
    public boolean isMaskOrOtherDataChanged() {
        return maskOrOtherDataChanged;
    }

    /**
     * @param maskOrOtherDataChanged the maskOrOtherDataChanged to set
     */
    public void setMaskOrOtherDataChanged(boolean maskOrOtherDataChanged) {
        this.maskOrOtherDataChanged = maskOrOtherDataChanged;
    }

    private class ExpSubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            getLatestExperimentContext();
        }
    }

    private class CompSubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            getLatestCompExp();
        }
    }

    protected void getLatestCompExp() {
        final Collection<? extends CompositeExperiment> items = compContextResults.allInstances();
        if (!items.isEmpty()) {
            CompositeExperiment data = null;
            Iterator<CompositeExperiment> it = (Iterator<CompositeExperiment>) items.iterator();
            while (it.hasNext()) {
                data = it.next();
            }
            if (data != null) {
                compexp = data;
                context.clear();
            }

        } else {
            //  p("No exp context in list");
        }
    }

    private void getLatestExperimentContext() {
        final Collection<? extends ExperimentContext> items = expContextResults.allInstances();
        if (!items.isEmpty()) {
            ExperimentContext data = null;
            Iterator<ExperimentContext> it = (Iterator<ExperimentContext>) items.iterator();
            while (it.hasNext()) {
                data = it.next();
            }
            if (data != null) {
                exp = data;
                context.clear();
            }

        } else {
            //  p("No exp context in list");
        }
    }

    public String loadContext(String file) {
        ExplorerContext loaded = null;
        p(" ============ LOADING CONTEXT ============");
        try {
            InputStream fout = new FileInputStream(file);
            InputStream buffer = new BufferedInputStream(fout);
            ObjectInput in = new ObjectInputStream(buffer);
            loaded = (ExplorerContext) in.readObject();
            in.close();
        } catch (Exception e) {
            err(ErrorHandler.getString(e));
            return "Could not load context from file " + file + ":" + ErrorHandler.getString(e);
        }
        if (loaded == null) {
            return "Could not load explorer context from " + file;
        }
        // else copy objects over
        // check experiment compatibility

        p("Loaded Exp is:" + loaded.exp);
        p("exp is: " + exp);

        WellCoordinate abs = loaded.exp.getWellContext().getAbsoluteCoordinate();
        p("Got abs coord: " + abs + " from loaded contex");
        p("Abs data area coord from loadec ontext: " + absdataAreaCoord);
        if (exp.getColOffset() != loaded.exp.getColOffset() || exp.getRowOffset() != loaded.exp.getRowOffset()) {
            GuiUtils.showNonModalMsg("Trying to pick right block for these masks at " + abs);

            if (compexp == null) {
                getLatestCompExp();
            }
            if (compexp != null) {
                DatBlock b = compexp.findBlock(abs);
                if (b == null) {
                    GuiUtils.showNonModalDialog("Please pick the right block - I was not able to find the block for " + abs + "<br>:"
                            + loaded.exp.getRawDir(), "Different block");
                    return null;
                } else {
                    exp = compexp.getContext(b, false);
                    // publishing exp context
                    GuiUtils.showNonModalDialog("Selecting block " + b, "Publishing Experiment Context");
                    p("PUBLISHING EXP:"+exp);
                    GlobalContext.getContext().setExperimentContext(exp, false);
                    LookupUtils.publish(expContent, exp);
                }
            }
            if (exp.getColOffset() != loaded.exp.getColOffset() || exp.getRowOffset() != loaded.exp.getRowOffset()) {
                GuiUtils.showNonModalDialog("Please pick the right block first for " + abs + "<br>:" + loaded.exp.getRawDir(), "Different block");
                return null;
            }
        } else {
            p("Same offset");
        }
        if (exp.getNrcols() != loaded.exp.getNrcols() || exp.getNrrows() != loaded.exp.getNrrows()) {
            GuiUtils.showNonModalDialog("Experiments may not compatible - this data is from experiment or block<br>"
                    + "Nr rows/cols=" + exp.getNrrows() + "/" + exp.getNrcols() + " vs " + loaded.exp.getNrrows() + "/" + loaded.exp.getNrcols() + "<br>Raw dir="
                    + loaded.exp.getRawDir(), "Maybe not compatible");
            //return null;
        }

        exp.getWellContext().setAbsCoordinate(abs);
        data = null;
        dataChanged(null);

        exp.setFlow(loaded.exp.getFlow());
        exp.setFileType(loaded.exp.getFileType());
        exp.setFrame(loaded.exp.getFrame());
        exp.setCacheDir(loaded.exp.getCacheDir());

        this.absdataAreaCoord = loaded.absdataAreaCoord;
        exp.getWellContext().setAbsCoordinate(absdataAreaCoord);
        this.dataAreaCoordChanged(absdataAreaCoord);

        this.frame = loaded.frame;
        this.widcoords = loaded.widcoords;
        this.bgMask = loaded.bgMask;
        this.mainframe = loaded.mainframe;
        this.signalMask = loaded.signalMask;
        this.ignoreMask = loaded.ignoreMask;
        this.histoMask = loaded.histoMask;
        this.cropleft = loaded.cropleft;
        this.cropright = loaded.cropright;
        this.startframe = loaded.startframe;
        this.endframe = loaded.endframe;
        this.span = loaded.span;
        this.medianfunction = loaded.medianfunction;
        this.rasterSize = loaded.rasterSize;
        this.preferrednrwidgets = loaded.preferrednrwidgets;

        this.masks = loaded.masks;
        masksChanged();




        return null;
    }

    private void clear() {
        data = null;
        ignoreMask = null;
        bgMask = null;
        signalMask = null;
        histoMask = null;
        masks = null;
        widgets = null;

        masksChanged();
        dataChanged(null);
        absdataAreaCoord = null;// exp.getWellContext().getCoordinate();
        widcoords = null;

        // this.dataAreaCoordChanged(absdataAreaCoord);

    }

    public static synchronized ExplorerContext getCurContext(ExperimentContext exp) {
        if (context == null) {
            p("+++++++++++++++++ context is null, creating explorer context");
            context = new ExplorerContext(exp);
        } else {
            if (!context.exp.getResultsDirectory().equals(exp.getResultsDirectory())
                    || !context.exp.getRawDir().equals(exp.getRawDir())) {
                // new context
                p("Got new context: " + exp.getResultsDirectory());
                context.exp = exp;
                context.clear();

            }
        }
        return context;
    }

    /**
     * @return the bgMask
     */
    public BitMask getBgMask() {
        return bgMask;
    }

    /**
     * @param bgMask the bgMask to set
     */
    public void setBgMask(BitMask bgMask) {
        this.bgMask = bgMask;
    }

    /**
     * @param signalMask the signalMask to set
     */
    public void setSignalMask(BitMask signalMask) {
        // p("Set signal mask: " + signalMask);
        //   Exception e = new Exception("tracking mask selection");
        //   p(ErrorHandler.getString(e));
        this.signalMask = signalMask;
    }

    public void setHistoMask(BitMask histoMask) {
        //   p("Set histo mask: " + histoMask);
        //   Exception e = new Exception("tracking mask selection");
        //   p(ErrorHandler.getString(e));
        this.histoMask = histoMask;
    }

    public void setIgnoreMask(BitMask mask) {
        this.ignoreMask = mask;
    }

    public BitMask getIgnoreMask() {
        return ignoreMask;
    }

    public BitMask getSignalMask() {
        return signalMask;
    }

    public BitMask getHistoMask() {
        return histoMask;
    }

    public WellCoordinate getMaincoord() {
        return this.exp.getWellContext().getCoordinate();
    }

    /**
     * @param preferrednrwidgets the preferrednrwidgets to set
     */
    public void setPreferrednrwidgets(int preferrednrwidgets) {
        this.preferrednrwidgets = preferrednrwidgets;
    }

    /**
     * @return the preferrednrwidgets
     */
    public int getPreferrednrwidgets() {
        if (preferrednrwidgets == 0) {
            preferrednrwidgets = 5;
        }
        return preferrednrwidgets;
    }

    public void setMedianFunction(String s) {
        this.medianfunction = s;
    }

    public String getMedianFunction() {
        return medianfunction;
    }

    private class WellSelectionListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            getLatestSelection();
        }
    }

    private void getLatestSelection() {
        final Collection<? extends WellSelection> selections = selectionSelection.allInstances();
        if (!selections.isEmpty()) {
            //  p("Getting last selection");
            WellSelection selection = null;
            Iterator<WellSelection> it = (Iterator<WellSelection>) selections.iterator();
            while (it.hasNext()) {
                selection = it.next();
            }
            p("Got a well selection (probably frome some heat map): " + selection);
            getExp().getWellContext().setSelection(selection);

            WellCoordinate middle = selection.getMiddle();
            getExp().getWellContext().setCoordinate(middle);

            if (this.getAbsDataAreaCoord() != null) {
                if (this.maskOrOtherDataChanged) {
                    int ans = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), "<html>Would you like to use <b>" + getExp().getWellContext().getAbsoluteCoordinate() + "</b> and reload all data<br>"
                            + "Note: you will <b><font color='990000'>lose all masks</font>!</b></html>", "New Coordinate", JOptionPane.OK_CANCEL_OPTION);
                    if (ans == JOptionPane.OK_OPTION) {

                        setAbsDataAreaCoord(getExp().getWellContext().getAbsoluteCoordinate());
                        p("Setting ABSOLUTE coords to middle of selection " + this.getAbsDataAreaCoord());
                    }
                } else {
                    setAbsDataAreaCoord(getExp().getWellContext().getAbsoluteCoordinate());
                }
            }

            maskOrOtherDataChanged = false;
            //   p("SubscriberListener Got WellSelection:" + cur_context);
            // rasterViewUpdate(true);
        }

    }

    public ExplorerContext(ExperimentContext exp) {
        this.exp = exp;
        this.absdataAreaCoord = exp.getWellContext().getAbsoluteCoordinate();
        list = new Vector<ContextChangedListener>();
    }

    public void addListener(ContextChangedListener l) {
        if (list.contains(l)) {
            return;
        }
        Vector<ContextChangedListener> li = (Vector<ContextChangedListener>) list.clone();
        for (ContextChangedListener old : li) {
            if (l.getClass().equals(old.getClass())) {
                list.remove(old);
            }
        }
        exp.addListener(l);
        list.add(l);
    }

    public void removeListener(ContextChangedListener l) {
        list.remove(l);
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(ExplorerContext.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        Logger.getLogger(ExplorerContext.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(ExplorerContext.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        //System.out.println("ExplorerContext: " + msg);
        Logger.getLogger(ExplorerContext.class.getName()).log(Level.INFO, msg);
    }

    /**
     * @return the masks
     */
    public ArrayList<BitMask> getMasks() {
        if (masks == null) {
            masks = new ArrayList<BitMask>();
        }
        if (masks.size() < 1) {
            BfMask bfmask = this.exp.getWellContext().getMask();
            if (bfmask != null || data != null) {
                createMasks();
            }

        }

        return masks;
    }

    public BitMask getDefaultMask() {
        ArrayList<BitMask> masks = getMasks();
        for (BitMask m : masks) {
            if (m.getName().toLowerCase().indexOf("pinned") > -1) {
                return m;
            }
        }
        return null;
    }

    /**
     * @return the exp
     */
    public ExperimentContext getExp() {
        return exp;
    }

    /**
     * @return the flow
     */
    public int getFlow() {
        return this.exp.getFlow();
    }

    /**
     * @param flow the flow to set
     */
    public void setFlow(int flow) {

        exp.setFlow(flow);
    }

    /**
     * @return the frame
     */
    public int getFrame() {
        if (frame < 0) {
            frame = 10;
        }
        return frame;
    }

    /**
     * @param frame the frame to set
     */
    public void setFrame(int frame) {
        Vector<ContextChangedListener> li = (Vector<ContextChangedListener>) list.clone();
        if (frame != this.frame) {
            this.frame = frame;
            for (ContextChangedListener l : li) {
                l.frameChanged(frame);
            }
        }

    }

    public void widgetChanged(Widget w) {
        Vector<ContextChangedListener> li = (Vector<ContextChangedListener>) list.clone();
        for (ContextChangedListener l : li) {
            l.widgetChanged(w);
        }
    }

    public void typeChanged(RawType w) {
        Vector<ContextChangedListener> li = (Vector<ContextChangedListener>) list.clone();
        for (ContextChangedListener l : li) {
            l.fileTypeChanged(w);
        }
    }

    public void dataChanged(RasterData w) {
        Vector<ContextChangedListener> li = (Vector<ContextChangedListener>) list.clone();
        for (ContextChangedListener l : li) {
            l.dataChanged(w, -1, -1, -1, -1, -1, -1);
        }

    }

    public void maskChanged(BitMask w) {
        Vector<ContextChangedListener> li = (Vector<ContextChangedListener>) list.clone();
        for (ContextChangedListener l : li) {
            l.maskChanged(w);
        }
        this.setMaskOrOtherDataChanged(true);
    }

    public void masksChanged() {
        Vector<ContextChangedListener> li = (Vector<ContextChangedListener>) list.clone();
        for (ContextChangedListener l : li) {
            l.masksChanged();
        }
    }

    public void masAdded(BitMask w) {
        Vector<ContextChangedListener> li = (Vector<ContextChangedListener>) list.clone();
        for (ContextChangedListener l : li) {
            l.maskAdded(w);
        }
    }

    public void maskRemoved(BitMask mask) {
        if (getSignalMask() == mask) {
            setSignalMask(null);
        }
        if (getBgMask() == mask) {
            setBgMask(null);
        }
        if (getIgnoreMask() == mask) {
            setIgnoreMask(null);
        }
        if (getHistoMask() == mask) {
            setHistoMask(null);
        }
        getMasks().remove(mask);
        masksChanged();


        Vector<ContextChangedListener> li = (Vector<ContextChangedListener>) list.clone();
        for (ContextChangedListener l : li) {
            l.maskRemoved(mask);
        }
    }

    public void maskSelected(BitMask w) {
        Vector<ContextChangedListener> li = (Vector<ContextChangedListener>) list.clone();
        for (ContextChangedListener l : li) {
            l.maskSelected(w);
        }
    }

    public void dataAreaCoordChanged(WellCoordinate w) {
        Vector<ContextChangedListener> li = (Vector<ContextChangedListener>) list.clone();
        for (ContextChangedListener l : li) {
            l.dataAreaCoordChanged(w);
        }
    }

    public void coordChanged(WellCoordinate w) {
        Vector<ContextChangedListener> li = (Vector<ContextChangedListener>) list.clone();
        for (ContextChangedListener l : li) {
            l.coordChanged(w);
        }
    }

    /**
     * @return the widgets
     */
    public ArrayList<Widget> getWidgets() {
        if (widgets == null) {
            widgets = new ArrayList<Widget>();
        }
        return widgets;
    }

    /**
     * @return the widcoords
     */
    public ArrayList<WellCoordinate> getWidcoords() {
        return widcoords;
    }

    /**
     * @param widcoords the widcoords to set
     */
    public void setWidcoords(ArrayList<WellCoordinate> widcoords) {
        this.widcoords = widcoords;
    }

    public void tellUserWhyDataNotThere() {
        tellUserWhyDataNotThere(getFlow());
    }

    public void tellUserWhyDataNotThere(int flow) {
        WellCoordinate abs = getAbsDataAreaCoord();
        DataAccessManager manager = DataAccessManager.getManager(getExp().getWellContext());
        WellCoordinate rel = this.getRelativeDataAreaCoord();
        boolean exists = manager.existsFile(getExp().getWellContext(), rel, flow, RawType.ACQ);
        String msg = "<html>";

        if (exists) {
            msg += "I got no data for flow " + flow + ", " + getFiletype() + " in <b>" + getExp().getRawDir() + "</b> at " + getAbsDataAreaCoord() + "<br>";
            msg += "The file exists, but maybe the absolute data area coordinates " + abs
                    + " are out of bounds?<br>(Relative to block, the coords are: " + rel
                    + ", offsets=" + exp.getColOffset() + "/" + exp.getRowOffset() + ", selected coord=" + exp.getWellContext().getCoordinate() + ")";
            if (!exp.isBlock()) {
                msg += "<br>The experiment is NOT a block";
            }

        } else {
            // Exception e = new Exception("Tracing");
            //  p(ErrorHandler.getString(e));
            if (getExp().isChipBB()) {
                if (!getExp().isBlock()) {

                    msg += "<br>For Proton experiments,  pick a block:";
                    msg += "<ul>";
                    msg += "<li>by <b>clicking</b> on a block in the <b>Proton Block Component</b><br>(after the image has been computed, which can take 2-3 minutes the first time this experiment is viewed)</li>";
                    msg += "<li>or by selecting a block in the <b>dropdown</b> in the <b>Proton Block Component</b>"
                            + "<br>(can be done before composite image is computed)</li>";
                    msg += "<li>or by selecting/entering the block folder directly in the <b>Open Experiment Component</b> <br>"
                            + "(quickest because it bypasses the composite image creation of all blocks)</li>";
                    msg += "</ul>";
                } else {
                    msg += "I got no data for flow " + flow + ", " + getFiletype() + " in <b>" + getExp().getRawDir() + "</b> at " + getAbsDataAreaCoord() + "<br>";
                    msg += "(And it looks like a BLOCK of a Proton experiment to me)<br>";
                    msg += "The block may not contain all data!<br>";
                    msg += "You might want to check the folder: " + getExp().getRawDir();
                    msg += "<br>(You could try to pick the folder yourself via <b>Offline Component</b>)";

                }
            } else {
                msg += "I got no data for flow " + flow + ", " + getFiletype() + " in <b>" + getExp().getRawDir() + "</b> at " + getAbsDataAreaCoord() + "<br>";
                msg += "(And it does not look like a Proton experiment to me)<br>";
                msg += "<b><font color='990000'>I did not find the file for flow " + flow + ", " + getFiletype() + "</font></b>";
                msg += "<br>You might want to check your path, or use the <b>Offline Component</b> to select the folder with the raw files<br>";
            }

        }
        msg += "</html>";
        GuiUtils.showNonModalDialog(msg, "ExplorerContext");
        //JOptionPane.showMessageDialog(null, msg);
    }

    /**
     * @return the maincoord
     */
    public WellCoordinate getAbsDataAreaCoord() {
        if (absdataAreaCoord == null) {

            if (exp != null && exp.getWellContext() != null) {
                p("data area coordinate is null... using value from well context");
                absdataAreaCoord = exp.getWellContext().getAbsoluteCoordinate();
                exp.makeAbsolute(absdataAreaCoord);
            } else {
                p("Got no exp or no wellcontext: " + exp);
            }
        }
        return absdataAreaCoord;
    }

    public WellCoordinate getRelativeDataAreaCoord() {
        WellCoordinate coord = getAbsDataAreaCoord();
        if (coord == null || exp == null) {
            return null;
        }
        WellCoordinate rel = new WellCoordinate(coord.getCol(), coord.getRow());
        exp.makeRelative(rel);
        return rel;
    }

    /**
     * @param maincoord the maincoord to set
     */
    public void setAbsDataAreaCoord(WellCoordinate abscoord) {
        if (abscoord != this.absdataAreaCoord) {
            this.absdataAreaCoord = abscoord;
            exp.getWellContext().setAbsCoordinate(abscoord);
            p("SENDING DATA AREA COORD CHANGED EVENT: " + abscoord);
            this.dataAreaCoordChanged(abscoord);
            // also send event using lookup?

            //this.createMasks();
        }

    }

    public void setRelDataAreaCoord(WellCoordinate rel) {
        if (exp == null || rel == null) {
            return;
        }
        WellCoordinate abs = new WellCoordinate(rel.getCol(), rel.getRow());
        exp.makeAbsolute(abs);
        setAbsDataAreaCoord(abs);
    }

    public void addMask(BitMask mask) {
        if (masks == null) {
            masks = new ArrayList<BitMask>();
        }
        masks.add(mask);
        this.masAdded(mask);
        this.setMaskOrOtherDataChanged(true);

    }

    public void removeMask(BitMask mask) {
        masks.remove(mask);
        this.maskRemoved(mask);
    }

    /**
     * @return the filetype
     */
    public RawType getFiletype() {
        if (exp.getFileType() == null) {
            p("GOT NO FILE TYPE, using raw type ACQ");
            exp.setFileType(RawType.ACQ);
        }
        return exp.getFileType();
    }

    /**
     * @param filetype the filetype to set
     */
    public void setFiletype(RawType filetype) {

        exp.setFileType(filetype);
    }

    /**
     * @return the data
     */
    public RasterData getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public synchronized void setData(RasterData d) {

        if (d == null) return;
        if (d != this.data) {
            WellCoordinate oldc = null;
            WellCoordinate newc = d.getRelStartcoord();
            if (this.data != null) {
                oldc = data.getRelStartcoord();
            }
            this.data = d;
            // only remove widgets if they are in a different area!
            if (oldc != null && oldc.toString().equalsIgnoreCase(newc.toString())) {
                this.getWidgets().clear();
            }
            this.dataChanged(d);
            this.createMasks();
        }

    }

    /**
     * @return the rasterSize
     */
    public int getRasterSize() {
        if (rasterSize == 0) {
            rasterSize = 100;
        }
        return rasterSize;
    }

    /**
     * @param rasterSize the rasterSize to set
     */
    public void setRasterSize(int rasterSize) {
        this.rasterSize = rasterSize;
    }

    /**
     * @return the startframe
     */
    public int getStartframe() {
        if (startframe == 0) {
            startframe = 5;
        }

        return startframe;
    }

    /**
     * @param startframe the startframe to set
     */
    public void setStartframe(int startframe) {
        this.startframe = startframe;
    }

    /**
     * @return the endframe
     */
    public int getEndframe() {

        if (endframe == 0) {
            endframe = 35;
        }
        return endframe;
    }

    /**
     * @param endframe the endframe to set
     */
    public void setEndframe(int endframe) {
        this.endframe = endframe;
    }

    /**
     * @return the cropleft
     */
    public int getCropleft() {
        if (cropleft < 0) {
            cropleft = 2;
        }
        return cropleft;
    }

    /**
     * @param cropleft the cropleft to set
     */
    public void setCropleft(int cropleft) {
        this.cropleft = cropleft;
    }

    /**
     * @return the cropright
     */
    public int getCropright() {
        if (cropright <= 0) {
            cropright = 30;
        }
        return cropright;
    }

    /**
     * @param cropright the cropright to set
     */
    public void setCropright(int cropright) {
        this.cropright = cropright;
    }

    public void frameChanged(int frame) {
        for (ContextChangedListener l : list) {
            l.frameChanged(frame);
        }
    }

    public void createMasks() {

        maskOrOtherDataChanged = false;
        this.ignoreMask = null;
        this.bgMask = null;
        this.signalMask = null;
        this.histoMask = null;
        int x0 = -1;
        int y0 = -1;
        int dx = this.getRasterSize();
        if (data != null) {
            x0 = data.getRelStartCol();
            y0 = data.getRelStartRow();
            dx = data.getRaster_size();
        } else if (this.getAbsDataAreaCoord() != null) {
            p("createMasks: gpt abs coord " + getAbsDataAreaCoord());
            x0 = this.getRelativeDataAreaCoord().getCol();
            y0 = this.getRelativeDataAreaCoord().getRow();
        } else {
            p("Got no data and no coordinate, won't create masks");
            return;
        }
//        if (masks != null && masks.size()>0) {
//            BitMask m = masks.get(0);
//        }
        this.masks = new ArrayList<BitMask>();
        BfMask bfmask = this.exp.getWellContext().getMask();
        WellCoordinate relcoord = new WellCoordinate(x0, y0);
        p("Createing all new masks at REL " + relcoord + "/ abs=" + exp.getAbsolute(relcoord));
        if (bfmask != null) {
            p("creating masks based on bfmask at " + relcoord);
            masks = bfmask.createMasks(x0, y0, dx, dx);
            this.bgMask = masks.get(1);
            this.signalMask = masks.get(masks.size() - 1);
            //        pinnedMask = selectedMask;
        } else {
            if (data != null) {
                p("creating pinned mask at REL " + relcoord);
                BitMask pinned = new BitMask(relcoord, dx, dx);
                pinned.setName("0. " + BfMaskFlag.PINNED.getName());
                for (int c = 0; c < dx; c++) {
                    for (int r = 0; r < dx; r++) {
                        if (data.isPinned(c, r)) {
                            pinned.set(c, r, true);
                        }
                    }
                }
                masks.add(pinned);
                BitMask bg = new BitMask(relcoord, dx, dx);
                bg.not(pinned);
                bg.setName("1. not pinned");
                masks.add(bg);
                this.bgMask = bg;
//                BitMask all = new BitMask(coord, dx, dx);
//                all.subtract(bg, bg);
//                all.setName("2. all");                
//                masks.add(all);
                //     this.signalMask = all;
//               
                ignoreMask = pinned;
            } else {
                p("Got no data, cannot create pinned mask");
            }
            //       pinnedMask = pinned;
        }

        this.masksChanged();
    }
//    public BitMask getPinnedMask() {
//        returnp pinnedMask;
//    }

    public void setSpan(int span) {
        this.span = span;
    }

    public int getSpan() {
        return span;
    }
}
