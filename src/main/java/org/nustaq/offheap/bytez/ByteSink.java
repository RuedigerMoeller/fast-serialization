package org.nustaq.offheap.bytez;

/**
 * Created by moelrue on 5/5/15.
 */
public interface ByteSink {

    public void put(long byteIndex, byte value);
    public long length();
    public void copyTo(BasicBytez other, long otherByteIndex, long myByteIndex, long lenBytes);

}
