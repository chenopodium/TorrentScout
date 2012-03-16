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

/*
 * ChipPanel.java
 *
 * Created on 02.11.2011, 11:16:48
 */
package com.iontorrent.chipview;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.expmodel.FiletypeListener;
import com.iontorrent.guiutils.flow.FiletypePanel;
import com.iontorrent.expmodel.FlowListener;
import com.iontorrent.guiutils.flow.FlowNrPanel;
import com.iontorrent.expmodel.FrameListener;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.guiutils.flow.FramePanel;
import com.iontorrent.guiutils.wells.CoordSelectionPanel;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.rawdataaccess.wells.BfMaskFlag;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.utils.LookupUtils;
import com.iontorrent.utils.ProgressListener;
import com.iontorrent.utils.io.FileUtils;
import com.iontorrent.wellmodel.BfHeatMap;
import com.iontorrent.wellmodel.ChipWellDensity;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellSelection;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Chantal Roth
 */
public class ChipPanel extends javax.swing.JPanel implements FlowListener, FiletypeListener, FrameListener, TaskListener {

    private ExperimentContext exp;
    private ChipDensityPanel den;
    private BfHeatMap mask;
    private String lastmsg;
    private FlowNrPanel flowPanel;
    private FiletypePanel typePanel;
    private FramePanel framePanel;
    private RawType filetype;
    private int flow;
    private int frame;
    private String lastfilewitherror;
    CreateHeatMapTask curtask;
    private transient final InstanceContent wellSelectionContent = LookupUtils.getPublisher(WellSelection.class);

    /** Creates new form ChipPanel */
    public ChipPanel(ExperimentContext exp) {
        initComponents();
        this.exp = exp;
        mask = BfHeatMap.getMask(exp);
        den = new ChipDensityPanel(exp);
        filetype = exp.getFileType();
        flow = exp.getFlow();
        panMain.add("Center", den);

        flowPanel = new FlowNrPanel(this);
        typePanel = new FiletypePanel(this);
        framePanel = new FramePanel(this);
        frame = 15;
        framePanel.setFrame(frame);
        typePanel.setType(RawType.ACQ);
        typePanel.setOpaque(false);
        //bar.setLayout(new FlowLayout());
        bar.add(this.framePanel);
        bar.add(this.flowPanel);
        bar.add(this.typePanel);

        //  update();
    }

    @Override
    public void flowChanged(ArrayList<Integer> flows) {
        int f = flows.get(0);

        if (this.flow == f) {
            //    return;
        }
        this.flow = f;
        exp.setFlow(f);
        update(true);

    }

    @Override
    public void fileTypeChanged(RawType filetype) {
        this.exp.setFileType(filetype);
        this.filetype = filetype;
        flow = 0;
        flowPanel.setFlow(0);
        this.exp.setFlow(flow);
        typePanel.setType(filetype);
        update(true);

    }

    public void update(boolean createnewmask) {

        if (mask == null || createnewmask) {
            this.exp = GlobalContext.getContext().getExperimentContext();
            if (exp == null) {
                p("No experiment");
                return;
            }
            mask = BfHeatMap.getMask(exp);
            den.setExp(exp);
        }
        if (mask == null) {
            //  GuiUtils.showNonModalMsg("Got no experiment context yet");
            return;
        }
// check if we just did it
        // get flow info
        String base = "?";

        if (filetype == RawType.ACQ) {
            base = "" + this.exp.getWellContext().getBase(flow);
        }
        this.flowPanel.setToolTipText("<html>Base for flow " + flow + ":" + base + "<br>Flow order: " + exp.getFlowOrder() + "</html>");
        this.btnReload.setToolTipText("Reload data for flow " + flow + ", raw dir=" + exp.getRawDir());
        String file = mask.getImageFile("chip", BfMaskFlag.RAW, flow, filetype, frame);
        if (!mask.hasImage("chip", BfMaskFlag.RAW, flow, filetype, frame)) {
            if (curtask != null) {
                // GuiUtils.showNonModalMsg("Alreading computing a heat map, please wait");;
            } else if (this.lastfilewitherror == null || !lastfilewitherror.equals(file)) {
                GuiUtils.showNonModalMsg("Creating image for " + filetype + " and flow " + flow);
                curtask = new CreateHeatMapTask(this);
                curtask.execute();
            } else {
                JOptionPane.showMessageDialog(this, "<html>Could not generate heat map for " + filetype + ", flow " + flow
                        + "<br>raw dir: " + this.exp.getRawDir()
                        + "<br>The raw file is probably not there - "
                        + "<br>maybe because this is the root of a bb experiment?"
                        + "<br>(in that case pick a block first in the composite view!)</html>");
                // Exception e = new Exception("Tracking update");
                //p(ErrorHandler.getString(e));
            }
            //   afterGotHeatMap();
//            den.clear();
//            den.repaint();
//            panMain.repaint();
        } else {
            afterGotHeatMap();
        }


    }

