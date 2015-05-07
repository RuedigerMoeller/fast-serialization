package ser;

import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectOutput;
import org.nustaq.serialization.FSTObjectOutputNoShared;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by moelrue on 5/7/15.
 */
public class Git67 {

    @Test
    public void testUnshared() {
        Set<Long> obj = new LinkedHashSet<>();
        obj.add(11373L);
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.setShareReferences(false);
        byte[] bytes = conf.asByteArray(obj);
        Object o = conf.asObject(bytes);
    }

    @Test
    public void testUnsharedStream() throws IOException {
        Set<Long> obj = new LinkedHashSet<>();
        obj.add(11373L);
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.setShareReferences(false);
        FSTObjectOutputNoShared fout = new FSTObjectOutputNoShared();
        fout.writeObject(obj);
        byte[] bytes = fout.getCopyOfWrittenBuffer();
        Object o = conf.asObject(bytes);
    }

    @Test
    public void testUnsharedStream1() throws IOException {
        Set<Long> obj = new LinkedHashSet<>();
        obj.add(11373L);
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.setShareReferences(false);
        FSTObjectOutput fout = new FSTObjectOutput();
        fout.writeObject(obj);
        byte[] bytes = fout.getCopyOfWrittenBuffer();
        Object o = conf.asObject(bytes);
    }
}
