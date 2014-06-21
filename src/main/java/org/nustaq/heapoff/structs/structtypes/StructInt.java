package org.nustaq.heapoff.structs.structtypes;

import org.nustaq.heapoff.structs.FSTStruct;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 24.07.13
 * Time: 15:21
 * To change this template use File | Settings | File Templates.
 */
public class StructInt extends FSTStruct {

    protected int value;

    public StructInt(int value) {
        this.value = value;
    }

    public int get() {
        return value;
    }

    public void set(int val) {
        value = val;
    }

    public boolean equals( Object o ) {
        if ( o instanceof StructInt ) {
            return ((StructInt) o).get() == value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value;
    }

}
