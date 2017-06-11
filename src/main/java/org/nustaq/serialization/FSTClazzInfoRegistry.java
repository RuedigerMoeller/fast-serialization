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
package org.nustaq.serialization;

import org.nustaq.serialization.util.FSTMap;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: Möller
 * Date: 03.11.12
 * Time: 13:11
 * To change this template use File | Settings | File Templates.
 */
public class FSTClazzInfoRegistry {

    private final FSTMap mInfos = new FSTMap(97);
    private final FSTSerializerRegistry serializerRegistry = new FSTSerializerRegistry();
    private boolean ignoreAnnotations = false;
    private final AtomicBoolean rwLock = new AtomicBoolean(false);
    private boolean structMode = false;

    FSTClazzInfoRegistry() {
    }

    FSTClazzInfo getCLInfo(Class c, FSTConfiguration conf) {
        while(!rwLock.compareAndSet(false,true));
        try {
            FSTClazzInfo res = (FSTClazzInfo) mInfos.get(c);
            if (res == null) {
                if (c == null) {
                    rwLock.set(false);
                    throw new NullPointerException("Class is null");
                }
                res = new FSTClazzInfo(conf, c, ignoreAnnotations);
                mInfos.put(c, res);
            }
            return res;
        } finally {
            rwLock.set(false);
        }
    }

    FSTSerializerRegistry getSerializerRegistry() {
        return serializerRegistry;
    }

    final boolean isIgnoreAnnotations() {
        return ignoreAnnotations;
    }

    public void setIgnoreAnnotations(boolean ignoreAnnotations) {
        this.ignoreAnnotations = ignoreAnnotations;
    }


    void setSerializerRegistryDelegate(FSTSerializerRegistryDelegate delegate) {
        serializerRegistry.setDelegate(delegate);
    }

    void setStructMode(boolean structMode) {
        this.structMode = structMode;
    }

    boolean isStructMode() {
        return structMode;
    }
}
