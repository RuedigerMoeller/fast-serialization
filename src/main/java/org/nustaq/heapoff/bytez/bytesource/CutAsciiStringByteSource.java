package org.nustaq.heapoff.bytez.bytesource;

/**
 * Created by ruedi on 27.06.14.
 */

import org.nustaq.heapoff.bytez.bytesource.AsciiStringByteSource;

/**
 * returns 0 instead of throwing index exception
 */
public class CutAsciiStringByteSource extends AsciiStringByteSource {
    public CutAsciiStringByteSource(String arr) {
        super(arr);
    }

    public CutAsciiStringByteSource(String arr, int off) {
        super(arr, off);
    }

    public CutAsciiStringByteSource(String arr, int off, int len) {
        super(arr, off, len);
    }

    @Override
    public byte get(long index) {
        if ( index + off <= string.length() )
            return super.get(index);
        return 0;
    }
}
