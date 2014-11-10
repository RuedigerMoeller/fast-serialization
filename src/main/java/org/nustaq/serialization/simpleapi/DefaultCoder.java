package org.nustaq.serialization.simpleapi;

import org.nustaq.serialization.FSTConfiguration;

/**
 * Created by ruedi on 09.11.14.
 *
 * Encodes Objects to byte arrays and vice versa using slight value compression and a platform neutral data
 * layout (no diff regarding big/little endian). Implementation is conservative (no unsafe)
 *
 * As this makes use of the stream oriented API, operation is not zero copy. However this is not too significant
 * compared to cost of serialization.
 *
 * KEEP and reuse instances, creation is expensive.
 *
 * This class can be used from multiple threads. If you are sure to run into contention issues,
 * consider using a threadlocal to hold your coder instance.
 *
 */
public class DefaultCoder {

    protected FSTConfiguration conf;

    public DefaultCoder() {
        conf = FSTConfiguration.createDefaultConfiguration();
    }

    /**
     * throw
     * @param preregister
     */
    public DefaultCoder( Class ... preregister ) {
        this();
        conf.registerClass(preregister);
    }

    /**
     * will throw an FSTBuffer2SmallException if buffer is too small.
     * The required size is part of the exception.
     *
     */
    public void toByteArray( Object obj, byte result[], int resultOffset ) {

    }

    public byte[] asByteArray( Object o ) {
        return null;
    }

    public Object toObject( byte arr[], int off ) {
        return null;
    }

}
