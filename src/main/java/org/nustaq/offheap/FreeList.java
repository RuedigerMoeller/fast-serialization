package org.nustaq.offheap;

/**
 * Created by ruedi on 04.07.14.
 */
public class FreeList {

    long flists[][] = new long[32][]; // 16, 32, 64, 128, 256, 512,
    int flIndex[] = new int[32];

    // get freelist index for given length
    int computeListIndex( int len ) {
        int powIndex = 32-Integer.numberOfLeadingZeros(len-1);
        return powIndex;
    }

    int computeLen( int len ) {
        int powIndex = 32-Integer.numberOfLeadingZeros(len-1);
        return 1<<powIndex;
    }

    public long findFreeBlock(int len) {
        int index = computeListIndex(len);
        if (flIndex[index] > 0) {
            return flists[index][--flIndex[index]];
        }
        return 0;
    }

    public void addToFree(long offset, int len) {
        int index = computeListIndex(len);
        if ( flists[index] == null )
            flists[index] = new long[500];
        if ( flIndex[index] >= flists[index].length ) {
            long newFree[] = new long[Math.min(flists[index].length * 2, Integer.MAX_VALUE - 1)];
            System.arraycopy(flists[index], 0, newFree, 0, flIndex[index]);
            flists[index] = newFree;
        }
        flists[index][flIndex[index]++] = offset;
    }

    public static void main(String arg[]) {
        FreeList li = new FreeList();
        li.computeListIndex(13);
        li.computeListIndex(16);
        li.computeListIndex(15);
        li.computeListIndex(17);
        li.computeListIndex(99);
        li.computeListIndex(777);
        li.computeListIndex(127);
    }

}
