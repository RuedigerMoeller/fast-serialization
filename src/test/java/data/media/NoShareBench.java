package data.media;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import de.ruedigermoeller.serialization.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

/**
 * Created by ruedi on 03.03.14.
 */
public class NoShareBench {

    public static void main( String a[] ) throws IOException, ClassNotFoundException {
        ObjectInputStream oin = new ObjectInputStream(new FileInputStream(".\\src\\test\\java\\data\\test.os"));
        final Object medpa = oin.readObject();

        boolean register = false;

        final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.setShareReferences(false);
        final Class[] classes = {Image.class, Media.class, MediaContent.class, Image.Size.class, Media.Player.class};
        if (register)
            conf.registerClass(classes);

        FSTObjectOutput out = new FSTObjectOutput(conf);
//        FSTObjectOutput out = new FSTObjectOutputNoShared(conf);
        FSTObjectInput in = new FSTObjectInput(conf);
//        FSTObjectInput in = new FSTObjectInputNoShared(conf);

        Kryo kry = new Kryo();
        kry.setReferences(false);

        if (register) {
            kry.setRegistrationRequired(true);
            for (int i = 0; i < classes.length; i++) {
                Class aClass = classes[i];
                kry.register(aClass);
            }
            kry.register(ArrayList.class);
        }
        Input kin = new Input(new byte[2000]);
        Output kout = new Output();

        final int iters = 100000;
        while (true) {
            bench(medpa, out, in, iters*10);
            benchK(medpa,kry,kout,kin,iters*10);
        }
    }

    static byte buf[] = new byte[2000];

    private static void benchK(Object medpa, Kryo k, Output out, Input in, int iters) throws IOException, ClassNotFoundException {
        long rt = 0; long wt=0;
        for ( int i = 0; i < iters; i++) {
            long tim = System.nanoTime();
            out.setBuffer(buf);
            k.writeClassAndObject(out, medpa);
            final byte[] buf = out.toBytes();
            wt+=System.nanoTime()-tim;

            tim =System.nanoTime();
            in.setBuffer(buf);
            final Object read = k.readClassAndObject(in);
            rt += System.nanoTime()-tim;
        }
        System.out.println("k wt:"+(wt/iters)+" rt:"+(rt/iters));
    }

    private static void bench(Object medpa, FSTObjectOutput out, FSTObjectInput in, int iters) throws IOException, ClassNotFoundException {
        long rt = 0; long wt=0; int len = 0;
        for ( int i = 0; i < iters; i++) {
            long tim = System.nanoTime();
            out.resetForReUse();
            out.writeObject(medpa);
            final byte[] buf = out.getCopyOfWrittenBuffer();
            wt+=System.nanoTime()-tim;
            len = out.getWritten();

            tim =System.nanoTime();
            in.resetForReuseUseArray(buf);
            final Object read = in.readObject();
            rt += System.nanoTime()-tim;
        }
        System.out.println("f wt:"+(wt/iters)+" rt:"+(rt/iters)+" l:"+len);
    }
}
