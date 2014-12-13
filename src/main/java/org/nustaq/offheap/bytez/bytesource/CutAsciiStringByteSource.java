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

package org.nustaq.offheap.bytez.bytesource;

/**
 * Created by ruedi on 27.06.14.
 */

/**
 * returns 0 instead of throwing index exception
 */
public class CutAsciiStringByteSource extends AsciiStringByteSource {
    public CutAsciiStringByteSource(String arr) {
        super(arr);
    }

    public CutAsciiStringByteSource(String arr, int off) {
        super(arr, off);
    }

    public CutAsciiStringByteSource(String arr, int off, int len) {
        super(arr, off, len);
    }

    @Override
    public byte get(long index) {
        if ( index + off <= string.length() )
            return super.get(index);
        return 0;
    }
}
