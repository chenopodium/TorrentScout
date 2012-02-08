/*
 * Copyright (C) 2012 Life Technologies Inc.
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
package com.iontorrent.guiutils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class FadeLabel extends JPanel implements ActionListener {

    private static final Font FONT = new Font("Serif", Font.PLAIN, 32);
    private String msg;
    private static final float DELTA = -0.1f;
    private static final Timer timer = new Timer(1000, null);
    private float alpha = 1f;

    public FadeLabel(String msg) {
        this.msg = msg;
        this.setPreferredSize(new Dimension(256, 96));
        this.setOpaque(true);
        this.setBackground(Color.black);
        timer.setInitialDelay(1000);
        timer.addActionListener(this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(FONT);
        int xx = this.getWidth();
        int yy = this.getHeight();
        int w2 = g.getFontMetrics().stringWidth(msg) / 2;
        int h2 = g.getFontMetrics().getDescent();
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, xx, yy);
        g2d.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_IN, alpha));
        g2d.setPaint(Color.BLUE);
        g2d.drawString(msg, xx / 2 - w2, yy / 2 + h2);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        alpha += DELTA;
        if (alpha < 0) {
            alpha = 1;
            timer.restart();
        }
        repaint();
    }

    public static void showFade(final String msg) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                JFrame f = new JFrame();
                f.setLayout(new GridLayout(0, 1));
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                
                f.add(new FadeLabel(msg));
                f.pack();
                f.setVisible(true);
            }
        });
    }

    static public void main(String[] args) {
        String msg = args[0];
        showFade(msg);
    }
}
