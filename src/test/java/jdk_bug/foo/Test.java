package jdk_bug.foo;

import jdk_bug.foo.bean.TestBean;

// submitted & found by jswaelens from github
public class Test {

	public static void main(String[] args) {
		
		TestBean b =new TestBean();
		b.doSomething(false);
	}
}
