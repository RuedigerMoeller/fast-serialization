package ser;
import com.cedarsoftware.util.DeepEquals;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import junit.framework.Assert;
import org.junit.Test;

import javax.security.auth.Subject;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;

/**
 Copyright [2014] Ruediger Moeller

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

public class SpecialsTest {

    static class ToWrite implements Serializable{
        String dummy = "empty";

        public ToWrite(String dummy) {
            this.dummy = dummy;
        }

        public Object writeReplace() {
            return new ToRead(dummy);
        }
    }

    static class ToRead implements Serializable {
        String dummy;

        public ToRead(String dummy) {
            this.dummy = dummy;
        }

        public Object readResolve() {
            return dummy;
        }
    }

    public void exceptionTest(FSTConfiguration conf) throws IOException, ClassNotFoundException {
        FSTObjectOutput out = conf.getObjectOutput();
        Exception e;
        try {
            throw new Exception("Test");
        } catch (Exception ex) {
            e = ex;
        }
        out.writeObject(e);
        out.flush();
        FSTObjectInput in = new FSTObjectInput(conf);
        in.resetForReuseUseArray(out.getBuffer(),out.getWritten());
        Object ex = in.readObject();
        System.out.println("success "+ex);
    }

    public void test( FSTConfiguration conf, Serializable toTest ) {
        byte b[] = conf.asByteArray(toTest);
        Object deser = conf.asObject(b);
        if (!DeepEquals.deepEquals(toTest, deser)) {
            throw new RuntimeException("oh no "+toTest);
        }
    }


    @Test
    public void main() throws Exception {
        boolean succ = true;
        try {
            FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

            System.out.println(conf.asByteArray(new HTMLDocument()).length);

            test(conf, new HTMLDocument());


            HashSet test = new HashSet();
            test.add("pok");
            test(conf, (Serializable) Collections.synchronizedSet(test));


            test(conf, new BigDecimal(100.0));

            test(conf, new Object[] {new Subject(), "no corruption"});

            InetAddress localhost = InetAddress.getByName("::1");
            test( conf, new Object[] { localhost, "dummy" });

            exceptionTest(conf);

            ToWrite w = new ToWrite("bla");

            byte b[] = null;
            FSTObjectOutput out = new FSTObjectOutput(conf);
            out.writeObject(w);
            out.flush();
            b = out.getBuffer();

            FSTObjectInput in = new FSTObjectInput(conf);
            in.resetForReuseUseArray(b, b.length);
            Object res = in.readObject();

            if (!res.equals("bla")) {
                throw new RuntimeException("fail " + res);
            }

            test(conf, new Object[] { new HTMLEditorKit(), "no corruption" });
            test(conf, new Object[] { new File("/tmp"), "no corruption" });
            test(conf, System.getProperties());
        } catch ( Exception e ) {
            e.printStackTrace();
            succ = false;
        }

        Assert.assertTrue(succ);

    }

}
