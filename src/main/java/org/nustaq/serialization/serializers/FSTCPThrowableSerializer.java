package org.nustaq.serialization.serializers;

import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 * Created by ruedi on 02.05.14.
 * enable serialization of throwables for cross platform
 */
public class FSTCPThrowableSerializer extends FSTBasicObjectSerializer {
    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        Throwable th = (Throwable) toWrite;
        out.writeStringUTF(th.getMessage());
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String msg = in.readStringUTF();
        try {
            Constructor constructor = objectClass.getConstructor(String.class);
            return constructor.newInstance(msg);
        } catch (Exception e) {
            return new Throwable("unable to deserialize original exception with message:"+msg);
        }
    }
}
