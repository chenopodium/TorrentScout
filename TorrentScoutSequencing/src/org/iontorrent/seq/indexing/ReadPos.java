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

import com.iontorrent.utils.StringTools;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class ReadPos {

    public String readname;
    public long pos;
    public int reversed;
    public int revcompread;
    public long endpos;

    public String toString() {
        return "[" + readname + "," + pos + "," + endpos + "," + reversed + "," + revcompread + "]";
    }

    public static ReadPos fromString(String s) {
        s = s.trim();
        if (s.startsWith("[")) s = s.substring(1, s.length()-1);
      //  p("FromString: "+s);
        ArrayList<String> list = StringTools.parseList(s, ",");
        if (list == null || list.size()<5) {
            err("Bad read pos: "+s);
            return null;
        }
        ReadPos rp = new ReadPos();
        rp.readname = list.get(0);
        rp.pos = Long.parseLong(list.get(1));
        rp.endpos = Long.parseLong(list.get(2));
        if (rp.pos > rp.endpos) {
            err("Error parsing rp "+s+", endpos<pos: "+rp.endpos+"<"+rp.pos);
            return null;
        }
        rp.reversed = list.get(3).equalsIgnoreCase("true") ? 1: 0;
        rp.revcompread = list.get(4).equalsIgnoreCase("true")?  1: 0;
        return rp;
    }
    public boolean equals(Object obj) {
        ReadPos rp = (ReadPos) obj;
        return readname.equals(rp.readname) && rp.pos == pos && reversed == rp.reversed;
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(ReadPos.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        
        Logger.getLogger(ReadPos.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(ReadPos.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
//  System.out.println("ReadPos: " + msg);
        //Logger.getLogger( ReadPos.class.getName()).log(Level.INFO, msg);
    }
}
