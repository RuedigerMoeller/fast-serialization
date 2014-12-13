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

import org.nustaq.serialization.FSTConfiguration;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Scanner;

/**
 * a simple text <=> object serialization. More readable than JSon, less complication/error prone than Yaml.
 * <p>
 * Main use is configuration files.
 * - Supports Pojo's only.
 * - No multidimensional or nested arrays.
 * - No untyped Arrays (e.g. Object x[] = new byte[] { 1, 2})
 * - Collections: Map and List. Use Generics to type collections (can omit type tags then)
 * Example from fast-cast:
 * <pre>
 *     public static ClusterConf readFrom( String filePath ) throws Exception {
 *       return (ClusterConf) new Kson()
 *              .map(PublisherConf.class, SubscriberConf.class, TopicConf.class, ClusterConf.class)
 *              .readObject(new File(filePath));
 *   }
 * </pre>
 */
public class Kson {

    public static FSTConfiguration conf = FSTConfiguration.createStructConfiguration().setForceClzInit(true);

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
            Type actualTypeArgument = null;
            if (actualTypeArguments != null && actualTypeArguments.length > 0 )
                actualTypeArgument = actualTypeArguments[0];
                if ( actualTypeArgument instanceof Class == false ) {
                    while ( actualTypeArgument instanceof ParameterizedType ) {
                        actualTypeArgument = ((ParameterizedType) actualTypeArgument).getRawType();
                    }
                    if ( actualTypeArgument instanceof Class == false )
                        return null;
                }
                return (Class<?>) actualTypeArgument;
        }
        return null;
    }

    public static Class fumbleOutGenericValueType(Field field) {
        Type genericType = field.getGenericType();
        if ( genericType instanceof ParameterizedType ) {
            ParameterizedType params = (ParameterizedType) genericType;
            Type[] actualTypeArguments = params.getActualTypeArguments();
            Type actualTypeArgument = null;
            if (actualTypeArguments != null && actualTypeArguments.length > 1 ) {
                actualTypeArgument = actualTypeArguments[1];
                if ( actualTypeArgument instanceof Class == false ) {
                    while ( actualTypeArgument instanceof ParameterizedType ) {
                        actualTypeArgument = ((ParameterizedType) actualTypeArgument).getRawType();
                    }
                    if ( actualTypeArgument instanceof Class == false )
                        return null;
                }
            }
            return (Class<?>) actualTypeArgument;
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
        return readObject(file,(String)null);
    }

    public Object readObject(File file, Class type) throws Exception {
        return readObject(file,type.getName());
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
