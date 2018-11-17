package org.nustaq.serialization.serializers;


import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;


public class FSTProxySerializer extends FSTBasicObjectSerializer {

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        Class<?>[] ifaces = clzInfo.getClazz().getInterfaces();
        ClassLoader cl = out.getConf().getClassLoader();
        out.writeInt(ifaces.length);
        for (Class i : ifaces)
            out.writeUTF(i.getName());
        out.writeObject(Proxy.getInvocationHandler(toWrite));
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException {
        ClassLoader cl = in.getConf().getClassLoader();
        int numIfaces = in.readInt();
        String[] interfaces = new String[numIfaces];
        for (int i = 0; i < numIfaces; i++) {
            interfaces[i] = in.readUTF();
        }
        Class[] classObjs = new Class[interfaces.length];

        for(int i = 0; i < interfaces.length; ++i) {
            try {
                classObjs[i] = Class.forName(interfaces[i], false, cl);
            } catch (ClassNotFoundException e) {
                classObjs[i] = Class.forName(interfaces[i], false, this.getClass().getClassLoader());
            }
        }
        InvocationHandler ih = (InvocationHandler)in.readObject();
        Object res = Proxy.newProxyInstance(in.getConf().getClassLoader(),classObjs,ih);
        in.registerObject(res,streamPositioin,serializationInfo,referencee);
        return res;
    }
}