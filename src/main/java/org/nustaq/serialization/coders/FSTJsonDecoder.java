package org.nustaq.serialization.coders;

import com.fasterxml.jackson.core.*;
import org.nustaq.serialization.*;
import org.nustaq.serialization.util.FSTInputStream;
import org.nustaq.serialization.util.FSTUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by moelrue on 5/21/15.
 */
public class FSTJsonDecoder implements FSTDecoder {

    static class TokenBufferedInput {
        JsonParser input;

        public TokenBufferedInput(JsonParser input) {
            this.input = input;
        }

        public JsonToken nextToken() throws IOException {
            return input.nextToken();
        }

        public String getCurrentName() throws IOException {
            return input.getCurrentName();
        }

        public String getText() throws IOException {
            return input.getText();
        }

        public String nextTextValue() throws IOException {
            return input.nextTextValue();
        }

        public int getIntValue() throws IOException {
            return input.getIntValue();
        }

        public long getLongValue() throws IOException {
            return input.getLongValue();
        }

        public float getFloatValue() throws IOException {
            return input.getFloatValue();
        }

        public double getDoubleValue() throws IOException {
            return input.getDoubleValue();
        }

        public boolean getBooleanValue() throws IOException {
            return input.getBooleanValue();
        }

        public byte getByteValue() throws IOException {
            return input.getByteValue();
        }

        public short getShortValue() throws IOException {
            return input.getShortValue();
        }

        public int nextIntValue(int i) throws IOException {
            return input.nextIntValue(i);
        }

        public JsonToken getCurrentToken() {
            return input.getCurrentToken();
        }

        public JsonLocation getCurrentLocation() {
            return input.getCurrentLocation();
        }

        public int getCurrentTokenId() {
            return input.getCurrentTokenId();
        }

        public String nextFieldName() throws IOException {
            return input.nextFieldName();
        }

        public void close() throws IOException {
            input.close();
        }

        public Number getNumberValue() throws IOException {
            return input.getNumberValue();
        }

        public String getValueAsString() throws IOException {
            return input.getValueAsString();
        }

        public JsonToken currentToken() {
            return input.currentToken();
        }

        public JsonToken nextValue() throws IOException {
            return input.nextValue();
        }

        public Object getCurrentValue() {
            return input.getCurrentValue();
        }

        public JsonStreamContext getParsingContext() {
            return input.getParsingContext();
        }
    }

    protected FSTConfiguration conf;
    protected FSTJsonFieldNames fieldNames;

    protected TokenBufferedInput input;
    protected JsonFactory fac;

    protected FSTInputStream fstInput;
    protected String unknownFallbackReadFieldName; // contains read fieldName in case of Unknown resulting from plain JSon structure
    protected HashMap<String,Class> clzCache = new HashMap<>(31);
    protected String lastUnknown;
    protected int unknownNestLevel;

    public FSTJsonDecoder(FSTConfiguration conf) {
        fac = conf.getCoderSpecific();
        setConf(conf);
    }

    @Override
    public void setConf(FSTConfiguration conf) {
        this.conf = conf;
        fieldNames = conf.getJsonFieldNames();
    }

