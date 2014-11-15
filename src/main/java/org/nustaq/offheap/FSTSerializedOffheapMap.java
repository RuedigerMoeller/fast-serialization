package org.nustaq.offheap;

import org.nustaq.offheap.bytez.ByteSource;
import org.nustaq.offheap.bytez.bytesource.ByteArrayByteSource;
import org.nustaq.offheap.bytez.bytesource.BytezByteSource;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.simpleapi.FSTBufferTooSmallException;
import org.nustaq.serialization.simpleapi.FSTCoder;
import org.nustaq.serialization.util.FSTUtil;

import java.io.Serializable;

/**
 * An offheap map using serialization for Value Objects.
 * Note there are several wrappers to represent keys as ByteSource (see FSTAsciiStringOffheapMap for example)
 * this class is not threadsafe
 */
public abstract class FSTSerializedOffheapMap<K,V> extends FSTCodedOffheapMap<K,V> {

    protected ByteArrayByteSource tmpVal;
    FSTCoder coder;
    protected byte buffer[] = new byte[2048];

    public FSTSerializedOffheapMap(int keyLen, long sizeBytes, int numberOfElems, FSTCoder coder) {
        super(keyLen, sizeBytes, numberOfElems);
        this.coder = coder;
        tmpVal = new ByteArrayByteSource(buffer);
    }

    public FSTSerializedOffheapMap(String mappedFile, int keyLen, long sizeMemBytes, int numberOfElems, FSTCoder coder) throws Exception {
        super(mappedFile, keyLen, sizeMemBytes, numberOfElems);
        this.coder = coder;
        tmpVal = new ByteArrayByteSource(buffer);
    }

    public ByteSource encodeValue(V value) {
        try {
            int len = coder.toByteArray(value,buffer,0,buffer.length);
            tmpVal.setLen(len);
        } catch (FSTBufferTooSmallException bts) {
            buffer = new byte[buffer.length*2];
            tmpVal.setArr(buffer);
            tmpVal.setOff(0);
            tmpVal.setLen(buffer.length);
            return encodeValue(value);
        } catch (Exception e) {
            FSTUtil.rethrow(e);
        }
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
        while ( val.getLen() > buffer.length ) {
            buffer = new byte[buffer.length*2];
            tmpVal.setArr(buffer);
            tmpVal.setOff(0);
            tmpVal.setLen(buffer.length);
        }
        memory.getArr(val.getOff(), buffer, 0, val.getLen());
        return (V) coder.toObject(buffer);
    }

    public FSTCoder getCoder() {
        return coder;
    }
}
