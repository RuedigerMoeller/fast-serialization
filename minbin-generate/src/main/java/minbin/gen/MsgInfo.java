package minbin.gen;

import java.lang.reflect.Parameter;

/**
 * Created by ruedi on 30.09.14.
 */
public class MsgInfo {

    Parameter[] parameters;
    Class params[];
	String name;
	String returnType;

	public MsgInfo(Class[] params, String name, String returnType, Parameter[] parameters) {
		this.params = params;
		this.name = name;
		this.returnType = returnType;
        this.parameters = parameters;
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

    public Parameter[] getParameters() {
        return parameters;
    }
}
