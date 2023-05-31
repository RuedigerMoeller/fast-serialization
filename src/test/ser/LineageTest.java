package ser;

import java.io.Serializable;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.nustaq.serialization.FSTClazzLineageInfo.*;

/**
 * Created by odd on 2017-03-09.
 */
@Ignore // do not understand this test case. fails with jdk 1.20
public class LineageTest {
  public static class O {}
  public static class OO extends O {}
  public interface I {}
  public interface II extends I {}
  public static class OIO extends O implements I {}
  public static class OIOO extends OIO {}
  public static class OIOOIO extends OIOO implements I {}
  public static class OIOIIO extends OIO implements II {}
  public static class OIOOIIO extends OIOO implements II {}

  @Test
  public void testSpecificity() {
    assertSpecificity(null, 0);
    assertSpecificity(Object.class, 0);
    assertSpecificity(Serializable.class, 1);
    assertSpecificity(String.class, 4);
    assertSpecificity(O.class, 1);
    assertSpecificity(OO.class, 2);
    assertSpecificity(I.class, 1);
    assertSpecificity(II.class, 2);
    assertSpecificity(OIO.class, 3);
    assertSpecificity(OIOO.class, 4);
    assertSpecificity(OIOOIO.class, 6);
    assertSpecificity(OIOIIO.class, 6);
    assertSpecificity(OIOOIIO.class, 7);
  }

  @Test
  public void testLineage() {
    assertLineage(null);
    assertLineage(Object.class, Object.class);
    assertLineage(Serializable.class, Serializable.class);
    assertLineage(String.class, String.class, Serializable.class, Comparable.class, CharSequence.class, Object.class);
    assertLineage(O.class, O.class, Object.class);
    assertLineage(OO.class, OO.class, O.class, Object.class);
    assertLineage(I.class, I.class);
    assertLineage(II.class, II.class, I.class);
    assertLineage(OIO.class, OIO.class, O.class, I.class, Object.class);
    assertLineage(OIOO.class, OIOO.class, OIO.class, O.class, I.class, Object.class);
    assertLineage(OIOOIO.class, OIOOIO.class, OIOO.class, OIO.class, O.class, I.class, Object.class);
    assertLineage(OIOIIO.class, OIOIIO.class, OIO.class, II.class, O.class, I.class, Object.class);
    assertLineage(OIOOIIO.class, OIOOIIO.class, OIOO.class, OIO.class, II.class, O.class, I.class, Object.class);
  }

  private void assertSpecificity(final Class<?> clazz, final int expected) {
    assertEquals(clazz != null ? clazz.getSimpleName() : "null" + " is " + expected, expected, getSpecificity(clazz));
  }
  private void assertLineage(final Class<?> clazz, final Class<?>... expected) {
    assertArrayEquals(clazz != null ? clazz.getSimpleName() : "null" + " is " + Arrays.toString(expected), expected, getLineage(clazz));
  }
}
