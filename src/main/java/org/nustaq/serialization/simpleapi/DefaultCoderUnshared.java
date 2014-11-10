package org.nustaq.serialization.simpleapi;

/**
 * Created by ruedi on 09.11.14.
 *
 * Same as DefaultCoder, but without cycle detection and shared references. Will throw Exception
 * if cyclic object graphs are encoded.
 *
 */
public class DefaultCoderUnshared extends DefaultCoder {

    public DefaultCoderUnshared() {
    }

    public DefaultCoderUnshared(Class... preregister) {
        super(preregister);
    }

}
