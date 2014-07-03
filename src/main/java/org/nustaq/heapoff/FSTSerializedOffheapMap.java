package org.nustaq.heapoff;

import org.nustaq.heapoff.bytez.ByteSource;
import org.nustaq.heapoff.bytez.bytesource.ByteArrayByteSource;
import org.nustaq.heapoff.bytez.bytesource.BytezByteSource;
import org.nustaq.serialization.FSTConfiguration;

import java.io.Serializable;

/**
 * An offheap map using serialization for Value Objects.
 * Note there are several wrappers to represent keys as ByteSource (see FSTAsciiStringOffheapMap for example)
 */
public abstract class FSTSerializedOffheapMap<K,V> extends FSTCodedOffheapMap<K,V> {

    ByteArrayByteSource tmpVal;
    FSTConfiguration conf;

    public FSTSerializedOffheapMap(int keyLen, long sizeBytes, int numberOfElems, FSTConfiguration conf) {
        super(keyLen, sizeBytes, numberOfElems);
        this.conf = conf;
        tmpVal = new ByteArrayByteSource(null,0,0);
    }

    public FSTSerializedOffheapMap(String mappedFile, int keyLen, long sizeMemBytes, int numberOfElems, FSTConfiguration conf) throws Exception {
        super(mappedFile, keyLen, sizeMemBytes, numberOfElems);
        this.conf = conf;
        tmpVal = new ByteArrayByteSource(null,0,0);
    }

    public ByteSource encodeValue(V value) {
        byte[] bytes = conf.asByteArray((Serializable) value);
        tmpVal.setArr(bytes);
        tmpVal.setOff(0);
        tmpVal.setLen(bytes.length);
        return tmpVal;
    }

    /**
     * default is to let 100% room for later entry growth. Avoid fragmentation as hell.
     * @param lengthOfEntry
     * @return
     */
    @Override
    protected int getEntryLengthForContentLength(long lengthOfEntry) {
        return (int) (lengthOfEntry*2);
    }

    public V decodeValue(BytezByteSource val) {
        byte[] bytes = memory.toBytes(val.getOff(), val.getLen());
        return (V) conf.asObject(bytes);
    }

}
