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
package com.iontorrent.guiutils.zoomcanvas;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Chantal Roth
 */
public class BasicCanvas extends JPanel implements MouseListener, MouseMotionListener {

    private Color color_select = Color.blue;
    private Color color_zoom = Color.red;
    private int startx = -1;
    private int oldmousey = -1;
    private int oldmousex = -1;
    private int mousex = -1;
    private int mousey = -1;
    private int dragx = -1;
    private int dragy = -1;
    private int endx = -1;
    private int endy = -1;
    private int font_height;
    private int font_width;
    private int font_base_line;
    private boolean drawRect = true;
    private boolean drawLine = false;
    private static final int SELECT = 1;
    private static final int ZOOM = 2;
    private int mode = SELECT;
    private boolean firstline = true;
    private DrawIF drawobject = null;

    // ***************************************************************************
    // CONSTRUCTOR
    // ***************************************************************************
    public BasicCanvas() {
        super(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        setBackground(Color.white);
        setToolTipText("canvas");

    }
    // ***************************************************************************
    // GET/SET
    // ***************************************************************************

    public void setDrawable(DrawIF drawobject) {
        this.drawobject = drawobject;
    }

    public DrawIF getDrawable() {
        return drawobject;
    }
    // ***************************************************************************
    // FONT INFO
    // ***************************************************************************

    protected void setFontInfo() {
        FontMetrics fm = getFontMetrics(getFont());
        int font_widths[] = fm.getWidths();
        font_width = -1;
        for (int i = 0; i < font_widths.length; i++) {
            if (font_widths[i] > font_width) {
                font_width = font_widths[i];
            }
        }

        font_height = fm.getHeight();
        font_base_line = fm.getMaxAscent();
    }
    // ***************************************************************************
    // TOOLTIP
    // ***************************************************************************

    public String getToolTipText(MouseEvent evt) {
        Point p = evt.getPoint();
        return p.toString();
    }
    // ***************************************************************************
    // GET/SET
    // ***************************************************************************

    public Point getStart() {
        return new Point(dragx, dragy);
    }

    public Point getEnd() {
        return new Point(endx, endy);
    }

    public void setColorSelect(Color c) {
        color_select = c;
    }

    public void setColorZoom(Color c) {
        color_zoom = c;
    }

    public void setDrawRect(boolean b) {
        drawRect = b;
    }

    public void setDrawLine(boolean b) {
        drawLine = b;
    }

    public void setFont(Font f) {
        super.setFont(f);
        setFontInfo();
    }

    public int getFontWidth() {
        return font_width;
    }

    public int getFontHeight() {
        return font_height;
    }

    public int getFontBaseLine() {
        return font_base_line;
    }

    public int getLineHeight() {
        return font_height;
    }
    // ***************************************************************************
    // DRAWING
    // ***************************************************************************

    private void drawXorRect(Graphics g, int sx, int sy, int dx, int dy) {
        drawXorRect(g, sx, sy, dx, dy, color_select);
    }

    private void drawXorRect(Graphics g, int sx, int sy, int dx, int dy, Color c) {
        //System.out.println("basic canvas: drawxorrect");
        if (!drawRect || drawLine) {
            return;
        }
        int x1 = sx;
        int y1 = sy;

        if (dx < 0) {
            x1 = sx + dx;
            dx = -dx;
        }
        if (dy < 0) {
            y1 = sy + dy;
            dy = -dy;
        }
        g.setColor(c);
        g.setXORMode(Color.white);
//			p("drawing rect:"+x1+", "+y1+", dx:"+dx+", dy:"+dy);

        g.drawRect(x1, y1, dx, dy);
        //	g.drawLine(x1, y1, dx, dy);
        if (drawobject != null) {
            drawobject.setPosition(new Point(x1, y1));
            drawobject.setDimension(new Dimension(dx, dy));
            drawobject.draw(g);
        }
    }

    private void drawXorLine(Graphics g, int sx, int sy, int x, int y) {
        drawXorLine(g, sx, sy, x, y, color_select);
    }

    private void drawXorLine(Graphics g, int sx, int sy, int x, int y, Color c) {
        //System.out.println("basic canvas: drawxorrect");
        if (!drawLine || drawRect) {
            return;
        }

        g.setColor(c);
        g.setXORMode(Color.white);
        g.drawLine(sx, sy, x, y);

    }

    public void clear(Graphics g) {

        if (g == null) {
            return;
        }
        //	System.out.println("basiccanvas: clear");
        g.setColor(Color.white);
        try {
            g.fillRect(0, 0, (int) getSize().getWidth(), (int) getSize().getHeight());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    // ***************************************************************************
    // MOUSE
    // ***************************************************************************
    @Override
    public void mouseClicked(MouseEvent evt) {
    }

    @Override
    public void mousePressed(MouseEvent evt) {
        oldmousex = evt.getX();
        oldmousey = evt.getY();
        dragx = oldmousex;
        dragy = oldmousey;
        endx = dragx;
        endy = dragy;
        boolean shift = evt.isShiftDown() || evt.isMetaDown();
        if (!shift) {
            mode = SELECT;
        } else {
            mode = ZOOM;
        }
    }

    @Override
    public void mouseReleased(MouseEvent evt) {

        if (mousex <= 0) {
            return;
        }
        endx = evt.getX();
        endy = evt.getY();
        int dx = Math.abs(endx - oldmousex);
        int dy = Math.abs(endy - oldmousey);
        Graphics g = getGraphics();
        if (mode == SELECT) {
            drawXorRect(g, oldmousex, oldmousey, mousex - oldmousex, mousey - oldmousey);
            if (drawLine) {
                drawXorLine(g, oldmousex, oldmousey, mousex, mousey);
            }
        } else if (mode == ZOOM) {
            drawXorRect(g, oldmousex, oldmousey, mousex - oldmousex, mousey - oldmousey, color_zoom);
        }
        drawLine = false;
        if (dx < 5 || dy < 5) {
            mousex = -1;
            return;
        }
        g.setPaintMode();
        mousex = -1;
    }

    @Override
    public void mouseEntered(MouseEvent evt) {
    }

    @Override
    public void mouseExited(MouseEvent evt) {
    }

    @Override
    public void mouseMoved(MouseEvent evt) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Graphics g = getGraphics();
        boolean shift = e.isShiftDown() || e.isMetaDown();

        if (mousex > -1) {
            if (mode == SELECT) {
                drawXorRect(g, oldmousex, oldmousey, mousex - oldmousex, mousey - oldmousey);
                drawXorLine(g, oldmousex, oldmousey, mousex, mousey);
            } else if (mode == ZOOM) {
                drawXorRect(g, oldmousex, oldmousey, mousex - oldmousex, mousey - oldmousey, color_zoom);
            }
        }
        mousex = e.getX();
        mousey = e.getY();
        int dx = mousex - oldmousex;
        int dy = mousey - oldmousey;
        if (mode == SELECT) {
            drawXorRect(g, oldmousex, oldmousey, dx, dy);
            drawXorLine(g, oldmousex, oldmousey, mousex, mousey);
        } else if (mode == ZOOM) {
            drawXorRect(g, oldmousex, oldmousey, dx, dy, color_zoom);
        }
    }
    // *****************************************************************
    // TEST
    // *****************************************************************

    public static void main(String args[]) {

        JFrame frame = new JFrame("BasicCanvas Test");

        frame.setSize(400, 400);
        BasicCanvas view = new BasicCanvas();
        frame.getContentPane().add(view);
        frame.show();

    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(BasicCanvas.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(BasicCanvas.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(BasicCanvas.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("BasicCanvas: " + msg);
        //Logger.getLogger( BasicCanvas.class.getName()).log(Level.INFO, msg, ex);
    }
}
