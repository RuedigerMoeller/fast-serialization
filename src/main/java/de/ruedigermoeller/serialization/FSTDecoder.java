package de.ruedigermoeller.serialization;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ruedi on 27.03.14.
 */
public interface FSTDecoder {

    String readStringUTF() throws IOException;
    String readStringAsc() throws IOException;
    Object readFPrimitiveArray(Class componentType, int len);
    void readFIntArr(int len, int[] arr) throws IOException;
    int readFInt() throws IOException;
    double readFDouble() throws IOException;
    float readFFloat() throws IOException;
    byte readFByte() throws IOException;
    long readFLong() throws IOException;
    char readFChar() throws IOException;
    short readFShort() throws IOException;
    int readPlainInt() throws IOException;

    void push(byte[] buf, int pos, int length);
    void pop();
    byte[] getBuffer();
    int getInputPos();
    void moveTo(int position);
    void setInputStream(InputStream in);
    void ensureReadAhead(int bytes);

    void reset();
    void resetToCopyOf(byte[] bytes, int off, int len);
    void resetWith(byte[] bytes, int len);

}
