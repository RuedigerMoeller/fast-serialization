package de.ruedigermoeller.heapofftest;

public class SimpleHistogram {
    int operCount;
    int milliDelayCount[] = new int[100];
    int tenMilliDelayCount[] = new int[300];
    int hundredDelayCount[] = new int[1000];

    public SimpleHistogram() {
    }

    public int[] getMilliDelayCount() {
        return milliDelayCount;
    }

    public int[] getTenMilliDelayCount() {
        return tenMilliDelayCount;
    }

    public int[] getHundredDelayCount() {
        return hundredDelayCount;
    }


    public void runRequest(Runnable toRun) {
        long tim = System.currentTimeMillis();
        toRun.run();
        int dur = (int) (System.currentTimeMillis()-tim);
        operCount++;
        if ( dur < 50 )
            milliDelayCount[dur]++;
        else if ( dur < 3000 )
            tenMilliDelayCount[dur/10]++;
        else {
            hundredDelayCount[dur/100]++;
        }
    }

    public void clear() {
        milliDelayCount = new int[100];
        tenMilliDelayCount = new int[300];
        hundredDelayCount = new int[1000];
        operCount = 0;
    }

    public void addTo( SimpleHistogram other ) {
        for (int i = 0; i < milliDelayCount.length; i++) {
            other.milliDelayCount[i] += milliDelayCount[i];
        }
        for (int i = 0; i < tenMilliDelayCount.length; i++) {
            other.tenMilliDelayCount[i] += tenMilliDelayCount[i];
        }
        for (int i = 0; i < hundredDelayCount.length; i++) {
            other.hundredDelayCount[i] += hundredDelayCount[i];
        }
        other.operCount += operCount;
    }

    public void dump() {
        System.out.println( "ops:"+operCount);
        for (int i = 0; i < getMilliDelayCount().length; i++) {
            int i1 = getMilliDelayCount()[i];
            if ( i1 > 0 ) {
                System.out.println("["+i+"]\t"+i1);
            }
        }
        for (int i = 0; i < getTenMilliDelayCount().length; i++) {
            int i1 = getTenMilliDelayCount()[i];
            if ( i1 > 0 ) {
                System.out.println("["+i*10+"]\t"+i1);
            }
        }
        for (int i = 0; i < getHundredDelayCount().length; i++) {
            int i1 = getHundredDelayCount()[i];
            if ( i1 > 0 ) {
                System.out.println("["+i*100+"]\t"+i1);
            }
        }
    }

}