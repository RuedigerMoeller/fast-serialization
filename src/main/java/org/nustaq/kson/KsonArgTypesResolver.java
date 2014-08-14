package org.nustaq.kson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ruedi on 14.08.2014.
 */
public interface KsonArgTypesResolver {
    Class[] getArgTypes(Class outerClass, List currentParse);
}
