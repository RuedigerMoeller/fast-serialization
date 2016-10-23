package kson;

/**
 * Created by ruedi on 12.08.2014.
 */
public class KsonPhoneNumber {
    private String type;
    private String value;

    public KsonPhoneNumber(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}
