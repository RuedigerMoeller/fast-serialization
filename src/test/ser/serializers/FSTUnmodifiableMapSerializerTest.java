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
import org.nustaq.serialization.serializers.FSTUnmodifiableMapSerializer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Jakub Kubrynski
 */
@SuppressWarnings("unchecked")
public class FSTUnmodifiableMapSerializerTest {

    static final String TEST_VALUE_1 = "TestValue1";
    static final String TEST_KEY_1 = "TestKey1";
    static final String TEST_VALUE_2 = "TestValue2";
    static final String TEST_KEY_2 = "TestKey2";
    static final String TEST_VALUE_3 = "TestValue3";
    static final String TEST_KEY_3 = "TestKey3";

    @Test
    public void shouldSerializeUnmodifiableMap() throws Exception {
        //given
        Map<String, String> tmp = new LinkedHashMap<>();
        tmp.put(TEST_KEY_1, TEST_VALUE_1);
        tmp.put(TEST_KEY_2, TEST_VALUE_2);
        tmp.put(TEST_KEY_3, TEST_VALUE_3);
        Map<String, String> map = Collections.unmodifiableMap(tmp);

        FSTConfiguration configuration = FSTConfiguration.createJsonNoRefConfiguration();
        //when
        byte[] bytes = configuration.asByteArray((map));
        map = (Map<String, String>) configuration.asObject(bytes);
        //then
        assertTrue(FSTUnmodifiableMapSerializer.UNMODIFIABLE_MAP_CLASS.isAssignableFrom(map.getClass()));
        assertEquals(3, map.size());

        assertArrayEquals(new String[]{TEST_KEY_1, TEST_KEY_2, TEST_KEY_3}, map.keySet().toArray(new String[map.keySet().size()]));
        assertArrayEquals(new String[]{TEST_VALUE_1, TEST_VALUE_2, TEST_VALUE_3}, map.values().toArray(new String[map.values().size()]));
    }
}