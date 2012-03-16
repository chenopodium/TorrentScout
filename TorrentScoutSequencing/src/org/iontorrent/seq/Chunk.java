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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class Chunk implements SequenceIF {

    	private SequenceIF seq;
 	private long start;
 	
 	private int chromo;

	public int getChromo() {
		return chromo;
	}
	public void setChromo(int chromo) {
		this.chromo = chromo;
	}
	private static final boolean DEBUG=false;
 	
    @Override
	public String getName() {
 		return seq.getName();
 	}
// 	public Chunk( StringBuffer seq,int chromo, long start2) {
//		 this.seq = new Sequence(seq);
//		 this.start = start2;
//	//	 this.setPosition(start);		
//		 this.chromo = chromo;
//		
//	}
// 	
 	public Chunk(SequenceIF seq, int chromo, long start) {
 		this.setSeq(seq);
 		this.start = start;
 		this.chromo = chromo;
 		
 	}
 	public boolean equals(Object o) {
 		if (o== null || (!(o instanceof Chunk))) return false;
 		Chunk ch = (Chunk)o;
 		if (ch.getStart()!=getStart()) return false;
 		if (ch.getLength() != getLength()) return false;
 		
 		int col = 0;
		String line1 = "";
		String line2 = "";
 		for (int i = 0; i < ch.getLength(); i++) {
 			char c = getBaseChar(i);
			char g = ch.getBaseChar(i);
			//if (show) {
				line1 = line1 + c;
				line2 = line2 + g;
				col++;
				if (col > 60) {
//					if (i-start < 100) {
//						System.out.println("v:"+line1+" "+i);
//					
//						System.out.println("v:"+line2+" "+i);
//						System.out.println("");
//					}
					col = 0;
					line1 = "";
					line2 = "";
				}
		//	}
			if (g == 'G' || g == 'A' || g == 'T' || g == 'C') {
				if (g != c) {
					//miss++;
					p("Chunks not the same @" + i + ":"
							+ c + "<>" + g);
					System.out.println("Lines so far are:");
					System.out.println("v:"+line1+" "+i);
					System.out.println("v:"+line2+" "+i);
					
					return false;
				}
			}
 		}
 		return true;
 	}
 	
 	private static void out(String s) {
		System.out.println("Read: "+s);
	}
// 	/** pos is the position in the GENOME */
// 	public void setBaseCharAtGenome(long pos, char c) {
// 		//if ( c != 'x') p("wr "+c+" @ "+pos);
//		getSeq().((int) (pos-start), c);
//	}
 	/** get base where pos is the GENOME pos */
	public char getBaseCharFromGenome(long pos) {
	//	p("Getting base "+(pos-start));
		if (pos - start > this.getLength()) {
			err("getBaseCharFromGenome, pos out of range:"+pos+", start="+start+", len="+this.getLength());
		}
		else if (pos-start <0) {
			err("pos "+pos+" - start "+ start +" < 0: "+(pos-start));
		}
		return getSeq().getBaseChar((int) (pos - start));
	}
	public boolean isBaseInGenome(long pos) {
		return getSeq().isBase((int) (pos - start));	
	}
 
	public static void main(String[] args) {
		

	}
	public String toShortString() {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < seq.getLength(); i++) {
			b = b.append(seq.getBasecharPositionCode(i));
		}
		return b.toString();
	}
    @Override
	public String toString() {
		return "Chunk from "+start+" to "+(start+getSeq().getLength())+", length: "+getSeq().getLength();
	}
	

	
	public long getStart() {
		return start;
	}
	public long getEnd() {
		return start + getSeq().getLength();
	
	}

//	public long getPosition() {
//		warn("use getstart");
//		return start;
//	}
	public void setStart(int start) {
		this.start = start;
	}
    @Override
	public int getLength() {
		return getSeq().getLength();
	}
	
    @Override
	public int find(SequenceIF s, int start, int end) {
		return getSeq().find(s, start, end);
	}
	
		
         @Override
	public ArrayList<Integer> findAll(SequenceIF s, int start, int end) {
		return getSeq().findAll(s, start, end);
	}
           @Override
	public ArrayList<Integer> findAll(SequenceIF s, int start, int end, boolean debug) {
		return getSeq().findAll(s, start, end, debug);
	}
	@Override
	public byte getBasecharPositionCode(int pos) {
		return getSeq().getBasecharPositionCode(pos);
	}
	/** get base where pos is the GENOME pos */
	public int getBaseByteFromGenome(long pos) {
	//	p("Getting base "+(pos-start));
		return getSeq().getBasecharPositionCode((int) (pos - start));
	}
	@Override
	public char getBaseChar(int pos) {
		return getSeq().getBaseChar(pos);
	}
	
	
	@Override
	public boolean isGap(int pos) {
		return getSeq().isGap(pos);
	}
	
	@Override
	public String toSequenceString() {
		return getSeq().toSequenceString();
	}
	public void setSeq(SequenceIF seq) {
		this.seq = seq;
	}
	public SequenceIF getSeq() {
		return seq;
	}
	@Override
	public boolean isBase(int pos) {
		return seq.isBase(pos);
	}
	
	public SequenceIF createNew() {
		
		return new Chunk(seq, chromo, start);
	}
	
//	
//	public void setBasesAt(int i, String str) {
//		for(int pos = i; pos < i+str.length(); pos++) {
//			this.setBaseCharAt(pos, str.charAt(pos));
//		}
//		
//	}
	
    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(Chunk.class.getName()).log(Level.SEVERE, msg, ex);
    }
    
    private void err(String msg) {
      //  this.msg = msg;
        Logger.getLogger(Chunk.class.getName()).log(Level.SEVERE, msg);
    }
    
    private static void warn(String msg) {
        Logger.getLogger(Chunk.class.getName()).log(Level.WARNING, msg);
    }
    
    private static void p(String msg) {
//  System.out.println("Chunk: " + msg);
        //Logger.getLogger( Chunk.class.getName()).log(Level.INFO, msg);
    }

    @Override
    public void insertCharAt(int i, char c) {
        seq.insertCharAt(i, c);
    }

    @Override
    public void removeBaseAt(int i) {
        seq.removeBaseAt(i);
    }

    @Override
    public SequenceIF complement() {
        return seq.complement();
    }
    
     @Override
    public SequenceIF reverse() {
          return seq.reverse();
     }
}
