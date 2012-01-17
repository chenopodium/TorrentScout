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
package com.iontorrent.guiutils.netbeans;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Chantal Roth
 */
public class OpenWindowAction extends AbstractAction {

    private Mode mode;
    TopComponent window;

    public OpenWindowAction(Mode mode, TopComponent window) {
        super(mode.getName().replace("_", " "));
        this.window = window;
        this.mode = mode;
        
    }

    public static Action[] getActions(TopComponent window) {
        WindowManager m = WindowManager.getDefault();
        Iterator it = m.getModes().iterator();
        Action[] actions = new Action[m.getModes().size()];
        Mode mode = null;

        for (int i = 0; it.hasNext();) {
            mode = (Mode) it.next();
            actions[i] = new OpenWindowAction(mode, window);
            i++;
        }

        return actions;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        mode.dockInto(window);
        window.open();
        window.requestAttention(true);
        window.requestActive();
        window.requestVisible();
        window.toFront();
    }

    public String getName() {
        return mode.getName();
    }

    public String toSring() {
        return mode.getName();
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(OpenWindowAction.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(OpenWindowAction.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(OpenWindowAction.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("OpenWindowAction: " + msg);
        //Logger.getLogger( OpenWindowAction.class.getName()).log(Level.INFO, msg, ex);
    }
}
