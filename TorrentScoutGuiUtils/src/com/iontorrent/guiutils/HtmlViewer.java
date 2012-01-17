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
package com.iontorrent.guiutils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

/**
 *
 * @author Chantal Roth
 */
public class HtmlViewer {

   
    public static JComponent getComponent(String html, HyperlinkListener listener) {
          // create jeditorpane
        JEditorPane jEditorPane = new JEditorPane();
        jEditorPane.setOpaque(false);
        // make it read-only
        jEditorPane.setEditable(false);
        
        // create a scrollpane; modify its attributes as desired
        JScrollPane scrollPane = new JScrollPane(jEditorPane);
        
        // add an html editor kit
        HTMLEditorKit kit = new HTMLEditorKit();
        jEditorPane.setEditorKit(kit);
        if (listener != null) {
            jEditorPane.addHyperlinkListener(listener);
        }
//        // add some styles to the html
//        StyleSheet styleSheet = kit.getStyleSheet();        
//        styleSheet.addRule("h1 {color: blue;}");
//        styleSheet.addRule("h2 {color: #ff0000;}");
      //  styleSheet.addRule("pre {font : 10px monaco; color : black; background-color : #fafafa; }");

        // create some simple html as a string
        
        String htmlString = "<h1>Welcome!</h1>\n"
                          + "<h2>This is an H2 header</h2>\n"
                          + "<p>This is some sample text</p>\n"
                          + "<p><a href=\"http://devdaily.com/blog/\">devdaily blog</a></p>\n";
        
        if (html != null) htmlString = html.trim();
        if (!html.startsWith("<html><body>") && !html.startsWith("<html>\n<body>")) {                        
            html = "<html><body bgcolor=\"FFFFFF\">"+html+"\n</body></html>";
        }
        else if (!html.startsWith("<html>")) {
            html = "<html>"+html+"</html>";
        }
      //  p("1. About to show html page: "+html);
        // create a document, set it on the jeditorpane, then add the html
        Document doc = kit.createDefaultDocument();
        jEditorPane.setDocument(doc);
        jEditorPane.setText(htmlString);
        
        return scrollPane;
    }
    public static void show( Frame frame, String title, String html,HyperlinkListener listener) {
        JComponent comp = getComponent(html, listener);

        // now add it all to a frame
        JFrame j = new JFrame(title);
        if (frame != null) j.setIconImage(frame.getIconImage());
        j.getContentPane().add(comp, BorderLayout.CENTER);
     
        j.setSize(new Dimension(800,600));
     
        // center the jframe, then make it visible
        j.setLocation(400, 200);
        j.setVisible(true);
        j.show();
      //  p("showing page: "+html);
        j.repaint();
        //JOptionPane.showMessageDialog(null, scrollPane);
    }
    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(HtmlViewer.class.getName()).log(Level.SEVERE, msg, ex);
    }
    
    private void err(String msg) {
     
        Logger.getLogger(HtmlViewer.class.getName()).log(Level.SEVERE, msg);
    }
    
    private static void warn(String msg) {
        Logger.getLogger(HtmlViewer.class.getName()).log(Level.WARNING, msg);
    }
    
    private static void p(String msg) {
        System.out.println("HtmlViewer: " + msg);
        //Logger.getLogger( HtmlViewer.class.getName()).log(Level.INFO, msg);
    }
}
