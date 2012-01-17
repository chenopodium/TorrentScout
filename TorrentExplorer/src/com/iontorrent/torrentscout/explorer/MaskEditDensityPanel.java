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
package com.iontorrent.torrentscout.explorer;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.rawdataaccess.wells.BitMask;
import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.wellmodel.BitWellDensity;
import com.iontorrent.wellmodel.WellCoordinate;
import com.iontorrent.wellmodel.WellSelection;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Chantal Roth
 */
public class MaskEditDensityPanel extends MaskDensityPanel {

    BitMask mask;

    public MaskEditDensityPanel(ExperimentContext exp, BitMask mask) {
        super(exp);
        this.mask = mask;
        
        
        //this.imagePanel.seta
    }
    

    @Override
    protected void drawCoords(Graphics2D g, int cols, int maxy, int rows, int maxx) {
        
        drawCoords(g, cols, maxy, rows, maxx, Color.lightGray);
     
        g.setFont(new Font("sans serif", Font.BOLD, 12));
        g.setColor(Color.yellow);
        g.drawString(mask.computePercentage() + "%", BORDER, 25);

    }
    @Override
    protected void afterImageCreated() {
      if (imagePanel != null && mask != null) {
          p("SETTING AREA OFFSETS TO:"+mask.getRelCoord());
            this.imagePanel.setAreaOffsetX(mask.getRelCoord().getCol());
            this.imagePanel.setAreaOffsetY(mask.getRelCoord().getRow());
        }  
      else p("NOT setting area offsts, image is "+imagePanel+", mask is: "+mask);
    }

    @Override
    protected void drawCoords(Graphics2D g, int cols, int maxy, int rows, int maxx, Color coordcolor) {
       // p("Drawing coords");

        int COORDDELTA = 100;
        //if (cols/BUCKET)
        if (this.BUCKET > 39) {
            COORDDELTA = 500;
        } else if (this.BUCKET > 9) {
            COORDDELTA = 200;
        } else if (this.BUCKET == 2) {
            COORDDELTA = 50;
        } else if (this.BUCKET == 1) {
            COORDDELTA = 20;
        }
        if (COORDDELTA * pixperrow / BUCKET < fontsize * 4) {
            // too narrow
            COORDDELTA = COORDDELTA * 2;
        }
        g.setStroke(new BasicStroke(2));
        g.setFont(fcoord);


        int offx = 0;
        int offy = 0;
        if (expcontext != null) {
            offx = expcontext.getColOffset();
            offy = expcontext.getRowOffset();
            //  p(" Got offset:"+offx+"/"+offy);
        }

        offx += mask.getRelCoord().getCol();
        offy += mask.getRelCoord().getRow();

        drawCoords(g, coordcolor, offx, cols, COORDDELTA, maxy, offy, rows, maxx);
    }

    @Override
    public double getValue(int col, int row) {
        int offx = 0;
        int offy = 0;
        if (expcontext != null) {
            offx = expcontext.getColOffset();
            offy = expcontext.getRowOffset();
            //  p(" Got offset:"+offx+"/"+offy);
        }

       
        int d = mask.getMaskAt(col-offx, row-offy);
        return d;

    }
     @Override
     /** cooords are relative to this experiment or block */
    protected ArrayList<WellCoordinate> getCoords(WellSelection sel) {
        if (mask == null) return null;
        ArrayList<WellCoordinate> coords = mask.getAllCoordsWithData(MAX_COORDS,
                sel.getCoord1().getCol(), sel.getCoord1().getRow(), sel.getCoord2().getCol(), sel.getCoord2().getRow());
        return coords;
    }
     @Override
    public String getToolTipText(MouseEvent e) {
        WellCoordinate coord = imagePanel.getCoord(e);
        if (wellcontext == null) {
            return "No well context";
        }
        int offx = 0;
        int offy = 0;
        if (expcontext != null) {
            offx = expcontext.getColOffset();
            offy = expcontext.getRowOffset();
            //  p(" Got offset:"+offx+"/"+offy);
        }

        offx += mask.getRelCoord().getCol();
        offy += mask.getRelCoord().getRow(); 
        int realcol = coord.getX() + offx;
        int realrow = coord.getY() + offy;
        

        // also get value
        return "x/col=" + realcol + ", y/row=" + realrow + ", value "+coord+"=" + imagePanel.getValue(coord.getCol(), coord.getRow());
        //+" (im: "+coord.x+"/"+coord.y+"), chipy: "+(image.getHeight()-coord.y-BORDER)+
        //" ), bucket: "+bucket_size+", pixpercol: "+pixpercol+" BORDER="+BORDER;

    }

    public void setContext(BitMask mask, int bucketsize) {
        this.wellcontext = expcontext.getWellContext();
        this.mask = mask;
        wellDensity = new BitWellDensity(mask, bucketsize);

        if (wellDensity == null) {
            return;
        }
        try {
            //          p("Creating images");
            createAndDrawImage();
        } catch (IOException ex) {
            p(ErrorHandler.getString(ex));
        }
    }
}
