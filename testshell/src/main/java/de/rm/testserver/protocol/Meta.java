package de.rm.testserver.protocol;

import de.ruedigermoeller.serialization.annotations.Serialize;

import java.io.Serializable;

/**
 * Created by ruedi on 27.05.2014.
 */
public class Meta implements Serializable {

    Class classes[] = { BasicValues.class, MirrorRequest.class, Person.class, TestRequest.class };

}
