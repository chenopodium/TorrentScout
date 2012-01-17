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
package com.iontorrent.wellalgorithms;

import com.iontorrent.utils.compile.InMemoryCompiler;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class WellAlgorithmFactory {

    public static WellAlgorithm createCustomAlgorithm(String computeMethodBody, WellContextFilter filter, int span) {
        String cl = getClassString(computeMethodBody);
        
        InMemoryCompiler comp = new InMemoryCompiler("com.iontorrent.wellalgorithm", "CustomAlg", cl);
        Class clazz = comp.compile();
        Constructor con = null;
        try {
            con = clazz.getConstructor(WellContextFilter.class, Integer.class);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(WellAlgorithmFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(WellAlgorithmFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        WellAlgorithm alg = null;
        try {
            alg = (WellAlgorithm) con.newInstance(filter, span);
            //WellAlgorithm alg = (WellAlgorithm) clazz.newInstance();
        } catch (InstantiationException ex) {
            Logger.getLogger(WellAlgorithmFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(WellAlgorithmFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(WellAlgorithmFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(WellAlgorithmFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return alg;
    }
    public static String getClassString(String computeMethodBody) {
        String s = "package com.iontorrent.wellalgorithms;\n";
        s+="\n";
        s+="import com.iontorrent.wellmodel.WellFlowData;\n";
        s+="import com.iontorrent.wellmodel.WellFlowDataResult;\n";
        s+="import com.iontorrent.wellmodel.WellFlowDataResult.ResultType;\n";
        s+="import java.util.ArrayList;\n";
        s+="\n";
        s+="public class NearestNeighbor extends WellAlgorithm {\n";
        s+="    \n";
        s+="    public NearestNeighbor(WellContextFilter filter, int span) {\n";
        s+="        super(filter, span);\n";
        s+="    }\n";
        s+="\n";
        s+="    @Override\n";
        s+="    public ArrayList<WellFlowDataResult> compute() {\n";
        s+=         computeMethodBody+"\n";            
        s+="    }\n";
        s+="}\n";
    return s;
    }
    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(WellAlgorithmFactory.class.getName()).log(Level.SEVERE, msg, ex);
    }
    
    private void err(String msg) {
        
        Logger.getLogger(WellAlgorithmFactory.class.getName()).log(Level.SEVERE, msg);
    }
    
    private static void warn(String msg) {
        Logger.getLogger(WellAlgorithmFactory.class.getName()).log(Level.WARNING, msg);
    }
    
    private static void p(String msg) {
        System.out.println("WellAlgorithmFactory: " + msg);
        //Logger.getLogger( WellAlgorithmFactory.class.getName()).log(Level.INFO, msg);
    }
}
