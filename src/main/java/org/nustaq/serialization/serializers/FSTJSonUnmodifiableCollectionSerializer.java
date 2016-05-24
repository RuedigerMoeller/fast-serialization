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
import org.nustaq.serialization.FSTObjectOutput;
import org.nustaq.serialization.util.FSTUtil;

import java.io.IOException;
import java.util.*;

/**
 * For JSON only, see {@link <a href="https://github.com/RuedigerMoeller/fast-serialization/issues/114">Unable to deserialize unmodifiable collections from JSON</a>}.
 *
 * @author Jakub Kubrynski
 */
public class FSTJSonUnmodifiableCollectionSerializer extends FSTCollectionSerializer {

    public static final Class<?>
        UNMODIFIABLE_COLLECTION_CLASS,
        UNMODIFIABLE_RANDOM_ACCESS_LIST_CLASS,
        UNMODIFIABLE_SET_CLASS,
//        UNMODIFIABLE_SORTED_SET_CLASS,
//        UNMODIFIABLE_NAVIGABLE_SET_CLASS,
        UNMODIFIABLE_LIST_CLASS;

    static {
        UNMODIFIABLE_LIST_CLASS = Collections.unmodifiableList(new LinkedList()).getClass();
        UNMODIFIABLE_RANDOM_ACCESS_LIST_CLASS = Collections.unmodifiableList(new ArrayList()).getClass();
        UNMODIFIABLE_SET_CLASS = Collections.unmodifiableSet(Collections.emptySet()).getClass();
        // 1.8 only
//        UNMODIFIABLE_SORTED_SET_CLASS = Collections.unmodifiableSortedSet(Collections.emptySortedSet()).getClass();
//        UNMODIFIABLE_NAVIGABLE_SET_CLASS = Collections.unmodifiableNavigableSet(Collections.emptyNavigableSet()).getClass();
        UNMODIFIABLE_COLLECTION_CLASS = Collections.unmodifiableCollection(new ArrayList()).getClass();
    }

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        out.writeObject(clzInfo.getClazz());
        Collection coll = (Collection) toWrite;
        out.writeInt(coll.size());
        for (Iterator iterator = coll.iterator(); iterator.hasNext(); ) {
            out.writeObject(iterator.next());
        }
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
        Class clazz = (Class) in.readObject();
        int len = in.readInt();

        try {
            if ( UNMODIFIABLE_RANDOM_ACCESS_LIST_CLASS == clazz) {
                List res = new ArrayList(len);
                fillArray(in, serializationInfo, referencee, streamPosition, res, len);
                return Collections.unmodifiableList(res);
            }
            if ( UNMODIFIABLE_LIST_CLASS == clazz) {
                List res = new LinkedList();
                fillArray(in, serializationInfo, referencee, streamPosition, res, len);
                return Collections.unmodifiableList(res);
            }
            if ( UNMODIFIABLE_SET_CLASS == clazz) {
                Set res = new HashSet(len);
                fillArray(in, serializationInfo, referencee, streamPosition, res, len);
                return Collections.unmodifiableSet(res);
            }
            // 1.8 only
//            if ( UNMODIFIABLE_SORTED_SET_CLASS == clazz ) {
//                Set res = new TreeSet();
//                fillArray(in, serializationInfo, referencee, streamPosition, res, len);
//                return Collections.unmodifiableSet(res);
//            }
//            if (UNMODIFIABLE_NAVIGABLE_SET_CLASS == clazz) {
//                Set res = new TreeSet();
//                fillArray(in, serializationInfo, referencee, streamPosition, res, len);
//                return Collections.unmodifiableSet(res);
//            }
            if (UNMODIFIABLE_COLLECTION_CLASS == clazz) {
                Collection res = new ArrayList(len);
                fillArray(in, serializationInfo, referencee, streamPosition, res, len);
                return Collections.unmodifiableCollection(res);
            }
            throw new RuntimeException("unexpected class tag "+clazz);
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
            final Object o = in.readObject();
            col.add(o);
        }
    }
}
