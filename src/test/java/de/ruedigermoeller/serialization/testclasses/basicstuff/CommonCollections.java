package de.ruedigermoeller.serialization.testclasses.basicstuff;

import de.ruedigermoeller.serialization.annotations.*;
import de.ruedigermoeller.serialization.testclasses.HasDescription;

import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 12.11.12
 * Time: 22:46
 * To change this template use File | Settings | File Templates.
 */
public class CommonCollections implements Serializable, HasDescription {

    int z = 0;
    String test = "Test";

    ArrayList collections = new ArrayList();

    HashMap<String,Integer> map = new HashMap<String, Integer>();
    ArrayList<String> list = new ArrayList<String>();

    @Override
    public String getDescription() {
        return "In depth test of collections incl. collections of collections.<br> (ArrayList, ArrayDeque, ConcurrentLinkedQueue, Vector, TreeSet, LinkedList, TreeMap, ConcurrentHashMap, Hashtable, HashMap). Note: has been modified to workaround Kryo-specific Issue.";
    }

    static class MyComp implements Comparator, Serializable{ // Kryo can't handle this if non-static (invalid sanity check)
        @Override
        public int compare(Object o1, Object o2) {
            return (""+o1).compareTo(""+o2);
        }
    };

    public CommonCollections() {

        // fixme: test also null values/keys

        Collection al = new ArrayList(); fillCollectionRandom(al); collections.add(al);
        al = new ArrayDeque(); fillCollectionRandom(al); collections.add(al);
        al = new ConcurrentLinkedQueue(); fillCollectionRandom(al); collections.add(al);
        al = new Vector(); fillCollectionRandom(al); collections.add(al); // fails with jdk impl, why ?
//        al = new TreeSet(new MyComp()); fillCollectionRandom(al); collections.add(al); // Classcast Exception in KRYO
        al = new HashSet(); fillCollectionRandom(al); collections.add(al);
        al = new LinkedList(); fillCollectionRandom(al); collections.add(al);
//
        Map mp = new HashMap(); fillMapRandom(mp); collections.add(mp);
        mp = new Hashtable(); fillMapRandom(mp); collections.add(mp);
        mp = new ConcurrentHashMap(); fillMapRandom(mp); collections.add(mp);
        mp = new TreeMap(new MyComp()); fillMapRandom(mp); collections.add(mp);

        for (int i = 0; i<200;i ++ ) {
            map.put("xy" + i, i);
        }
        collections.add(map);

        for (int i = 0; i<200;i ++ ) {
            list.add("yx" + i);
        }
        collections.add(list);
        collections.add(new ArrayList(list));

    }

    public void fillMapRandom( Map map ) {
        for ( int i = 0; i < 20; i++ ) {
            Object key = randomValue(i);
            Object value = randomValue(i*7);
            map.put(key,value);
        }
    }

    public void fillCollectionRandom( Collection map ) {
        for ( int i = 0; i < 20; i++ ) {
            Object key = randomValue(i);
            map.add(key);
        }
    }

    private Object randomValue(int i) {
        Object value = null;
        switch ( i%5) {
            case 0:
                value = new Integer(i);
                break;
            case 1:
                value = i;
                break;
            case 2:
                value = new Long(i);
                break;
            case 3:
                value = ""+new Dimension(i,i);
                break;
            case 4:
                value = ""+new Rectangle(i,i);
                break;
        }
        return value;
    }

}
