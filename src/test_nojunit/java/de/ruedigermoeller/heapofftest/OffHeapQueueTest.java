package de.ruedigermoeller.heapofftest;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectOutput;
import de.ruedigermoeller.serialization.testclasses.HtmlCharter;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

/**
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 * <p/>
 * Date: 18.06.13
 * Time: 21:05
 * To change this template use File | Settings | File Templates.
 */
public class OffHeapQueueTest {

    private static final int QTESTIT = 1000000;

    static class QueueWriter extends Thread {
        FSTOffheapQueue queue;
        private final CountDownLatch latch;
        private Object toWrite;
        private final int iter;
        private boolean encode = true;
        FSTOffheapQueue.ConcurrentWriteContext context;
        private final boolean useConc;


        public QueueWriter(FSTOffheapQueue q, CountDownLatch latch, Object toWrite, int iter, boolean enc, boolean useConc) throws IOException {
            queue = q;
            this.latch = latch;
            this.toWrite = toWrite;
            this.iter = iter;
            this.useConc = useConc;
            encode = enc;
            context = q.createConcurrentWriter();
            if ( ! enc ) {
                FSTObjectOutput out = new FSTObjectOutput(FSTConfiguration.createDefaultConfiguration());
                out.writeObject(toWrite);
                byte[] buffer = out.getBuffer();
                byte tw[] = new byte[out.getWritten()];
                System.arraycopy(buffer,0,tw,0,out.getWritten());
                this.toWrite = tw;
            }
        }

        public void run() {
            for (int i = 0; i < iter; i++) {
                try {
                    if ( encode ) {
                        if (useConc) {
                            queue.addConcurrent(toWrite);
                        } else {
                            context.add(toWrite);
                        }
                    } else {
                        queue.addBytes((byte[])toWrite);
                    }
                } catch (IOException e) {
                    System.exit(-1);
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                if ( i % 1000 == 9999 ) {
                    System.out.print(">");
                }
            }
            latch.countDown();
        }
    }

    static class QueueReader extends Thread {
        FSTOffheapQueue queue;
        int toRead = 0;
        int sumread = 0;
        private final CountDownLatch latch;
        boolean decode = false;
        FSTOffheapQueue.ConcurrentReadContext context;
        boolean conc;

        public QueueReader(FSTOffheapQueue q, int toRead, CountDownLatch latch, boolean dec, boolean useConc) throws IOException {
            queue = q;
            this.toRead = toRead;
            this.latch = latch;
            this.decode = dec;
            context = queue.createConcurrentReader();
            conc = useConc;
        }

