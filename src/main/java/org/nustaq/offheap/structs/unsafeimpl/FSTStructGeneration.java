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

import org.nustaq.serialization.FSTClazzInfo;
import javassist.*;
import javassist.expr.FieldAccess;

/**
 * necessary methods to instrument/generate a struct subclass
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
