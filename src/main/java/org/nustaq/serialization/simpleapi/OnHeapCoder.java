package org.nustaq.serialization.simpleapi;

import org.nustaq.offheap.bytez.malloc.MallocBytez;
import org.nustaq.offheap.bytez.onheap.HeapBytez;
import org.nustaq.serialization.*;
import org.nustaq.serialization.coders.FSTBytezDecoder;
import org.nustaq.serialization.coders.FSTBytezEncoder;

import java.io.IOException;

/**
 * Created by ruedi on 13.11.14.
 */
public class OnHeapCoder {

    protected FSTConfiguration conf;
    HeapBytez writeTarget;
    HeapBytez readTarget;
    FSTObjectOutput out;
    FSTObjectInput in;

    public OnHeapCoder() {
        conf = FSTConfiguration.createFastBinaryConfiguration();
        writeTarget = new HeapBytez(new byte[0]);
        readTarget = new HeapBytez(new byte[0]);
        conf.setStreamCoderFactory(new FSTConfiguration.StreamCoderFactory() {
            @Override
            public FSTEncoder createStreamEncoder() {
                FSTBytezEncoder fstBytezEncoder = new FSTBytezEncoder(conf, writeTarget);
                fstBytezEncoder.setAutoResize(false);
                return fstBytezEncoder;
            }

            @Override
            public FSTDecoder createStreamDecoder() {
                return new FSTBytezDecoder(conf,readTarget);
            }
        });
        out = conf.getObjectOutput();
        in = conf.getObjectInput();
    }

    /**
     * throw
     * @param preregister
     */
    public OnHeapCoder( Class ... preregister ) {
        this();
        conf.registerClass(preregister);
    }

    /**
     * throws FSTBufferTooSmallExcpetion in case object does not fit into given range
     *
     * @param o
     * @param availableSize
     * @throws java.io.IOException
     * @return number of bytes written to the memory region
     */
    public int writeObject( Object o, byte arr[], int startIndex, int availableSize ) throws IOException {
        writeTarget.setBase(arr, startIndex, availableSize);
        out.writeObject(o);
        int written = out.getWritten();
        out.resetForReUse();
        return written;
    }

    public Object readObject( byte arr[], int startIndex, int availableSize ) throws IOException, ClassNotFoundException {
        readTarget.setBase(arr,startIndex,availableSize);
        Object o = in.readObject();
        in.resetForReuse(null);
        return o;
    }

}
