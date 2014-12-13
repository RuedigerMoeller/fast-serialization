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

import sun.misc.Cleaner;
import sun.nio.ch.FileChannelImpl;

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
    private Cleaner cleaner;

    public MMFBytez(String filePath, long length, boolean clearFile) throws Exception {
        super(0, 0);
        init( filePath, length, clearFile );
    }

    protected void init(String file, long length, boolean clearFile) throws Exception {
        File f = new File(file);
        if ( f.exists() && clearFile ) {
            f.delete();
        }
        this.file = f;

        if ( f.exists() ) {
            length = f.length();
        }

        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        raf.setLength(length); // FIXME: see stackoverflow. does not work always
        FileChannel fileChannel = raf.getChannel();

        this.fileChannel = raf.getChannel();
        this.baseAdress = map0(fileChannel, imodeFor(FileChannel.MapMode.READ_WRITE), 0L, length);
        this.length = length;
        this.cleaner = Cleaner.create(this, new Unmapper(baseAdress, length, fileChannel));
    }

    public void freeAndClose() {
        cleaner.clean();
    }



    /**
     * stuff copied from OpenHFT library (too big to depend on for fst) ...
     *
     * Copyright 2013 Peter Lawrey
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *         http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */

    /**
     * @param fileChannel
     * @param imode
     * @param start
     * @param size
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */

    private static final int MAP_RO = 0;
    private static final int MAP_RW = 1;
    private static final int MAP_PV = 2;

    private static long map0(FileChannel fileChannel, int imode, long start, long size) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method map0 = fileChannel.getClass().getDeclaredMethod("map0", int.class, long.class, long.class);
        map0.setAccessible(true);
        return (Long) map0.invoke(fileChannel, imode, start, size);
    }

    private static void unmap0(long address, long size) throws Exception {
        Method unmap0 = FileChannelImpl.class.getDeclaredMethod("unmap0", long.class, long.class);
        unmap0.setAccessible(true);
        unmap0.invoke(null, address, size);
    }

    private static int imodeFor(FileChannel.MapMode mode) {
        int imode = -1;
        if (mode == FileChannel.MapMode.READ_ONLY)
            imode = MAP_RO;
        else if (mode == FileChannel.MapMode.READ_WRITE)
            imode = MAP_RW;
        else if (mode == FileChannel.MapMode.PRIVATE)
            imode = MAP_PV;
        assert (imode >= 0);
        return imode;
    }

    static class Unmapper implements Runnable {
        private final long size;
        private final FileChannel channel;
        private volatile long address;

        Unmapper(long address, long size, FileChannel channel) {
            assert (address != 0);
            this.address = address;
            this.size = size;
            this.channel = channel;
        }

        public void run() {
            if (address == 0)
                return;

            try {
                unmap0(address, size);
                address = 0;

                if (channel.isOpen()) {
                    channel.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
