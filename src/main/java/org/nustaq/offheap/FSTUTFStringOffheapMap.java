package org.nustaq.offheap;

import org.nustaq.offheap.bytez.ByteSource;
import org.nustaq.offheap.bytez.bytesource.UTFStringByteSource;
import org.nustaq.serialization.simpleapi.DefaultCoder;
import org.nustaq.serialization.simpleapi.FSTCoder;

/**
 * Created by ruedi on 05/08/15.
 */
public class FSTUTFStringOffheapMap<V> extends FSTSerializedOffheapMap<String,V> {

    /**
     * see other mem constructor, additional allows to insert a coder.
     * Useful to preregister classes (speed+size gain, no classnames written!).
     * E.g. new DefaultCoder(MyValue.class, MyOtherValue.class, .. )
     */
    public FSTUTFStringOffheapMap(int keyLen, long sizeMemBytes, int numberOfEleems, FSTCoder coder) {
        super(keyLen, sizeMemBytes, numberOfEleems, coder);
    }

    /**
     *
     * @param keyLen - maximum len of a key
     * @param sizeMemBytes - size of memory (not bigger than OS heap
     * @param numberOfEleems - estimation on number of key-value pairs (autogrow, just educated guess)
     */
    public FSTUTFStringOffheapMap(int keyLen, long sizeMemBytes, int numberOfEleems) {
        super(keyLen, sizeMemBytes, numberOfEleems, new DefaultCoder());
    }

    /**
     * see other mem mapped file constructor + additional allows to insert a coder.
     * Useful to preregister classes (speed+size gain, no classnames written!).
     * E.g. new DefaultCoder(MyValue.class, MyOtherValue.class, .. )
     */
    public FSTUTFStringOffheapMap(String mappedFile, int keyLen, long sizeMemBytes, int numberOfElems,FSTCoder coder) throws Exception {
        super(mappedFile, keyLen, sizeMemBytes, numberOfElems, coder);
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
    public FSTUTFStringOffheapMap(String file, int keylen, long size, int numelem) throws Exception {
        this(file,keylen,size,numelem,new DefaultCoder());
    }

    public ByteSource encodeKey(String key) {
        if ( key.length() > keyLen )
            throw new RuntimeException("key too long: '"+key+"' maxlen:"+keyLen);
        return new UTFStringByteSource(key).padLeft(keyLen);
    }


}
