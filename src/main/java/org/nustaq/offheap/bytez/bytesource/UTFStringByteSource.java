package org.nustaq.offheap.bytez.bytesource;

import org.nustaq.offheap.bytez.ByteSource;
import org.nustaq.serialization.util.FSTUtil;

import java.io.UnsupportedEncodingException;

/**
 * Created by ruedi on 05/08/15.
 */
public class UTFStringByteSource implements ByteSource {

    byte bytes[];

    public UTFStringByteSource(String key) {
        try {
            bytes = key.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
    }

    @Override
    public byte get(long index) {
        return bytes[((int) index)];
    }

    @Override
    public long length() {
        return bytes.length;
    }

    public UTFStringByteSource padLeft(int keyLen) {
        if ( bytes.length < keyLen ) {
            byte newBytes[] = new byte[keyLen];
            System.arraycopy(bytes,0,newBytes,keyLen-bytes.length,bytes.length);
            bytes = newBytes;
        }
        return this;
    }

}
