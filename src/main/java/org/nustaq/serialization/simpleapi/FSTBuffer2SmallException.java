package org.nustaq.serialization.simpleapi;

/**
 * Created by ruedi on 09.11.14.
 */
public class FSTBuffer2SmallException extends RuntimeException {

    int requiredSize;

    public FSTBuffer2SmallException(int requiredSize) {
        this.requiredSize = requiredSize;
    }

    public int getRequiredSize() {
        return requiredSize;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
