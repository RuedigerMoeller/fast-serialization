package org.nustaq.heapoff.bytez;

/**
 * Created by ruedi on 27.06.14.
 */
public interface ByteSource {
    public byte get(long index);
    public long length();
}
