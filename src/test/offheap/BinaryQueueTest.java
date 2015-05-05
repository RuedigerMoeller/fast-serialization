package offheap;

import junit.framework.Assert;
import org.junit.Test;
import org.nustaq.offheap.BinaryQueue;
import org.nustaq.offheap.bytez.ByteSink;
import org.nustaq.offheap.bytez.ByteSource;
import org.nustaq.offheap.bytez.onheap.HeapBytez;

import java.util.Arrays;

/**
 * Created by moelrue on 5/5/15.
 */
public class BinaryQueueTest {

    @Test
    public void binQ() {
        HeapBytez sink = new HeapBytez(100);
        BinaryQueue q = new BinaryQueue();
        for ( int ii = 0; ii < 500; ii++ ) {
            q.add(createMSG((byte) 1, 88));
            Assert.assertTrue(q.size() == 88);
            q.add(createMSG((byte) 2, 99));
            Assert.assertTrue(q.size() == 88 + 99);
            long len = q.poll(sink, 0, 88);
            for (int i = 0; i < len; i++) {
                Assert.assertTrue(sink.get(i) == 1);
            }
            Assert.assertTrue(q.size() == 99);
            len = q.poll(sink, 0, 99);
            for (int i = 0; i < len; i++) {
                Assert.assertTrue(sink.get(i) == 2);
            }
            Assert.assertTrue(q.size() == 0);
        }
    }

    @Test
    public void binOverflow() {
        HeapBytez sink = new HeapBytez(100);
        BinaryQueue q = new BinaryQueue();
        for ( int ii = 0; ii < 50; ii++ ) {
            q.add(createMSG((byte) 1, 88));
            q.add(createMSG((byte) 2, 99));
        }
        Assert.assertTrue(q.size() == 50 * (99 + 88));
        for ( int ii = 0; ii < 50; ii++ ) {
            long len = q.poll(sink, 0, 88);
            for (int i = 0; i < len; i++) {
                Assert.assertTrue(sink.get(i) == 1);
            }
            len = q.poll(sink, 0, 99);
            for (int i = 0; i < len; i++) {
                Assert.assertTrue(sink.get(i) == 2);
            }
        }
        System.out.println("cap "+q.capacity()+" size "+q.size());
    }

    private ByteSource createMSG(byte num, int len) {
        byte[] b = new byte[len];
        Arrays.fill(b, num);
        return new HeapBytez(b);
    }
}
