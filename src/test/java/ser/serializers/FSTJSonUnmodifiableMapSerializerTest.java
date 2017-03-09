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
package ser.serializers;

import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.serializers.FSTJSonUnmodifiableMapSerializer;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Jakub Kubrynski
 */
@SuppressWarnings("unchecked")
public class FSTJSonUnmodifiableMapSerializerTest {

    static final String TEST_VALUE = "TestValue";
    static final String TEST_KEY = "TestKey";

    @Test
    public void shouldSerializeUnmodifiableMap() throws ClassNotFoundException {
        //given
        Map<String, String> map = Collections.unmodifiableMap(Collections.singletonMap(TEST_KEY, TEST_VALUE));
        FSTConfiguration conf = FSTConfiguration.createJsonNoRefConfiguration();
        //when
        byte[] bytes = conf.asByteArray((map));
        map = (Map<String, String>) conf.asObject(bytes);
        //then
        assertTrue(FSTJSonUnmodifiableMapSerializer.UNMODIFIABLE_MAP_CLASS.isAssignableFrom(map.getClass()));
        assertEquals(1, map.size());
        assertTrue(map.containsKey(TEST_KEY));
        assertTrue(map.containsValue(TEST_VALUE));
    }

}