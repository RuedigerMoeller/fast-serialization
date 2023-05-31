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
package org.nustaq.offheap.bytez.malloc;


import java.io.RandomAccessFile;

import java.io.File;
import java.lang.foreign.SegmentScope;
import java.nio.channels.FileChannel;

/**
 * Bytez allocated inside a memory mapped file. Some Mmap file stuff handling is copied from OpenHFT library (too big to depend on for fst),
 * a great tool for all kind of binary/low level java stuff. Check it out at github.
 */
public class MMFBytez extends MemoryBytez {
    private File file;
    private SegmentScope scope;

    public MMFBytez(String filePath, long length, boolean clearFile) throws Exception {
        init(filePath, length, clearFile);
    }

    protected void init(String file, long length, boolean clearFile) throws Exception {
        File f = new File(file);
        if (f.exists() && clearFile) {
            f.delete();
        }
        if ( ! f.exists() ) {
            f.getParentFile().mkdirs();
            f.createNewFile();
        }

        scope = SegmentScope.auto();
        memseg = new RandomAccessFile(f, "rw").getChannel().map(FileChannel.MapMode.READ_WRITE, 0, length, scope);
///        memseg = MemorySegment.mapFile(f.toPath(), 0, length, FileChannel.MapMode.READ_WRITE, scope);
        this.file = f;
    }

    public void freeAndClose() {
        //scope.close();
    }

    public File getFile() {
        return file;
    }

//    public static void main(String[] args) throws Exception {
//        MMFBytez mmfBytez = new MMFBytez("/tmp/mmf", 2000, false);
//        mmfBytez.put(1999, (byte) 1999);
//        for ( int i = 0; i < mmfBytez.length(); i++ ) {
//            mmfBytez.put(i,(byte)i);
//            System.out.println(mmfBytez.get(i));
//        }
//    }

}