    private void afterGotHeatMap() {
        if (!mask.hasRead(BfMaskFlag.RAW)) {
            GuiUtils.showNonModalMsg("Loading image " + mask.getImageFile("chip", BfMaskFlag.RAW, flow, filetype, frame));
            if (this.exp.getNrcols() < 1 || exp.getNrrows() < 1) {
                p("Bad cols rows");
                exp.findColsRows(flow, filetype);
                p("cols rows are now: " + exp.getNrcols() + "/" + exp.getNrrows());
                mask.updateInfo();
            }
            mask.readData(BfMaskFlag.RAW, mask.getImageFile("chip", BfMaskFlag.RAW, flow, filetype, frame));

        }
        // p("Mask cols: "+mask.c)
        den.setScoreMask(mask, 2, flow, filetype, frame);
        if (this.exp.getWellContext() == null || exp.getWellContext().getSelection() == null) {
            int mx = exp.getNrcols() / 2 - 10;
            int my = exp.getNrrows() / 2 - 10;
            den.createDefaultSelection(mx, my, mx + 20, my + 20);

        }
        panMain.repaint();
        den.repaint();
        repaint();
    }

    @Override
    public void taskDone(Task t) {
        p("Task " + t + " is done");
        setCursor(null);
        if (t.isSuccess()) {
            curtask = null;
            afterGotHeatMap();
        } else {
            lastfilewitherror = curtask.getFile();
            curtask = null;
        }

    }

    @Override
    public void frameChanged(int frame) {
        if (frame != this.frame) {
            this.frame = frame;
            update(true);
        }
    }

    private class CreateHeatMapTask extends Task {

        boolean ok;
        String file;

        public CreateHeatMapTask(TaskListener tlistener) {
            super(tlistener, ProgressHandleFactory.createHandle("Reading flow " + flow + ", frame " + frame + " of " + filetype + " file"));
            this.file = mask.getImageFile("chip", BfMaskFlag.RAW, flow, filetype, frame);

        }

        public String getFile() {
            return file;
        }

        @Override
        public Void doInBackground() {
            ok = createImageFileFromScoreFlag(this);
            return null;
        }

        public boolean isSuccess() {
            return ok;
        }
    }

