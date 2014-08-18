package org.nustaq.offheap.bytez.bytesource;

/**
 * Created by ruedi on 27.06.14.
 */
public class LeftCutStringByteSource extends AsciiStringByteSource {


    public LeftCutStringByteSource(String arr) {
        super(arr);
    }

    public LeftCutStringByteSource(String arr, int off) {
        super(arr, off);
    }

    public LeftCutStringByteSource(String arr, int off, int len) {
        super(arr, off, len);
    }

    @Override
    public byte get(long index) {
        int shift = len - string.length();
        if ( index < shift ) {
            return 0;
        }
        return super.get(index-shift);
    }

}
