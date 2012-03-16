/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.main;

import com.iontorrent.utils.settings.Config;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class MailConfig {

    private static URL initialUrl = MailConfig.class.getResource("mailconfig.xml");
    private static Config config;
    
    public static Config getConfig() {
        if (config != null) {
            return config;
        }
        config = new Config(initialUrl);

        return config;
    }

    public MailConfig() {
    }

    public static String getFileVersion() {
        getConfig();
        return config.getStringValue("version");
    }

    public String getSubject() {
        return getConfig().getStringValue("subject");
    }

    public String getCC() {
        return getConfig().getStringValue("cc");
    }

    public String getRecipient() {
        return getConfig().getStringValue("recipient");
    }

    public String getBody() {
        return getConfig().getStringValue("body");
    }

    @Override
    public String toString() {
        return getRecipient() + "?cc=" + getCC() + "&subject=" + getSubject() + "&body=" + getBody();
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(MailConfig.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(MailConfig.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(MailConfig.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
//  System.out.println("MailConfig: " + msg);
        //Logger.getLogger( MailConfig.class.getName()).log(Level.INFO, msg);
    }
}
