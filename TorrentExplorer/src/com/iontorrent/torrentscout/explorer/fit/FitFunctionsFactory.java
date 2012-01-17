/*
 * Copyright (C) 2011 Life Technologies Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.iontorrent.torrentscout.explorer.fit;

import com.iontorrent.torrentscout.explorer.ExplorerContext;
import com.iontorrent.torrentscout.explorer.process.PlotFunction;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class FitFunctionsFactory {

    static ArrayList<AbstractHistoFunction> fun;

    static ArrayList<PlotFunction> plot;
    
    static AbstractHistoFunction peak;
    static AbstractHistoFunction zeromer;
    
    public static ArrayList<AbstractHistoFunction> getFunctions(ExplorerContext maincont) {
        if (fun == null)  {
            fun = new ArrayList<AbstractHistoFunction> ();
            peak = new PeakFunction(maincont);
            fun.add(peak);
            zeromer = new ParametricAdjustment(maincont);
            fun.add(zeromer);
            fun.add(new CombineFunction(maincont, (PlotFunction)peak, (PlotFunction)zeromer));
            fun.add(new IntegralFunction(maincont));
            fun.add(new MaxMinusEndHeightFunction(maincont));
            // this.boxFunc.addItem(new EndheightlFunction1(maincont));
            fun.add(new RSMErrorFunction(maincont));
            fun.add(new CountFunction(maincont));
            fun.add(new SlopeFunction(maincont));
        }
        for (AbstractHistoFunction f: fun) {
            f.setContext(maincont);
        }
        return fun;
    }
    public PlotFunction getPeakFunction() {
        return (PlotFunction) peak;
    }
    public PlotFunction getZeromerFunction() {
        return (PlotFunction) zeromer;
    }
     public static ArrayList<PlotFunction> getPlotFunctions(ExplorerContext maincont) {
         if (plot == null) {
            plot = new ArrayList<PlotFunction> ();
            if (fun == null)  {
                fun = getFunctions (maincont);           
            }
            for (AbstractHistoFunction f: fun) {
                if (f instanceof PlotFunction) {
                    plot.add((PlotFunction)f);
                }
            }
         }
        return plot;
    }


    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(FitFunctionsFactory.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        Logger.getLogger(FitFunctionsFactory.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(FitFunctionsFactory.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        //System.out.println("FitFunctionsFactory: " + msg);
        Logger.getLogger(FitFunctionsFactory.class.getName()).log(Level.INFO, msg);
    }
}
