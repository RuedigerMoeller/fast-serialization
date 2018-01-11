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
public class FSTJSonUnmodifiableMapSerializer extends FSTMapSerializer {

    public static final Class<?> UNMODIFIABLE_MAP_CLASS;

    static {
        UNMODIFIABLE_MAP_CLASS = Collections.unmodifiableMap(new HashMap()).getClass();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
        try {
            // note: unlike with list's JDK uses a single wrapper for unmodifiable maps, so information regarding ordering gets lost.
            // as the enclosed map is private, there is also no possibility to detect that case
            // we could always create a linkedhashmap here, but this would have major performance drawbacks.

            // this only hits JSON codec as JSON codec does not implement a full JDK-serialization fallback (like the binary codecs)
            int len = in.readInt();
            if (UNMODIFIABLE_MAP_CLASS.isAssignableFrom(objectClass)) {
                Map res = new HashMap(len);
                in.registerObject(res, streamPosition, serializationInfo, referencee);
                for (int i = 0; i < len; i++) {
                    Object key = in.readObjectInternal(null);
                    Object val = in.readObjectInternal(null);
                    res.put(key, val);
                }
                return Collections.unmodifiableMap(res);
            }
        } catch (Throwable th) {
            FSTUtil.rethrow(th);
        }
        return null;
    }

}
