package ser;

import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectOutput;
import org.nustaq.serialization.FSTObjectOutputNoShared;
import org.nustaq.serialization.serializers.FSTCollectionSerializer;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by moelrue on 5/7/15.
 */
public class Git67 {

    @Test
    public void testUnshared() {
        Set<Long> obj = new LinkedHashSet();
        obj.add(11373L);
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.setShareReferences(false);
        conf.registerSerializer(LinkedHashSet.class,new FSTCollectionSerializer(),true);
        byte[] bytes = conf.asByteArray(obj);
        Object o = conf.asObject(bytes);
    }

}
