package minbin.gen;

/**
 * Created by ruedi on 30.09.14.
 */
public class MessageInfo {

	Class params[];
	String name;
	String returnType;

	public MessageInfo(Class[] params, String name, String returnType) {
		this.params = params;
		this.name = name;
		this.returnType = returnType;
	}

	public Class[] getParams() {
		return params;
	}

	public void setParams(Class[] params) {
		this.params = params;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}
}
