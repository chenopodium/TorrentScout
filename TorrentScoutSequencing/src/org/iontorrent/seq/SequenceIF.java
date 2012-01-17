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
package org.iontorrent.seq;

import java.util.ArrayList;

/**
 *
 * @author Chantal Roth
 */
public interface SequenceIF {

    public static final int G = 0;
    public static final int A = 1;
    public static final int T = 2;
    public static final int C = 3;
    public static final int X = 4;
    public static final int GAP = 5;
    public static final int POSSIBLEGAP = 6;
    public static final int SPACE = 7;
    public static final char GAPCHAR = '_';
    public static final char POSSGAPCHAR = '?';
    public static final char SPACECHAR = '.';
    public static final int BASES[] = {G, A, T, C, X, GAP, POSSIBLEGAP, SPACE};
    public static final char BASECHARS[] = {'G', 'A', 'T', 'C', 'X', GAPCHAR, POSSGAPCHAR, SPACECHAR};

    public int getLength();

    public String getName();

    public boolean isGap(int pos);

    public boolean isBase(int pos);

    public char getBaseChar(int pos);

    public byte getBasecharPositionCode(int pos);

    public String toSequenceString();

    public void insertCharAt(int i, char c);

    public void removeBaseAt(int i);

    public ArrayList<Integer> findAll(SequenceIF seq, int start, int end);
    
    public ArrayList<Integer> findAll(SequenceIF seq, int start, int end, boolean debug);

    public int find(SequenceIF seq, int start, int end);
    
    public SequenceIF complement();
    
    public SequenceIF reverse();
    
    
}
