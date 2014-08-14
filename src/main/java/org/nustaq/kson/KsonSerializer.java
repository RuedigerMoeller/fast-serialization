package org.nustaq.kson;

import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTConfiguration;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 * <p>
 * Date: 20.12.13
 * Time: 12:03
 * <p>
 * Created by ruedi on 07.08.2014.
 */
public class KsonSerializer {
    protected KsonCharOutput out;
    protected KsonTypeMapper mapper;
    protected boolean pretty = true;
    protected boolean writeNull = false;
    protected FSTConfiguration conf;

    public KsonSerializer(KsonCharOutput out, KsonTypeMapper mapper, FSTConfiguration conf) {
        this.out = out;
        this.mapper = mapper;
        this.conf = conf;
    }

    public void writeObject(Object o) throws Exception {
        writeObjectInternal(null, null, o, 0);
    }

    protected void writeObjectInternal(Class expectedClass, Class expectedValueClass, Object o, int indent) throws Exception {
        if (o == null) {
            out.writeString("null");
            return;
        }
        if (o instanceof Character) {
            if (indent >= 0) writeIndent(indent);
            final char ch = ((Character) o).charValue();
            if (ch==0)
                System.out.println("POK");
            if (ch <128 && ch > 32)
                out.writeString(o.toString());
            else
                out.writeString(Integer.toString(ch));
            if (indent >= 0) writeln();
        } else if (o instanceof Number || o instanceof Boolean) {
            if (indent >= 0) writeIndent(indent);
            out.writeString(o.toString());
            if (indent >= 0) writeln();
        } else if (o instanceof String) {
            if (indent >= 0) writeIndent(indent);
            writeString((String) o);
            if (indent >= 0) writeln();
        } else if (o instanceof Map) {
            Map map = (Map) o;
            writeIndent(indent);
            out.writeString("{");
            writeln();
            for (Iterator iterator = map.keySet().iterator(); iterator.hasNext(); ) {
                Object next = iterator.next();
                Object value = map.get(next);
                boolean valueSingleLine = isSingleLine(null, value);
                boolean keySingleLine = isSingleLine(null, next);
                if (keySingleLine) {
                    writeIndent(indent + 1);
                    if ( next instanceof String ) {
                        writeKey((String) next);
                    } else
                        writeObjectInternal(expectedClass, null, next, -1);
                    out.writeString(" : ");
                    if (!valueSingleLine)
                        writeln();
                } else {
                    writeObjectInternal(expectedClass, null, next, indent + 1);
                    writeIndent(indent + 1);
                    out.writeString(":");
                    if (!valueSingleLine)
                        writeln();
                }
                if (valueSingleLine) {
                    out.writeChar(' ');
                    writeObjectInternal(expectedValueClass, null, value, -1);
                    if (iterator.hasNext())
                        writeListSep();
                    writeln();
                } else {
                    writeObjectInternal(expectedValueClass, null, value, indent + 1);
                    if (iterator.hasNext())
                        writeListSep();
                }
            }
            writeIndent(indent);
            out.writeChar('}');
            writeln();
        } else if (o instanceof Collection) {
            Collection coll = (Collection) o;
            writeIndent(indent);
            writeListStart();
            writeln();
            for (Iterator iterator = coll.iterator(); iterator.hasNext(); ) {
                Object next = iterator.next();
                writeObjectInternal(expectedClass, null, next, indent + 1);
                if (iterator.hasNext())
                    writeListSep();
            }
            writeIndent(indent);
            writeListEnd();
            writeln();
        } else if (o.getClass().isArray()) {
            writeIndent(indent);
            writeListStart();
            int len = Array.getLength(o);
            boolean lastWasSL = false;
            Class expect = o.getClass().getComponentType();
            for (int ii = 0; ii < len; ii++) {
                Object val = Array.get(o, ii);
                if (isSingleLine(null, val)||isSingleLineCLazz(expect)) {
                    writeObjectInternal(expect, null, val, -1);
                    out.writeChar(' ');
                    if (ii < len-1)
                        writeListSep();
                    lastWasSL = true;
                } else {
                    if (ii == 0)
                        writeln();
                    writeObjectInternal(expect, null, val, indent + 2);
                    if (ii < len-1)
                        writeListSep();
                    lastWasSL = false;
                }
            }
            if (!lastWasSL)
                writeIndent(indent);
            writeListEnd();
            writeln();
        } else {

            writeIndent(indent);
            writeClazzTag(expectedClass, o);
            writeln();

            FSTClazzInfo clInfo = conf.getCLInfoRegistry().getCLInfo(o.getClass());
            FSTClazzInfo.FSTFieldInfo[] fieldInfo = clInfo.getFieldInfo();

            for (int i = 0; i < fieldInfo.length; i++) {
                FSTClazzInfo.FSTFieldInfo fstFieldInfo = fieldInfo[i];
                Class expectedKey = Kson.fumbleOutGenericKeyType(fstFieldInfo.getField());
                Class expectedValue = Kson.fumbleOutGenericValueType(fstFieldInfo.getField());
                Object fieldValue = fstFieldInfo.getField().get(o);
                //              fieldValue = mapper.coerceWriting(fieldValue);
                if (!isNullValue(fstFieldInfo, fieldValue) || writeNull) {
                    writeIndent(indent + 1);
                    final String name = fstFieldInfo.getField().getName();
                    writeKey(name);
                    out.writeChar(':');
                    if (isSingleLine(fstFieldInfo, fieldValue)) {
                        out.writeString(" ");
                        writeObjectInternal(expectedKey, expectedValue, fieldValue, 0);
                        writeListSep();
                    } else {
                        writeln();
                        writeObjectInternal(expectedKey, expectedValue, fieldValue, indent + 2);
                        writeListSep();
                    }
                }
            }
            removeLastListSep();
            writeIndent(indent);
            out.writeChar('}');
            writeln();
        }
    }

