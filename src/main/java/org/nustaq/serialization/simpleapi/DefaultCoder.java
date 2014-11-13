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
 * This class cannot be used concurrently.
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
     * will throw an FSTBufferTooSmallException if buffer is too small.
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
