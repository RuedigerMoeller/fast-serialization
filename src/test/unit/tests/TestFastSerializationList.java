package unit.tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectOutput;

import unit.tests.externalizable.ExternalizableTestClass;

public class TestFastSerializationList {

	@SuppressWarnings("unchecked")
	@Test
	public void serializationTest() throws IOException, ClassNotFoundException {

		// Instantiation of the ExternalizableTestClass object
		int integer = 10;
		String path = "path";
		List<ExternalizableTestClass> list = new ArrayList<ExternalizableTestClass>();
		ExternalizableTestClass object = new ExternalizableTestClass(integer, path);
		list.add(object);
		list.add(object);

		// when serialized and deserialized, the integer value of all ExternalizableTestClass objects should be
		// overwritten by readResolve and set to 1.

		// Java
		// 1. serialize
		byte[] data1;
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(bos)) {
			out.writeObject(list);
			data1 = bos.toByteArray();
		}

		// 2. deserialize
		List<ExternalizableTestClass> list1 = new ArrayList<ExternalizableTestClass>();
		try (ByteArrayInputStream bis = new ByteArrayInputStream(data1); ObjectInput in = new ObjectInputStream(bis)) {
			list1 = (List<ExternalizableTestClass>) in.readObject();
		}

		for (ExternalizableTestClass testClass : list1) {
			Assert.assertEquals(1, testClass.getInteger());
		}

		// FST
		// 1. serialize
		FSTConfiguration config = FSTConfiguration.createDefaultConfiguration();
		FSTObjectOutput out2 = config.getObjectOutput();
		out2.writeObject(list);
		byte[] data2 = out2.getCopyOfWrittenBuffer();
		out2.close();

		// 2. deserialize
		List<ExternalizableTestClass> list2 = (ArrayList<ExternalizableTestClass>) config.getObjectInput(data2).readObject();
		
		for (ExternalizableTestClass testClass : list2) {
			Assert.assertEquals(1, testClass.getInteger());
		}
	}

}
