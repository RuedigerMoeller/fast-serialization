package org.nustaq.serialization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})

/**
 * support for adding fields. For each release increment the version value. No Version annotation means version=0
 *
 * e.g.
 *
 * class MyClass implements Serializable {
 *
 *     // fields on initial release 1.0
 *     int x;
 *     String y;
 *
 *     // fields added with release 1.5
 *     @Version(1) String added;
 *     @Version(1) String alsoAdded;
 *
 *     // fields added with release 2.0
 *     @Version(2) String added;
 *     @Version(2) String alsoAdded;
 *
 * }
 *
 * Notes:
 * - Removing field will break backward compatibility
 * - Can slow down serialization over time (if many versions)
 * - does not work for Externalizable or Classes which make use of JDK-special features such as readObject/writeObject
 *   (AKA does not work if fst has to fall back to 'compatible mode' for an object).
 *
 */
public @interface Version {
    byte value();
}
