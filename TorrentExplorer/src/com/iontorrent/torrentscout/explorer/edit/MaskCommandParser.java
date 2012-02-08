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

import com.iontorrent.rawdataaccess.wells.BitMask;
import com.iontorrent.torrentscout.explorer.ExplorerContext;
import com.iontorrent.utils.StringTools;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class MaskCommandParser {
  ExplorerContext cont;
    /** Creates new form MaskCommandPanel */
    ArrayList<BitMask> tmpmasks;
    ArrayList<BitMask> masks;
    BitMask template;
    ArrayList<String> errors;
    BitMask res;
    String message;
    
    public MaskCommandParser(ExplorerContext cont) {
        this.cont = cont;
    }

    public BitMask getResult() {
        return res;
    }

    public String doParseAction(String cmd) {
        res = parse(cmd);
        String err = "";
        String msg = "";
        if (errors.size() > 0) {
            err += "\nErrors:\n" + errors.toString();
        }
        if (res != null) {
           if (err.length()>0) msg = "result is in: " + res + err;
        } else {
            msg = "Got no reslt" + err;
        }
        // cont.masksChanged();
        return msg;
    }

    public void executeInnermost(String cmd, BitMask res) {
        // no mor parenthesis
        // find operator
        ArrayList<String> args = StringTools.parseList(cmd, " ");
        // 1 and 2, not 1, invert 1, 1 or 2, 1 minus 2
        // only not and invert
        if (args.size() == 1) {
            // a = b
            String a = args.get(0);
            BitMask mask = findMask(a, true);
            if (mask != null) {
                res.copyFrom(mask);
            }
        } else if (args.size() == 2) {
            String a = args.get(0);
            if (isNot(a) || isCopy(a)) {
                String name = args.get(1);
                BitMask mask = findMask(name, true);
                if (mask != null) {
                    if (isNot(a)) {
                        p("Inverting " + mask);
                        res.invert(mask);
                    } else {
                        p("copying " + mask);
                        res.copyFrom(mask);
                    }
                }

            } else {
                addError("Found 2 arguments, was expecting not or copy: " + cmd);
            }
        } else if (args.size() == 3) {
            String name1 = args.get(0);
            String op = args.get(1);
            String last = args.get(2);
            BitMask mask1 = findMask(name1, true);
            if (isShift(op)) {
                // get how much
                int d = 0;
                try {
                    d = Integer.parseInt(last);
                } catch (Exception e) {
                    addError("Could not convert " + last + " to integer");
                    return;
                }
                if (isShiftLeft(op)) {
                    res.shift(mask1, -d, 0);
                } else if (isShiftRight(op)) {
                    res.shift(mask1, d, 0);
                } else if (isShiftup(op)) {
                    res.shift(mask1, 0, -d);
                } else if (isShiftdown(op)) {
                    res.shift(mask1, 0, d);
                }
            } else {
                String name2 = last;
                BitMask mask2 = findMask(name2, true);

                if (mask1 != null && mask2 != null) {
                    p("evaluating: '" + res + "'='" + mask1 + "' " + op + " '" + mask2 + "'");
                    if (isAnd(op)) {
                        res.intersect(mask1, mask2);
                    } else if (isOr(op)) {
                        res.add(mask1, mask2);
                    } else if (isPlus(op)) {
                        res.add(mask1, mask2);
                    } else if (isMinus(op)) {
                        res.subtract(mask1, mask2);
                    } else {
                        addError("Don't know what operation " + op + " is");
                    }
                }
            }


        } else if (args.size() == 4) {
            String name1 = args.get(0);
            String op = args.get(1);
            String sx = args.get(2);
            String sy = args.get(2);
            BitMask mask1 = findMask(name1, true);
            if (isShiftDiag(op)) {
                // get how much
                int dx = 0;
                try {
                    dx = Integer.parseInt(sx);
                } catch (Exception e) {
                    addError("Could not convert " + sx + " to integer");
                    return;
                }
                int dy = 0;
                try {
                    dy = Integer.parseInt(sy);
                } catch (Exception e) {
                    addError("Could not convert " + sy + " to integer");
                    return;
                }
                res.shift(mask1, dx, dy);
            }
        } else {
            addError("Got " + args.size() + " arguments: " + cmd + ", not sure what to do :-)");
        }

    }

    public BitMask parse(String cmd) {
        BitMask res = null;
        if (cont == null) {
            return null;
        }
        
        if (cmd == null || cmd.length() < 1) {
            return null;
        }
        cmd = normalize(cmd);
        masks = cont.getMasks();
        if (masks == null || masks.size() < 1) {
            return null;
        }

        errors = new ArrayList<String>();
        template = masks.get(0);
        tmpmasks = new ArrayList<BitMask>();

        // first see if there is an '='
        int eq = cmd.indexOf("=");
        if (eq > 0) {
            String left = cmd.substring(0, eq - 1);
            String right = cmd.substring(eq + 1).trim();
            // p("Got left: " + left + ", right: " + right);
            res = findMask(left, false);
            if (res == null) {
                p("creating new result mask with name " + left);
                res = new BitMask(template);
                res.setName(left);
                masks.add(res);
            }
            execute(res, right);
        } else {
            // got some other command
            res = parseOtherCommand(cmd);
        }
        // now parse recursively...
        return res;
    }
    

    private BitMask parseOtherCommand(String cmd) {
        res = null;
        if (cmd.startsWith("new")) {
            res = new BitMask(template);
        } else if (cmd.startsWith("clear")) {
            message = "";
        } else if (cmd.startsWith("hello")) {
            message = "hi :-)";
        }
        return res;
    }

    private void execute(BitMask res, String cmd) {
        // p("parsing " + cmd);
        // (m1 - m2) + (not m3)
        // find inner most ()
        int left = cmd.lastIndexOf("(");
        if (left > -1) {
            //  p("Found ( at " + left);
            int right = cmd.indexOf(")", left + 1);
            if (right > -1) {

                //p("Found ()");
                String inner = cmd.substring(left + 1, right);
                //   p("parsing "+inner);
                int nr = tmpmasks.size();
                BitMask tmp = new BitMask(template);
                tmp.setName("tmp" + nr);;
                tmpmasks.add(tmp);
                executeInnermost(inner, tmp);
                String newcmd = "";
                if (left > 0) {
                    newcmd = cmd.substring(0, left - 1) + " ";
                } else {
                    newcmd = "";
                }
                newcmd += tmp.getName();
                if (right + 1 < cmd.length()) {
                    newcmd += " " + cmd.substring(right + 1);
                }
                //p("Command is now: "+newcmd);
                execute(res, newcmd);
            } else {
                addError("Non matching parenthesis. Found ( in " + cmd + " but no ) after the (");
                return;
            }
        } else {
            executeInnermost(cmd, res);
        }

    }

    private boolean isNot(String s) {
        s = s.trim();
        return s.equals("!") || s.equals("not") || s.equals("~");
    }

    private boolean isCopy(String s) {
        s = s.trim();
        return s.equals("copy") || s.equals("dupliate");
    }

    private boolean isShiftLeft(String s) {
        s = s.trim();
        return s.equals("<") || s.equals("shiftleft") || s.equals("left");
    }

    private boolean isShiftDiag(String s) {
        s = s.trim();
        return s.equals("shift") || s.equals("diag") || s.equals("shiftdiag");
    }

    private boolean isShift(String s) {
        return isShiftLeft(s) || isShiftRight(s) || isShiftup(s) || isShiftdown(s);
    }

    private boolean isShiftRight(String s) {
        s = s.trim();
        return s.equals(">") || s.equals("shiftright") || s.equals("right");
    }

    private boolean isShiftup(String s) {
        s = s.trim();
        return s.equals("^") || s.equals("shiftup") || s.equals("up");
    }

    private boolean isShiftdown(String s) {
        s = s.trim();
        return s.equals("v") || s.equals("shiftdown") || s.equals("down");
    }

    private boolean isAnd(String s) {
        s = s.trim();
        return s.equals("and") || s.equals("&");
    }

    private boolean isOr(String s) {
        s = s.trim();
        return s.equals("or") || s.equals("|");
    }

    private boolean isPlus(String s) {
        s = s.trim();
        return s.equals("+");
    }

    private boolean isMinus(String s) {
        s = s.trim();
        return s.equals("-");
    }

 
    public String normalize(String cmd) {
        cmd = cmd.toLowerCase().trim();

        cmd = cmd.replace("\t", " ");
        cmd = cmd.replace("\n", " ");
        cmd = cmd.replace("'", "");
        cmd = cmd.replace("\"", "");
        cmd = cmd.replace("[", "(");
        cmd = cmd.replace("]", ")");
        cmd = cmd.replace("}", ")");
        cmd = cmd.replace("{", "(");

        cmd = cmd.replace("not", " ! ");
        cmd = cmd.replace("~", " ! ");
        cmd = cmd.replace("and", " & ");
        cmd = cmd.replace("intersect", " & ");
        cmd = cmd.replace(" or", " + ");
        //cmd = cmd.replaceAll("xor", "^");
        cmd = cmd.replace("minus", " - ");
        cmd = cmd.replace("subtract", " - ");
        cmd = cmd.replace("add", " + ");
        cmd = cmd.replace("+", " + ");
        cmd = cmd.replace("-", " - ");
        cmd = cmd.replace("!", " ! ");
        cmd = cmd.replaceAll("  ", " ");
        p("Normalized command: " + cmd);
      
        return cmd;
    }

    private BitMask findMask(String name, boolean errorifnotfound) {
        if (name.startsWith("tmp")) {
            for (BitMask m : tmpmasks) {
                if (m.getName().equalsIgnoreCase(name)) {
                    return m;
                }
            }
        } else {
            for (BitMask m : masks) {
                if (m.getName()==null) {
                    err("Mask has no name: "+m);
                }
                
                else if (m.getName().equalsIgnoreCase(name)) {
                    return m;
                }
            }
            // check mask name after space
            for (int i = 0; i < masks.size(); i++) {
                BitMask m = masks.get(i);
                String mname = m.getName();
                int p = mname.indexOf(" ");
                int q = mname.indexOf(".");
                if (p > 0 || q > 0) {
                    p = Math.max(p, q);
                    mname = mname.substring(p).trim();
                    if (name.equalsIgnoreCase(mname)) {
                        return m;
                    }
                }

            }
            // check mask number
            for (int i = 0; i < masks.size(); i++) {
                BitMask m = masks.get(i);
                if (name.equalsIgnoreCase("" + i)) {
                    return m;
                }
            }
            // check name start
            for (int i = 0; i < masks.size(); i++) {
                BitMask m = masks.get(i);
                if (m.getName().toLowerCase().startsWith(name)) {
                    return m;
                }
            }
            // check mask name after space
            for (int i = 0; i < masks.size(); i++) {
                BitMask m = masks.get(i);
                String mname = m.getName();
                int p = mname.indexOf(" ");
                int q = mname.indexOf(".");
                if (p > 0 || q > 0) {
                    p = Math.max(p, q);
                    mname = mname.substring(p).trim().toLowerCase();
                    if (mname.startsWith(name)) {
                        return m;
                    }
                }

            }
        }
        if (errorifnotfound) {
            addError("I don't know which mask you mean with '" + name + "' - use the full name, or the number of the mask (starting with 0)");
        }
        return null;

    }
    private void addError(String err) {
        errors.add(err);        
    }
    public ArrayList<String> getErrors() {
        return errors;
    }
     
    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(MaskCommandParser.class.getName()).log(Level.SEVERE, msg, ex);
    }
    
    private static void err(String msg) {
        Logger.getLogger(MaskCommandParser.class.getName()).log(Level.SEVERE, msg);
    }
    
    private static void warn(String msg) {
        Logger.getLogger(MaskCommandParser.class.getName()).log(Level.WARNING, msg);
    }
    
    private static void p(String msg) {
        //System.out.println("MaskCommandParser: " + msg);
        Logger.getLogger(MaskCommandParser.class.getName()).log(Level.INFO, msg);
    }

    public String getMessage() {
        return message;
    }
}
