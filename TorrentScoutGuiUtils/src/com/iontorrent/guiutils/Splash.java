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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.Timer;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Splash extends JDialog {

    static int dy = 0;
    
    public Splash(String title, String msg, int secs) {
        if (secs<1) secs = 1;
        if (secs > 30) secs = 30;
        setLocationRelativeTo(null);
        this.setUndecorated(true);
        
        JPanel main = new JPanel(new GridLayout(2, 1));
      //  JLabel tit = new JLabel(title);
        super.setTitle(title);
        JLabel label = new JLabel(msg);

        main.setBackground(Color.black);
        label.setForeground(Color.orange);
        label.setBackground(Color.black);
        this.setBackground(Color.black);
     //   tit.setForeground(Color.white);
     //   tit.setBackground(Color.black);

        label.setFont(new Font("Arial", Font.BOLD, 18));
     //   tit.setFont(new Font("Arial", Font.PLAIN, 24));
        this.add(main);
    //    main.add(tit);
        main.add(label);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        
        int x = (int) Math.max(100, screen.getWidth() / 2 - 400);
        int y = (int) Math.max(100, screen.getHeight() / 2 - 200)+dy;
        dy += 80;
        if (dy > 200) dy = 0;
        this.setLocation(x, y);
        this.setVisible(true);
        pack();
      
        // 99 x dt. 1 sec -> 10ms
        Timer timer = new Timer();
        
        timer.schedule(new Fader(this), 2000, 10*secs);
    }
}
