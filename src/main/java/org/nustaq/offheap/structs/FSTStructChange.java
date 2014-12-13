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
package org.nustaq.offheap.structs;

import org.nustaq.offheap.bytez.Bytez;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 01.11.13
 * Time: 14:17
 * To change this template use File | Settings | File Templates.
 */
// FIXME: move to long indizies
public class FSTStructChange implements Serializable {

    int changeOffsets[];
    int changeLength[];
    String changedFields[];
    int curIndex;
    transient public String _parentField;
    transient public FSTStructChange _parent;

    byte snapshot[]; // created by snapshotChanges, contains new byte values

    public FSTStructChange(FSTStructChange par, String parField) {
        _parentField = parField;
        _parent = par;
    }

    public FSTStructChange() {
        changeLength = new int[2];
        changeOffsets = new int[2];
        changedFields = new String[2];
    }

    public void addChange(int offset, int len, String field) {
        addChange((long)offset,(long)len,field);
    }

    public void addChange(long offset, int len, String field) {
        addChange((long)offset,(long)len,field);
    }

    public void addChange(long offset, long len, String field) {
        if ( _parent != null ) {
            _parent.addChange(offset,len,_parentField);
            return;
        }
        if ( curIndex > 0 && changeOffsets[curIndex-1]+changeLength[curIndex-1] == offset ) {
            changeLength[curIndex-1]+=len;
            return;
        }
        if ( curIndex >= changeOffsets.length ) {
            int newOff[] = new int[changeOffsets.length*2];
            System.arraycopy(changeOffsets,0,newOff,0,changeOffsets.length);
            int newLen[] = new int[changeOffsets.length*2];
            System.arraycopy(changeLength,0,newLen,0,changeLength.length);
            String newCF[] = new String[changeOffsets.length*2];
            System.arraycopy(changedFields,0,newCF,0,changedFields.length);

            changeOffsets = newOff;
            changeLength = newLen;
            changedFields = newCF;
        }
        changeOffsets[curIndex] = (int) (offset);
        changeLength[curIndex] = (int) len;
        changedFields[curIndex] = field;
        curIndex++;
    }

    public void rebase(int toSubtract) {
        for (int i = 0; i < curIndex; i++) {
            changeOffsets[i]-=toSubtract;
        }
    }

    /**
     * collects all changes and rebases.
     * @param originBase
     * @param origin
     */
    public void snapshotChanges(int originBase, Bytez origin) {
        int sumLen = 0;
        for (int i = 0; i < curIndex; i++) {
            sumLen += changeLength[i];
        }
        snapshot = new byte[sumLen];
        int targetIdx = 0;
        for (int i = 0; i < curIndex; i++) {
            int changeOffset = changeOffsets[i];
            int len = changeLength[i];
            for ( int ii = 0; ii < len; ii++) {
                snapshot[targetIdx++] = origin.get(changeOffset+ii);
            }
        }
        rebase(originBase);
    }

    public void applySnapshot(FSTStruct target) {
        Bytez arr = target.getBase();
        int baseIdx = (int) target.getOffset();
        int snapIdx = 0;
        for (int i = 0; i < curIndex; i++) {
            int changeOffset = changeOffsets[i];
            int len = changeLength[i];
            for ( int ii = 0; ii < len; ii++) {
                arr.put(baseIdx+changeOffset+ii,snapshot[snapIdx++]);
            }
        }
    }

    public String[] getChangedFields() {
        return changedFields;
    }

    public byte[] getSnapshot() {
        return snapshot;
    }
}
