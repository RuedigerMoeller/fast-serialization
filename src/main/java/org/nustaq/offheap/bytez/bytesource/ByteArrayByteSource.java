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

import org.nustaq.offheap.bytez.ByteSource;

/**
 * Created by ruedi on 27.06.14.
 */
public class ByteArrayByteSource implements ByteSource
{

    byte arr[];
    int off;
    int len;

    public ByteArrayByteSource(byte[] arr) {
        this.arr = arr;
        off = 0;
        len = arr.length;
    }

    public ByteArrayByteSource(byte[] arr, int off) {
        this.arr = arr;
        this.off = off;
        len = arr.length-off;
    }

    public ByteArrayByteSource(byte[] arr, int off, int len) {
        this.arr = arr;
        this.off = off;
        this.len = len;
    }

    @Override
    public byte get(long index) {
        return arr[((int) (index + off))];
    }

    @Override
    public long length() {
        return len;
    }

    public byte[] getArr() {
        return arr;
    }

    public void setArr(byte[] arr) {
        this.arr = arr;
    }

    public int getOff() {
        return off;
    }

    public void setOff(int off) {
        this.off = off;
    }

    public void setLen(int len) {
        this.len = len;
    }
}
