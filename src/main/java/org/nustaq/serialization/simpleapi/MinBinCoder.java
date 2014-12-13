/*
 * Copyright 2014 Ruediger Moeller.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
