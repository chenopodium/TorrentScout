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
package com.iontorrent.utils.args;

import java.util.*;


/**
 * This is an abstract class representing an argument which configures
 * some other class.  Subclasses should implement validity checking for
 * specific types, i.e. integers, dates, strings, etc.  The class can
 * generate a string describing itself suitable for command-line usage
 * descriptions.  The value can be set directly or from a set of
 * java.util.Properties.  This allows a class to be configured by another class
 * without needing to know the specific details of the class being configured,
 * i.e. what properties it has and how to set them.
 */
public abstract class Argument {
    private String name;
    private Object value;
    private Object defaultValue;
    private boolean required;
    private String description;

    /**
     * A list of arguments that depend on this one.  This means simply that
     * if this argument has a value, then all dependents must also have
     * a value.
     */
    private List dependents;

    public Argument(String name) {
        this(name, null);
    }

    /**
     * Construct a new Argument with the given name and default value.
     * @param name name of the new argument
     * @param defaultValue the value that will be used if
     */
    public Argument(String name, Object defaultValue) {
        if(name == null) throw new NullPointerException("name cannot be null");
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public void setValue(Properties argumentSet) {
        setValue(argumentSet.getProperty(this.name));
    }

    public void setValue(String value) {
        this.value = validateArgument(value);
    }

    public Object getValue() {
        return value == null ? getDefaultValue()
                             : value;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getName() {
        return name;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isRequired() {
        return required;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Add an argument that is dependent on this one.  This means simply
     * that if this argument has a value, then all arguments added to this
     * argument as dependents must also have a value.
     * @param dependent
     */
    public void addDependent(Argument dependent) {
        if(dependents == null) dependents = new ArrayList();
        dependents.add(dependent);
    }

    /**
     * If this argument is set, check whether dependent arguments are set
     * as well.  If not throw an IllegalArgumentException.  If this argument
     * is not set or all dependent arguments are set, return quietly.
     * @throws IllegalArgumentException if this argument is set and one or more
     *         dependent arguments are not set.
     */
    public void checkDependents() throws IllegalArgumentException {
        if(!isSet() || dependents == null) return;
        for(int i = 0; i < dependents.size(); i++) {
            Argument dependent = (Argument)dependents.get(i);
            if(!dependent.isSet()) {
                throw new IllegalArgumentException("The argument '" + getName() +
                    "' is set and thus requires the argument '" +
                    dependent.getName() + "' to be set as well.");
            }
        }
    }

    /**
     * Return a string describing how to use this argument on the command-line.
     * The method uses the style argument to produce a message tailored to a
     * specific command-line style.
     * @arg argumentStyle The command-line style that should be used in the
     *      usage string returned by this method
     * @returns a one-line string describing how to use this argument on the
     *          command-line including default values and whether required.
     */
    public String getUsageDescription(CommandLineStyle argumentStyle) {
        StringBuffer usage = new StringBuffer();
        usage.append(getArgumentDescriptor(argumentStyle));
        if (description != null) {
            usage.append(" ").append(description);
        }
        if (defaultValue != null) {
            usage.append(" (default=").append(defaultValue).append(")");
        }
        usage.append(" (required=");
        usage.append(isRequired() && getDefaultValue() == null).append(")");
        if(dependents != null && dependents.size() > 0) {
            usage.append(" (If this argument is set, the following must also be set: ");
            for(int i = 0; i < dependents.size(); i++) {
                Argument dependent = (Argument)dependents.get(i);
                if(i > 0) usage.append(", ");
                usage.append(dependent.getName());
            }
            usage.append(")");
        }
        return usage.toString();
    }

    public boolean isSet() {
        return value != null;
    }

    /**
    * Subclasses must override this to return a string stating the type
    * of their argument, i.e. integer, date, etc.  This is intended only
    * for use in display strings, not in implementing conditional logic.
    * @return a string stating the argument type.
    */
    public abstract String getTypeDescriptor();

    /**
     * This method is called whenever the value of this argument is set to
     * ensure the value is valid.
     * @param value the value to validate
     * @returns an <code>Object</code> representing the real value of this
     *          argument or null if the argument is null
     * @throws IllegalArgumentException if the value is null or valid
     */
    protected Object validateArgument(String value)
        throws IllegalArgumentException {
        if (value == null) {
            if ((getDefaultValue() == null) && isRequired()) {
                throw new IllegalArgumentException("Value of this required " +
                    "argument (" + getName() + ") cannot be null");
            }
            return null;
        }
        return validateArgumentValue(value);
    }

    /**
     * Subclasses must override this to check if the value is valid.  If not,
     * an <code>IllegalArgumentException</code> should be thrown.  If valid,
     * an object appropriate to the subclass type is returned to represent
     * the actual value.  The given argument will never be null.
     * @param value the value to validate
     * @returns an <code>Object</code> representing the real value of this
     *          argument.
     * @throws IllegalArgumentException if the value is not valid
     */
    protected abstract Object validateArgumentValue(String value);

    protected String getArgumentDescriptor(CommandLineStyle argumentStyle) {
        if (argumentStyle != null) {
            return argumentStyle.format(name, getTypeDescriptor());
        } else {
            return getDefaultArgumentDescriptor();
        }
    }

    private String getDefaultArgumentDescriptor() {
        return name.toUpperCase();
    }
}
