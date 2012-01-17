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

/*
 * MaskCommandPanel.java
 *
 * Created on 17.11.2011, 17:04:54
 */
package com.iontorrent.torrentscout.explorer.edit;

import com.iontorrent.rawdataaccess.wells.BitMask;
import com.iontorrent.torrentscout.explorer.ExplorerContext;
import com.iontorrent.utils.StringTools;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author Chantal Roth
 */
public class MaskCommandPanel extends javax.swing.JPanel {

    ExplorerContext cont;
    /** Creates new form MaskCommandPanel */
    ArrayList<BitMask> tmpmasks;
    ArrayList<BitMask> masks;
    BitMask template;
    ArrayList<String> errors;
    BitMask res;
    ActionListener list;

    public MaskCommandPanel(ExplorerContext cont, ActionListener list) {
        initComponents();
        this.cont = cont;
        this.list = list;
    }

    public BitMask getResult() {
        return res;
    }

    public void doParseAction() {
        res = parse();
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
       if (msg != null && msg.length()>0) JOptionPane.showMessageDialog(this, msg);
        // this.txtCmd.append("\n" + msg);
        cont.maskChanged(res);
        // cont.masksChanged();
        list.actionPerformed(null);
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

    public BitMask parse() {
        BitMask res = null;
        if (cont == null) {
            return null;
        }
        String cmd = this.txtCmd.getText();
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

    private void doHintAction() {
        String msg = "<html>You can do the following things here:<ul>";
        msg += "<li>General commands: new, clear</li>";
        msg += "<li>Mask names: (say the mask is 1. ignore)<br>"
                + "- you can use the number 1<br>"
                + "- the full mask name 1. ignore<br>"
                + "- just the part aftr the space ignoree<br>"
                + "- just the beginning of the name 1. ig"
                + "<br>(it will pick the first it finds)</li>";
        msg += "<li>And operation: and, & </li>";
        msg += "<li>Or operation: or, |</li>";
        msg += "<li>Plus operation: add, + </li>";
        msg += "<li>Subtract operation: minus, subtract, - </li>";
        msg += "<li>Not operation: not, !, ~ </li>";
        msg += "<li>Copy operation: copy, duplicate </li>";
        //   msg += "<li>(shift operations are almost implemented) </li>";
        msg += "<li>Shift diagonal: shift, diag, shiftdiag; </li>";
        msg += "<li>Shift left: shiftleft, left, &lt; </li>";
        msg += "<li>Shift right: shiftright, right, &gt; </li>";
        msg += "<li>Shift up: shiftup, up, ^ </li>";
        msg += "<li>Shift down: shiftdown, down, v </li>";
        msg += "<li>Example: ignore = (empty or pinned)</li>";
        msg += "<li>Example: newbg = (empty minus somemask)</li>";
        msg += "<li>Example: c = (a | b) & (c | d)</li>";
        msg += "<li>Example: c = !a and (b > 4) (shifting be to the right by for, combining with not a)</li>";
        msg += "<li>Example:0 = 0 shift 4 4 (shifting mask by 4 in x and 4 in y direction)</li>";
        msg += "</ul></html>";
        JOptionPane.showMessageDialog(this, msg);
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
        this.txtCmd.setText(cmd);;
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
                if (m.getName().equalsIgnoreCase(name)) {
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

    private BitMask parseOtherCommand(String cmd) {
        BitMask res = null;
        if (cmd.startsWith("new")) {
            res = new BitMask(template);
        } else if (cmd.startsWith("clear")) {
            this.txtCmd.setText("");
        } else if (cmd.startsWith("hello")) {
            this.txtCmd.setText("hi :-)");
        }
        return res;
    }

    private void p(String msg) {
        System.out.println("MaskParser: " + msg);
        this.txtCmd.append("\n" + msg);
    }

    private void addError(String err) {
        errors.add(err);
        this.txtCmd.append("\n" + err);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        bclear = new javax.swing.JButton();
        bparse = new javax.swing.JButton();
        hint = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtCmd = new javax.swing.JTextArea();

        setLayout(new java.awt.BorderLayout());

        jToolBar1.setRollover(true);

        bclear.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/document-new-3.png"))); // NOI18N
        bclear.setText(org.openide.util.NbBundle.getMessage(MaskCommandPanel.class, "MaskCommandPanel.bclear.text")); // NOI18N
        bclear.setFocusable(false);
        bclear.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bclear.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bclear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bclearActionPerformed(evt);
            }
        });
        jToolBar1.add(bclear);

        bparse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/system-run-3.png"))); // NOI18N
        bparse.setText(org.openide.util.NbBundle.getMessage(MaskCommandPanel.class, "MaskCommandPanel.bparse.text")); // NOI18N
        bparse.setFocusable(false);
        bparse.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bparse.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bparse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bparseActionPerformed(evt);
            }
        });
        jToolBar1.add(bparse);

        hint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/torrentscout/explorer/help-hint.png"))); // NOI18N
        hint.setText(org.openide.util.NbBundle.getMessage(MaskCommandPanel.class, "MaskCommandPanel.hint.text")); // NOI18N
        hint.setToolTipText(org.openide.util.NbBundle.getMessage(MaskCommandPanel.class, "MaskCommandPanel.hint.toolTipText")); // NOI18N
        hint.setFocusable(false);
        hint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        hint.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        hint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hintActionPerformed(evt);
            }
        });
        jToolBar1.add(hint);

        add(jToolBar1, java.awt.BorderLayout.NORTH);

        txtCmd.setColumns(70);
        txtCmd.setRows(4);
        txtCmd.setText(org.openide.util.NbBundle.getMessage(MaskCommandPanel.class, "MaskCommandPanel.txtCmd.text")); // NOI18N
        txtCmd.setToolTipText(org.openide.util.NbBundle.getMessage(MaskCommandPanel.class, "MaskCommandPanel.txtCmd.toolTipText")); // NOI18N
        jScrollPane1.setViewportView(txtCmd);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void hintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hintActionPerformed

        doHintAction();     }//GEN-LAST:event_hintActionPerformed

    private void bparseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bparseActionPerformed
        this.doParseAction();
    }//GEN-LAST:event_bparseActionPerformed

    private void bclearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bclearActionPerformed
        this.txtCmd.setText("");
    }//GEN-LAST:event_bclearActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bclear;
    private javax.swing.JButton bparse;
    private javax.swing.JButton hint;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JTextArea txtCmd;
    // End of variables declaration//GEN-END:variables
}
