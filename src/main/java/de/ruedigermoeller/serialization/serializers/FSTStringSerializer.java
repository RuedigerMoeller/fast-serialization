package de.ruedigermoeller.serialization.serializers;

import de.ruedigermoeller.serialization.*;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 07.12.12
 * Time: 01:26
 * To change this template use File | Settings | File Templates.
 */
public class FSTStringSerializer extends FSTBasicObjectSerializer {

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        if ( referencedBy.isCompressed() ) {
            out.writeStringCompressed((String) toWrite);
        } else {
            out.writeStringUTF((String) toWrite);
        }
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        if ( referencee.isCompressed() ) {
            String s = in.readStringCompressed();
            in.registerObject(s, streamPositioin, serializationInfo, referencee);
            return s;
        } else {
            String s = in.readStringUTF();
            in.registerObject(s, streamPositioin, serializationInfo, referencee);
            return s;
        }
    }

}
