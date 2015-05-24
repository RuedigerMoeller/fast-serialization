package org.nustaq.serialization.coders;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.nustaq.serialization.*;
import org.nustaq.serialization.minbin.MBObject;
import org.nustaq.serialization.util.FSTInputStream;
import org.nustaq.serialization.util.FSTUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by moelrue on 5/21/15.
 */
public class FSTJsonDecoder implements FSTDecoder {

    FSTConfiguration conf;

    JsonParser input;
    JsonFactory fac;

    private FSTInputStream fstInput;

    public FSTJsonDecoder(FSTConfiguration conf) {
        this.conf = conf;
        fac = conf.getCoderSpecific();
    }

    @Override
    public String readStringUTF() throws IOException {
        JsonToken jsonToken = input.nextToken();
        if ( jsonToken == JsonToken.VALUE_NULL )
            return null;
        if ( jsonToken == JsonToken.FIELD_NAME )
            return input.getCurrentName();
        return input.getText();
    }

    @Override
    public String readStringAsc() throws IOException {
        return (String) input.nextTextValue();
    }

    @Override
    /**
     * if array is null => create own array. if len == -1 => use len read
     */
    public Object readFPrimitiveArray(Object array, Class componentType, int len) {
        try {
            if (componentType == double.class) {
                double[] da = (double[]) array;
                for (int i = 0; i < da.length; i++) {
                    input.nextToken();
                    da[i] = input.getDoubleValue();
                }
                return da;
            }
            if (componentType == float.class) {
                float[] da = (float[]) array;
                for (int i = 0; i < da.length; i++) {
                    input.nextToken();
                    da[i] = input.getFloatValue();
                }
                return da;
            }
            Object arr = array;
            int length = Array.getLength(arr);
            if (len != -1 && len != length)
                throw new RuntimeException("unexpected arrays size");
            if (componentType == boolean.class) {
                boolean[] da = (boolean[]) array;
                for (int i = 0; i < da.length; i++) {
                    input.nextToken(); da[i] = input.getBooleanValue();
                }
                return da;
            }
            else if (componentType == byte.class) {
                byte[] da = (byte[]) array;
                for (int i = 0; i < da.length; i++) {
                    input.nextToken(); da[i] = input.getByteValue();
                }
                return da;
            }
            else if (componentType == short.class) {
                short[] da = (short[]) array;
                for (int i = 0; i < da.length; i++) {
                    input.nextToken(); da[i] = input.getShortValue();
                }
                return da;
            }
            else if (componentType == char.class) {
                char[] da = (char[]) array;
                for (int i = 0; i < da.length; i++) {
                    input.nextToken(); da[i] = (char) input.getIntValue();
                }
                return da;
            }
            else if (componentType == int.class) {
                int[] da = (int[]) array;
                for (int i = 0; i < da.length; i++) {
                    input.nextToken(); da[i] = (int) input.getIntValue();
                }
                return da;
            }
            else if (componentType == long.class) {
                long[] da = (long[]) array;
                for (int i = 0; i < da.length; i++) {
                    input.nextToken(); da[i] = input.getLongValue();
                }
                return da;
            }
            else throw new RuntimeException("unsupported type " + componentType.getName());
        } catch (Exception e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
        return null;
    }

    @Override
    public void readFIntArr(int len, int[] arr) throws IOException {
        JsonToken jsonToken = input.nextToken();
        if ( ! jsonToken.isStructStart() )
            throw new RuntimeException("Expected array start");
        for (int i = 0; i < len; i++) {
            input.nextToken(); arr[i] = input.getIntValue();
        }
    }

    @Override
    public int readFInt() throws IOException {
        return input.nextIntValue(-1);
    }

    @Override
    public double readFDouble() throws IOException {
        input.nextToken();
        return input.getDoubleValue();
    }

    @Override
    public float readFFloat() throws IOException {
        input.nextToken();
        return input.getFloatValue();
    }

    @Override
    public byte readFByte() throws IOException {
        input.nextToken();
        return input.getByteValue();
    }

    @Override
    public int readIntByte() throws IOException {
        input.nextToken();
        return input.getByteValue();
    }

    @Override
    public long readFLong() throws IOException {
        input.nextToken();
        return input.getLongValue();
    }

    @Override
    public char readFChar() throws IOException {
        input.nextToken();
        return (char) input.getIntValue();
    }

    @Override
    public short readFShort() throws IOException {
        input.nextToken();
        return input.getShortValue();
    }

    @Override
    public int readPlainInt() throws IOException {
        throw new RuntimeException("not supported");
    }

    @Override
    public byte[] getBuffer() {
        return fstInput.buf;
    }

    @Override
    public int getInputPos() {
        JsonLocation currentLocation = input.getCurrentLocation();
        long byteOffset = currentLocation.getByteOffset();
        if ( input.getCurrentToken() == JsonToken.FIELD_NAME )
            byteOffset-=2; // eager parsing of jackson ':' + '['/'{'
        return (int) byteOffset;
    }

    @Override
    public void moveTo(int position) {
        throw new RuntimeException("not supported");
    }

    @Override
    public void setInputStream(InputStream in) {
        try {
            if ( fstInput != null )
                fstInput.initFromStream(in);
            else
                fstInput = new FSTInputStream(in);
            if ( in != FSTObjectInput.emptyStream )
                input = fac.createParser(fstInput);
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
    }

    @Override
    public int ensureReadAhead(int bytes) {
        return 0;
    }

    @Override
    public void reset() {
        fstInput.reset();
        input = null;
    }

    @Override
    public void resetToCopyOf(byte[] bytes, int off, int len) {
        if (off != 0 )
            throw new RuntimeException("not supported");
        byte b[] = new byte[len];
        System.arraycopy(bytes,off,b,0,len);
        fstInput = new FSTInputStream(b);
        try {
            input = fac.createParser(fstInput);
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
    }

    @Override
    public void resetWith(byte[] bytes, int len) {
        fstInput = new FSTInputStream(bytes,0,len);
        try {
            input = fac.createParser(fstInput);
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
    }

    public int getObjectHeaderLen() // len field of last header read (if avaiable)
    {
        return lastObjectLen;
    }

    int lastObjectLen;
    Class lastDirectClass;
    public byte readObjectHeaderTag() throws IOException {
        lastObjectLen = -1;
        lastReadDirectObject = null;
        lastDirectClass = null;
        JsonToken jsonToken = input.nextToken();
        if ( jsonToken == JsonToken.END_OBJECT )
            jsonToken = input.nextToken();
        if ( jsonToken == JsonToken.VALUE_STRING ) {
            lastReadDirectObject = input.getValueAsString();
            return FSTObjectOutput.DIRECT_OBJECT;
        }
        if ( jsonToken == JsonToken.VALUE_TRUE ) {
            lastReadDirectObject = Boolean.TRUE;
            return FSTObjectOutput.DIRECT_OBJECT;
        } else
        if ( jsonToken == JsonToken.VALUE_FALSE ) {
            lastReadDirectObject = Boolean.FALSE;
            return FSTObjectOutput.DIRECT_OBJECT;
        } else
        if ( jsonToken == JsonToken.VALUE_NUMBER_INT ) {
            lastReadDirectObject = input.getNumberValue();
            return FSTObjectOutput.DIRECT_OBJECT;
        } else
        if ( jsonToken == JsonToken.VALUE_NUMBER_FLOAT ) {
            lastReadDirectObject = input.getDoubleValue();
            return FSTObjectOutput.DIRECT_OBJECT;
        } else
        if ( jsonToken == JsonToken.START_ARRAY ) {
            lastReadDirectObject = createPrimitiveArrayFrom(readJSonArr2List());
            return FSTObjectOutput.DIRECT_ARRAY_OBJECT;
        }
        if ( jsonToken == JsonToken.VALUE_NULL ) {
            lastReadDirectObject = null;
            return FSTObjectOutput.NULL;
        }
        if ( jsonToken != JsonToken.START_OBJECT ) {
            throw new RuntimeException("Expected Object start, got '"+jsonToken+"'");
        }

        String typeTag = input.nextFieldName();
        if ( typeTag.equals("type") ) {
            // object
            String type = input.nextTextValue();
            String valueTag = input.nextFieldName();
            if ( ! "obj".equals(valueTag) ) {
                throw new RuntimeException("expected value attribute for object of type:"+type);
            }
            if ( ! input.nextToken().isStructStart() ) {
                throw new RuntimeException("expected struct start");
            }
            try {
                lastDirectClass = classForName(conf.getClassForCPName(type));
            } catch (ClassNotFoundException e) {
                FSTUtil.<RuntimeException>rethrow(e);
            }
            return FSTObjectOutput.OBJECT;
        } else if ( typeTag.equals("seqType") ) {
            // sequence
            String type = input.nextTextValue();
            try {
                lastDirectClass = classForName(conf.getClassForCPName(type));
                String valueTag = input.nextFieldName();
                if ( ! "seq".equals(valueTag) ) {
                    throw new RuntimeException("expected value attribute for object of type:"+type);
                }
                if ( ! input.nextToken().isStructStart() ) {
                    throw new RuntimeException("expected array start");
                }
            } catch (ClassNotFoundException e) {
                FSTUtil.<RuntimeException>rethrow(e);
            }
            return FSTObjectOutput.ARRAY;
        } else if ( typeTag.equals("ref") ) {
            return FSTObjectOutput.HANDLE;
        } else if ( typeTag.equals("enum") ) {
            try {
                String clName = input.nextTextValue();
                Class aClass = classForName(conf.getClassForCPName(clName));
                String valueTag = input.nextFieldName();
                if ( ! "val".equals(valueTag) ) {
                    throw new RuntimeException("expected value attribute for enum of type:"+clName);
                }
                String enumString = input.nextTextValue();
                lastReadDirectObject = Enum.valueOf(aClass,enumString);
                input.nextToken(); // object end
            } catch (ClassNotFoundException e) {
                FSTUtil.<RuntimeException>rethrow(e);
            }
            return FSTObjectOutput.DIRECT_OBJECT;
        }
        throw new RuntimeException("expected object header");
    }

    private Object createPrimitiveArrayFrom( List directObject ) {
        if ( directObject.size() == 0 || directObject.get(0) instanceof String == false ) {
            directObject.add(0,"int");
        }
        Class arrT = null;
        switch ((String)directObject.get(0)) {
            case "boolean": arrT = boolean.class; break;
            case "byte": arrT = byte.class; break;
            case "char": arrT = char.class; break;
            case "short": arrT = short.class; break;
            case "int": arrT = int.class; break;
            case "long": arrT = long.class; break;
            case "float": arrT = float.class; break;
            case "double": arrT = double.class; break;
        }
        Object newObj = Array.newInstance(arrT, directObject.size()-1);
        for (int i = 0; i < directObject.size()-1; i++) {
            Object n = directObject.get(i+1);
            if (arrT == boolean.class )
                Array.setBoolean(newObj, i, (Boolean) n);
            else if (arrT == byte.class )
                Array.setByte(newObj, i, ((Number) n).byteValue());
            else if (arrT == char.class )
                Array.setChar(newObj, i, (char) ((Number)n).intValue());
            else if (arrT == short.class )
                Array.setShort(newObj, i, ((Number)n).shortValue());
            else if (arrT == int.class )
                Array.setInt(newObj, i, ((Number)n).intValue());
            else if (arrT == long.class )
                Array.setLong(newObj, i, ((Number)n).longValue());
            else if (arrT == float.class )
                Array.setFloat(newObj, i, ((Number)n).floatValue());
            else if (arrT == double.class )
                Array.setDouble(newObj, i, ((Number)n).doubleValue());
        }
        return newObj;
    }

    public List readJSonArr2List() throws IOException {
        List arrayTokens = new ArrayList();
        JsonToken elem = input.nextToken();
        while ( ! elem.isStructEnd() ) {
            if ( elem == JsonToken.VALUE_NUMBER_INT ) {
                arrayTokens.add(input.getLongValue());
            } else if ( elem == JsonToken.VALUE_NUMBER_FLOAT ) {
                arrayTokens.add(input.getDoubleValue());
            } else if ( elem == JsonToken.VALUE_TRUE ) {
                arrayTokens.add(true);
            } else if ( elem == JsonToken.VALUE_FALSE ) {
                arrayTokens.add(false);
            } else if ( elem == JsonToken.VALUE_NULL ) {
                arrayTokens.add(null);
            } else {
                arrayTokens.add(input.getText());
            }
            elem = input.nextValue();
        }
        return arrayTokens;
    }

    public Object getDirectObject() // in case class already resolves to read object (e.g. mix input)
    {
        Object tmp = lastReadDirectObject;
        lastReadDirectObject = null;
        return tmp;
    }

    Object lastReadDirectObject; // in case readClass already reads full minbin value
    @Override
    public FSTClazzInfo readClass() throws IOException, ClassNotFoundException {
        if (lastDirectClass != null ) {
            FSTClazzInfo clInfo = conf.getCLInfoRegistry().getCLInfo(lastDirectClass);
            lastDirectClass = null;
            return clInfo;
        }
//        Object read = input.readObject();
//        String name = (String) read;
//        String clzName = conf.getClassForCPName(name);
//        return conf.getCLInfoRegistry().getCLInfo(classForName(clzName));
        return null;
    }

    HashMap<String,Class> clzCache = new HashMap<>();
    @Override
    public Class classForName(String name) throws ClassNotFoundException {
        if ("Object".equals(name))
            return MBObject.class;
        Class aClass = clzCache.get(name);
        if (aClass!=null)
            return aClass;
        aClass = Class.forName(name);
        clzCache.put(name,aClass);
        return aClass;
    }

    @Override
    public void registerClass(Class possible) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void close() {
        //TODO
        throw new RuntimeException("not implemented");
    }

    @Override
    public void skip(int n) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void readPlainBytes(byte[] b, int off, int len) {
        try {
            for (int i = 0; i < len; i++) {
                input.nextToken();
                b[i+off] = input.getByteValue();
            }
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
    }

    @Override
    public boolean isMapBased() {
        return true;
    }

    public void consumeEndMarker() { // empty as flawed in minbin impl
    }

    @Override
    public Object readArrayHeader() throws Exception {
        if ( lastDirectClass == null ) {
            JsonToken jsonToken = input.nextToken();
            String type = null;
            if ( jsonToken == JsonToken.START_ARRAY ) {
                // direct primitive array [1,2, ..]
                return createPrimitiveArrayFrom(readJSonArr2List());
            } else if ( jsonToken == JsonToken.VALUE_NULL ) {
                return null;
            } else {
                jsonToken = input.nextToken(); // seqType
                if ( "type".equals(input.getText())) {
                    // object
                    type = input.nextTextValue();
                    String valueTag = input.nextFieldName();
                    if ( ! "obj".equals(valueTag) ) {
                        throw new RuntimeException("expected value attribute for object of type:"+type);
                    }
                    return classForName(conf.getClassForCPName(type));
                }
                if ( ! "seqType".equals(input.getText()) ) {
                    System.out.println(">" + input.getCurrentToken()+" "+input.getText());
                    input.nextToken();
                    System.out.println(">" + input.getCurrentToken()+" "+input.getText());
                    input.nextToken();
                    System.out.println(">" + input.getCurrentToken()+" "+input.getText());
                    input.nextToken();
                    System.out.println(">" + input.getCurrentToken()+" "+input.getText());
                    throw new RuntimeException("expected seqType");
                }
                jsonToken = input.nextToken(); // seqType : ""
                type = input.getText();
                jsonToken = input.nextToken(); // seq
                jsonToken = input.nextToken(); // seq : [
            }
//                throw new RuntimeException("expected array start of nested array");
//                lastReadDirectObject = readJSonArr2List();
            return classForName(conf.getClassForCPName(type));
        }
        Class ldc = this.lastDirectClass;
        this.lastDirectClass = null; // marker, only valid once
        return ldc;
    }

    @Override
    public void readExternalEnd() {
        consumeEndMarker();
    }

    @Override
    public boolean isEndMarker(String s) {
        return s == null || s.equals("}") || s.equals("]");
    }

    @Override
    public int readVersionTag() throws IOException {
        return 0; // versioning not supported for json
    }

    @Override
    public void pushBack(int bytes) {
        //fstInput.psetPos(input.getPos()-bytes);
        throw new RuntimeException("not supported");
    }

    private void consumeEnd() {
        try {
            JsonToken jsonToken = input.nextToken();
            if ( ! jsonToken.isStructEnd() ) {
                throw new RuntimeException("end of structure expected "+jsonToken);
            }
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
    }

    @Override
    public void readArrayEnd() {
        try {
            JsonToken jsonToken = input.nextToken(); // ]
            if ( jsonToken == JsonToken.END_ARRAY )
                jsonToken = input.nextToken();    // }
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
    }

    @Override
    public void readObjectEnd() {
        consumeEnd();
    }

    @Override
    public Object coerceArrayElement(Class arrType, Object value) {
        if ( value instanceof Number ) {
            Number n = (Number) value;
            if ( arrType == Byte.class ) {
                return new Byte(n.byteValue());
            } else if ( arrType == Short.class ) {
                return new Short(n.shortValue());
            } else if ( arrType == Integer.class ) {
                return new Integer(n.intValue());
            } else if ( arrType == Long.class ) {
                return new Long(n.longValue());
            } else if ( arrType == Double.class ) {
                return new Double(n.doubleValue());
            } else if ( arrType == Float.class ) {
                return new Float(n.floatValue());
            } else if ( arrType == Character.class ) {
                return new Character((char) n.intValue());
            }
        }
        return value;
    }


}
