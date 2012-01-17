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
package com.iontorrent.sound;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Chantal Roth
 */
public class AudioPlay extends Thread {

    private String filename;
    private URL url;
    private Position curPosition;
    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb 

    enum Position {

        LEFT, RIGHT, NORMAL
    };

   

    public AudioPlay(String wavfile) {
        filename = wavfile;
        curPosition = Position.NORMAL;
    }

    public AudioPlay(String wavfile, Position p) {
        filename = wavfile;
        curPosition = p;
    }

    public AudioPlay(URL url) {
        this.url = url;
        curPosition = Position.NORMAL;
    }

    @Override
    public void run() {

        AudioInputStream audioInputStream = null;
        if (url != null) {
            try {
                audioInputStream = AudioSystem.getAudioInputStream(url);
            } catch (UnsupportedAudioFileException e) {
                err(e);
                return;
            } catch (IOException e) {
                err(e);
                return;
            }

        } else {
            File soundFile = new File(filename);
            if (!soundFile.exists()) {
                System.err.println("Wave file not found: " + filename);
                return;
            }
            try {
                audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            } catch (UnsupportedAudioFileException e) {
                err(e);
                return;
            } catch (IOException e) {
                err(e);
                return;
            }
        }


        AudioFormat format = audioInputStream.getFormat();
        SourceDataLine auline = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        try {
            auline = (SourceDataLine) AudioSystem.getLine(info);
            auline.open(format);
        } catch (LineUnavailableException e) {
            err(e);
            return;
        } catch (Exception e) {
            err(e);
            return;
        }

        if (auline.isControlSupported(FloatControl.Type.PAN)) {
            FloatControl pan = (FloatControl) auline.getControl(FloatControl.Type.PAN);
            if (curPosition == Position.RIGHT) {
                pan.setValue(1.0f);
            } else if (curPosition == Position.LEFT) {
                pan.setValue(-1.0f);
            }
        }

        auline.start();
        int nBytesRead = 0;
        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];

        try {
            while (nBytesRead != -1) {
                nBytesRead = audioInputStream.read(abData, 0, abData.length);
                if (nBytesRead >= 0) {
                    auline.write(abData, 0, nBytesRead);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } finally {
            auline.drain();
            auline.close();
        }

    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(AudioPlay.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(Exception ex) {
        Logger.getLogger(AudioPlay.class.getName()).log(Level.SEVERE, "Got an error: " + ex.getMessage(), ex);
    }

    private void err(String msg) {
        Logger.getLogger(AudioPlay.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(AudioPlay.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("AudioPlay: " + msg);
        //Logger.getLogger( AudioPlay.class.getName()).log(Level.INFO, msg, ex);
    }
}
