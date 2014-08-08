package org.nustaq.konfigkaiser;


import org.nustaq.serialization.FSTClazzInfo;

import java.lang.reflect.Array;
import java.util.*;

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
public class KKDeserializer {

    protected KKCharInput in;
    protected KKTypeMapper mapper;

    public KKDeserializer(KKCharInput in, KKTypeMapper mapper) {
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

    public Object readObject( Class expect, Class genericKeyType, Class genericValueType ) throws Exception {
        try {
            skipWS();
            String type = readId();
            Object literal = mapper.mapLiteral(type);
            if (literal!=null)
                return literal;
            skipWS();
            Class mappedClass = null;
            if ( "".equals(type) ) {
                mappedClass = expect;
            } else {
                mappedClass = mapper.getType(type);
            }
            if ( mappedClass == null ) {
                return type; // assume string
            }
            if ( mappedClass == List.class || mappedClass == Collection.class )
                mappedClass = ArrayList.class;
            if ( mappedClass == Map.class )
                mappedClass = HashMap.class;
            FSTClazzInfo clInfo = KonfigKaiser.conf.getCLInfoRegistry().getCLInfo(mappedClass);
            int ch = in.readChar();
            if ( ch != '{' ) {
                throw new KonfigParseException("expected '{'", in);
            }
            Object res = null;
            if ( Map.class.isAssignableFrom(clInfo.getClazz()) ) {
                res = clInfo.newInstance(true);
                List keyVals = readList(genericKeyType, genericValueType);
                for (int i = 0; i < keyVals.size(); i+=2 ) {
                    Object fi = keyVals.get(i);
                    Object val = keyVals.get(i+1);
                    ((Map)res).put(fi,val);
                }
            } else if (Collection.class.isAssignableFrom(clInfo.getClazz())) {
                List keyVals = readList(genericKeyType, genericKeyType);
                if ( clInfo.getClazz() == ArrayList.class ) { // default constructor fails ...
                    res = new ArrayList<>(keyVals.size());
                } else {
                    res = clInfo.newInstance(true);
                }
                for (int i = 0; i < keyVals.size(); i++) {
                    Object o = keyVals.get(i);
                    ((Collection)res).add(o);
                }
            } else if ( clInfo.getClazz().isArray() ) {
                Class componentType = clInfo.getClazz().getComponentType();
                if ( componentType.isArray() )
                    new KonfigParseException("nested arrays not supported",in);
                List keyVals = readList(componentType, componentType);
                res = Array.newInstance(componentType,keyVals.size());
                for (int i = 0; i < keyVals.size(); i++) {
                    Array.set(res,i,keyVals.get(i));
                }
            } else {
                res = clInfo.newInstance(true);
                List keyVals = readObjectFields(clInfo);
                for (int i = 0; i < keyVals.size(); i+=2 ) {
                    String fi = (String) keyVals.get(i);
                    Object val = keyVals.get(i+1);
                    clInfo.getFieldInfo(fi,null).getField().set(res,val);
                }
            }
            return res;
        } catch (Exception ex) {
            throw new KonfigParseException("unexpected error, tried reading object",in, ex);
        }
    }

    protected List readObjectFields( FSTClazzInfo targetClz ) throws Exception {
        ArrayList result = new ArrayList();
        skipWS();
        while ( in.peekChar() > 0 && in.peekChar() != '}' ) {

            String field = (String) readValue(String.class,null,null);
            result.add(field);
            if ( in.peekChar() == ':' ) {
                in.readChar(); // skip
                skipWS();
            }
            FSTClazzInfo.FSTFieldInfo fieldInfo = targetClz.getFieldInfo(field, null);
            Class type = fieldInfo == null ? null : fieldInfo.getType();
            if ( fieldInfo != null ) {
                result.add(readValue(type, KonfigKaiser.fumbleOutGenericKeyType(fieldInfo.getField()), KonfigKaiser.fumbleOutGenericValueType(fieldInfo.getField())));
            } else {
                System.out.println("No such field '"+field+"' on class "+targetClz.getClazz().getName());
            }
            skipWS();
        }
        in.readChar(); // consume }
        return result;
    }

    protected List readList( Class keyType, Class valueType ) throws Exception {
        ArrayList result = new ArrayList();
        skipWS();
        boolean expectKey = true;
        while ( in.peekChar() > 0 && in.peekChar() != '}' ) {
            if ( expectKey ) {
                result.add(readValue(keyType, null, null ));
                expectKey = !expectKey;
            } else {
                if ( in.peekChar() == ':' ) {
                    in.readChar(); // skip
                    skipWS();
                }
                result.add(readValue(valueType, null, null ));
                expectKey = !expectKey;
            }
            skipWS();
        }
        in.readChar(); // consume }
        return result;
    }

    protected Object readValue( Class expected, Class genericKeyType, Class genericValueType ) throws Exception {
        skipWS();
        int ch = in.peekChar();
        if ( ch == '"' || ch == '\'' || isFromStringValue(expected) ) {
            // string
            return mapper.coerceReading(expected, readString(ch == '"' || ch == '\''));
        } else if ( Character.isLetter(ch) || ch == '{' ) {
            // object
            return readObject(expected, genericKeyType, genericValueType );
        } else if ( Character.isDigit(ch) || ch == '+' || ch == '-' || ch == '.' ) {
            Class type = expected;
            if ( type == float.class || type == double.class ) {
                // .. extra slow for float&double. fixme: optimize this
                String num = readNums();
                double val = Double.parseDouble(num);
                if ( type == double.class ) {
                    return val;
                } else if ( type == float.class ) {
                    return (float)val;
                } else if ( type == String.class ) {
                    return "" + val;
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
                    return l != 0;
                } else if ( type == byte.class || type == Byte.class ) {
                    byte b = (byte) (((l + 256) & 0xff) - 256);
                    return b;
                } else if ( type == char.class || type == Character.class) {
                    return (char) l;
                } else if ( type == short.class || type == Short.class) {
                    return (short) l;
                } else if ( type == int.class || type == Integer.class) {
                    return (int) l;
                } else if ( type == long.class || type == Long.class) {
                    return l;
                } else if ( type == String.class) {
                    return "" + l;
                } else {
                    return l;
                }
            }
        }
        throw new KonfigParseException("value expected",in);
    }

    protected boolean isFromStringValue(Class type) {
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
                (!quoted && ch > 32 && ch != '#' && ch != '}' && ch != ':' && ch != ',' && ! Character.isWhitespace(ch) ) ) {
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

}
