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
package com.iontorrent.genometochip;

import com.iontorrent.expmodel.ExperimentContext;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import org.iontorrent.seq.Read;
import org.iontorrent.seq.alignment.Alignment;

/**
 *
 * @author Chantal Roth
 */
public class ReadlTableModel extends DefaultTableModel {

    //   private static final int MAX_ROWS = 500;
    private static String[] HEADER;
    ArrayList<Read> reads;
    long genomepos;
    ExperimentContext exp;
    
    public ReadlTableModel(ExperimentContext exp, ArrayList<Read> reads, long genomepos) {
        super();
        this.exp = exp;
        this.reads = reads;
        this.genomepos = genomepos;

        createHeader();
        this.setColumnIdentifiers(HEADER);
        p("Got " + reads.size() + " reads");
    }

    private void createHeader() {
        //                      0        1       2       3       4              5                    6          7        8                  9
        HEADER = new String[]{"row", "col", "reverse", "seq len", "pos in read", "pos in alignment", "flow #", "base", "reference name", "alignment info"};

    }

    @Override
    public int getRowCount() {
        if (reads == null) {
            return 0;
        }
        return reads.size();

    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    //                      0        1       2       3       4              5                    6     7        8               9
    //  HEADER = new String[]{"row", "col", "reverse", "len", "pos in read", "pos in alignment", "flow #", "base", ref name,  "al info"};
    @Override
    public Class getColumnClass(int col) {
        // if (col  == 2) return Float.class;
        if (col < 2) {
            return Integer.class;
        }
        if (col == 2) {
            return Boolean.class;
        } else if (col == 3) {
            return String.class;
        } else if (col == 7) {
            return String.class;
        } else if (col == 8) {
            return String.class;
        } else if (col == 9) {
            return String.class;
        } else {
            return Integer.class;
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        Read r = reads.get(row);
        if (col == 0) {
            return r.getRow()+exp.getRowOffset();
        } else if (col == 1) {
            return r.getCol()+exp.getColOffset();
        } else if (col == 2) {
            return r.isReverse();
        } else if (col == 3) {
            return r.getLength();
        } else if (col == 4) {
            int basepos = r.getPosInRead(genomepos);
            return basepos;
        } else if (col == 5) {
            int basepos = r.getPosInRead(genomepos);
            int alpos = r.getAlign().getPosInAl(basepos);
            return alpos;
        } else if (col == 6) {
            int basepos = r.getPosInRead(genomepos);

            return r.findFlow(basepos);
        } else if (col == 7) {
            int basepos = r.getPosInRead(genomepos);
            return r.getBaseChar(basepos);

        } else if (col == 8) {
            return r.getReferenceName();

        } else if (col == 9) {
            Alignment al = r.getAlign();
            String s = "% ident=" + al.getIdentityPerc() + ", nr indels=" + al.getGaps() + ", mismatches=" + al.getMismatches();
            return s;
            //return r.toSequenceString();
        } else {
            return null;
        }

    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(ReadlTableModel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(ReadlTableModel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(ReadlTableModel.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
//  System.out.println("ReadlTableModel: " + msg);
        //Logger.getLogger( ReadlTableModel.class.getName()).log(Level.INFO, msg, ex);
    }

    public Read getRead(int row) {
        return reads.get(row);
    }
}
