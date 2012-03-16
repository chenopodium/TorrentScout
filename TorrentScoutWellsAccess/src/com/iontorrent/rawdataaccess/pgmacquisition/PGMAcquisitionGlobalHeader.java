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
package com.iontorrent.rawdataaccess.pgmacquisition;

import com.iontorrent.utils.io.FileUtils;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class PGMAcquisitionGlobalHeader {

    public static final int MAX_CHANNELS = 4;
    private long signature; // uint32
    private long version; // uint32
    /** header size	unsigned int32 	size in bytes of the header */
    private long headerSize;
    /** data size	unsigned int32 	size in bytes of the data portion  */
    private long dataSize;
    public static final long DAT_HEADER_SIGNATURE = 0xDEADBEEFL;
    public static final int DAT_HEADER_UNINTERLACED = 0;
    public static final int DAT_HEADER_INTERLACED = 4;

    private PGMHeader header;
    
  

    protected void read(DataInputStream in) {
        if (in == null ) {
            err("NO input stream!");
            return;
        }
        try {
            signature = FileUtils.getUInt32(in);
            if (getSignature() != DAT_HEADER_SIGNATURE) {
                err("Signature must be " + Long.toHexString(DAT_HEADER_SIGNATURE) + ", but was " + Long.toHexString(getSignature()));
            }
            setVersion(FileUtils.getUInt32(in));

            headerSize = FileUtils.getUInt32(in);
            dataSize = FileUtils.getUInt32(in);
            if (getVersion() == 4) {
                header = new PGMAcquisitionRegionHeader();
            }
            else if(getVersion() == 3) {
                header = new PGMAcquisitionHeader();
            }
            else {
                err("Unknown header version: "+getVersion());
            }
            header.read(in);
         //   p("read header: "+toString());

        } catch (IOException ex) {
            err("Could not read header info of acquisition", ex);
        }
    }
    public boolean isRegionFormat() {
        return getVersion() == 4;
    }
    
    @Override
    public String toString() {
        return "GlobalHeader: headerSize=" + getHeaderSize() + ", version=" + getVersion() + " , dataSize=" + getDataSize()+
                "\n"+header.toString();
    }

    /**
     * @return the signature
     */
    public long getSignature() {
        return signature;
    }

    /**
     * @return the version
     */
    public long getVersion() {
        return version;
    }

    /**
     * @return the headerSize
     */
    public long getHeaderSize() {
        return headerSize;
    }

    /**
     * @return the dataSize
     */
    public long getDataSize() {
        return dataSize;
    }

    private void err(String msg, Exception ex) {
        Logger.getLogger(PGMAcquisitionGlobalHeader.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(PGMAcquisitionGlobalHeader.class.getName()).log(Level.SEVERE, msg);
    }

    private void p(String msg) {
        System.out.println("PGMAcquisitionGlobalHeader: " + msg);
        //Logger.getLogger( PGMAcquisitionGlobalHeader.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @param version the version to set
     */
    public void setVersion(long version) {
        this.version = version;
    }

    /**
     * @return the header
     */
    public PGMHeader getHeader() {
        return header;
    }

    public int getNrFrames() {
        return header.getNrFrames();
    }

    public int getNrCols() {
        return header.getNrCols();
    }

    public int getNrRows() {
       return header.getNrRows();
    }

    public int getInterlacetype() {
        return header.getInterlacetype();
    }
}
