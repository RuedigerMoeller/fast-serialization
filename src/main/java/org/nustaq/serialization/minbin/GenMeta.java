package org.nustaq.serialization.minbin;

import java.io.Serializable;
import java.util.List;

/**
 * Created by ruedi on 29.05.2014.
 */
public interface GenMeta extends Serializable {
    public List<Class> getClasses();
}
