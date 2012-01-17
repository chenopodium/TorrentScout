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
package com.iontorrent.utils.compile;


import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.janino.SimpleCompiler;


/**
 *
 * @author Chantal Roth
 */
public class InMemoryCompiler {

    protected String name;
    protected String source;
    protected Class compiledClass;
    private String classname;
    private String pack;
    
    public InMemoryCompiler(String pack, String name, String source) {
        this.name = name;
        this.source = source;
        String path = pack.replace(".", "/");
        this.pack = pack;
        classname = "src/"+path+"/"+ name+".java";
    }

    public Class compile() {
        createIt();
//        try {
//            SimpleCompiler comp =  new SimpleCompiler(classname) ;
//            try {
//                compiledClass = comp.getClassLoader().loadClass(pack+"."+name);
//            } catch (ClassNotFoundException ex) {
//                err(ex.getMessage(), ex);
//            }
//        } catch (Exception ex) {
//          err(ex.getMessage(), ex);        
//        }
//       return compiledClass;
        return null;
    }
    
    public void createIt() {
        try {
            File f = new File(classname);
            if (f.exists()) f.delete();
            File dir = f.getParentFile();
            if (!dir.exists()) dir.mkdirs();
            FileWriter aWriter = new FileWriter(classname, true);
            aWriter.write(source);
            aWriter.flush();
            aWriter.close();
            classname = f.getPath();
        } catch (Exception e) {
            err("Could not write file: "+classname, e);
        }
       // JOptionPane.showMessageDialog(null ,"Created file: "+classname);
    }

    
    public void executeMain() throws Exception {
        Method m = compiledClass.getMethod("main", new Class[]{String[].class});
        m.invoke(null, new Object[]{null});
    }

    public double executeDouble(String name, double arg1, double arg2) throws Exception {
        Method m = compiledClass.getMethod(name, new Class[]{Double.class, Double.class});
        Double res = (Double) m.invoke(null, new Object[]{arg1, arg2});
        return res.doubleValue();
    }

    public double executeDouble(String name, double arg1) throws Exception {
        Method m = compiledClass.getMethod(name, new Class[]{Double.class});
        Double res = (Double) m.invoke(null, new Object[]{arg1});
        return res.doubleValue();
    }

    public Class getCompiledClass() {
        return this.compiledClass;
    }

    static class InMemorySourceCompilerClassLoader extends ClassLoader {

        public Class getClassFromBytes(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }

   
    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(InMemoryCompiler.class.getName()).log(Level.SEVERE, msg, ex);
    }
    
    private void err(String msg) {
      
        Logger.getLogger(InMemoryCompiler.class.getName()).log(Level.SEVERE, msg);
    }
    
    private static void warn(String msg) {
        Logger.getLogger(InMemoryCompiler.class.getName()).log(Level.WARNING, msg);
    }
    
    private static void p(String msg) {
        System.out.println("InMemoryCompiler: " + msg);
        //Logger.getLogger( InMemoryCompiler.class.getName()).log(Level.INFO, msg);
    }
}
