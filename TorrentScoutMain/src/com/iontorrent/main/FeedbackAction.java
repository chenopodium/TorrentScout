/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.main;

import com.iontorrent.utils.log.ShortFormatter;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(category = "Help",
id = "com.iontorrent.main.FeedbackAction")
@ActionRegistration(iconBase = "com/iontorrent/main/mail-new.png",
displayName = "#CTL_FeedbackAction")
@ActionReferences({
    @ActionReference(path = "Menu/Help", position = 1600),
    @ActionReference(path = "Toolbars/File", position = 500),
    @ActionReference(path = "Shortcuts", name = "O-H"),
    @ActionReference(path = "Shortcuts", name = "D-B"),
    @ActionReference(path = "Shortcuts", name = "D-M")
})
@Messages("CTL_FeedbackAction=Feedback")
public final class FeedbackAction implements ActionListener {

    static MailConfig config = new MailConfig();

    public void actionPerformed(ActionEvent e) {
        String mailto = config.toString();
      //  mailto = replaceWS(mailto);
        String msg = "";
        Throwable t = ShortFormatter.getFormatter().getLastException();
        if (t != null) {
            p("appending last error: "+t);
            msg +=",\nI found an error:\n"+t.getMessage()+"\n";
            if (t.getStackTrace() != null) {
                for (StackTraceElement st: t.getStackTrace()) {            
                    msg += st.toString()+"\n";
                }
            }
            mailto += msg;
        }
        mailto  +=",\nI would like to report the following:\n";
        if (Desktop.isDesktopSupported()) {
            Desktop desk = Desktop.getDesktop();
            if (desk.isSupported(Desktop.Action.MAIL)) {
                try {
                  //  mailto  = mailto.replaceAll("\n", "%0D%0A");
                    URI uri = new URI("mailto", mailto, null);
                  //  JOptionPane.showMessageDialog(null, "Sending email: " + mailto);
                    desk.mail(uri);
                    return;
                } catch (Exception ex) {
                    err("Could not send email", ex);
                }
            }
        }
        msg = "Email recipient: "+config.getRecipient()+"\n"+msg;
        if (msg.length()>0) {
            JTextArea area = new JTextArea();
            area.setText("I could not find your email client.\nPlease send the email with the following information:\n"+msg);
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), area);
        }

    }
    private String replaceWS(String s) {
        s = s.replaceAll(" ", "%20");
        s = s.replaceAll("\n", "%0D%0A");
        return s;
    }
    
    private String encodex(final String s) {
        try {           
            return java.net.URLEncoder.encode(s, "utf-8").replaceAll("\\+", "%20").replaceAll("\\%0A", "%0D%0A").replaceAll(" ", "%20");
        } catch (Throwable x) {
            return s;
        }
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(FeedbackAction.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(FeedbackAction.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(FeedbackAction.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("FeedbackAction: " + msg);
        //Logger.getLogger( FeedbackAction.class.getName()).log(Level.INFO, msg);
    }
}
