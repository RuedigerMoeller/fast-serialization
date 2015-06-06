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
package org.nustaq.serialization.serializers;

import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 07.12.12
 * Time: 20:39
 * To change this template use File | Settings | File Templates.
 */

/**
 * Long and Integer are built in for speed.
 */
public class FSTBigNumberSerializers {

    public static class FSTByteSerializer extends FSTBasicObjectSerializer {
        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
            out.writeByte(((Byte)toWrite).byteValue());
        }

        @Override
        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
            Object res = Byte.valueOf(in.readByte());
            return res;
        }

        /**
         * @return true if FST can skip a search for same instances in the serialized ObjectGraph. This speeds up reading and writing and makes
         *         sense for short immutable such as Integer, Short, Character, Date, .. . For those classes it is more expensive (CPU, size) to do a lookup than to just
         *         write the Object twice in case.
         */
        @Override
        public boolean alwaysCopy() {
            return true;
        }

        @Override
        public boolean writeTupleEnd() {
            return false;
        }

    }

    static public class FSTCharSerializer extends FSTBasicObjectSerializer {
        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
            out.writeChar(((Character)toWrite).charValue());
        }

        @Override
        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
            Object res = Character.valueOf(in.readChar());
            return res;
        }

        @Override
        public boolean alwaysCopy() {
            return true;
        }

        @Override
        public boolean writeTupleEnd() {
            return false;
        }

    }

    static public class FSTShortSerializer extends FSTBasicObjectSerializer {

        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
            out.writeShort(((Short)toWrite).shortValue());
        }

        @Override
        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
            Object res = Short.valueOf(in.readShort());
            return res;
        }

        @Override
        public boolean alwaysCopy() {
            return true;
        }

        @Override
        public boolean writeTupleEnd() {
            return false;
        }

    }

    static public class FSTFloatSerializer extends FSTBasicObjectSerializer {

        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
            out.writeFloat(((Float) toWrite).floatValue());
        }

        @Override
        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
            Object res = new Float(in.readFloat());
            return res;
        }

        @Override
        public boolean alwaysCopy() {
            return true;
        }
        @Override
        public boolean writeTupleEnd() {
            return false;
        }

    }

    static public class FSTDoubleSerializer extends FSTBasicObjectSerializer {

        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
            out.writeDouble(((Double) toWrite).doubleValue());
        }

        @Override
        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
            Object res = new Double(in.readDouble());
            return res;
        }

        @Override
        public boolean alwaysCopy() {
            return true;
        }

        @Override
        public boolean writeTupleEnd() {
            return false;
        }

    }

}
