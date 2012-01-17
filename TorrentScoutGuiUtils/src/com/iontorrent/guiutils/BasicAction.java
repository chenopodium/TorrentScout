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
package com.iontorrent.guiutils;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.KeyStroke;

/**
 *
 * @author Chantal Roth
 */
public abstract class BasicAction extends AbstractAction {
	protected JButton btn;

	protected JButton smallbtn;

	protected JButton iconbtn;

	protected Component parent;

	protected String name;

	protected boolean toolbarAction = true;

	protected KeyStroke keyStroke;

	public BasicAction(String name, Icon icon, Component parent) {
		super(name, icon);
		this.name = name;
		this.parent = parent;
		btn = new JButton(name, icon);
		smallbtn = new JButton(icon);
		iconbtn = new JButton(icon);
		init();
	}

	public BasicAction(String name, Icon icon) {
		this(name, icon, null);
	}

	public void setName(String n) {
	 
	    this.name = n;
	    btn.setName(n);
	}
	public void setIcon(Icon icon) {
        putValue(Action.SMALL_ICON, icon);
		smallbtn.setIcon(icon);
	}

	public BasicAction(String name) {
		this(name, null, null);
	}
	
	public BasicAction cloneAction() {
		return null;
	}

	private void init() {
		smallbtn.setMargin(new Insets(1, 1, 1, 1));
		smallbtn.setBorderPainted(false);
		iconbtn.setMargin(new Insets(1, 1, 1, 1));
		btn.addActionListener(this);
		//btn.setMargin(new Insets(1, 1, 1, 1));

		smallbtn.addActionListener(this);
		iconbtn.addActionListener(this);
		smallbtn.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent evt) {
				smallbtn.setBorderPainted(true);
			}

			public void mouseExited(MouseEvent evt) {
				smallbtn.setBorderPainted(false);
			}
		});

	}

	public String getToolTipText() {
		return btn.getToolTipText();
	}

	public void setToolTipText(String s) {
		btn.setToolTipText(s);
		smallbtn.setToolTipText(s);
		iconbtn.setToolTipText(s);
	}

	public JButton getButton() {
		return btn;
	}

	public JButton getIconButton() {
		return iconbtn;
	}

	public JButton getSmallButton() {
		return smallbtn;
	}

	public String getName() {
		return name;
	}

	public abstract void actionPerformed(ActionEvent e);

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		btn.setEnabled(enabled);
		smallbtn.setEnabled(enabled);
		iconbtn.setEnabled(enabled);
	}

	public boolean isToolbarAction() {
		return toolbarAction;
	}

	public void setToolbarAction(boolean b) {
		toolbarAction = b;
	}

	public void setKeyStroke(KeyStroke keystroke) {
		this.keyStroke = keystroke;
	}

	public KeyStroke getKeyStroke() {
		return keyStroke;
	}
}
