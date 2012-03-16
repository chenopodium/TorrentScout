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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.samtools.util.CloseableIterator;
import net.sf.samtools.util.StringUtil;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileReader.ValidationStringency;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMProgramRecord;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMTextHeaderCodec;
import org.openide.util.Exceptions;

/**
 * Internal class for reading SAM text files.
 */
public class MySamTextReader {
    // From SAM specification
    boolean showLines = false;
    private static final int QNAME_COL = 0;
    private static final int FLAG_COL = 1;
    private static final int RNAME_COL = 2;
    private static final int POS_COL = 3;
    private static final int MAPQ_COL = 4;
    private static final int CIGAR_COL = 5;
    private static final int MRNM_COL = 6;
    private static final int MPOS_COL = 7;
    private static final int ISIZE_COL = 8;
    private static final int SEQ_COL = 9;
    private static final int QUAL_COL = 10;
    private static final int NUM_REQUIRED_FIELDS = 11;
    // Read string must contain only these characters
    private static final Pattern VALID_BASES = Pattern.compile("^[acgtnACGTN.=]+$");
    private SeekableRAStream mReader;
    private SAMFileHeader mFileHeader = null;
    private String mCurrentLine = null;
    private RecordIterator mIterator = null;
    private File mFile = null;
    private final MyTextTagCodes tagCodec = new MyTextTagCodes();
    private SAMFileReader.ValidationStringency validationStringency = SAMFileReader.ValidationStringency.LENIENT;

    /**
     * Prepare to read a SAM text file.
     * @param stream Need not be buffered, as this class provides buffered reading.
     */
//    public MySamTextReader(final SeekableRAStream stream) {
//        mReader = (SeekableRAStream) stream;
//        
//
//    }
   private void readHeader() {
     //   p("About to read sam header");
       //this.advanceLine();
        showLines = true;
        final SAMTextHeaderCodec headerCodec = new SAMTextHeaderCodec();
        headerCodec.setValidationStringency(ValidationStringency.LENIENT);
        mFileHeader = headerCodec.decode(mReader, mFile.toString());
     //   p("Got sam program records: "+mFileHeader.getProgramRecords());
        showLines = false;
      //  advanceLine();
    }
   
    /**
     * Prepare to read a SAM text file.
     * @param stream Need not be buffered, as this class provides buffered reading.
     * @param file For error reporting only.
     */
    public MySamTextReader(SeekableRAStream stream, final File file) {
        mReader = (SeekableRAStream) stream;        
        mFile = file;
        readHeader();

    }

    void close() {
        if (mReader != null) {
            mReader.close();
            mReader = null;
        }
    }

    public SAMFileHeader getFileHeader() {
        return mFileHeader;
    }

    public SAMFileReader.ValidationStringency getValidationStringency() {
        return validationStringency;
    }

    public void setValidationStringency(final SAMFileReader.ValidationStringency stringency) {
        this.validationStringency = stringency;
    }

    /**
     * There can only be one extant iterator on a SAMTextReader at a time.  The previous one must
     * be closed before calling getIterator().  Because the input stream is not seekable, a subsequent
     * call to getIterator() returns an iterator that starts where the last one left off.
     *
     * @return Iterator of SAMRecords in file order.
     */
    public CloseableIterator<SAMRecord> getIterator() {
        if (mReader == null) {
            throw new IllegalStateException("File reader is closed");
        }
        if (mIterator != null) {
            throw new IllegalStateException("Iteration in progress");
        }
        mIterator = new RecordIterator();
        return mIterator;
    }

    /**
     * Unsupported for SAM text files.
     */
    CloseableIterator<SAMRecord> query(final String sequence, final int start, final int end, final boolean contained) {
        throw new UnsupportedOperationException("Cannot query SAM text files");
    }

