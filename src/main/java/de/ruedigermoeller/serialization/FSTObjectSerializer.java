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
package de.ruedigermoeller.serialization;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 10.11.12
 * Time: 12:33
 * To change this template use File | Settings | File Templates.
 */

/**
 * @see FSTBasicObjectSerializer
 */
public interface FSTObjectSerializer {
    /**
     * write the contents of a given object
     */
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition)
            throws IOException;

    /**
     * read the content to an already instantiated object
     */
    public void readObject(FSTObjectInput in, Object toRead, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy)
            throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException;

    /**
     * useful if you register for a class and its subclasses, but want to exclude a specific subclass
     */
    public boolean willHandleClass(Class cl);

    /**
     * @return true if FST can skip a search for same instances in the serialized ObjectGraph. This speeds up reading and writing and makes
     * sense for short immutable such as Integer, Short, Character, Date, .. . For those classes it is more expensive (CPU, size) to do a lookup than to just
     * write the Object twice in case.
     */
    public boolean alwaysCopy();

    /**
     * return null to delegate object instantiation to FST. If you want to implement object instantiation yourself, usually you leave the readObject method empty
     * and handle instantiation and reading the object here. You must call registerObjectForWrite immediately after creating it on the FSTObjectInput
     */
    public Object instantiate(Class objectClass, FSTObjectInput fstObjectInput, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin)
            throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException;
}
