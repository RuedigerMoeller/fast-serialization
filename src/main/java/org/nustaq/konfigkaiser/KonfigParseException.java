package org.nustaq.konfigkaiser;

/**
 * Created by ruedi on 26.12.13.
 */
public class KonfigParseException extends RuntimeException {

    public KonfigParseException(String ms, KKCharInput in) {
        super(ms+":"+in.getString(in.position()-10,10));
    }

    public KonfigParseException(String s, KKCharInput in, Throwable ex) {
        super(s+":"+in.getString(in.position()-30,30),ex);
    }
}