    @Override
    public String readStringUTF() throws IOException {
        if ( unknownFallbackReadFieldName != null ) {
            String unkReadAhead = unknownFallbackReadFieldName;
            unknownFallbackReadFieldName = null;
            return unkReadAhead;
        }
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
            if (componentType == int.class) {
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
            } else if (componentType == double.class) {
                double[] da = (double[]) array;
                for (int i = 0; i < da.length; i++) {
                    input.nextToken();
                    da[i] = input.getDoubleValue();
                }
                return da;
            } else if (componentType == float.class) {
                float[] da = (float[]) array;
                for (int i = 0; i < da.length; i++) {
                    input.nextToken();
                    da[i] = input.getFloatValue();
                }
                return da;
            } else if (componentType == boolean.class) {
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
        int currentTokenId = input.getCurrentTokenId();
        if ( currentTokenId == JsonTokenId.ID_FALSE ) {
            return 0;
        }
        if ( currentTokenId == JsonTokenId.ID_TRUE ) {
            return 1;
        }
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
                createParser();
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
        unknownNestLevel = 0;
        firstCall = true;
    }

    @Override
    public void resetToCopyOf(byte[] bytes, int off, int len) {
        if (off != 0 )
            throw new RuntimeException("not supported");
        byte b[] = new byte[len];
        System.arraycopy(bytes,off,b,0,len);
        if ( fstInput == null )
            fstInput = new FSTInputStream(b);
        else
            fstInput.resetForReuse(bytes,len);
        try {
            createParser();
            unknownNestLevel = 0;
            firstCall = true;
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
    }

    public void createParser() throws IOException {
        if ( input != null )
            input.close();
        input = new TokenBufferedInput(fac.createParser(fstInput));
    }

    @Override
    public void resetWith(byte[] bytes, int len) {
        if ( fstInput == null ) {
            fstInput = new FSTInputStream(bytes,0,len);
        }
        else {
            fstInput.resetForReuse(bytes,len);
        }
        try {
            createParser();
            unknownNestLevel = 0; // fixme: should delegate to core reset (not now as I don't want to break things)
            firstCall = true;
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
    boolean firstCall = true;
    public byte readObjectHeaderTag() throws IOException {
        boolean isFirst = firstCall;
        firstCall = false;
        lastObjectLen = -1;
        lastReadDirectObject = null;
        lastDirectClass = null;
        JsonToken jsonToken = input.nextToken();
        if ( jsonToken == JsonToken.END_OBJECT )
            jsonToken = input.nextToken();
        switch (jsonToken) {
            case VALUE_STRING: {
                lastReadDirectObject = input.getValueAsString();
                return FSTObjectOutput.DIRECT_OBJECT;
            }
            case VALUE_TRUE: {
                lastReadDirectObject = Boolean.TRUE;
                return FSTObjectOutput.DIRECT_OBJECT;
            }
            case VALUE_FALSE: {
                lastReadDirectObject = Boolean.FALSE;
                return FSTObjectOutput.DIRECT_OBJECT;
            }
            case VALUE_NUMBER_INT: {
                lastReadDirectObject = input.getNumberValue();
                return FSTObjectOutput.DIRECT_OBJECT;
            }
            case VALUE_NUMBER_FLOAT: {
                lastReadDirectObject = input.getDoubleValue();
                return FSTObjectOutput.DIRECT_OBJECT;
            }
            case START_ARRAY: {
                if (unknownNestLevel > 0 || isFirst) {
                    lastReadDirectObject = createUnknownArray();
                    return FSTObjectOutput.DIRECT_ARRAY_OBJECT;
                } else {
                    lastReadDirectObject = createPrimitiveArrayFrom(readJSonArr2List(getTmpList()));
                    return FSTObjectOutput.DIRECT_ARRAY_OBJECT;
                }
            }
            case VALUE_NULL: {
                lastReadDirectObject = null;
                return FSTObjectOutput.NULL;
            }
        }
        if ( jsonToken != JsonToken.START_OBJECT ) {
            if ( jsonToken == JsonToken.END_ARRAY ) { // tweak to get end marker when reading json array with readObject
                lastReadDirectObject = "]";
                return FSTObjectOutput.DIRECT_OBJECT;
            }
            throw new RuntimeException("Expected Object start, got '"+jsonToken+"' "+input.currentToken()+" '"+input.getValueAsString()+"'");
        }

        String typeTag = input.nextFieldName();
        if ( typeTag.equals(fieldNames.TYPE) ) {
            // object
            String type = input.nextTextValue();
            String valueTag = input.nextFieldName();
            if ( ! fieldNames.OBJ.equals(valueTag) ) {
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
        } else if ( typeTag.equals(fieldNames.SEQ_TYPE) ) {
            // sequence
            String type = input.nextTextValue();
            try {
                lastDirectClass = classForName(conf.getClassForCPName(type));
                String valueTag = input.nextFieldName();
                if ( ! fieldNames.SEQ.equals(valueTag) ) {
                    throw new RuntimeException("expected value attribute for object of type:"+type);
                }
                if ( ! input.nextToken().isStructStart() ) {
                    throw new RuntimeException("expected array start");
                }
            } catch (ClassNotFoundException e) {
                FSTUtil.<RuntimeException>rethrow(e);
            }
            return FSTObjectOutput.ARRAY;
        } else if ( typeTag.equals(fieldNames.REF) ) {
            return FSTObjectOutput.HANDLE;
        } else if ( typeTag.equals(fieldNames.ENUM) ) {
            try {
                String clName = input.nextTextValue();
                Class aClass = classForName(conf.getClassForCPName(clName));
                String valueTag = input.nextFieldName();
                if ( ! fieldNames.VAL.equals(valueTag) ) {
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
        // fall back to unknown
        lastDirectClass = Unknown.class;
        unknownFallbackReadFieldName = typeTag;
        return FSTObjectOutput.OBJECT;
    }

    List tmpList;
    private List getTmpList() {
        if ( tmpList == null ) {
            tmpList = new ArrayList(32);
        } else {
            tmpList.clear();
        }
        return tmpList;
    }

    protected Unknown createUnknownArray() throws IOException {
        unknownNestLevel++;
        List arrayTokens = new ArrayList(14);
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
            } else if ( elem == JsonToken.VALUE_STRING ){
                arrayTokens.add(input.getText());
            } else if ( elem == JsonToken.START_OBJECT ) {
                arrayTokens.add( readUnknownObject() );
            } else if ( elem == JsonToken.START_ARRAY ) {
                arrayTokens.add( createUnknownArray() );
            } else {
                throw new RuntimeException("unexpected array content in Unknown array");
            }
            elem = input.nextValue();
        }
        unknownNestLevel--;
        return new Unknown().items(arrayTokens);
    }

    private Unknown readUnknownObject() throws IOException {
        Unknown unk = new Unknown();
        JsonToken elem = input.nextToken();
        boolean expectField = true;
        String field = null;
        while ( ! elem.isStructEnd() ) {
            if ( expectField ) {
                field = input.getValueAsString();
                expectField = false;
            } else {
                if ( elem == JsonToken.VALUE_NUMBER_INT ) {
                    unk.set(field,input.getLongValue());
                } else if ( elem == JsonToken.VALUE_NUMBER_FLOAT ) {
                    unk.set(field,input.getDoubleValue());
                } else if ( elem == JsonToken.VALUE_TRUE ) {
                    unk.set(field,true);
                } else if ( elem == JsonToken.VALUE_FALSE ) {
                    unk.set(field,false);
                } else if ( elem == JsonToken.VALUE_NULL ) {
                    unk.set(field,null);
                } else if ( elem == JsonToken.VALUE_STRING ){
                    unk.set(field,input.getText());
                } else if ( elem == JsonToken.START_OBJECT ) {
                    unk.set(field,readUnknownObject());
                } else if ( elem == JsonToken.START_ARRAY ) {
                    unk.set(field, createUnknownArray());
                } else {
                    throw new RuntimeException("unexpected array content in Unknown array");
                }
                expectField = true;
            }
            elem = input.nextToken();
        }
        return unk;
    }

    private Object createPrimitiveArrayFrom( List directObject ) {
        if ( directObject.size() == 0 || directObject.get(0) instanceof String == false ) {
            directObject.add(0,"int"); //fixme:slow
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
            default:
                directObject.add(0,"dummy");
                arrT = String.class;
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
            else if (arrT == String.class ) {
                Array.set(newObj,i,n);
            } else {
                System.err.println("unexpected primitive array type:"+arrT);
            }
        }
        return newObj;
    }

    public List readJSonArr2List(List arrayTokens) throws IOException {
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
            FSTClazzInfo clInfo = conf.getCLInfoRegistry().getCLInfo(lastDirectClass, conf);
            lastDirectClass = null;
            return clInfo;
        }
        return null;
    }

    @Override
    public Class classForName(String name) throws ClassNotFoundException {
        Class aClass = clzCache.get(name);
        if ( aClass == Unknown.class )
            lastUnknown = name;
        if (aClass!=null)
            return aClass;
        aClass = conf.getClassRegistry().classForName(name,conf);
        if ( aClass == Unknown.class )
            lastUnknown = name;
        clzCache.put(name,aClass);
        return aClass;
    }

    @Override
    public void registerClass(Class possible) {
//        throw new RuntimeException("not implemented");
    }

    @Override
    public void close() {
        //nothing to do (?)
//        throw new RuntimeException("not implemented");
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
                if (unknownNestLevel>0) {
                    return createUnknownArray();
                } else {
                    // direct primitive array [1,2, ..]
                    return createPrimitiveArrayFrom(readJSonArr2List(getTmpList()));
                }
            } else if ( jsonToken == JsonToken.VALUE_NULL ) {
                return null;
            } else {
                jsonToken = input.nextToken(); // seqType
                if ( fieldNames.TYPE.equals(input.getText())) {
                    // object
                    type = input.nextTextValue();
                    String valueTag = input.nextFieldName();
                    if ( !fieldNames.OBJ.equals(valueTag) ) {
                        throw new RuntimeException("expected value attribute for object of type:"+type);
                    }
                    return classForName(conf.getClassForCPName(type));
                }
                if (!fieldNames.SEQ_TYPE.equals(input.getText()) ) {
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
            return classForName(conf.getClassForCPName(type));
        }
        Class ldc = this.lastDirectClass;
        if ( ldc == Unknown.class ) {
            ldc = Object[].class;
        }
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
            if ( jsonToken == null )
                return;
            if ( ! jsonToken.isStructEnd() ) {
                throw new RuntimeException("end of structure expected found:"+jsonToken+" : value:"+input.getValueAsString()+" fname:'"+input.getCurrentName()+"'");
            }
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
    }

    @Override
    public void readArrayEnd(FSTClazzInfo clzSerInfo) {
        try {
            JsonToken jsonToken = input.nextToken(); // ]
            if ( jsonToken == JsonToken.END_ARRAY ) { //&& (clzSerInfo == null || clzSerInfo.getSer() == null) ) { // need to read only 1 in case of custom ser
                jsonToken = input.nextToken();    // }
            } else {
                //System.out.println("debug "+clzSerInfo);
            }
            Object dummyDebug = jsonToken;
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
    }

    @Override
    public void readObjectEnd() {
    }

    @Override
    public void readArrayObjectEnd() {
        JsonParser jackIn = this.input.input;
        JsonToken currentToken = jackIn.currentToken();
        boolean inArr = jackIn.getParsingContext().inArray();
        boolean parInArr = jackIn.getParsingContext().getParent().inArray();
        System.out.println("POK "+currentToken+" "+inArr+" par "+parInArr);
        if ( parInArr )
            consumeEnd();
    }

    @Override
    public Object coerceElement(Class arrType, Object value) {
        if ( arrType == byte[].class && value instanceof String ) {
            try {
                return ((String) value).getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
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

    @Override
    public int available() {
        fstInput.ensureReadAhead(1);
        return fstInput.available();
    }

    @Override
    public boolean inArray() {
        return input.getParsingContext().inArray();
    }

    @Override
    public void startFieldReading(Object newObj) {
        if ( newObj instanceof Unknown ) {
            ((Unknown) newObj).setType(lastUnknown);
            lastUnknown = null;
            unknownNestLevel++;
        }
    }

    @Override
    public void endFieldReading(Object newObj) {
        if ( newObj instanceof Unknown ) {
            unknownNestLevel--;
        }
    }

}
