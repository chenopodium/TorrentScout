/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.main.startup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(category = "Help",
id = "com.iontorrent.main.MainAction")
@ActionRegistration(iconBase = "com/iontorrent/main/startup/help-hint.png",
displayName = "#CTL_MainAction")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1),
    @ActionReference(path = "Toolbars/File", position = 1),
    @ActionReference(path = "Shortcuts", name = "C-N"),
    @ActionReference(path = "Shortcuts", name = "D-N"),
    @ActionReference(path = "Shortcuts", name = "F1")
})
@Messages("CTL_MainAction=Torrent Scout Navigator")
public final class MainAction implements ActionListener {

        @Override
    public String toString() {
        return "Helps you to pick components";
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        TopComponent tc = (TopComponent) WindowManager.getDefault().findTopComponent("TorrentScoutMainPageTopComponent");       
        openComponent(tc, true);
    }
    protected static void openComponent(TopComponent tc, boolean attention) {
        if (tc != null) {
            if( !tc.isOpened()) tc.open();
            tc.requestActive();
            tc.requestVisible();
            if (attention) tc.requestAttention(true);
        }
    }
}
