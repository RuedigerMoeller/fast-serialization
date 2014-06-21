package org.nustaq.serialization.util;

import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 17.12.12
 * Time: 23:27
 * To change this template use File | Settings | File Templates.
 */
public class FSTOrderedConcurrentJobExecutor {

    abstract public static class FSTRunnable implements Runnable {
        Semaphore sem;
        FSTRunnable next;

        public final void run() {
            runConcurrent();
            sem.release();
        }

        public abstract void runConcurrent();
        public abstract void runInOrder();

    }

    class OrderedRunnable implements Runnable {
        FSTRunnable toRun;

        @Override
        public void run() {
            try {
                toRun.sem.acquire();
                toRun.runInOrder();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                toRun.sem.release();
                gateway.release();
            }
        }
    }

    ExecutorService pool, orderedPool;
    FSTRunnable jobs[];
    OrderedRunnable orderedRunnableCache[];
    Semaphore sems[];
    int curIdx = 0;
    private int threads;
    Semaphore gateway;

    public FSTOrderedConcurrentJobExecutor(int threads) {
        threads *= 2;
        this.pool = Executors.newFixedThreadPool(threads/2);
        this.orderedPool = Executors.newSingleThreadExecutor();
        this.threads = threads;
        jobs = new FSTRunnable[threads];
        gateway = new Semaphore(threads);
        orderedRunnableCache = new OrderedRunnable[threads];
        sems = new Semaphore[threads];
        for (int i = 0; i < jobs.length; i++) {
            orderedRunnableCache[i] = new OrderedRunnable();
            sems[i] = new Semaphore(1);
        }
    }

    public void addCall(final FSTRunnable toRun) throws InterruptedException {
        gateway.acquire();
        if ( jobs[curIdx] == null ) {
            jobs[curIdx] = toRun;
        } else {
            jobs[curIdx].sem.acquire();
            jobs[curIdx].sem.release();
            jobs[curIdx] = toRun;
        }

        toRun.sem = sems[curIdx];
        toRun.sem.acquire();

        OrderedRunnable ord = orderedRunnableCache[curIdx];
        ord.toRun = toRun;

        curIdx = (curIdx+1) % threads;

        orderedPool.execute(ord);
        pool.execute(toRun);

    }

    public void waitForFinish() throws InterruptedException {
        final Semaphore sem = new Semaphore(0);
        orderedPool.execute(new Runnable() {
            @Override
            public void run() {
                sem.release();
            }
        });
        sem.acquire();
    }

    public int getNumThreads() {
        return sems.length/2;
    }

    public static void main( String args[] ) throws InterruptedException {
        FSTOrderedConcurrentJobExecutor jex = new FSTOrderedConcurrentJobExecutor(8);

        final long sumtim = System.currentTimeMillis();
        for ( int i = 0; i < 4; i++) {
            final int finalI = i;
            FSTRunnable job = new FSTRunnable() {

                int count = finalI;

                @Override
                public void runConcurrent() {
                    long tim = System.currentTimeMillis();
                    for ( int j=0; j < 99999999; j++ ) {
                        String s = "asdipo"+j+"oij";
                        int idx = s.indexOf("oij");
                        for ( int k=0; k < 1; k++ ) {
                            String ss = "asdipo"+k+"oij";
                            idx = s.indexOf("oij");
                        }
                    }
                    System.out.println("tim "+count+" "+(System.currentTimeMillis()-tim));
                }

                @Override
                public void runInOrder() {
                    System.out.println(finalI);
                }
            };
            jex.addCall(job);
        }
        jex.waitForFinish();
        System.out.println("all time " + (System.currentTimeMillis() - sumtim));
    }
}
