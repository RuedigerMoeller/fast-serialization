package de.ruedigermoeller.serialization;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ruedi on 27.03.14.
 */
public interface FSTDecoder {

    String readStringUTF() throws IOException;
    String readStringAsc() throws IOException;
    Object readFPrimitiveArray(Object array, Class componentType, int len);
    void readFIntArr(int len, int[] arr) throws IOException;
    int readFInt() throws IOException;
    double readFDouble() throws IOException;
    float readFFloat() throws IOException;
    byte readFByte() throws IOException;
    long readFLong() throws IOException;
    char readFChar() throws IOException;
    short readFShort() throws IOException;
    int readPlainInt() throws IOException;

    byte[] getBuffer();
    int getInputPos();
    void moveTo(int position);
    void setInputStream(InputStream in);
    void ensureReadAhead(int bytes);

    void reset();
    void resetToCopyOf(byte[] bytes, int off, int len);
    void resetWith(byte[] bytes, int len);

    FSTClazzInfo readClass() throws IOException, ClassNotFoundException;

    Class classForName(String name) throws ClassNotFoundException;

    void registerClass(Class possible);
    void close();

    void skip(int n);
    void readPlainBytes(byte[] b, int off, int len);

    // read fields as LeanMap. Header has already been read
    LeanMap readMap(FSTClazzInfo.FSTFieldInfo referencee, FSTClazzInfo serializationInfo);

    byte readObjectHeaderTag() throws IOException;
}
