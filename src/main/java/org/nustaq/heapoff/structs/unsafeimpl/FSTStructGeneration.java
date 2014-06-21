package org.nustaq.heapoff.structs.unsafeimpl;

import org.nustaq.serialization.FSTClazzInfo;
import javassist.*;
import javassist.expr.FieldAccess;

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
 * Date: 22.06.13
 * Time: 20:50
 * To change this template use File | Settings | File Templates.
 */
public interface FSTStructGeneration {

    FSTStructGeneration newInstance();
    void defineFieldStructIndex(FSTClazzInfo.FSTFieldInfo fieldInfo, FSTClazzInfo clInfo, CtMethod method);
    void defineStructWriteAccess(FieldAccess f, CtClass type, FSTClazzInfo.FSTFieldInfo fieldInfo);
    void defineStructReadAccess(FieldAccess f, CtClass type, FSTClazzInfo.FSTFieldInfo fieldInfo);
    void defineArrayAccessor(FSTClazzInfo.FSTFieldInfo fieldInfo, FSTClazzInfo clInfo, CtMethod method);
    void defineArrayLength(FSTClazzInfo.FSTFieldInfo fieldInfo, FSTClazzInfo clInfo, CtMethod method);
    void defineArrayIndex(FSTClazzInfo.FSTFieldInfo indexfi, FSTClazzInfo clInfo, CtMethod method);
    void defineArrayElementSize(FSTClazzInfo.FSTFieldInfo indexfi, FSTClazzInfo clInfo, CtMethod method);
    void defineArrayPointer(FSTClazzInfo.FSTFieldInfo indexfi, FSTClazzInfo clInfo, CtMethod method);
    void defineStructSetCAS(FSTClazzInfo.FSTFieldInfo casAcc, FSTClazzInfo clInfo, CtMethod method);
}