    /**
     * called when writing a key of an key:value inside an object or map
     * @param name
     */
    protected void writeKey(String name) {
        out.writeString(name);
    }

    /**
     * determines classname tagging. Overrifing can enforce class tags always or (JSon) write as
     * special attribute
     * @param expectedClass
     * @param o
     */
    protected void writeClazzTag(Class expectedClass, Object o) {
        if (expectedClass == o.getClass()) {
            out.writeString("{");
        } else {
            String stringForType = mapper.getStringForType(o.getClass());
            out.writeString(stringForType + " {");
        }
    }

    protected void writeListEnd() {
        out.writeChar(']');
    }

    protected void writeListStart() {
        out.writeString("[ ");
    }

    // quirksmode: true
    protected void removeLastListSep() {

    }

    protected void writeListSep() {

    }

    private boolean isSingleLine(FSTClazzInfo.FSTFieldInfo fstFieldInfo, Object fieldValue) {
        if (fstFieldInfo == null) {
            if (fieldValue instanceof Class) { // now that's dirty ..
                Class clz = (Class) fieldValue;
                return isSingleLineCLazz(clz);
            }
            return fieldValue!=null && isSingleLineCLazz(fieldValue.getClass());
        }
        if (fstFieldInfo.isArray() && isSingleLine(null, fstFieldInfo.getType().getComponentType())) {
            return true;
        }
        return (fieldValue!=null && isSingleLineCLazz(fieldValue.getClass())) || isSingleLineCLazz(fstFieldInfo.getType());
    }

    private boolean isSingleLineCLazz(Class clz) {
        return String.class.isAssignableFrom(clz) || Number.class.isAssignableFrom(clz) || clz.isPrimitive() || clz == Boolean.class || clz == Character.class;
    }

    static Character zeroC = new Character((char) 0);

    private boolean isNullValue(FSTClazzInfo.FSTFieldInfo fstFieldInfo, Object fieldValue) {
        if (fieldValue==null)
            return true;
        if (writeNull)
            return false;
        if (fstFieldInfo != null ) {
            if (fstFieldInfo.getType().isPrimitive()) {
                if (fieldValue instanceof Number) {
                    return ((Number) fieldValue).doubleValue() == 0.0;
                }
                return fieldValue.equals(zeroC) || fieldValue.equals(Boolean.FALSE);
            }
        }
        return false;
    }

    protected void writeln() {
        if (pretty)
            out.writeChar('\n');
    }

    public void writeString(String string) {
        if (string == null) {
            out.writeString("null");
            return;
        }
        if (string.length() == 0) {
            out.writeString("\"\"");
            return;
        }

        char b;
        char c = 0;
        int i;
        int len = string.length();
        String t;
        boolean ws = shouldQuote(string);

        if (ws)
            out.writeChar('\"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    out.writeChar('\\');
                    out.writeChar(c);
                    break;
                case '/':
                    if (b == '<') {
                        out.writeChar('\\');
                    }
                    out.writeChar(c);
                    break;
                case '\b':
                    out.writeString("\\b");
                    break;
                case '\t':
                    out.writeString("\\t");
                    break;
                case '\n':
                    out.writeString("\\n");
                    break;
                case '\f':
                    out.writeString("\\f");
                    break;
                case '\r':
                    out.writeString("\\r");
                    break;
                default:
                    if (c < ' ' || (c >= '\u0080' && c < '\u00a0') ||
                            (c >= '\u2000' && c < '\u2100')) {
                        t = "000" + Integer.toHexString(c);
                        out.writeString("\\u" + t.substring(t.length() - 4));
                    } else {
                        out.writeChar(c);
                    }
            }
        }
        if (ws)
            out.writeChar('"');
    }

    protected boolean shouldQuote(String string) {
        boolean ws = false;
        for (int ii = 0; ii < string.length(); ii++) {
            final char ch = string.charAt(ii);
            if (Character.isWhitespace(ch) || ch == ',' || ch == ':' || ch == '[' || ch == ']' || ch == '{' || ch == '}') {
                ws = true;
                break;
            }
        }
        return ws;
    }

    protected void writeIndent(int indent) {
        if (pretty) {
            for (int i = 0; i < indent * 2; i++) {
                out.writeChar(' ');
            }
        }
    }

    public boolean isWriteNull() {
        return writeNull;
    }

    public void setWriteNull(boolean writeNull) {
        this.writeNull = writeNull;
    }

    public void writeObject(Object o, Class aClass) throws Exception {
        writeObjectInternal(aClass, null, o, 0);
    }
}
