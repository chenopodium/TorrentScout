/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.main.startup;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.FolderManager;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.guiutils.netbeans.OpenWindowAction;
import com.iontorrent.sequenceloading.SequenceLoader;
import com.iontorrent.utils.LookupUtils;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//com.iontorrent.main.startup//TorrentScoutMainPage//EN",
autostore = false)
@TopComponent.Description(preferredID = "TorrentScoutMainPageTopComponent",
iconBase = "com/iontorrent/main/startup/chip_16.png",
persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "com.iontorrent.main.startup.TorrentScoutMainPageTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_TorrentScoutMainPageAction",
preferredID = "TorrentScoutMainPageTopComponent")
public final class TorrentScoutMainPageTopComponent extends TopComponent {

    ImageIcon icon;
    ExperimentContext exp;
    private transient final Lookup.Result<ExperimentContext> expContextResults =
            LookupUtils.getSubscriber(ExperimentContext.class, new SubscriberListener());

    public TorrentScoutMainPageTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "CTL_TorrentScoutMainPageTopComponent"));
        setToolTipText(NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "HINT_TorrentScoutMainPageTopComponent"));
        icon = new ImageIcon(getClass().getResource("startup.png"));
        this.setSize(icon.getIconWidth(), icon.getIconHeight());
        this.setMaximumSize(this.getSize());
        this.setBackground(Color.black);
     //   WindowManagerImpl man = (WindowManagerImpl) WindowManager.getDefault();
