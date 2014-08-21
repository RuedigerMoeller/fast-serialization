package org.nustaq.kson;

import org.nustaq.serialization.FSTConfiguration;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Scanner;

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
 * Time: 16:45
 */

/**
 * a simple text <=> object serialization. More readable than JSon, less complication/error prone than Yaml.
 * <p>
 * Main use is configuration files.
 * - Supports Pojo's only.
 * - No multidimensional or nested arrays.
 * - No untyped Arrays (e.g. Object x[] = new byte[] { 1, 2})
 * - Collections: Map and List
 */
public class Kson {

    public static FSTConfiguration conf = FSTConfiguration.createStructConfiguration();

    KsonTypeMapper mapper;

    public Kson(KsonTypeMapper mapper) {
        this.mapper = mapper;
    }

    public Kson() {
        this(new KsonTypeMapper());
    }

    public static Class fumbleOutGenericKeyType(Field field) {
        Type genericType = field.getGenericType();
        if ( genericType instanceof ParameterizedType) {
            ParameterizedType params = (ParameterizedType) genericType;
            Type[] actualTypeArguments = params.getActualTypeArguments();
            if (actualTypeArguments != null && actualTypeArguments.length > 0 && actualTypeArguments[0] instanceof Class)
                return (Class<?>) actualTypeArguments[0];
        }
        return null;
    }

    public static Class fumbleOutGenericValueType(Field field) {
        Type genericType = field.getGenericType();
        if ( genericType instanceof ParameterizedType ) {
            ParameterizedType params = (ParameterizedType) genericType;
            Type[] actualTypeArguments = params.getActualTypeArguments();
            if (actualTypeArguments != null && actualTypeArguments.length > 1  && actualTypeArguments[1] instanceof Class)
                return (Class<?>) actualTypeArguments[1];
        }
        return null;
    }

    public Kson map(String name, Class c) {
        mapper.map(name,c);
        return this;
    }

    /**
     * map to simple name
     * @param c
     * @return
     */
    public Kson map(Class ... c) {
        mapper.map(c);
        return this;
    }

    public Object readObject(String dson) throws Exception {
        KsonStringCharInput in = new KsonStringCharInput(dson);
        return new KsonDeserializer(in, mapper).readObject(null, null, null);
    }

    public Object readObject(String dsonOrJSon, String expectedType, KsonArgTypesResolver resolve) throws Exception {
//        System.out.println(dsonOrJSon);
        KsonStringCharInput in = new KsonStringCharInput(dsonOrJSon);
        final Class type = mapper.getType(expectedType);
        return new KsonDeserializer(in, mapper).setArgTypesRessolver(resolve).readObject(type, String.class, null);
    }

    public Object readObject(String dsonOrJSon, String expectedType) throws Exception {
        if (expectedType == null) {
            return readObject(dsonOrJSon);
        }
        KsonStringCharInput in = new KsonStringCharInput(dsonOrJSon);
        final Class type = mapper.getType(expectedType);
        return new KsonDeserializer(in, mapper).readObject(type, String.class, null);
    }

    public Object readObject(File file) throws Exception {
        return readObject(file,null);
    }

    public Object readObject(File file, String type) throws Exception {
        FileInputStream fin = new FileInputStream(file);
        try {
            return readObject(fin, "UTF-8", type);
        } finally {
            fin.close();
        }
    }

    public Object readObject(InputStream stream, String encoding, String expectedType) throws Exception {
        return readObject(new Scanner(stream, encoding).useDelimiter("\\A").next(), expectedType);
    }

    public String writeObject(Object o) throws Exception {
        KsonStringOutput out = new KsonStringOutput();
        new KsonSerializer(out,mapper, conf).writeObject(o);
        return out.getBuilder().toString();
    }

    public String writeObject(Object o, boolean tagTopLevel) throws Exception {
        KsonStringOutput out = new KsonStringOutput();
        new KsonSerializer(out,mapper, conf).writeObject(o,tagTopLevel?null:o.getClass());
        return out.getBuilder().toString();
    }

    public String writeJSonObject(Object o, boolean tagTopLevel) throws Exception {
        KsonStringOutput out = new KsonStringOutput();
        new JSonSerializer(out,mapper, conf).writeObject(o,tagTopLevel?null:o.getClass());
        return out.getBuilder().toString();
    }

    public String writePlainJSonObject(Object o) throws Exception {
        KsonStringOutput out = new KsonStringOutput();
        new JSonSerializer(out,mapper, conf).noTypeTags().writeObject(o, null);
        return out.getBuilder().toString();
    }

    public KsonTypeMapper getMapper() {
        return mapper;
    }
}
