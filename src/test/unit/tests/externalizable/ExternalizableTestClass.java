package unit.tests.externalizable;
import java.io.*;

public class ExternalizableTestClass implements Externalizable {
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalizableTestClass.class);
	
	private String path;
	private int integer;
	
	public ExternalizableTestClass() {
	}

	public ExternalizableTestClass(int integer, String path) {
		this.path = path;
		this.integer = integer;
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(getPath());
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//		LOGGER.info("readExternal()");
		setPath(in.readUTF());
	}

	public Object readResolve() throws ObjectStreamException {
//		LOGGER.info("readResolve()");
		return new ExternalizableTestClass(1, this.getPath());
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getInteger() {
		return integer;
	}

	public void setInteger(int integer) {
		this.integer = integer;
	}
}
