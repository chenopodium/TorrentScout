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

public class Fader extends TimerTask {

    private JDialog jDialog;

    public Fader(JDialog jDialog) {
        this.jDialog = jDialog;
    }
    //As Fader extends from Timer, it's the run() method which does the main job

    @Override
    public void run() {
        //The opacity is reduced by 0,01f steps
        //If this value equals 0 (invisible), we close the JDialog with dispose()
        if (AWTUtilities.getWindowOpacity(jDialog) > 0.01f){
            AWTUtilities.setWindowOpacity(jDialog, AWTUtilities.getWindowOpacity(jDialog) - 0.01f);
        }
        else {
            jDialog.dispose();
        }
    }
}
