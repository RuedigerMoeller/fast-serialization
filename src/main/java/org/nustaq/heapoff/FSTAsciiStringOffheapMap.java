package org.nustaq.heapoff;

import org.nustaq.heapoff.bytez.ByteSource;
import org.nustaq.heapoff.bytez.bytesource.LeftCutStringByteSource;
import org.nustaq.serialization.FSTConfiguration;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Created by ruedi on 27.06.14.
 */
public class FSTAsciiStringOffheapMap<V> extends FSTSerializedOffheapMap<String,V> {

    LeftCutStringByteSource tmpKey;

    public FSTAsciiStringOffheapMap(int keyLen, long size, int indexSizeMB, FSTConfiguration conf) {
        super(keyLen, size, indexSizeMB, conf);
    }

    @Override
    protected void init(int keyLen, long size, int indexSizeMB) {
        super.init(keyLen, size, indexSizeMB);
        tmpKey = new LeftCutStringByteSource(null,0,keyLen);
    }

    protected ByteSource encodeKey(String key) {
        if ( key.length() > tmpKey.length() )
            throw new RuntimeException("key too long");
        tmpKey.setString(key);
        return tmpKey;
    }

}


