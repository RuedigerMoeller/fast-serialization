package ser;

import com.cedarsoftware.util.DeepEquals;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;

import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by ruedi on 22/05/16.
 */
public class UmodifiableTest {

    @Test
    public void testUnmodifiableMap() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        HashMap map = new HashMap();
        map.put("Hello", new Date());
        Map un = Collections.unmodifiableMap(map);
        Map res = (Map) conf.asObject(conf.asByteArray(un));
        assertTrue(DeepEquals.deepEquals(res, un));
    }

    @Test
    public void testUnmodifiableList() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        ArrayList list = new ArrayList();
        list.add("Hello");
        list.add(new Date());
        List un = Collections.unmodifiableList(list);
        List res = (List) conf.asObject(conf.asByteArray(un));
        assertTrue(DeepEquals.deepEquals(res,un));
    }

    @Test
    public void testUnmodifiableLinkedList() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        List list = new LinkedList();
        list.add("Hello");
        list.add(new Date());
        List un = Collections.unmodifiableList(list);
        List res = (List) conf.asObject(conf.asByteArray(un));
        assertTrue(DeepEquals.deepEquals(res,un));
    }

    @Test
    public void testUnmodifiableLinkedHashMap() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

        Map m1 = new LinkedHashMap<String, Integer>();
        m1.put("a", 1);
        m1.put("BB", 1);
        m1.put("ccc", 1);
        m1.put("aa", 1);
        m1.put("BBa", 1);
        m1.put("ccca", 1);
        m1 = Collections.unmodifiableMap(m1);

        Map un = Collections.unmodifiableMap(m1);
        Map res = (Map) conf.asObject(conf.asByteArray(un));
        assertTrue(DeepEquals.deepEquals(res,un));

        System.out.println(res.keySet());
        System.out.println(m1.keySet());
    }


}
