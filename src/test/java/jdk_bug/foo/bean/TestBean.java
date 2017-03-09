package jdk_bug.foo.bean;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;


public class TestBean extends TestBeanAncestor {
	
	static ThreadLocal<FSTConfiguration> fstConf = null;
	
	static {
		
		fstConf = new ThreadLocal<FSTConfiguration>() {
			protected FSTConfiguration initialValue() {
				 return FSTConfiguration.createDefaultConfiguration();
			};
		};	
	}
	
	public void doSomething(boolean fst) {
		
		serialize(new BillingValidationRule(), fst);
	}
	
	private final class BillingValidationRule implements Serializable {

		public BillingValidationRule() {
		}

		public Object rule = null;
		public int var1 = 1;
		public int var2 = 2;
	}

	public static void serialize(Object object, boolean useFst) {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try {
			if(useFst) {
				FSTObjectOutput out = fstConf.get().getObjectOutput(baos);
				out.writeObject(object);
			} else {
                getByteBuffer(object).position(0);
                getByteBuffer(object).putInt(9999);
                getByteBuffer(object).position(0);
                System.out.println("original object "+ extractFirstBufferField(object));

				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(object);
				oos.close();

                ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
                Object deser = in.readObject();
                int res = extractFirstBufferField(deser);


                System.out.println("deserialized "+ res);
            }
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    protected static int extractFirstBufferField(Object deser) throws IllegalAccessException {
        ByteBuffer p = getByteBuffer(deser);
        p.position(0);
        return p.getInt();
    }

    private static ByteBuffer getByteBuffer(Object deser) throws IllegalAccessException {
        Field[] fields = deser.getClass().getDeclaredFields();
        Field th0 = null;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if ( field.getName().indexOf("this$0") >= 0 )
                th0 = field;
        }
        th0.setAccessible(true);
        TestBean this0 = (TestBean) th0.get(deser);
        return ((AnImpl) this0.factory.a).p;
    }
}
