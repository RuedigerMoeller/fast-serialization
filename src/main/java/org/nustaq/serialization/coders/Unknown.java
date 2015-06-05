package org.nustaq.serialization.coders;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ruedi on 05/06/15.
 *
 * used by some Coders (currently Json) to represent objects of unknown classes. Only
 * possible for coders including fieldNames in the stream
 *
 */
public class Unknown implements Serializable {

    Map<String,Object> fields;
    List items;
    String type;

    public Unknown() {
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFieldValue(String name, Object value ) {
        if ( fields == null ) {
            fields = new HashMap();
        }
        fields.put(name, value);
    }

    public Unknown addItem( Object item ) {
        if ( items == null ) {
            items = new ArrayList();
        }
        items.add(item);
        return this;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public String getType() {
        return type;
    }

    public List getItems() {
        return items;
    }
}
