package de.ruedigermoeller.serialization.testclasses.jdkcompatibility;

import com.cedarsoftware.util.DeepEquals;
import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;

import javax.security.auth.Subject;
import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

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

    public static void exceptionTest(FSTConfiguration conf) throws IOException, ClassNotFoundException {
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
        in.resetForReuseUseArray(out.getBuffer(),0,out.getWritten());
        Object ex = in.readObject();
        System.out.println("success "+ex);
    }

    public static void test( FSTConfiguration conf, Serializable toTest ) {
        byte b[] = conf.asByteArray(toTest);
        Object deser = conf.asObject(b);
        if (!DeepEquals.deepEquals(toTest, deser)) {
            throw new RuntimeException("oh no "+toTest);
        }
    }


    public static void main(String[]s) throws Exception {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        exceptionTest(conf);


        ToWrite w = new ToWrite("bla");

        byte b[] = null;
        FSTObjectOutput out = new FSTObjectOutput(conf);
        out.writeObject(w);
        out.flush();
        b = out.getBuffer();

        FSTObjectInput in = new FSTObjectInput(conf);
        in.resetForReuseUseArray(b,0,b.length);
        Object res = in.readObject();

        if ( !res.equals("bla") ) {
            throw new RuntimeException("fail "+res);
        }

        ReadResolve.main(s);
        test(conf, new Subject());
        test(conf, new HTMLEditorKit());
        test(conf, new HTMLDocument());
        test(conf, System.getProperties());

    }

}
