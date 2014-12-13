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
 * implement bytesource on top a string, the string is right aligned inside the byte source
 */
public class LeftCutStringByteSource extends AsciiStringByteSource {


    public LeftCutStringByteSource(String arr) {
        super(arr);
    }

    public LeftCutStringByteSource(String arr, int off) {
        super(arr, off);
    }

    public LeftCutStringByteSource(String arr, int off, int len) {
        super(arr, off, len);
    }

    @Override
    public byte get(long index) {
        int shift = len - string.length();
        if ( index < shift ) {
            return 0;
        }
        return super.get(index-shift);
    }

}
