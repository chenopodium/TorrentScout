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

import com.iontorrent.utils.io.FileUtils;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class GlobalContext {

    private static GlobalContext context;
    private FolderManager manager;
    public static boolean DEBUG = true;

    public static boolean isDebug() {
        return DEBUG;
    }

    public GlobalContext() {

        manager = FolderManager.getManager();
    }

    public FolderManager getManager() {
        return manager;
    }

    public void setContext(String rulekey) {
        manager.setRule(rulekey, false);
    }

    public void setExperimentContext(ExperimentContext exp, boolean updateFolders) {        
        manager.setExperimentContext(exp, updateFolders);
    }

    public ExperimentContext getExperimentContext() {
        return manager.getExperimentContext();
    }

    public String verify() {
        String msg = "";
        if (this.getExperimentContext() == null) {
            return null;
        }
        ExperimentContext exp = getExperimentContext();
        File d = new File(exp.getCacheDir());
        if (!d.exists()) {
            d.mkdirs();
            d.setExecutable(true);
            d.setWritable(true);
        }
        if (!d.exists()) {
            msg = "<li>The <b>cache</b> dir <b>" + d.toString() + "</b> does not seem to exist</li>";
        }
        d = new File(exp.getRawDir());
        if (!d.exists()) {
            msg += "<li>The <b>raw</b> dir <b>" + d.toString() + "</b> does not seem to exist</li>";
        }
        d = new File(exp.getResultsDirectory());
        if (!d.exists()) {
            msg += "<li>The <b>results</b> dir <b>" + d.toString() + "</b> does not seem to exist</li>";
        }
        if (msg.length() > 0) {
            msg = "There is a problem with the directories:<ul>" + msg;
            msg += "</ul><font color='aa0000'>Please check your folder rule settings</font><br>";
            // manager.ruleChanged();
        }
        return msg;
    }

    public String getContextInfo(boolean alsoOkMsgs) {

        if (getExperimentContext() == null) {
            return "GlobalContext has no experiment context";
        }
        ExperimentContext exp = getExperimentContext();

        String msg = "";
        String d = exp.getRawDir();
        if (d == null) {
            msg = "<font color='aa0000'>Got no raw directory info</font><br> ";
        } else if (d.length() < 2) {
            msg = "<font color='aa0000'>Strange raw path <b>" + d + "</b>, check the rules or select an experiment</font><br> ";
        }
        else if (FileUtils.isUrl(d) && !FileUtils.exists(d + "acq_0000.dat")) {
            msg += "<font color='aa0000'>I cannot see the raw URL <b> " + d + "/acq_0000.dat</b></font><br> ";
        } else if (!FileUtils.isUrl(d) && !(new File(d).exists())) {
            msg += "<font color='aa0000'>I cannot access the raw directory <b> " + d + "</b></font><br> ";
            msg += "(Does the dir exist? Is there a symlink? Is the rule '<b>"+this.getRawRule()+"</b>' correct?)<br>";
        } else {
            if (alsoOkMsgs) {
                msg += "<font color='00aa00'>I am able to access the raw directory</font> " + d + "<br>";
            }
        }

        if (exp.getCacheDir() == null) {
            msg += "<font color='aa0000'>Got no cache directory info</font><br> ";
        } else {
            File dir = new File(exp.getCacheDir());
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
                dir.setExecutable(true);
                dir.setWritable(true);
            }
            if (!dir.exists()) {
                msg += "<font color='aa0000'>I don't see the cache directory <b>" + dir + "</b></font><br> ";
                msg += "(Does the dir exist? Is the rule '<b>"+this.getCacheRule()+"</b>' correct?)<br>";
            } else {
                if (alsoOkMsgs) {
                    msg += "<font color='00aa00'>I am able to <b>access</b> the cache directory</font> " + dir + "<br>";
                }
                // now also check WRITE PERMISSIONS
                if (dir.canWrite()) {
                    if (alsoOkMsgs) {
                        msg += "<font color='00aa00'>I am able to <b>write</b> to the cache directory</font> " + dir + "<br>";
                    }
                } else {
                    msg += "<font color='aa0000'>I don't seem to have <b>write permissions</b> for the cache directory <b>" + dir + "</b></font><br> ";
                }
            }


            d = exp.getResultsDirectory();
            if (d.length() < 2) {
                msg = "<font color='aa0000'>Strange results path " + d + ", plesae check the rules</font><br> ";
                msg += "(Does the dir exist? Is the rule '<b>"+this.getResultsRule()+"</b>' correct?)<br>";               
            }

            if ((FileUtils.isUrl(d) && !FileUtils.exists(d + "status.txt"))
                    || (!FileUtils.isUrl(d) && !(new File(d).exists()))) {
                msg += "<font color='aa0000'>I cannot access the results directory <b>" + d + "</b></font><br>";
                msg += "(Does the dir exist? Is the rule '<b>"+this.getResultsRule()+"</b>' correct?)<br>";               
            } else {
                if (alsoOkMsgs) {
                    msg += "<font color='00aa00'>I am able to access the results directory</font> " + d + "<br>";
                }
            }
        }
        return msg;
    }

    public String verifyCacheDir() {
        if (getExperimentContext() == null) return "";
        
        String msg = "";
        ExperimentContext exp = getExperimentContext();
        File d = new File(exp.getCacheDir());
        if (!d.exists()) {
            d.mkdirs();
            d.setExecutable(true);
            d.setWritable(true);
        }
        if (!d.exists()) {
            msg = "<li>The <b>cache</b> dir <b>" + d.toString() + "</b> does not seem to exist</li>";
        } else {
            if (!d.canWrite()) {
                msg += "<li><font color='aa0000'>I cannot <b>write</b> to the cache directory " + d + "</font></li> ";
            }

        }
        if (msg.length() > 0) {
            msg = "There is a problem with the directories:<ul>" + msg;
            msg += "</ul><font color='aa0000'>Please check your folder rule settings</font><br>";
           // manager.ruleChanged();
        }
        return msg;
    }

    public String toString() {
        String s = "Cache dir: " + getCacheDir() + "\n";
        s += "Results dir: " + getResultsDir() + "\n";
        s += "Raw dir: " + getRawDir() + "\n";
        s += "Database url: " + getDbUrl() + "\n";
        return s;
    }

    public static GlobalContext getContext() {
        if (context == null) {
            context = new GlobalContext();
        }
        return context;
    }

    public static void setContext(GlobalContext c) {
        context = c;
    }

    public boolean isComplexRule() {
        return manager.isComplexRule();
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(GlobalContext.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(GlobalContext.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(GlobalContext.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("GlobalContext: " + msg);
        //Logger.getLogger( ExGlobalContextperimentContext.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the cacheDir
     */
    public String getCacheDir() {
        return manager.getCacheDir();
    }

    public String getResultsDir() {
        return manager.getResultsDir();
    }

    public String getRawDir() {
        return manager.getRawDir();
    }

    
    public String getRawRule() {
        return manager.getRawRule();
    }

    public String getResultsRule() {
        return manager.getResultsRule();
    }

    /**
     * @return the dbUrl
     */
    public String getDbUrl() {
        return manager.getDbUrl();
    }

    public String getServerUrl() {
        String db = getDbUrl();
        if (db == null) {
            return null;
        }
        int col = db.lastIndexOf(":");
        if (col > 0) {
            db = db.substring(0, col);
        }
        return db;
    }

    public String getServer() {
        return getServerUrl();
    }

    public void setDbUrl(String URL) {
        manager.setUrl(URL);
    }

    private String getCacheRule() {
        return manager.getCacheRule();
    }
}
