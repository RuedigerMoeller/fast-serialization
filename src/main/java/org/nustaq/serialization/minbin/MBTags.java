package org.nustaq.serialization.minbin;

import java.util.Iterator;

/**
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 * <p/>
 * Date: 12.04.2014
 * Time: 22:12
 * To change this template use File | Settings | File Templates.
 */
public class MBTags {

    public static class NullTagSer extends MinBin.TagSerializer {
        @Override
        public void writeTag(Object data, MBOut out) {
        }

        @Override
        public Object readTag(MBIn in) {
            return null;
        }

        @Override
        public Class getClassEncoded() {
            return Object.class;
        }
    }

    public static class StringTagSer extends MinBin.TagSerializer {
        @Override
        public void writeTag(Object data, MBOut out) {
            String s = (String) data;
            boolean isAsc = s.length() < 64;
            if ( isAsc ) {
                for (int i = 0; i < s.length(); i++) {
                    if (s.charAt(i) >= 127) {
                        isAsc = false;
                        break;
                    }
                }
            }
            if (isAsc) {
                byte[] strBytes = s.getBytes();
                out.writeOut((byte) (MinBin.INT_8|MinBin.ARRAY_MASK));
                out.writeIntPacked(strBytes.length);
                out.writeRaw(strBytes, 0, strBytes.length);
            } else {
                final char[] chars = s.toCharArray();
                out.writeArray(chars, 0, chars.length);
            }
        }

        @Override
        public Object readTag(MBIn in) {
            Object arr = in.readArray();
            if (arr instanceof byte[]) {
                byte[] ba = (byte[]) arr;
                return new String(ba, 0, 0, ba.length);
            } else if (arr instanceof char[]) {
                char[] ca = (char[]) arr;
                return new String(ca, 0, ca.length);
            }
            return null;
        }

        @Override
        public Class getClassEncoded() {
            return String.class;
        }
    }

    public static class DoubleTagSer extends MinBin.TagSerializer {
        //fixme: very slow
        @Override
        public void writeTag(Object data, MBOut out) {
            double d = ((Double) data).doubleValue();
            {
                byte[] bytes = Double.toString(d).getBytes();
                out.writeArray(bytes, 0, bytes.length);
            }
        }

        @Override
        public Object readTag(MBIn in) {
            byte ba[] = (byte[]) in.readArray();
            return Double.parseDouble(new String(ba, 0, 0, ba.length));
        }

        @Override
        public Class getClassEncoded() {
            return Double.class;
        }
    }

    public static class FloatTagSer extends MinBin.TagSerializer {
        //fixme: very slow
        @Override
        public void writeTag(Object data, MBOut out) {
            byte[] bytes = Float.toString((Float) data).getBytes();
            out.writeArray(bytes, 0, bytes.length);
        }

        @Override
        public Object readTag(MBIn in) {
            byte ba[] = (byte[]) in.readArray();
            return Float.parseFloat(new String(ba, 0, 0, ba.length));
        }

        @Override
        public Class getClassEncoded() {
            return Float.class;
        }
    }

    public static class DoubleArrTagSer extends MinBin.TagSerializer {
        @Override
        public void writeTag(Object data, MBOut out) {
            double d[] = (double[]) data;
            out.writeIntPacked(((double[]) data).length);
            for (int i = 0; i < d.length; i++) {
                byte[] bytes = Double.toString(d[i]).getBytes();
                out.writeArray(bytes, 0, bytes.length);
            }
        }

        @Override
        public Object readTag(MBIn in) {
            int len = (int) in.readInt();
            double res[] = new double[len];
            for (int i = 0; i < len; i++) {
                byte ba[] = (byte[]) in.readArray();
                res[i] = Double.parseDouble(new String(ba, 0, 0, ba.length));
            }
            return res;
        }

        @Override
        public Class getClassEncoded() {
            return double[].class;
        }
    }

