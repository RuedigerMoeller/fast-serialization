package ser;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Created by ruedi on 14/04/15.
 */
public class FloatBench {

    public static void main0(String[] args) throws Exception {

        while( true ) {
            CountingOutputStream count;
            long start, elapsed;

            Object obj = new double[100_000_000];


            count = new CountingOutputStream();
            start = System.nanoTime();
            try(ObjectOutputStream oos = new ObjectOutputStream(count)) {
                oos.writeObject(obj);
            }
            elapsed = System.nanoTime() - start;
            System.out.println("STD " + count.count + " bytes written in " + (elapsed)/1000000L + "ms");

            count = new CountingOutputStream();
            start = System.nanoTime();
            try(FSTObjectOutput fos = new FSTObjectOutput(count)) {
                fos.writeObject(obj);
            }
            elapsed = System.nanoTime() - start;
            System.out.println("FST " + count.count + " bytes written in " + (elapsed)/1000000L + "ms");
        }
    }

    public static void main(String[] args) throws Exception {

        while( true ) {
            CountingOutputStream count;
            long start, elapsed;

            double obj[] = new double[1_000];
            ByteArrayOutputStream bout = new ByteArrayOutputStream(4*obj.length);

            start = System.nanoTime();
            for ( int n = 0; n < 100000; n++ ) {
                try(ObjectOutputStream oos = new ObjectOutputStream(bout)) {
                    oos.writeObject(obj);
                }
                bout.reset();
            }
            elapsed = System.nanoTime() - start;
            System.out.println("STD :" + (elapsed)/1000000L + "ms");

            FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
            start = System.nanoTime();
            for ( int n = 0; n < 100000; n++ ) {
                bout.write(conf.asByteArray(obj));
                bout.reset();
            }
            elapsed = System.nanoTime() - start;
            System.out.println("FST :" + (elapsed)/1000000L + "ms");

        }
    }

    public static class CountingOutputStream extends OutputStream {
        public long count = 0;
        @Override public void write(int b) { count++; }
    }
}
