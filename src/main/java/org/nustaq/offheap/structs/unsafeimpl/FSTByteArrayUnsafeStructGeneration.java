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
package org.nustaq.offheap.structs.unsafeimpl;

import org.nustaq.offheap.structs.FSTStruct;
import org.nustaq.serialization.FSTClazzInfo;
import javassist.*;
import javassist.expr.FieldAccess;

/**
 * Date: 22.06.13
 * Time: 20:54
 * To change this template use File | Settings | File Templates.
 */
public class FSTByteArrayUnsafeStructGeneration implements FSTStructGeneration {

    public static boolean trackChanges = false;

    @Override
    public FSTStructGeneration newInstance() {
        return new FSTByteArrayUnsafeStructGeneration();
    }

    @Override
    public void defineStructWriteAccess(FieldAccess f, CtClass type, FSTClazzInfo.FSTFieldInfo fieldInfo) {
        int off = fieldInfo.getStructOffset();
        try {
            boolean vola = fieldInfo.isVolatile();
            validateAnnotations(fieldInfo,vola);
            String insert = "";
            if ( vola ) {
                insert = "Volatile";
            }
            String fieldName = "\""+fieldInfo.getName()+"\"";
            if ( type == CtPrimitiveType.booleanType ) {
                final String body = "___bytes.putBool" + insert + "((long)" + off + "+___offset, $1 );";
                if (trackChanges) {
                    f.replace("{" +
                            body +
                            "if (tracker!=null) tracker.addChange("+off+"+___offset,1,"+ fieldName +");" +
                            "}");
                } else {
                    f.replace(body);
                }
            } else
            if ( type == CtPrimitiveType.byteType ) {
                final String body = "___bytes.put"+insert+"("+off+"+___offset,$1);";
                if (trackChanges) {
                    f.replace("{" +
                            body +
                            "if (tracker!=null) tracker.addChange("+off+"+___offset,1,"+ fieldName +");" +
                            "}");
                } else {
                    f.replace(body);
                }
            } else
            if ( type == CtPrimitiveType.charType ) {
                final String body = "___bytes.putChar"+insert+"("+off+"+___offset,$1);";
                if (trackChanges) {
                    f.replace("{" +
                            body +
                            "if (tracker!=null) tracker.addChange("+off+"+___offset,2,"+ fieldName +");" +
                            "}");
                } else {
                    f.replace(body);
                }
            } else
            if ( type == CtPrimitiveType.shortType ) {
                final String body = "___bytes.putShort"+insert+"("+off+"+___offset,$1);";
                if (trackChanges) {
                    f.replace("{" +
                            body +
                            "if (tracker!=null) tracker.addChange("+off+"+___offset,2,"+ fieldName +");" +
                            "}");
                } else {
                    f.replace(body);
                }
            } else
            if ( type == CtPrimitiveType.intType ) {
                final String body = "___bytes.putInt"+insert+"("+off+"+___offset,$1);";
                if (trackChanges) {
                    f.replace("{" +
                            body +
                            "if (tracker!=null) tracker.addChange("+off+"+___offset,4,"+ fieldName +");" +
                            "}");
                } else {
                    f.replace(body);
                }
            } else
            if ( type == CtPrimitiveType.longType ) {
                final String body = "___bytes.putLong"+insert+"("+off+"+___offset,$1);";
                if (trackChanges) {
                    f.replace("{" +
                            body +
                            "if (tracker!=null) tracker.addChange("+off+"+___offset,8,"+ fieldName +");" +
                            "}");
                } else {
                    f.replace(body);
                }
            } else
            if ( type == CtPrimitiveType.floatType ) {
                final String body = "___bytes.putFloat"+insert+"("+off+"+___offset,$1);";
                if (trackChanges) {
                    f.replace("{" +
                            body +
                            "if (tracker!=null) tracker.addChange("+off+"+___offset,4,"+ fieldName +");" +
                            "}");
                } else {
                    f.replace(body);
                }
            } else
            if ( type == CtPrimitiveType.doubleType ) {
                final String body = "___bytes.putDouble"+insert+"("+off+"+___offset,$1);";
                if (trackChanges) {
                    f.replace("{" +
                            body +
                            "if (tracker!=null) tracker.addChange("+off+"+___offset,8,"+ fieldName +");" +
                            "}");
                } else {
                    f.replace(body);
                }
            } else
            {
                String code =
                "{"+
                    "long tmpOff = ___offset + ___bytes.getInt("+off+" + ___offset);"+
                    "if ( $1 == null ) { " +
                        "___bytes.putInt(tmpOff+4,-1); " +
                        "return; " +
                    "}"+
                    "int obj_len=___bytes.getInt(tmpOff); "+
                    "org.nustaq.offheap.structs.FSTStruct struct = (org.nustaq.offheap.structs.FSTStruct)$1;"+
                    "if ( !struct.isOffHeap() ) {"+
                    "    struct=___fac.toStruct(struct);"+ // FIMXE: do direct toByte to avoid tmp alloc
                    "}"+
                    "if (struct.getByteSize() > obj_len ) throw new RuntimeException(\"object too large to be written\");"+
                    (trackChanges ? "if (tracker!=null) tracker.addChange(tmpOff,struct.getByteSize(),"+ fieldName +"); ":"") +
//                    "unsafe.copyMemory(struct.___bytes,struct.___offset,___bytes,tmpOff,(long)struct.getByteSize());"+
                    "struct.___bytes.copyTo(___bytes,tmpOff,struct.___offset,(long)struct.getByteSize());"+
                    "___bytes.putInt(tmpOff, obj_len);"+ // rewrite original size
                "}";
                f.replace(code);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    void validateAnnotations(FSTClazzInfo.FSTFieldInfo fieldInfo, boolean vola) {
        if ( vola ) {
            if ( ! fieldInfo.isIntegral() )
                throw new RuntimeException("volatile only applicable to primitive types");
        }
    }

    @Override
    public void defineArrayAccessor(FSTClazzInfo.FSTFieldInfo fieldInfo, FSTClazzInfo clInfo, CtMethod method) {
        boolean vola = fieldInfo.isVolatile();
        validateAnnotations(fieldInfo,vola);
        String insert = "";
        if ( vola ) {
            insert = "Volatile";
        }
        try {
            Class arrayType = fieldInfo.getArrayType();
            int off = fieldInfo.getStructOffset();
            String prefix ="{ long _st_off=___offset + ___bytes.getInt("+off+"+___offset);"+ // array base offset in byte arr
                    "int _st_len=___bytes.getInt("+off+"+4+___offset); "+
                    "if ($1>=_st_len||$1<0) throw new ArrayIndexOutOfBoundsException(\"index:\"+$1+\" len:\"+_st_len);";
            if ( method.getReturnType() == CtClass.voidType ) {
                String record = "";
                String fieldName = "\""+fieldInfo.getName()+"\"";
                if ( arrayType == boolean.class ) {
                    if (trackChanges) {
                        record = "if (tracker!=null) tracker.addChange(_st_off+$1,1,"+ fieldName +");";
                    }
                    method.setBody(prefix+"___bytes.putBool"+insert+"( _st_off+$1,$2);"+record+"}");
                } else
                if ( arrayType == byte.class ) {
                    if (trackChanges) {
                        record = "if (tracker!=null) tracker.addChange((long)_st_off+$1,1,"+ fieldName +");";
                    }
                    method.setBody(prefix+"___bytes.put"+insert+"( _st_off+$1,$2);"+record+"}");
                } else
                if ( arrayType == char.class ) {
                    if (trackChanges) {
                        record = "if (tracker!=null) tracker.addChange(_st_off+$1*2,2,"+ fieldName +");";
                    }
                    method.setBody(prefix+"___bytes.putChar"+insert+"( _st_off+$1*2,$2);"+record+"}");
                } else
                if ( arrayType == short.class ) {
                    if (trackChanges) {
                        record = "if (tracker!=null) tracker.addChange(_st_off+$1*2,2,"+ fieldName +");";
                    }
                    method.setBody(prefix+" ___bytes.putShort"+insert+"( _st_off+$1*2,$2);"+record+"}");
                } else
                if ( arrayType == int.class ) {
                    if (trackChanges) {
                        record = "if (tracker!=null) tracker.addChange(_st_off+$1*4,4,"+ fieldName +");";
                    }
                    method.setBody(prefix+" ___bytes.putInt"+insert+"(_st_off+$1*4,$2);"+record+"}");
                } else
                if ( arrayType == long.class ) {
                    if (trackChanges) {
                        record = "if (tracker!=null) tracker.addChange(_st_off+$1*8,8,"+ fieldName +");";
                    }
                    method.setBody(prefix+"___bytes.putLong"+insert+"( _st_off+$1*8,$2);"+record+"}");
                } else
                if ( arrayType == double.class ) {
                    if (trackChanges) {
                        record = "if (tracker!=null) tracker.addChange(_st_off+$1*8,8,"+ fieldName +");";
                    }
                    method.setBody(prefix+"___bytes.putDouble"+insert+"( _st_off+$1*8,$2);"+record+"}");
                } else
                if ( arrayType == float.class ) {
                    if (trackChanges) {
                        record = "if (tracker!=null) tracker.addChange(_st_off+$1*4,4,"+ fieldName +");";
                    }
                    method.setBody(prefix+"___bytes.putFloat"+insert+"(_st_off+$1*4,$2);"+record+"}");
                } else {
                    String structCL = FSTStruct.class.getName();
                    method.setBody(
                    prefix+
                        "int _elem_len=___bytes.getInt("+off+"+8+___offset); "+
                        structCL+" struct = ("+structCL+")$2;"+
                        "if ( struct == null ) { " +
                            "___bytes.putInt((long)_st_off+$1*_elem_len+4,-1); " +
                            "return; " +
                        "}"+
                        "if ( !struct.isOffHeap() ) {"+
                        "    struct=___fac.toStruct(struct);"+ // FIMXE: do direct toByte to avoid tmp alloc
                        "}"+
                        "if ( _elem_len < struct.getByteSize() )"+
                        "    throw new RuntimeException(\"Illegal size when rewriting object array value. elem size:\"+_elem_len+\" new object size:\"+struct.getByteSize()+\"\");"+
                        (trackChanges ? "if (tracker!=null) tracker.addChange(_st_off+$1*_elem_len, struct.getByteSize(),"+ fieldName +"); ":"") +
//                        "unsafe.copyMemory(struct.___bytes,struct.___offset,___bytes,(long)_st_off+$1*_elem_len,(long)struct.getByteSize());"+
                        "struct.___bytes.copyTo(___bytes,(long)_st_off+$1*_elem_len,struct.___offset,(long)struct.getByteSize());"+
                    "}"
                    );
                }
            } else { // read access
                if ( arrayType == boolean.class ) {
                    String src = prefix + "return ___bytes.getBool" + insert + "( (long)_st_off+$1); }";
                    method.setBody(src);
                } else
                if ( arrayType == byte.class ) {
                    method.setBody(prefix+"return ___bytes.get"+insert+"( (long)_st_off+$1);}");
                } else
                if ( arrayType == char.class ) {
                    method.setBody(prefix+"return ___bytes.getChar"+insert+"( (long)_st_off+$1*2); }");
                } else
                if ( arrayType == short.class ) {
                    method.setBody(prefix+"return ___bytes.getShort"+insert+"( (long)_st_off+$1*2);}");
                } else
                if ( arrayType == int.class ) {
                    method.setBody(prefix+"return ___bytes.getInt"+insert+"( (long)_st_off+$1*4);}");
                } else
                if ( arrayType == long.class ) {
                    method.setBody(prefix+"return ___bytes.getLong"+insert+"( (long)_st_off+$1*8);}");
                } else
                if ( arrayType == double.class ) {
                    method.setBody(prefix+"return ___bytes.getDouble"+insert+"( (long)_st_off+$1*8);}");
                } else
                if ( arrayType == float.class ) {
                    method.setBody(prefix+"return ___bytes.getFloat"+insert+"( (long)_st_off+$1*4);}");
                } else { // object array
                    String meth =
                    prefix+
                        "int _elem_len=___bytes.getInt("+off+"+8+___offset); "+
                        "return ("+fieldInfo.getArrayType().getName()+")___fac.getStructPointerByOffset(___bytes,(long)_st_off+$1*_elem_len);"+
                    "}";
                    method.setBody(meth);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("in field "+fieldInfo.getField(),ex);
        }
    }

    public void defineStructSetCAS(FSTClazzInfo.FSTFieldInfo casAcc, FSTClazzInfo clInfo, CtMethod method) {
        int off = casAcc.getStructOffset();
        try {
            if ( method.getParameterTypes().length != 2 ) {
                throw new RuntimeException("CAS setter requires expected and newValue args");
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        try {
            if ( casAcc.getType() == int.class ) {
                method.setBody("return ___bytes.compareAndSwapInt("+off+"+___offset,$1,$2);");
            } else
            if ( casAcc.getType() == int.class ) {
                method.setBody("return ___bytes.compareAndSwapLong("+off+"+___offset,$1,$2);");
            } else {
                throw new RuntimeException("CAS access only applicable to int and long.");
            }
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void defineArrayElementSize(FSTClazzInfo.FSTFieldInfo indexfi, FSTClazzInfo clInfo, CtMethod method) {
        int off = indexfi.getStructOffset();
        try {
            method.setBody("{ return ___bytes.getInt("+off+"+8+___offset); }");
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void defineArrayIndex(FSTClazzInfo.FSTFieldInfo fieldInfo, FSTClazzInfo clInfo, CtMethod method) {
        int index = fieldInfo.getStructOffset();
        try {
            method.setBody("{ return (int) (___bytes.getInt( ___offset+"+index+")+___offset); }");
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void defineArrayPointer(FSTClazzInfo.FSTFieldInfo indexfi, FSTClazzInfo clInfo, CtMethod method) {
        int index = indexfi.getStructOffset();
        CtClass[] parameterTypes = new CtClass[0];
        try {
            parameterTypes = method.getParameterTypes();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        if ( parameterTypes != null && parameterTypes.length ==1 ) {
            try {
                if (indexfi.isIntegral()) {
                    method.setBody("{ ___fac.fillPrimitiveArrayBasePointer($1,___bytes, ___offset, "+index+"); }");
                } else {
                    method.setBody("{ ___fac.fillTypedArrayBasePointer($1,___bytes, ___offset, "+index+"); }");
                }
            } catch (CannotCompileException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                if (indexfi.isIntegral()) {
                    method.setBody("{ return (org.nustaq.offheap.structs.FSTStruct)___fac.createPrimitiveArrayBasePointer(___bytes, ___offset, "+index+"); }");
                } else
                    method.setBody("{ return ("+indexfi.getArrayType().getName()+")___fac.createTypedArrayBasePointer(___bytes, ___offset, "+index+"); }");
            } catch (CannotCompileException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void defineArrayLength(FSTClazzInfo.FSTFieldInfo fieldInfo, FSTClazzInfo clInfo, CtMethod method) {
        int off = fieldInfo.getStructOffset();
        try {
            method.setBody("{ return ___bytes.getInt("+off+"+4+___offset); }");
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void defineFieldStructIndex(FSTClazzInfo.FSTFieldInfo fieldInfo, FSTClazzInfo clInfo, CtMethod method) {
        int off = fieldInfo.getStructOffset();
        try {
            method.setBody("{ return "+off+"; }");
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void defineStructReadAccess(FieldAccess f, CtClass type, FSTClazzInfo.FSTFieldInfo fieldInfo) {
        boolean vola = fieldInfo.isVolatile();
        validateAnnotations(fieldInfo,vola);
        String insert = "";
        String statement = "";
        if ( vola ) {
            insert = "Volatile";
        }
        int off = fieldInfo.getStructOffset();
        try {
            if ( type == CtPrimitiveType.booleanType ) {
                f.replace("$_ = ___bytes.getBool"+insert+"("+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.byteType ) {
                f.replace("$_ = ___bytes.get"+insert+"("+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.charType ) {
                f.replace("$_ = ___bytes.getChar"+insert+"("+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.shortType ) {
                f.replace("$_ = ___bytes.getShort"+insert+"("+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.intType ) {
                f.replace("$_ = ___bytes.getInt"+insert+"("+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.longType ) {
                f.replace("$_ = ___bytes.getLong"+insert+"("+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.floatType ) {
                f.replace("$_ = ___bytes.getFloat"+insert+"("+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.doubleType ) {
                f.replace("$_ = ___bytes.getDouble"+insert+"("+off+"+___offset);");
            } else { // object ref or obje array
                String typeString = type.getName();
                if ( type.isArray() ) {
                    throw new RuntimeException("invalid direct access to array in struct code. Use arrayaccessor name convention as documented."+fieldInfo);
                }
                if ( ! FSTStruct.class.isAssignableFrom(Class.forName( typeString) ) ) {
                    throw new RuntimeException("invalid type, require at least FSTStruct "+fieldInfo);
                }
                statement = "{ int tmpIdx = ___bytes.getInt( " + off + " + ___offset); if (tmpIdx < 0) return null;" +
                        "long __tmpOff = ___offset + tmpIdx; " +
                        "" + typeString + " tmp = (" + typeString + ")___fac.getStructPointerByOffset(___bytes,__tmpOff); " +
                        "if ( tmp == null ) return null;" +
                        (trackChanges ? "tmp.tracker = new org.nustaq.offheap.structs.FSTStructChange(tracker,\"" + fieldInfo.getName() + "\"); " : "")+
                        "$_ = tmp; " +
                        "}";
                f.replace(statement);
//                f.replace("{ Object _o = unsafe.toString(); $_ = _o; }");
            }
        } catch (Exception ex) {
            throw new RuntimeException(""+fieldInfo+" "+statement,ex);
        }
    }
}
