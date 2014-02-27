package de.ruedigermoeller.serialization.testclasses.libtests;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.UnsafeInput;
import com.esotericsoftware.kryo.io.UnsafeOutput;
import de.ruedigermoeller.serialization.testclasses.jdkcompatibility.Swing;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;

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
 * Date: 16.06.13
 * Time: 10:35
 * To change this template use File | Settings | File Templates.
 */
public class KryoUnsafeTest extends SerTest {
    static Kryo kryo = new Kryo();

    public KryoUnsafeTest(String title) {
        super(title);
    }

    public String getColor() {
        return "#A0A0A0";
    }

    Input out;
    @Override
    protected void readTest(ByteArrayInputStream bin, Class cl) {
        if ( out ==null )
            out = new UnsafeInput(bin);
        else
            out.setInputStream(bin);
        Object res = kryo.readObject(out,cl);
        out.close();
        resObject = res;
    }

    Output output;
    @Override
    protected void writeTest(Object toWrite, OutputStream bout, Class aClass) {
        if ( output == null )
            output = new UnsafeOutput(bout);
        else
            output.setOutputStream(bout);
        kryo.writeObject(output, toWrite);
        output.close();
    }

}
