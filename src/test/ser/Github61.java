package ser;

import org.nustaq.serialization.*;

import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Created by ruedi on 14.04.2015.
 */
public class Github61 {

    /** Size of primitive array */
    static final int SIZE = 100_000_000;

    public static void main(String[] args) throws Exception {

        CountingOutputStream count;
        long start, elapsed;

        Object obj = new double[SIZE];

        while ( true ) {

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


            count = new CountingOutputStream();
            FSTConfiguration conf = FSTConfiguration.createFastBinaryConfiguration();
            start = System.nanoTime();
            try(FSTObjectOutput fos = new FSTObjectOutput(count,conf)) {
                fos.writeObject(obj);
            }
            elapsed = System.nanoTime() - start;
            System.out.println("FST unsafe " + count.count + " bytes written in " + (elapsed)/1000000L + "ms");
        }
    }


    public static class CountingOutputStream extends OutputStream {
        public long count = 0;
        @Override public void write(int b) { count++; }
    }


}
