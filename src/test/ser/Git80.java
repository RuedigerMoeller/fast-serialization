package ser;

import org.nustaq.serialization.*;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by ruedi on 05/08/15.
 */
public class Git80 {

    public static void main(final String[] args)
            throws Exception
    {
        URI uri = URI.create("http://example.com");
        URL url = uri.toURL();

        FSTConfiguration config = FSTConfiguration.createDefaultConfiguration();
        config.setShareReferences(false);
        //config.registerSerializer(URI.class, new FSTURISerializer(), true);
        //config.registerSerializer(URL.class, new FSTURLSerializer(), true);

        test(config, uri);
        test(config, url);
    }

    private static void test(FSTConfiguration config, Serializable object)
            throws IOException, ClassNotFoundException
    {
            FSTObjectOutput output = config.getObjectOutput();
            output.writeObject(object);
            byte[] serialized = output.getCopyOfWrittenBuffer();
            FSTObjectInput input = config.getObjectInput(serialized);
            Object result = input.readObject();
            System.out.println("equals: " + object.equals(result));
    }

    public static abstract class FSTStringBasedImmutableObjectSerializer<Type> extends FSTBasicObjectSerializer
    {

        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition)
                throws IOException
        {
            String s = toWrite.toString();
            out.writeStringUTF(s);
        }

        @Override
        public boolean alwaysCopy()
        {
            return true;
        }

        @Override
        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition)
                throws Exception
        {
            String s = in.readStringUTF();
            Object res = instantiate(s);
            return res;
        }

        protected abstract Type instantiate(String string)
                throws Exception;

    }

    public static class FSTURISerializer extends FSTStringBasedImmutableObjectSerializer<URI>
    {

        @Override
        protected URI instantiate(String string)
                throws URISyntaxException
        {
            return new URI(string);
        }

    }

    public static class FSTURLSerializer extends FSTStringBasedImmutableObjectSerializer<URL>
    {

        @Override
        protected URL instantiate(String string)
                throws MalformedURLException
        {
            return new URL(string);
        }

    }
}

