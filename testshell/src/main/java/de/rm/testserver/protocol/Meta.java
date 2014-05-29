package de.rm.testserver.protocol;

import minbin.gen.GenMeta;

/**
 * Created by ruedi on 27.05.2014.
 */
public class Meta implements GenMeta {

    public Class[] getClasses() {
        return new Class[]{ BasicValues.class, MirrorRequest.class, Event.class, TestRequest.class, Person.class };
    }

}
