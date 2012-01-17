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
package com.iontorrent.seqview;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.wellmodel.WellContext;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.iontorrent.seq.Read;
import org.iontorrent.seq.alignment.Alignment;

/**
 *
 * @author Chantal Roth
 */
public class AlignmentPanel extends JPanel {

    private final int WIDTH = 80;
    JEditorPane area;

    public AlignmentPanel() {
        setLayout(new BorderLayout());
        area = new JEditorPane();
        area.setContentType("text/html");
        try {
            area.setText("");
        } catch (Exception e) {
            err("Error updating text pane: " + e.getMessage());
        }
        add("Center", new JScrollPane(area));
    }

    public void update(ExperimentContext exp, WellContext context, Read read, String error) {
        // p("Got :"+read);

        String msg = "";
        String title = "<H2>Run "+exp.getResultsName()+"</H2>";
        title += "<b><font color='000099'>Alignment at " + context.getAbsoluteCoordinate() + "</font></b><br>";
        // expContext.
        Alignment al = null;
        if (read != null) {
            msg += "<b>Sff read " + read.getName().trim() + "</b>: ";
            //  msg += getCoordString(WIDTH)+"<br>";
            msg += read.toSequenceString() + "<br>";
            //    msg += "<b>Flowgram</b>:"+ Arrays.toString(read.getFlowgram())+"<br>";
            //  msg += "<b>Empty flows</b>:"+ read.getEmptyFlows()+"<br>";
            // msg += "<b>Non-empty flows</b>:"+ read.getNonEmptyFlows()+"<br>";
            //msg += "<b>Flow index</b>:"+ Arrays.toString(read.getf())+"<br>");
            al = read.getAlign();
        } else {
            msg = "I found no sff read at this location. ";
            if (error != null && error.length() > 0) {
                msg += " Reason:" + error;
                error = null;
            }
        }

        if (al != null) {
            msg += "<br><b>Flags:</b> " + read.getFlags() + " (reverse: " + read.isReverse() + ")";
            //   msg += "<br><b>Analysis command line:</b><br> " + read.getCommandLine();
            msg += "<br><b>Genome position:</b> " + read.getAlignmentStart() + "-" + read.getAlignmentEnd();

            if (read.isReverse()) {
                Alignment rev = al.getReverseAlignment();
                rev.calculateStats();
                msg += "<br><br><b>Alignment in sequencing order:</b>";
                msg += rev.toHtml();
                msg += "<br><b>Alignment in reverse order (forward relative to reference):</b>";
            }
            msg += al.toHtml();
            msg += "<br><b>Reference:</b> " + read.getAlign().getRefSeq1().toSequenceString();          
            msg += "<br><b>Cigar string:</b> " + read.getCigarString();
            msg += "<br><b>MD string:</b> " + read.getMd();

        } else {
            msg += "<br>I see no alignment.";
            if (error != null && error.length() > 0) {
                msg += " Reason:" + error;
                error = null;
            }
        }
        if (read != null && read.getFlowgram() != null) {
            msg += "<br><b>Flow Key</b>:" + read.getKey() + "<br>";
            msg += "<br><b>Flowgram</b>:";
            msg += read.getHtmlFlowGramInfo();
            msg += "<br><b>Flow Index</b>:<br>" + Arrays.toString(read.getAbsoluteFlowIndex()) + "<br>";
            msg += "<br><b>Flowgram</b>:<br>" + Arrays.toString(read.getFlowgram()) + "<br>";
        }
        try {
            if (error != null) {
                msg += "<br>" + error;
            }
            area.setText("<html>" + title + "<font face='Courier' size='3'>" + msg + "</font></html>");
            area.setCaretPosition(0);
            area.scrollRectToVisible(new Rectangle(0,0,0,0));

        } catch (Exception e) {
            err("Error updating text pane: " + e.getMessage());
        }
//        
//       
//        invalidate();
//        revalidate();

    }

//     public String getCoordString(int len) {
//        
//        StringBuffer res = new StringBuffer();
//        for (int pos = 0; pos <= len; pos++) {
//            res = res.append(" ");
//        }
//           
//        for (int pos = 10; pos <=len; pos+=10) {
//            res.insert(pos, ""+pos);           
//        }
//        String s = res.toString();
//        s = s.replace(" ","&nbsp;");
//        return s;
//    }
    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(AlignmentPanel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(AlignmentPanel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(AlignmentPanel.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("AlignmentPanel: " + msg);
        //Logger.getLogger( AlignmentPanel.class.getName()).log(Level.INFO, msg, ex);
    }

    void clear(ExperimentContext exp) {
        String title = "No experiment selected yet"; 
        if (exp != null) title = "<H2>"+exp.getResultsName()+"</H2><br>No well selected yet";
        this.area.setText(title);
    }
}
