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
 * Created on May 20, 2004
 */
package com.iontorrent.utils.args;

/**
 * This type of argument is used to basically erase an argument of the
 * same name in an existing argument list.  It has an empty usage description
 * and no value.
 * @author dguist
 */
public class EmptyArgument extends Argument {

	/**
	 * Construct an argument with the given name.  An empty argument
	 * is never required.
	 */
	public EmptyArgument(String name) {
		super(name);
	}

	/**
	 * Always returns false.
	 */
	public boolean isRequired() {
		return false;
	}

	/**
	 * Return null.
	 */
	public String getTypeDescriptor() {
		return null;
	}

	/**
	 * Return the empty string.
	 */
	public String getUsageDescription(CommandLineStyle argumentStyle) {
		return "";
	}
	
	/**
	 * Return null.
	 */
	protected Object validateArgumentValue(String value) {
		return null;
	}

}
