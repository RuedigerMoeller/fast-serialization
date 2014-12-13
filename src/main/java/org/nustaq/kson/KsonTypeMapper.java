/*
 * Copyright 2014 Ruediger Moeller.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nustaq.kson;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

/**
 * simple implementation of type mapper.
 *  * Maps Classes to short string names and vice versa.
 *  * allows to add user-defined type conversions (e.g. Date, Collections)
 *
 *  This default implementation supports Date<=>String and Collections<=>Array coercion.
 */
public class KsonTypeMapper {

    public static final Object NULL_LITERAL = "NULL";
    protected boolean useSimplClzName = true;
    protected HashMap<String,Class> typeMap = new HashMap<String, Class>();
    protected HashMap<Class, String> reverseTypeMap = new HashMap<Class, String>();

    protected DateFormat dateTimeInstance = DateFormat.getDateTimeInstance();;

    public KsonTypeMapper() {
        map("map", HashMap.class).map("list", HashMap.class).map("set",HashSet.class);
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

    public KsonTypeMapper map(String name, Class c) {
        typeMap.put(name, c);
        reverseTypeMap.put(c,name);
        return this;
    }

    public KsonTypeMapper map(Object ... stringAndClasses) {
        for (int i = 0; i < stringAndClasses.length; i+=2) {
            map( stringAndClasses[i], stringAndClasses[i+1]);
        }
        return this;
    }

    public KsonTypeMapper map(Class ... c) {
        for (int i = 0; i < c.length; i++) {
            Class aClass = c[i];
            map(aClass.getSimpleName(),aClass);
        }
        return this;
    }

    public boolean isUseSimplClzName() {
        return useSimplClzName;
    }

    public void setUseSimplClzName(boolean useSimplClzName) {
        this.useSimplClzName = useSimplClzName;
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
        if (type==null)
            return readObject;
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
                if ( type.isInterface() ) {
                    if ( List.class.isAssignableFrom(type) ) {
                        type = ArrayList.class;
                    } else if (Map.class.isAssignableFrom(type) ) {
                        type = HashMap.class;
                    }
                }
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
        } else if ( (type == char.class || Character.class.isAssignableFrom(type)) && readObject instanceof String ) {
            return ((String) readObject).charAt(0);
        }
        return readObject;
    }

    public DateFormat getDateTimeInstance() {
        return dateTimeInstance;
    }

    public void setDateTimeInstance(DateFormat dateTimeInstance) {
        this.dateTimeInstance = dateTimeInstance;
    }

    public Object mapLiteral(String type) {
        if (type.equals("null")) {
            return NULL_LITERAL;
        }
        if (type.equals("true") || type.equals("yes") || type.equals("y")) {
            return Boolean.TRUE;
        }
        if (type.equals("false") || type.equals("no") || type.equals("n")) {
            return Boolean.FALSE;
        }
        return null;
    }

    public String getStringForType(Class<? extends Object> aClass) {
        String res = reverseTypeMap.get(aClass);
        if (res==null)
            res = useSimplClzName ? aClass.getSimpleName() : aClass.getName();
        return res;
    }
}
