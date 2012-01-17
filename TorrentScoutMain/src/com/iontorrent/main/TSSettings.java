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
package com.iontorrent.main;

import com.iontorrent.utils.SystemTool;
import com.iontorrent.utils.ToolBox;
import com.iontorrent.utils.settings.Config;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 *
 * @author Chantal Roth
 */
public class TSSettings {

    private static SystemTool system = new SystemTool();
    private static File userFile = new File(system.getUserHome() + "/.nbapp-torrentscout/settings.xml");
    private static Config config = new Config(TSSettings.class.getResource("settings.xml"));
    private static String KEY_NAME="Settings";
    
  
    public TSSettings() {     
    }
    
    public static double getVersion() {
        return config.getDoubleValue(KEY_NAME + ".version"); 
    }
    public static boolean isOverwriteFolderConfig() {
        return config.getBooleanValue(KEY_NAME + ".overwrite_folderconfig");
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(TSSettings.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(Exception ex) {
        Logger.getLogger(TSSettings.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
    }

    private void err(String msg) {
        Logger.getLogger(TSSettings.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(TSSettings.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("TSSettings: " + msg);
        //Logger.getLogger( TSSettings.class.getName()).log(Level.INFO, msg, ex);
    }

   
  
}
