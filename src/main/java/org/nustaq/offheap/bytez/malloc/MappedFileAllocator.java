package org.nustaq.offheap.bytez.malloc;

import org.nustaq.offheap.bytez.Bytez;
import org.nustaq.offheap.bytez.BytezAllocator;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by ruedi on 02.07.14.
 */
public class MappedFileAllocator implements BytezAllocator {

    ByteBuffer readWriteBuf;

    protected void allocFile(File f) throws IOException {
        // create a random access file stream (read-write)
        FileChannel readWriteChannel = new RandomAccessFile(f, "rw").getChannel();
        // map a region of this channel's file directly into memory
        readWriteBuf =
            readWriteChannel.map(FileChannel.MapMode.READ_WRITE, 0, 1024*1024*100);
        readWriteBuf.putInt(100000,13);
    }

    @Override
    public Bytez alloc(long len) {
        return null;
    }

    @Override
    public void free(Bytez bytes) {

    }

    @Override
    public void freeAll() {

    }

    public static void main(String arg[]) throws IOException {
        MappedFileAllocator alloc = new MappedFileAllocator();
        alloc.allocFile( new File("/tmp/fileTest.mapf") );
        System.out.println(alloc);
    }
}
