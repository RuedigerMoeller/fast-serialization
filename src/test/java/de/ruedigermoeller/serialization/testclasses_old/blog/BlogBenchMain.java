package de.ruedigermoeller.serialization.testclasses_old.blog;

import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;

import java.io.*;

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
 * Date: 10.10.13
 * Time: 19:48
 * To change this template use File | Settings | File Templates.
 */
public class BlogBenchMain {

    static final int JDK_SER = 0;
    static final int FST_DROPIN = 1;
    static final int FST_FACTORY = 2;
    static final int FST_REUSE = 3;

    public ByteArrayOutputStream bout = new ByteArrayOutputStream(100000);
    ByteArrayInputStream bin;
    public long timWrite;
    public long timRead;
    Object testObject;
    int length;
    private int type = 0; // 0 - fst 1 - JDK
    int iterations;

    public BlogBenchMain(int type, int iterations) {
        this.type = type;
        this.iterations = iterations;
    }

    public void run( Object toWrite ) throws Exception {
        testObject = toWrite;

        System.gc(); // clean heap
        System.out.println("write ..");
        FSTConfiguration fstConf = FSTConfiguration.getDefaultConfiguration();
        ObjectOutputStream jdkOut = null;
        long startTim = System.currentTimeMillis();
        for ( int i = 0; i < iterations; i++ ) {
            bout.reset(); // very cheap
            switch ( type ) {
                case FST_FACTORY:
                    writeTest(fstConf.getObjectOutput(bout), toWrite, type );
                    break;
                case JDK_SER:
                    writeTest(new ObjectOutputStream(bout), toWrite, type);
                    break;
                case FST_DROPIN:
                    writeTest(new FSTObjectOutput(bout), toWrite, type);
                    break;
            }

        }
        timWrite = System.currentTimeMillis()-startTim;
        byte[] bytes = bout.toByteArray();
        length = bytes.length;
        bin = new ByteArrayInputStream(bytes);

        System.gc(); // clean heap
        System.out.println("read ..");
        startTim = System.currentTimeMillis();
        for ( int i = 0; i < iterations; i++ ) {
            switch ( type ) {
                case FST_FACTORY:
                    readTest(fstConf.getObjectInput(bin), type);
                    break;
                case JDK_SER:
                    readTest(new ObjectInputStream(bin), type);
                    break;
                case FST_DROPIN:
                    readTest(new FSTObjectInput(bin), type);
                    break;
            }
            bin.reset();
        }
        timRead = System.currentTimeMillis()-startTim;
    }

    public void dumpRes(String type) {
        System.out.println(type+"  Size:" + length + ",  TimeRead: " + (timRead * 1000 * 1000 / iterations) + " nanoseconds,   TimeWrite: " + (timWrite * 1000 * 1000 / iterations) + " nanoseconds");
    }

    protected void readTest(ObjectInput in, int type) throws Exception {
        in.readObject();
        if ( type != FST_FACTORY )
            in.close();
    }

    protected void writeTest(ObjectOutput out, Object toWrite, int type) throws Exception {
        out.writeObject(toWrite);
        if ( type == FST_FACTORY )
            out.flush();
        else
            out.close();
    }

    public static void main(String args[]) throws Exception {

        int iterations = 1000000;

        BlogBenchMain fst0 = new BlogBenchMain(FST_DROPIN,iterations);
        BlogBenchMain fst1 = new BlogBenchMain(FST_FACTORY,iterations);
        BlogBenchMain jdk = new BlogBenchMain(JDK_SER,iterations);

        Object test = new BlogBench(13);

        // warm up
        jdk.run(test);
        fst0.run(test);
//        fst1.run(test); unfair double warmup

        //test
        fst1.run(test);
        fst1.dumpRes("FST Factory - Plain Serializable");

        //test
        fst0.run(test);
        fst0.dumpRes("FST Dropin - Plain Serializable");

        jdk.run(test);
        jdk.dumpRes("JDK - Plain Serializable ");

        test = new BlogBenchExternalizable(13);
        fst1.run(test);
        fst1.dumpRes("FST Factory - Externalizable");

        fst0.run(test);
        fst0.dumpRes("FST Dropin - Externalizable");

        jdk.run(test);
        jdk.dumpRes("JDK - Externalizable ");

        test = new BlogBenchAnnotated(13);
        fst1.run(test);
        fst1.dumpRes("FST Factory - Annotated");

        fst0.run(test);
        fst0.dumpRes("FST Dropin - Annotated");

        FSTConfiguration.getDefaultConfiguration().registerClass(BlogBenchAnnotated.class);

        fst0.run(test);
        fst0.dumpRes("FST Dropin - Annotated class registered");

        test = new BlogBenchAnnotated[] {
                new BlogBenchAnnotated(13),
                new BlogBenchAnnotated(13),
                new BlogBenchAnnotated(13),
                new BlogBenchAnnotated(13),
                new BlogBenchAnnotated(13),
                new BlogBenchAnnotated(13),
                new BlogBenchAnnotated(13),
                new BlogBenchAnnotated(13),
                new BlogBenchAnnotated(13),
                new BlogBenchAnnotated(13)
        };

        fst0.run(test);
        fst0.dumpRes("FST Dropin - Annotated class registered bulk 10");

        test = new BlogBenchExternalizable[] {
                new BlogBenchExternalizable(13),
                new BlogBenchExternalizable(13),
                new BlogBenchExternalizable(13),
                new BlogBenchExternalizable(13),
                new BlogBenchExternalizable(13),
                new BlogBenchExternalizable(13),
                new BlogBenchExternalizable(13),
                new BlogBenchExternalizable(13),
                new BlogBenchExternalizable(13),
                new BlogBenchExternalizable(13)
        };
        jdk.run(test);
        jdk.dumpRes("JDK Externlizable bulk 10");

    }
}
