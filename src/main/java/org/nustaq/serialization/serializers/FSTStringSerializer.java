package org.nustaq.serialization.serializers;

import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 07.12.12
 * Time: 01:26
 * To change this template use File | Settings | File Templates.
 */
public class FSTStringSerializer extends FSTBasicObjectSerializer {

    public static FSTStringSerializer Instance = new FSTStringSerializer(); // used directly

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        out.writeStringUTF((String) toWrite);
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String s = in.readStringUTF();
        in.registerObject(s, streamPositioin, serializationInfo, referencee);
        return s;
    }

    @Override
    public boolean writeTupleEnd() {
        return false;
    }
}
