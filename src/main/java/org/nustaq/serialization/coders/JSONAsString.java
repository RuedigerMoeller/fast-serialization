package org.nustaq.serialization.coders;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by ruedi on 21.03.17.
 *
 * advice json serializer to convert annotated field to a string (e.g. byte or char arrays)
 *
 * !!!!!!!! supports byte[] only currently !!!!!!!!!!!!!!!
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)

public @interface JSONAsString {
}
