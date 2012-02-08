/*
 * Copyright (C) 2012 Life Technologies Inc.
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
package com.iontorrent.torrentscout.explorer.edit;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class OperationFactory {

    public static ArrayList<AbstractOperation> getOps() {
        ArrayList<AbstractOperation> ops = new ArrayList<AbstractOperation>();
        ops.add(new AddOp());
        ops.add(new SubtractOp());
        ops.add(new AndOp());
        ops.add(new XorOp());
        ops.add(new CopyOp());
        ops.add(new ShiftOp(0, 0));

        ops.add(new ShiftOp(0, 4));
        ops.add(new ShiftOp(4, 0));
        ops.add(new ShiftOp(-4, 0));
        ops.add(new ShiftOp(0, -4));
        return ops;
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(OperationFactory.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        Logger.getLogger(OperationFactory.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(OperationFactory.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        //System.out.println("OperationFactory: " + msg);
        Logger.getLogger(OperationFactory.class.getName()).log(Level.INFO, msg);
    }
}
