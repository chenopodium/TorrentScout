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
package org.iontorrent.seq.indexing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public abstract class AbstractIndexManager {
    
	protected HashMap <Long, ArrayList<String>>  map;
	protected String file;
	boolean changed;
	
	public AbstractIndexManager() {
		changed = false;
	}
	
	public abstract void load();
	public abstract void  save();
	
	public abstract void update(long loc, long var1, Object value);
	
	public abstract ArrayList<String> find(long rid);
	
    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(AbstractIndexManager.class.getName()).log(Level.SEVERE, msg, ex);
    }
    
    private void err(String msg) {
       
        Logger.getLogger(AbstractIndexManager.class.getName()).log(Level.SEVERE, msg);
    }
    
    private static void warn(String msg) {
        Logger.getLogger(AbstractIndexManager.class.getName()).log(Level.WARNING, msg);
    }
    
    private static void p(String msg) {
    //    System.out.println("AbstractIndexManager: " + msg);
        //Logger.getLogger( AbstractIndexManager.class.getName()).log(Level.INFO, msg);
    }
}