    private boolean createImageFileFromScoreFlag(ProgressListener progress) {
        p("createImageFileFromScoreFlag: Creating chip image");
        if (mask == null) {
            mask = BfHeatMap.getMask(exp);
            den.setExp(exp);
        }
        ChipWellDensity gen = new ChipWellDensity(exp, flow, filetype, frame, 2);
        String msg = null;
        try {
            p("Creating whole whip well density for flow " + flow + "  type " + filetype);
            msg = gen.createHeatMapImages(progress);
            mask.updateInfo();
            //  p("after generate whole chip image: "+mask.)
        } catch (Exception e) {
            msg = e.getMessage();
            p("createImageFileFromScoreFlag: Got an error: " + ErrorHandler.getString(e));

            return false;
        }

        if (msg != null && msg.length() > 0) {
            if (lastmsg == null || !msg.equalsIgnoreCase(lastmsg)) {
                GuiUtils.showNonModalMsg(msg);
            }
            lastmsg = msg;
            return false;
        }
        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bar = new javax.swing.JToolBar();
        jLabel4 = new javax.swing.JLabel();
        btnSelect = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        btnReload = new javax.swing.JButton();
        hint = new javax.swing.JButton();
        panMain = new javax.swing.JPanel();

        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        bar.setRollover(true);

        jLabel4.setText(org.openide.util.NbBundle.getMessage(ChipPanel.class, "ChipPanel.jLabel4.text")); // NOI18N
        bar.add(jLabel4);

        btnSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/select-rectangular.png"))); // NOI18N
        btnSelect.setText(org.openide.util.NbBundle.getMessage(ChipPanel.class, "ChipPanel.btnSelect.text")); // NOI18N
        btnSelect.setToolTipText(org.openide.util.NbBundle.getMessage(ChipPanel.class, "ChipPanel.btnSelect.toolTipText")); // NOI18N
        btnSelect.setFocusable(false);
        btnSelect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSelect.setMargin(new java.awt.Insets(1, 1, 1, 1));
        btnSelect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });
        bar.add(btnSelect);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/maskview/document-export.png"))); // NOI18N
        jButton2.setText(org.openide.util.NbBundle.getMessage(ChipPanel.class, "ChipPanel.jButton2.text")); // NOI18N
        jButton2.setToolTipText(org.openide.util.NbBundle.getMessage(ChipPanel.class, "ChipPanel.jButton2.toolTipText")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        bar.add(jButton2);

        btnReload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/maskview/view-refresh-3.png"))); // NOI18N
        btnReload.setText(org.openide.util.NbBundle.getMessage(ChipPanel.class, "ChipPanel.btnReload.text")); // NOI18N
        btnReload.setToolTipText(org.openide.util.NbBundle.getMessage(ChipPanel.class, "ChipPanel.btnReload.toolTipText")); // NOI18N
        btnReload.setFocusable(false);
        btnReload.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnReload.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnReload.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnReload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReloadActionPerformed(evt);
            }
        });
        bar.add(btnReload);

        hint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/help-hint.png"))); // NOI18N
        hint.setText(org.openide.util.NbBundle.getMessage(ChipPanel.class, "ChipPanel.hint.text")); // NOI18N
        hint.setToolTipText(org.openide.util.NbBundle.getMessage(ChipPanel.class, "ChipPanel.hint.toolTipText")); // NOI18N
        hint.setFocusable(false);
        hint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        hint.setMargin(new java.awt.Insets(1, 1, 1, 1));
        hint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        hint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hintActionPerformed(evt);
            }
        });
        bar.add(hint);

        add(bar, java.awt.BorderLayout.NORTH);

        panMain.setOpaque(false);
        panMain.setLayout(new java.awt.BorderLayout());
        add(panMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        if (exp == null || mask == null) {
            GuiUtils.showNonModalMsg("Got no well context or bfmask file");
            return;
        }
        CoordSelectionPanel pan = new CoordSelectionPanel();
        pan.setMaxX(this.exp.getNrcols());
        pan.setMaxY(this.exp.getNrrows());
        int ans = JOptionPane.showConfirmDialog(this, pan, "Enter a selection:", JOptionPane.OK_CANCEL_OPTION);
        if (ans == JOptionPane.CANCEL_OPTION) {
            return;
        }
        WellCoordinate c1 = pan.getCoord1();
        WellCoordinate c2 = pan.getCoord2();
        WellSelection sel = new WellSelection(c1, c2);
        exp.getWellContext().setSelection(sel);

        LookupUtils.publish(wellSelectionContent, sel);

		}//GEN-LAST:event_btnSelectActionPerformed

        private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
            this.den.export();
		}//GEN-LAST:event_jButton2ActionPerformed

    private void btnReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReloadActionPerformed
        // DELETE FILE IF IT EXISTS

        this.exp = GlobalContext.getContext().getExperimentContext();
        if (exp == null) {
            return;
        }
        mask = BfHeatMap.getMask(exp);
        den.setExp(exp);
        if (mask == null) {
            //  GuiUtils.showNonModalMsg("Got no experiment context yet");
            return;
        }

        String file = mask.getImageFile("chip", BfMaskFlag.RAW, flow, filetype, frame);
        if (file == null) {
            update(true);
        }
        if (FileUtils.exists(file)) {
            File f = new File(file);
            f.delete();
        }
        update(false);
		}//GEN-LAST:event_btnReloadActionPerformed

    private void hintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hintActionPerformed
        doHintAction();
    }//GEN-LAST:event_hintActionPerformed

    private void doHintAction() {
        String msg = "<html><ul>";
        msg += "<li>Move the image around with the <b>right</b> mouse button </li>";
        msg += "<li>Select a coordinate by clicking the <b>left</b> mouse button </li>";
        msg += "<li>Zoom the image in and out with a <b>mouse wheel</b> </li>";
        msg += "</ul></html>";
        JOptionPane.showMessageDialog(this, msg);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar bar;
    private javax.swing.JButton btnReload;
    private javax.swing.JButton btnSelect;
    private javax.swing.JButton hint;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel panMain;
    // End of variables declaration//GEN-END:variables

    private void p(String msg) {
//  System.out.println("ChipPanel: " + msg);
        Logger.getLogger(ChipPanel.class.getName()).log(Level.INFO, msg);
    }
}
