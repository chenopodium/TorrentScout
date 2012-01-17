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


public class InputValidation {
  // **********************************************************************
// INPUT VALIDATION
// **********************************************************************
  // check if  a string is a non-negative number
  public static boolean isNumber(String str) {
      double target;
        if ( str == null || str.length() < 1 )  return false;
        try {
            Double.parseDouble( str);
        }
        catch ( NumberFormatException ex ) {
            return false;
        }
        return true;
    }

    // check if the string is a positive integer (non-zero)
    public static boolean isInteger(String str) {
      double target;
        if ( str == null || str.length() < 1 )  return false;
        try {
            Integer.parseInt( str);
        }
        catch ( NumberFormatException ex ) {
            return false;
        }
        return true;
    }
    
    public static boolean isBoolean(String str){
        if(str != null && str.length() > 1){
            try {
                Boolean.getBoolean(str);
            }
            catch (Exception e){
                return false;
            }
        }
        return true;   
    }

  


}