package org.nustaq.serialization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})

/**
 * support for adding fields without breaking compatibility to old streams.
 * For each release of your app increment the version value. No Version annotation means version=0.
 * Note that each added field needs to be annotated.
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
 *     @Version(2) String addedv2;
 *     @Version(2) String alsoAddedv2;
 *
 * }
 *
 * If an old class is read, new fields will be set to default values. You can register a VersionConflictListener
 * at FSTObjectInput in order to fill in defaults for new fields.
 *
 * Notes/Limits:
 * - Removing fields will break backward compatibility. You can only Add new fields.
 * - Can slow down serialization over time (if many versions)
 * - does not work for Externalizable or Classes which make use of JDK-special features such as readObject/writeObject
 *   (AKA does not work if fst has to fall back to 'compatible mode' for an object).
 * - in case you use custom serializers, your custom serializer has to handle versioning
 *
 */
public @interface Version {
    byte value();
}
