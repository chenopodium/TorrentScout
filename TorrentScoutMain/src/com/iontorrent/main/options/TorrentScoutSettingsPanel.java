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
package com.iontorrent.main.options;

import com.iontorrent.expmodel.FolderConfig;
import com.iontorrent.expmodel.FolderManager;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.utils.LookupUtils;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.openide.util.lookup.InstanceContent;

public final class TorrentScoutSettingsPanel extends javax.swing.JPanel {

    private final TorrentScoutSettingsOptionsPanelController controller;
  //  private boolean DOACTIONS;
    FolderManager manager;
    private String default_rule;
    RulePanel rulePanel;
    FolderConfig config;
    GlobalContext context;
    SiteList siteList;
    JButton btnAdd;
    JButton btnDel;
    JButton btnReset;
    private String lastmsg;
    private transient final InstanceContent globalContent = LookupUtils.getPublisher(GlobalContext.class);

   public TorrentScoutSettingsPanel(TorrentScoutSettingsOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
        context = GlobalContext.getContext();
        manager = FolderManager.getManager();
        // TODO listen to changes in form fields and call controller.changed()

        siteList = new SiteList();


    //    DOACTIONS = true;
        siteList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {

            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listRulesValueChanged(evt);
            }
        });


        //context.setContext(default_rule);

        // publishContext();
        siteList.setToolTipText("You can also manually edit the file folderconfig.xml in the .nbapp-torrentscout folder");

        add("West", siteList);

        JPanel pan = new JPanel();
        pan.setLayout(new FlowLayout());
        btnAdd = new JButton("Add");
        pan.add(btnAdd);
        btnDel = new JButton("Delete");
        pan.add(btnDel);
        btnReset = new JButton("Reset config file");
        pan.add(btnReset);
        btnReset.setToolTipText("Copies the folderconfig.xml file from the deployed jar file to the user_home/.nbapp-torrentscout/folderconfig.xml");
        add("South", pan);
        
        initView();
        
        btnAdd.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (addConfig()) {
                    return;
                }
            }
        });
        btnReset.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                manager.resetFolderConfig();
                JOptionPane.showMessageDialog(rulePanel, "I reloaded the configs from the deployed file.\nPlease close and reopen the options panel to see the changes");
            }
        });
        btnDel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                deleteConfig();

            }
        });

    }

    public void initView() {
         manager = FolderManager.getManager();
        // TODO listen to changes in form fields and call controller.changed()

        FolderManager.setDefaultRule();
        default_rule = manager.getRule();
        if (default_rule != null) {
            siteList.setSelectedValue(default_rule);
            update(default_rule);
        }
        
      //  GuiUtils.showNonModelMsg("initView: got rule:"+default_rule);
    }

    public void saveConfig() {
        if (config != null) {
            try {
                config.save();
                if (config.isDefault()) {
                    default_rule = config.getName();
                }
                //manager.ruleChanged();
            } catch (Exception ex) {
                err("Could not save config", ex);
            }
        }
    }

    private void deleteConfig() throws HeadlessException {
        FolderConfig selected = siteList.getSelectedValue();
        if (selected == null) {
            return;
        }
        int ans = JOptionPane.showConfirmDialog(this, "Do you really want to delete site " + selected + "?");
        if (ans == JOptionPane.OK_OPTION) {
            FolderConfig.delete(selected.getKey());
        }
    }

    private boolean addConfig() throws HeadlessException {
        String name = JOptionPane.showInputDialog(this, "Please enter a name for this configuration:");
        p("Got name: " + name);
        if (name == null) {
            return true;
        }
        FolderConfig newconf = FolderConfig.addConfig(name);
        newconf.save();
        this.siteList.add(newconf);
        update(name);
        return false;
    }

    private void listRulesValueChanged(javax.swing.event.ListSelectionEvent evt) {
//        if (!DOACTIONS) {
//            return;
//        }

        String rulename = siteList.getSelectedValue().getKey();
        update(rulename);

    }

    private void publishContext() {
        if (context == null) {
            return;
        }
        saveConfig();
        GlobalContext.setContext(context);
        globalContent.remove(context);
        LookupUtils.publish(globalContent, context);
    }

    private void update(String rulename) {
        p("user clicked on " + rulename);
        saveConfig();
        config = manager.getConfig(rulename);
     //   context.setContext(rulename);
        default_rule = rulename;
        p("Got selected config:" + config);
        if (rulePanel != null) {
            remove(rulePanel);
        }
        rulePanel = new RulePanel(config);
        add("Center", rulePanel);
        invalidate();
        revalidate();
//        repaint();
 //       publishContext();
      //  this.paintImmediately(0, 0, 800, 800);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    void load() {
        // TODO read settings and initialize GUI
        // Example:        
        // someCheckBox.setSelected(Preferences.userNodeForPackage(TSGeneralSettingsPanel.class).getBoolean("someFlag", false));
        // or for org.openide.util with API spec. version >= 7.4:
        // someCheckBox.setSelected(NbPreferences.forModule(TSGeneralSettingsPanel.class).getBoolean("someFlag", false));
        // or:
        // someTextField.setText(SomeSystemOption.getDefault().getSomeStringProperty());
       initView();

    }

    public void store() {
        // TODO store modified settings
        // Example:
        // Preferences.userNodeForPackage(TSGeneralSettingsPanel.class).putBoolean("someFlag", someCheckBox.isSelected());
        // or for org.openide.util with API spec. version >= 7.4:
        // NbPreferences.forModule(TSGeneralSettingsPanel.class).putBoolean("someFlag", someCheckBox.isSelected());
        // or:
        // SomeSystemOption.getDefault().setSomeStringProperty(someTextField.getText());


        saveConfig();
        if (config != null) {
            try {
                // check if thereis no default rule
                if (manager.setDefaultRule()== null) {
                    config.setDefault(true);
                }
                manager.setDefaultRule();                            
            } catch (Exception ex) {
                err("Could not save config", ex);
            }
            context.setContext(config.getKey());
            publishContext();
            manager.ruleChanged(true);
        }
        p("store: Saved config, publishing global context, setting global context");        
        
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(TorrentScoutSettingsPanel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(TorrentScoutSettingsPanel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(TorrentScoutSettingsPanel.class.getName()).log(Level.WARNING, msg);
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }

    private void p(String string) {
        System.out.println("TSGeneralSettingsPanel: " + string);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
