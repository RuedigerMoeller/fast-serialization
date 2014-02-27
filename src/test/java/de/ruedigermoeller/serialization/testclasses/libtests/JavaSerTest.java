package de.ruedigermoeller.serialization.testclasses.libtests;

import java.io.*;

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
 * Time: 10:40
 * To change this template use File | Settings | File Templates.
 */
public class JavaSerTest extends SerTest {

    public JavaSerTest(String title) {
        super(title);
    }

    public String getColor() {
        return "#c0c0c0";
    }

    @Override
    protected void readTest(ByteArrayInputStream bin, Class cl) {
        try {
            ObjectInputStream out = new ObjectInputStream(bin);
            Object res = out.readObject();
            out.close();
            resObject = res;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    protected void writeTest(Object toWrite, OutputStream bout, Class aClass) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(bout);
            out.writeObject(toWrite);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
