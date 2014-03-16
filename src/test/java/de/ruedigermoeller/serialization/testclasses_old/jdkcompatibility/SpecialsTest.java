package de.ruedigermoeller.serialization.testclasses_old.jdkcompatibility;

import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

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
 * Date: 03.08.13
 * Time: 16:35
 * To change this template use File | Settings | File Templates.
 */
public class SpecialsTest {

    static class ToWrite implements Serializable{
        String dummy = "empty";

        public ToWrite(String dummy) {
            this.dummy = dummy;
        }

        public Object writeReplace() {
            return new ToRead(dummy);
        }
    }

    static class ToRead implements Serializable {
        String dummy;

        public ToRead(String dummy) {
            this.dummy = dummy;
        }

        public Object readResolve() {
            return dummy;
        }
    }

    public static void exceptionTest(FSTConfiguration conf) throws IOException, ClassNotFoundException {
        FSTObjectOutput out = conf.getObjectOutput();
        Exception e;
        try {
            throw new Exception("Test");
        } catch (Exception ex) {
            e = ex;
        }
        out.writeObject(e);
        out.flush();
        FSTObjectInput in = new FSTObjectInput(conf);
        in.resetForReuseUseArray(out.getBuffer(),0,out.getWritten());
        Object ex = in.readObject();
        System.out.println("success "+ex);
    }

    public static void main(String[]s) throws IOException, ClassNotFoundException {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        exceptionTest(conf);


        ToWrite w = new ToWrite("bla");

        byte b[] = null;
        FSTObjectOutput out = new FSTObjectOutput(conf);
        out.writeObject(w);
        out.flush();
        b = out.getBuffer();

        FSTObjectInput in = new FSTObjectInput(conf);
        in.resetForReuseUseArray(b,0,b.length);
        Object res = in.readObject();

        if ( !res.equals("bla") ) {
            throw new RuntimeException("fail "+res);
        }

    }

}
