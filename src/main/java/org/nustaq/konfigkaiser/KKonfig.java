package org.nustaq.konfigkaiser;

import org.nustaq.serialization.FSTConfiguration;

import java.io.*;
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
public class KKonfig {

    public static FSTConfiguration conf = FSTConfiguration.createStructConfiguration();

    KKTypeMapper mapper;

    public KKonfig(KKTypeMapper mapper) {
        this.mapper = mapper;
    }

    public KKonfig() {
        this(new KKTypeMapper());
    }

    public KKonfig map(String name, Class c) {
        mapper.map(name,c);
        return this;
    }

    public KKonfig map(Class c) {
        mapper.map(c);
        return this;
    }

    public Object readObject(String dson) throws Exception {
        KKStringCharInput in = new KKStringCharInput(dson);
        return new KKDeserializer(in, mapper).readObject(null, null, null);
    }

    public Object readObject(File file) throws Exception {
        FileInputStream fin = new FileInputStream(file);
        try {
            return readObject(fin, "UTF-8");
        } finally {
            fin.close();
        }
    }

    public Object readObject(InputStream stream, String encoding) throws Exception {
        return readObject(new Scanner(stream, encoding).useDelimiter("\\A").next());
    }

}
