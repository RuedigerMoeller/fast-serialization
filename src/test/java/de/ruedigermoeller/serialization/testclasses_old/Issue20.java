package de.ruedigermoeller.serialization.testclasses_old;

import com.sun.xml.internal.ws.encoding.soap.SerializationException;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import de.ruedigermoeller.serialization.testclasses.enterprise.SimpleOrder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
 * Date: 19.12.13
 * Time: 20:58
 * To change this template use File | Settings | File Templates.
 */
public class Issue20 {

    public byte[] serialize(java.io.Serializable obj){
        ByteArrayOutputStream bout = new ByteArrayOutputStream(16*1024);
        FSTObjectOutput out = new FSTObjectOutput(bout);
        byte[] bytes = null;
        try {
            out.writeObject(obj);
            out.flush();
            bytes = bout.toByteArray();


        } catch (IOException ex) {
            ex.printStackTrace(System.out);
            throw new SerializationException(ex);
        }finally{
            bout = null;
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }
    public java.lang.Object deserialize(byte[] objectData){
        FSTObjectInput in = null;
        try {
            ByteArrayInputStream bIn = new ByteArrayInputStream(objectData);
            in = new FSTObjectInput(bIn);
            return in.readObject();
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            throw new SerializationException(ex);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String arg[]) {
        Issue20 test = new Issue20();
        byte[] hello = test.serialize(SimpleOrder.generateOrder(27));
        Object obj = test.deserialize(hello);
        System.out.println("res "+obj);
    }

}
