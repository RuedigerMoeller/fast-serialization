package de.ruedigermoeller.serialization.testclasses_old.big;

/*
 * License placeholder
 */


import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import de.ruedigermoeller.serialization.util.FSTInputStream;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

/**
 *
 * @author dmitriya
 */
public class BigTest {

    /**
     * De serialization or just programmatically create objects.
     */
    static boolean doSerialize = true;
    /**
     * How many objects to generate.
     */
    static final int numObjects = 500 * 1000;
    /**
     * How many times to repeat the serialization/deserialization cycle.
     */
    static final int numRounds = 3;



    // other vars
    static final Random rnd;
    static {
        rnd = new Random();
        rnd.setSeed(42L);
    }
    static String cachePath = Paths.get(".", "SomeObjArray.ser").normalize().toAbsolutePath().toString();
    static final int minStrLen = 15;
    static final int maxStrLen = 15;
    static final int minArrLen = 10;
    static final int maxArrLen = 10;


    public static void main(String[] args) throws IOException, Exception {
        test();
        Thread.sleep(10000000);
    }

    private static void test() {
        try {
        for (int j = 0; j < numRounds; j++) {
            System.out.printf("\nRound %d\n\n", j);
            SomeObj[] objects = generateSomeObjArray(numObjects);



            if (doSerialize) {

                try {
                    serialize(cachePath, objects);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FSTConfiguration.getDefaultConfiguration().clearCaches();
                // forget about original objects array, it is now eligible for GC
                objects = null;
                // now we have our array back + FST overhead
                SomeObj[] deserialized = new SomeObj[0];
                try {
                    deserialized = deserialize(cachePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                FSTConfiguration.getDefaultConfiguration().clearCaches();
                try {
                    Files.delete(Paths.get(cachePath));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // create another similar array - OOM Error, however if
                // FST memory was released, there would have been enough memory
                // to create the array
                SomeObj[] objects2 = generateSomeObjArray(numObjects);


                // these lines are here to ensure GC doesn't touch 'objects2 'and 'deserialized'
                SomeObj obj = objects2[rnd.nextInt(objects2.length)];
                obj = deserialized[rnd.nextInt(deserialized.length)];

            } else {

                // generate another 500k objects
                SomeObj[] objects2 = generateSomeObjArray(numObjects);


                // these lines are here to ensure GC doesn't touch 'objects2 'and 'deserialized'
                SomeObj obj = objects[rnd.nextInt(objects2.length)];
                obj = objects2[rnd.nextInt(objects2.length)];
            }
        }
        } catch(Throwable t) {
            t.printStackTrace();
        }
        System.out.println("Finished.");
    }


    private static SomeObj[] generateSomeObjArray(int size) {
        SomeObj[] objects = new SomeObj[size];
        SomeOtherObj child;
        SomeObj parent;
        for (int i = 0; i < size; i++) {
            child = new SomeOtherObj(generateRandomDoubleArray());
            parent = new SomeObj(generateRandomString(), generateRandomDoubleArray(), child);
            objects[i] = parent;
        }
        return objects;
    }

    private static String generateRandomString() {
        int len = rnd.nextInt((maxStrLen - minStrLen) + 1) + minStrLen;
        char[] chars = new char[len];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char)(rnd.nextInt(('Z' - 'A') + 1) + 'A');
        }
        return new String(chars);
    }

    private static double[] generateRandomDoubleArray() {
        int len = rnd.nextInt((maxArrLen - minArrLen) + 1) + minArrLen;
        double[] doubles = new double[len];
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] = rnd.nextDouble();
        }
        return doubles;
    }

    private static void serialize(String filePath, SomeObj[] peptides) throws IOException {
        // save the object to file
        FileOutputStream fos = null;
        FSTObjectOutput out = null;
        Files.createDirectories(Paths.get(filePath).getParent());
        try {
            fos = new FileOutputStream(filePath);
            out = new FSTObjectOutput(fos);
            long start = System.nanoTime();
            System.out.println("Started serializing (FST lib)..");

            out.writeObject(peptides, SomeObj[].class);

            System.out.printf("Finished serializing (FST lib), tool: %.4f s\n", ((System.nanoTime()-start))/1e9d);
            out.close();
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (out != null)
                out.close();
            if (fos != null)
                fos.close();
        }
    }

    private static SomeObj[] deserialize(String filePath) throws IOException, Exception {
        // read the object from file
        FileInputStream fis = null;
        FSTObjectInput in = null;
        try {
            fis = new FileInputStream(filePath);
            in = new FSTObjectInput(fis);
            long start = System.nanoTime();
            System.out.println("Started deserializing file (FST lib)..");

            SomeObj[] pepsDeserialized = (SomeObj[])in.readObject(SomeObj[].class);

            System.out.printf("Finished deserializing (FST lib), took: %.4f s\n", ((System.nanoTime()-start))/1e9d);
            in.close();
            return pepsDeserialized;
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (in != null)
                in.close();
            if (fis != null)
                fis.close();
        }
    }

    public static class SomeObj implements Serializable {
        String str;
        double[] doubles;
        SomeOtherObj child;

        public SomeObj(String str, double[] doubles, SomeOtherObj child) {
            this.str = str;
            this.doubles = doubles;
            this.child = child;
        }
    }

    public static class SomeOtherObj implements Serializable {
        double[] moreDoubles;

        public SomeOtherObj(double[] moreDoubles) {
            this.moreDoubles = moreDoubles;
        }
    }
}