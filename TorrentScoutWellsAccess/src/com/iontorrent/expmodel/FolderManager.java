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
import com.iontorrent.utils.io.FileTools;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Chantal Roth
 */
public class FolderManager {

    private static FolderConfig offline = new FolderConfig("offline");
    private static FolderConfig rule ;
    private ExperimentContext exp;
    // cached data
    private String raw;
    private String results;
    private String cache;
    //private String dburl;
    private static FolderManager manager;
    private String msg;
    private Fstab fstab;

    public static void resetFolderConfig() {
        offline.resetFolderConfig();
    }

    public void checkVersion() {
        String sysver = SystemTool.getProperty("version");

        p("System version (via master.jnlp or similar) " + sysver);
        String configver = offline.getFileVersion();
        p("Got folderconfig version: " + configver);
        if (configver == null || configver.trim().length() < 1
                || (sysver != null && !sysver.equalsIgnoreCase(configver))) {
            resetFolderConfig();

        }
    }

    public static FolderManager getManager() {
        if (manager == null) {
            manager = new FolderManager();
        }
        return manager;
    }

    public static String setDefaultRule() {
        manager = FolderManager.getManager();
        p("setDefaultRule Finding default rule in manager:");
        String default_rule = null;
        p("setDefaultRule Got keys: " + manager.getKeys());

        String codebase = SystemTool.getProperty("codebase");
        String os = SystemTool.getOsName().toLowerCase();
        if (os != null && os.equalsIgnoreCase("linux")) {
            os = "unix";
        }
        if (os != null && os.startsWith("mac")) {
            os = "unix";
        }
        // p("Got codebase:'" + codebase + "', system os: " + os);
        if (codebase != null) {
            int col = codebase.lastIndexOf(":");
            if (col > 0) {
                codebase = codebase.substring(0, col);
            }
            col = codebase.lastIndexOf("torrentscout");
            if (col > 0) {
                codebase = codebase.substring(0, col);
            }
            if (codebase.startsWith("http://")) {
                codebase = codebase.substring(7);
            }
            if (codebase.endsWith("/")) {
                codebase = codebase.substring(0, codebase.length() - 1);
            }
            if (codebase.endsWith(":")) {
                codebase = codebase.substring(0, codebase.length() - 1);
            }
            if (!codebase.startsWith("www")) {
                int dot = codebase.indexOf(".");
                if (dot > 0) {
                    codebase = codebase.substring(0, dot);
                }
            }
           // p("Normalized codebase=" + codebase);
        }
        default_rule = null;


        for (String key : manager.getKeys()) {
            if (manager.getConfig(key).isDefault()) {
                if (default_rule == null) {
                    default_rule = key.toLowerCase();
                    p("setDefaultRule  Got default rule: " + default_rule);
                } else {
                    manager.getConfig(key).setDefault(false);
                }
            }
        }

        if (default_rule == null && codebase != null) {
            p("setDefaultRule:Finding rule based on codebase: " + codebase);
            //    p("System OS: "+os);
            for (String key : manager.getKeys()) {
                FolderConfig config = manager.getConfig(key);
                String url = config.getUrl();
                if (url.startsWith("http://")) {
                    url = codebase.substring(7);
                }
                p("setDefaultRule:Comparing '" + url + "' starts with '" + codebase + "' ?");
                if (url.startsWith(codebase)) {
                    // also check system!
                    String cos = config.getOS().toLowerCase().substring(0, 1);

                    p("setDefaultRule:Now comparing OS:" + os + " starts with " + cos);
                    if (os.startsWith(cos)) {
                        default_rule = key.toLowerCase();
                        p("Found rule based on CODEBASE and matching os " + config.getOS() + ": " + default_rule);
                        break;
                    }
                }
            }
        }
        if (default_rule == null) {
            p("setDefaultRule:Found no default rule among: " + manager.getKeys());
            if (manager.getKeys() != null && manager.getKeys().size() == 1) {
                p("Since there is only ONE rule, using that one as default");
                default_rule = manager.getKeys().get(0);
            }
        }
        //  if (default_rule == null)  default_rule="offline";
        if (default_rule != null) {
            manager.setRule(default_rule, false);
        }
        p("setDefaultRule:Got default rule: " + default_rule + " with url=" + manager.getDbUrl());

        return default_rule;
    }

    private FolderManager() {
        rule = offline;
        checkVersion();
    }

    public String getCacheDir() {
        return cache;
    }

