package org.nustaq.heapoff;

import org.nustaq.heapoff.bytez.ByteSource;
import org.nustaq.heapoff.bytez.bytesource.ByteArrayByteSource;
import org.nustaq.heapoff.bytez.bytesource.BytezByteSource;
import org.nustaq.serialization.FSTConfiguration;

import java.io.Serializable;

/**
 * Created by ruedi on 27.06.14.
 */
public abstract class FSTSerializedOffheapMap<K,V> extends FSTCodedOffheapMap<K,V> {

    ByteArrayByteSource tmpVal;
    FSTConfiguration conf;

    public FSTSerializedOffheapMap(int keyLen, long size, FSTConfiguration conf) {
        super(keyLen, size);
        this.conf = conf;
    }

    @Override
    protected void init(int keyLen, long size) {
        super.init(keyLen, size);
        tmpVal = new ByteArrayByteSource(null,0,0);
    }

    protected ByteSource encodeValue(V value) {
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

    protected V decodeValue(BytezByteSource val) {
        byte[] bytes = memory.toBytes(val.getOff(), val.getLen());
        return (V) conf.asObject(bytes);
    }

}
