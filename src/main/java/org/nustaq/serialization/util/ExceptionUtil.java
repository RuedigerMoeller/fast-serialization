package org.nustaq.serialization.util;

import org.omg.CORBA.SystemException;

/**
 * Created by fabianterhorst on 24.09.16.
 */

public final class ExceptionUtil {

    public static RuntimeException rethrow(Exception e) {
        if (e instanceof SystemException) {
            throw (SystemException) e;
        }
        RuntimeException exception = new RuntimeException(e);
        StackTraceElement[] stackTrace = exception.getStackTrace();
        StackTraceElement[] newStackTrace = new StackTraceElement[stackTrace.length - 1];
        System.arraycopy(stackTrace, 1, newStackTrace, 0, stackTrace.length - 1);
        exception.setStackTrace(newStackTrace);
        throw exception;
    }

    private ExceptionUtil() {
    }
}
