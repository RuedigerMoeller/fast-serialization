package ser;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTClazzInfo.FSTFieldInfo;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

// contributed by rdicroce
public class Bug34 {

        @Test
        public void testCustomSerializer() throws Exception {
                FSTConfiguration FST = FSTConfiguration.createDefaultConfiguration();
                //FST.setForceSerializable(true);
                FST.registerSerializer(Bug34.NonSerializableClass.class, new Serializer(), false);
                FSTObjectOutput out = FST.getObjectOutput();
                out.writeObject(new Bug34.NonSerializableClass());

                FSTObjectInput in = FST.getObjectInput(out.getCopyOfWrittenBuffer());
                assertEquals(NonSerializableClass.class, in.readObject().getClass());
        }

        private static class Serializer extends FSTBasicObjectSerializer {

                @Override
                public void writeObject(FSTObjectOutput out, Object toWrite,
                                FSTClazzInfo clzInfo, FSTFieldInfo referencedBy,
                                int streamPosition) throws IOException {
                        out.writeByte(0);
                }

                @Override
                public Object instantiate(Class objectClass, FSTObjectInput in,
                                FSTClazzInfo serializationInfo, FSTFieldInfo referencee,
                                int streamPosition) throws IOException,
                                ClassNotFoundException, InstantiationException,
                                IllegalAccessException {
                        Object o = new NonSerializableClass();
                        in.readByte();
                        in.registerObject(o, streamPosition, serializationInfo, referencee);
                        return o;
                }

        }

        public static class NonSerializableClass {

        }

}