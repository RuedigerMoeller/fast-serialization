/*
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
package org.nustaq.serialization.serializers;

import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.util.FSTUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jakub Kubrynski
 */
public class FSTUnmodifiableCollectionSerializer extends FSTCollectionSerializer {

    public static final Class<?> UNMODIFIABLE_COLLECTION_CLASS;
    public static final Class<?> UNMODIFIABLE_LIST_CLASS;
    public static final Class<?> UNMODIFIABLE_SET_CLASS;

    static {
        UNMODIFIABLE_COLLECTION_CLASS = Collections.unmodifiableCollection(new ArrayList()).getClass();
        UNMODIFIABLE_LIST_CLASS = Collections.unmodifiableList(new ArrayList()).getClass();
        UNMODIFIABLE_SET_CLASS = Collections.unmodifiableSet(new HashSet()).getClass();
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
        try {
            int len = in.readInt();
            if (UNMODIFIABLE_LIST_CLASS.isAssignableFrom(objectClass)) {
                List res = new ArrayList(len);
                fillArray(in, serializationInfo, referencee, streamPosition, res, len);
                return Collections.unmodifiableList(res);
            }
            if (UNMODIFIABLE_SET_CLASS.isAssignableFrom(objectClass)) {
                Set res = new LinkedHashSet(len);
                fillArray(in, serializationInfo, referencee, streamPosition, res, len);
                return Collections.unmodifiableSet(res);
            }
            if (UNMODIFIABLE_COLLECTION_CLASS.isAssignableFrom(objectClass)) {
                Collection res = new ArrayList(len);
                fillArray(in, serializationInfo, referencee, streamPosition, res, len);
                return Collections.unmodifiableCollection(res);
            }
        } catch (Throwable th) {
            FSTUtil.<RuntimeException>rethrow(th);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void fillArray(FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition, Object res, int len) throws ClassNotFoundException, IOException, IllegalAccessException, InstantiationException {
        in.registerObject(res, streamPosition, serializationInfo, referencee);
        Collection col = (Collection) res;
        if (col instanceof ArrayList) {
            ((ArrayList) col).ensureCapacity(len);
        }
        for (int i = 0; i < len; i++) {
            final Object o = in.readObjectInternal(null);
            col.add(o);
        }
    }
}
