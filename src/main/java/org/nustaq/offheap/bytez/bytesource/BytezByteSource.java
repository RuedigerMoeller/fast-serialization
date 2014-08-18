package org.nustaq.offheap.bytez.bytesource;

import org.nustaq.offheap.bytez.ByteSource;
import org.nustaq.offheap.bytez.Bytez;

/**
 * Created by ruedi on 27.06.14.
 */
public class BytezByteSource implements ByteSource {

    Bytez bytes;
    long off;
    int len;

    public BytezByteSource(Bytez bytes, long off, int len) {
        this.bytes = bytes;
        this.off = off;
        this.len = len;
    }

    @Override
    public byte get(long index) {
        return bytes.get(index+off);
    }

    @Override
    public long length() {
        return len;
    }

    public Bytez getBytes() {
        return bytes;
    }

    public void setBytes(Bytez bytes) {
        this.bytes = bytes;
    }

    public long getOff() {
        return off;
    }

    public void setOff(long off) {
        this.off = off;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }
}
