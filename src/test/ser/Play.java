package ser;

import com.cedarsoftware.util.DeepEquals;
import org.nustaq.serialization.*;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by ruedi on 23/05/15.
 */
public class Play implements Serializable {

    public Play() {
    }

    String str[];

    static class T implements Serializable {

        String s;
        int i;
        T1 t1;

        public T() {}

        public T(int dummy) {
            s = "pok";
            i = 100;
            t1 = new T1();
        }

    }

    static class T1 implements Serializable {

        String s;
        int i;

        public T1() {}

        public T1(int dummy) {
            s = "pok1";
            i = 101;
        }

    }

    public static class TSer extends FSTBasicObjectSerializer {
        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
            out.defaultWriteObject(toWrite,clzInfo);
        }

        @Override
        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
            T t = new T();
            in.defaultReadObject(referencee,serializationInfo,t);
            return t;
        }
    }

    public static class T1Ser extends FSTBasicObjectSerializer {
        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
            out.defaultWriteObject(toWrite, clzInfo);
        }

        @Override
        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
            T1 t = new T1();
            in.defaultReadObject(referencee,serializationInfo,t);
            return t;
        }
    }

    public static void main(String[] args) {
        FSTObjectRegistry.POS_MAP_SIZE = 1;
        FSTConfiguration conf = FSTConfiguration.createJsonConfiguration();

        conf.registerSerializer( T.class,new TSer(), true );
        conf.registerSerializer( T1.class,new T1Ser(), true );

        Object p = new T(1);
//        Object p = new BigDecimal(123);
//        Object p = new Object[] {"A", new BasicFSTTest.SubClassedAList().$("A").$("B").$("C"), "Ensure stream not corrupted" };
        System.out.println(conf.asJsonString(p));
        byte[] bytes = conf.asByteArray(p);
        Object deser = conf.asObject(bytes);
        System.out.println(DeepEquals.deepEquals(p,deser));
    }
}
