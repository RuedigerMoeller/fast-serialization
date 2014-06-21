package de.ruedigermoeller.serialization.testclasses_old.jdkcompatibility;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: moelrue
 * Date: 8/9/13
 * Time: 1:19 PM
 * To change this template use File | Settings | File Templates.
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
        in.resetForReuseUseArray(b,b.length);
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
