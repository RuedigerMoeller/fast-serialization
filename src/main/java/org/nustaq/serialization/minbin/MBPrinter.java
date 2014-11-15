package org.nustaq.serialization.minbin;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;

/**
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 * <p/>
 * Date: 13.04.2014
 * Time: 18:08
 * 
 * decodes and prints a MinBin serialized stream
 */
public class MBPrinter {

    public static void printMessage(byte[] binarMsg) {
        printMessage(binarMsg,System.out);
    }

    public static void printMessage(Object readMessageObject, PrintStream out) {
        new MBPrinter().prettyPrintStreamObject(readMessageObject,out,"");
        out.println();
    }

    public static void printMessage( byte[] message, PrintStream out ) {
        MBIn in = new MBIn(message,0);
        printMessage(in.readObject(), out);
    }

	public static String print2String(byte minbinMsg[]) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		MBPrinter.printMessage(minbinMsg, new PrintStream(os));
		try {
			return os.toString("UTF8");
		} catch (UnsupportedEncodingException e) {
			return e.getMessage();
		}
	}

    protected void prettyPrintStreamObject( Object o, PrintStream out, String indent) {
        if (o instanceof MBObject) {
            prettyPrintObject((MBObject) o, out, indent + "  ");
        } else if ( o instanceof MBSequence ) {
            prettyPrintSequence((MBSequence) o, out, indent + "  ");
        } else
            out.print(objectToString(o));
    }

    protected void prettyPrintObject(MBObject t, PrintStream out, String indent) {
        prettyPrintStreamObject(t.getTypeInfo(),out,indent);
        out.println(" {");
        for (java.util.Iterator iterator = t.keyIterator(); iterator.hasNext(); ) {
            String next = (String) iterator.next();
            out.print(indent+"  ");
            prettyPrintStreamObject(next, out, indent);
            out.print(" : ");
            prettyPrintStreamObject( t.get(next), out, indent );
            out.println();
        }
        out.print(indent + "}");
    }

    protected void prettyPrintSequence(MBSequence t, PrintStream out, String indent) {
        prettyPrintStreamObject(t.getTypeInfo(),out,indent);
        out.println(" [");
        for (int i = 0; i < t.size(); i++) {
            out.print(indent+"  ");
            prettyPrintStreamObject(t.get(i), out, indent);
            out.println();
        }
        out.print(indent + "]");
    }

    protected String arrayToString(Object o) {
        int len = Array.getLength(o);
        Class c = o.getClass().getComponentType();
        String res = "[ ";
        for (int i = 0; i < len; i++) {
            if ( c == byte.class ) res+=Array.getByte(o,i);
            if ( c == boolean.class ) res+=Array.getBoolean(o, i);
            if ( c == char.class ) res+=Array.getChar(o, i);
            if ( c == short.class ) res+=Array.getShort(o, i);
            if ( c == int.class ) res+=Array.getInt(o, i);
            if ( c == long.class ) res+=Array.getLong(o, i);
            if ( c == float.class ) res+=Array.getFloat(o, i);
            if ( c == double.class ) res+=Array.getDouble(o, i);
            if ( i < len-1)
                res+=",";
        }
        res += " ]";
        return res;
    }

    protected String objectToString(Object o) {
        if ( o == null ) {
            return "NULL";
        }
        if ( o.getClass().isArray() )
            return arrayToString(o);
        if ( o instanceof String ) {
            return "\""+o+"\"";
        }
        if ( o instanceof Character )
            return "'"+o+"'("+(int)((Character) o).charValue()+")";
        return ""+o;
    }

}