    public int findBestBaseDir(String plugindir, String expdir) {
        if (expdir == null) return -1;
        p("Checking fstab for " + expdir);
        fstab = new Fstab(plugindir + "fstab.txt", rule.getFstab());

        if (expdir.startsWith("\\")) {
            expdir = expdir.substring(1);
        }
        int s = expdir.indexOf("\\");
        if (s > 0) {
            expdir = expdir.substring(0, s);
        }
        if (expdir.startsWith("/")) {
            expdir = expdir.substring(1);
        }
        s = expdir.indexOf("/");
        if (s > 0) {
            expdir = expdir.substring(0, s);
        }
        p("Fiding " + expdir + " in map");
        String dev = fstab.getDevice(expdir);
        if (dev != null) {
            p("Found " + expdir + "->" + dev);

            ArrayList<String> basedirs = rule.getBaseDirs();
            // p("Checking "+basedirs.size()+" basedirs: "+rule.getBaseDirs());
            if (basedirs.size() > 1) {
                this.showNonModelMsg(this.getRule() + ": Checking which of the " + basedirs.size() + " base folders is best...", 15);
            }
            for (int i = 0; i < basedirs.size(); i++) {
                String bdir = basedirs.get(i);
                if (bdir.startsWith("\\\\")) {
                    bdir = bdir.substring(2);
                }
                if (bdir.startsWith("/")) {
                    bdir = bdir.substring(1);
                }
                if (bdir.startsWith("net/")) {
                    bdir = bdir.substring(4);
                }
                p("Does " + bdir + " start with " + dev + "?");
                if (bdir.startsWith(dev)) {
                    rule.setBaseDir(i);
                    rule.setDefaultBaseDir(i);
                    p("  yes, found match: " + bdir + ", basedir is now: " + rule.getBaseDir());
                    return i;
                }
            }
        }
        return -1;
    }

    private static void showNonModelMsg(String title, int secs) {
        final JFrame f = new JFrame();
        f.setTitle(title);
        f.setLocation(600, 600);
        f.setSize(600, 30);
        f.setVisible(true); // if modal, application will pause here
        f.repaint();

        ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
        s.schedule(new Runnable() {

            public void run() {
                f.setVisible(false); //should be invoked on the EDT
                f.dispose();
            }
        }, secs, TimeUnit.SECONDS);

    }

    private String replaceVars(String dir) {
        // search for all vars

        if (dir == null) {
            warn("no dir: " + dir);
            return "";
        }
        String expname = "";
        String res = "";
        String pgm = "";
        String expdir = "";
        String resdir = "";
        if (exp != null && exp.getExperimentName() != null) {
            //   p("Got an exp context " + exp.getExperimentName());
            expname = exp.getExperimentName();
            res = exp.getResultsName();
            pgm = exp.getPgm();
            if (pgm == null || pgm.length() < 1) {
                err("Got no PGM from experiment " + expname);

            }
            resdir = exp.getResDirFromDb();
            expdir = exp.getExpDir();

        } else {
            // warn("Got no exp context or context has no name");
        }
        String base = rule.getBaseDir();

        //   p("Replacing vars in "+dir);
        //   p("Base:"+base+", pgm: "+pgm+", exp:"+expname+", res:"+res);
        dir = dir.replace("${BASE}", base);
        dir = dir.replace("${BASE_DIR}", base);

        if (pgm != null) {
            dir = dir.replace("${PGM_NAME}", pgm);
            dir = dir.replace("${PGM}", pgm);
        }

        if (res != null) {
            dir = dir.replace("${RESULTS_NAME}", res);
            dir = dir.replace("${RESULTS}", res);
            dir = dir.replace("${RESULT_NAME}", res);
            dir = dir.replace("${RESULT}", res);
        }
        if (expname != null) {
            dir = dir.replace("${EXP}", expname);
            dir = dir.replace("${EXP_NAME}", expname);
            dir = dir.replace("${EXPERIMENT}", expname);
            dir = dir.replace("${EXPERIMENT_NAME}", expname);
        }
        if (expdir != null) {
            dir = dir.replace("${EXP_DIR}", expdir);
        }
        if (resdir != null) {
            dir = dir.replace("${RESULT_DIR}", resdir);
        }
        if (resdir != null) {
            dir = dir.replace("${RESULTS_DIR}", resdir);
        }
        if (expdir != null) {
            dir = dir.replace("${EXPERIMENT_DIR}", expdir);
        }
        //dir = dir.replace("${EXP_DIR}", expdir);


        if (raw != null) {
            dir = dir.replace("${RAW}", raw);
            dir = dir.replace("${RAW_DIR}", raw);
            dir = dir.replace("${RAW_DIRECTORY}", raw);
        }
        //dir = dir.replace("\\", "/");
        boolean isUrl = dir.startsWith("//") || dir.indexOf("://") > -1;
        if (!isUrl) {
            dir = dir.replace("//", "/");
        }
        boolean network = dir.startsWith("\\\\");
        if (network) {
            dir = dir.replace("/", "\\");
        }

        dir = FileTools.addSlashOrBackslash(dir);
        return dir;
    }

    public String getRawDir() {
        return raw;
    }

    public String getResultsDir() {
        return results;
    }

    public String getDbUrl() {
        return rule.getServer();
    }

    public void setExperimentContext(ExperimentContext exp, boolean update) {
        this.exp = exp;
        boolean ignore = false;
        if (this.getRule().toLowerCase().startsWith("offline")) {
            p("Rule is offline, ignoring rules");
            ignore = true;
        }
        if (update) {
            ruleChanged(ignore);
        }
    }

