package ser;

import com.cedarsoftware.util.DeepEquals;
import junit.framework.Assert;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import org.nustaq.serialization.serializers.FSTCollectionSerializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.Stack;

/**
 * Created by ruedi on 20.11.2014.
 */
public class GitHub39 {

    static class OneStackClass implements Serializable {

        Stack<String> a;

        public OneStackClass(Stack<String> a) {
            this.a = a;
        }
    }

    static class TwoStackClass implements Serializable {

        Stack<String> a;
        Stack<String> b;

        public TwoStackClass(Stack<String> a, Stack<String> b) {
            this.a = a;
            this.b = b;
        }
    }

    @Test
    public void test() {

        OneStackClass osc = new OneStackClass(new Stack<String>());
        byte[] mywriteMethod1 = FSTSerialization.mywriteMethod(osc);
        OneStackClass myreadMethod1 = FSTSerialization.myreadMethod(mywriteMethod1, OneStackClass.class);
        Assert.assertTrue(DeepEquals.deepEquals(osc,myreadMethod1));
        //
        TwoStackClass tsc = new TwoStackClass(new Stack<String>(), new Stack<String>());
        byte[] mywriteMethod2 = FSTSerialization.mywriteMethod(tsc);
        TwoStackClass myreadMethod2 = FSTSerialization.myreadMethod(mywriteMethod2, TwoStackClass.class);
        Assert.assertTrue(DeepEquals.deepEquals(tsc,myreadMethod2));

    }

    static public class FSTSerialization {

        final static FSTConfiguration configuration;

        public static FSTConfiguration getConfiguration() {
            return configuration;
        }

        static {
            configuration = FSTConfiguration.createDefaultConfiguration();
//            configuration.setShareReferences(true);
//            configuration.registerSerializer(Stack.class,new FSTCollectionSerializer(), true);
            configuration.registerClass(
                Stack.class,
                String.class
            );
        }

        public static <T> T myreadMethod(byte[] stream, Class<T> c) {
            FSTObjectInput in = configuration.getObjectInput(stream);
            T result = null;
            try {
                result = (T) in.readObject(c);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return result;
        }

        @SuppressWarnings("null")
        public static byte[] mywriteMethod(Object toWrite) {
            return mywriteMethod(null, toWrite);
        }

        public static byte[] mywriteMethod(byte[] stream, Object toWrite) {
            FSTObjectOutput out = configuration.getObjectOutput(stream);
            try {
                out.writeObject(toWrite, toWrite.getClass());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                out.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return out.getBuffer();
        }

    }

}
