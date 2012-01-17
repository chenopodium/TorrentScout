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
package org.iontorrent.seq.sam;

import com.iontorrent.utils.io.FileUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import net.sf.samtools.BAMIndex;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileReader.ValidationStringency;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;
import net.sf.samtools.util.CloseableIterator;
import net.sf.samtools.util.SequenceUtil;
import org.iontorrent.seq.Coord;
import org.iontorrent.seq.DNASequence;
import org.iontorrent.seq.Read;
import org.iontorrent.seq.alignment.Alignment;
import org.iontorrent.seq.alignment.Cell;
import org.iontorrent.seq.indexing.LocToReadIndexFinder;
import org.iontorrent.seq.indexing.ReadPos;
import org.iontorrent.seq.indexing.WellToLocIndexFinder;
import org.openide.util.Exceptions;

/**
 *
 * @author Chantal Roth
 */
public class SamUtils {

    //  File samfile;
    File bamfile;
    //  File samorbamfile;
    // File welltosamindexfile;
    //   WellToSamIndex welltosamindex;
    String errmsg;
    LocToReadIndexFinder genometoreadindexer = null;
    WellToLocIndexFinder welltolocdindexer = null;
    String cache_dir;
    String result_dir;
    String plugin_dir;

    public SamUtils(File asamfile, File abamfile,  String cache_dir, String result_dir, String plugin_dir) {
        // this.samfile = asamfile;
        this.bamfile = abamfile;
        this.result_dir = result_dir;
        this.cache_dir = cache_dir;
        this.plugin_dir = plugin_dir;
        if (bamfile == null) {
            err("Bamfile cannot be null");
        }
//        if (bamfile.exists()) {
//            samorbamfile = bamfile;
//        }
        //this.welltosamindexfile = indexfile;
        int len = bamfile.getName().length();
        String name = bamfile.getName().toLowerCase();
        int hashcode = len*10+name.charAt(len/2)+name.charAt(len/4);
        genometoreadindexer = new LocToReadIndexFinder(result_dir,hashcode);
         if (!genometoreadindexer.hasIndex()) {
            genometoreadindexer = new LocToReadIndexFinder(plugin_dir,hashcode);
        }
        if (!genometoreadindexer.hasIndex()) {
            genometoreadindexer = new LocToReadIndexFinder(cache_dir, hashcode);
        }
        welltolocdindexer = new WellToLocIndexFinder(result_dir, hashcode);
        if (!welltolocdindexer.hasIndex()) {
            welltolocdindexer = new WellToLocIndexFinder(plugin_dir, hashcode);
        }
        if (!welltolocdindexer.hasIndex()) {
            welltolocdindexer = new WellToLocIndexFinder(cache_dir, hashcode);
        }
        if (!welltolocdindexer.hasIndex()) {
            p("welltolocindexer.hasIndex is false: Got no well to loc index at all. Checked the dirs:"
                    + "\nplugin dir: "+plugin_dir+"\nresults dir: "+result_dir+"\ncache dir: "+cache_dir);
        }

    }

    private File findFileSomewhere(String filename) {
        File f = new File(this.result_dir + filename);
        if (f.exists()) {
            return f;
        }

        f = new File(this.plugin_dir + filename);
        if (f.exists()) {
            return f;
        }

        f = new File(this.cache_dir + filename);
        if (f.exists()) {
            return f;
        }

        if (FileUtils.canWrite(result_dir)) {
            f = new File(result_dir + filename);
        } else if (FileUtils.canWrite(plugin_dir)) {
            f = new File(plugin_dir + filename);
        } else {
            f = new File(this.cache_dir + filename);
        }
        p("findFileSomewhere:" + filename + "->" + f);
        p("cache would be: " + cache_dir);
        return f;
    }
//    public void setWellToSamIndexFile(File f) {
//        this.welltosamindexfile = f;
//    }
    public boolean hasGenomeToReadIndex() {
        return genometoreadindexer.hasIndex();
    }

    public File getGenomeToreadIndexFile() {
        return genometoreadindexer.getIndexFile();
    }

    public boolean hasWellToLocIndex() {
        return welltolocdindexer.hasIndex();
    }

