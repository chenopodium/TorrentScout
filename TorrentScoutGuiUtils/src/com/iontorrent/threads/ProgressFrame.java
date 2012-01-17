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

package com.iontorrent.threads;


import com.iontorrent.utils.ProgressListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
/**
 *
 * @author Chantal Roth
 */
public class ProgressFrame implements ProgressListener{

    private JFrame frame;
    private JProgressBar progressBar;
    private JLabel label;
    
    public ProgressFrame(Component parent, Image im, String title) {
        frame = new JFrame(title);
        if (im != null) frame.setIconImage(im);
        frame.setSize(300, 70);
        if (parent != null) frame.setLocation(parent.getX()+parent.getWidth()/2, parent.getY()+parent.getHeight()/2);
        else frame.setLocation(500, 400);
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        label = new JLabel(title);
        frame.getContentPane().add(label,BorderLayout.CENTER);
        frame.getContentPane().add(progressBar,BorderLayout.SOUTH);
        frame.setVisible(true);
    }
    public void stop() {        
        frame.setVisible(false); 
       
        frame.dispose();
    }
    @Override
    public void setMessage(String msg) {
        label.setText(msg);
    }
    public static ProgressFrame showProgress(Component parent, Image im, String title) {
        ProgressFrame f = new ProgressFrame(parent, im, title);
        return f;
    }

/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( ProgressFrame.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( ProgressFrame.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( ProgressFrame.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("ProgressFrame: " + msg);
        //Logger.getLogger( ProgressFrame.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the frame
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * @return the progressBar
     */
    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public void setValue(int val) {
        progressBar.setValue(val);
    }

    @Override
    public void setProgressValue(int progress) {
        progressBar.setValue(progress);
    }
}
