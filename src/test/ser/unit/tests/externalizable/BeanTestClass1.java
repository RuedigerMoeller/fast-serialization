package ser.unit.tests.externalizable;

import java.io.Serializable;

public class BeanTestClass1 implements Serializable {
	private static final long serialVersionUID = 1L;

	private ExternalizableTestClass object1;

	private ExternalizableTestClass object2;

	public BeanTestClass1(ExternalizableTestClass object1, ExternalizableTestClass object2) {
		this.setObject1(object1);
		this.setObject2(object2);
	}

	public ExternalizableTestClass getObject1() {
		return object1;
	}

	public void setObject1(ExternalizableTestClass object1) {
		this.object1 = object1;
	}

	public ExternalizableTestClass getObject2() {
		return object2;
	}

	public void setObject2(ExternalizableTestClass object2) {
		this.object2 = object2;
	}

}
