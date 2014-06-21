package org.rm.testserver.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ruedi on 17.05.14.
 */
public class Person implements Serializable {

    String name;
    String firstName;
    String misc;

    public List<Person> friends = new ArrayList();
    public List<Person> followers = new ArrayList();

    public Person(String name, String firstName, String misc) {
        this.name = name;
        this.firstName = firstName;
        this.misc = misc;
    }

    public Person addFriend(Person p) { friends.add(p); return this; }
    public Person addFollower(Person p) { followers.add(p); return this; }

}
