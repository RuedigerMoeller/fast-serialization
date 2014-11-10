package org.nustaq.serialization.simpleapi;

import org.nustaq.serialization.FSTConfiguration;

/**
 * Created by ruedi on 09.11.14.
 *
 * see DefaultCoder. Uses MinBinCodec
 *
 */
public class MinBinCoder extends DefaultCoder {

    public MinBinCoder() {
        conf = FSTConfiguration.createCrossPlatformConfiguration();
    }

    /**
     * registers given classes as shortnames (using class.simpleName).
     * This means the resulting stream will contain short names instead of full qualified names for
     * these classes.
     *
     * @param preregister
     */
    public MinBinCoder(Class... preregister) {
        this();
        conf.registerCrossPlatformClassMappingUseSimpleName(preregister);
    }

}
