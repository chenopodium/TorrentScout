/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iontorrent.acqview;

import com.iontorrent.wellmodel.WellFlowDataResult.ResultType;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;

/**
 *
 * @author Chantal Roth
 */
public class ResultBox extends JCheckBox {

    ResultType type;

    public ResultBox(String name, ResultType type) {
        super(name);
        this.type = type;
        this.setToolTipText("<html>" + type.getDesc() + "</html>");
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(ResultBox.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        
        Logger.getLogger(ResultBox.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(ResultBox.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("ResultBox: " + msg);
        //Logger.getLogger( ResultBox.class.getName()).log(Level.INFO, msg);
    }
}
