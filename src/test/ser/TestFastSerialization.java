package ser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectOutput;
import ser.externalizable.ExternalizableTestClass;


public class TestFastSerialization {

	@Test
	public void serializationTest() throws IOException, ClassNotFoundException {

		// Instantiation of the ExternalizableTestClass object
		int integer = 10;
		String path = "path";
		ExternalizableTestClass object = new ExternalizableTestClass(integer, path);

		// when serialized and deserialized, the integer value should be
		// overwritten by readResolve and set to 1.

		// Java
		// 1. serialize
		byte[] data1;
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(bos)) {
			out.writeObject(object);
			data1 = bos.toByteArray();
		}

		// 2. deserialize
		ExternalizableTestClass object1;
		try (ByteArrayInputStream bis = new ByteArrayInputStream(data1); ObjectInput in = new ObjectInputStream(bis)) {
			object1 = (ExternalizableTestClass) in.readObject();
		}

		Assert.assertEquals(1, object1.getInteger());

		// FST
		// 1. serialize
		FSTConfiguration config = FSTConfiguration.createDefaultConfiguration();
		byte[] data2 = config.asByteArray(object);

		// 2. deserialize
		ExternalizableTestClass object2 = (ExternalizableTestClass) config.asObject(data2);

		Assert.assertEquals(1, object2.getInteger());
	}

}
