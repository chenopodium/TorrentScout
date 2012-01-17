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
package org.iontorrent.seq;

import java.util.Comparator;


public class SequenceLengthComparator implements Comparator {


	public int compare(Object o1, Object o2) {
		SequenceIF s1 = (SequenceIF) o1;
		SequenceIF s2 = (SequenceIF) o2;
	
		return (int) (s2.getLength() - s1.getLength());
	}
}