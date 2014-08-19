package org.nustaq.serialization;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;

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
 * Date: 03.03.14
 * Time: 20:07
 */

 /** 
 * Subclass optimized for "unshared mode". Cycles and Objects referenced more than once will not be detected.
 * Additionally JDK compatibility is not supported (read/writeObject and stuff). Use case is highperformance
 * serialization of plain cycle free data (e.g. messaging). Can perform significantly faster (20-40%).
 */
public class FSTObjectInputNoShared extends FSTObjectInput {

    public FSTObjectInputNoShared() throws IOException {
    }

    public FSTObjectInputNoShared(FSTConfiguration conf) {
        super(conf);
        conf.setShareReferences(false);
    }

    /**
     * Creates a FSTObjectInput that uses the specified
     * underlying InputStream.
     *
     * @param in the specified input stream
     */
    public FSTObjectInputNoShared(InputStream in) throws IOException {
        super(in);
    }

    /**
     * Creates a FSTObjectInput that uses the specified
     * underlying InputStream.
     * <p/>
     * Don't create a FSTConfiguration with each stream, just create one global static configuration and reuseit.
     * FSTConfiguration is threadsafe.
     *
     * @param in   the specified input stream
     * @param conf
     */
    public FSTObjectInputNoShared(InputStream in, FSTConfiguration conf) {
        super(in, conf);
    }

    public void registerObject(Object o, int streamPosition, FSTClazzInfo info, FSTClazzInfo.FSTFieldInfo referencee) {
        return;
    }

    public void resetForReuse(InputStream in) throws IOException {
        if ( closed ) {
            throw new RuntimeException("can't reuse closed stream");
        }
        codec.reset();
        codec.setInputStream(in);
    }

    public void resetForReuseCopyArray(byte bytes[], int off, int len) throws IOException {
        if ( closed ) {
            throw new RuntimeException("can't reuse closed stream");
        }
        codec.reset();
        codec.resetToCopyOf(bytes, off, len);
    }

    protected Object instantiateAndReadNoSer(Class c, FSTClazzInfo clzSerInfo, FSTClazzInfo.FSTFieldInfo referencee, int readPos) throws Exception {
        Object newObj;
        newObj = clzSerInfo.newInstance(codec.isMapBased());
        if (newObj == null) {
            throw new IOException(referencee.getDesc() + ":Failed to instantiate '" + c.getName() + "'. Register a custom serializer implementing instantiate.");
        }
        if ( clzSerInfo.isExternalizable() )
        {
            codec.ensureReadAhead(readExternalReadAHead);
            ((Externalizable)newObj).readExternal(this);
            codec.readExternalEnd();
        } else {
            FSTClazzInfo.FSTFieldInfo[] fieldInfo = clzSerInfo.getFieldInfo();
            readObjectFields(referencee, clzSerInfo, fieldInfo, newObj,0,0);
        }
        return newObj;
    }


}
