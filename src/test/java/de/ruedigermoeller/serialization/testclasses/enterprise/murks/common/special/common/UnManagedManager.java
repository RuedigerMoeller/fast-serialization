package de.ruedigermoeller.serialization.testclasses.enterprise.murks.common.special.common;

import de.ruedigermoeller.serialization.annotations.EqualnessIsIdentity;
import de.ruedigermoeller.serialization.testclasses.enterprise.ObjectOrientedDataType;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 02.12.12
 * Time: 15:05
 * To change this template use File | Settings | File Templates.
 */
public class UnManagedManager extends ObjectOrientedDataType {
    public UnManagedManager() {
    }

    public UnManagedManager(String valueString) {
        super(valueString);
    }
}
