package de.ruedigermoeller.serialization.testclasses_old.jdkcompatibility;

import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;

import java.io.*;

/**
 * Created by ruedi on 12.02.14.
 */
public class RWObject implements Serializable {

    static class RWA extends RWObject {

        int A = 1;
        int AA = 2;

        private void writeObject( ObjectOutputStream out ) throws IOException {
            System.out.println("RWA:write");
            out.defaultWriteObject();
        }

//        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
//            in.defaultReadObject();
//        }

    }

    int root = 22;
    String test = "test";
    String atest = "atest";
//    private void writeObject( ObjectOutputStream out ) {
//        System.out.println("RWObject:write");
//    }

    private void readObject( ObjectInputStream out ) throws IOException, ClassNotFoundException {
        System.out.println("RWObject:read");
        out.defaultReadObject();
    }

    static class RWB extends RWA {

        int B = 3;
        int BB = 4;
//        private void writeObject( ObjectOutputStream out ) throws IOException {
//            System.out.println("RWB:write");
//            out.defaultWriteObject();
//        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
        }
    }

    static class RWC extends RWB //implements Externalizable
    {

        int C = 5;
        int CC = 6;
        private void writeObject( ObjectOutputStream out ) throws IOException {
            System.out.println("RWC:write");
            out.defaultWriteObject();
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            System.out.println("externalize RWC");
//            ((ObjectOutputStream)out).defaultWriteObject();
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        }
    }

    public static void main(String arg[]) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = new ObjectOutputStream(new ByteArrayOutputStream(50000));
        out.writeObject(new RWC());

        System.out.println("--");

        final ByteArrayOutputStream bout = new ByteArrayOutputStream(50000);
        FSTObjectOutput fsout = FSTConfiguration.getDefaultConfiguration().getObjectOutput(bout);
        final RWC obj = new RWC();
        obj.A = 11; obj.AA = 22; obj.B = 33; obj.BB = 44; obj.C = 55; obj.CC = 66;
        fsout.writeObject(obj);

        FSTObjectInput in = FSTConfiguration.getDefaultConfiguration().getObjectInput(bout.toByteArray());
        RWC rwc = (RWC) in.readObject();
        System.out.println("POK");
    }

}
