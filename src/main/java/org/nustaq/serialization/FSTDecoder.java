/*
 * Copyright 2014 Ruediger Moeller.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nustaq.serialization;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ruedi on 27.03.14.
 */
public interface FSTDecoder {

    void setConf( FSTConfiguration conf );
    String readStringUTF() throws IOException;
    String readStringAsc() throws IOException;
    Object readFPrimitiveArray(Object array, Class componentType, int len);
    void readFIntArr(int len, int[] arr) throws IOException;
    int readFInt() throws IOException;
    double readFDouble() throws IOException;
    float readFFloat() throws IOException;
    byte readFByte() throws IOException;
    int readIntByte() throws IOException;
    long readFLong() throws IOException;
    char readFChar() throws IOException;
    short readFShort() throws IOException;
    int readPlainInt() throws IOException;

    byte[] getBuffer();
    int getInputPos();
    void moveTo(int position);
    void setInputStream(InputStream in);
    int ensureReadAhead(int bytes); // might signal eof by returning -1, depends on decoder impl though

    void reset();
    void resetToCopyOf(byte[] bytes, int off, int len);
    void resetWith(byte[] bytes, int len);

    FSTClazzInfo readClass() throws IOException, ClassNotFoundException;

    Class classForName(String name) throws ClassNotFoundException;

    void registerClass(Class possible);
    void close();

    void skip(int n);
    void readPlainBytes(byte[] b, int off, int len);

    byte readObjectHeaderTag() throws IOException;
    int getObjectHeaderLen(); // len field of last header read (if avaiable)

    boolean isMapBased();

    Object getDirectObject(); // in case class already resolves to read object (e.g. mix input)

    void consumeEndMarker();

    // returns class or directly read array
    Object readArrayHeader() throws IOException, ClassNotFoundException, Exception;

    // read end maker and consume, if no endmarker found do nothing
    void readExternalEnd();

    boolean isEndMarker(String s);

    int readVersionTag() throws IOException;

    void pushBack(int bytes);

    // clashes with read end marker, however avoid breaking minbin, so new methods for JSONDe/Encoding
    // clzSerInfo can be null
    void readArrayEnd(FSTClazzInfo clzSerInfo);

    void readObjectEnd();

    Object coerceElement(Class arrType, Object value);

    /**
     * @return a value > 0 if more bytes are available
     */
    int available();

    /**
     * return wether current stream is reading an object or sequence currently
     * makes sense for MinBin and JSon only, only Json serializer actually has implemented it
     * @return
     */
    boolean inArray();

    /**
     * quirks for json unknown decoding. Need a hook to set original classname
     * only called when reading named fields
     * @param newObj
     */
    void startFieldReading(Object newObj);

    /**
     * only called when reading named fields
     * @param newObj
     */
    void endFieldReading(Object newObj);
}
