package org.nustaq.serialization.dson;


import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTConfiguration;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

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
 * Date: 20.12.13
 * Time: 01:16
 * To change this template use File | Settings | File Templates.
 */
public class DsonDeserializer {

    protected DsonCharInput in;
    protected DsonTypeMapper mapper;

    public DsonDeserializer(DsonCharInput in, DsonTypeMapper mapper) {
        this.in = in;
        this.mapper = mapper;
    }

    protected void skipWS() {
        int ch = in.readChar();
        while ( ch >= 0 && Character.isWhitespace(ch) ) {
            ch = in.readChar();
        }
        if ( ch == '#' ) {
            ch=in.readChar();
            while ( ch >= 0 && ch != '\n' ) {
                ch = in.readChar();
            }
            skipWS();
        } else if ( ch > 0 )
            in.back(1);
    }

    public Object readObject() throws Exception {
        try {
            skipWS();
            String type = readId();
            Object literal = mapper.mapLiteral(type);
            if (literal!=null)
                return literal;
            skipWS();
            Class mappedClass = mapper.getType(type);
            if ( mappedClass == null ) {
                return type; // assume string
            }
            FSTClazzInfo clInfo = FSTConfiguration.getDefaultConfiguration().getCLInfoRegistry().getCLInfo(mappedClass);
            int ch = in.readChar();
            if ( ch != '{' ) {
                //throw new RuntimeException("expected { at "+in.position());
                in.back(1); // use ';' as terminator
            }
            String implied = null;
            if (in.peekChar()==':') { // implied attribute
                implied = mapper.getImpliedAttr(mappedClass, type);
                if (implied==null)
                    throw new DsonParseException("expected implied attribute",in);
                skipWS();
            }
            Object res = clInfo.newInstance(true);
            readFields( implied, res, clInfo);
            return res;
        } catch (Exception ex) {
            throw new DsonParseException("unexpected error, tried reading object",in, ex);
        }
    }

    protected void readFields(String implied, Object target, FSTClazzInfo clz ) throws Exception {
        skipWS();
        while ( in.peekChar() > 0 && in.peekChar() != '}' && in.peekChar() != ';' && in.peekChar() != ']' ) {
            String name = readId();
            if (name.length()==0 && implied != null)
                name = implied;
            skipWS();
            int ch = in.readChar();
            FSTClazzInfo.FSTFieldInfo fieldInfo = clz.getFieldInfo(name, null);
            if ( fieldInfo == null ) {
                throw new RuntimeException("no such field:"+name+" on class "+clz.getClazz().getName());
            }
            if ( ch == ':' ) {
                // key val
                skipWS();
                if ( implied != null && name.equals(implied) && in.peekChar() == '{' ) // allow to use {} on implieds
                    in.readChar();
                readValue(target, fieldInfo);
            } else {
                if (fieldInfo.getType() == boolean.class ) {
                    fieldInfo.setBooleanValue(target,true); in.back(1);
                } else {
                    // scalar ? => error
                    throw new DsonParseException("expected key value",in);
                }
            }
            skipWS();
        }
        in.readChar(); // consume }
    }

    protected void readValue( Object target, FSTClazzInfo.FSTFieldInfo field) throws Exception {
        skipWS();
        int ch = in.peekChar();
        if ( ch == '"' || ch == '\'' || (field != null && isStringValue(field.getType())) ) {
            // string
            field.getField().set(target, mapper.coerceReading(field.getType(), readString(ch == '"' || ch == '\'')));
        } else if ( ch == '[' ) {
            // primitive array
            readArray(target, field);
        } else if ( Character.isLetter(ch) ) {
            // object
            field.setObjectValue(target, readObject());
        } else if ( Character.isDigit(ch) || ch == '+' || ch == '-' || ch == '.' ) {
            Class type = field.getType();
            if ( type == float.class || type == double.class ) {
                // .. extra slow for float&double. fixme: optimize this
                String num = readNums();
                double val = Double.parseDouble(num);
                if ( type == double.class ) {
                    field.setDoubleValue(target,val);
                } else if ( type == float.class ) {
                    field.setFloatValue(target,(float)val);
                } else if ( type == String.class ) {
                    field.setObjectValue(target, "" + val);
                } else {
                    throw new RuntimeException("cannot assign floating point to "+type.getName());
                }
            } else {
                // num
                boolean neg = false;
                if ( ch == '+' ) {
                    in.readChar();
                } else if ( ch == '-' ) {
                    neg = true;
                    in.readChar();
                }
                long l = readLong() * (neg?-1:1);
                if (type == boolean.class ) {
                    field.getField().set(target,l != 0);
                } else if ( type == byte.class || type == Byte.class ) {
                    byte b = (byte) (((l + 256) & 0xff) - 256);
                    field.getField().set(target, b);
                } else if ( type == char.class || type == Character.class) {
                    field.getField().set(target, (char) l);
                } else if ( type == short.class || type == Short.class) {
                    field.getField().set(target, (short) l);
                } else if ( type == int.class || type == Integer.class) {
                    field.getField().set(target, (int) l);
                } else if ( type == long.class || type == Long.class) {
                    field.getField().set(target, l);
                } else if ( type == String.class) {
                    field.getField().set(target, "" + l);
                } else if ( type == Object.class ) {
                    field.getField().set(target, l);
                } else {
                    throw new RuntimeException("cannot assign number to "+type.getName());
                }
            }
        }
    }

