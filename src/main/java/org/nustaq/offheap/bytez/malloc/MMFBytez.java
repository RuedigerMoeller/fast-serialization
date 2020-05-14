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


import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;

/**
 * Bytez allocated inside a memory mapped file. Some Mmap file stuff handling is copied from OpenHFT library (too big to depend on for fst),
 * a great tool for all kind of binary/low level java stuff. Check it out at github.
 */
public class MMFBytez extends MallocBytez {
    private File file;
    private FileChannel fileChannel;

    public MMFBytez(String filePath, long length, boolean clearFile) throws Exception {
        super(0, 0);
        init(filePath, length, clearFile);
    }

    protected void init(String file, long length, boolean clearFile) throws Exception {
        File f = new File(file);
        if (f.exists() && clearFile) {
            f.delete();
        }
        this.file = f;

        if (f.exists()) {
            length = f.length();
        }

        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        raf.setLength(length); // FIXME: see stackoverflow. does not work always
        FileChannel fileChannel = raf.getChannel();

        this.fileChannel = raf.getChannel();
        this.baseAdress = // map0(fileChannel, imodeFor(FileChannel.MapMode.READ_WRITE), 0L, length);
        this.length = length;
//        this.cleaner = Cleaner.create(this, new Unmapper(baseAdress, length, fileChannel));
    }

    public void freeAndClose() {
//        cleaner.clean();
    }

    /**
     * hack to update underlying file in slices handed out to app
     */
    public void _setMMFData(File file, FileChannel fileChannel, Object /*Cleaner*/ cleaner) {
        this.file = file;
        this.fileChannel = fileChannel;
        //this.cleaner = cleaner;
    }

    public File getFile() {
        return file;
    }

    public FileChannel getFileChannel() {
        return fileChannel;
    }

    public Object /*Cleaner*/ getCleaner() {
        return null; //cleaner;
    }

}