package org.nustaq.kson;

/**
 * Created by ruedi on 26.12.13.
 */
public class KsonParseException extends RuntimeException {

    public KsonParseException(String ms, KsonCharInput in) {
        super(ms+":"+in.getString(in.position()-10,10));
    }

    public KsonParseException(String s, KsonCharInput in, Throwable ex) {
        super(s+":"+in.getString(in.position()-80,80),ex);
    }
}
