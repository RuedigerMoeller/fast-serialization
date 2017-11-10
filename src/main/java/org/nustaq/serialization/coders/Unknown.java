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

    /**
     * access nested data. unk.dot( 3, "id" );
     * @param propPath
     * @return
     */
    public Object dot(Object ... propPath) {
        return dotImpl(0,propPath);
    }

    public <T> T ddot(Object ... propPath) {
        List res = new ArrayList(propPath.length*2);
        for (int i = 0; i < propPath.length; i++) {
            Object o = propPath[i];
            if ( o instanceof String) {
                if ( ((String) o).indexOf('.') >= 0 ) {
                    String split[] = ((String) o).split("\\.");
                    for (int j = 0; j < split.length; j++) {
                        String s = split[j];
                        res.add(s);
                    }
                } else
                    res.add(o);
            } else
                res.add(o);
        }
        return (T) dotImpl(0,res.toArray());
    }

    public Unknown dotUnk(Object ... propPath) {
        return (Unknown) dotImpl(0,propPath);
    }

    public String dotStr(Object ... propPath) {
        return (String) dotImpl(0,propPath);
    }

    public Integer dotInt(Object ... propPath) {
        return ((Number) dotImpl(0,propPath)).intValue();
    }

    private Object dotImpl(int index, Object ... propPath) {
        if (propPath[index] instanceof Number) {
            int idx = ((Number) propPath[index]).intValue();
            if ( ! isSequence() || idx < 0 || idx >= items.size() )
                return null;
            Object o = items.get(idx);
            if ( index == propPath.length-1 )
                return o;
            return ((Unknown)o).dotImpl(index+1, propPath);
        } else {
            String field = ""+propPath[index];
            if ( isSequence() )
                return null;
            Object o = get(field);
            if ( index == propPath.length-1 )
                return o;
            return ((Unknown)o).dotImpl(index+1, propPath);
        }
    }

    public int getInt(String name) {
        Number o = (Number) get(name);
        if ( o != null ) {
            return o.intValue();
        }
        return 0;
    }

    public double getDouble(String name) {
        Number o = (Number) get(name);
        if ( o != null ) {
            return o.doubleValue();
        }
        return 0;
    }

    public String getString(String name) {
        Object o = get(name);
        if ( o != null ) {
            return o.toString();
        }
        return null;
    }

    public Object get(String name) {
        if ( fields == null )
            return null;
        return fields.get(name);
    }

    public List getArr( String name) {
        Object o = get(name);
        if ( o instanceof Unknown ) {
            return ((Unknown) o).getItems();
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

    public Unknown fields(Map<String, Object> fields) {
        this.fields = fields;
        return this;
    }

    public Unknown items(List items) {
        this.items = items;
        return this;
    }

    public Unknown type(String type) {
        this.type = type;
        return this;
    }

    public Unknown put( String field, Object ... vals) {
        Unknown unk = new Unknown();
        for (int i = 0; i < vals.length; i++) {
            Object val = vals[i];
            unk.add(val);
        }
        set(field,unk);
        return this;
    }
}
