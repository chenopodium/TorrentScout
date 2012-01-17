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
package com.iontorrent.torrentscout.explorer.edit;

import com.iontorrent.rawdataaccess.wells.BitMask;
import javax.swing.JOptionPane;

/**
 *
 * @author Chantal Roth 
 * chantal.roth@lifetech.com
 */
public class ShiftOp extends AbstractOperation {

    int dx;
    int dy;

    public ShiftOp(int dx, int dy) {
        super("Shift " + (dx == 0 && dy == 0 ? "(custom)" : dx + "/" + dy), "Shift the entire mask by a certain number of wells");
        this.dx = dx;
        this.dy = dy;
        nrargs = 1;
    }

    @Override
    public boolean execute(BitMask m1, BitMask unused, BitMask m3) {
        boolean res = false;
        if (dx == 0 && dy == 0) {
            String ans = JOptionPane.showInputDialog("Select shift in X direction: ");
            if (ans != null) {
                try {
                    dx = Integer.parseInt(ans);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Could not convert "+ans+" to integer");
                }

            }
            ans = JOptionPane.showInputDialog("Select shift in Y direction: ");
            if (ans != null) {
                try {
                    dy = Integer.parseInt(ans);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Could not convert "+ans+" to integer");
                }

            }
            res = m3.shift(m1, dx, dy);
            dx = 0;
            dy = 0;
        }
        else res = m3.shift(m1, dx, dy);
        return res;
    }
}
