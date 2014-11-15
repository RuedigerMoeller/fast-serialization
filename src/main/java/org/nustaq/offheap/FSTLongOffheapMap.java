package org.nustaq.offheap;

import org.nustaq.offheap.bytez.ByteSource;
import org.nustaq.offheap.bytez.bytesource.ByteArrayByteSource;
import org.nustaq.serialization.simpleapi.DefaultCoder;
import org.nustaq.serialization.simpleapi.FSTCoder;

/**
 * Created by ruedi on 15.11.14.
 *
 * Same as FSTAsciiStringOffheapMap, but uses Long as key
 * see FSTAsciiStringOffheapMap for doc.
 */
public class FSTLongOffheapMap<V> extends FSTSerializedOffheapMap<Long,V> {

    public FSTLongOffheapMap(long sizeBytes, int numberOfElems, FSTCoder coder) {
        super(8, sizeBytes, numberOfElems, coder);
    }

    public FSTLongOffheapMap(String mappedFile, long sizeMemBytes, int numberOfElems, FSTCoder coder) throws Exception {
        super(mappedFile, 8, sizeMemBytes, numberOfElems, coder);
    }

    public FSTLongOffheapMap(long sizeBytes, int numberOfElems) {
        super(8, sizeBytes, numberOfElems, new DefaultCoder());
    }

    public FSTLongOffheapMap(String mappedFile, long sizeMemBytes, int numberOfElems) throws Exception {
        super(mappedFile, 8, sizeMemBytes, numberOfElems, new DefaultCoder());
    }

    byte longbyte[] = new byte[8];
    ByteSource tmpKey = new ByteArrayByteSource(longbyte);

    @Override
    public ByteSource encodeKey(Long key) {
        int count = 0;
        long v = key;
        longbyte[count++] = (byte) (v >>> 0);
        longbyte[count++] = (byte) (v >>> 8);
        longbyte[count++] = (byte) (v >>> 16);
        longbyte[count++] = (byte) (v >>> 24);
        longbyte[count++] = (byte) (v >>> 32);
        longbyte[count++] = (byte) (v >>> 40);
        longbyte[count++] = (byte) (v >>> 48);
        longbyte[count++] = (byte) (v >>> 56);
        return tmpKey;
    }
}
