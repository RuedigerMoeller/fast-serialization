package de.rm.testserver.protocol;

import de.ruedigermoeller.serialization.annotations.Serialize;
import minbin.gen.GenMeta;

import java.io.Serializable;

/**
 * Created by ruedi on 27.05.2014.
 */
public class Meta implements GenMeta {

    public Class[] getClasses() {
        return new Class[]{BasicValues.class, MirrorRequest.class, Person.class, TestRequest.class};
    }

}
