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
import org.nustaq.serialization.serializers.FSTUnmodifiableCollectionSerializer;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Jakub Kubrynski
 */
@SuppressWarnings("unchecked")
public class FSTUnmodifiableCollectionSerializerTest {

    static final String TEST_VALUE = "TestValue";

    @Test
    public void shouldSerializeUnmodifiableList() throws ClassNotFoundException {
        //given
        List<String> list = Collections.unmodifiableList(Collections.singletonList(TEST_VALUE));
        FSTConfiguration conf = FSTConfiguration.createJsonNoRefConfiguration();
        //when
        byte[] bytes = conf.asByteArray((list));
        list = (List<String>) conf.asObject(bytes);
        //then
        assertTrue(FSTUnmodifiableCollectionSerializer.UNMODIFIABLE_LIST_CLASS.isAssignableFrom(list.getClass()));
        assertEquals(1, list.size());
        assertTrue(list.contains(TEST_VALUE));
    }

    @Test
    public void shouldSerializeUnmodifiableSet() throws ClassNotFoundException {
        //given
        Set<String> set = Collections.unmodifiableSet(Collections.singleton(TEST_VALUE));
        FSTConfiguration conf = FSTConfiguration.createJsonNoRefConfiguration();
        //when
        byte[] bytes = conf.asByteArray((set));
        set = (Set<String>) conf.asObject(bytes);
        //then
        assertTrue(FSTUnmodifiableCollectionSerializer.UNMODIFIABLE_SET_CLASS.isAssignableFrom(set.getClass()));
        assertEquals(1, set.size());
        assertTrue(set.contains(TEST_VALUE));
    }

}