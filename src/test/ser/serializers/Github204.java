package ser.serializers;

import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by ruedi on 31.07.17.
 */
public class Github204 {

    @Test
    public void testMapWriteReplace() throws Exception {
        Map<String, Object> map = new MyMap<>();
        map.put("value", 1234.5);

        Map<String, Object> map1 = new MyMap<>();
        map1.put("othervalue", 5678.9);

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        FSTObjectOutput out = new FSTObjectOutput(buf, getTestConfiguration());
        out.writeObject(map);
        out.writeObject(map1);
        out.writeObject("Ensure Stream is not corrupted");
        out.close();

        FSTObjectInput in = getTestConfiguration().getObjectInput(new ByteArrayInputStream(buf.toByteArray()));
        Map<?, ?> res = (Map<?, ?>) in.readObject();
        Map<?, ?> res1 = (Map<?, ?>) in.readObject();
        in.close();

        assertEquals(LinkedHashMap.class, res.getClass());
        assertEquals(1, res.size());
        assertEquals(1234.5, res.get("value"));
        assertEquals(LinkedHashMap.class, res1.getClass());
        assertEquals(1, res1.size());
        assertEquals(5678.9, res1.get("othervalue"));
    }

    private FSTConfiguration getTestConfiguration() {
        return FSTConfiguration.createDefaultConfiguration();
    }

    public static class MyMap<K, V> implements Map<K, V>, Serializable {
        private final Map<K, V> data;

        public MyMap() {
            this(new HashMap());
        }


        public MyMap(Map<K, V> data) {
            this.data = data;
        }

        @Override
        public int size() {
            return data.size();
        }

        @Override
        public boolean isEmpty() {
            return data.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return data.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return data.containsValue(value);
        }

        @Override
        public V get(Object key) {
            return data.get(key);
        }

        @Override
        public V put(K key, V value) {
            return data.put(key, value);
        }

        @Override
        public V remove(Object key) {
            return data.remove(key);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            data.putAll(m);
        }

        @Override
        public void clear() {
            data.clear();
        }

        @Override
        public Set<K> keySet() {
            return data.keySet();
        }

        @Override
        public Collection<V> values() {
            return data.values();
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return data.entrySet();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MyMap<?, ?> myMap = (MyMap<?, ?>) o;
            return Objects.equals(data, myMap.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(data);
        }

        private Object writeReplace() throws ObjectStreamException {
            return new LinkedHashMap<K, V>(this);
        }
    }
}
