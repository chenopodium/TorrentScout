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
package org.iontorrent.acqview.movie;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class MovieState {

    private MovieStatus status;
    private int frame;
    private int nrframes;
    private PlayMode mode;

    public MovieState(int frame, int nrframes) {
        status = MovieStatus.STOPPED;
        mode = PlayMode.ONCE;
        this.frame = frame;
        this.nrframes = nrframes;
    }

    public MovieStatus play() {
        status = MovieStatus.PLAYING;
        return getStatus();
    }

    public MovieStatus setFrame(int frame) {
        this.frame = frame;
        if (frame < 0 || frame > nrframes) stop();
        return status;
    }
    public MovieStatus nextFrame() {
        frame++;
        if (getFrame() >= getNrframes()) {
            if (getMode() == PlayMode.ONCE) {
                status = stop();
            }
            else frame = 0;
        }
        return getStatus();
    }

    public MovieStatus prevFrame() {
        frame--;
        return getStatus();
    }

    public MovieStatus pause() {
        status = MovieStatus.PAUSED;
        return getStatus();
    }

    public MovieStatus stop() {
        frame = 0;
        status = MovieStatus.STOPPED;
        return getStatus();
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(MovieState.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(MovieState.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(MovieState.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("MovieState: " + msg);
        //Logger.getLogger( MovieState.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the status
     */
    public MovieStatus getStatus() {
        return status;
    }

    /**
     * @return the frame
     */
    public int getFrame() {
        return frame;
    }

    /**
     * @return the nrframes
     */
    public int getNrframes() {
        return nrframes;
    }

    /**
     * @return the mode
     */
    public PlayMode getMode() {
        return mode;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(PlayMode mode) {
        this.mode = mode;
    }
}
