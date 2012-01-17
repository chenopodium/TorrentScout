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

package com.iontorrent.dataloading;


import com.iontorrent.rawdataaccess.pgmacquisition.DataAccessManager;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.wellmodel.RasterData;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.utils.ProgressListener;
import com.iontorrent.wellmodel.WellContext;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Chantal Roth
 */
public class RasterDataAreaLoader extends Task {

    File rawfile;
    WellCoordinate coord;
    int flow;
    RawType filetype;
    RasterData data;
    WellContext context;
    Exception ex;
    public RasterDataAreaLoader(TaskListener tasklistener, ProgressListener proglistener, WellContext context, WellCoordinate coord, int flow, RawType filetype) {
        super(tasklistener, proglistener);
        this.coord = coord;
        this.flow = flow;
        this.filetype = filetype;
        this.context = context;
    }
    public RasterData getResult() {
        return data;
    }
     public boolean isSuccess() {
        return data != null;
    }
    public Exception getException() {
        return ex;
    }
     @Override
    public Void doInBackground() {
        
        DataAccessManager manager = DataAccessManager.getManager(context);
        try {
            data = manager.getRasterData(coord, flow, filetype, this.getProgListener());
        }
        catch (Exception e) {
            this.ex = e;
        }
        
        return null;
      }

/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( RasterDataCoordLoader.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( RasterDataCoordLoader.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( RasterDataCoordLoader.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("RasterDataLoader: " + msg);
        //Logger.getLogger( RasterDataCoordLoader.class.getName()).log(Level.INFO, msg, ex);
    }

    
   
}
