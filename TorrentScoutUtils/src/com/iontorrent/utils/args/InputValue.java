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

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InputValue {

    private String def = null;
    private String question = null;
    private String type = null;
    private Object value = null;
    private String name = null;
    private boolean valid = false;
    private boolean required = true;
    private boolean set = false;
    private String cmdline = null;
    private boolean isFile;
    private String description;

    public InputValue(String name, String question) {
        this(name, question, "String", null);
    }

    public InputValue(String name) {
        this(name, null, "String", null);
    }

    public InputValue(String name, String question, String type) {
        this(name, question, type, null);
    }

    public InputValue(String name, String question, String type, String def) {
        this(name, question, type, def, "-" + name, true);
    }

    public InputValue(String name, String question, String type, String def, String cmdline, boolean required) {
        this.def = def;
        if (question == null) {
            question = "what is the value for " + name;
        }
        if (type == null) {
            type = "String";
        }
        this.type = type;
        this.question = question;
        this.name = name;
        if (cmdline == null) {
            this.cmdline = "-" + name;
        } else {
            this.cmdline = cmdline;
        }
        setRequired(required);
        value = null;
    }

// *****************************************************************
// GET/SET
// *****************************************************************
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getCmdLine() {
        return cmdline;
    }

    public void setCmdLine(String cmd) {
        this.cmdline = cmd;
    }

    public String getQuestion() {
        return question;
    }

    public String getDefault() {
        return def;
    }

    public String getName() {
        return name;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean b) {
        this.valid = b;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean b) {
        this.required = b;
    }

    public boolean isSet() {
        return set;
    }

    public void setSet(boolean b) {
        this.set = b;
    }

    public String toString() {
        return question + "/" + type + "/" + def + "/" + name;
    }

// *****************************************************************
// CHECK
// *****************************************************************
    public boolean checkInput(String value) {
        if (value == null) {
            setValue(null);
            return false;
        }
        boolean valid = false;
        String type = getType();
        if (type.equalsIgnoreCase("string")) {
            setValue(value);
            valid = true;
        } else if (type.equalsIgnoreCase("file")) {
            File f = new File(value);
            if (f.exists()) {
                setValue(value);
                valid = true;
            } else {
                valid = false;
                err("File " + value + " does not exist");
            }
        } else if (type.equalsIgnoreCase("boolean")) {
            boolean res = value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("y");
            setValue(new Boolean(res));
            valid = true;
        } else if (type.startsWith("int")) {
            int d = 0;
            try {
                d = Integer.parseInt(value);
                valid = true;
            } catch (Exception e) {
                valid = false;
            }
            setValue(new Integer(d));
        } else if (type.equalsIgnoreCase("float")) {
            float f = 0;
            try {
                f = Float.parseFloat(value);
                valid = true;
            } catch (Exception e) {
                valid = false;
            }
            setValue(new Float(f));
        } else if (type.equalsIgnoreCase("double")) {
            double f = 0;
            try {
                f = Double.parseDouble(value);
                valid = true;
            } catch (Exception e) {
                valid = false;
            }
            setValue(new Double(f));
        }
        setValid(valid);
        if (!valid) {
            setValue(null);
        } else {
            this.setSet(true);
        }
        return valid;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean isFile) {
        this.isFile = isFile;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // *****************************************************************
// LOG
// *****************************************************************
    private void err(String msg, Exception ex) {
        Logger.getLogger(InputValue.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        Logger.getLogger(InputValue.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(InputValue.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("InputValue: " + msg);
        //Logger.getLogger( InputValue.class.getName()).log(Level.INFO, msg, ex);
    }
}
