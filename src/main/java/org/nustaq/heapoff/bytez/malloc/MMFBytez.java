package org.nustaq.heapoff.bytez.malloc;

import sun.misc.Cleaner;
import sun.nio.ch.FileChannelImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Bytez allocated MFMapped. Mmap file stuff handling is copied from OpenHFT library (too big to depend on for fst),
 * a great tool for all kind of binary/low level java stuff. Check it out at github.
 */
public class MMFBytez extends MallocBytez {
    private File file;
    private FileChannel fileChannel;
    private Cleaner cleaner;
    private long address;

    public MMFBytez(String filePath, long minlength, boolean clearFile) throws Exception {
        super(0, 0);
        init( filePath, minlength, clearFile );
    }

    protected void init(String file, long minlength, boolean clearFile) throws Exception {
        File f = new File(file);
        this.file = f;
        // create a random access file stream (read-write)
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        FileChannel fileChannel = raf.getChannel();
        // map a region of this channel's file directly into memory
        this.fileChannel = raf.getChannel();
        this.address = map0(fileChannel, imodeFor(FileChannel.MapMode.READ_WRITE), 0L, minlength);
        this.cleaner = Cleaner.create(this, new Unmapper(address, minlength, fileChannel));
    }

    /**
     * Trick copied from OpenHFT library (too big to depend on for fst)
     *
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
