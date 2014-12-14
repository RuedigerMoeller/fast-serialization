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
import org.nustaq.serialization.util.FSTUtil;

import java.io.IOException;

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
 * Works similar to the unsafe coders, but does not use Unsafe. Note that reading and writing
 * coder must match each other in type and configuration.
 *
 */
public class DefaultCoder implements FSTCoder {

    protected FSTConfiguration conf;
    FSTObjectInput input;
    FSTObjectOutput output;

    public DefaultCoder(boolean shared, Class ... toPreRegister) {
        conf = FSTConfiguration.createDefaultConfiguration();
        conf.setShareReferences(shared);
        if ( toPreRegister != null && toPreRegister.length > 0 ) {
            conf.registerClass(toPreRegister);
        }
        if ( shared ) {
            input = new FSTObjectInput(conf);
            output = new FSTObjectOutput(conf);
        } else {
            input = new FSTObjectInputNoShared(conf);
            output = new FSTObjectOutputNoShared(conf);
        }
    }

    public DefaultCoder( Class ... preregister ) {
        this(true, preregister);
    }

    public DefaultCoder() {
        this(true);
    }

    /**
     * will throw an FSTBufferTooSmallException if buffer is too small.
     */
    public int toByteArray( Object obj, byte result[], int resultOffset, int avaiableSize ) {
        output.resetForReUse();
        try {
            output.writeObject(obj);
        } catch (IOException e) {
            throw FSTUtil.rethrow(e);
        }
        int written = output.getWritten();
        if ( written > avaiableSize ) {
            throw FSTBufferTooSmallException.Instance;
        }
        System.arraycopy(output.getBuffer(),0,result,resultOffset, written);
        return written;
    }

    public byte[] toByteArray( Object o ) {
        output.resetForReUse();
        try {
            output.writeObject(o);
        } catch (IOException e) {
            throw FSTUtil.rethrow(e);
        }
        return output.getCopyOfWrittenBuffer();
    }

    @Override
    public FSTConfiguration getConf() {
        return conf;
    }


    public Object toObject( byte arr[], int off, int len ) {
        try {
            if ( off == 0 ) {
                input.resetForReuseUseArray(arr);
            } else {
                input.resetForReuseCopyArray(arr, off, len);
            }
            return input.readObject();
        } catch (Exception e) {
            throw FSTUtil.rethrow(e);
        }
    }

    public Object toObject( byte arr[] ) {
        return toObject(arr,0,arr.length);
    }

}
