package jdk_bug.foo.bean;
import java.nio.ByteBuffer;


public class AnImpl extends SomeAbstract {

//	private Properties p = new Properties();
	public ByteBuffer p = ByteBuffer.allocateDirect(1000);
    {
        p.putInt(13);
    }
}
