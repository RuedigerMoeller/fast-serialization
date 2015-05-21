package org.nustaq.serialization.coders;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import org.nustaq.serialization.*;
import org.nustaq.serialization.util.FSTOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ruedi on 20/05/15.
 *
 */
public class FSTJSonEncoder implements FSTEncoder {

    static JsonFactory fac = new JsonFactory();
    FSTConfiguration conf;

    protected JsonGenerator gen;
    FSTOutputStream out;

    public FSTJSonEncoder(FSTConfiguration conf) {
        this.conf = conf;
    }

    @Override
    public void writeRawBytes(byte[] bufferedName, int off, int length) throws IOException {
        gen.writeBinary(bufferedName,off,length);
        gen.flush();
    }

    @Override
    public void writePrimitiveArray(Object array, int start, int length) throws IOException {
        gen.writeStartArray();
        for (int i=0; i < length; i++ ) {
            Number num = (Number) Array.get(array, start + i);
            if ( num instanceof Float || num instanceof Double ) {
                gen.writeNumber(num.doubleValue());
            } else
                gen.writeNumber(num.longValue());
        }
        gen.writeEndArray();
        gen.flush();
    }

    @Override
    public void writeStringUTF(String str) throws IOException {
        gen.writeString(str);
        gen.flush();
    }

    @Override
    public void writeFShort(short c) throws IOException {
        gen.writeNumber(c);
        gen.flush();
    }

    @Override
    public void writeFChar(char c) throws IOException {
        gen.writeNumber(c);
        gen.flush();
    }

    @Override
    public void writeFByte(int v) throws IOException {
        gen.writeNumber(v);
        gen.flush();
    }

    @Override
    public void writeFInt(int anInt) throws IOException {
        gen.writeNumber(anInt);
        gen.flush();
    }

    @Override
    public void writeFLong(long anInt) throws IOException {
        gen.writeNumber(anInt);
        gen.flush();
    }

    @Override
    public void writeFFloat(float value) throws IOException {
        gen.writeNumber(value);
        gen.flush();
    }

    @Override
    public void writeFDouble(double value) throws IOException {
        gen.writeNumber(value);
        gen.flush();
    }

    @Override
    public int getWritten() {
        return out.pos-out.getOff();
    }

    @Override
    public void skip(int i) {
        throw new RuntimeException("not supported");
    }

    @Override
    public void close() throws IOException {
        gen.close();
        out.close();
    }

    @Override
    public void reset(byte[] outbytes) {
        if (outbytes==null) {
            out.reset();
        } else {
            out.reset(outbytes);
        }
//        try {
//            gen = fac.createGenerator(out);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void flush() throws IOException {
        gen.flush();
        out.flush();
    }

    @Override
    public void writeInt32At(int position, int v) {
        throw new RuntimeException("not supported");
    }