    protected void readArray(Object target, FSTClazzInfo.FSTFieldInfo field) throws Exception {
        in.readChar(); // consume [
        skipWS();
        int ch = in.peekChar();
        ArrayList objects = new ArrayList();
        while ( ch != ']' && ch > 0 ) {
            // some code redundancy for efficiency (avoid objects for default key:val
            if ( ch == '"' || ch == '\'' || (field!=null && isStringValue(field.getType().getComponentType() ) ) ) {
                // string
                objects.add(readString(ch == '"' || ch == '\''));
            } else if ( isIdStart(ch) ) {
                // object
                objects.add(readObject());
            } else if ( Character.isDigit(ch) || ch == '+' || ch == '-' || ch == '.' ) {
                // num
                boolean neg = false;
                if ( ch == '+' ) {
                    in.readChar();
                } else if ( ch == '-' ) {
                    neg = true;
                    in.readChar();
                }
                long l = readLong() * (neg?-1:1);
                if ( in.peekChar() == '.' ) {
                    String num = l + readNums();
                    objects.add(Double.parseDouble(num));
                } else {
                    objects.add(l);
                }
            } else {
                throw new RuntimeException("could not parse '"+in.getString(in.position()-10,10)+"' expected array elements or ]");
            }
            skipWS();
            ch = in.peekChar();
            if ( ch == ',' || ch == '>' || ch == ':' ) {
                in.readChar();skipWS();
                ch = in.peekChar();
            }
        }
        in.readChar();
        Class arrayType = field.getArrayType();
        if ( arrayType == null ) {
            arrayType = Object.class;
        }
        Object array = Array.newInstance(arrayType, objects.size());
        for (int i = 0; i < objects.size(); i++) {
            Object o = objects.get(i);
            Class type = arrayType;
            if (type == boolean.class ) {
                if ( o instanceof Boolean )
                    ((boolean[])array)[i] = ((Boolean)o).booleanValue();
                else
                    ((boolean[])array)[i] = ((Number)o).intValue() != 0;
            } else if ( type == byte.class ) {
                ((byte[])array)[i] = ((Number)o).byteValue();
            } else if ( type == char.class ) {
                Character val = (Character) mapper.coerceReading(char.class, o);
                ((char[])array)[i] = val;
            } else if ( type == short.class ) {
                ((short[])array)[i] = ((Number)o).shortValue();
            } else if ( type == int.class ) {
                ((int[])array)[i] = ((Number)o).intValue();
            } else if ( type == long.class ) {
                ((long[])array)[i] = ((Number)o).longValue();
            } else if ( type == float.class ) {
                ((float[])array)[i] = ((Number)o).floatValue();
            } else if ( type == double.class ) {
                ((double[])array)[i] = ((Number)o).doubleValue();
            } else {
                Array.set(array, i, mapper.coerceReading(type, o));
            }
        }
        field.getField().set(target,mapper.coerceReading(field.getType(), array));
    }

    protected boolean isStringValue(Class type) {
        return type == String.class ;
    }