    public CloseableIterator<SAMRecord> queryUnmapped() {
        throw new UnsupportedOperationException("Cannot query SAM text files");
    }

//    public void readHeader() {
//        while (mCurrentLine == null || mCurrentLine.startsWith("@")) {
//            advanceLine();
//        }
//
//    }

    public String advanceLine() {
        mCurrentLine = mReader.readLine();
        if (showLines) p("nextLine:"+mCurrentLine);
        return mCurrentLine;
    }

    private String makeErrorString(final String reason) {
        String fileMessage = "";
        if (mFile != null) {
            fileMessage = "File " + mFile + "; ";
        }
        return "Error parsing text SAM file. " + reason + "; " + fileMessage
                + "Line  " + mCurrentLine;
    }

    private RuntimeException reportFatalErrorParsingLine(final String reason) {
        return new SAMFormatException(makeErrorString(reason));
    }

    private void reportErrorParsingLine(final String reason) {
        final String errorMessage = makeErrorString(reason);

        if (validationStringency == SAMFileReader.ValidationStringency.STRICT) {
            p(reason);
        } else if (validationStringency == SAMFileReader.ValidationStringency.LENIENT) {
            p("Ignoring SAM validation error due to lenient parsing:");
            p(errorMessage);
        }
    }

    private void reportErrorParsingLine(final Exception e) {
        final String errorMessage = makeErrorString(e.getMessage());
        if (validationStringency == SAMFileReader.ValidationStringency.STRICT) {
            p(e.getMessage());
        } else if (validationStringency == SAMFileReader.ValidationStringency.LENIENT) {
            p("Ignoring SAM validation error due to lenient parsing:");
            p(errorMessage);
        }
    }

    /**
     * SAMRecord iterator for SAMTextReader
     */
    private class RecordIterator implements CloseableIterator<SAMRecord> {

        /**
         * Allocate this once rather than for every line as a performance optimization.
         * The size is arbitrary -- merely large enough to handle the maximum number
         * of fields we might expect from a reasonable SAM file.
         */
        private final String[] mFields = new String[10000];
        private SAMRecord mCurrentRecord;

        private RecordIterator() {
            assert (mReader != null);


        }

        public void close() {
            mCurrentRecord = null;
            mReader.close();

        }

