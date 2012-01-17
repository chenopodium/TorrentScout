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
package com.iontorrent.guiutils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.windows.WindowManager;

/**
 *
 * @author Chantal Roth
 */
public class GuiUtils {

    static final String IMG_INFO1 = "dialog-information-4.png";
    static final String IMG_INFO2 = "dialog-information.png";

    public GuiUtils() {
    }

    public static void showNonModalMsg(String msg) {
        showNonModalMsg(msg, false);
    }

    public static void showNonModalMsg(String msg, int secs) {
        showNonModelMsg(msg, msg, false, secs);
    }

    public static void showNonModalMsg(String msg, boolean lbl) {
        showNonModelMsg(msg, msg, lbl, 3);
    }

    public static void showNonModalMsg(String msg, boolean lbl, int secs) {
        showNonModelMsg(msg, msg, lbl, secs);
    }

    public static JFrame showNonModalProgress(ProgressHandle handle) {

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(ProgressHandleFactory.createMainLabelComponent(handle),
                BorderLayout.SOUTH);
        panel.add(ProgressHandleFactory.createProgressComponent(handle),
                BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panel.setSize(new Dimension(400, 70));
        panel.setPreferredSize(new Dimension(400, 70));
        panel.add("North", ProgressHandleFactory.createDetailLabelComponent(handle));

        final JFrame f = new JFrame();
        URL u = GuiUtils.class.getResource(IMG_INFO2);
        if (u != null) {
            f.setIconImage(new ImageIcon(u).getImage());
        }
        setMiddleAndShow(f, 400, 80);
        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add(panel, BorderLayout.CENTER);
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        f.setAlwaysOnTop(true);

        return f;

    }

    public static JFrame showNonModalMsg(JComponent comp, String title) {
        final JFrame f = new JFrame();
        return showNonModalMsg(f, comp, title);

    }

    public static JFrame showNonModalDialog(String msg, String title) {
        return showNonModalDialog(new JLabel(msg), title);
    }

    public static JFrame showNonModalDialog(JComponent comp, String title) {
        JFrame f = new JFrame();
        URL u = GuiUtils.class.getResource(IMG_INFO2);
        if (u != null) {
            f.setIconImage(new ImageIcon(u).getImage());
        }
        final JDialog newdialog = new JDialog(f, title, false);
        newdialog.setLocation(400, 100);
        newdialog.getContentPane().add(comp);
        newdialog.pack();
        newdialog.setAlwaysOnTop(true);
        newdialog.setVisible(true);
        KeyListener l = new MyEscapeListener(newdialog);
        newdialog.addKeyListener(l);
        comp.addKeyListener(l);
        f.addKeyListener(l);
        comp.setToolTipText("Click escape, space or tab to close this window (or click on the x)");
        return f;
    }

    private static class MyEscapeListener implements KeyListener {

        private JDialog d;

        public MyEscapeListener(JDialog d) {
            this.d = d;
        }

        @Override
        public void keyTyped(KeyEvent e) {
            int c = e.getKeyCode();
            p("Got key on dialog: " + c);
            if (c == KeyEvent.VK_ESCAPE || c == KeyEvent.VK_ENTER || c == KeyEvent.VK_SPACE) {
                //closing frame
                d.dispose();

            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            p("keyPressed on dialog");
            keyTyped(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static JFrame showNonModalMsg(JFrame msgframe, JComponent comp, String title) {

        URL u = GuiUtils.class.getResource(IMG_INFO2);
        if (u != null) {
            msgframe.setIconImage(new ImageIcon(u).getImage());
        }

        msgframe.getContentPane().add(comp);
        msgframe.setTitle(title);

        setMiddleAndShow(msgframe, comp.getWidth(), comp.getHeight());
        msgframe.pack();

        msgframe.repaint();
        return msgframe;
    }

    public static void showNonModelMsg(String title, String msg, boolean lbl, int secs) {
        JLabel label;
        final JFrame f = new JFrame();
        URL u = GuiUtils.class.getResource(IMG_INFO2);
        if (u != null) {
            f.setIconImage(new ImageIcon(u).getImage());
        }
        label = new JLabel(msg);
        if (lbl) {
            f.getContentPane().add(label);
        }
        f.setTitle(title);
        p(msg);

        int w = Math.max(title.length() * 15, 300);
        int h = 28;
        if (lbl) {
            h = 80;
        }
        setMiddleAndShow(f, w, h);
        if (lbl) {
            label.paintImmediately(0, 0, w, h);
        }
        f.repaint();
//Must schedule the close before the dialog becomes visible
        ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
        s.schedule(new Runnable() {

            public void run() {
                f.setVisible(false); //should be invoked on the EDT
                f.dispose();
            }
        }, secs, TimeUnit.SECONDS);

    }

    private static void setMiddleAndShow(Frame f, int w, int h) {
        int x = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2);
        int y = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2);
        y = Math.max(0, y - h / 2);
        x = Math.max(0, x - w / 2);
        f.setLocation(x, y);
        f.setSize(w, h);
        f.setVisible(true);
        f.setAlwaysOnTop(true);
        f.toFront();
    }

    public static Color stringToColor(String value) //"003399" 
    {
        Color c = Color.black;
        if (value.startsWith("0x")) {
            value = value.substring(2);
        }
        if (value.length() > 6) {
            value = value.substring(2);
        }
        try {
            int red = (Integer.decode("0x" + value.substring(0, 2))).intValue();
            int green = (Integer.decode("0x" + value.substring(2, 4))).intValue();
            int blue = (Integer.decode("0x" + value.substring(4, 6))).intValue();
            c = new Color(red, green, blue);
        } catch (Exception e) {

            err("Could not decode " + value + ", it should be a hex string", e);
        }
        //   p("Converting "+value+" to color "+c);
        return c;

    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(GuiUtils.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(GuiUtils.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(GuiUtils.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("GuiUtils: " + msg);
        //Logger.getLogger( GuiUtils.class.getName()).log(Level.INFO, msg, ex);
    }
}