    @Override
    public void setOutstream(OutputStream outstream) {
        out = new FSTOutputStream(outstream);
        try {
            gen = fac.createGenerator(out).setPrettyPrinter(new DefaultPrettyPrinter());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ensureFree(int bytes) throws IOException {
        out.ensureFree(bytes);
    }

    @Override
    public byte[] getBuffer() {
        return out.getBuf();
    }

    @Override
    public void registerClass(Class possible) {

    }

    @Override
    public void writeClass(Class cl) {
       // already written in write tag
    }

    @Override
    public void writeClass(FSTClazzInfo clInf) {
       // already written in write tag
    }

    @Override
    public boolean writeTag(byte tag, Object infoOrObject, long somValue, Object toWrite) throws IOException {
        switch (tag) {
            case FSTObjectOutput.HANDLE:
                gen.writeStartObject();
                gen.writeFieldName("ref");
                gen.writeNumber(somValue);
                gen.writeEndObject();
                return true;
            case FSTObjectOutput.NULL:
                gen.writeNull();
                return true;
            case FSTObjectOutput.TYPED:
            case FSTObjectOutput.OBJECT:
                FSTClazzInfo clzInfo = (FSTClazzInfo) infoOrObject;
                if (clzInfo.getClazz() == String.class )
                    break;
                if (clzInfo.getClazz() == Double.class )
                    break;
                if (clzInfo.getClazz() == Float.class )
                    break;
                if (clzInfo.getClazz() == Byte.class )
                    break;
                if (clzInfo.getClazz() == Short.class )
                    break;
                if (clzInfo.getClazz() == Integer.class )
                    break;
                if (clzInfo.getClazz() == Long.class )
                    break;
                if (clzInfo.getClazz() == Character.class )
                    break;
                if (clzInfo.getClazz() == Boolean.class )
                    break;
                if ( clzInfo.getSer()!=null || clzInfo.isExternalizable() ) {
                    gen.writeStartObject();
                    gen.writeFieldName("seqType");
                    writeSymbolicClazz(clzInfo.getClazz());
                    gen.writeFieldName("seq");
                    gen.writeStartArray();
                } else
                {
                    gen.writeStartObject();
                    gen.writeFieldName("type");
                    writeSymbolicClazz(clzInfo.getClazz());
                    gen.writeFieldName("value");
                    gen.writeStartObject();
                }
                gen.flush();
                break;
            case FSTObjectOutput.ONE_OF:
                throw new RuntimeException("not implemented");
            case FSTObjectOutput.STRING:
                break; // ignore, header created by calling writeUTF
            case FSTObjectOutput.BIG_BOOLEAN_FALSE:
                gen.writeBoolean(Boolean.FALSE);
                break; // ignore, header created by writing long. FIXME: won't work
            case FSTObjectOutput.BIG_BOOLEAN_TRUE:
                gen.writeBoolean(Boolean.FALSE);
                break; // ignore, header created by writing long. FIXME: won't work
            case FSTObjectOutput.BIG_LONG:
                break; // ignore, header implicitely created by writing long.
            case FSTObjectOutput.BIG_INT:
                break;// ignore, header implicitely created by writing int.
            case FSTObjectOutput.ARRAY:
                Class<?> clz = infoOrObject.getClass();
                Class<?> componentType = clz.getComponentType();
                if ( clz.isArray() && componentType.isPrimitive() )
                {
                    writePrimitiveArray(infoOrObject,0,Array.getLength(infoOrObject));
                    return true;
                } else {
                    gen.writeStartObject();
                    gen.writeFieldName("seqType");
                    writeSymbolicClazz(clz);
                    gen.writeFieldName("seq");
                    gen.writeStartArray();
                    gen.flush();
                }
                break;
            case FSTObjectOutput.ENUM:
//                out.writeTagHeader(MinBin.SEQUENCE);
//                boolean isEnumClass = toWrite.getClass().isEnum();
//                Class c = toWrite.getClass();
//                if (!isEnumClass) {
//                    // weird stuff ..
//                    while (c != null && !c.isEnum()) {
//                        c = toWrite.getClass().getEnclosingClass();
//                    }
//                    if (c == null) {
//                        throw new RuntimeException("Can't handle this enum: " + toWrite.getClass());
//                    }
//                    writeSymbolicClazz(c);
//                } else {
//                    writeSymbolicClazz(c);
//                }
//                out.writeIntPacked(1);
//                out.writeObject(toWrite.toString());
                return true;
            default:
                throw new RuntimeException("unexpected tag "+tag);
        }
        return false;
    }

    private void writeSymbolicClazz(Class<?> clz) {
        try {
            gen.writeString(classToString(clz));
            gen.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String classToString(Class clz) {
        return conf.getCPNameForClass(clz);
    }

    @Override
    public void writeAttributeName(FSTClazzInfo.FSTFieldInfo subInfo) {
        try {
            gen.writeFieldName(subInfo.getName());
            gen.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void externalEnd(FSTClazzInfo clz) {
        try {
            if ( gen.getOutputContext().inObject() ) {
                gen.writeEndObject();
            } else {
                gen.writeEndArray();
            }
            gen.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isWritingAttributes() {
        return true;
    }

    @Override
    public boolean isPrimitiveArray(Object array, Class<?> componentType) {
        return componentType.isPrimitive() && array instanceof double[] == false && array instanceof float[] == false;
    }

    @Override
    public boolean isTagMultiDimSubArrays() {
        return true;
    }

    @Override
    public void writeVersionTag(int version) throws IOException {
        // versioning not supported for minbin
        // use as endobject trigger
        if ( version == 0 ) {
            if ( gen.getOutputContext().inObject() ) {
                gen.writeEndObject();
            } else {
                gen.writeEndArray();
            }
//            gen.writeEndObject();
            gen.flush();
        }
    }

    @Override
    public boolean isByteArrayBased() {
        return true;
    }


    static class JSTST implements Serializable {
        int i = 41;
        int ii[] = { 1,2,3,4 };
//        String st[] = {"A","B"};
        String test = "psodkf";
        Integer bi = 666;
        double d = 44.5555;
//        Double dd = 555.44;
//        int arr[] = { 1,2,3,4 };
        HashMap mp = new HashMap();
        ArrayList l = new ArrayList();
        {
            mp.put("Hello", 13);
            l.add("pok");l.add(new HashMap<>());l.add(new double[]{ 12.3,44.5});
        }
    }

    public static void main( String a[] ) {
        final FSTConfiguration conf = FSTConfiguration.createMinBinConfiguration();
        conf.setStreamCoderFactory(new FSTConfiguration.StreamCoderFactory() {
            @Override
            public FSTEncoder createStreamEncoder() {
                return new FSTJSonEncoder(conf);
            }

            @Override
            public FSTDecoder createStreamDecoder() {
                return new FSTJSonDecoder(conf);
            }
        });

        conf.registerCrossPlatformClassMappingUseSimpleName(new Class[] {JSTST.class} );
//        JSTST object[] = { new JSTST(),new JSTST(),new JSTST() };
        JSTST object = new JSTST();
        byte[] bytes = conf.asByteArray(object);
        System.out.println(new String(bytes,0));

        Object deser = conf.asObject(bytes);
        System.out.println("deser");
    }
}
