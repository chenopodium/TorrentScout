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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class Settings {

    public static int PIN_MAX=  16000;
    public static int PIN_MIN= -16000;
    
    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, msg, ex);
    }
    
    private static void err(String msg) {
        Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, msg);
    }
    
    private static void warn(String msg) {
        Logger.getLogger(Settings.class.getName()).log(Level.WARNING, msg);
    }
    
    private static void p(String msg) {
        //System.out.println("Settings: " + msg);
        Logger.getLogger(Settings.class.getName()).log(Level.INFO, msg);
    }
}
