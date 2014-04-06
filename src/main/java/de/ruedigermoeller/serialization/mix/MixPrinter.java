package de.ruedigermoeller.serialization.mix;

import java.io.PrintStream;
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
 * Date: 05.04.2014
 * Time: 22:05
 * 
 * implemented as sidebar to avoid mixing printing into read/write code (easier to port)
 * 
 */
public class MixPrinter {

    public static void printMessage(Object readMessageObject, PrintStream out) {
        new MixPrinter().prettyPrintStreamObject(readMessageObject,out,"");
        out.println();
    }

    public static void printMessage( byte[] message, PrintStream out ) {
        MixIn in = new MixIn(message,0);
        printMessage(in.readValue(), out);
    }

    protected void prettyPrintAtom( Mix.Atom at, PrintStream out, String indent) {
        out.print("#" + at.getName() + "(" + at.getId() + ")");
    }

    protected void prettyPrintStreamObject( Object o, PrintStream out, String indent) {
        if (o instanceof Mix.Atom) {
            prettyPrintAtom((Mix.Atom) o, out, indent+"  ");
        } else if (o instanceof Mix.Tupel) {
            prettyPrintTupel((Mix.Tupel) o, out, indent + "  ");
        }
        else
            out.print(objectToString(o));
    }

    protected void prettyPrintTupel( Mix.Tupel t, PrintStream out, String indent) {
        prettyPrintStreamObject(t.getId(),out,indent);
        out.println(" {");
        Object content[] = t.getContent(); 
        for (int i = 0; i < content.length; i++) {
            Object o = content[i];
            if ( t.isStrMap() ) {
                if ( (i&1) == 1 ) {
                    prettyPrintStreamObject( o, out, indent );
                    out.println();
                } else {
                    out.print(indent+"  ");
                    prettyPrintStreamObject(o, out, indent);
                    out.print(" : ");
                }
            } else {
                out.print(indent+"  ");
                prettyPrintStreamObject(o, out, indent);
                out.println();
            }
        }
        out.print(indent + "}");
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
