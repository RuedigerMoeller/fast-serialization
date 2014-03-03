package data.media;

import de.ruedigermoeller.serialization.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by ruedi on 03.03.14.
 */
public class NoShareBench {

    public static void main( String a[] ) throws IOException, ClassNotFoundException {
        ObjectInputStream oin = new ObjectInputStream(new FileInputStream(".\\src\\test\\java\\data\\test.os"));
        final Object medpa = oin.readObject();

        final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.setShareReferences(false);
        conf.registerClass(Image.class,Media.class,MediaContent.class,Image.Size.class,Media.Player.class);

//        FSTObjectOutput out = new FSTObjectOutput(conf);
        FSTObjectOutput out = new FSTObjectOutputNoShared(conf);
        FSTObjectInput in = new FSTObjectInputNoShared(conf);

        final int iters = 100000;
        bench(medpa, out, in, iters);
        while (true)
            bench(medpa, out, in, iters*10);
    }

    private static void bench(Object medpa, FSTObjectOutput out, FSTObjectInput in, int iters) throws IOException, ClassNotFoundException {
        long rt = 0; long wt=0;
        for ( int i = 0; i < iters; i++) {
            long tim = System.nanoTime();
            out.resetForReUse();
            out.writeObject(medpa);
            final byte[] buf = out.getCopyOfWrittenBuffer();
            wt+=System.nanoTime()-tim;

            tim =System.nanoTime();
            in.resetForReuseUseArray(buf);
            final Object read = in.readObject();
            rt += System.nanoTime()-tim;
        }
        System.out.println("wt:"+(wt/iters)+" rt:"+(rt/iters));
    }
}