    public static class FloatArrTagSer extends MinBin.TagSerializer {
        @Override
        public void writeTag(Object data, MBOut out) {
            float d[] = (float[]) data;
            out.writeIntPacked(((double[]) data).length);
            for (int i = 0; i < d.length; i++) {
                byte[] bytes = Float.toString(d[i]).getBytes();
                out.writeArray(bytes, 0, bytes.length);
            }
        }

        @Override
        public Object readTag(MBIn in) {
            int len = (int) in.readInt();
            float res[] = new float[len];
            for (int i = 0; i < len; i++) {
                byte ba[] = (byte[]) in.readArray();
                res[i] = Float.parseFloat(new String(ba, 0, 0, ba.length));
            }
            return res;
        }

        @Override
        public Class getClassEncoded() {
            return float[].class;
        }
    }
    
    public static class MBObjectTagSer extends MinBin.TagSerializer {

        /**
         * tag is already written. break down the given object into more tags or primitives
         *
         * @param data
         * @param out
         */
        @Override
        public void writeTag(Object data, MBOut out) {
            MBObject ob = (MBObject) data;
            out.writeObject(ob.getTypeInfo());
            out.writeIntPacked(ob.size());
            for (Iterator iterator = ob.keyIterator(); iterator.hasNext(); ) {
                String next = (String) iterator.next();
                out.writeTag(next);
                out.writeObject(ob.get(next));
            }
        }

        /**
         * tag is already read, reconstruct the object
         *
         * @param in
         * @return
         */
        @Override
        public Object readTag(MBIn in) {
            Object typeInfo = in.readObject();
            int len = (int) in.readInt();
            if ( len == -1 ) { // read to end marker
                len = Integer.MAX_VALUE;
            }
            MBObject obj = new MBObject(typeInfo);
            for ( int i=0; i < len; i++ ) {
                Object key = in.readObject();
                if (MinBin.END_MARKER == key)
                    break;
                obj.put((String) key,in.readObject());
            }
            return obj;
        }

        /**
         * @return the class this tag serializer is responsible for
         */
        @Override
        public Class getClassEncoded() {
            return MBObject.class;
        }
    }

    public static class MBSequenceTagSer extends MinBin.TagSerializer {

        /**
         * tag is already written. break down the given object into more tags or primitives
         *
         * @param data
         * @param out
         */
        @Override
        public void writeTag(Object data, MBOut out) {
            MBSequence ob = (MBSequence) data;
            out.writeTag(ob.getTypeInfo());
            out.writeIntPacked(ob.size());
            for (int i = 0; i < ob.size(); i++) {
                Object o = ob.get(i);
                out.writeObject(o);
            }
        }

        /**
         * tag is already read, reconstruct the object
         *
         * @param in
         * @return
         */
        @Override
        public Object readTag(MBIn in) {
            Object typeInfo = in.readObject();
            int len = (int) in.readInt();
            if ( len == -1 ) { // read to end marker
                len = Integer.MAX_VALUE;
            }
            MBSequence obj = new MBSequence(typeInfo);
            for ( int i=0; i < len; i++ ) {
                Object o = in.readObject();
                if ( MinBin.END_MARKER == o)
                    break;
                obj.add(o);
            }
            return obj;
        }

        /**
         * @return the class this tag serializer is responsible for
         */
        @Override
        public Class getClassEncoded() {
            return MBSequence.class;
        }
    }

    public static class BigBoolTagSer extends MinBin.TagSerializer {
        @Override
        public void writeTag(Object data, MBOut out) {
            out.writeInt(MinBin.INT_8,((Boolean)data)?1:0);
        }

        @Override
        public Object readTag(MBIn in) {
            return in.readInt() == 0 ? Boolean.FALSE:Boolean.TRUE;
        }

        @Override
        public Class getClassEncoded() {
            return Boolean.class;
        }
    }

    public static class RefTagSer extends MinBin.TagSerializer {
        @Override
        public void writeTag(Object data, MBOut out) {
            out.writeInt(MinBin.INT_32, ((Number)data).intValue());
        }

        @Override
        public Object readTag(MBIn in) {
            return (int)in.readInt();
        }

        @Override
        public Class getClassEncoded() {
            return null;
        }
    }

}
