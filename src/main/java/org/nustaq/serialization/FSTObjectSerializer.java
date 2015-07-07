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
package org.nustaq.serialization;

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

    public static final String REALLY_NULL = "REALLY_NULL";
    /**
     * write the contents of a given object
     */
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition)
            throws IOException;

    /**
     * read the content to an already instantiated object
     */
    public void readObject(FSTObjectInput in, Object toRead, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy)
        throws Exception;

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
    public Object instantiate(Class objectClass, FSTObjectInput fstObjectInput, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition)
        throws Exception;
}
