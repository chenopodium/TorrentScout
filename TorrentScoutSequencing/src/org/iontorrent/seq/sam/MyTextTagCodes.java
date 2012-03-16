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

package org.iontorrent.seq.sam;



import net.sf.samtools.util.Iso8601Date;
import net.sf.samtools.util.StringUtil;
import net.sf.samtools.util.DateParser;

import java.util.Map;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.SAMFormatException;

/**
 * Converter between SAM text representation of a tag, and in-memory Object representation.
 * Note that this class is not thread-safe, in that some local variables have been made into instance
 * variables in order to reduce object creation, but it should not ever be the case that the same
 * instance is used in multiple threads.
 */
class MyTextTagCodes {
    private static final int NUM_TAG_FIELDS = 3;

    /**
     * This is really a local variable of decode(), but allocated here to reduce allocations.
     */
    private final String[] fields = new String[NUM_TAG_FIELDS];

    /**
     * This is really a local variable of decodeTypeAndValue(), but allocated here to reduce allocations.
     */
    private final String[] typeAndValueFields = new String[NUM_TAG_FIELDS - 1];

//    /**
//     * Convert in-memory representation of tag to SAM text representation.
//     * @param tagName Two-character tag name.
//     * @param value Tag value as approriate Object subclass.
//     * @return SAM text String representation, i.e. name:type:value
//     */
//    String encode(final String tagName, Object value) {
//        final StringBuilder sb = new StringBuilder(tagName);
//        sb.append(':');
//        char tagType = BinaryTagCodec.getTagValueType(value);
//        switch (tagType) {
//            case 'c':
//            case 'C':
//            case 's':
//            case 'S':
//            case 'I':
//                tagType = 'i';
//        }
//        if (tagType == 'H') {
//            value = StringUtil.bytesToHexString((byte[])value);
//        } else if (tagType == 'i') {
//            final long longVal = ((Number) value).longValue();
//            if (longVal > Integer.MAX_VALUE || longVal < Integer.MIN_VALUE) {
//                throw new SAMFormatException("Value for tag " + tagName + " cannot be stored in an Integer: " + longVal);
//            }
//        }
//        sb.append(tagType);
//        sb.append(':');
//        sb.append(value.toString());
//        return sb.toString();
//    }

    /**
     * Encode a standard tag, which should not have a type field.
     * @param tagName 2-character String.
     * @param value Not necessarily a String.  Some of these are integers but the type is implied by
     * the tagName.  Converted to String with toString().
     * @return Colon-separated text representation suitable for a SAM header, i.e. name:value.
     */
    String encodeUntypedTag(final String tagName, final Object value) {
        final StringBuilder sb = new StringBuilder(tagName);
        sb.append(':');
        sb.append(value.toString());
        return sb.toString();
    }

    /**
     * Convert typed tag in SAM text format (name:type:value) into tag name and Object value representation.
     * @param tag SAM text format name:type:value tag.
     * @return Tag name as 2-character String, and tag value in appropriate class based on tag type.
     */
    Map.Entry<String, Object> decode(final String tag) {
        final int numFields = StringUtil.splitConcatenateExcessTokens(tag, fields, ':');
        if (numFields != MyTextTagCodes.NUM_TAG_FIELDS) {
            throw new SAMFormatException("Not enough fields in tag '" + tag + "'");
        }
        final String key = fields[0];
        final String type = fields[1];
        final String stringVal = fields[2];
        final Object val = convertStringToObject(type, stringVal);
        return new Map.Entry<String, Object>() {
            public String getKey() {
                return key;
            }

            public Object getValue() {
                return val;
            }

            public Object setValue(final Object o) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Similar to decode() method above, but the tag name has already been stripped off.
     * @param typeAndValue type:string-value, or, for backward-compatibility, just string-value.
     * @return Value converted into the appropriate type.
     */
    Object decodeTypeAndValue(final String typeAndValue) {
        // Allow colon in tag value
        final int numFields = StringUtil.splitConcatenateExcessTokens(typeAndValue,  typeAndValueFields, ':');
        if (numFields == 1) {
            // For backward compatibility, if no colon, treat as String type
            return typeAndValue;
        }
        return convertStringToObject(typeAndValueFields[0], typeAndValueFields[1]);
    }

    private Object convertStringToObject(final String type, final String stringVal) {
        final Object val;
        if (type.equals("Z")) {
            val = stringVal;
        } else if (type.equals("A")) {
            if (stringVal.length() != 1) {
                throw new SAMFormatException("Tag of type A should have a single-character value");
            }
            val = stringVal.charAt(0);
        } else if (type.equals("i")) {
            try {
                val = new Integer(stringVal);
            } catch (NumberFormatException e) {
                throw new SAMFormatException("Tag of type i should have signed decimal value");
            }
        } else if (type.equals("f")) {
            try {
                val = new Float(stringVal);
            } catch (NumberFormatException e) {
                throw new SAMFormatException("Tag of type f should have single-precision floating point value");
            }
        } else if (type.equals("H")) {
            try {
                val = StringUtil.hexStringToBytes(stringVal);
            } catch (NumberFormatException e) {
                throw new SAMFormatException("Tag of type H should have valid hex string with even number of digits");
            }
        } else {
            throw new SAMFormatException("Unrecognized tag type: " + type);
        }
        return val;
    }

    Iso8601Date decodeDate(final String dateStr) {
        try {
            return new Iso8601Date(dateStr);
        } catch (DateParser.InvalidDateException ex) {
            try {
                return new Iso8601Date(DateFormat.getDateTimeInstance().parse(dateStr));
            } catch (ParseException e) {
                try {
                    return new Iso8601Date(new Date(dateStr));
                } catch (Exception e1) {
                    throw new DateParser.InvalidDateException("Could not parse as date: " + dateStr, e);
                }
            }
        }
    }

/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( MyTextTagCodes.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( MyTextTagCodes.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( MyTextTagCodes.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
//  System.out.println("MyTextTagCodes: " + msg);
        //Logger.getLogger( MyTextTagCodes.class.getName()).log(Level.INFO, msg, ex);
    }
}
