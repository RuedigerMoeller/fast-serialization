package org.nustaq.serialization.simpleapi;

/**
 * Created by ruedi on 09.11.14.
 *
 * enables zero copy encoding to offheap memory. The encoding is platform dependent (endianess) and
 * no attemps on compression are made.
 *
 * Use case: messaging, offheap en/decoding, tmp preservation of state
 *
 */
public class OffHeapCoder {

    public void writeObjectUnshared( Object o, long address, int availableSize ) {

    }

    public Object readObjectUnshared( long address, int availableSize ) {
        return null;
    }

    public void writeObject( Object o, long address, int availableSize ) {

    }

    public Object readObject( long address, int availableSize ) {
        return null;
    }

}