//        ModeImpl mode = (ModeImpl) man.findMode(this);
//        man.switchMaximizedMode(mode);
//       // man.
   
        pickregion.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                pickregion.setEnabled(false);
                if (exp.doesExplogHaveBlocks()) {
                    proton.setVisible(true);
                } else {
                    chip.setVisible(true);
                    if (exp.hasBfMask()) bfmask.setVisible(true);
                    else pickregion.setToolTipText("Found no bfmask.bin file for this experiment");
                }
            }
        });
        initActions();
        checkState();
    }

    private void initActions() {
        connect("File", "Open", open);
        connect("File", "Browse", browse);
        connect("File", "Configure", configure);
        
        connect("Pick Region", "BF", bfmask);
        connect("Pick Region", "Whole", chip);
        connect("Pick Region", "Proton", proton);

        connect("View", "Iono", iono);
        connect("View", "Align", align);
        connect("View", "Raw", chart);
        
        connect("Find", "Genome", genome);
        connect("Find", "Find", score);

        connect("Process", "Process", process);
        connect("Process", "Fit", fit);
        connect("Process", "Automate", automate);
        connect("Process", "Edit", masks);
    }

    private class SubscriberListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            final Collection<? extends ExperimentContext> items = expContextResults.allInstances();
            if (!items.isEmpty()) {
                exp = null;
                Iterator<ExperimentContext> it = (Iterator<ExperimentContext>) items.iterator();
                while (it.hasNext()) {
                    exp = it.next();
                    checkState();
                }

            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        process = new javax.swing.JButton();
        masks = new javax.swing.JButton();
        raw = new javax.swing.JButton();
        fit = new javax.swing.JButton();
        automate = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        score = new javax.swing.JButton();
        genome = new javax.swing.JButton();
        find = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        openexperiment = new javax.swing.JButton();
        open = new javax.swing.JButton();
        configure = new javax.swing.JButton();
        browse = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        pickregion = new javax.swing.JButton();
        bfmask = new javax.swing.JButton();
        chip = new javax.swing.JButton();
        proton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        chart = new javax.swing.JButton();
        iono = new javax.swing.JButton();
        view = new javax.swing.JButton();
        align = new javax.swing.JButton();

        setBackground(java.awt.Color.black);
        setForeground(java.awt.Color.white);

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));
        jPanel1.setOpaque(false);

        process.setBackground(new java.awt.Color(153, 204, 255));
        org.openide.awt.Mnemonics.setLocalizedText(process, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.process.text")); // NOI18N

        masks.setBackground(new java.awt.Color(153, 204, 255));
        org.openide.awt.Mnemonics.setLocalizedText(masks, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.masks.text")); // NOI18N

        raw.setBackground(new java.awt.Color(255, 204, 0));
        raw.setFont(new java.awt.Font("Tahoma", 1, 14));
        org.openide.awt.Mnemonics.setLocalizedText(raw, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.raw.text")); // NOI18N
        raw.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.raw.toolTipText")); // NOI18N
        raw.setMaximumSize(null);
        raw.setOpaque(false);
        raw.setPreferredSize(new java.awt.Dimension(111, 25));
        raw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rawActionPerformed(evt);
            }
        });

        fit.setBackground(new java.awt.Color(153, 204, 255));
        org.openide.awt.Mnemonics.setLocalizedText(fit, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.fit.text")); // NOI18N
        fit.setMaximumSize(new java.awt.Dimension(69, 23));

        automate.setBackground(new java.awt.Color(153, 204, 255));
        org.openide.awt.Mnemonics.setLocalizedText(automate, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.automate.text")); // NOI18N
        automate.setMaximumSize(new java.awt.Dimension(69, 23));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(raw, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(masks, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                    .addComponent(automate, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                    .addComponent(fit, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                    .addComponent(process, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {automate, fit, masks, process});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(raw, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(process)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(automate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(masks)
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));
        jPanel2.setOpaque(false);

        score.setBackground(new java.awt.Color(153, 204, 255));
        org.openide.awt.Mnemonics.setLocalizedText(score, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.score.text")); // NOI18N

        genome.setBackground(new java.awt.Color(153, 204, 255));
        org.openide.awt.Mnemonics.setLocalizedText(genome, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.genome.text")); // NOI18N

        find.setBackground(new java.awt.Color(255, 204, 0));
        find.setFont(new java.awt.Font("Tahoma", 1, 14));
        org.openide.awt.Mnemonics.setLocalizedText(find, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.find.text")); // NOI18N
        find.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.find.toolTipText")); // NOI18N
        find.setMaximumSize(null);
        find.setOpaque(false);
        find.setPreferredSize(new java.awt.Dimension(111, 25));
        find.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(find, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(genome, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                            .addComponent(score, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(find, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(score)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(genome)
                .addContainerGap())
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {genome, score});

        jPanel3.setBackground(new java.awt.Color(0, 0, 0));
        jPanel3.setOpaque(false);

        openexperiment.setBackground(new java.awt.Color(255, 204, 0));
        openexperiment.setFont(new java.awt.Font("Tahoma", 1, 14));
        org.openide.awt.Mnemonics.setLocalizedText(openexperiment, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.openexperiment.text")); // NOI18N
        openexperiment.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.openexperiment.toolTipText")); // NOI18N
        openexperiment.setMaximumSize(null);
        openexperiment.setOpaque(false);
        openexperiment.setSelected(true);
        openexperiment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openexperimentActionPerformed(evt);
            }
        });

        open.setBackground(new java.awt.Color(153, 204, 255));
        org.openide.awt.Mnemonics.setLocalizedText(open, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.open.text")); // NOI18N

        configure.setBackground(new java.awt.Color(153, 204, 255));
        org.openide.awt.Mnemonics.setLocalizedText(configure, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.configure.text")); // NOI18N

        browse.setBackground(new java.awt.Color(153, 204, 255));
        org.openide.awt.Mnemonics.setLocalizedText(browse, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.browse.text")); // NOI18N
        browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(openexperiment, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(open, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(configure, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                    .addComponent(browse, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE))
                .addGap(53, 53, 53))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {configure, open});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(openexperiment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(configure)
                .addGap(5, 5, 5)
                .addComponent(browse)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(open)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {configure, open});

        jPanel4.setBackground(new java.awt.Color(0, 0, 0));
        jPanel4.setOpaque(false);

        pickregion.setBackground(new java.awt.Color(255, 204, 0));
        pickregion.setFont(new java.awt.Font("Tahoma", 1, 14));
        org.openide.awt.Mnemonics.setLocalizedText(pickregion, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.pickregion.text")); // NOI18N
        pickregion.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.pickregion.toolTipText")); // NOI18N
        pickregion.setMaximumSize(null);
        pickregion.setOpaque(false);
        pickregion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pickregionActionPerformed(evt);
            }
        });

        bfmask.setBackground(new java.awt.Color(153, 204, 255));
        org.openide.awt.Mnemonics.setLocalizedText(bfmask, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.bfmask.text")); // NOI18N

        chip.setBackground(new java.awt.Color(153, 204, 255));
        org.openide.awt.Mnemonics.setLocalizedText(chip, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.chip.text")); // NOI18N

        proton.setBackground(new java.awt.Color(153, 204, 255));
        org.openide.awt.Mnemonics.setLocalizedText(proton, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.proton.text")); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pickregion, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(chip, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bfmask, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(proton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {chip, proton});

        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pickregion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chip)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bfmask)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(proton)
                .addContainerGap())
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {chip, proton});

        jPanel5.setBackground(new java.awt.Color(0, 0, 0));
        jPanel5.setOpaque(false);

        chart.setBackground(new java.awt.Color(153, 204, 255));
        org.openide.awt.Mnemonics.setLocalizedText(chart, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.chart.text")); // NOI18N

        iono.setBackground(new java.awt.Color(153, 204, 255));
        org.openide.awt.Mnemonics.setLocalizedText(iono, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.iono.text")); // NOI18N

        view.setBackground(new java.awt.Color(255, 204, 0));
        view.setFont(new java.awt.Font("Tahoma", 1, 14));
        org.openide.awt.Mnemonics.setLocalizedText(view, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.view.text")); // NOI18N
        view.setToolTipText(org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.view.toolTipText")); // NOI18N
        view.setMaximumSize(null);
        view.setOpaque(false);
        view.setPreferredSize(new java.awt.Dimension(111, 25));
        view.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewActionPerformed(evt);
            }
        });

        align.setBackground(new java.awt.Color(153, 204, 255));
        org.openide.awt.Mnemonics.setLocalizedText(align, org.openide.util.NbBundle.getMessage(TorrentScoutMainPageTopComponent.class, "TorrentScoutMainPageTopComponent.align.text")); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(view, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(iono)
                            .addComponent(align)))))
        );

        jPanel5Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {align, chart, iono});

        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(view, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(iono)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(align)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {align, chart, iono});

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(71, 71, 71)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(956, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public Action[] getActions() {
        return OpenWindowAction.getActions(this);
    }

    private void pickregionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pickregionActionPerformed
    }//GEN-LAST:event_pickregionActionPerformed

    public void connect(String category, String name, JButton btn) {
        final String FOLDER = "Actions/" + category + "/";

       // p("Finding action " + FOLDER + name);

        List<Action> items = (List<Action>) Utilities.actionsForPath(FOLDER);
      //  p("Got: " + items);
        for (Action ac : items) {
            if (ac.toString().indexOf(name) > -1) {
                Actions.connect(btn, ac);
                btn.setToolTipText("<html>"+ac.toString()+"</html>");
                return;
            }
        }
        p("---------- Could not find Action "+FOLDER +" with name "+name+":"+items);

    }

    private void checkState() {
        openexperiment.setEnabled(true);
        browse.setVisible(false);
        configure.setVisible(false);
        open.setVisible(false);
        

        pickregion.setEnabled(false);
        chip.setVisible(false);
        bfmask.setVisible(false);
        proton.setVisible(false);

        view.setEnabled(false);
        iono.setVisible(false);
        align.setVisible(false);
        chart.setVisible(false);
        
        raw.setEnabled(false);
        process.setVisible(false);
        automate.setVisible(false);
        fit.setVisible(false);
        masks.setVisible(false);


        find.setEnabled(false);
        score.setVisible(false);
        genome.setVisible(false);
        if (GlobalContext.getContext().getExperimentContext() != null) {
            pickregion.setEnabled(true);
            exp = GlobalContext.getContext().getExperimentContext();
            if (exp.doesExplogHaveBlocks()) {
                // no block picked yet
            } else {
                // regular exp or insde a block
                raw.setEnabled(true);
                SequenceLoader loader = SequenceLoader.getSequenceLoader(this.exp, false, true);
                
                find.setEnabled(true);
                view.setEnabled(true);
//                if (exp.hasSff() || exp.hasBam() || exp.hasWells()) {
//                    if (exp.hasSff() || exp.hasBam()) {
//                        find.setEnabled(true);
//                    }
//                    else find.setToolTipText("I found no .sff or .bam file for this experiment");
//                    view.setEnabled(true);
//                }
//                else {
//                    // explain why no find?
//                    find.setToolTipText("I found no 1.wells, .sff or .bam file for this experiment");
//                    view.setToolTipText("I found no 1.wells, .sff or .bam file for this experiment");
//                }
            }


        }
    }
    private void openexperimentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openexperimentActionPerformed
        Preferences p = Preferences.userNodeForPackage(com.iontorrent.main.options.TorrentScoutSettingsPanel.class);
        boolean firstTime = p.getBoolean("first_time_start", true);
        FolderManager manager = FolderManager.getManager();
        String default_rule = manager.setDefaultRule();

        p("firstTime=" + firstTime);

//        if (firstTime
//                || default_rule
//                == null) {
//            TorrentScoutSettingsPanel pan = new TorrentScoutSettingsPanel(new TorrentScoutSettingsOptionsPanelController());
//            int ok = JOptionPane.showConfirmDialog(this, pan, "Please configure Torrent Scout", JOptionPane.OK_CANCEL_OPTION);
//            if (ok == JOptionPane.OK_OPTION) {
//                pan.store();
//            }
//            default_rule = manager.setDefaultRule();
//            //OptionsDisplayer.getDefault().open("TorrentScoutOptions/TorrentScoutSettings");
//        }
        //   p.put("first_time_start", "false");

        this.openexperiment.setEnabled(false);
        open.setVisible(true);
        configure.setVisible(true);
        browse.setVisible(
                true);

    }//GEN-LAST:event_openexperimentActionPerformed

    private void findActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findActionPerformed
        find.setEnabled(false);
        SequenceLoader loader = SequenceLoader.getSequenceLoader(this.exp, false, true);
        p("bam: "+loader.foundBamFile()+", exp.hasbam: "+exp.hasBam());
        score.setVisible(true);
        genome.setVisible(true);
        if (!exp.hasBam() &&  !exp.hasSff()) {
            score.setEnabled(false);
            score.setText("Requires a .bam and a .sff file (at least one of them)");
        }
        else score.setEnabled(true);
        if (!exp.hasBam()) {
            genome.setEnabled(false);
            genome.setText("Requires a .bam file");
        }
        else genome.setEnabled(true);
    }//GEN-LAST:event_findActionPerformed

    private void viewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewActionPerformed
        view.setEnabled(false);
        SequenceLoader loader = SequenceLoader.getSequenceLoader(this.exp, false, true);
        p("bam: "+loader.foundBamFile()+", exp.hasbam: "+exp.hasBam());
        iono.setVisible(true);
        align.setVisible(true);
        if (!exp.hasSff() && !exp.hasWells()) {
            iono.setEnabled(false);
            iono.setToolTipText("Requires a 1.wells and ideally a .sff file");
        }
        else iono.setEnabled(true);
        if(!exp.hasBam()) {
            align.setEnabled(false);
            align.setToolTipText("Requires a .bam file");
        }
        else align.setEnabled(true);
        chart.setVisible(true);
    }//GEN-LAST:event_viewActionPerformed

    private void rawActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rawActionPerformed
        raw.setEnabled(false);
        process.setVisible(true);
        fit.setVisible(true);
        automate.setVisible(true);
        masks.setVisible(true);
    }//GEN-LAST:event_rawActionPerformed

    private void browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_browseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton align;
    private javax.swing.JButton automate;
    private javax.swing.JButton bfmask;
    private javax.swing.JButton browse;
    private javax.swing.JButton chart;
    private javax.swing.JButton chip;
    private javax.swing.JButton configure;
    private javax.swing.JButton find;
    private javax.swing.JButton fit;
    private javax.swing.JButton genome;
    private javax.swing.JButton iono;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JButton masks;
    private javax.swing.JButton open;
    private javax.swing.JButton openexperiment;
    private javax.swing.JButton pickregion;
    private javax.swing.JButton process;
    private javax.swing.JButton proton;
    private javax.swing.JButton raw;
    private javax.swing.JButton score;
    private javax.swing.JButton view;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
        toFront();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black);
        g.fillRect(0,0,getWidth(), getHeight());
        if (icon != null) {
            //   p("Drawing icon omage: "+icon.getIconWidth()+"/"+icon);
            g.drawImage(icon.getImage(), 0, 0, this);
        }
        //else p("Got no icon image ");
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version


    }

    private static void p(String msg) {
        Logger.getLogger(TorrentScoutMainPageTopComponent.class.getName()).log(Level.INFO, msg);
    }
}
