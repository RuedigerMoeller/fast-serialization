package de.ruedigermoeller.serialization;

/**
 * Created by ruedi on 31.03.14.
 */
public interface FSTCrossPlatformSerialzer extends FSTObjectSerializer {

    boolean writeTupleEnd();
    boolean asMap();

}
