package de.ruedigermoeller.serialization.testclasses.remoting;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 19.11.12
 * Time: 12:04
 * To change this template use File | Settings | File Templates.
 */

import de.ruedigermoeller.serialization.testclasses.HasDescription;

import java.io.Serializable;

/**
 * measure speed with small objects as typical in remoting
 */
public class ShortRemoteCall implements Serializable, HasDescription {
    int methodId;
    Object args[];
    int someSenderIdOrWhatever;
    int callBackId;

    public ShortRemoteCall() {
    }

    public ShortRemoteCall(int dummy) {
        methodId = 44;
        args = new Object[]{}; // no args
        someSenderIdOrWhatever = 345782;
        callBackId = 35378758;
    }

    @Override
    public String getDescription() {
        return "measures overhead of stream initialization+classname decoding. Only one very short object is serialized";
    }
}
