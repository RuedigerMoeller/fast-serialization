package ser;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;

public class FSTJavaAssistTest {
  static FSTConfiguration cfg = FSTConfiguration.createDefaultConfiguration();
  static Class proxyClass;

  static {
    ProxyFactory factory = new ProxyFactory();
    factory.setSuperclass(Target.class);
    factory.setInterfaces(new Class[] { Serializable.class, WriteReplace.class });
    proxyClass = factory.createClass();
  }

  @Test
  public void testProxyInArrayList() throws Exception {
    Collection list = Arrays.asList(
            newProxy(new Target("One")),
            newProxy(new Target("Two")),
            newProxy(new Target("Three")));

    Collection result = thereAndBack(list);
    for (Object item : result) {
      System.out.println(item);
      assertThat(item, instanceOf(Target.class));
      assertThat(((Target) item).text, notNullValue());
      assertThat(((Target) item).text, instanceOf(String.class));
    }
  }

  private Target newProxy(final Target target) throws Exception {
    MethodHandler handler = new MyHandler(target);

    Object instance = proxyClass.newInstance();
    ((ProxyObject) instance).setHandler(handler);
    return (Target) instance;
  }

  public Collection thereAndBack(Collection data) throws Exception {
    byte[] buf = cfg.asByteArray(data);
    System.out.println(new String(buf));

    FSTObjectInput objectInput = new FSTObjectInput(new ByteArrayInputStream(buf));
    Collection list = (Collection) objectInput.readObject();
    objectInput.close();
    return list;
  }

  public static class MyHandler implements MethodHandler {
    private Target target;

    public MyHandler(Target target) {
      this.target = target;
    }

    public Target getTarget() {
      return target;
    }

    @Override
    public Object invoke(Object self, Method overridden, Method forwarder, Object[] args) throws Throwable {
      if ("writeReplace".equals(overridden.getName()) && args.length == 0) {
        return target;
      }
      return overridden.invoke(target, args);
    }
  }

  public static class Target implements Serializable {
    private static final long serialVersionUID = 1L;

    public String text;

    public Target() {
    }

    public Target(String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return text;
    }
  }

  public static interface WriteReplace {
    public Object writeReplace();
  }
}