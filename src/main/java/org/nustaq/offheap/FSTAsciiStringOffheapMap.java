package org.nustaq.offheap;

import org.nustaq.offheap.bytez.ByteSource;
import org.nustaq.offheap.bytez.bytesource.LeftCutStringByteSource;
import org.nustaq.serialization.FSTConfiguration;

/**
 * Created by ruedi on 27.06.14.
 */
public class FSTAsciiStringOffheapMap<V> extends FSTSerializedOffheapMap<String,V> {

    LeftCutStringByteSource tmpKey;

    public FSTAsciiStringOffheapMap(int keyLen, long sizeMemBytes, int numberOfEleems, FSTConfiguration conf) {
        super(keyLen, sizeMemBytes, numberOfEleems, conf);
        tmpKey = new LeftCutStringByteSource(null,0,keyLen);
    }

    public FSTAsciiStringOffheapMap(String mappedFile, int keyLen, long sizeMemBytes, int numberOfElems,FSTConfiguration conf) throws Exception {
        super(mappedFile, keyLen, sizeMemBytes, numberOfElems, conf);
        tmpKey = new LeftCutStringByteSource(null,0,keyLen);
    }

    public ByteSource encodeKey(String key) {
        if ( key.length() > tmpKey.length() )
            throw new RuntimeException("key too long: '"+key+"' maxlen:"+tmpKey.length());
        tmpKey.setString(key);
        return tmpKey;
    }

}


