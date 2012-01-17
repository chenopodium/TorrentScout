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

import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.utils.ProgressListener;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;

/**
 *
 * @author Chantal Roth
 */
public abstract class Task extends SwingWorker<Void, Void> implements ProgressListener{
    
    protected TaskListener tasklistener;
    private ProgressListener proglistener;
    private ProgressHandle handle;
    private JFrame frame;
    
    public Task(TaskListener tasklistener) {
         this.tasklistener = tasklistener;
    }
    public Task(TaskListener tasklistener, ProgressListener proglistener, PropertyChangeListener proplist) {
         this.tasklistener = tasklistener;
         this.proglistener = proglistener;
         if (proplist != null) this.addPropertyChangeListener(proplist);
    }
   
    public Task(TaskListener tasklistener, ProgressHandle handle, PropertyChangeListener proplist) {
         this.tasklistener = tasklistener;
         
         setProgressHandle(handle);
         if (proplist != null) this.addPropertyChangeListener(proplist);
    }
    public void setProgressHandle(ProgressHandle h) {
        this.handle = h;
        
         try {
            if (handle != null) {
                frame = GuiUtils.showNonModalProgress(handle);
                handle.start(100, 100);
                
            }
         }
         catch (Exception e) {}
    }
    public ProgressHandle getProgressHandle() {
        return handle;
    }
    public Task(TaskListener tasklistener, ProgressHandle proglistener) {
        this(tasklistener, proglistener, null);
        
    }
    public abstract boolean isSuccess();
    
    @Override
    public void stop() {
        this.cancel(true);
    }
    public Task(TaskListener tasklistener, ProgressListener proglistener) {
        this(tasklistener, proglistener, null);
        
    }
    public TaskListener getTaskListener() {
        return tasklistener;
    }
    public ProgressListener getProgListener() {
        return proglistener;
    }

    /*
     * Executed in event dispatching thread
     */
    @Override
    public void done() {
        p("Task "+getClass().getName()+"  is done");   
        if (handle != null){
            try {
                handle.finish();
                
            }
            catch (Exception e){}
        }
        closeProgressFrame();
        if (tasklistener != null) tasklistener.taskDone(this);
        if (proglistener != null) proglistener.stop();
    }
    public JFrame getProgressFrame() {
        return frame;
    }
    public void closeProgressFrame() {
        if (frame != null) {
            frame.setVisible(false);
            frame.dispose();
        }
    }
     public void setText(String text) {
       
        if (proglistener != null) proglistener.setMessage(text);
        if (handle != null) handle.setDisplayName(text);
    }
    @Override
    public void setProgressValue(int prog) {
        if (prog > 99) prog = 99;
        if (prog <= 0) prog = 1;
        super.setProgress(prog);
        if (proglistener != null) proglistener.setProgressValue(prog);
        if (handle != null) handle.progress(prog);
    }
    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(Task.class.getName()).log(Level.SEVERE, msg, ex);
    }
    
    private void err(String msg) {
        Logger.getLogger(Task.class.getName()).log(Level.SEVERE, msg);
    }
    
    private void warn(String msg) {
        Logger.getLogger(Task.class.getName()).log(Level.WARNING, msg);
    }
    
    private void p(String msg) {
        System.out.println("Task: " + msg);
        //Logger.getLogger( Task.class.getName()).log(Level.INFO, msg, ex);
    }
     @Override
    public void setMessage(String msg) {
        setText(msg);
    }
}
