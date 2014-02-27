package de.ruedigermoeller.heapoff.structs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 16.07.13
 * Time: 10:32
 * To change this template use File | Settings | File Templates.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Align {
    int value();
}
