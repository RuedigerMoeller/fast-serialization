package de.rm.testserver.protocol;

import java.io.Serializable;
import java.util.*;

/**
 * Created by ruedi on 16.05.14.
 */
public class BasicValues implements Serializable {

    String aString = "a String";
    String aStringArr[] = { "One", "Two" };
    int anInt = 123;
    int anIntArray[] = {1,2,3,4};
    List aList = new ArrayList<String>();
    Map aMap = new HashMap<String,Double>();

    public BasicValues() {
        aList.addAll(Arrays.asList("A","B","C"));
        aMap.put("Rüdiger", 42.42d);
        aMap.put("Emil", 7.07d);
    }
}
