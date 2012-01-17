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
package com.iontorrent.expmodel;

import com.iontorrent.utils.SystemTool;
import com.iontorrent.utils.ToolBox;
import com.iontorrent.utils.io.FileTools;
import com.iontorrent.utils.io.FileUtils;
import com.iontorrent.utils.settings.Config;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 *
 * @author Chantal Roth
 */
public class FolderConfig {

    private static SystemTool system = new SystemTool();
    private static File userFile = new File(system.getUserHome() + "/.nbapp-torrentscout/folderconfig.xml");
    private static URL initialUrl;
    private static Config config;

    public static Config getConfig() {
        return getConfig(false);
    }
    public static Config getConfig(boolean anyway) {
        if (!anyway && config != null) {
            return config;
        }
        getInitialUrl();
        p("Reading deployed file again: " + initialUrl);
        config = new Config(userFile, initialUrl);
        if (config == null || config.getKeys().size() < 1) {
            if (userFile.exists()) {
                p("Deleteing local properties file " + userFile);
                userFile.delete();
                getConfig();
            }
        }
        return config;
    }
    private String key;
    private String msg;

    public static void resetFolderConfig() {
        String file = figureOutFileName();
        //JOptionPane.showMessageDialog(null, "Reading config file "+file);
        resetFolderConfig(file);

    }

    public static String figureOutFileName() {        
        String file = "folderconfig.xml";
        
        return file;
    }

    public static URL getInitialUrl() {
        String file = figureOutFileName();
        initialUrl = initialUrl = FolderConfig.class.getResource(file);
        return initialUrl;
    }

    /** delete local properties file and restore deployed one */
    public static void resetFolderConfig(String filename) {
        p("Resetting folder config: "+filename);
        if (userFile.exists()) {
            p("Deleteing local properties file " + userFile);
            userFile.delete();                        
        }
        else p("Was not able to find user file: "+userFile);
        
        getConfig(true);
    }

    public String getKey() {
        return key;
    }

    public FolderConfig(String key) {
        this.key = key.toLowerCase();

    }

    public static String getFileVersion() {
        getConfig();
        return config.getStringValue("version");
    }

    public String getBaseDir() {
        ArrayList<String> dirs = getBaseDirs();
        if (dirs == null || dirs.size() < 1) {
            //  p("No base dirs");
            return "";
        }
        int index = getDefaultBaseDir();
        if (index < 0 || index >= dirs.size()) {
            p("Base dir index out of bounds: " + index + " dirs: " + dirs.size());
            index = 0;
        }

        String dir = dirs.get(index).trim();
        dir = FileTools.addSlashOrBackslash(dir);
        //   p("returning basedir with index "+index+" of "+dirs.size()+":"+dir);
        return dir;

    }

    public ArrayList<String> getBaseDirs() {
        String dirs = getBaseDirsString();

        // p("Basedirstring: "+dirs);

        ArrayList<String> dirlist = ToolBox.splitString(dirs, " ");
        return dirlist;
    }

