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
package com.iontorrent.main.data;

import com.iontorrent.main.data.RawFileConverter.Conversion;
import com.iontorrent.rawdataaccess.pgmacquisition.RasterIO;
import com.iontorrent.rawdataaccess.pgmacquisition.RawDataFacade;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.utils.io.FileUtils;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Chantal Roth
 */
public class RawFileConverter {

    private Conversion[] conv;

    public RawFileConverter() {
    }

    public class Conversion {

        private int start;
        private int end;
        private int max;
        private RawType type;

        public Conversion(RawType type) {
            this.type = type;
        }

        public String toString() {
            return "Conversion from " + getStart() + "-" + getEnd() + " of " + getType().getDescription();
        }

        /**
         * @return the start
         */
        public int getStart() {
            return start;
        }

        /**
         * @return the end
         */
        public int getEnd() {
            return end;
        }

        /**
         * @param end the end to set
         */
        public void setEnd(int end) {
            this.end = end;
        }

        /**
         * @return the max
         */
        public int getMax() {
            return max;
        }

        /**
         * @return the type
         */
        public RawType getType() {
            return type;
        }
    }

    public Conversion[] getConversion() {
        return conv;
    }

    public String collectConversionParams(String raw_dir, String cache_dir) {
        
       String msg = createConversionObject(raw_dir, cache_dir);
       if (msg != null) return msg;
       
        ConversionPanel cpanel = new ConversionPanel(conv, raw_dir, cache_dir);
        int ans = JOptionPane.showConfirmDialog(null, cpanel, "Raw data caching for faster viewing", JOptionPane.OK_CANCEL_OPTION);
        if (ans != JOptionPane.OK_OPTION) {
            p("User aborted conversion");
            conv = null;
            return msg;
        } else {
            msg = cpanel.getResult();
            if (msg != null && msg.length() > 0) {
                JOptionPane.showMessageDialog(null, "<html>RawFileConverter:<br>Got a msg from getting result:" + msg + "</html>");
                conv = null;
            }
        }
        return msg;

    }
    public String showConversion(String raw_dir, String cache_dir) {
        
       String msg = createConversionObject(raw_dir, cache_dir);
       if (msg != null) return msg;
       
        ConversionPanel cpanel = new ConversionPanel(conv, raw_dir, cache_dir);
        JOptionPane.showMessageDialog(null, cpanel, "Conversion is done: result of conversion", JOptionPane.OK_CANCEL_OPTION);
        
        return msg;

    }
    
    public String createConversionObject(String raw_dir, String cache_dir) {
        String d = raw_dir;
        if ( (FileUtils.isUrl(d) && !FileUtils.exists(d+"acq_0000.dat")) || 
                    (!FileUtils.isUrl(d) && !(new File(d).exists()))) {
            return "I cannot access the raw directory "+raw_dir+", please check the path<br>";
        }
        File dir = new File(cache_dir);
        if (!dir.exists()) {
            return "I cannot access the cache directory "+dir+", please check the path<br>";
        }
        if (!dir.canWrite()) {
            return "I don't have write permssion in the ache directory "+dir+", please change the path<br>";
        }
        conv = new Conversion[RawType.values().length];

        String msg = "";
        for (int i = 0; i < RawType.values().length; i++) {
            RawType rtype = RawType.values()[i];
            Conversion con = new Conversion(rtype);
            conv[i] = con;
            int nrfiles = RasterIO.getNrFiles(rtype, raw_dir);
           
            con.max = nrfiles;
            // count nr of flows so far
            RawDataFacade io = RawDataFacade.getFacade(raw_dir, cache_dir, rtype);

            int nrflowsSoFar = io.getNrFlowsInCache();
            if (nrflowsSoFar < 0) {
                String raw = io.getRawFilePath(rtype, raw_dir, 0);
                if (raw != null && FileUtils.exists(raw)) {
                    p("I could not find the raw file or url "+raw +" (Myabe it's an old experiment?)<br>");               
                }
                //else p("Problem with accessing raw file in " + raw_dir + " for type " + rtype + "<br>");
            }
            con.start = nrflowsSoFar;
            // the end is between start and nrfiles so far
        }
        if (msg != null && msg.length() > 0) {
            JOptionPane.showMessageDialog(null, "<html>RawFileConverter<br>" + msg + "</html>");
            conv = null;
            return msg;
        }
        return null;
    }
    
    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(RawFileConverter.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(RawFileConverter.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(RawFileConverter.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
//  System.out.println("RawFileConverter: " + msg);
        //Logger.getLogger( RawFileConverter.class.getName()).log(Level.INFO, msg, ex);
    }
}
