package de.ruedigermoeller.serialization.testclasses.jdkcompatibility;

import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;

import java.io.Serializable;

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

public class ReadResolve
{
    public static class Holder implements Serializable {
        private Object o;
        private Object o2;
    }

    public static class ToRead implements Serializable {
        private final String string;

        public ToRead(String string) {
            this.string = string;
        }

        private Object readResolve() {
            return string;
        }
    }

    public static void checkEquals( Object a, Object b ) {
        if ( ! a.equals(b) ) {
            throw new RuntimeException("fail");
        }
    }

    public static void testReadResolve() throws Exception {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

        Holder holder = new Holder();
        holder.o = new ToRead("foo");
        holder.o2 = holder.o;

        byte[] b = null;
        FSTObjectOutput out = new FSTObjectOutput(conf);
        out.writeObject(holder);
        out.flush();
        b = out.getBuffer();

        FSTObjectInput in = new FSTObjectInput(conf);
        in.resetForReuseUseArray(b,0,b.length);
        Object res = in.readObject();

        checkEquals(Holder.class, res.getClass());
        checkEquals(String.class, ((Holder) res).o.getClass());
        checkEquals("foo", ((Holder) res).o);

        checkEquals(String.class, ((Holder) res).o2.getClass());
        checkEquals("foo", ((Holder) res).o2);
    }

    public static void main(String arg[]) throws Exception {
        testReadResolve();
    }

}
