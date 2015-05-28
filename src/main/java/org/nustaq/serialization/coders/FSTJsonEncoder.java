package org.nustaq.serialization.coders;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.io.SerializedString;
import org.nustaq.serialization.*;
import org.nustaq.serialization.util.FSTOutputStream;
import org.nustaq.serialization.util.FSTUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;

/**
 * Created by ruedi on 20/05/15.
 *
 */
public class FSTJsonEncoder implements FSTEncoder {

    public static final String TYPE = "typ";
    public static final String OBJ = "obj";
    public static final String SEQ_TYPE = "styp";
    public static final String SEQ = "seq";
    public static final String ENUM = "enum";
    public static final String VAL = "val";
    public static final String REF = "ref";

    public static final SerializedString TYPE_S = new SerializedString(TYPE);
    public static final SerializedString OBJ_S = new SerializedString(OBJ);
    public static final SerializedString SEQ_TYPE_S = new SerializedString(SEQ_TYPE);
    public static final SerializedString SEQ_S = new SerializedString(SEQ);
    public static final SerializedString ENUM_S = new SerializedString(ENUM);
    public static final SerializedString VAL_S = new SerializedString(VAL);
    public static final SerializedString REF_S = new SerializedString(REF);

    JsonFactory fac;
    FSTConfiguration conf;

    protected JsonGenerator gen;
    FSTOutputStream out;

    public FSTJsonEncoder(FSTConfiguration conf) {
        this.conf = conf;
        fac = conf.getCoderSpecific();
    }

    @Override
    public void writeRawBytes(byte[] bufferedName, int off, int length) throws IOException {
        gen.writeBinary(bufferedName,off,length);
    }

    @Override
    public void writePrimitiveArray(Object array, int start, int length) throws IOException {
        gen.writeStartArray();
        Class<?> componentType = array.getClass().getComponentType();
        if ( componentType != int.class ) {
            gen.writeString(componentType.getSimpleName());
        } else { // fast path for int
            int arr[] = (int[]) array;
            for (int i=0; i < length; i++ ) {
                gen.writeNumber(arr[i]);
            }
            gen.writeEndArray();
            return;
        }
        if ( array instanceof boolean[] ) {
            boolean arr[] = (boolean[]) array;
            for (int i=0; i < length; i++ ) {
                gen.writeBoolean(arr[i]);
            }
        } else if ( array instanceof long[] ) {
            long arr[] = (long[]) array;
            for (int i=0; i < length; i++ ) {
                gen.writeNumber(arr[i]);
            }
        } else if ( array instanceof double[] ) {
            double arr[] = (double[]) array;
            for (int i=0; i < length; i++ ) {
                gen.writeNumber(arr[i]);
            }
        } else if ( array instanceof char[] ) {
            char arr[] = (char[]) array;
            for (int i=0; i < length; i++ ) {
                gen.writeNumber(arr[i]);
            }
        } else {
            for (int i=0; i < length; i++ ) {
                Number num = (Number) Array.get(array, start + i);
                if ( num instanceof Float || num instanceof Double ) {
                    gen.writeNumber(num.doubleValue());
                } else
                    gen.writeNumber(num.longValue());
            }
        }
        gen.writeEndArray();
    }

    @Override
    public void writeStringUTF(String str) throws IOException {
        gen.writeString(str);
    }

    @Override
    public void writeFShort(short c) throws IOException {
        gen.writeNumber(c);
    }

    @Override
    public void writeFChar(char c) throws IOException {
        gen.writeNumber(c);
    }

    @Override
    public void writeFByte(int v) throws IOException {
        gen.writeNumber(v);
    }

    @Override
    public void writeFInt(int anInt) throws IOException {
        gen.writeNumber(anInt);
    }

    @Override
    public void writeFLong(long anInt) throws IOException {
        gen.writeNumber(anInt);
    }

    @Override
    public void writeFFloat(float value) throws IOException {
        gen.writeNumber(value);
    }

    @Override
    public void writeFDouble(double value) throws IOException {
        gen.writeNumber(value);
    }

    @Override
    public int getWritten() {
//        try {
//            gen.flush();
//        } catch (IOException e) {
//            FSTUtil.<RuntimeException>rethrow(e);
//        }
//        System.out.println(pos+" "+out.pos);
        return out.pos-out.getOff() + ((FSTConfiguration.JacksonAccessWorkaround)gen).getOutputTail();
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
            createGenerator();
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
    }

    public void createGenerator() throws IOException {
        if ( gen != null )
            gen.close();
        gen = fac.createGenerator(out);
    }

    @Override
    public void ensureFree(int bytes) throws IOException {
        out.ensureFree(bytes);
    }

