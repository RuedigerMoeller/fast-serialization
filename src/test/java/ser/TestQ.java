package ser;

import junit.framework.Assert;
import org.junit.Test;
import org.nustaq.offheap.BinaryQueue;
import org.nustaq.offheap.bytez.ByteSink;
import org.nustaq.offheap.bytez.onheap.HeapBytez;

import java.util.Arrays;

/**
 * Created by ruedi on 09.09.2015.
 */
public class TestQ {
    
    @Test
    public void simple() {

        BinaryQueue bq = new BinaryQueue(3);
        bq.add((byte)1);
        bq.add((byte)2);
        bq.add((byte)3);
        bq.add((byte)4);
        bq.add((byte)5);
        bq.add((byte)6);
        bq.add((byte)7);

        for (int i = 0; i < 4; i++) {
            int poll = bq.poll();
            System.out.println(poll);
            Assert.assertTrue(poll==i+1);
        }
        System.out.println(bq.capacity());
    }

    @Test
    public void simpleCycle() {

        BinaryQueue bq = new BinaryQueue(3);
        for( int ii = 0; ii < 1_000; ii++ ) {
            int loop = (int) (Math.random()*32);
            for (int i = 0; i < loop; i++) {
                bq.add((byte)i);
            }
            Assert.assertTrue(bq.available() == loop);
            for (int i = 0; i < loop; i++) {
                int poll = bq.poll();
                Assert.assertTrue(poll==i);
            }
            Assert.assertTrue(bq.available()==0);
        }
    }

    @Test
    public void fat() {

        BinaryQueue bq = new BinaryQueue(3);
        bq.add(new HeapBytez(new byte[] {1,1,1,1,1}) );
        bq.add(new HeapBytez(new byte[] {2,1,1,1,1}));
        bq.add(new HeapBytez(new byte[] {3,1,1,1,1}));
        bq.add(new HeapBytez(new byte[] {4,1,1,1,1}));
        bq.add(new HeapBytez(new byte[] {5,1,1,1,1}));
        bq.add(new HeapBytez(new byte[] {6,1,1,1,1}));
        bq.add(new HeapBytez(new byte[] {7,1,1,1,1}));

        HeapBytez sink = new HeapBytez(new byte[5]);
        for (int i = 0; i < 7; i++) {
            long poll = bq.poll(sink,0,5);
            System.out.println(Arrays.toString(sink.asByteArray()));
            Assert.assertTrue(sink.get(0) == i + 1);
        }
    }

    @Test
    public void cycle() {

        BinaryQueue bq = new BinaryQueue(3);

        for ( int ii = 0; ii < 20; ii++ ) {
            bq.add(new HeapBytez(new byte[] {1,1,1,1,1}) );
            bq.add(new HeapBytez(new byte[] {2,1,1,1,1}));
            bq.add(new HeapBytez(new byte[] {3,1,1,1,1}));
            bq.add(new HeapBytez(new byte[] {4,1,1,1,1}));
            bq.add(new HeapBytez(new byte[] {5,1,1,1,1}));
            bq.add(new HeapBytez(new byte[] {6,1,1,1,1}));
            bq.add(new HeapBytez(new byte[]{7, 1, 1, 1, 1}));

            HeapBytez sink = new HeapBytez(new byte[5]);
            for (int i = 0; i < 7; i++) {
                long poll = bq.poll(sink,0,5);
                System.out.println(Arrays.toString(sink.asByteArray()));
                Assert.assertTrue(sink.get(0) == i + 1);
            }
            Assert.assertTrue(bq.poll(sink,0,5)==0);
        }
        System.out.println(bq.capacity());
    }

}
