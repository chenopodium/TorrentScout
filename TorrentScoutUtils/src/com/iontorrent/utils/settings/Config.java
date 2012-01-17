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
package com.iontorrent.utils.settings;

import com.iontorrent.utils.io.FileTools;
import com.iontorrent.utils.StringTools;
import com.iontorrent.utils.SystemTool;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration.Node;

import org.apache.commons.configuration.XMLConfiguration;

/**
 * Base class for configuration
 * 
 *
 */
public class Config {

    protected XMLConfiguration fConfig;
    protected ArrayList<String> keys;
    private URL url;
    //private File file;

    public Config(File userFile, URL initialConfigFile) {

        if (userFile == null) {
            warn("Got no user file:" + userFile);
            //userConfigFile = initialConfigFile
        }
        if (!userFile.exists() || userFile.length() < 10) {
            p("Copy source to target first");
            File parent = userFile.getParentFile();
            if (!parent.exists()) {
                p("Parent " + parent + " does not exist, creating folder");
                boolean ok = parent.mkdirs();
                parent.setExecutable(true);
                parent.setWritable(true);
                if (!parent.exists()) {
                    err("Parent folder still does not exist");
                    userFile = new File(parent.getParentFile() + "/" + userFile.getName());
                    p("Trying " + userFile);
                }
            }
            boolean ok = FileTools.copyUrl(initialConfigFile, userFile);
            if (!ok) {
                err("Was uable to copy " + initialConfigFile + " to " + userFile);
            }
        } else {
            p("Already found user file " + userFile);
        }
        try {
            this.url = userFile.toURI().toURL();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
        p("Reading XML file from URL:" + url);
        this.setXMLConfig(url);

        init();
    }

    public Config(URL initialConfigFile) {
        this.url = initialConfigFile;
        p("Reading XML file from URL:" + url);
        this.setXMLConfig(initialConfigFile);

        init();
    }

    public HierarchicalConfiguration.Node createNode(String attName, String value) {
        HierarchicalConfiguration.Node node = new HierarchicalConfiguration.Node(attName);
        node.setValue(value);
        return node;
    }

    public void addAttribute(String attName, String value, List<HierarchicalConfiguration.Node> attNodes) {
        HierarchicalConfiguration.Node attrNode = createNode(attName, value);
        // attrNode.set(true);
        attNodes.add(attrNode);
    }

    public void delete(String key) {
        fConfig.clearTree(key);
        keys.remove(key);
    }

    public void addNodes(String key, List<HierarchicalConfiguration.Node> nodes) {
        fConfig.addNodes(key, nodes);

        this.keys.add(key);
        try {
            save();
        } catch (Exception ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getUrl() {
        return url.toString();
    }

    public ArrayList<String> getKeys() {
        
        return keys;
    }

    private void init() {
        keys = new ArrayList<String>();
        if (fConfig == null) return;
        Iterator it = fConfig.getKeys();
       

        while (it.hasNext()) {
            String key = (String) it.next();
            key = key.toUpperCase();
            int dot = (key.indexOf("."));
            if (dot > -1) {
                key = key.substring(0, dot);
            }
            if (!keys.contains(key)) {
                // p("Adding key:" + key);
                keys.add(key.toUpperCase());
            }
        }
        Collections.sort(keys);
    }

    public int getOrder(String key) {
        key = key.toUpperCase();
        for (int i = 0; i < keys.size(); i++) {
            if (key.equalsIgnoreCase(keys.get(i))) {
                //	p("found key "+key+" at pos "+i);
                return i;
            }
        }
        //	p("Could NOT find key "+key+", using pos -1");
        return -1;
    }

    public String getStringValue(String configName) {
        String res = fConfig.getString(configName);

        if (res == null) {
           // p(configName + " missing (" + url + ")");
            return "";
        } else {
            return res;
        }
    }

    public boolean getBooleanValue(String configName) {
        try {
            return fConfig.getBoolean(configName);
        } catch (Exception e) {
        }
        return false;
    }

    public int getIntValue(String configName) {
        int i = 0;
        //	p("getting "+configName+":"+fConfig.getString(configName));
        try {
            i = fConfig.getInt(configName);
        } catch (Exception e) {
            if (e instanceof NoSuchElementException) {
                p(configName + " missing (" + url + ")");
            } else {
                p("Value " + configName + " can't be parsed: " + e.getMessage() + " file:" + url);
            }
        }
        return i;
    }

    public double getDoubleValue(String configName) {
        double d = 0;
        try {
            d = fConfig.getDouble(configName);
        } catch (Exception e) {
            if (e instanceof NoSuchElementException) {
                p(configName + " missing (" + url + ")");
            } else {
                p("Double Value " + configName + " can't be parsed: " + e.getMessage() + " file:" + url);
            }
        }
        return d;
    }

    public long getLongValue(String key) {
        return fConfig.getLong(key);
    }

    public String[] getStrings(String key) {
        return fConfig.getStringArray(key);
    }

    public void setProperty(String propName, String value) {
        fConfig.setProperty(propName, value);
        
    }

    public void save() throws Exception {
        fConfig.save();
    }

    protected void setXMLConfig(String configFile) {
        try {
            fConfig = new XMLConfiguration(configFile);
        } catch (Exception cex) {

            err("Failed to retriev service configuration file. "
                    + configFile);
        }
    }

    protected void setXMLConfig(URL configFile) {
        try {
            fConfig = new XMLConfiguration(configFile);
        } catch (Exception cex) {
            err("Couldn't get config xml file from url "
                    + configFile+": "+cex.getMessage());
        }
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(Config.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(Config.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(Config.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("Config: " + msg);
        //Logger.getLogger( Config.class.getName()).log(Level.INFO, msg, ex);
    }
}
