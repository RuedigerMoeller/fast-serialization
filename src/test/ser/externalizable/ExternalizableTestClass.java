package ser.externalizable;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;


public class ExternalizableTestClass implements Externalizable {
	

	private String path;
	private int integer;
	
	public ExternalizableTestClass() {
	}

	public ExternalizableTestClass(int integer, String path) {
		this.path = path;
		this.integer = integer;
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(getPath());
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		setPath(in.readUTF());
	}

	public Object readResolve() throws ObjectStreamException {
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
