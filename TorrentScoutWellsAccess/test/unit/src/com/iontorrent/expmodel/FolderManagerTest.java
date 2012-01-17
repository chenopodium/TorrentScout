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
package com.iontorrent.expmodel;

import org.junit.Test;

/**
 *
 * @author Chantal Roth
 */
public class FolderManagerTest {
    
    public FolderManagerTest() {
    }

  
   
    @Test
    public void testGetBestBsaeDir() {
        System.out.println("    public void testGetBestBsaeDir()");
        FolderManager man = FolderManager.getManager();
        //man.resetFolderConfig();
        man.setRule("ioneast", false);
        String expdir = "/results6/somexperiment";
        String plugindir ="s:/data/test/";
        int dir = man.findBestBaseDir(plugindir, expdir);
        System.out.println("Best basedir="+dir+":"+man.getCurrentBaseDir());
    }
}