    protected long readLong() {
        long res = 0;
        long fak = 1;
        int ch = in.readChar();
        while ( Character.isDigit(ch) ) {
            res += (ch-'0')*fak;
            fak*=10;
            ch = in.readChar();
        }
        in.back(1);
        long reverse = 0;
        while (res != 0) {
            reverse = reverse * 10+ (res % 10);
            res = res / 10;
        }
        return reverse;
    }

    protected String readString(boolean quoted) {
        StringBuilder b = new StringBuilder(15);
        int end = quoted ? in.readChar() : ' '; // " or '
        int ch = in.readChar();
        while( (quoted && ch != end) ||
                (!quoted && ch > 32 && ch != '#' && ch != '}' && ch != ']' && ch != ';' && ch != ',' && ! Character.isWhitespace(ch) ) ) {
            if ( ch == '\\' ) {
                ch = in.readChar();
                switch (ch) {
                    case '\\':
                        b.append(ch);   break;
                    case '"':
                        b.append('"'); break;
                    case '/':
                        b.append('/'); break;
                    case 'b':
                        b.append('\b'); break;
                    case 'f':
                        b.append('\f'); break;
                    case 'n':
                        b.append('\n'); break;
                    case 'r':
                        b.append('\r'); break;
                    case 't':
                        b.append('\t'); break;
                    case 'u':
                        b.append("\\u"+(char)in.readChar()+(char)in.readChar()+(char)in.readChar()+(char)in.readChar()); break;
                    default:
                        throw new RuntimeException("unknown escape "+(char)ch+" in "+in.position());
                }
            } else {
                b.append((char)ch);
            }
            ch = in.readChar();
        }
        if ( ! quoted ) {
            in.back(1);
        }
        return b.toString();
    }

    protected String readNums() {
        skipWS();
        int pos = in.position();
        int ch = in.readChar();
        while( Character.isDigit(ch) || ch == '.' || ch == 'E' ||ch == 'e' || ch=='+' || ch=='-' ) {
            ch = in.readChar();
        }
        in.back(1);
        return in.getString(pos, in.position() - pos);
    }

    protected String readId() {
        skipWS();
        int pos = in.position();
        int ch = in.readChar();
        while( isIdPart(ch) && ch != ':' ) {
            ch = in.readChar();
        }
        in.back(1);
        return in.getString(pos, in.position() - pos);
    }

    protected boolean isIdPart(int ch) {
        return Character.isLetterOrDigit(ch) || ch == '$' || ch == '#' || ch == '_';
    }

    protected boolean isIdStart(int ch) {
        return Character.isLetter(ch) || ch == '$' || ch == '#' || ch == '_';
    }

    public static void main(String a[]) throws Exception {
        DsonTypeMapper mapper = new DsonTypeMapper();
        mapper
           .map("cond", UD.class).map("and", UD.class).map("or", UD.class).map("attr", UD.class)
           .map("select", Sel.class).map("subscribe", Sub.class)
           .implyAttrFromType("and", "and").implyAttrFromType("or", "or").implyAttrFromType("attr", "attr")
           .implyAttrFromType("select", "cond").implyAttrFromType("subscribe", "cond");
        DsonDeserializer ser = null;// new DsonDeserializer(new DsonStringCharInput(s),mapper);

        DataInputStream in = new DataInputStream( new FileInputStream("c:\\tmp\\test.dson") );
        StringBuilder b = new StringBuilder();
        int c = 0;
        try {
            do {
                c =  in.read();
                if ( c >= 0 )
                    b.append((char)c);
            } while( c >= 0 );
        } catch (Exception e) {}
        String file = b.toString();
        long tim = System.currentTimeMillis();
        Object read = null;
        for ( int i = 0; i < 1; i++ ) {
            ser = new DsonDeserializer(new DsonStringCharInput(file),mapper);
            read = ser.readObject();
        }
//        System.out.println(System.currentTimeMillis()-tim);
//        System.out.println();
        new DsonSerializer(new DsonPSCharOut(System.out),mapper).writeObject(read);
//        System.out.println();
//        System.out.println();
//        new DsonSerializer(new DsonPSCharOut(System.out),mapper).writeObject(new UD());
    }


    public static class UD {
        String attr;
        Object greater;
        String contains;
        Object lesser;
        Object equals;
        Object greaterEq;
        Object lesserEq;
        UD and[];
        UD or[];
        boolean negate;
    }

    public static class Sel {
        UD cond;
    }

    public static class Sub {
        UD cond;
    }

}
