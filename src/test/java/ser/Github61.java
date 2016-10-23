package ser;

import org.nustaq.serialization.*;

import java.io.*;

/**
 * Created by ruedi on 14.04.2015.
 */
public class Github61 {

    /** Size of primitive array */
    static final int SIZE = 50_000_000;

    public static void main(String[] args) throws Exception {

        CountingOutputStream count;
        long start, elapsed;

        Object obj = new double[SIZE];

        count = new CountingOutputStream();
        FSTConfiguration fastconf = FSTConfiguration.createUnsafeBinaryConfiguration();
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        while ( true ) {

            start = System.nanoTime();
            try(ObjectOutputStream oos = new ObjectOutputStream(count)) {
                oos.writeObject(obj);
            }
            elapsed = System.nanoTime() - start;
            System.out.println("STD " + count.getCount() + " bytes written in " + (elapsed) / 1000000L + "ms");
            count.reset();

            start = System.nanoTime();
            try(FSTObjectOutput fos = conf.getObjectOutput(count)) {
                fos.writeObject(obj);
            }
            elapsed = System.nanoTime() - start;
            System.out.println("FST " + count.getCount() + " bytes written in " + (elapsed) / 1000000L + "ms");
            count.reset();

            start = System.nanoTime();
            try(FSTObjectOutput fos = fastconf.getObjectOutput(count)) {
                fos.writeObject(obj);
            }
            elapsed = System.nanoTime() - start;
            System.out.println("FST unsafe " + count.getCount() + " bytes written in " + (elapsed)/1000000L + "ms");
            count.reset();
        }
    }


    public static class CountingOutputStream extends ByteArrayOutputStream {

        public CountingOutputStream() {
            super(SIZE*10);
        }

        public int getCount() {
            return count;
        }
    }


}
