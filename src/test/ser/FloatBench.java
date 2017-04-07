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

    // as originally submitted. problematic as GC dominates runtime
    public static void main0(String[] args) throws Exception {

        while( true ) {
            CountingOutputStream count;
            long start, elapsed;

            Object obj = new double[100000000];
//            FSTConfiguration conf = FSTConfiguration.createUnsafeBinaryConfiguration();
            FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();


            count = new CountingOutputStream();
            start = System.nanoTime();
            ObjectOutputStream oos = new ObjectOutputStream(count);
            oos.writeObject(obj);
            elapsed = System.nanoTime() - start;
            System.out.println("STD " + count.count + " bytes written in " + (elapsed)/1000000L + "ms");

            count = new CountingOutputStream();
            start = System.nanoTime();
            FSTObjectOutput fos = new FSTObjectOutput(count,conf);
            fos.writeObject(obj);
            elapsed = System.nanoTime() - start;
            System.out.println("FST " + count.count + " bytes written in " + (elapsed)/1000000L + "ms");
        }
    }

    public static void main(String[] args) throws Exception {

        while( true ) {
            CountingOutputStream count;
            long start, elapsed;

            count = new CountingOutputStream();

//            Object obj = new float[20_000];
//            Object obj = new long[10_000];
            Object obj = new boolean[80000];
//            Object obj = new byte[80_000];
//            Object obj = new double[10_000];
//            Object obj = new char[40_000];
//            Object obj = new short[40_000];
//            Object obj = new int[20_000];

//            FSTConfiguration conf = FSTConfiguration.createUnsafeBinaryConfiguration();
            FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

            start = System.nanoTime();
            for ( int n = 0; n < 10000; n++ ) {
                ObjectOutputStream oos = new ObjectOutputStream(count);
                oos.writeObject(obj);
            }
            elapsed = System.nanoTime() - start;
            System.out.println("STD :" + (elapsed)/1000000L + "ms");

            start = System.nanoTime();
            for ( int n = 0; n < 10000; n++ ) {
                FSTObjectOutput fos = new FSTObjectOutput(count,conf);
                fos.writeObject(obj);
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
