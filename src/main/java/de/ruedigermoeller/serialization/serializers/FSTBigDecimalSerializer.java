package de.ruedigermoeller.serialization.serializers;

import de.ruedigermoeller.serialization.*;
import de.ruedigermoeller.serialization.util.FSTInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by ruedi on 29/01/15.
 */
public class FSTBigDecimalSerializer extends FSTBasicObjectSerializer {
    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        BigDecimal bd = (BigDecimal) toWrite;
        out.writeCInt(bd.scale());
        out.writeObject(bd.toBigInteger().toByteArray());
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        int scale = in.readCInt(); // read"C"Int means compressed int
        BigDecimal bigDecimal = new BigDecimal( new BigInteger((byte[]) in.readObject()), scale);
        // need to register for ref restauration
        in.registerObject(bigDecimal,streamPositioin,serializationInfo,referencee);
        return bigDecimal;
    }


    public static void main( String arg[] ) {
        // init static conf
        /* public static */FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.registerSerializer(BigDecimal.class, new FSTBigDecimalSerializer(),false);

        // later
        InputStream instream = null;
        FSTObjectInput in = new FSTObjectInput(instream,conf);
        // or
        // conf.getObjectInput(..)
        //...

        // similar with output
    }
}
