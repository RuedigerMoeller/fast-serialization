package org.rm.testserver.protocol;

import java.io.Serializable;
import java.util.*;

/**
 * Created by ruedi on 16.05.14.
 */
public class BasicValues implements Serializable {

    String aString = "a String";
    String aStringArr[] = { "One", "Two" };

    byte bytes[] = { -1, -2, -3, 0, 127, -128 };
    byte b = -128;
    byte b1 = 0;
    byte b2 = 127;

    int anInt = Integer.MIN_VALUE;
    int anInt1 = Integer.MAX_VALUE;
    int anInt2 = 7777;
    int anIntArray[] = {1,2,3,4, anInt, anInt1, 0,123123,-123123};

    public List aList = new ArrayList<String>();
    public Map aMap = new HashMap();

    public BasicValues() {
        aList.addAll(Arrays.asList("A","B","C"));
        aMap.put("Rüdiger", 42.42d);
        aMap.put("Emil", 7.07d);
    }
}
