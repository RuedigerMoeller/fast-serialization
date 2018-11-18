package org.nustaq.serialization.serializers;

import java.io.IOException;

import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTClazzInfo.FSTFieldInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

public class FSTTimestampSerializer extends FSTBasicObjectSerializer {

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo,
                            FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        out.writeLong(((java.sql.Timestamp)toWrite).getTime());
    }

    @Override
    public boolean alwaysCopy(){
        return true;
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo,
                              FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
        long l = in.readLong();
        Object res = new java.sql.Timestamp(l);
        return res;
    }
}
