package de.ruedigermoeller.serialization.dson;

/**
 * Created by ruedi on 26.12.13.
 */
public class DsonParseException extends RuntimeException {

    public DsonParseException(String ms, DsonCharInput in) {
        super(ms+":"+in.getString(in.position()-10,10));
    }

    public DsonParseException(String s, DsonCharInput in, Throwable ex) {
        super(s+":"+in.getString(in.position()-10,10),ex);
    }
}
