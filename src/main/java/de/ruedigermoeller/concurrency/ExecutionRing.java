package de.ruedigermoeller.concurrency;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by ruedi on 15.04.14.
 */
public class ExecutionRing<T> {
    public final static int EMPTY = 0;
    public final static int LOADED = 1;
    public final static int DONE = 2;

    int sizeBits = 2;
    int mask = (1 << sizeBits) - 1;

    ExecEntry<T> entries[] = new ExecEntry[1<<sizeBits];
    Executor workers = Executors.newFixedThreadPool(1<<sizeBits);
    volatile int writeSeq = 0;
    volatile int readSeq = 0;

    public static class ExecEntry<T> {
        volatile int done = EMPTY;
        T data;

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        void done() {
            done = DONE;
        }
    }

    public static interface Work<T> {
        public void doWork(ExecEntry<T> entry);
    }

    public ExecutionRing(final Work<T> work) {
        for ( int i = 0; i < entries.length; i++ ) {
            entries[i] = new ExecEntry<>();
        }
        for ( int i = 0; i < entries.length; i++ ) {
            final int finI = i;
            workers.execute(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        while (entries[finI].done!=LOADED);
                        work.doWork(entries[finI]);
                        entries[finI].done();
                    }
                }
            });
        }
    }

    public boolean isEmpty() {
        return readSeq != writeSeq;
    }

    public boolean add(T data) {
        int tmpSeq = (writeSeq+1)& mask;
        if (entries[tmpSeq].done!=EMPTY) {
            return false;
        }
        entries[tmpSeq].data = data;
        entries[tmpSeq].done = LOADED;
        writeSeq = tmpSeq;
        return true;
    }

    public T take() {
        int tmpSeq = (readSeq+1)& mask;
        if (entries[tmpSeq].done != DONE) {
            return null;
        }
        final T data = entries[readSeq].data;
        readSeq = tmpSeq;
        entries[readSeq].done = EMPTY;
        return data;
    }

    public static void main(String arg[]) {
        ExecutionRing ring = new ExecutionRing(new Work() {
            @Override
            public void doWork(ExecEntry entry) {
                int arr[] = (int[]) entry.getData();
                for (int i = 1; i < arr.length; i++) {
                    while (arr[i] != Integer.MAX_VALUE)
                        arr[i]++;
                }
            }
        });
        int[] res;
        int procCount = 0;
        for (int i=0; i < 20; i++) {
            System.out.println(i);
            final int[] data = {i,1};
            while (!ring.add(data)) {
                while( (res = (int[]) ring.take()) != null ) {
                    System.out.println("pc "+res[0]);
                    procCount++;
                }
            }
        }
        while( !ring.isEmpty() ) {
            res = (int[]) ring.take();
            if (res!=null) {
                System.out.println("pce "+res[0]);
                procCount++;
            }
        }
        System.out.println("finished");
    }

}
