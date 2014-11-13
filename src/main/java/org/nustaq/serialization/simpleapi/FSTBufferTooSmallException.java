package org.nustaq.serialization.simpleapi;

/**
 * Created by ruedi on 09.11.14.
 */
public class FSTBufferTooSmallException extends RuntimeException {

    public static FSTBufferTooSmallException Instance = new FSTBufferTooSmallException();

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