        public void run() {
            FSTOffheapQueue.ByteBufferResult result = new FSTOffheapQueue.ByteBufferResult();
            int len[] = {0};
            for (int i = 0; i < toRead; i++) {
                try {
                    if ( decode ) {
                        if ( conc ) {
                            queue.takeObjectConcurrent();
                        } else {
                            context.takeObject(len);
                            sumread+=len[0];
                        }
                    } else {
                        queue.takeBytes(result);
                        sumread+=result.len;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
            latch.countDown();
        }
    }
    public static void testQu(HtmlCharter charter) throws IOException, InterruptedException, InstantiationException, IllegalAccessException, ClassNotFoundException, ExecutionException {
        String str ="slkdflskdenlekldkmlsdklsdkfmsldkmflkemclekmclsdkmclseoijowijowidjwoeidjwoeidjlkdscfjvbfkn�sdcmsldkcm";
        str += str;str += str;str += str;str += str;str += str;str += str;str += str;
        FSTOffheapQueue queue = new FSTOffheapQueue(500,4);
        long tim = System.currentTimeMillis();
        for ( int j = 0; j < 50; j++ ) {
            for (int i = 0; i < 1000; i++ ) {
                String o = str+"String " + i;
                queue.add(o);
            }
            for (int i = 0; i < 1000; i++ ) {
                String s = (String) queue.takeObject(null);
                if ( ! s.endsWith("String " + i) ) {
                    throw new RuntimeException("queue bug");
                }
            }
        }
        tim = System.currentTimeMillis()-tim;
        System.out.println("S-S "+tim);
        queue = new FSTOffheapQueue(500,4);
        tim = System.currentTimeMillis();
        for ( int j = 0; j < 50; j++ ) {
            for (int i = 0; i < 1000; i++ ) {
                String o = str+"String " + i;
                queue.addConcurrent(o);
            }
            for (int i = 0; i < 1000; i++ ) {
                String s = (String) queue.takeObject(null);
                if ( ! s.endsWith("String " + i) ) {
                    System.out.println("queue bug conc write '"+s+"' expect '"+"String " + i+"'");
                }
            }
        }
        tim = System.currentTimeMillis()-tim;
        System.out.println("C-S "+tim);
        queue = new FSTOffheapQueue(500,4);
        tim = System.currentTimeMillis();
        for ( int j = 0; j < 50; j++ ) {
            for (int i = 0; i < 1000; i++ ) {
                String o = str+"String " + i;
                queue.add(o);
            }
            for (int i = 0; i < 1000; i++ ) {
                String s = (String) queue.takeObjectConcurrent();
                if ( ! s.endsWith("String " + i) ) {
                    throw new RuntimeException("queue bug");
                }
            }
        }
        tim = System.currentTimeMillis()-tim;
        System.out.println("S-C "+tim);
        queue = new FSTOffheapQueue(500,4);
        tim = System.currentTimeMillis();
        for ( int j = 0; j < 50; j++ ) {
            for (int i = 0; i < 1000; i++ ) {
                String o = str+"String " + i;
                queue.addConcurrent(o);
            }
            for (int i = 0; i < 1000; i++ ) {
                String s = (String) queue.takeObjectConcurrent();
                if ( ! s.endsWith("String " + i) ) {
                    throw new RuntimeException("queue bug");
                }
            }
        }
        tim = System.currentTimeMillis()-tim;
        System.out.println("C-C "+tim);
        System.out.println("qtest ok");
    }

    public static void benchQu(HtmlCharter charter, int numreader, int numWriter, boolean encwrite, boolean decread, boolean useConc, boolean concread) throws IOException, InterruptedException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        FSTOffheapQueue queue = new FSTOffheapQueue(50,4);
        CountDownLatch latch = new CountDownLatch(numreader+numWriter);
        QueueReader reader[] = new QueueReader[numreader];
        OffHeapTest.ExampleOrder order = new OffHeapTest.ExampleOrder();

        charter.openChart("Offheap Queue - "+(useConc?"Conc Encoding W":"Single W")+" "+(concread?"Conc Decoding R":"Single R")+" - "+((encwrite&&!decread)?"writer encodes.":"reader decodes.")+" "+numreader+" reader, "+numWriter+" writer. "+QTESTIT+" objects written/read." );

//        Trader order = Trader.generateTrader(13, true);
//        SmallThing thing = new SmallThing();

        long tim = System.currentTimeMillis();
        for (int i=0; i < numreader; i++) {
            reader[i] = new QueueReader(queue,QTESTIT/numreader, latch,decread, concread);
            reader[i].start();
        }

        for (int i=0; i < numWriter; i++) {
            new QueueWriter(queue, latch, order, QTESTIT/numWriter+1, encwrite, useConc ).start();
        }

        latch.await();
        tim = System.currentTimeMillis()-tim;
        int sumread = 0;
        for (int i = 0; i < reader.length; i++) {
            QueueReader queueReader = reader[i];
            sumread+=reader[i].sumread;
        }
        System.out.println("heap queue "+(useConc?"Conc Encoding W":"Single W")+" "+(concread?"Conc Decoding R":"Single R")+" - "+((encwrite&&!decread)?"writer encodes.":"reader decodes.")+" "+numreader+" reader, "+numWriter+" writer "+QTESTIT+" writes time:"+tim+" obj/sec:"+(QTESTIT/tim)*1000+" MB read "+(sumread)/1000/1000);
        charter.chartBar("time", (int) tim, 500, "#a0a0ff");
        charter.chartBar("obj/sec", (int) (QTESTIT / tim) * 1000, 10000, "#a0ffa0");
        charter.chartBar("MB/sec", (int) (sumread/tim)*1000/1000/1000,2,"#ffa0a0");
        charter.closeChart();
    }


}
