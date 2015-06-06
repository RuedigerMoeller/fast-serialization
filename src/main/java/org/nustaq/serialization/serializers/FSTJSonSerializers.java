package org.nustaq.serialization.serializers;

import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by ruedi on 24/05/15.
 */
public class FSTJSonSerializers {

    public static class BigDecSerializer extends FSTBasicObjectSerializer {
        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
            out.writeStringUTF(toWrite.toString());
        }

        @Override
        public boolean alwaysCopy() {
            return true;
        }

        @Override
        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
            return new BigDecimal(in.readStringUTF());
        }
    }
}
