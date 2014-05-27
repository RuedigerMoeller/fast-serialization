package de.ruedigermoeller.serialization.testclasses.jdkcompatibility;

import com.cedarsoftware.util.DeepEquals;
import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;

import javax.security.auth.Subject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * Created by ruedi on 27.05.14.
 */
public class GitIssue12 {

    public static void main(String[] args) throws Exception
    {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

        Serializable toTest = new Subject();
        byte b[] = conf.asByteArray(toTest);
        Object deser = conf.asObject(b);
        if ( ! DeepEquals.deepEquals(toTest,deser) ) {
            throw new RuntimeException("oh no");
        }
    }
}
