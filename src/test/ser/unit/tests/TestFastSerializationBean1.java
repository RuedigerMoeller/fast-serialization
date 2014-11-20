package ser.unit.tests;

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

import ser.unit.tests.externalizable.BeanTestClass1;
import ser.unit.tests.externalizable.ExternalizableTestClass;

public class TestFastSerializationBean1 {

	@Test
	public void serializationTest() throws IOException, ClassNotFoundException {
		// Instantiation of the ExternalizableTestClass object
		int integer = 10;
		String path = "path";
		ExternalizableTestClass object = new ExternalizableTestClass(integer, path);
		BeanTestClass1 bean = new BeanTestClass1(object, object);

		// when serialized and deserialized, the integer value of all ExternalizableTestClass objects should be
		// overwritten by readResolve and set to 1.

		// Java
		// 1. serialize
		byte[] data1;
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(bos)) {
			out.writeObject(bean);
			data1 = bos.toByteArray();
		}

		// 2. deserialize
		BeanTestClass1 bean1;
		try (ByteArrayInputStream bis = new ByteArrayInputStream(data1); ObjectInput in = new ObjectInputStream(bis)) {
			bean1 = (BeanTestClass1) in.readObject();
		}

		Assert.assertEquals(1, bean1.getObject1().getInteger());
		Assert.assertEquals(1, bean1.getObject2().getInteger());

		// FST
		// 1. serialize
		FSTConfiguration config = FSTConfiguration.createDefaultConfiguration();
		FSTObjectOutput out2 = config.getObjectOutput();
		out2.writeObject(bean);
		byte[] data2 = out2.getCopyOfWrittenBuffer();
		out2.close();

		// 2. deserialize
		BeanTestClass1 bean2 = (BeanTestClass1) config.getObjectInput(data2).readObject();
		
		Assert.assertEquals(1, bean2.getObject1().getInteger());
		Assert.assertEquals(1, bean2.getObject2().getInteger());
	}

}
