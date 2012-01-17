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

package com.iontorrent.wellmodel;


import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Chantal Roth
 */
public abstract class WellFilter {
  
    private String name;
    private String description;
    
    public WellFilter(String name, String description) {
       this.name = name;
       this.description = description;
    }
    public abstract boolean passes(WellCoordinate coord);

    public static boolean passes(WellCoordinate coord, ArrayList<WellFilter> filters) {
        if (filters == null) return true;
        for (WellFilter f: filters) {
            if (!f.passes(coord)) return false;
        }
        return true;
    }
/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( WellFilter.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( WellFilter.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( WellFilter.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("WellFilter: " + msg);
        //Logger.getLogger( WellFilter.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

  
}
