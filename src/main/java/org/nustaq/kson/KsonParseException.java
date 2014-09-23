package org.nustaq.kson;

import java.util.Stack;

/**
 * Created by ruedi on 26.12.13.
 */
public class KsonParseException extends RuntimeException {

    public KsonParseException(String ms, KsonCharInput in) {
        super(ms+":"+in.getString(in.position()-20,20)+getStackString(in));
    }

    private static String getStackString(KsonCharInput in) {
        if ( in instanceof KsonStringCharInput && ((KsonStringCharInput) in).stack != null) {
            final Stack<KsonDeserializer.ParseStep> stack = ((KsonStringCharInput) in).stack;
            String res = "\n\n";
            for (int i = stack.size()-1; i >= 0; i--) {
                KsonDeserializer.ParseStep parseStep = stack.get(i);
                res += "  "+parseStep+"\n";
            }
            return res;
        }
        return null;
    }

    public KsonParseException(String s, KsonCharInput in, Throwable ex) {
        super(s+":"+in.getString(in.position()-20,20)+getStackString(in),ex);
    }
}