    @Override
    public byte[] getBuffer() {
        try {
            gen.flush();
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
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
                gen.writeFieldName(REF_S);
                gen.writeNumber(somValue);
                gen.writeEndObject();
                return true;
            case FSTObjectOutput.NULL:
                gen.writeNull();
                return true;
            case FSTObjectOutput.TYPED:
            case FSTObjectOutput.OBJECT:
                FSTClazzInfo clzInfo = (FSTClazzInfo) infoOrObject;
                if (clzInfo.useCompatibleMode() && clzInfo.getSer() == null ) {
                    throw new RuntimeException("Unsupported backward compatibility mode for class '"+clzInfo.getClazz().getName()+"'. Pls register a Custom Serializer to fix");
                }

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
                    gen.writeFieldName(TYPE_S);
                    writeSymbolicClazz(clzInfo, clzInfo.getClazz());
                    gen.writeFieldName(OBJ_S);
                    gen.writeStartArray();
                } else
                {
                    gen.writeStartObject();
                    gen.writeFieldName(TYPE_S);
                    writeSymbolicClazz(clzInfo,clzInfo.getClazz());
                    gen.writeFieldName(OBJ_S);
                    gen.writeStartObject();
                }
                break;
            case FSTObjectOutput.ONE_OF:
                throw new RuntimeException("not implemented");
            case FSTObjectOutput.STRING:
                break; // ignore, header created by calling writeUTF
            case FSTObjectOutput.BIG_BOOLEAN_FALSE:
                gen.writeBoolean(Boolean.FALSE);
                break; // ignore, header created by writing long. FIXME: won't work
            case FSTObjectOutput.BIG_BOOLEAN_TRUE:
                gen.writeBoolean(Boolean.TRUE);
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
                    gen.writeFieldName(SEQ_TYPE_S);
                    writeSymbolicClazz(null,clz);
                    gen.writeFieldName(SEQ_S);
                    gen.writeStartArray();
                }
                break;
            case FSTObjectOutput.ENUM:
                boolean isEnumClass = toWrite.getClass().isEnum();
                Class c = toWrite.getClass();
                if (!isEnumClass) {
                    // weird stuff ..
                    while (c != null && !c.isEnum()) {
                        c = toWrite.getClass().getEnclosingClass();
                    }
                    if (c == null) {
                        throw new RuntimeException("Can't handle this enum: " + toWrite.getClass());
                    }
                }
                gen.writeStartObject();
                gen.writeFieldName(ENUM_S);
                writeSymbolicClazz(null,c);
                gen.writeFieldName(VAL_S);
                gen.writeString(toWrite.toString());
                gen.writeEndObject();
                return true;
            default:
                throw new RuntimeException("unexpected tag "+tag);
        }
        return false;
    }

    private void writeSymbolicClazz(FSTClazzInfo clzInfo, Class<?> clz) {
        try {
            if ( clzInfo != null ) {
                SerializedString buffered = (SerializedString) clzInfo.getDecoderAttached();
                if ( buffered == null ) {
                    buffered = new SerializedString(classToString(clz));
                    clzInfo.setDecoderAttached(buffered);
                }
                gen.writeString(buffered);
            } else {
                gen.writeString(classToString(clz));
            }
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
    }

    protected String classToString(Class clz) {
        return conf.getCPNameForClass(clz);
    }

    @Override
    public void writeAttributeName(FSTClazzInfo.FSTFieldInfo subInfo) {
        try {
            if ( gen.getOutputContext().inArray() )
                gen.writeString(subInfo.getName());
            else {
                SerializedString bufferedName = (SerializedString) subInfo.getBufferedName();
                if ( bufferedName == null ) {
                    bufferedName = new SerializedString(subInfo.getName());
                    subInfo.setBufferedName(bufferedName);
                }
                gen.writeFieldName(bufferedName);
            }
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
    }

    @Override
    public void externalEnd(FSTClazzInfo clz) {
        try {
            Class clazz = clz.getClazz();
            if ( clazz == Byte.class ||
                 clazz == Short.class ||
                 clazz == Integer.class ||
                 clazz == Long.class ||
                 clazz == Float.class ||
                 clazz == Double.class ||
                 clazz == Character.class ||
                 clazz == Boolean.class )
                return;
            if ( gen.getOutputContext().inArray() )
                gen.writeEndArray();
            if ( gen.getOutputContext().inObject() )
                gen.writeEndObject();
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
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
    }

    @Override
    public boolean isByteArrayBased() {
        return true;
    }

    @Override
    public void writeArrayEnd() {
        try {
            if ( gen.getOutputContext().inArray() )
                gen.writeEndArray();
            if ( gen.getOutputContext().inObject() )
                gen.writeEndObject();
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
    }

    @Override
    public void writeFieldsEnd(FSTClazzInfo serializationInfo) {
        try {
            JsonStreamContext outputContext = gen.getOutputContext();
            if ( outputContext.inObject() ) {
                gen.writeEndObject();
            } else {
                gen.writeEndArray();
            }
            if ( outputContext.inObject() )
                gen.writeEndObject();
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
            try {
                gen.flush();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            System.out.println( new String(out.buf,0,out.pos) );
        }
    }

}
