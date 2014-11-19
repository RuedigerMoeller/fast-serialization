package unit.tests.externalizable;

import java.io.Serializable;
import java.util.Set;

public class BeanTestClass2 implements Serializable {
	private static final long serialVersionUID = 1L;

	private ExternalizableTestClass object;

	private Set<ExternalizableTestClass> set;

	public BeanTestClass2(ExternalizableTestClass object, Set<ExternalizableTestClass> set) {
		this.setObject1(object);
		this.setSet(set);
	}

	public ExternalizableTestClass getObject() {
		return object;
	}

	public void setObject1(ExternalizableTestClass object) {
		this.object = object;
	}

	public Set<ExternalizableTestClass> getSet() {
		return set;
	}

	public void setSet(Set<ExternalizableTestClass> set) {
		this.set = set;
	}

}
