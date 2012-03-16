/*
 * Copyright (C) 2012 Life Technologies Inc.
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
package com.iontorrent.guiutils;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.TimerTask;
import javax.swing.JDialog;
import com.sun.awt.AWTUtilities;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class Fader extends TimerTask {

    private JDialog jDialog;
    double visibility;
    boolean trans;

    public Fader(JDialog jDialog) {
        this.jDialog = jDialog;
        visibility = AWTUtilities.getWindowOpacity(jDialog);

        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] gs =
                    ge.getScreenDevices();
            for (int j = 0; j < gs.length; j++) {
                GraphicsDevice gd = gs[j];
                GraphicsConfiguration[] gc =  gd.getConfigurations();
                for (int i = 0; i < gc.length; i++) {
                    if (AWTUtilities.isTranslucencyCapable(gc[i])) {
                        trans = true;
                    }
                }
            }
        } catch (Exception e) {
        }
    }
    //As Fader extends from Timer, it's the run() method which does the main job

    @Override
    public void run() {
        //The opacity is reduced by 0,01f steps
        //If this value equals 0 (invisible), we close the JDialog with dispose()
        visibility = visibility - 0.01;
        if (visibility < 0.01) {
            jDialog.dispose();
        }
        if (trans) {
            float op = AWTUtilities.getWindowOpacity(jDialog);
            if (op > 0.01) {
                AWTUtilities.setWindowOpacity(jDialog, op - 0.01f);
            } else {
                jDialog.dispose();
            }
        }
    }
}
