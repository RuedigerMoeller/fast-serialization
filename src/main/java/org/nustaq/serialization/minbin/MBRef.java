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
package org.nustaq.serialization.minbin;

import java.io.Serializable;

/**
 * Created by ruedi on 02.05.14.
 */
public class MBRef implements Serializable {
    int streamPosition;

    public MBRef(int streamPosition) {
        this.streamPosition = streamPosition;
    }

    public int getStreamPosition() {
        return streamPosition;
    }

    public void setStreamPosition(int streamPosition) {
        this.streamPosition = streamPosition;
    }

    @Override
    public String toString() {
        return "MBRef("+streamPosition +')';
    }
}
