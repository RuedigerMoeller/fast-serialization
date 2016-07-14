package ser.serializers;

import org.junit.Assert;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;

/**
 * User: Evgeniy Devyatyh
 */
public class FSTEnumSetSerializerTest {

    private enum SomeEnum {
        override {
            @Override
            public boolean getSomeBool() {
                return true;
            }
        }, plain;

        public boolean getSomeBool() {
            return false;
        }
    }

    static class Clazz implements Serializable {
        Set<SomeEnum> enumSet = EnumSet.of(SomeEnum.override, SomeEnum.plain);

    }

    @Test
    public void shouldSerializeEnumSet() throws Exception {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        byte[] bytes = conf.asByteArray(new Clazz());
        Clazz o = (Clazz) conf.asObject(bytes);
        Assert.assertEquals(o.enumSet, EnumSet.of(SomeEnum.override, SomeEnum.plain));
    }

    @Test
    public void shouldCompatibleSerializeEnumSet() throws Exception {
        FSTConfiguration conf = FSTConfiguration.createMinBinConfiguration();
        byte[] bytes = conf.asByteArray(new Clazz());
        Clazz o = (Clazz) conf.asObject(bytes);
        Assert.assertEquals(o.enumSet, EnumSet.of(SomeEnum.override, SomeEnum.plain));
    }

    @Test
    public void shouldCompatibleSerializeEmptyEnumSet() throws Exception {
        FSTConfiguration conf = FSTConfiguration.createMinBinConfiguration();
        Clazz object = new Clazz();
        object.enumSet = EnumSet.noneOf(SomeEnum.class);
        byte[] bytes = conf.asByteArray(object);
        Clazz o = (Clazz) conf.asObject(bytes);
        Assert.assertEquals(o.enumSet, EnumSet.noneOf(SomeEnum.class));
    }
}
