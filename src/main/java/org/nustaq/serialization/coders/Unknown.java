package org.nustaq.serialization.coders;

import java.io.Serializable;
import java.util.*;

/**
 * Created by ruedi on 05/06/15.
 *
 * Can used by some Coders (namely Json) to represent objects of unknown classes.
 * As binary codec's do not include fieldnames in their outputstream, this can only
 * be supported for fieldname containing (but slow) codec's.
 *
 */
public class Unknown implements Serializable {

    Map<String,Object> fields;
    List items;
    String type;

    public Unknown() {
    }

    public Unknown(String type) {
        setType(type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public Unknown set(String name, Object value) {
        if ( fields == null ) {
            fields = new HashMap();
        }
        fields.put(name, value);
        return this;
    }

    public Unknown add(Object item) {
        if ( items == null ) {
            items = new ArrayList();
        }
        items.add(item);
        return this;
    }

    public int getInt(String name) {
        Number o = (Number) fields.get(name);
        if ( o != null ) {
            return o.intValue();
        }
        return 0;
    }

    public double getDouble(String name) {
        Number o = (Number) fields.get(name);
        if ( o != null ) {
            return o.doubleValue();
        }
        return 0;
    }

    public String getString(String name) {
        Object o = fields.get(name);
        if ( o != null ) {
            return o.toString();
        }
        return null;
    }

    public Object[] getArr( String name) {
        Object o[] = (Object[]) fields.get(name);
        if ( o != null ) {
            return o;
        }
        return null;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public String getType() {
        return type;
    }

    public List getItems() {
        if ( items == null ) {
            items = new ArrayList();
        }
        return items;
    }

    public boolean isSequence() {
        return items != null && (fields == null || fields.size() == 0);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("<").append(type).append("> ");
        if ( isSequence() ) {
            sb.append("[ ");
            List items = getItems();
            for (int i = 0; i < items.size(); i++) {
                Object o = items.get(i);
                sb.append(o);
                if ( i != items.size()-1 ) {
                    sb.append(", ");
                }
            }
            sb.append(" ]");
            return sb.toString();
        } else {
            sb.append("{ ");
            for (Map.Entry<String, Object> stringObjectEntry : fields.entrySet()) {
                sb.append(stringObjectEntry.getKey()).append(" : ").append(stringObjectEntry.getValue());
                sb.append(", ");
            }
            sb.append(" }");
            return sb.toString();
        }
    }
}
