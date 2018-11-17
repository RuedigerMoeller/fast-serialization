package org.nustaq.serialization.serializers;

import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.IOException;
import java.util.BitSet;

public class FSTBitSetSerializer extends FSTBasicObjectSerializer {
    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo,
                            FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        out.writeObject(((BitSet)toWrite).toLongArray());
    }

    @Override
    public boolean alwaysCopy(){
        return true;
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo,
                              FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
        long[] l = (long[])in.readObject();
        Object res = BitSet.valueOf(l);
        return res;
    }

}