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
package com.iontorrent.torrentscout.explorer;

import com.iontorrent.torrentscout.explorer.options.TorrentExplorerPanel;
import com.iontorrent.utils.io.FileTools;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class Export {

    public static String getFile(String title, String ext, boolean tosave) {
        Preferences p = NbPreferences.forModule(TorrentExplorerPanel.class);
        String path = p.get("export_path", null);

        String file = FileTools.getFile(title, ext, path, tosave);
        if (file != null) {
            // remmeber path
            path = new File(file).getParent();
            p.put("export_path", path);
        }
        p("Export: file is: "+file+", path is: "+path);
       
        return file;
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(Export.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        Logger.getLogger(Export.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(Export.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        //System.out.println("Export: " + msg);
        Logger.getLogger(Export.class.getName()).log(Level.INFO, msg);
    }
}