    public File getWellToLocIndexFile() {
        return welltolocdindexer.getIndexFile();
    }

    public ArrayList<ReadPos> findReadsByGenomePos(long genomepos) {
        if (!genometoreadindexer.hasIndex()) {
            createReadLocationsIndexFile();
        }
        ArrayList<ReadPos> res = genometoreadindexer.findReads(genomepos);

        return res;
    }

    public long findReadpossByWell(String readname) {
        if (!welltolocdindexer.hasIndex()) {
            createReadLocationsIndexFile();
        }
        ArrayList<ReadPos> res = welltolocdindexer.findReads(readname);
        if (res == null || res.size() < 1) {
            return -1;
        } else {
            return res.get(0).pos;
        }

    }

    public void createReadLocationsIndexFile() {
        File readlocfile = new File(genometoreadindexer.getReadLocationsFileName());
        if (readlocfile.exists()) {
            p("Readlocfile " + readlocfile + "  already exists, won't overwrite it");
        } else {
          //  SAMFileWriterFactory fact = new SAMFileWriterFactory();
             int count = 0;
             p("Creating readlocfile " + readlocfile + "  from BAM " + bamfile);
            try {
                //ValidationStringency.DEFAULT_STRINGENCY = ValidationStringency.SILENT;
                final SAMFileReader inputSam = new SAMFileReader(bamfile);
                inputSam.setValidationStringency(ValidationStringency.SILENT);
                
                PrintWriter out = null;
                try {
                    out = new PrintWriter(new FileWriter(readlocfile));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (out == null) {
                    err("Could not create index file " + readlocfile);
                    return;
                }
               
                for (final SAMRecord rec : inputSam) {
                    ReadPos rp = new ReadPos();
                    
                    rp.readname = rec.getReadName();
                    int start = rec.getAlignmentStart();
                    int end = rec.getAlignmentEnd();
                    if (start > 0 || end > 0 && rp.readname!= null) {
                        rp.pos = Math.min(start, end);
                        rp.endpos = Math.max(start, end);
                        out.println(rp.pos + "\t" + rp.toString());
                        count++;
                    }
                }
                out.close();
                inputSam.close();
            }
            catch (Exception e) {
                err("createReadLocationsIndexFile: "+e.getMessage());
            }
            if (count ==0) {
                warn("Found NO records in BAM file! Won't do index");
                return;
                       
            }
        }

        genometoreadindexer.createLocToReadIndex();
        welltolocdindexer.createWellToLocIndex();
    }

    public static void convertBamToSam(File bam, File sam) {
        final SAMFileReader input = new SAMFileReader(bam);

        SAMFileWriterFactory fact = new SAMFileWriterFactory();
        fact.setCreateIndex(true);
        final SAMFileWriter output = fact.makeSAMWriter(input.getFileHeader(),
                true, sam);

        //outputSam.getFileHeader().setSortOrder(SortOrder.S);
        for (final SAMRecord samRecord : input) {
            // Convert read name to upper case.
            samRecord.setReadName(samRecord.getReadName().toUpperCase());
            output.addAlignment(samRecord);

        }

        output.close();
        input.close();
    }

    public static void convertSamToBam(File bam, File sam) {
        final SAMFileReader input = new SAMFileReader(sam);

        SAMFileWriterFactory fact = new SAMFileWriterFactory();
        fact.setCreateIndex(true);

        final SAMFileWriter output = fact.makeBAMWriter(input.getFileHeader(), true, bam);

        for (final SAMRecord samRecord : input) {
            // Convert read name to upper case.
            samRecord.setReadName(samRecord.getReadName().toUpperCase());
            output.addAlignment(samRecord);

        }

        output.close();
        input.close();
    }

//    public void buildBamIndex(File bam) {
//        String path = bam.toString();
//        final String indexFileBase = path.endsWith(".bam")
//                ? path.substring(0, path.lastIndexOf(".")) : path;
//        final File indexFile = new File(indexFileBase + BAMIndex.BAMIndexSuffix);
//        if (indexFile.exists()) {
//            if (!indexFile.canWrite()) {
//                err("Not creating BAM index since unable to write index file " + indexFile);
//            }
//        }
//        final SAMFileReader input = new SAMFileReader(bam);
//        BAMIndexer indexer = new BAMIndexer(indexFile, input.getFileHeader());
//
//    }
//    public void processAllSAMRecords(File sam, SamHandler handler) {
//        final SAMFileReader inputSam = new SAMFileReader(sam);
//
//
//        for (final SAMRecord samRecord : inputSam) {
//            // Convert read name to upper case.
//            handler.processSam(samRecord);
//        }
//
//        inputSam.close();
//    }
    public String getErrorMsg() {
        return errmsg;
    }
//

    public interface SamHandler {

        public void processSam(SAMRecord sam);
    }

//    public void createWellToSamIndex() {
//       
//        if (!welltosamindex.hasIndex()) {
//             createReadLocationsIndexFile();
//        }
//    }
    public void createWellToLocIndex() {
        if (!this.welltolocdindexer.hasIndex()) {
            createReadLocationsIndexFile();
        }
    }

//    public boolean hasWellToReadIndex() {
//        if (welltosamindexfile == null || !welltosamindexfile.exists()) {
//            return false;
//        }
//        if (welltosamindex == null) {
//            welltosamindex = new WellToSamIndex(samfile, welltosamindexfile);
//        }
//        return welltosamindex.hasIndex();
//    }
    public Coord getCoord(String name) {
        return WellToSamIndex.extractWellCoord(name);
    }

    public SAMRecord getSequenceViaBai(String readname) {
        p("Finding SAM via BAI: " + readname);
        Coord c = this.getCoord(readname);
        int x = c.x;
        int y = c.y;
        long posInGenome = findReadpossByWell(readname);
        p("read " + readname + " maps to pos " + posInGenome);
        if (posInGenome < 0) {
            p("Read " + readname + " is not mapped/aligned: " + posInGenome);
            return null;
        }
        ArrayList<SAMRecord> recs = findRecords(posInGenome);
        if (recs == null) {
            p("Got no samrecors for pos " + posInGenome);
        } else {
            p("Found " + recs.size() + " BAM records for pos " + posInGenome);
        }
        for (SAMRecord rec : recs) {
            Coord coord = this.getCoord(rec.getReadName());
            if (coord.x == x && coord.y == y) {
                p("Found SamRecord for " + x + "/" + y + ":" + rec);
                return rec;
            }
        }
        p("Could not find " + readname + " via bai");
        return null;
    }

//    public SAMRecord getSequenceByIndex(int x, int y) {
//        if (welltosamindex == null) {
//            welltosamindex = new WellToSamIndex(samfile, welltosamindexfile);
//        }
//        return welltosamindex.findSequence(x, y);
//
//    }
    public static String getCommandLine(SAMRecord rec) {
        /**
         * @HD	VN:1.3	SO:unsorted
        @SQ	SN:gi|170079663|ref|NC_010473.1|	LN:4686137
        @RG	ID:6G8PE	LB:e_coli_dh10b	PL:IONTORRENT	PU:PGM/314R	SM:altbeads	PG:tmap
        @PG	ID:tmap	VN:0.0.18	CL:mapall -A 5 -M 3 -O 3 -E 1 -R PL:IONTORRENT -R LB:e_coli_dh10b -R ID:6G8PE -R SM:altbeads -R PU:PGM/314R -n 6 
         * -f /results/referenceLibrary/tmap-f2/e_coli_dh10b/e_coli_dh10b.fasta -r trunc.R_2011_03_28_18_36_46_user_HEN-272-R9017-BB229_LN434_NHB-KM_Auto_HEN-272-R9017-BB229_LN434_NHB-KM_3807.fastq -v map1 map2 map3
        6G8PE:767:518	16	gi|170079663|ref|NC_010473.1|	1	77	27S22M1I24M1D53M	*	0	0	CAGTCGGTGATTTAGTAAGTATTTTTCAGCTTTTCATTCTGACTGCAACGGGGCAATATGTCTCTGTGTGGATTAAAAAAGAGTGTCTGATAGCAGCTTCTGAACTGGTTACCTGCCGTGAGTAAAT	
         */
        if (rec.getHeader() == null) {
            return "Got no header";
        }
        if (rec.getHeader().getProgramRecords() == null || rec.getHeader().getProgramRecords().size() < 1) {
            return "Got no program records";
        }

        String cl = rec.getHeader().getProgramRecords().get(0).getCommandLine();
        return cl;
    }

    public void extractData(SAMRecord rec, Read read) {
        if (rec == null) {
            return;
        }
        read.setFlags(rec.getFlags());


        Object md = rec.getAttribute("MD");
        if (md == null) {
            read.setMd("");
        } else {
            read.setMd("" + md);
        }
        //  SequenceUtil.make
        Alignment al = extractAlignment(rec, read);
        if (al != null) {
            if (read.isReverse()) {
                al.setSeqReverse(true);
            }
            // p("Got alignment: " + al);
            read.setReferenceName(rec.getReferenceName());
            //    read.setFastaFile(getFastaFile(rec));
            read.setCommandLine(getCommandLine(rec));
            read.setAlignmentStart(rec.getAlignmentStart());
            read.setAlignmentEnd(rec.getAlignmentEnd());
            read.setCigarString(rec.getCigarString());
            read.setAlign(al);
        } else {
            p("Got NO alignment from record " + rec.getCigarString());
        }
    }

    public static String getFastaFile(SAMRecord rec) {
        /**
         * @HD	VN:1.3	SO:unsorted
        @SQ	SN:gi|170079663|ref|NC_010473.1|	LN:4686137
        @RG	ID:6G8PE	LB:e_coli_dh10b	PL:IONTORRENT	PU:PGM/314R	SM:altbeads	PG:tmap
        @PG	ID:tmap	VN:0.0.18	CL:mapall -A 5 -M 3 -O 3 -E 1 -R PL:IONTORRENT -R LB:e_coli_dh10b -R ID:6G8PE -R SM:altbeads -R PU:PGM/314R -n 6 
         * -f /results/referenceLibrary/tmap-f2/e_coli_dh10b/e_coli_dh10b.fasta -r trunc.R_2011_03_28_18_36_46_user_HEN-272-R9017-BB229_LN434_NHB-KM_Auto_HEN-272-R9017-BB229_LN434_NHB-KM_3807.fastq -v map1 map2 map3
        6G8PE:767:518	16	gi|170079663|ref|NC_010473.1|	1	77	27S22M1I24M1D53M	*	0	0	CAGTCGGTGATTTAGTAAGTATTTTTCAGCTTTTCATTCTGACTGCAACGGGGCAATATGTCTCTGTGTGGATTAAAAAAGAGTGTCTGATAGCAGCTTCTGAACTGGTTACCTGCCGTGAGTAAAT	
         */
        String cl = getCommandLine(rec);
        if (cl == null) {
            err("No command line");
            return null;
        }
        int fasta = cl.indexOf(".fasta");
        if (fasta < 0) {
            String msg = "Could not find fasta file in command line " + cl;
            err(msg);
            return null;
        }
        cl = cl.substring(0, fasta + ".fasta".length());
        int sl = cl.lastIndexOf("/");
        String name = "";
        if (sl > 0) {
            name = cl.substring(sl + 1);
        }
        int sp = cl.lastIndexOf(" ");
        if (sp < 0) {
            sp = cl.lastIndexOf("\t");
        }
        if (sp > 0) {
            cl = cl.substring(sp);
        }
        String fullpath = cl;
        //   p("Got fasta file:" + name + " in dir " + fullpath);
        return fullpath;
    }

    public DNASequence getReferenceSequence(SAMRecord rec) {
        //errmsg = null;
        DNASequence ref = null;
        int start = rec.getAlignmentStart() - 1;
        int end = rec.getAlignmentEnd();
        if (end < 1) {
            return new DNASequence();
        }
        byte[] refchars = null;
        try {
            refchars = SequenceUtil.makeReferenceFromAlignment(rec, true);
        } catch (Exception e) {
             //   p("Got no MD tag for: " + rec.getReadName());
        }



        StringBuffer seq = new StringBuffer();
        if (refchars != null && refchars.length > 0) {
            for (int i = 0; i < refchars.length; i++) {
                char c = (char) refchars[i];
                if (c != '-' && c != '_' && c != '0') {
                    seq = seq.append(c);
                    if (c != 'G' && c != 'A' && c != 'T' && c != 'C' && c != 'N') {
                        p("getReferenceSequence: Got strange ref base:" + c + "/" + (int) c);

                    }
                }
            }
            ref = new DNASequence(seq);
        }


        if (ref == null) {
            errmsg = "Was not able to get sequence " + start + "-" + end + " from " + rec.getReadName();
          //  p(errmsg);
         //   p("Header is: "+rec.getHeader().toString());
          //  p("Cigar string: "+ rec.getCigarString());
//            Exception e = new Exception("test");
//            e.printStackTrace();
            int len = end - start + 1;
            StringBuffer b = new StringBuffer();
            for (int i = 0; i < len; i++) {
                b = b.append('x');
            }
            ref = new DNASequence(b);
        }

//        if (errmsg != null) {
//            p(errmsg);
//        }
        ref.setName(rec.getReferenceName() + " " + start + "-" + end);
        return ref;
    }

    public ArrayList<String> getReferenceNames() {
        ArrayList<String> names = new ArrayList<String>();
        final SAMFileReader input = new SAMFileReader(bamfile);
        SAMSequenceDictionary dict = input.getFileHeader().getSequenceDictionary();
        List<SAMSequenceRecord> recs = dict.getSequences();
        for (SAMSequenceRecord rec : recs) {
            names.add(rec.getSequenceName());
        }
        return names;
    }
    // find well coordinates for given sequence position

    public ArrayList<SAMRecord> findRecords(long posInGenome) {
        ArrayList<String> refnames = getReferenceNames();
        ArrayList<SAMRecord> res = new ArrayList<SAMRecord>();
        for (String refname : refnames) {
            ArrayList<SAMRecord> oneres = findRecords(posInGenome, refname);
            if (oneres != null) {
                res.addAll(oneres);
            }
        }
        return res;
    }

    public boolean hasBai() {
         if (!bamfile.exists()) {
            errmsg = "Bam file " + bamfile + " does not exist";
            err(errmsg);
            return false;
        }
        String path = bamfile.toString();
        final String indexFileBase = path.endsWith(".bam")
                ? path.substring(0, path.lastIndexOf(".")) : path;
        File indexFile = new File(indexFileBase + BAMIndex.BAMIndexSuffix);
        if (!indexFile.exists()) {
            indexFile = new File(indexFileBase + ".bam.bai");
        }
        if (!indexFile.exists()) {
            errmsg = "Bam file index " + indexFile + " does not exist, please create the index first... ";            
            err(errmsg);
            return false;
        }
        else return true;
    }
    public ArrayList<SAMRecord> findRecords(long posInGenome, String refname) {
        if (!hasBai()) return null;
        final SAMFileReader input = new SAMFileReader(bamfile);

        CloseableIterator<SAMRecord> it = input.query(refname, (int) posInGenome, (int) posInGenome, false);
        ArrayList<SAMRecord> res = new ArrayList<SAMRecord>();
        for (; it != null && it.hasNext();) {
            res.add(it.next());
        }
        return res;

    }

    public Alignment extractAlignment(SAMRecord rec) {
        return extractAlignment(rec, null);
    }

    public Alignment extractAlignment(SAMRecord rec, Read read) {
        if (rec == null) {
            return null;
        }
        if (rec.getCigar() == null) {
            return null;
        }
        List<CigarElement> cigar = rec.getCigar().getCigarElements();
        

        int readlen = rec.getReadLength();
        int kmerlen[] = new int[readlen + 1];
        int maxkmer = 0;

        //  String seq = rec.getReadString();
        StringBuffer alref = new StringBuffer();
        StringBuffer alread = new StringBuffer();
        //StringBuffer refseq = new StringBuffer();
        String readseq = rec.getReadString();
        DNASequence ref = getReferenceSequence(rec);
        int flags = rec.getFlags();
        //      p("Flags: " + flags);

        StringBuffer mark = new StringBuffer();
        int posinseq2 = 0;
        int posinref1 = 0;
        int pos1 = 0;
        int pos2 = 0;
        int start1 = 0;
        int start2 = 0;
        ArrayList<Cell> cells = new ArrayList<Cell>();
        for (CigarElement el : cigar) {
            int len = el.getLength();
            String OP = el.getOperator().name().toUpperCase();
            Cell cell = new Cell();
            cell.set(pos1, pos2, 0);
            cells.add(cell);

            //		System.out.print(el.getOperator().name()+":"+el.getLength()+" " );
            if (OP.equals("I")) {
                // totins += len;
                for (int i = 0; i < len; i++) {
                    alref = alref.append("_");
                    char base = readseq.charAt(posinseq2);
                    byte b = DNASequence.getCharToByte(base);
                    
                    alread = alread.append(base);
                    mark = mark.append(" ");
                    posinseq2++;
                    pos2++;

                }
            } else if (OP.equals("D")) {
                //  totdel += len;

                for (int i = 0; i < len; i++) {
                    alref = alref.append(ref.getBaseChar(posinref1));
                    alread = alread.append("_");
                    mark = mark.append(" ");
                    pos1++;
                    posinref1++;
                }
            } else if (OP.equals("M") || OP.equals("X") || OP.equals("=")) {
                if (len < kmerlen.length) {
                    kmerlen[len]++;
                }
                if (len > maxkmer) {
                    maxkmer = len;
                }
                for (int i = 0; i < len; i++) {
                    char base = readseq.charAt(posinseq2);
                    if (ref.getBaseChar(posinref1)=='x' || ref.getBaseChar(posinref1)=='X') {
                        ref.setBaseCharAt(posinref1, base);
                      //  p("putting "+base+" at "+posinref1);
                    }
                    char baseref = ref.getBaseChar(posinref1);
                    alref = alref.append(baseref);
                    alread = alread.append(base);
                    
                    if (base == baseref) {
                        mark = mark.append("|");
                    } else {
                        mark = mark.append(":");
                    }
//                    if (OP.equals("X")) {
//                        mark = mark.append(":");
//                    } else {
//                        mark = mark.append("|");
//                    }
                    posinseq2++;
                    posinref1++;
                    pos1++;
                    pos2++;

                }
                /** 
                 * M 0 alignment match (can be a sequence match or mismatch)
                I 1 insertion to the reference
                D 2 deletion from the reference
                N 3 skipped region from the reference
                S 4 soft clipping (clipped sequences present in SEQ)
                H 5 hard clipping (clipped sequences NOT present in SEQ)
                P 6 padding (silent deletion from padded reference)
                = 7 sequence match
                X 8 sequence mismatch
                 */
            } else if (OP.equals("S")) { // SKIP
                if (posinseq2 == 0) {
                    start2 = len;
                }
                posinseq2 += len;

            } else {
                p("Unknown cigar element:" + el.getOperator().name());
            }
        }
/// include skipped bases!
        // NO KEY!
        //start2 += 4;
        Alignment al = new Alignment(null, null);
        al.setCelllist(cells);
        al.setRefAlign1(new DNASequence(alref));
        al.setSeqAlign2(new DNASequence(alread));
        al.setSeqStart2(start2);
        al.setRefStart1(start1);
        al.setRefSeq1(ref);

        if (read == null) {
            DNASequence dread = new DNASequence(readseq);
            dread.setName("SAM/BAM read");
            al.setSeq2(dread);
        } else {
            al.setSeq2(read);
        }
        al.setMarkupLine(mark.toString().toCharArray());
        al.calculateStats();
        al.setGenomeStartpos(rec.getAlignmentStart());
        al.setGenomeEndpos(rec.getAlignmentEnd());
//p("Got ref: "+al.getRefSeq1());
        return al;

    }
    public int[] computeQlengths(SAMRecord rec, int[] errorValues) {
        Alignment al = extractAlignment(rec);
        return al.computeQlengths(errorValues);
    }
   
    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(SamUtils.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {

        Logger.getLogger(SamUtils.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(SamUtils.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("SamUtils: " + msg);
        //Logger.getLogger( SamUtils.class.getName()).log(Level.INFO, msg, ex);
    }
}
