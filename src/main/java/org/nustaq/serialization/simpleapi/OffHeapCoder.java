package org.nustaq.serialization.simpleapi;

import org.nustaq.offheap.bytez.malloc.MallocBytez;
import org.nustaq.offheap.bytez.onheap.HeapBytez;
import org.nustaq.serialization.*;
import org.nustaq.serialization.coders.FSTBytezDecoder;
import org.nustaq.serialization.coders.FSTBytezEncoder;

import java.io.IOException;

/**
 * Created by ruedi on 09.11.14.
 *
 * enables zero copy encoding to offheap memory. The encoding is platform dependent (endianess) and
 * no attemps on compression are made.
 *
 * Use case: messaging, offheap en/decoding, tmp preservation of state
 * NOT thread safe
 *
 * Do not confuse this with a stream. Each single writeObject is an isolated operation,
 * so restoring of references inside an object graph only happens for refs inside the object graph
 * given to writeObject.
 *
 */
public class OffHeapCoder {

    protected FSTConfiguration conf;
    MallocBytez writeTarget;
    MallocBytez readTarget;
    FSTObjectOutput out;
    FSTObjectInput in;

    public OffHeapCoder() {
        conf = FSTConfiguration.createFastBinaryConfiguration();
        writeTarget = new MallocBytez(0l,0);
        conf.setStreamCoderFactory( new FSTConfiguration.StreamCoderFactory() {
            @Override
            public FSTEncoder createStreamEncoder() {
                FSTBytezEncoder fstBytezEncoder = new FSTBytezEncoder(conf, writeTarget);
                fstBytezEncoder.setAutoResize(true);
                return fstBytezEncoder;
            }
            @Override
            public FSTDecoder createStreamDecoder() {
                return new FSTBytezDecoder(conf);
            }
        });
        out = conf.getObjectOutput();
        in = conf.getObjectInput();
    }

    /**
     * throw
     * @param preregister
     */
    public OffHeapCoder( Class ... preregister ) {
        this();
        conf.registerClass(preregister);
    }

    /**
     * throws FSTBufferTooSmallExcpetion in case object does not fit
     * @param o
     * @param address
     * @param availableSize
     * @throws IOException
     */
    public void writeObject( Object o, long address, int availableSize ) throws IOException {
        writeTarget.setBase(address, availableSize);
        out.writeObject(o);
        out.resetForReUse();
    }

    public Object readObject( long address, int availableSize ) {
        return null;
    }

    public void writeObjectUnshared( Object o, long address, int availableSize ) {

    }

    public Object readObjectUnshared( long address, int availableSize ) {
        return null;
    }

}
