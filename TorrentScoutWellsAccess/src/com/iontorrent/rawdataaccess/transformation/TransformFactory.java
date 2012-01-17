/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.rawdataaccess.transformation;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class TransformFactory {

    static ArrayList<DataTransformation> list;
    
    public static ArrayList<DataTransformation> getTransformations() {
        if (list != null && list.size()>0) return list;
        Normalize norm = new Normalize();
        XTChannelCorrect cor = new XTChannelCorrect();
        list = new ArrayList<DataTransformation>();
        list.add(norm);
        list.add(cor);
        return list;
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(TransformFactory.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {

        Logger.getLogger(TransformFactory.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(TransformFactory.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("TransformFactory: " + msg);
        //Logger.getLogger( TransformFactory.class.getName()).log(Level.INFO, msg);
    }
}
