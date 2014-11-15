package org.nustaq.serialization.simpleapi;

import org.nustaq.serialization.FSTConfiguration;

/**
 * Created by ruedi on 15.11.14.
 */
public interface FSTCoder {

    public Object toObject( byte arr[], int startIndex, int availableSize);
    public Object toObject( byte arr[] );

    public int toByteArray( Object obj, byte result[], int resultOffset, int availableSize );
    public byte[] toByteArray( Object o );

    // take care: changes in setup have to happen before first use
    public FSTConfiguration getConf();

}
