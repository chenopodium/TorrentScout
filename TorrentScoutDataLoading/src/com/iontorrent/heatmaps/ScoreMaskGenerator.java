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
package com.iontorrent.heatmaps;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.rawdataaccess.wells.ScoreMaskFlag;
import com.iontorrent.results.scores.ScoreMask;

import com.iontorrent.sequenceloading.SequenceLoader;
import com.iontorrent.sff.Sff;
import com.iontorrent.sff.SffRead;
import com.iontorrent.utils.io.FileUtils;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import org.iontorrent.seq.sam.WellToSamIndex;
import org.iontorrent.seq.Coord;
import org.iontorrent.seq.Read;
import org.iontorrent.seq.alignment.Alignment;
import org.iontorrent.seq.sam.SamUtils;
import org.iontorrent.seq.sam.SamUtils.SamHandler;
//import org.openide.util.Exceptions;

/**
 *
 * @author Chantal Roth
 */
public class ScoreMaskGenerator implements SamHandler {

    ScoreMask mask;
    String msg;
    SequenceLoader loader = null;
    ExperimentContext exp;

    public ScoreMaskGenerator(ScoreMask mask, ExperimentContext exp) {
        this.exp = exp;
        this.mask = mask;
        loader = SequenceLoader.getSequenceLoader(exp);
    }

    /** run this via a thread!!! */
    public String generateImageFiles(ScoreMaskFlag flag) {
       // double[][] data = mask.createEmptyData();
        if (mask.hasImage(flag)) {
            p("Image already exists :-)");
            return null;
        }
        String msg = "";
        if (flag.isIn(flag.SAM_FLAGS)) {
            try {
                msg = processBamFile(true);
                if (mask.hasImage(flag)) {
                    return null;
                }

            } catch (IOException ex) {
                msg = "I could not read the sam.parsed file: " + ex.getMessage();
                ex.printStackTrace();
            }
        }
        //   } else {
//        if (flag.isIn(ScoreMaskFlag.WELLS_FLAGS)) {
//            try {
//                msg += readWellStatsFile(true);
//                if (mask.hasImage(flag)) {
//                    return null;
//                }
//                //  msg = "I have not implemented the code to genreate the image for " + flag + " yet :-)";
//            } catch (IOException ex) {
//                msg += "<br>I could not read the wellstats file: " + ex.getMessage();
//                ex.printStackTrace();
//            }
//        }
        if (flag.isIn(ScoreMaskFlag.SFF_FLAGS)) {
            try {
                msg += readSffFile(true);
                if (mask.hasImage(flag)) {
                    return null;
                }
                //  msg = "I have not implemented the code to genreate the image for " + flag + " yet :-)";
            } catch (IOException ex) {
                msg += "<br>I could not read the sff file: " + ex.getMessage();
                ex.printStackTrace();
            }
        }
        return msg;
    }

