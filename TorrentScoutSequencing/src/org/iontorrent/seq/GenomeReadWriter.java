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

import com.iontorrent.utils.io.FileTools;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class GenomeReadWriter {

    protected static int TEST_LINE_SIZE = 80;
    public static final int CHUNK_SIZE = 16000;
    public static final int BUCKET_SIZE = CHUNK_SIZE / 4;
    protected File fastaFileWithWhitespace;
    protected File seqFileWithoutWhitespace;
    private long fileSize;
    protected RandomAccessFile readBuffer;
    protected Chunk curChunk;
    protected static HashMap<String, GenomeReadWriter> writermap = new HashMap<String, GenomeReadWriter>();
    // cache chunks?
    protected HashMap<Long, byte[]> cache = new HashMap<Long, byte[]>();
    private boolean strip;

    public static GenomeReadWriter getGenomeReadWriter(File fastaFile) {
        return getGenomeReadWriter(fastaFile, true);
    }

    public static GenomeReadWriter getGenomeReadWriter(File fastaFile, boolean strip) {
        //	p("GenomeRW:" + fastaFile);

        GenomeReadWriter writer = writermap.get(fastaFile.toString());
        if (writer == null) {
            writer = new GenomeReadWriter(fastaFile, strip);
            writermap.put(fastaFile.toString(), writer);
        }
        //	else p("Already found writer for "+fastaFile.toString());
        return writer;
    }

    // cache chunks... if possible? or cache a larger super chunk...?
    protected GenomeReadWriter(File fastaFile, boolean strip) {
        this.fastaFileWithWhitespace = fastaFile;
        this.strip = strip;
        init();
    }

    public long getFileSize() {
        if (this.fileSize > 0) {
            return fileSize;
        }
        if (seqFileWithoutWhitespace == null) {

            //p("Got no file with out whitespace, calling init");
            if (seqFileWithoutWhitespace == null) {
                seqFileWithoutWhitespace = new File(fastaFileWithWhitespace.toString() + ".stripped");
            }
            init();
        }
        fileSize = this.seqFileWithoutWhitespace.length();
        p("Size of " + seqFileWithoutWhitespace + ":" + fileSize);
        if (fileSize == 0) {
            p("Size of file: " + seqFileWithoutWhitespace + " is :" + seqFileWithoutWhitespace.length() + ", source fasta file is: " + this.fastaFileWithWhitespace);
            extractWhiteSpaceAndConvertToBinary(fastaFileWithWhitespace, seqFileWithoutWhitespace);
            fileSize = this.seqFileWithoutWhitespace.length();
            if (fileSize == 0) {
                err("Size of file: " + seqFileWithoutWhitespace + " is :" + seqFileWithoutWhitespace.length() + ", source fasta file is: " + this.fastaFileWithWhitespace);
            }
        }
        return fileSize;
    }

    private static boolean isBinary(File f) {
        return f.getAbsolutePath().indexOf(".stripped") > 0 || f.getAbsolutePath().indexOf(".bin") > 0;
    }

    protected void checkFileName() {
        p("checkFileName");
        String fn = fastaFileWithWhitespace.toString();
        if (!fn.endsWith(".fasta") && !fn.endsWith("bin") && !fn.endsWith(".stripped")) {
            err("Illegal file name for fasta file:" + fastaFileWithWhitespace);
        }
        if (fn.endsWith(".stripped")) {
            seqFileWithoutWhitespace = fastaFileWithWhitespace;
        }
        if (isBinary(fastaFileWithWhitespace)) {
            //	p("specified file is already the stripped or binary file");			
            if (!fastaFileWithWhitespace.exists() && !fastaFileWithWhitespace.toString().endsWith(".bin")) {
                p("But stripped file does not exist: " + fastaFileWithWhitespace.toString());
            } else {
                seqFileWithoutWhitespace = fastaFileWithWhitespace;
            }
        } else {
            p("Source file is fasta file and is NOT binary: " + fastaFileWithWhitespace);

            return;
        }

    }

    public static void extractWhiteSpaceAndConvertToBinary(File fastaFileWithWhitespace) {
        File bin = new File(fastaFileWithWhitespace.getAbsolutePath() + ".stripped");
        extractWhiteSpaceAndConvertToBinary(fastaFileWithWhitespace, bin);
    }

    public static void extractWhiteSpaceAndConvertToBinary(File fastaFileWithWhitespace, File binaryFile) {
        String fasta = fastaFileWithWhitespace.toString();
        if (fasta.endsWith(".stripped")) {
            fastaFileWithWhitespace = new File(fasta.substring(0, fasta.length() - 9));
        }
        p("removing whitespace from " + fastaFileWithWhitespace + ", converting to " + binaryFile);
        if (fastaFileWithWhitespace.equals(binaryFile)) {
            err("Files are the same:" + fastaFileWithWhitespace + "/" + binaryFile);
        }
        if (isBinary(fastaFileWithWhitespace)) {
            err("File is binary:" + fastaFileWithWhitespace);
        }
        try {
            binaryFile.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            BufferedWriter writ = new BufferedWriter(new FileWriter(binaryFile));
            BufferedReader read = new BufferedReader(new FileReader(fastaFileWithWhitespace));
            boolean done = false;
            int lines = 0;
            DNASequence S = new DNASequence();
            while (!done) {
                String line = read.readLine();
                if (line == null) {
                    done = true;
                } else {
                    line = line.trim();
                    if (!line.startsWith(">")) {
                        // also convert to BYTES
                        for (int i = 0; i < line.length(); i++) {
                            char c = line.charAt(i);
                            if (Character.isWhitespace(c) || Character.isDigit(c)) {
                                //ignore
                            } else {
                                byte b = S.toBasecharPositionCode(c);
                                if (b > S.BASECHARS.length) {
                                    err("bytecode too large:" + b + ", basechars are: " + S.BASECHARS);
                                }
                                //		System.out.println(b);
                                writ.write(b);
                            }
                        }
                        lines++;

                        if (lines % 100000 == 0) {
                            writ.flush();
                            p("wrote line " + lines + ": " + line);
                        }
                    } else {
                        p("Not writing fasta header: " + line);
                    }
                }
            }
            writ.flush();
            writ.close();
            read.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void converToFastaFile(File fastaFileWithWhitespace, File binaryFile) {
        p("Converting " + binaryFile + " to fasta file " + fastaFileWithWhitespace);
        if (fastaFileWithWhitespace.equals(binaryFile)) {
            err("Files are the same:" + fastaFileWithWhitespace + "/" + binaryFile);
        }
        if (isBinary(fastaFileWithWhitespace)) {
            err("File is binary:" + fastaFileWithWhitespace);
        }
        try {
            fastaFileWithWhitespace.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        GenomeReadWriter reader = new GenomeReadWriter(binaryFile, false);
        try {
            BufferedWriter writ = new BufferedWriter(new FileWriter(fastaFileWithWhitespace));
            boolean done = false;
            int lines = 0;
            writ.write(">" + binaryFile + " conversion to Fasta\n");
            int pos = 0;
            while (pos < reader.getFileSize()) {
                int delta = 70;
                if (pos + delta > reader.getFileSize()) {
                    delta = (int) (reader.getFileSize() - pos);
                }
                Chunk ch = reader.getChunk(pos, delta, true);
                lines++;
                pos += ch.getLength();
                writ.write(ch.toSequenceString() + "\n");
                if (lines % 1000 == 0) {
                    writ.flush();
                    p("wrote line " + lines + ": " + ch);
                }
            }
            writ.flush();
            writ.close();
            reader.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void insertTemplateIntoFastaFileAndConvert(String template, long pos, File fasta, File binaryFile) {
        p("Inserting template " + template + " into file " + fasta + " at pos " + pos + ", and will create file " + binaryFile);

        try {
            binaryFile.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            BufferedWriter writ = new BufferedWriter(new FileWriter(binaryFile));
            BufferedReader read = new BufferedReader(new FileReader(fasta));
            boolean done = false;
            int lines = 0;
            int curpos = 0;
            DNASequence S = new DNASequence();
            while (!done) {
                String line = read.readLine();
                if (line == null) {
                    done = true;
                } else {
                    line = line.trim();
                    if (!line.startsWith(">")) {
                        // also convert to BYTES
                        for (int i = 0; i < line.length(); i++) {
                            char c = line.charAt(i);
                            if (Character.isWhitespace(c) || Character.isDigit(c)) {
                                //ignore
                            } else {
                                byte b = S.toBasecharPositionCode(c);
                                if (b > S.BASECHARS.length) {
                                    err("bytecode too large:" + b + ", basechars are: " + S.BASECHARS);
                                }
                                //		System.out.println(b);
                                if (pos == curpos) {
                                    p("Now inserting template at " + curpos);
                                    for (int j = 0; j < template.length(); j++) {
                                        byte t = S.toBasecharPositionCode(template.charAt(j));
                                        if (t > S.BASECHARS.length) {
                                            err("bytecode too large:" + t + " from template " + template + ", basechars are: " + S.BASECHARS);
                                        }
                                        writ.write(t);
                                    }
                                }
                                writ.write(b);
                                curpos++;
                            }
                        }
                        lines++;

                        if (lines % 100000 == 0) {
                            writ.flush();
                            p("wrote line " + lines + ": " + line);
                        }
                    } else {
                        p("Not writing fasta header: " + line);
                    }
                }
            }
            writ.flush();
            writ.close();
            read.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void addStartToCircularGenome(File binaryGenome, File withStart, int readlength) {
        p("File " + withStart + " does not exist yet, will first copy genome file.");

        try {
            if (withStart.exists()) {
                withStart.delete();
            }
            withStart.createNewFile();
            InputStream in = new FileInputStream(binaryGenome);

            //For Overwrite the file.
            OutputStream out = new FileOutputStream(withStart);
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            p("Will now append genome first " + readlength + " bases to end of file " + withStart);
            RandomAccessFile read = new RandomAccessFile(binaryGenome, "r");
            p("starting with pos " + (read.length() - 1) + " in file " + binaryGenome);
            p("fp is:" + read.getFilePointer());

            for (long pos = 0; pos < readlength; pos++) {
                //  p("seeking:"+pos);
                read.seek(pos);
                byte b = read.readByte();
                // get complement!
                out.write(b);
                if (pos % 1000 == 0) {
                    out.flush();
                    p("wrote start of genome file for pos " + pos);
                }
            }
            out.flush();
            out.close();
            read.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void convertBinaryFileToReadableFile(File binaryFile, File fastaFileWithWhitespace) {
        p("Will convert BINARY file " + binaryFile + " to readable fasta file " + fastaFileWithWhitespace);
        try {
            p("Creating file " + fastaFileWithWhitespace);
            fastaFileWithWhitespace.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            BufferedWriter writ = new BufferedWriter(new FileWriter(fastaFileWithWhitespace));
            RandomAccessFile read = new RandomAccessFile(binaryFile, "r");
            boolean done = false;
            long lines = 0;
            int col = -1;
            DNASequence S = new DNASequence();
            byte[] buf = new byte[1024];
            while (!done) {
                int nrread = read.read(buf);
                if (nrread < 1) {
                    done = true;
                } else {
                    for (int i = 0; i < nrread; i++) {
                        byte b = buf[i];
                        col++;
                        if (col > 80) {
                            col = 0;
                            writ.write('\n');
                            lines++;
                        }
                        char c = S.byteToChar(b);
                        writ.write(c);
                        if (lines % 50000 == 0 && col == 0 && lines > 0) {
                            writ.flush();
                            p("processed " + lines + " lines");
                            //p("lines "+lines+": "+buf.toString());
                        }
                    }
                }
            }
            writ.flush();
            writ.close();
            read.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void init() {
        checkFileName();
        if (seqFileWithoutWhitespace == null) {
            p("Binary file not specified, won't do anything");
            return;
        }
        if (!isBinary(seqFileWithoutWhitespace)) {
            err("Sequence file w/o whitespace should end with .stripped or .bin or .comp:" + seqFileWithoutWhitespace + ", strip is:" + strip);
        }
        setFileSize(seqFileWithoutWhitespace.length());
        //p("init: seq file wo whitespace:"+seqFileWithoutWhitespace+", size: "+this.getFileSize());
        // Exception x = new Exception();
        // x.printStackTrace();

        openToRead();

    }

    private void openToRead() {
        if (seqFileWithoutWhitespace == null || !seqFileWithoutWhitespace.exists()) {
            err("File "+seqFileWithoutWhitespace+" not found");
            return;
        }
        try {
            readBuffer = new RandomAccessFile(seqFileWithoutWhitespace, "rw");
            try {
                this.fileSize = readBuffer.length();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public Chunk getChunkForPos(long pos) {
        return getChunkForPos(pos, CHUNK_SIZE);
    }

    public Chunk getChunkForPos(long pos, long end) {
        //	p(this.getClass().getName()+":Getting chunk from "+pos+"-"+end+", length:"+(end-pos)+":"+getClass().getName());
        if (pos < 0) {
            pos = 0;
        }
        if (end <= pos) {
            end = pos + CHUNK_SIZE;
        }
        Chunk ch = null;

        int size = (int) (end - pos);
        if (curChunk != null) {
            //p("got genome, checking of genome is still in range:"+pos+"-"+pos+
            // r.getLength()+", genome:"+lastChunk.getStart()+"-"+lastChunk.
            // getStart()+lastChunk.getLength());
            int delta = (int) (pos - curChunk.getStart());
            if (delta >= 0 && delta < this.CHUNK_SIZE * 0.8
                    && curChunk.getStart() + curChunk.getLength() >= end) {
                //	 p("+++Reusing old chunk: "+curChunk+" for pos "+pos);
                ch = curChunk;
            }
            // else p("NOT reusing chunk "+curChunk+" for pos "+pos+"-"+end);
        }
        if (ch == null) {
            ch = getChunk(pos, (int) (end - pos));
        }
        curChunk = ch;
        if (ch == null) {
            //	p("Got no chunk, probably end of file");
            return null;
        }

        //p("Chunk "+ch.getClass().getName()+" size is:"+ch.getLength());
        if (size != ch.getLength()) {
            //	p("requested size not the same:"+size +"<>"+ch.getLength()+", probably end of file");
        }
        return ch;
    }

    private void createEmptyFile(long size) {
        createEmptyFile(size, null);

    }

    protected boolean stripSourceFile() {
        return strip;
    }

    public void createEmptyFile(long size, String initstring) {
        createEmptyFile(size, initstring, new DNASequence());
    }

    public void createEmptyFile(long size, String initstring, DNASequence S) {
        if (initstring == null) {
            initstring = "5";
            warn("Initstring was null, using " + initstring);
        }
        //err("Initstring is:"+initstring+", "+(byte)initstring.charAt(0));
        this.setFileSize(size);
        //Sequence S = new Sequence();
        try {
            if (seqFileWithoutWhitespace == null) {
                this.checkFileName();
                err("seqFileWithoutWhitespace is null. Input file was:" + this.fastaFileWithWhitespace);
            }
            seqFileWithoutWhitespace.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            DataOutputStream writ = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(seqFileWithoutWhitespace)));

            for (int i = 1; i <= size; i++) {
                byte b = S.toBasecharPositionCode(initstring.charAt((i - 1) % initstring.length()));
                writ.writeByte(b);
                //		if (i  < 100) p("Creating empty file, wrote byte "+b+", ")
                if (i % 100000 == 0) {
                    writ.flush();
                }
            }
            writ.flush();
            writ.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void writeChunk(Chunk ch) {
        if (ch == null) {
            warn("writeChunk: Chunk is null, can");
        } else {
            long start = ch.getStart();
            //	p("Writing chunk "+ch.toShortString()+" to file "+this.getFastaFile());
            write(start, ch);
        }
    }

    public void writeChunk(Chunk ch, long start, long end) {

        write(start, ch, end);
    }

    public void close() {
        try {
            readBuffer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void write(long start, SequenceIF seq) {
        write(start, seq, 0);
    }

    protected void write(long start, SequenceIF seq, long end) {
        curChunk = null;
        if (cache != null) {
            cache.clear();
        }
        seek(start);
        // get current character
        int i = 0;
        long pos = start;
        if (end <= start) {
            end = seq.getLength();
        } else {
            end = end - start;
        }
        // p("   Writing "+seq+" chars @ position "+start+"-"+(start+end));

        while (i < end && i < seq.getLength()) {
            try {

                byte b = seq.getBasecharPositionCode(i++);

                // if (c == 'x') c = (""+(pos%8+1)).charAt(0);
                pos++;
                if (b > 100) {
                    err("byte representation of char too large:" + b);
                }
                readBuffer.writeByte(b);
                // if (i < 100) System.out.print(c);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // p("Wrote "+i+" bases");
    }

    public Chunk getNextChunk() {

        if (curChunk == null) {
            return getChunk(0);
        } else {
            return getChunk(curChunk.getStart() + curChunk.getLength());
        }
    }

    public Chunk getChunk(long start) {
        //	p("Getting chumk of size "+CHUNK_SIZE);
        return getChunk(start, CHUNK_SIZE, false);
    }

    public Chunk getChunk(long start, int size) {
        return getChunk(start, size, false);
    }

    public Chunk getChunk(long start, int size, boolean exactSize) {
        // p("Loading chunk at "+start);
        byte[] buf = fetch(start, start + size, exactSize);
        if (buf == null) {
            return null;
        }
        curChunk = new Chunk(new DNASequence(buf), 1, start);
        // p("Got chunk: "+ch);
        return curChunk;
    }

    public DNASequence fetchSequence(long start, long end) {
        return fetchSequence(start, end, false);
    }

    public DNASequence fetchSequence(long start, long end, boolean exactSize) {
        byte[] buf = fetch(start, end, exactSize);
        if (buf == null) {
            return null;
        }
        return new DNASequence(buf);
    }

    public byte[] fetch(long start, long end) {
        return fetch(start, end, false);
    }

    public byte[] fetch(long start, long end, boolean exactSize) {
        if (!this.seqFileWithoutWhitespace.exists()) {
            p("File does not exist: "+this.seqFileWithoutWhitespace);
            return null;
        }
        if (end < start) {
            err("fetch: End " + end + " < start " + start);
        }

        if (fileSize == 0) {
            fileSize = getFileSize();
        }
        //	p(this.getClass().getName()+":Fetching from "+start+"-"+end+", size:"+(end-start)+", filesize "+this.getFileName()+":"+fileSize);
        int len = Math.max(1, (int) (Math.min(end, fileSize) - start));
        if (len <= 0 || start >= fileSize) {
            p("End of file reached: " + len + " start=" + start + " end=" + end
                    + ", fileSize=" + fileSize);
            return null;
        }
        if (len > Integer.MAX_VALUE) {
            warn("Sized of buffer too large:" + len + ", cannot read that many bytes. Wil lreturn null");
            return null;
        }
        int size = (int) (end - start);
        if (len < size) {
            //	p("Lenght was shortened to:"+len+" due to file size");
        }
        byte[] buf = cache.get(new Long(start));
        if (buf != null) {
            //	p("Fetch: found chunk in cache for pos "+start);
            if (buf.length > len) {
                if (exactSize) {
                    //p("Buffered genome larger than requested one, must shorten it");
                    buf = Arrays.copyOfRange(buf, 0, len);
                }
                return buf;
            } else if (buf.length < len) {
                //	p("Buffered genome SMALLER than requested one. NOT Ok ->reloading");
                buf = null;
            } else {
                return buf;
            }
        }

        seek(start);
        if (readBuffer == null) return null;
        int read = -1;
        // thre is NO WHITESPACE IN TEH FILE ANYMORE
        // AND THE FILE CONTAINS ONLY INTEGERS!
        byte[] rbuf = new byte[len];
        try {
            read = readBuffer.read(rbuf);
        } catch (IOException e) {
            e.printStackTrace();
            
        }
        if (read < 0) {
            warn("Could not read from file at position " + start + " and len " + len + ", read<0."
                    + "\nFile length: " + fileSize + ", file is: " + this.seqFileWithoutWhitespace);
            buf = new byte[0];
            return buf;
        }
        buf = new byte[read];
        System.arraycopy(rbuf, 0, buf, 0, read);
//		// VERIFY
//		for (int i = 0; i < buf.length; i++) {
//			if (buf[i] > )
//		}
        // p("done reading");
        if (cache.size() > 10000) {
            cache.clear();
        }
        cache.put(new Long(start), buf);
        return buf;
    }

    protected void seek(long start) {
        if (readBuffer == null) {
            openToRead();
        }
        if (readBuffer == null) return;
        try {
            readBuffer.seek(start);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
//	

    public static void test() {
        String testfile = "text.txt";
        int size = TEST_LINE_SIZE * 10;
        int start = TEST_LINE_SIZE;
        String str = "GATCGATC";
        GenomeReadWriter gr = new GenomeReadWriter(new File(testfile), false);

        gr.readWriteTest000(start, size, str);
        gr.readWriteTest001(start, size, str);
        gr.readWriteTest00(start, size, str);
        gr.readWriteTesta(start, size, str);

        gr.readWriteTest3(start, size, str);
        gr.readWriteTest4(start, size, str);
    }

    private void readWriteTest000(int start, int size, String str) {
        createEmptyFile(size);
        String f1 = FileTools.getFileAsString(seqFileWithoutWhitespace.getAbsolutePath());
        showFile();
        Chunk ch1 = getChunk(0, TEST_LINE_SIZE);
        p("Chunk w/o bases:\n" + ch1);
        writeChunk(ch1);

        Chunk ch2 = getChunk(start, TEST_LINE_SIZE);
        p("Chunk after write:\n" + ch2);
        boolean ok = ch1.equals(ch2);
        if (!ok) {
            err("SEQUENCES NOT THE SAME:" + ch1 + "/" + ch2);
        }

        String f2 = FileTools.getFileAsString(seqFileWithoutWhitespace.getAbsolutePath());
        if (!f1.equals(f2)) {
            err("FILE not the same after read/write");
        }
    }

    private void readWriteTest001(int start, int size, String str) {
        createEmptyFile(size, "GATCGATC");
        String f1 = FileTools.getFileAsString(seqFileWithoutWhitespace.getAbsolutePath());
        showFile();
        Chunk ch1 = getChunk(0, TEST_LINE_SIZE + 1);
        p("Chunk w/o bases:\n" + ch1);
        writeChunk(ch1);

        Chunk ch2 = getChunk(0, TEST_LINE_SIZE + 1);
        p("Chunk after write:\n" + ch2);
        boolean ok = ch1.equals(ch2);
        if (!ok) {
            err("SEQUENCES NOT THE SAME:" + ch1 + "/" + ch2);
        }

        String f2 = FileTools.getFileAsString(seqFileWithoutWhitespace.getAbsolutePath());
        if (!f1.equals(f2)) {
            err("FILE not the same after read/write");
        }
    }

    private void readWriteTest00(int start, int size, String str) {
        createEmptyFile(size, "GATCGATC");
        String f1 = FileTools.getFileAsString(seqFileWithoutWhitespace.getAbsolutePath());
        showFile();
        Chunk ch1 = getChunk(start, TEST_LINE_SIZE);
        p("Chunk w/o bases:\n" + ch1);
        writeChunk(ch1);

        Chunk ch2 = getChunk(start, TEST_LINE_SIZE);
        p("Chunk after write:\n" + ch2);
        boolean ok = ch1.equals(ch2);
        if (!ok) {
            err("SEQUENCES NOT THE SAME:" + ch1 + "/" + ch2);
        }

        String f2 = FileTools.getFileAsString(seqFileWithoutWhitespace.getAbsolutePath());
        if (!f1.equals(f2)) {
            err("FILE not the same after read/write");
        }
    }

    private void readWriteTest0(int start, int size, String str) {
        createEmptyFile(size, "GATCGATC");
        String f1 = FileTools.getFileAsString(seqFileWithoutWhitespace.getAbsolutePath());
        showFile();
        Chunk ch1 = getChunk(start - 1, TEST_LINE_SIZE);
        p("Chunk w/o bases:\n" + ch1);
        writeChunk(ch1);

        Chunk ch2 = getChunk(start - 1, TEST_LINE_SIZE);
        p("Chunk after write:\n" + ch2);
        boolean ok = ch1.equals(ch2);
        if (!ok) {
            err("SEQUENCES NOT THE SAME:" + ch1 + "/" + ch2);
        }

        String f2 = FileTools.getFileAsString(seqFileWithoutWhitespace.getAbsolutePath());
        if (!f1.equals(f2)) {
            err("FILE not the same after read/write");
        }
    }

    private void readWriteTest3(int start, int size, String str) {
        createEmptyFile(size, "GATCGATC");
        start = TEST_LINE_SIZE * 2 + 1;
        String f1 = FileTools.getFileAsString(seqFileWithoutWhitespace.getAbsolutePath());
        // showFile();
        Chunk ch1 = getChunk(start, TEST_LINE_SIZE);
        p("Read:\n" + ch1.toSequenceString());
        writeChunk(ch1);

        Chunk ch2 = getChunk(start, TEST_LINE_SIZE);
        p("write:\n" + ch2.toSequenceString());
        boolean ok = ch1.equals(ch2);
        if (!ok) {
            err("SEQUENCES NOT THE SAME:" + ch1 + "/" + ch2);
        }

        String f2 = FileTools.getFileAsString(seqFileWithoutWhitespace.getAbsolutePath());
        if (!f1.equals(f2)) {
            err("FILE not the same after read/write");
        }
    }

    private void readWriteTest4(int start, int size, String str) {
        createEmptyFile(size, "GATCGATC");
        start = TEST_LINE_SIZE / 2;
        String f1 = FileTools.getFileAsString(seqFileWithoutWhitespace.getAbsolutePath());
        // showFile();
        Chunk ch1 = getChunk(start, TEST_LINE_SIZE * 2);
        p("Read:\n" + ch1.toSequenceString());
        writeChunk(ch1);

        Chunk ch2 = getChunk(start, TEST_LINE_SIZE * 2);
        p("write:\n" + ch2.toSequenceString());
        boolean ok = ch1.equals(ch2);
        if (!ok) {
            err("SEQUENCES NOT THE SAME:" + ch1 + "/" + ch2);
        }

        String f2 = FileTools.getFileAsString(seqFileWithoutWhitespace.getAbsolutePath());
        if (!f1.equals(f2)) {
            err("FILE not the same after read/write");
        }
    }

    private void readWriteTesta(int start, int size, String str) {
        createEmptyFile(size, "GATCGATC");
        String f1 = FileTools.getFileAsString(seqFileWithoutWhitespace.getAbsolutePath());
        showFile();
        Chunk ch1 = getChunk(start, 100);
        p("Chunk w/o bases:\n" + ch1.toSequenceString());
        writeChunk(ch1);

        Chunk ch2 = getChunk(start, 100);
        p("Chunk after write:\n" + ch2.toSequenceString());
        boolean ok = ch1.equals(ch2);
        if (!ok) {
            err("SEQUENCES NOT THE SAME:" + ch1 + "/" + ch2);
        }

        String f2 = FileTools.getFileAsString(seqFileWithoutWhitespace.getAbsolutePath());
        if (!f1.equals(f2)) {
            err("FILE not the same after read/write");
        }
    }

    public void showFile() {
        p("\n" + FileTools.getFileAsString(seqFileWithoutWhitespace.getAbsolutePath()));
    }

    protected void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        if (seqFileWithoutWhitespace == null) {
            this.checkFileName();
            err("seqFileWithoutWhitespace is null. Input file was:" + this.fastaFileWithWhitespace);
        }
        return seqFileWithoutWhitespace.toString();
    }

    public String getFileWOWhiteSpace() {
        if (seqFileWithoutWhitespace == null) {
            this.checkFileName();
            err("seqFileWithoutWhitespace is null. Input file was:" + this.fastaFileWithWhitespace);
        }
        return seqFileWithoutWhitespace.toString();
    }

    public String getFastaFile() {
        return this.fastaFileWithWhitespace.toString();
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(GenomeReadWriter.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        //   this.msg = msg;
        Logger.getLogger(GenomeReadWriter.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(GenomeReadWriter.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
//  System.out.println("GenomeReadWriter: " + msg);
        //Logger.getLogger( GenomeReadWriter.class.getName()).log(Level.INFO, msg);
    }
}
