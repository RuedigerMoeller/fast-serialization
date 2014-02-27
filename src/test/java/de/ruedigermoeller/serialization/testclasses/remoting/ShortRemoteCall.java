package de.ruedigermoeller.serialization.testclasses.remoting;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 19.11.12
 * Time: 12:04
 * To change this template use File | Settings | File Templates.
 */

import java.io.Serializable;

/**
 * measure speed with small objects as typical in remoting
 */
public class ShortRemoteCall implements Serializable {
    int methodId = 44;
    Object args[] = {}; // no args
    int someSenderIdOrWhatever = 345782;
    int callBackId = 35378758;
}
