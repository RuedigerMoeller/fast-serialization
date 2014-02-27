package de.ruedigermoeller.serialization.dson;

import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
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
 * Time: 00:35
 * To change this template use File | Settings | File Templates.
 */

/**
 * simple implementation of type mapper.
 *  * Maps Classes to short string names and vice versa.
 *  * allows to add user-defined type conversions (e.g. Date, Collections)
 *
 *  This default implementation support for Date<=>String and Collections<=>Array coercion.
 */
public class DsonTypeMapper {

    protected HashMap<String,Class> typeMap = new HashMap<String, Class>();
    protected HashMap<String,String> impliedMap = new HashMap<String, String>();
    protected HashMap<Class,String> reverseTypeMap = new HashMap<Class, String>();

    protected DateFormat dateTimeInstance = DateFormat.getDateTimeInstance();;

    public DsonTypeMapper() {
        map("map", HashMap.class);
    }

    public Class getType(String type) {
        Class res = typeMap.get(type);
        if ( res == null ) {
            try {
                res = Class.forName(type);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        return res;
    }

    public String getStringForType(Class c) {
        String res = reverseTypeMap.get(c);
        if ( res == null ) {
            return c.getName();
        }
        return res;
    }

    public DsonTypeMapper implyAttrFromType(String type, String attr) {
        impliedMap.put(type,attr);
        return this;
    }

    public DsonTypeMapper map(String name, Class c) {
        typeMap.put(name, c);
        if (reverseTypeMap.get(c)==null)
            reverseTypeMap.put(c,name);
        return this;
    }

    public DsonTypeMapper map(Class ... c) {
        for (int i = 0; i < c.length; i++) {
            Class aClass = c[i];
            map(aClass.getSimpleName(),aClass);
        }
        return this;
    }

    /**
     * map given Object to a target type.
     * (needs support in coerceWriting also)
     * Note one could add a pluggable Serializer/Coercer pattern here if required. Skipped for now for simplicity.
     *
     * @param type - of target field
     * @param readObject - object read from string
     * @return
     */
    public Object coerceReading(Class type, Object readObject) {
        // make hashmaps from arrays. warning: for optimal performance, use direct arrays[] only in your serialized classes
        if ( Map.class.isAssignableFrom(type) && readObject.getClass().isArray() ) {
            try {
                Map c = (Map) type.newInstance();
                int len = Array.getLength(readObject);
                for ( int i = 0; i < len; i+=2 ) {
                    c.put(Array.get(readObject, i), Array.get(readObject, i + 1));
                }
                return c;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else // make collections from arrays. warning: for optimal performance, use direct arrays[] only in your serialized classes
            if ( Collection.class.isAssignableFrom(type) && readObject.getClass().isArray() ) {
            try {
                Collection c = (Collection) type.newInstance();
                int len = Array.getLength(readObject);
                for ( int i = 0; i < len; i++ ) {
                    c.add(Array.get(readObject,i));
                }
                return c;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ( Date.class.isAssignableFrom(type) && readObject instanceof String) {
            try {
                return dateTimeInstance.parse((String) readObject);
            } catch (ParseException pe) {
                pe.printStackTrace();
            }
        }
        return readObject;
    }

    /**
     * map given object found in the object graph to another type representation.
     * Useful to map collecitons to arrays and
     * to support user-defined String representations (needs support in coerceReading also).
     *
     * Note one could add a pluggable Serializer/Coercer pattern here if required. Skipped for now for simplicity.
     *
     * @param objectValue
     * @return
     */
    public Object coerceWriting(Object objectValue) {
        if ( objectValue instanceof Map ) {
            Object result[] = new Object[((Map) objectValue).size()*2];
            int i = 0;
            for ( Iterator it = ((Map) objectValue).entrySet().iterator(); it.hasNext(); ) {
                Map.Entry next = (Map.Entry) it.next();
                result[i] = coerceWriting(next.getKey());
                result[i+1] = coerceWriting(next.getValue());
                i += 2;
            }
            return result;
        } else if ( objectValue instanceof Collection ) {
            // make arrays to collections if type impies. 
            // warning for optimal performance, use direct arrays[] only in your serialized classes
            return ((Collection) objectValue).toArray();
        } else if ( objectValue instanceof Date) {
            return dateTimeInstance.format((Date) objectValue);
        }
        return objectValue;
    }

    public DateFormat getDateTimeInstance() {
        return dateTimeInstance;
    }

    public void setDateTimeInstance(DateFormat dateTimeInstance) {
        this.dateTimeInstance = dateTimeInstance;
    }

    public Object mapLiteral(String type) {
        if (type.equals("true") || type.equals("yes") || type.equals("y")) {
            return Boolean.TRUE;
        }
        if (type.equals("false") || type.equals("no") || type.equals("n")) {
            return Boolean.FALSE;
        }
        return null;
    }

    public String getImpliedAttr(Class mappedClass, String type) {
        return impliedMap.get(type);
    }
}
