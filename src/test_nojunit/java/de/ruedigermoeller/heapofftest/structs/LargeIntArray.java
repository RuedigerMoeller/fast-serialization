package com.ruedigermoeller.heapofftest.structs;

import org.nustaq.offheap.structs.Align;
import org.nustaq.offheap.structs.FSTStruct;
import org.nustaq.offheap.structs.NoAssist;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 17.07.13
 * Time: 18:43
 * To change this template use File | Settings | File Templates.
 */
public class LargeIntArray extends FSTStruct {
    @Align(8)
    protected int largeArray[];

    public LargeIntArray() {
        largeArray = new int[2000000];
        for (int i = 0; i < largeArray.length; i++) {
            largeArray[i] = i;
        }
    }

    public int calcSumStraight() {
        int res = 0;
        final int max = largeArrayLen();
        for (int i = 0; i < max; i++) {
            res += largeArray(i);
        }
        return res;
    }

    transient protected FSTStruct pointer;
    @NoAssist
    public int calcSumPointered() {
        if ( pointer == null )
            pointer = new FSTStruct();
        largeArrayPointer(pointer);
        int res = 0;
        final int max = largeArrayLen();
        for (int i = 0; i < max; i++) {
            res += pointer.getInt();
            pointer.next(4);
        }
        return res;
    }

    public void largeArrayPointer(FSTStruct struct) {
        // generated
    }

    public int largeArray(int index) {
        return largeArray[index];
    }

    public int largeArrayLen() {
        return largeArray.length;
    }

    public static Object[] oldGenRef = new Object[10];

    public static void main(String arg[] ) throws InterruptedException {
        System.gc();
        byte[][] bytes = new byte[10*1024*1024][];
        Object randomStuff[] = new Object[1000];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = new byte[1024];
        }
        System.gc();
        System.gc();
        System.gc();
        // trigger gc's
        while( bytes[0][0] == 0 ) {
            for (int i = 0; i < randomStuff.length; i++) {
                randomStuff[i] = new Rectangle();
                if ( Math.random() > 0.9999 ) {
                    Thread.sleep(1);
                }
                if ( i < oldGenRef.length ) {
                    oldGenRef[i] = randomStuff[i];
                }
            }
            if ( Math.random() > 0.99999 ) {
                System.gc();
            }
        }

        if ( bytes[0][0] == 0 ) { // prevent escape analysis in case
            System.out.println("yes "+randomStuff);
        }
    }

}
