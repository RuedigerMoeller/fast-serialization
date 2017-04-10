package json;

import junit.framework.Assert;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.coders.JSONAsString;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * Created by ruedi on 21.03.17.
 */
public class StringEnc {

    public static class TS implements Serializable {
        @JSONAsString
        byte[] bytes;
    }

    @Test
    public void testStringenc() throws UnsupportedEncodingException {
        TS t = new TS();
        t.bytes = "ÄÖasdß".getBytes("UTF-8");
        FSTConfiguration conf = FSTConfiguration.createJsonConfiguration(true,false);
        byte[] bytes = conf.asByteArray(t);
        System.out.println(new String(bytes,"UTF-8"));
        Object res = conf.asObject(bytes);
        String x = new String(((TS) res).bytes, "UTF-8");
        System.out.println(x);
        Assert.assertTrue(x.equals("ÄÖasdß"));
    }

}
