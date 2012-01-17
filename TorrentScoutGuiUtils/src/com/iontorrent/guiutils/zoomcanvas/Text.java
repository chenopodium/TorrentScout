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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;

/**
 *
 * @author Chantal Roth
 */
public class Text extends GuiObject implements Drawable {

    protected String text;
    protected int x;
    protected int y;
    private int orig_x;
    private int orig_y;
    protected Font font = new Font("SansSerif", Font.PLAIN, 10);
    protected Font selected_font = new Font("SansSerif", Font.BOLD, 10);
    protected Color color = Color.black;
    //private boolean visible = true;
    protected int font_height = 0;
    protected int font_width = 0;
    private int font_sel_height = 0;
    private int font_sel_width = 0;
    protected int font_base_line = 0;
    private JComponent comp = null;
    public static final int DY = 15;
    // ***************************************************************************
    // FROM DRAWABLE
    // ***************************************************************************

    public Text(String text, int x, int y, Color color) {
        this(text, x, y, color, null);
    }

    public Text(String text, int x, int y) {
        this(text, x, y, null, null);
    }

    public Text(String text, int x, int y, Color color, Font font) {
        super(new Point(x, y));
        this.x = x;
        this.y = y;
        this.orig_x = x;
        this.orig_y = y;
        if (text == null) {
            err("text is null");
            text = "";
        }
        //text = ToolBox.replace(text, "\n", " ");
        this.text = text;
        if (font != null) {
            setFont(font);
        } else {
            setFont(this.font);
        }
        if (color != null) {
            this.color = color;
        }
        setAbsoluteSize(new Dimension(Math.max(50, text.length() * 8), DY));
        setAbsolutePosition(new Point(x, y));
    }

    // ***************************************************************************
    // GET/SET
    // ***************************************************************************
    public boolean isText() {
        return true;
    }

    public void setComponent(JComponent comp) {
        this.comp = comp;
    }

    public JComponent getComponent() {
        return comp;
    }

    public Font getFont() {
        return font;
    }

    public Font getSelectedFont() {
        return selected_font;
    }

    public void setFont(Font f) {
        this.font = f;
        font_width = 0;
        font_height = 0;
        Font sel_f = f.deriveFont(Font.BOLD);
        setSelectedFont(sel_f);
    }

    public void setSelectedFont(Font f) {
        this.selected_font = f;
        font_sel_height = 0;
        font_sel_width = 0;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public Color getColor() {
        return color;
    }

    ;
	public int getOrigX() {
        return orig_x;
    }

    public int getOrigY() {
        return orig_y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    // ***************************************************************************
    // SELECTION
    // ***************************************************************************

    /** Creates a new SelectionEvent and notifies any listeners */
    public void setSelected(boolean b) {
        setSelected(b, true);
    }

    public void setSelected(boolean b, boolean event) {
        super.setSelected(b, event);
        p("i got selected:" + this);
        if (getDrawable() != null && event) {
            getDrawable().setSelected(b, false);
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setColor(Color c) {
        this.color = c;
    }

    public boolean isSelectable() {
        return true;
    }

    public boolean isMovable() {
        return true;
    }

    public boolean isVisible() {
        return visible && isClassVisible();
    }

    public void setVisible(boolean b) {
        visible = b;
    }

    // ***************************************************************************
    // DRAW
    // ***************************************************************************
    public void draw(Graphics g2) {
        //	if (t.isSelected()) c = c.darker();
        clear(g2);
        g2.setColor(color);
        if (isSelected()) {
            g2.setColor(Color.red);
            g2.setFont(selected_font);
        } else {
            g2.setFont(font);
        }
        g2.drawString(text, x, y);
        //	drawBounds(g2);
    }

    public int getFontWidth() {
        return font_width;
    }

    public void calcFontMetrics(Graphics g) {
        FontMetrics fm = g.getFontMetrics(font);
        int font_widths[] = fm.getWidths();
        font_width = -1;
        for (int i = 0; i < font_widths.length; i++) {
            if (font_widths[i] > font_width) {
                font_width = font_widths[i];
            }
        }
        font_height = fm.getHeight() * 3 / 4;
        font_width = (int) (font_width * text.length() * 0.6);
        font_base_line = fm.getMaxAscent();

        setAbsoluteSize(new Dimension(font_width, font_height));
        setAbsolutePosition(new Point(x, y - font_height));
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y - font_height, font_width, font_height);
    }

    public void clear(Graphics g) {
        if (font_width <= 0) {
            calcFontMetrics(g);
        }
        g.setColor(getBackground());
        g.fillRect(x - 1, y - font_height, font_width + 2, font_height);
    }

    public Color getForeground() {
        return color;
    }

    public void setForeground(Color color) {
        this.color = color;
    }

    public String toHtml() {
        return text;
    }

    public String getName() {
        return text;
    }

    public String getDescription() {
        return text;
    }
    // ***************************************************************************
    // OTHER
    // ***************************************************************************

    public void moveTo(int newx, int newy) {
        int dx = newx - x;
        int dy = newy - y;
        move(dx, dy);
    }

    public void move(int dx, int dy) {
        //	System.out.println("Before move: x:"+x+"/y:"+y);
        x += dx;
        y += dy;
        if (comp != null) {
            comp.setLocation(comp.getX() + dx, comp.getY() + dy);
        }
        setAbsolutePosition(new Point(x, y));
        //	System.out.println("After move: x:"+x+"/y:"+y);
    }

    public void moveOrig(int dx, int dy) {
        orig_x += dx;
        orig_y += dy;
    }

    /** This method should be overwritten for any specific instance */
    public String toString() {
        return text;
    }

    public boolean containsPoint(Point p, double factorx, double factory) {
        int x1 = getX();
        double h = getHeight() / factory * 1.5;
        int y1 = (int) (getY() - h);
        int x2 = (int) (x1 + getWidth() / factorx);
        int y2 = (int) (y1 + h);
        p("----------- is text " + this + " at " + p + "?");
        //	p("factory:"+factory+", new height:"+d.getHeight()/factory);

        if (p.getX() >= x1 && p.getX() <= x2 && p.getY() >= y1 && p.getY() <= y2) {
            return true;
        } else {
            return false;
        }
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(Text.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(Text.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(Text.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("Text: " + msg);
        //Logger.getLogger( Text.class.getName()).log(Level.INFO, msg, ex);
    }
}
