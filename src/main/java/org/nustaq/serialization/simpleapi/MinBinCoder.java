package org.nustaq.serialization.simpleapi;

import org.nustaq.serialization.*;

/**
 * Created by ruedi on 09.11.14.
 *
 * see DefaultCoder. Uses MinBinCodec
 *
 */
public class MinBinCoder extends DefaultCoder {

    public MinBinCoder(boolean shared, Class ... toPreRegister) {
        conf = FSTConfiguration.createMinBinConfiguration();
        conf.setShareReferences(shared);
        if ( toPreRegister != null && toPreRegister.length > 0 ) {
            conf.registerCrossPlatformClassMappingUseSimpleName(toPreRegister);
        }
        input = new FSTObjectInput(conf);
        output = new FSTObjectOutput(conf);
    }

    public MinBinCoder(Class ... preregister) {
        this(true,preregister);
    }

    public MinBinCoder() {
        this(true);
    }

}
