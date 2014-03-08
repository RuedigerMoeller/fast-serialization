package de.ruedigermoeller.serialization.serializers;

/**
 * Created by ruedi on 07.03.14.
 */
/*
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 *
 */

import de.ruedigermoeller.serialization.FSTBasicObjectSerializer;
import de.ruedigermoeller.serialization.FSTClazzInfo;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import de.ruedigermoeller.serialization.util.FSTUtil;

import java.io.IOException;
import java.util.*;


import de.ruedigermoeller.serialization.*;
import de.ruedigermoeller.serialization.util.FSTUtil;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 10.11.12
 * Time: 15:55
 * To change this template use File | Settings | File Templates.
 */
public class FSTArrayListSerializer extends FSTBasicObjectSerializer {

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        ArrayList col = (ArrayList)toWrite;
        int size = col.size();
        out.writeCInt(size);
        Class[] possibleClasses = referencedBy.getPossibleClasses();
        for (int i = 0; i < size; i++) {
            Object o = col.get(i);
            out.writeObjectInternal(o, possibleClasses);
        }
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        try {
            int len = in.readCInt();
            ArrayList res = new ArrayList(len);
            in.registerObject(res, streamPositioin,serializationInfo, referencee);
            Class[] possibleClasses = referencee.getPossibleClasses();
            for ( int i = 0; i < len; i++ ) {
                final Object o = in.readObjectInternal(possibleClasses);
                res.add(o);
            }
            return res;
        } catch (Throwable th) {
            throw FSTUtil.rethrow(th);
        }
    }
}
