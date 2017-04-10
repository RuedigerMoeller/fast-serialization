package org.nustaq.serialization;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents a ordered class lineage based on the specificity of classes, where specificity is defined as follows:
 * <nl>
 * <li>null has specificity 0</li>
 * <li>java.lang.Object has specificity 0</li>
 * <li>an interface without any extends clause has specificity 1</li>
 * <li>a class or interface has a specificity of 1 + the specificity of the superclass + the sum of the specificity of the implemented interfaces.</li>
 * </nl>
 * @author Odd Moeller 2017-03-08.
 */
public final class FSTClazzLineageInfo {
  private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
  private static final LineageInfo OBJECT_LINEAGE_INFO = new LineageInfo(new LinkedHashSet<Class<?>>(Collections.singletonList(Object.class)), 0);
  private static final ConcurrentMap<Class<?>, LineageInfo> lineageInfos = new ConcurrentHashMap<Class<?>, LineageInfo>();
  public static final Comparator<Class> SPECIFICITY_CLASS_COMPARATOR = new Comparator<Class>() {
    @Override
    public int compare(final Class c1, final Class c2) {
      return getLineageInfo(c2).specificity - getLineageInfo(c1).specificity;
    }
  };

  private FSTClazzLineageInfo() {}

  /**
   * Returns the specificity of the specified class as defined above.
   */
  public static int getSpecificity(final Class<?> clazz) {
    if (clazz == null) return 0;
    final LineageInfo lineageInfo = FSTClazzLineageInfo.getLineageInfo(clazz);
    return lineageInfo == null ? 0 : lineageInfo.specificity;
  }

  /**
   * Returns the lineage of the specified class ordered by specificity (the class itself is at position 0 since it is most specific in its lineage).
   */
  public static Class<?>[] getLineage(final Class<?> clazz) {
    final LineageInfo lineageInfo = getLineageInfo(clazz);
    return lineageInfo == null ? EMPTY_CLASS_ARRAY : lineageInfo.lineage.toArray(new Class<?>[lineageInfo.lineage.size()]);
  }

  private static LineageInfo getLineageInfo(final Class<?> clazz) {
    if (clazz == null) return null;
    else if (clazz.equals(Object.class)) return OBJECT_LINEAGE_INFO;
    final LineageInfo lineageInfo = lineageInfos.get(clazz);
    if (lineageInfo != null) {
      return lineageInfo;
    }

    int specificity = 1;
    final LinkedHashSet<Class<?>> ancestors = new LinkedHashSet<Class<?>>();
    final Class<?> sc = getSuperclass(clazz);
    final LineageInfo sl = getLineageInfo(sc);
    if (sl != null) {
      ancestors.addAll(sl.lineage);
      specificity += sl.specificity;
    }
    for (final Class<?> i : getInterfaces(clazz)) {
      final LineageInfo il = getLineageInfo(i);
      if (il != null) {
        ancestors.removeAll(il.lineage);
        ancestors.addAll(il.lineage);
        specificity += il.specificity;
      }
    }
    final Class<?>[] array = ancestors.toArray(new Class<?>[ancestors.size()]);
    Arrays.sort(array, SPECIFICITY_CLASS_COMPARATOR);
    final LinkedHashSet<Class<?>> lineage = new LinkedHashSet<Class<?>>(array.length + 1);
    lineage.add(clazz);
    Collections.addAll(lineage, array);
    final LineageInfo result = new LineageInfo(lineage, specificity);
    lineageInfos.putIfAbsent(clazz, result);
    return result;
  }

  private static Class<?> getSuperclass(final Class<?> c) {
    if (c == null) return null;
    return c.getSuperclass();
  }

  private static Class<?>[] getInterfaces(final Class<?> c) {
    if (c == null) return null;
    return c.getInterfaces();
  }

  private static final class LineageInfo {
    private final LinkedHashSet<Class<?>> lineage;
    private final int specificity;
    private LineageInfo(final LinkedHashSet<Class<?>> lineage, final int specificity) {
      this.lineage = lineage;
      this.specificity = specificity;
    }
  }
}
