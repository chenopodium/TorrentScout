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
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Vector;

/**
 *
 * @author Chantal Roth
 */

public interface Drawable {

// ***************************************************************************
// DRAWING
// ***************************************************************************

	public void draw(Graphics g);

	public void clear(Graphics g);

	public Color getForeground();

	public void setForeground(Color c);

	public int getLayer();
// ***************************************************************************
// NEW POSITIONS
// ***************************************************************************
	public Point getPosition();

	public void setPosition(Point p);

	public Rectangle getBounds();

	public Dimension getSize();

// ***************************************************************************
// POSITIONS
// ***************************************************************************
	/** @deprecated */
	public Dimension getAbsoluteSize();

	/** @deprecated */
	public Point getAbsolutePosition();

	/** @deprecated */
	public void setAbsolutePosition(Point p);

	public void move(int dx, int dy);

	public int getX();

	public int getY();

	public int getWidth();

	public int getHeight();
// ***************************************************************************
// ATTRIBUTES
// ***************************************************************************
	public boolean isMovable();

	public boolean isSelectable();

	public boolean isStatic();

	public boolean isText();

	public ArrayList<Drawable> getDrawables();

	public boolean isVisible();

	public void setVisible(boolean vis);

// ***************************************************************************
// SELECTIONS
// ***************************************************************************


	public void setSelected(boolean b);

	public void setSelected(boolean b, boolean sendevent);

	public boolean isSelected();

// ***************************************************************************
// OTHER
// ***************************************************************************

	/** returns true, if the point p (a absolute position) is contained in this object (determinded
	 * using the absolute position and absolute size
	 */
	public boolean containsPoint(Point p);

	public boolean overlaps(Rectangle rect);

	public String toString();

	public String toHtml();

	public String getToolTipText(java.awt.event.MouseEvent evt);


}
