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
package com.iontorrent.guiutils.heatmap;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 *
 * @author Chantal Roth
 */
public class GradientPanel extends JPanel implements ActionListener {

    ColorModel colormodel;
    boolean drawLegendText;
    private int BX = 5;
    private int BY = 20;
    private int LX = 10;
    private int multiplier;
    private boolean log;
    private ActionListener listener;
    //private PopupFactory factory = PopupFactory.getSharedInstance();
    private MouseAdapter fAdapter;
    private JPopupMenu fColorMenu;
    private Font smallfont = new Font("Sans Serif", Font.PLAIN, 10);

    public GradientPanel(ColorModel colormodel) {
        this(colormodel, 1, false);
    }

    public GradientPanel(ColorModel colormodel, int multiplier, boolean log) {
        this.colormodel = colormodel;
        this.multiplier = multiplier;
        drawLegendText = true;
        this.log = log;
        this.setPreferredSize(new Dimension(LX + 2 * BX + 20, 100));
        this.setMinimumSize(new Dimension(LX + 2 * BX + 15, 50));
        makePopup();

    }

    void makePopup() {
        fColorMenu = new JPopupMenu("Settings");
        fColorMenu.add(makeMenuItem("Change maximum value"));
        fColorMenu.add(makeMenuItem("Change minimum value"));
       // fColorMenu.add(makeMenuItem("Use previous min/max values"));
        // Create a MouseAdapter that creates a Popup menu
        // when the right mouse or equivalent button clicked.
        fAdapter = new MouseAdapter() {
            // On some platforms, mouseReleased sets PopupTrigger.

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }

            // And on other platforms, mousePressed sets PopupTrigger.
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }

            // Get the component over which the right button click
            // occurred and show the menu there.
            public void showPopupMenu(MouseEvent e) {
                fColorMenu.show(GradientPanel.this, e.getX(), e.getY());
            }
        }; // anonymous MouseAdapter subclass
        addMouseListener(fAdapter);
    } // makePopup

    /** Change background color of selected components.**/
    @Override
    public void actionPerformed(ActionEvent e) {
        String name = e.getActionCommand().toLowerCase().trim();
        if (colormodel == null) return;
        double min = colormodel.getMin();
        double max = colormodel.getMax();
        if (name.indexOf("change min") > -1) {
            String ans = JOptionPane.showInputDialog("Enter a new MINIMUM value (current: min=" + min + ", max=" + max + ")");
            if (ans == null || ans.length() < 1) {
                return;
            }
            try {
                min = Double.parseDouble(ans);
            } catch (Exception ex) { }
        } else if (name.indexOf("change max") > -1) {
              String ans = JOptionPane.showInputDialog("Enter a new MAXIMUM value (current: min=" + min + " max=" + max + ")");
            if (ans == null || ans.length() < 1) {
                return;
            }
            try {
                max = Double.parseDouble(ans);
            } catch (Exception ex) {        }                 
        }
        colormodel.setMax(max);
        colormodel.setMin(min);
        colormodel.update();
        if (listener != null) {
            listener.actionPerformed(e);
        }
    } // actionPerformed

    /** A utility method for making menu items. **/
    private JMenuItem makeMenuItem(String label) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(this);
        return item;
    } // 

    public void setColorModel(ColorModel model) {
        this.colormodel = model;
    }

    public void setMin(int min) {
        colormodel.setMin(min);
    }

    public void setMax(int max) {
         colormodel.setMax(max);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (colormodel == null) {
            p("Got no colors");
            return;
        }
        
        int height = this.getHeight();

        this.setOpaque(true);
        // Legend


        Color colors[] = colormodel.getColors();
        for (int y = 0; y < height - BY * 2 - 1; y++) {

            int yStart = height - BY - 2 - y;
            
            g2d.setColor(colors[(int) ((y / (double) (height - 2 * BY)) * colors.length)]);
            g2d.fillRect(BX + 1, yStart, LX - 1, 1);
        }
        double min = colormodel.getMin();
        double max = colormodel.getMax();
        if (drawLegendText) {
            int x = BX + LX;
            g2d.setColor(Color.black);
            g2d.setFont(smallfont);
            double a = min / multiplier;
            double b = max / multiplier;
            double c = (min + max) / multiplier / 2;
            if (log) {
                a = Math.log(Math.max(1, a));
                b = Math.log(Math.max(1, b));
                c = Math.log(Math.max(1, c));
            }
            g2d.drawString("" + (int) a, BX, height - BY + 10);
            g2d.drawString("" + (int) b, BX, 15);

            int y = (height) / 2;
            g2d.drawString("" + (int) c, x + 2, y + 4);
            g2d.drawLine(x - LX, y, x, y);
        }
        g2d.setColor(Color.black);
        g2d.drawRect(BX, BY, LX, height - 2 * BY - 1);
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(GradientPanel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(GradientPanel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(GradientPanel.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("GradientPanel: " + msg);
        //Logger.getLogger( GradientPanel.class.getName()).log(Level.INFO, msg, ex);
    }

    public double getMax() {
        return colormodel.getMax();
    }

    public double getMin() {
        return colormodel.getMin();
    }

    /**
     * @return the listener
     */
    public ActionListener getListener() {
        return listener;
    }

    /**
     * @param listener the listener to set
     */
    public void setListener(ActionListener listener) {
        this.listener = listener;
    }
}