        public boolean hasNext() {
            try {
                return !mReader.eof();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return false;
        }

        public SAMRecord next() {
            if (!hasNext()) {
                p("Cannot call next() on exhausted iterator");
            }

            if (mCurrentLine == null) {
            //    p("getting next line");
                advanceLine();
            }
            if (mCurrentLine != null) {
           //     p("Parsing line "+mCurrentLine);
                parseLine();
           //     p("getting next line");
                advanceLine();
            }
         //   p("Got mCurrentRecord: "+mCurrentRecord);
            return mCurrentRecord;
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported: remove");
        }

        int parseInt(final String s, final String fieldName) {
            int ret=-1;
            try {
                ret = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                err("Non-numeric value in " + fieldName + " column: "+s);
            }
            return ret;
        }

        void validateReferenceName(final String rname, final String fieldName) {
            if (fieldName.equals("MRNM") && rname.equals("=")) {
                return;
            }
            if (getFileHeader().getSequenceDictionary().size() != 0) {
                if (getFileHeader().getSequence(rname) == null) {
                    reportErrorParsingLine(fieldName + " '" + rname + "' not found in any SQ record");
                }
            }
        }

        private void parseLine() {
            final int numFields = StringUtil.split(mCurrentLine, mFields, '\t');
            if (numFields < NUM_REQUIRED_FIELDS) {
                reportErrorParsingLine("Not enough fields");
            }
            if (numFields == mFields.length) {
                reportErrorParsingLine("Too many fields in SAM text record.");
            }
            for (int i = 0; i < numFields; ++i) {
                if (mFields[i].length() == 0) {
                    reportErrorParsingLine("Empty field at position " + i + " (zero-based)");
                }
          //      else p("Got :"+mFields[i]);
            }
            mCurrentRecord = new SAMRecord(mFileHeader);
            mCurrentRecord.setValidationStringency(getValidationStringency());
            mCurrentRecord.setHeader(mFileHeader);
            mCurrentRecord.setReadName(mFields[QNAME_COL]);

         //   p("Creating new sam record with name "+mCurrentRecord.getReadName());
            final int flags = parseInt(mFields[FLAG_COL], "FLAG");
            if (flags < 0) {
                err("Could not parse flag from line: "+mCurrentLine);
            }
            mCurrentRecord.setFlags(flags);

            final String rname = mFields[RNAME_COL];
            final int pos = parseInt(mFields[POS_COL], "POS");
            final int mapq = parseInt(mFields[MAPQ_COL], "MAPQ");
            final String cigar = mFields[CIGAR_COL];

            mCurrentRecord.setAlignmentStart(pos);
            mCurrentRecord.setMappingQuality(mapq);
            mCurrentRecord.setCigarString(cigar);
         //   p("cigar string: "+cigar);
            final String mateRName = mFields[MRNM_COL];
            if (mateRName.equals("*")) {
            } else {


                validateReferenceName(mateRName, "MRNM");
                if (mateRName.equals("=")) {
                    if (mCurrentRecord.getReferenceName() == null) {
                        //reportErrorParsingLine("MRNM is '=', but RNAME is not set");
                    } else {
                        mCurrentRecord.setMateReferenceName(mCurrentRecord.getReferenceName());
                    }
                } else {
                    mCurrentRecord.setMateReferenceName(mateRName);
                }
            }

            final int matePos = parseInt(mFields[MPOS_COL], "MPOS");
            final int isize = parseInt(mFields[ISIZE_COL], "ISIZE");
            if (!mCurrentRecord.getMateReferenceName().equals(SAMRecord.NO_ALIGNMENT_REFERENCE_NAME)) {
                if (matePos == 0) {
                    reportErrorParsingLine("MPOS must be non-zero if MRNM is specified");
                }
            } else {
                if (matePos != 0) {
                    reportErrorParsingLine("MPOS must be zero if MRNM is not specified");
                }
                if (isize != 0) {
                    reportErrorParsingLine("ISIZE must be zero if MRNM is not specified");
                }
            }
            mCurrentRecord.setMateAlignmentStart(matePos);
            mCurrentRecord.setInferredInsertSize(isize);
            if (mFields[SEQ_COL] != null && !mFields[SEQ_COL].equals("*")) {
                validateReadBases(mFields[SEQ_COL]);
                mCurrentRecord.setReadString(mFields[SEQ_COL]);
            }
            if (mFields[QUAL_COL] != null && !mFields[QUAL_COL].equals("*")) {

                mCurrentRecord.setBaseQualityString(mFields[QUAL_COL]);
            }

            for (int i = NUM_REQUIRED_FIELDS; i < numFields; ++i) {
                parseTag(mFields[i]);
            }
      //      p("Record is: "+mCurrentRecord+", cigar:"+mCurrentRecord.getCigarString());

        }

        private void validateReadBases(final String bases) {
            if (!VALID_BASES.matcher(bases).matches()) {
                reportErrorParsingLine("Invalid character in read bases");
            }
        }

        private void parseTag(final String tag) {
            Map.Entry<String, Object> entry = null;
            try {
                entry = tagCodec.decode(tag);
            } catch (SAMFormatException e) {
                reportErrorParsingLine(e);
            }
            if (entry != null) {
                mCurrentRecord.setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        p(msg + ",  ex:" + ex.getMessage());
        Logger.getLogger(MySamTextReader.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        p(msg);
        Logger.getLogger(MySamTextReader.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(MySamTextReader.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
//  System.out.println("MySamTextReader: " + msg);
        //  Logger.getLogger( MySamTextReader.class.getName()).log(Level.INFO, msg);
    }
}
