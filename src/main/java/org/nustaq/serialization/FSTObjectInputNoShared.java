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

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;

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
        getCodec().reset();
        getCodec().setInputStream(in);
    }

    public void resetForReuseCopyArray(byte bytes[], int off, int len) throws IOException {
        if ( closed ) {
            throw new RuntimeException("can't reuse closed stream");
        }
        getCodec().reset();
        getCodec().resetToCopyOf(bytes, off, len);
    }

    protected Object instantiateAndReadNoSer(Class c, FSTClazzInfo clzSerInfo, FSTClazzInfo.FSTFieldInfo referencee, int readPos) throws Exception {
        Object newObj;
        newObj = clzSerInfo.newInstance(getCodec().isMapBased());
        if (newObj == null) {
            throw new IOException(referencee.getDesc() + ":Failed to instantiate '" + c.getName() + "'. Register a custom serializer implementing instantiate or define empty constructor..");
        }
        if ( clzSerInfo.isExternalizable() )
        {
            getCodec().attemptReadAhead(readExternalReadAHead);
            ((Externalizable)newObj).readExternal(this);
            getCodec().readExternalEnd();
        } else {
            FSTClazzInfo.FSTFieldInfo[] fieldInfo = clzSerInfo.getFieldInfo();
            readObjectFields(referencee, clzSerInfo, fieldInfo, newObj,0,0);
        }
        return newObj;
    }


}
