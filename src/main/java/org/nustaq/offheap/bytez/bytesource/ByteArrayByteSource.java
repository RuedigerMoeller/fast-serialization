package org.nustaq.offheap.bytez.bytesource;

import org.nustaq.offheap.bytez.ByteSource;

/**
 * Created by ruedi on 27.06.14.
 */
public class ByteArrayByteSource implements ByteSource
{

    byte arr[];
    int off;
    int len;

    public ByteArrayByteSource(byte[] arr) {
        this.arr = arr;
        off = 0;
        len = arr.length;
    }

    public ByteArrayByteSource(byte[] arr, int off) {
        this.arr = arr;
        this.off = off;
        len = arr.length-off;
    }

    public ByteArrayByteSource(byte[] arr, int off, int len) {
        this.arr = arr;
        this.off = off;
        this.len = len;
    }

    @Override
    public byte get(long index) {
        return arr[((int) (index + off))];
    }

    @Override
    public long length() {
        return len;
    }

    public byte[] getArr() {
        return arr;
    }

    public void setArr(byte[] arr) {
        this.arr = arr;
    }

    public int getOff() {
        return off;
    }

    public void setOff(int off) {
        this.off = off;
    }

    public void setLen(int len) {
        this.len = len;
    }
}