    public void checkForUrl() {
        String url = findUrl();
        if (url != null) {
            JOptionPane.showMessageDialog(null,
                    "<html>" + url + " in " + key + "/" + getName() + " appears to be a URL.<br>If you use URLS, the program will have to"
                    + "<br><b><font color ='aa0000'>download most data files to the local drive!</b></font>"
                    + "<br>You should do this only if there is really no other way to access the raw and results data!"
                    + "<br>It will make things very slow, use a lot of space, and may also put a lot of strain<br>"
                    + "on your torrent server. <br><b>Please try to use mounted drives or symbolic links or similar.</b></html>", "I found a URL in " + key + "/" + getName(),
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public String findUrl() {
        ArrayList<String> dirs = getBaseDirs();
        for (String dir : dirs) {
            if (FileUtils.isUrl(dir)) {
                return dir;
            }
        }
        if (FileUtils.isUrl(this.getCacheRule())) {
            return this.getCacheRule();
        }
        if (FileUtils.isUrl(this.getRawRule())) {
            return this.getRawRule();
        }
        if (FileUtils.isUrl(this.getResultsRule())) {
            return this.getResultsRule();
        }
        return null;
    }

    public String getBaseDirsString() {
        if (config == null) {
            err("Got no config");
            return null;
        }
        if (key == null) {
            err("No key");
            return null;
        }
        String dirs = config.getStringValue(key + ".base_directories");
        if (dirs == null) {
            dirs = "";
        }
        dirs = dirs.trim();
        if (dirs.startsWith("<![CDATA[")) {
            dirs = dirs.substring(9);
            dirs = dirs.substring(0, dirs.length() - 3);
        }

        if (dirs.startsWith("[")) {
            dirs = dirs.substring(1);
        }
        if (dirs.endsWith("]")) {
            dirs = dirs.substring(0, dirs.length() - 1);
        }
        if (dirs.startsWith("[")) {
            dirs.substring(1);
        }
        if (dirs.startsWith("[")) {
            dirs.substring(1);
        }
        if (dirs.endsWith("]")) {
            dirs.substring(0, dirs.length() - 1);
        }
        if (dirs.endsWith("]")) {
            dirs.substring(0, dirs.length() - 1);
        }

        if (dirs.endsWith("'")) {
            dirs = dirs.substring(0, dirs.length() - 1);
        }
        if (dirs.startsWith("'")) {
            dirs.substring(1);
        }

        if (dirs.endsWith("\"")) {
            dirs = dirs.substring(0, dirs.length() - 1);
        }
        if (dirs.startsWith("\"")) {
            dirs.substring(1);
        }

        dirs = dirs.replace(",", " ");
        dirs = dirs.replace(",", " ");
        dirs = dirs.replace("  ", " ");
        return dirs.trim();
    }

    public void setBaseDirs(String dirs) {
        if (dirs != null) {
            dirs = dirs.trim();
        }
        //p("Setting base dirs to: "+dirs);
        dirs = dirs.replace(",", " ");

        config.setProperty(key + ".base_directories", dirs);
    }

    public void setBaseDir(int nr) {
        config.setProperty(key + ".default_base_dir", "" + nr);
    }

    public int getDefaultBaseDir() {
        return config.getIntValue(key + ".default_base_dir");
    }

    public void setDefaultBaseDir(int i) {
        config.setProperty(key + ".default_base_dir", "" + i);
    }

    public boolean has(String key) {
        if (getName() == null || getName().trim().length() < 1) {
            return false;
        } else {
            return true;
        }
    }

    public static void delete(String key) {
        config.delete(key);

    }

    public static FolderConfig addConfig(String key) {
        key = key.toLowerCase();
        List<HierarchicalConfiguration.Node> nodes = new ArrayList<HierarchicalConfiguration.Node>();
        config.addAttribute("name", key, nodes);
        config.addAttribute("server", "localhost:5432", nodes);
        config.addAttribute("raw_rule", "${BASE}/${EXP_DIR}", nodes);
        config.addAttribute("cache_rule", "~/torrentscout/${EXP_NAME}", nodes);
        config.addAttribute("results_rule", "${BASE}/${RESULT_DIR}", nodes);

        config.addAttribute("default_base_dir", "0", nodes);
        config.addAttribute("base_directories", "", nodes);
        config.addAttribute("isdefault", "false", nodes);

        config.addNodes(key, nodes);
        return new FolderConfig(key);

    }

    public void addNodes(String key, List<HierarchicalConfiguration.Node> nodes) {
        config.addNodes(key, nodes);
    }

    public String getRawRule() {
        return config.getStringValue(key + ".raw_rule");
    }

    public String getResultsRule() {
        return config.getStringValue(key + ".results_rule");
    }

    public String getCacheRule() {
        return config.getStringValue(key + ".cache_rule");
    }

    public void setCacheRule(String rule) {
        config.setProperty(key + ".cache_rule", rule);
    }

    public void setRawRule(String rule) {
        config.setProperty(key + ".raw_rule", rule);
    }

    public void setResultsRule(String rule) {
        config.setProperty(key + ".results_rule", rule);
    }

    public void setServer(String rule) {
        config.setProperty(key + ".server", rule);
    }

    @Override
    public String toString() {
        return getName();
    }

    public void setName(String rule) {
        config.setProperty(key + ".name", rule);
    }

    public static FolderConfig add(String key, String name) {
        FolderConfig fc = new FolderConfig(key);

        fc.setName(name);

        return fc;
    }

    public void save() {
        p("Storing config "+this.key);
        try {
            config.save();
        } catch (Exception ex) {
            err("Could not store config"+key+": "+ex, ex);
        }
    }

    public String getName() {
        return config.getStringValue(key + ".name");
    }

    public String getServer() {
        return config.getStringValue(key + ".server");
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(FolderConfig.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(Exception ex) {
        Logger.getLogger(FolderConfig.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
    }

    private void err(String msg) {
        Logger.getLogger(FolderConfig.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(FolderConfig.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("FolderConfig: " + msg);
        //Logger.getLogger( FolderConfig.class.getName()).log(Level.INFO, msg, ex);
    }

    public boolean isDefault() {
        return config.getBooleanValue(key + ".isdefault");
    }

    public void setDefault(boolean b) {
        config.setProperty(key + ".isdefault", "" + b);
    }

    public String getUrl() {
        return this.getServer();
    }

    public ArrayList<String> getKeys() {
        return config.getKeys();
    }

    public String getFstab() {
        return config.getStringValue(key + ".fstab");
    }

    public void setFstab(String fs) {
        config.setProperty(key + ".fstab", fs);
    }

    public String getOS() {
        String os = config.getStringValue(key + ".os");
        if (os == null) {
            String name = this.getName().toLowerCase();
            if (name.indexOf("unix") > -1) {
                os = "unix";
            } else if (name.indexOf("mac") > -1) {
                os = "mac";
            } else {
                os = "windows";
            }
        }
        return os;
    }

    public void setOS(String os) {
        config.setProperty(key + ".os", os);
    }
}
