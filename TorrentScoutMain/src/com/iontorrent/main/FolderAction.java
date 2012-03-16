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
package com.iontorrent.main;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.FolderManager;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.guiutils.BasicAction;
import com.iontorrent.guiutils.HtmlViewer;
import com.iontorrent.utils.StringTools;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;

/**
 *
 * @author Chantal Roth
 */
public class FolderAction extends BasicAction implements HyperlinkListener {

    ExperimentContext exp;

    public FolderAction(ExperimentContext exp) {
        super("Show experiment paths", null, null);

        ImageIcon icon = new ImageIcon(getClass().getResource("folder-go.png"));
        this.exp = exp;
        this.setIcon(icon);
        this.setToolTipText("Display experiment paths and rules, and try to open folders in file explorer");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //GuiUtils.showNonModelMsg("Trying to open folder in file explorer...");   
        String s = getHtmlText(exp);
        s= "<body bgcolor=\"FFFFFF\">"+s+"</body>";
        HtmlViewer.show(WindowManager.getDefault().getMainWindow(), "Overview for " + exp.getResultsName(), s, this);
    }

    public static String getHtmlText(ExperimentContext exp) {
        FolderManager.getManager().setExperimentContext(exp, false);
        String rep = exp.getReportLink();
        if (rep == null) {
            rep = "";
        }
        if (rep.startsWith("/")) {
            rep = rep.substring(1);
        }
        if (rep.startsWith("iondb")) {
            rep = rep.substring(5);
        }
        if (rep.startsWith("/")) {
            rep = rep.substring(1);
        }
        String link = GlobalContext.getContext().getServerUrl() + "/" + rep;
        if (!link.startsWith("http://")) {
            link = "http://" + link;
        }

        String s = "<h3>Run name: " + exp.getResultsName() + "</h3>";
        s += "Url to report: <a href=\"" + link + "\">" + link + "</a><br>";
        s += "<br><table border=\"1\"><tr><th>Variable name</th><th>Variable value</th></tr>";
        s += "<tr><td>${PGM_NAME}</td><td>" + exp.getPgm() + "</td></tr>";
        s += "<tr><td>${EXP_NAME}</td><td>" + exp.getExperimentName() + "</td></tr>";
        s += "<tr><td>${RESULT_NAME}</td><td>" + exp.getResultsName() + "</td></tr>";
        s += "<tr><td>${EXP_DIR}</td><td>" + exp.getExpDir() + "</td></tr>";
        s += "<tr><td>${RESULTS_DIR}</td><td>" + exp.getResDirFromDb() + "</td></tr>";
        s += "<tr><td>${BASE}</td><td>" + FolderManager.getManager().getBaseDirs() + "</td></tr>";
        s += "</table>";
        s += "<br>Rule for results dir: <b>" + GlobalContext.getContext().getResultsRule() + "</b><br>";
        s += "Rule for raw dir: <b>" + GlobalContext.getContext().getRawRule() + "</b><br>";
        s += "<br>Raw dir based on rule: <a href=\"file://" + exp.getRawDir() + "\">"
                + exp.getRawDir() + "</a><br>";
        s += "Results dir based on rules: <a href=\"file://" + exp.getResultsDirectory() + "\">"
                + exp.getResultsDirectory() + "</a><br>";

        s += "<br><table border=\"1\"><tr><th>Property</th><th>Value</th></tr>";
        s += "<tr><td>PGM</td><td>" + exp.getPgm() + "</td></tr>";
        s += "<tr><td>Chip type</td><td>" + exp.getChipType().replace("\"", "") + "</td></tr>";
        s += "<tr><td>BAM file</td><td>" + exp.getBamFileName() + "</td></tr>";
        s += "<tr><td>SFF file</td><td>" + exp.getSffFileName() + "</td></tr>";
        s += "<tr><td>Nr flows</td><td>" + exp.getNrFlows() + "</td></tr>";
        s += "<tr><td>Nr cols</td><td>" + exp.getNrcols() + "</td></tr>";
        s += "<tr><td>Nr rows</td><td>" + exp.getNrrows() + "</td></tr>";
        s += "<tr><td>Library key</td><td>" + exp.getLibraryKey() + "</td></tr>";
        s += "<tr><td>Flow order</td><td>" +  StringTools.addNL(exp.getFlowOrder(), "<br>" ,25) + "</td></tr>";

        s += "</table>";
        // JOptionPane.showMessageDialog(null, "<html>" + s + "</html>");
        return s;
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(FolderAction.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {

        Logger.getLogger(FolderAction.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(FolderAction.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
//  System.out.println("FolderAction: " + msg);
        //Logger.getLogger( FolderAction.class.getName()).log(Level.INFO, msg);
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getURL() == null || !e.getEventType().toString().equalsIgnoreCase("ACTIVATED")) {
            return;
        }
        // JOptionPane.showMessageDialog(null, "Got URL: " + e.getURL()+", type: "+e.getEventType());
        String url = e.getURL().toString();
        p("Got url: " + url);
        if (Desktop.isDesktopSupported()) {
            Desktop d = Desktop.getDesktop();
            String file = "";
            if (url.startsWith("file://")) {
                file = url.substring(7);
                p("File: " + file);
                try {
                    d.open(new File(file));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                try {
                    p("Trying to browse to " + url);
                    d.browse(new URI(url));
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
