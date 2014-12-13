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

import org.nustaq.offheap.bytez.malloc.MallocBytez;
import org.nustaq.offheap.bytez.onheap.HeapBytez;
import org.nustaq.serialization.*;
import org.nustaq.serialization.coders.FSTBytezDecoder;
import org.nustaq.serialization.coders.FSTBytezEncoder;
import org.nustaq.serialization.util.FSTUtil;

import java.io.IOException;

/**
 * Created by ruedi on 13.11.14.
 *
 * A coder writing fast binary encoding using unsafe to an underlying byte array ZERO COPY.
 *
 * Future version may choose to operate on DirectByteBuffer in case unsafe vanishes
 *
 * Note there is some copied code from OffHeaüCoder to avoid losing performance caused
 * by polymorphic method dispatch.
 *
 * ***********************************************************************
 * USE ONLY IF DEFAULTCODER (no unsafe) HAS BEEN PROVEN TO SLOW YOU DOWN.
 * ***********************************************************************
 *
 */
public class OnHeapCoder implements FSTCoder {

    protected FSTConfiguration conf;
    protected HeapBytez writeTarget;
    protected HeapBytez readTarget;
    protected FSTObjectOutput out;
    protected FSTObjectInput in;

    public OnHeapCoder() {
        this(true);
    }

    public OnHeapCoder(boolean sharedRefs) {
        conf = FSTConfiguration.createFastBinaryConfiguration();
        conf.setShareReferences(sharedRefs);
        writeTarget = new HeapBytez(new byte[0]);
        readTarget = new HeapBytez(new byte[0]);
        conf.setStreamCoderFactory(new FSTConfiguration.StreamCoderFactory() {
            @Override
            public FSTEncoder createStreamEncoder() {
                FSTBytezEncoder fstBytezEncoder = new FSTBytezEncoder(conf, writeTarget);
                fstBytezEncoder.setAutoResize(false);
                return fstBytezEncoder;
            }
            @Override
            public FSTDecoder createStreamDecoder() {
                return new FSTBytezDecoder(conf,readTarget);
            }
        });
        if ( sharedRefs ) {
            out = conf.getObjectOutput();
            in = conf.getObjectInput();
        } else {
            out = new FSTObjectOutputNoShared(conf);
            in = new FSTObjectInputNoShared(conf);
        }
    }

    /**
     * throw
     * @param preregister
     */
    public OnHeapCoder( Class ... preregister ) {
        this();
        conf.registerClass(preregister);
    }

    public OnHeapCoder( boolean sharedRefs, Class ... preregister ) {
        this(sharedRefs);
        conf.registerClass(preregister);
    }

    /**
     * throws FSTBufferTooSmallExcpetion in case object does not fit into given range
     * Zero Copy method
     *
     * @param o
     * @param availableSize
     * @throws java.io.IOException
     * @return number of bytes written to the memory region
     */
    @Override
    public int toByteArray( Object o, byte arr[], int startIndex, int availableSize ) {
        out.resetForReUse();
        writeTarget.setBase(arr, startIndex, availableSize);
        try {
            out.writeObject(o);
        } catch (IOException e) {
            throw FSTUtil.rethrow(e);
        }
        int written = out.getWritten();
        return written;
    }

    protected byte[] buff = new byte[4096];
    @Override
    public byte[] toByteArray(Object o) {
        try {
            out.resetForReUse();
            writeTarget.setBase(buff, 0, buff.length);
            try {
                out.writeObject(o);
            } catch (IOException e) {
                throw FSTUtil.rethrow(e);
            }
            return out.getCopyOfWrittenBuffer();
        } catch (FSTBufferTooSmallException ex) {
            buff = new byte[buff.length*2];
            return toByteArray(o);
        }
    }

    @Override
    public FSTConfiguration getConf() {
        return conf;
    }

    /**
     * throws FSTBufferTooSmallExcpetion in case object does not fit into given range
     *
     * @param arr
     * @param startIndex
     * @param availableSize
     * @return
     * @throws Exception
     */
    @Override
    public Object toObject( byte arr[], int startIndex, int availableSize) {
        try {
            in.resetForReuse(null);
            readTarget.setBase(arr,startIndex,availableSize);
            Object o = in.readObject();
            return o;
        } catch (Exception e) {
            throw FSTUtil.rethrow(e);
        }
    }

    /**
     * decode object into byte array (at position null)
     *
     * @param arr
     * @return
     * @throws Exception
     */
    @Override
    public Object toObject(byte[] arr) {
        return toObject(arr,0,arr.length);
    }


}
