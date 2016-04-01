package org.nustaq.serialization.util;

public class FSTStringUnifier {
    
    final boolean ENABLED = true;
    Object cache[];
    final int lenmask;

    public FSTStringUnifier(int bits) {
        this.lenmask = (1<<bits)-1;
        if ( ENABLED)
        {
            this.cache = new String[1<<bits];
        }
    }

    public String getCachedInstance(String obj) {
        if (!ENABLED)
            return obj;
        if ( obj == null ) {
            return null;
        }
        Object hashObj = getHashObject(obj);
        if( hashObj != null )
        {
            int hc = hashObj.hashCode()&lenmask;
            Object cacheValue = cache[hc];
            if ( cacheValue != null && cacheValue.equals(obj) ) {
                return (String) cacheValue;
            }
            cache[hc] = obj;
        }
        return obj;
    }
    
    public Object getHashObject(String obj )
    {
        return obj;
    }
}
