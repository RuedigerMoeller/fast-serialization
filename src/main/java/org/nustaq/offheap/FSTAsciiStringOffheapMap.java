package org.nustaq.offheap;

import org.nustaq.offheap.bytez.ByteSource;
import org.nustaq.offheap.bytez.bytesource.LeftCutStringByteSource;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.simpleapi.DefaultCoder;
import org.nustaq.serialization.simpleapi.FSTCoder;

/**
 * Created by ruedi on 27.06.14.
 *
 * An offheap hashmap. Keys are strings with a given max length. The longer the strings, the slower
 * lookup will be. Values < 50 are ok'ish.
 *
 * The Map can reside on disk (using mmapped files) or just in offheap.
 *
 * Note that changing stored classes will lead to decoding errors in case of persisted files.
 * You need to make use of FST versioning annotation in order to avoid this.
 */
public class FSTAsciiStringOffheapMap<V> extends FSTSerializedOffheapMap<String,V> {

    LeftCutStringByteSource tmpKey;

    /**
     * see other mem constructor, additional allows to insert a coder.
     * Useful to preregister classes (speed+size gain, no classnames written!).
     * E.g. new DefaultCoder(MyValue.class, MyOtherValue.class, .. )
     */
    public FSTAsciiStringOffheapMap(int keyLen, long sizeMemBytes, int numberOfEleems, FSTCoder coder) {
        super(keyLen, sizeMemBytes, numberOfEleems, coder);
        tmpKey = new LeftCutStringByteSource(null,0,keyLen);
    }

    /**
     *
     * @param keyLen - maximum len of a key
     * @param sizeMemBytes - size of memory (not bigger than OS heap
     * @param numberOfEleems - estimation on number of key-value pairs (autogrow, just educated guess)
     */
    public FSTAsciiStringOffheapMap(int keyLen, long sizeMemBytes, int numberOfEleems) {
        super(keyLen, sizeMemBytes, numberOfEleems, new DefaultCoder());
        tmpKey = new LeftCutStringByteSource(null,0,keyLen);
    }

    /**
     * see other mem mapped file constructor + additional allows to insert a coder.
     * Useful to preregister classes (speed+size gain, no classnames written!).
     * E.g. new DefaultCoder(MyValue.class, MyOtherValue.class, .. )
     */
    public FSTAsciiStringOffheapMap(String mappedFile, int keyLen, long sizeMemBytes, int numberOfElems,FSTCoder coder) throws Exception {
        super(mappedFile, keyLen, sizeMemBytes, numberOfElems, coder);
        tmpKey = new LeftCutStringByteSource(null,0,keyLen);
    }

    /**
     * create a memory mapped offheap hashmap
     * @param file - the file location. If file exists, it will be loaded
     *             Attention: in case classes changed you need to delete files as
     *             objects won't be compatible (except you made use of fst versioning features)
     * @param keylen - the maximum len of a key
     * @param size   - max size of file (can be GB, check OS settings to avoid eager write-to-disk)
     * @param numelem - estimated number of key-value pairs (will auto-grow, just educated guess)
     * @throws Exception
     */
    public FSTAsciiStringOffheapMap(String file, int keylen, long size, int numelem) throws Exception {
        this(file,keylen,size,numelem,new DefaultCoder());
    }

    public ByteSource encodeKey(String key) {
        if ( key.length() > tmpKey.length() )
            throw new RuntimeException("key too long: '"+key+"' maxlen:"+tmpKey.length());
        tmpKey.setString(key);
        return tmpKey;
    }

}