    public String generateAllMissingHeatMaps() {
        String msg = null;
        String errors = "";
//        if (!mask.hasAllWellImages()) {
//            try {
//                msg = readWellStatsFile(false);
//            } catch (IOException ex) {
//                Logger.getLogger(ScoreMaskGenerator.class.getName()).log(Level.SEVERE, null, ex);
//            }
        
//            if (msg != null && msg.length() > 0) {
//                errors += msg + "\n";
//            }
//        } else {
//            p("Alreay got all well stats heatmaps");
//        }
        if (!mask.hasAllSffImages()) {
            try {
                msg = readSffFile(false);
            } catch (IOException ex) {
                Logger.getLogger(ScoreMaskGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (msg != null && msg.length() > 0) {
                errors += msg + "\n";
            }
        } else {
            p("Alreay got all well stats heatmaps");
        }
        if (!mask.hasAllBamImages()) {
            try {
                msg = processBamFile(false);
            } catch (IOException ex) {
                Logger.getLogger(ScoreMaskGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (msg != null && msg.length() > 0) {
                errors += msg + "\n";
            }
        } else {
            p("Alreay got all bam heatmaps");
        }
        return errors;
    }

    // 0        1        2        3      4       5       6      7        8       9      10       11     12       13      14    15
//    col	row	isTF	isLib	isDud	isAmbg	nCall	cf	ie	dr	keySNR	keySD	keySig	oneSig	zeroSig	ppf	medAbsRes	multiplier
//44	4	1	0	0	0	91	0.0060	0.0070	0.00400	32.443	0.031	1.001	1.013	0.013	0.433	0.039	0.182
//39	4	1	0	0	0	101	0.0060	0.0070	0.00400	25.637	0.038	0.970	0.971	0.001	0.433	0.025	0.249
//129	4	1	0	0	0	106	0.0090	0.0070	0.00550	13.797	0.072	0.996	0.997	0.001	0.367	0.019	0.208
//263	4	1	0	0	0	107	0.0170	0.0060	0.00050	14.933	0.070	1.042	1.043	0.001	0.383	0.022	0.050
//    public String readWellStatsFile(boolean gui) throws IOException {
//        String file = exp.getResultsDirectory() + "wellStats.txt";
//        // File f = FileUtils.findAndCopyFileFromUrlTocache("wellStats.txt", context.getCacheDir(), context.getResultsDir(), false, false, null, 1024 * 1024);
//        if (!FileUtils.exists(file)) {
//            p("Got no wellstats file (outdated) " + file);
//            return null;
//        }
//
//        DataInputStream in = FileUtils.openFileOrUrl(file);
//
//        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//        String header = reader.readLine();
//
//        int len = (int) (new File(file).length() / 1000000);
//        if (gui) {
//            GuiUtils.showNonModelMsg("Reading wellstats file " + file + ", " + len + " MB, will take about " + len / 10 + " seconds", true, Math.max(len / 10, 5));
//        }
//        //    p("reading wellsstats file with header " + header);
//        String line = null;
//
//        int NR_WELLS_FLAGS = ScoreMaskFlag.WELLS_FLAGS.length;
//        double[][][] data = new double[NR_WELLS_FLAGS][mask.getNrCols()][mask.getNrRows()];
//        int count = 0;
//        int delta = ScoreMaskFlag.WELL_START;
//        do {
//            line = reader.readLine();
//            if (line != null) {
//                count++;
//
//                String[] cols = line.split("\t");
////                if (count % 100000 == 0) {
////                    p("Processing line "+count+"" + line + ", \n" + cols.length);
////                }
//                if (cols != null && cols.length > 17) {
//                    int col = Integer.parseInt(cols[0]);
//                    int row = Integer.parseInt(cols[1]);
//                    double cf = Double.parseDouble(cols[7]);
//                    double snr = Double.parseDouble(cols[10]);
//                    double ppf = Double.parseDouble(cols[15]);
//                    double ie = Double.parseDouble(cols[8]);
//                    double dr = Double.parseDouble(cols[9]);
//                    double nc = Double.parseDouble(cols[6]);
//                    // double nCall = Double.parseDouble(cols[6]);
////                    if (count % 100000 == 0) {
////                        p("got cf:" + cf + ", snr: " + snr + ", dr:" + dr);
////                    }
//
//                    data[ScoreMaskFlag.SNR.getCode() - delta][col][row] = (snr);
//                    data[ScoreMaskFlag.CAFIE.getCode() - delta][col][row] = (cf);
//                    data[ScoreMaskFlag.PPF.getCode() - delta][col][row] = (ppf);
//                    data[ScoreMaskFlag.IE.getCode() - delta][col][row] = (ie);
//                    data[ScoreMaskFlag.DR.getCode() - delta][col][row] = (dr);
//                    //data[ScoreMaskFlag.NCALL.getCode()][col][row] = (nc);
//                }
//            }
//        } while (line != null);
//        in.close();
//        reader.close();
//        p("Done reading file " + file);
//        // save image
//        String msg = "";
//        for (ScoreMaskFlag flag : ScoreMaskFlag.WELLS_FLAGS) {
//            String res = createImageFile(flag, data[flag.getCode()]);
//            if (res != null && res.length() > 4) {
//                msg = msg + "<br>" + res;
//            }
//        }
//        return msg;
//    }

    public String readSffFile(boolean gui) throws IOException {

        p("Reading sff file");
        if (loader.getSffFile() == null || !loader.foundSffFile()) {
            return "Found no SFF file: " + loader.getSffFile();
        }

        Sff sff = new Sff(loader.getSffFile().toString());
        if (sff == null) {
            return "Could not open file " + loader.getSfftfFile();
        }
        sff.openFile();
        long prev_pos = sff.readHeader();
        //    p("sff global header:" + sff.getGheader().toString());
        if (prev_pos <= 0) {
            return "fp after opening sff " + loader.getSffFile() + " is " + prev_pos;
        }

        int len = (sff.getReadCount() / 100000);
        if (gui) {
            GuiUtils.showNonModalMsg("Reading sff file " + loader.getSffFile() + " with " + sff.getReadCount() + " reads", true, Math.max(len / 10, 5));
        }

        ScoreMaskFlag flags[] = new ScoreMaskFlag[3];
        flags[0] = ScoreMaskFlag.SNR;
        flags[1] = ScoreMaskFlag.PPF;
        flags[2] = ScoreMaskFlag.SSQ;
        double[][] snrdata = new double[mask.getNrCols()][mask.getNrRows()];
        double[][] ppfdata = new double[mask.getNrCols()][mask.getNrRows()];
        double[][] ssqdata = new double[mask.getNrCols()][mask.getNrRows()];
        double[][][] data = new double[3][mask.getNrCols()][mask.getNrRows()];
        data[0] = snrdata;
        data[1] = ppfdata;
        data[2] = ssqdata;

        WellStatsCalculator calc = new WellStatsCalculator(exp);

        int nr = 0;
        boolean done = false;
        int count = 0;
        while (!done) {
            
            nr++;
            SffRead sffread = null;
            try {
                sffread = sff.readNextRead();

            } catch (Exception e) {
                err("got an error:" + e);
                done = true;
            }

            if (sffread == null) {
                p("We got null and are DONE for processing sff file, nr= " + nr);
                done = true;
            } else {

                int x = sffread.getCol();
                int y = sffread.getRow();
                if (x >= 0 && y >= 0) {
                    double ppf = sffread.computePpf();
                    double ssq = sffread.computeSSQ();
                    if (exp.getFlowOrder() != null) {
                        double snr = calc.computeKeySNR(sffread);
                        snrdata[x][y] = snr;
                    }
                   
                    ppfdata[x][y] = ppf;
                    ssqdata[x][y] = ssq;
                }
                count++;
            }
        }
        p("Processed " + count + " reads");
        for (int d = 0; d < data.length; d++) {
            if (d > 0 || exp.getFlowOrder() != null) {
               // p("Creating image for " + flags[d].getName() + ": values for x=100" + Arrays.toString(data[d][100]));
                String res = createImageFile(flags[d], data[d]);
                if (res != null && res.length() > 4) {
                    msg = msg + "<br>" + res;
                }
            }
        }
        return msg;
    }

//    // 0          1               2       3       4       5       6               7       8       9       10
//// name         strand	       tStart	tLen	qLen	match	percent.id	q7Errs	homErrs	mmErrs	indelErrs	qDNA.a	match.a	tDNA.a	tName	start.a	q7Len	q10Len	q17Len	q20Len	q47Len///
////H9T4W:45:663	0               104	30	123	30	    -2.1	7	2	0	7	GATTGTCAGTGTGCTTTTTATTTACTTTCAGTTATCACCGACTGCCCATAGAGAGGCTGAGACTGCAAGGACACAGGGATGTGATGAGCTGTAGTTTTTTTTTTTGGGCCGATGCGCGTGACT	|+++|||||||||||||||||||||||||||||++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++	G---GTCAGTGTGCTTTTTATTTACTTTCAGTT------------------------------------------------------------------------------------------	370	0	37	33	1	1	1	
////H9T4W:45:682	0               1	123	199	123	    0.382114    20	0	30	2        GACTGTAGCCTGGATATTATTCTTGTAGTTTACCTCTTTAAAAACAAAACAAAACAAAACAAAAAACTCCCCTTCCTCACTGCCCAATATAAAAGGCAAATGTGATACATGGCAGAGTTTGTGTCCCCCCCCCCCAGTTGTCTTGAAAGAATATCCGACATGTCCTGAGTGAGATTTTTTTTTTTCCCCCCCCCCCGAG	||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||+|||||||||||||||||||+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++	GACTGTAGCCTGGATATTATTCTTGTAGTTTACCTCTTTAAAAACAAAACAAAACAAAACAAAAAACTCCCCTTCCTCACTGCCCAATATAAAAGGCAAATGTG-TACATGGCAGAGTTTGTGT---------------------------------------------------------------------------	170	0	153	136	125	124	104	
//    public String readSamParsedFile() throws IOException {
//        String file = context.getResultsDir() + "Default.sam.parsed";
//        // File f = FileUtils.findAndCopyFileFromUrlTocache("Default.sam.parsed", context.getCacheDir(), context.getResultsDir(), false, false, null, 1024 * 1024);
//        if (!FileUtils.exists(file)) {
//            return "<br>I can't see the Default.sam.parsed file <b>" + file + "</b>";
//        }
//
//        DataInputStream in = FileUtils.openFileOrUrl(file);
//        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//        String header = reader.readLine();
//        p("reading Default.sam.parsed file with header " + header);
//        String line = null;
//
//        int NR_WELLS_FLAGS = ScoreMaskFlag.SAM_FLAGS.length;
//        int delta = ScoreMaskFlag.SAM_START;
//        ScoreMaskDataPoint[][][] data = new ScoreMaskDataPoint[NR_WELLS_FLAGS][mask.getNrCols()][mask.getNrRows()];
//        int count = 0;
//        do {
//            line = reader.readLine();
//
//            if (line != null) {
//                count++;
//
//                String[] cols = line.split("\t");
//                if (count % 100000 == 0) {
//                    p("Processing line:" + line + ", \n" + cols.length);
//                }
//
//                if (cols != null && cols.length > 19) {
//                    // 0          1               2       3       4       5       6               7       8       9       10             11      12      13      14      15      16      17      18      19      20
//// name         strand	       tStart	tLen	qLen	match	percent.id	q7Errs	homErrs	mmErrs	indelErrs	qDNA.a	match.a	tDNA.a	tName	start.a	q7Len	q10Len	q17Len	q20Len	q47Len///
////H9T4W:45:663	0               104	30	123	30	    -2.1	7	2	0	7			G	370	0	37	33	1	1	1	
////H9T4W:45:682	0               1	123	199	123	    0.382114    20	0	30	2        		-	170	0	153	136	125	124	104	
//
//                    String name = cols[0];
//                    Coord coord = WellToSamIndex.extractWellCoord(name);
//                    // XXX: warning
//                    // Default.sam parsed has the COORDINATES REVERSED!!!!
//                    //int col = coord.x;
//                    //int row = coord.y;
//                    int col = coord.y;
//                    int row = coord.x;
//                    double id = Double.parseDouble(cols[6]) * 100.d;
//                    id = Math.max(0, id);
//                    int qlen = Integer.parseInt(cols[4]);
//                    int tlen = Integer.parseInt(cols[3]);
//                    int match = Integer.parseInt(cols[5]);
//                    double indel = Double.parseDouble(cols[10]);
//                    double q10 = Double.parseDouble(cols[16]);
//                    double q7 = Double.parseDouble(cols[17]);
//                    double q17 = Double.parseDouble(cols[18]);
//                    double q20 = Double.parseDouble(cols[19]);
//                    double q47 = Double.parseDouble(cols[20]);
//                    // double nCall = Double.parseDouble(cols[6]);
//                    if (count % 100000 == 0) {
//                        p("got id:" + id + ", indel: " + indel);
//                    }
//
//                    if (col < mask.getNrCols() && row < mask.getNrRows()) {
//                        for (ScoreMaskFlag flag : ScoreMaskFlag.SAM_FLAGS) {
//                            data[flag.getCode() - delta][col][row] = new ScoreMaskDataPoint();
//                        }
//                        data[ScoreMaskFlag.IDENTITY.getCode() - delta][col][row].setValue(id);
//                        data[ScoreMaskFlag.QLEN.getCode() - delta][col][row].setValue(qlen);
//                        data[ScoreMaskFlag.INDEL.getCode() - delta][col][row].setValue(indel);
//                        data[ScoreMaskFlag.Q7LEN.getCode() - delta][col][row].setValue(q7);
//                        data[ScoreMaskFlag.Q10LEN.getCode() - delta][col][row].setValue(q10);
//                        data[ScoreMaskFlag.Q17LEN.getCode() - delta][col][row].setValue(q17);
//                        data[ScoreMaskFlag.Q20LEN.getCode() - delta][col][row].setValue(q20);
//                        data[ScoreMaskFlag.Q47LEN.getCode() - delta][col][row].setValue(q47);
//                        data[ScoreMaskFlag.TLEN.getCode() - delta][col][row].setValue(tlen);
//                        data[ScoreMaskFlag.MATCH.getCode() - delta][col][row].setValue(match);
//
//                    }
//                }
//            }
//        } while (line != null);
//        in.close();
//        reader.close();
//
//        p("Done reading file " + file);
//        // save image
//        String msg = "";
//        for (ScoreMaskFlag flag : ScoreMaskFlag.SAM_FLAGS) {
//            String res = createImageFile(flag, data[flag.getCode() - delta]);
//            if (res != null && res.length() > 0) {
//                msg = msg + "<br>" + res;
//            }
//
//        }
//        return msg;
//    }
    // 0          1               2       3       4       5       6               7       8       9       10
// name         strand	       tStart	tLen	qLen	match	percent.id	q7Errs	homErrs	mmErrs	indelErrs	qDNA.a	match.a	tDNA.a	tName	start.a	q7Len	q10Len	q17Len	q20Len	q47Len///
//H9T4W:45:663	0               104	30	123	30	    -2.1	7	2	0	7	GATTGTCAGTGTGCTTTTTATTTACTTTCAGTTATCACCGACTGCCCATAGAGAGGCTGAGACTGCAAGGACACAGGGATGTGATGAGCTGTAGTTTTTTTTTTTGGGCCGATGCGCGTGACT	|+++|||||||||||||||||||||||||||||++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++	G---GTCAGTGTGCTTTTTATTTACTTTCAGTT------------------------------------------------------------------------------------------	370	0	37	33	1	1	1	
//H9T4W:45:682	0               1	123	199	123	    0.382114    20	0	30	2        GACTGTAGCCTGGATATTATTCTTGTAGTTTACCTCTTTAAAAACAAAACAAAACAAAACAAAAAACTCCCCTTCCTCACTGCCCAATATAAAAGGCAAATGTGATACATGGCAGAGTTTGTGTCCCCCCCCCCCAGTTGTCTTGAAAGAATATCCGACATGTCCTGAGTGAGATTTTTTTTTTTCCCCCCCCCCCGAG	||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||+|||||||||||||||||||+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++	GACTGTAGCCTGGATATTATTCTTGTAGTTTACCTCTTTAAAAACAAAACAAAACAAAACAAAAAACTCCCCTTCCTCACTGCCCAATATAAAAGGCAAATGTG-TACATGGCAGAGTTTGTGT---------------------------------------------------------------------------	170	0	153	136	125	124	104	
    public String processBamFile(boolean gui) throws IOException {

        boolean ok = loader.foundBamFile();
        if (!ok) {
            String msg = loader.getMsg();
            p(msg);
            return msg;
        }

        p("reading bam file " + loader.getBamFile());
        int len = (int) (loader.getBamFile().length() / 1000000);
        if (gui) {
            int secs = len / 5;
            int mintime = 10;
            int time = Math.max(secs, mintime);
            
            String unit = " seconds";
            if (secs > 60) {
                time = secs/60;
                unit = " minute(s)";
            }
            else if (secs > 10) {
                secs = (int)(secs/10)*10;
            }
            GuiUtils.showNonModalMsg("Processing BAM file, this could take more than " + time + unit , true, secs);
        }

        SamUtils sam = loader.getSamUtils();

        if (sam == null) {
            warn("No sam utils");
            return "Problem with SamUtils";
        }
        int NR_BAM_FLAGS = ScoreMaskFlag.SAM_FLAGS.length;
        int delta = ScoreMaskFlag.SAM_START;
        double[][][] data = new double[NR_BAM_FLAGS][mask.getNrCols()][mask.getNrRows()];
        int count = 0; 
        //SAMTextHeaderCodec
       // SAMFileReader.setValidationStringency(SAMFileReader.ValidationStringency.SILENT);
        final SAMFileReader inputSam = new SAMFileReader(loader.getBamFile());
        
        inputSam.setValidationStringency(SAMFileReader.ValidationStringency.SILENT);
        //p("Stringency "+inputSam.);
        SAMRecordIterator it = inputSam.iterator();
        SAMRecord old = null;
        for (; it.hasNext();) {
            SAMRecord rec = null;
            int tries = 0;
            while (rec == null && it.hasNext() && tries < 5) {
                tries++;
                try {
                    rec = it.next();
                } catch (Exception e) {
                    err("Tries: " + tries + ":" + e.getMessage());
                    e.printStackTrace();
                }
            }
            if (tries >= 3) {
                break;
            }
            if (rec != null && old != null && rec.getReadName().equalsIgnoreCase(old.getReadName())) {
                break;
            }

            if (rec != null) {
                
                Alignment al = sam.extractAlignment(rec);
//                if (sam.getErrorMsg() != null) {
//                    p("old:" + old + " rec: " + rec);
//                }
                if (count % 10000 == 0) {
                    p("Processing record "+count);
                    p("Alignment: "+al);
                }
                count++;
                
                if (al != null) {
                    try {
                        old = rec;
                        String name = rec.getReadName();
                        Coord coord = WellToSamIndex.extractWellCoord(name);
                        // XXX: warning
                        // Default.sam parsed has the COORDINATES REVERSED!!!!
                        //int col = coord.x;
                        //int row = coord.y;
                        int col = coord.x;
                        int row = coord.y;
                        double id = al.getIdentityPerc();

                        int qlen = al.getSeq2().getLength();
                        int tlen = al.getRefSeq1().getLength();

                        int match = al.getIdentity();
                        double indel = al.getGaps();

                        int[] Qvalues = {7, 10, 17, 20, 47};

                        int[] qlens = null;
                        if (rec.getFlags() == 16) {
                            Alignment rev = al.getReverseAlignment();
                            rev.calculateStats();
                            qlens = rev.computeQlengths(Qvalues);
                        } else {
                            qlens = al.computeQlengths(Qvalues);
                        }
                        // double nCall = Double.parseDouble(cols[6]);
                        if (count % 100000 == 0) {
                                p("     got id:" + id + ", indel: " + indel+", qlens: "+Arrays.toString(qlens));
                        }

                        if (col < mask.getNrCols() && row < mask.getNrRows()) {
                            data[ScoreMaskFlag.IDENTITY.getCode() - delta][col][row] = (id);
                            data[ScoreMaskFlag.QLEN.getCode() - delta][col][row] = (qlen);
                            data[ScoreMaskFlag.INDEL.getCode() - delta][col][row] = (indel);
                            data[ScoreMaskFlag.Q7LEN.getCode() - delta][col][row] = (qlens[0]);
                            data[ScoreMaskFlag.Q10LEN.getCode() - delta][col][row] = (qlens[1]);
                            data[ScoreMaskFlag.Q17LEN.getCode() - delta][col][row] = (qlens[2]);
                            data[ScoreMaskFlag.Q20LEN.getCode() - delta][col][row] = (qlens[3]);
                            data[ScoreMaskFlag.Q47LEN.getCode() - delta][col][row] = (qlens[4]);
                            data[ScoreMaskFlag.TLEN.getCode() - delta][col][row] = (tlen);
                            data[ScoreMaskFlag.MATCH.getCode() - delta][col][row] = (match);
                        }
                    } catch (Exception e) {
                        msg = msg + "\nError processing BAM file: "+e.getMessage();
                        err(msg, e);
                        return msg;
                    }
                }
            }
        }


        inputSam.close();

        p("Done reading file " + loader.getBamFile());
        // save image
        String msg = "";

        for (ScoreMaskFlag flag : ScoreMaskFlag.SAM_FLAGS) {
            String res = createImageFile(flag, data[flag.getCode() - delta]);
            if (res != null && res.length() > 4) {
                msg = msg + "<br>" + res;
            }

        }

        return msg;
    }

    public String processBamFileForCustomFlag(boolean gui, ScoreMaskCalculatorIF compute) throws IOException {
        p("processBamFileForCustomFlag");
        File cf = new File(mask.getImageFile(compute.getFlag()));
        if (cf.exists()) {
            p("Deleting cached file  " + cf);
            cf.delete();
        }

        boolean ok = loader.foundBamFile();
        if (!ok) {
            String msg = loader.getMsg();
            return msg;
        }

        compute.setExpContext(exp);
        p("============================================================ reading bam file " + loader.getBamFile());
        p("Parameters: " + ((AbstractSMCalculator) compute).toFullString());
        int len = Math.max(5, (int) (loader.getBamFile().length() / 1000000));
        len = Math.min(300, len);
        if (gui) {
            if (compute.requiresRead()) {
                GuiUtils.showNonModalMsg("Processing SFF and BAM file, will take at least " + len*5  + " seconds", true, len*5);
            } else {
                GuiUtils.showNonModalMsg("Processing BAM file, probably takes at least " + len + " seconds", true, len);
            }
        }

        SamUtils sam = loader.getSamUtils();

        if (mask == null) {
            return "No mask in processBamFileForCustomFlag";

        }
        double[][] data = new double[mask.getNrCols()][mask.getNrRows()];
        int count = 0;
        final SAMFileReader inputSam = new SAMFileReader(loader.getBamFile());
        inputSam.setValidationStringency(SAMFileReader.ValidationStringency.SILENT);
       // p("Stringency "+inputSam.get);
        SAMRecordIterator it = inputSam.iterator();
        SAMRecord old = null;

        for (; it.hasNext();) {
            SAMRecord rec = null;
            int tries = 0;
            while (rec == null && it.hasNext() && tries < 5) {
                tries++;
                try {
                    rec = it.next();

                } catch (Exception e) {
                    err("Tries: " + tries + ":" + e.getMessage());
                    e.printStackTrace();
                }
            }
            if (tries >= 3) {
                break;
            }
            if (rec != null && old != null && rec.getReadName().equalsIgnoreCase(old.getReadName())) {
                break;
            }

            if (rec != null) {
                count++;               
                old = rec;
                String name = rec.getReadName();
                Coord coord = WellToSamIndex.extractWellCoord(name);
                int col = coord.x;
                int row = coord.y;

                 if (count % 1000 == 0) {
                    p("Processing rec:" + rec + ",row/col: "+row+"/"+col+", mask cols/rows="+mask.getNrCols()+"/"+mask.getNrRows());
                }
                if (col < mask.getNrCols() && row < mask.getNrRows()) {
                    Read read = null;
                    if (compute.requiresRead()) {
                        read = loader.getRead(col, row, null);
                    }
                    Alignment al = sam.extractAlignment(rec, read);
                    double value = compute.compute(al);
                    if (count % 1000 == 0) {
                        p("col: " + col + ", row: " + row + ", value for flag " + compute.getFlag() + ": " + value);
                    }

                    data[col][row] = (value);
                }
            }
            else p("Record is null");
        }


        inputSam.close();

        p("======================================= Done reading file " + loader.getBamFile() + "  for flag " + compute.getFlag());
        // save image
        String msg = "";

        String res = createImageFile(compute.getFlag(), data);
        if (res != null && res.length() > 0) {
            msg = msg + "<br>" + res;
        }

        // p("now readong file for  " + compute.getFlag());
        this.mask.readData(compute.getFlag());
        return msg;
    }

    private String createImageFile(ScoreMaskFlag flag, double[][] data) {


        String imageFileOrUrl = mask.getImageFile(flag);
        //   p("Creating image file " + imageFileOrUrl);
        // saves the data to an image file
        if (FileUtils.isUrl(imageFileOrUrl)) {
            return "<br>The file " + imageFileOrUrl + " is an URL, but I have to store the image in a file";
        }
        BufferedImage image = new BufferedImage(mask.getNrCols(), mask.getNrRows(), BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();
        //  p("Writing image " + imageFileOrUrl);
        boolean hasdata = false;
        for (int c = 0; c < mask.getNrCols(); c++) {
            for (int r = 0; r < mask.getNrRows(); r++) {

                double d = data[c][r];
                int val = flag.getIntValue(d);
                if (val > 0) {
                    hasdata = true;
                }
                // convert to rgb
                int red = val % 256;
                val = val / 256;
                int green = val % 256;

                val = val / 256;
                int blue = val % 256;// rrggbb
                raster.setSample(c, r, 0, red);
                raster.setSample(c, r, 1, green);
                raster.setSample(c, r, 2, blue);


            }
        }


        image.setData(raster);
        try {
            //           p("writing buffered image to " + imageFileOrUrl);
            ImageIO.write(image, "BMP", new File(imageFileOrUrl));
            //         p("Successfully wrote " + imageFileOrUrl + " for flag " + flag);
            return null;


        } catch (IOException ex) {
            Logger.getLogger(ScoreMask.class.getName()).log(Level.SEVERE, null, ex);

            return "Could not write data to file " + imageFileOrUrl
                    + ":" + ex.getMessage();
        }
    }

    /** ================== LOGGING ===================== */
    private static void err(String msg, Exception ex) {
        Logger.getLogger(ScoreMaskGenerator.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        this.msg = msg;
        Logger.getLogger(ScoreMaskGenerator.class.getName()).log(Level.SEVERE, msg);
    }

    private static void warn(String msg) {
        Logger.getLogger(ScoreMaskGenerator.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("ScoreMaskGenerator: " + msg);
        //Logger.getLogger( ScoreMaskGenerator.class.getName()).log(Level.INFO, msg, ex);
    }

    @Override
    public void processSam(SAMRecord sam) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
