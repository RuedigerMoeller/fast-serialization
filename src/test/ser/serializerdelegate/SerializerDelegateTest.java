package ser.serializerdelegate;

import junit.framework.Assert;
import org.junit.Test;
import org.nustaq.serialization.*;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by ruedi on 07/07/15.
 */
public class SerializerDelegateTest {

    static interface WrapperInterface {
        Object getWrapped();
    }

    static class WrappedObject implements WrapperInterface, Serializable {
        Object wrapped;

        public WrappedObject(Object wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public Object getWrapped() {
            return wrapped;
        }
    }

    static class MyExample implements Serializable{
        Object aa = new WrappedObject( "Hello" );
        Object object = "Hello";
    }


    static class MySkippingSerializer extends FSTBasicObjectSerializer {

        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
            out.writeObject(((WrappedObject)toWrite).getWrapped());
        }

        @Override
        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
            in.readObject(); // ensure everything read and registered same order as on write time
            return REALLY_NULL; // but just don't use/return it.
            // Note: returning 'null' will result in fst attempting to construct an instance
            // and call readObject on the serializer
        }
    }

    @Test
    public void main() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        final MySkippingSerializer skippingSer = new MySkippingSerializer();

        conf.setSerializerRegistryDelegate(new FSTSerializerRegistryDelegate() {
            @Override
            public FSTObjectSerializer getSerializer(Class cl) {
                if (WrapperInterface.class.isAssignableFrom(cl)) {
                    return skippingSer;
                }
                return null;
            }
        });

        MyExample myExample = new MyExample();
        byte[] b = conf.asByteArray(myExample);

        MyExample res = (MyExample) conf.asObject(b);
        Assert.assertTrue(res.aa == null);

    }
}
