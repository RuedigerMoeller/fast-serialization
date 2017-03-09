package kson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ruedi on 12.08.2014.
 */
public class KsonCustomer {
    private int id;
    private String name;

    private List<KsonPhoneNumber> phoneNumbers = new ArrayList<KsonPhoneNumber>();


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<KsonPhoneNumber> getPhoneNumbers() {
        return phoneNumbers;
    }
}
