package ser.bighm;

import com.cedarsoftware.util.DeepEquals;
import junit.framework.Assert;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by ruedi on 22/02/15.
 */
public class BigObjTest {

    static class TestCL implements Serializable {
        double sd = Math.random();
        double avg = Math.random();
    }

    public HashMap createHM() {
        HashMap hm = new HashMap();
        for ( int i=0; i < 500000; i++ ) {
            hm.put(i,Math.random());
        }
        return hm;
    }

    public HashMap createHM1(HashMap hmk) {
        HashMap hm = new HashMap();
        for (Iterator iterator = hmk.keySet().iterator(); iterator.hasNext(); ) {
            Object next = iterator.next();
            hm.put(next,new TestCL());
        }
        return hm;
    }

    @Test
    public void testHM() throws IOException, ClassNotFoundException {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        HashMap hm = createHM();
        HashMap hm1 = createHM1(hm);
        FSTObjectOutput oo = conf.getObjectOutput();
        oo.writeObject(hm);
        oo.writeObject(hm1);
        byte[] copyOfWrittenBuffer = oo.getCopyOfWrittenBuffer();

        FSTObjectInput in = conf.getObjectInput(copyOfWrittenBuffer);
        Object o1 = in.readObject();
        Object o2 = in.readObject();
        Assert.assertTrue(DeepEquals.deepEquals(o1,hm));
        Assert.assertTrue(DeepEquals.deepEquals(o2,hm1));
    }

    @Test
    public void testWithStreams() throws IOException, ClassNotFoundException {
        HashMap hm = createHM();
        HashMap hm1 = createHM1(hm);

        FSTObjectOutput out = new FSTObjectOutput(new FileOutputStream("../epsis.oos"));
        out.writeObject(hm);
        out.writeObject(hm1);
        out.close();

        FSTObjectInput in = new FSTObjectInput(new FileInputStream("../epsis.oos"));
        HashMap o1 = (HashMap) in.readObject();
        HashMap o2 = (HashMap) in.readObject();
        in.close();
        Assert.assertTrue(DeepEquals.deepEquals(o1, hm));
        Assert.assertTrue(DeepEquals.deepEquals(o2, hm1));
    }

}
