/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.rawdataaccess.transformation;


import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.utils.system.Parameter;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellFlowData;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public abstract class DataTransformation {

    private String name;
    private String description;
    protected Parameter[] params;
    protected WellContext context;
    protected RawType type;
    private boolean enabled;
    
    public DataTransformation(String name, String description) {
        this.name = name;
        this.description = description;
        setEnabled(true);
    }
    @Override
    public String toString() {
        return name;
    }
    
    public String toLongString() {        
        return toFullString()   ;
    }
    public abstract String transform(WellFlowData data, WellCoordinate coord, int flow);

    public String toFullString() {
        String s = "";//getName() + "\n" + getDescription() + "\n";
        if (params != null && params.length>0) {            
            for (Parameter p : this.getParams()) {
                s += p.toString() + "\n";
            }
        }
        return s;
    }

    public int getNrParams() {
        return getParams().length;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the params
     */
    public Parameter[] getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(Parameter[] params) {
        this.params = params;
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(DataTransformation.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {

        Logger.getLogger(DataTransformation.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(DataTransformation.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("DataTransformation: " + msg);
        //Logger.getLogger( DataTransformation.class.getName()).log(Level.INFO, msg);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the context
     */
    public WellContext getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(WellContext context, RawType type) {
        this.context = context;
        this.type = type;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
