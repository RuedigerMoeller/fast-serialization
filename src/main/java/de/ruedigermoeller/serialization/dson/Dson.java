package de.ruedigermoeller.serialization.dson;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

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
 * To change this template use File | Settings | File Templates.
 */
public class Dson {

    public static Dson singleton;
    public static AtomicBoolean lock = new AtomicBoolean(false);
    public static DsonTypeMapper defaultMapper = new DsonTypeMapper();

    public static Dson getInstance() {
        if ( singleton != null )
            return singleton;
        while( !lock.compareAndSet(false, true) );
        if ( singleton == null ) {
            singleton = new Dson();
        }
        lock.set(false);
        return singleton;
    }


    public Object readObject( String dson ) throws Exception {
        DsonStringCharInput in = new DsonStringCharInput(dson);
        return new DsonDeserializer(in, defaultMapper).readObject();
    }

    public Object readObject( File file ) throws Exception {
        FileInputStream fin = new FileInputStream(file);
        try {
            return readObject(fin,"UTF-8");
        } finally {
            fin.close();
        }
    }

    public Object readObject( InputStream stream, String encoding ) throws Exception {
        return readObject(new Scanner(stream,encoding).useDelimiter("\\A").next());
    }

    public String writeObject( Object toWrite ) {
        DsonStringOutput out = new DsonStringOutput();
        try {
            new DsonSerializer(out,defaultMapper).writeObject(toWrite);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toString();
    }

    public void writeObject( File file, Object toWrite ) {
    }

    public void writeObject( OutputStream stream, Object toWrite ) {
    }

}
