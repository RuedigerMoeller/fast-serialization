package de.ruedigermoeller.serialization.testclasses.docusample;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.cedarsoftware.util.DeepEquals;

import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;

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
 * Date: 15.06.13
 * Time: 22:26
 * To change this template use File | Settings | File Templates.
 */

/**
 * Tests multithreaded en/decoding.
 */
public class FSTTestApp {

    public void test(int i) throws IOException {
        FileOutputStream fout = null;
        FileInputStream in = null;
        try {
            fout = new FileOutputStream("/tmp/test-"+i+".tmp");
            Object[] toWrite = {
                    // fixme
//                    PrimitiveArrays.createPrimArray(),
//                    Trader.generateTrader(i, true),
//                    new FrequentCollections(),
//                    new LargeNativeArrays(),
//                    Primitives.createPrimArray()
            };

            mywriteMethod(fout, toWrite);

            in = new FileInputStream("/tmp/test-"+i+".tmp");
            Object read = myreadMethod(in);
            in.close();
            System.out.println(i+" SUCCESS:" + DeepEquals.deepEquals(read, toWrite));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if ( fout != null )
                fout.close();
            if ( in != null )
                in.close();
        }
    }

    public Object myreadMethod(InputStream stream) throws IOException, ClassNotFoundException {
        FSTObjectInput in = new FSTObjectInput(stream);
        Object result = in.readObject();
        in.close();
        return result;
    }

    public void mywriteMethod( OutputStream stream, Object toWrite ) throws IOException {
        FSTObjectOutput out = new FSTObjectOutput(stream);
        out.writeObject( toWrite );
        out.close();
    }

    public static void main(String arg[]) throws IOException, InterruptedException {
        System.setProperty("fst.unsafe","true");
        ExecutorService executorService = Executors.newFixedThreadPool(501);
        int count = 500;
        final CountDownLatch latch = new CountDownLatch(count);
        for ( int i = 0; i < count;  i++) {
            final int finalI = i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        new FSTTestApp().test(finalI);
                        latch.countDown();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        latch.await();
        executorService.shutdown();
    }

}