    public void setRule(String key, boolean checkFolders) {
        if (rule == null || !rule.getKey().equals(key)) {
            rule = new FolderConfig(key);
            p("*** SETTING RULE TO "+rule.getKey());
            rule.checkForUrl();
            ruleChanged(checkFolders);
        }
    }

    public String getRawRule() {
        return rule.getRawRule();
    }

    public String getResultsRule() {
        return rule.getResultsRule();
    }

    public void ruleChanged(boolean checkFolders) {
        p("RULECHANGED: check folders="+checkFolders);
        //  Exception e = new Exception("Who called rule change");
        //    e.printStackTrace();
        results = null;
        cache = null;
        raw = null;

        // replace all variables
        // test existance of folders if more than one rule
        if (exp != null && exp.getExpDir() != null && rule.getRawRule().contains("${BASE}")) {
            if (checkFolders) {
                findBestBaseDir(exp.getPluginDir(), exp.getExpDir());
            }
        }
        raw = replaceVars(rule.getRawRule());

        results = replaceVars(rule.getResultsRule());

        if (exp != null) {
            if (raw != null && !(new File(raw)).exists()) {
                p("Cannot find folder " + raw);
                if (!rule.getRawRule().contains("${BASE}") && rule.getBaseDirs() != null && rule.getBaseDirs().size() > 0) {
                    // we try an alternate rule:
                    String alternate = "/${BASE}/${PGM}/${EXP_NAME}/";
                    p("trying alternative rule: " + alternate);
                    raw = replaceVars(alternate);
                    if (!(new File(raw)).exists()) {
                        this.findDir(alternate, raw);
                    }
                } else {
                    raw = this.findDir(rule.getRawRule(), raw);
                }
            }
            if (results != null && !(new File(results)).exists()) {
                p("Could not find results path: "+results);
                results = this.findDir(rule.getResultsRule(), results);
            }
        }

        cache = replaceVars(rule.getCacheRule());


        if (exp != null) {
            String rule = this.getRule();
            if (rule == null) {
                rule = "";
            }
            if (exp.isIgnoreRule() || rule.startsWith("offline") || rule.length() < 1) {
                p("Ignoring rule " + rule + ". exp.isIgnore: " + exp.isIgnoreRule());
            } else {
                p("NOT ignoring rule...: " + this.getRule());
                exp.setCacheDir(cache);
                exp.setRawDir(raw);
                exp.setResultsDirectory(results);
                if (exp.isThumbnails()) {
                    exp.setThumbnailsRaw();
                }
                // p("Cache is now:      " + cache);
                p("Raw dir is now:    " + raw);
                p("Results dir is now:" + results);
            }
        }

        //   p("DB url is now:     " + dburl);
    }

    public String getCurrentBaseDir() {
        return rule.getBaseDir();
    }

    private String findDir(String dirrule, String dir) {
        if (dirrule != null && dirrule.contains("${BASE")) {
            int nrdirs = rule.getBaseDirs().size();
            int curdir = rule.getDefaultBaseDir();
            p("Got " + nrdirs + " dirs: current basedir:" + dir);
            //     p("got "+nrdirs+" directories, will check mutliple base dirs");
            boolean ok = false;
            int tries = 0;

            while (!ok && tries <= nrdirs) {
                File f = new File(dir);
                if (!f.exists()) {
                    p("Could not access " + dir);
                    tries++;
                    curdir++;
                    if (curdir >= nrdirs) {
                        curdir = 0;
                    }
                    rule.setBaseDir(curdir);

                    dir = replaceVars(dirrule);
                } else {
                    p("was able to access dir " + f);
                    ok = true;
                    return dir;
                }
            }
        }
        return dir;

    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(FolderManager.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(FolderManager.class.getName()).log(Level.SEVERE, msg);
        Exception e = new Exception();
        p(Arrays.toString(e.getStackTrace()));
    }

    private void warn(String msg) {
        Logger.getLogger(FolderManager.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("FolderManager: " + msg);
        Logger.getLogger(FolderManager.class.getName()).log(Level.INFO, msg);
    }

    public ExperimentContext getExperimentContext() {
        return exp;
    }

    public FolderConfig getConfig(String key) {
        return new FolderConfig(key);
    }

    public ArrayList<String> getKeys() {
        return offline.getKeys();
    }

    boolean isComplexRule() {
        return rule.getBaseDirs().size() > 1 && rule.getRawRule().indexOf("${BASE}") > -1;
    }

    public String getBaseDirs() {
        return rule.getBaseDirsString();
    }

    public String getRule() {
        if (rule == null) {
            p("rule is null, using offline");
            return "offline";
        }
        return rule.getKey();
    }

    void setUrl(String URL) {
        if (rule == null) {
            return;
        }
        rule.setServer(URL);
    }

    public String getCacheRule() {
        return rule.getCacheRule();
    }
}
