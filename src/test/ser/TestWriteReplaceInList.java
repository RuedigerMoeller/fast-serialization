package ser;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import org.nustaq.serialization.FSTObjectRegistry;

public class TestWriteReplaceInList {

	@Test
	public void testWriteReplaceInList() throws IOException, ClassNotFoundException {
		Container c = new Container();

		BaseClass b1 = new BaseClass();
		b1.value = "morphMe";
		c.list.add(b1);
		BaseClass b2 = new BaseClass();
		b2.value = "morphMe";
		c.list.add(b2);

		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		FSTObjectOutput out = new FSTObjectOutput(buf, getTestConfiguration());
		out.writeObject(c);
		out.close();

		ObjectInput in = getTestConfiguration().getObjectInput(new ByteArrayInputStream(buf.toByteArray()));
		Container res = (Container) in.readObject();
		assertEquals("you have morphed", ((Morpher) res.list.get(0)).value);
		assertEquals("you have morphed", ((Morpher) res.list.get(1)).value);
	}

	@org.junit.Before
	public void setUp() throws Exception {
		FSTObjectRegistry.POS_MAP_SIZE = 1;
	}

	protected FSTConfiguration getTestConfiguration() {
		FSTConfiguration.isAndroid = false;
		return FSTConfiguration.createDefaultConfiguration();
	}

	public static class BaseClass implements Serializable {
		public String value;

		private Object writeReplace() throws ObjectStreamException {
			if (value.equals("morphMe")) {
				Morpher m = new Morpher();
				m.value = "you have morphed";
				return m;
			}
			return this;
		}
	}

	public static class Morpher extends BaseClass {
	}

	public static class Container implements Serializable {
		public List list = new ArrayList<>();
	}

}
