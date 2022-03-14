package deserialization_cache_bug;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ObjectValidation {
	
	protected ObjectValidation() {}

	public static void validateFieldValueTypesForObject(Object obj) throws Exception {
		try {
			validateFieldValueTypesForObject(obj, Collections.newSetFromMap(new IdentityHashMap<>()));
		} catch (Throwable t) {
			throw new Exception("Invalid Object Identified", t);
		}
	}
	
	private static void validateFieldValueTypesForObject(Object obj, Set<Object> previouslyValidatedObjects) throws IllegalArgumentException, IllegalAccessException, Exception {
		if(obj == null) {
			return;
		}
		
		previouslyValidatedObjects.add(obj);
		
		if(obj.getClass().isArray()) {
			handleArray(obj, previouslyValidatedObjects);
		} else if(obj instanceof Iterable) { 
			handleIterable((Iterable<?>)obj, previouslyValidatedObjects);
		} else if(obj instanceof Map<?, ?>) {
			handleMap((Map<?, ?>)obj, previouslyValidatedObjects);
		} else {
			handleObject(obj, previouslyValidatedObjects, true);
		}
	}

	private static void handleArray(Object objArray, Set<Object> previouslyValidatedObjects) throws IllegalArgumentException, IllegalAccessException, Exception {
		for(int i = 0; i < Array.getLength(objArray); i++) {
			Object obj = Array.get(objArray, i);
			if(obj != null && !previouslyValidatedObjects.contains(obj)) {
				validateFieldValueTypesForObject(obj, previouslyValidatedObjects);
			}
		}
	}

	private static void handleIterable(Iterable<?> iterable, Set<Object> previouslyValidatedObjects) throws IllegalArgumentException, IllegalAccessException, Exception {
		Iterator<?> it = iterable.iterator();
		while(it.hasNext()) {
			Object obj = it.next();
			if(obj != null && !previouslyValidatedObjects.contains(obj)) {
				validateFieldValueTypesForObject(obj, previouslyValidatedObjects);
			}
		}
		handleObject(iterable, previouslyValidatedObjects, false); //Make sure that primitives are valid
	}

	private static void handleMap(Map<?,?> map, Set<Object> previouslyValidatedObjects) throws IllegalArgumentException, IllegalAccessException, Exception {
		Iterator<?> it = map.entrySet().iterator();
		while(it.hasNext()) {
			Entry<?, ?> entry = (Entry<?, ?>) it.next();
			if(entry != null) {
				if(entry.getKey() != null && !previouslyValidatedObjects.contains(entry.getKey())) {
					validateFieldValueTypesForObject(entry.getKey(), previouslyValidatedObjects);
				}
				if(entry.getValue() != null && !previouslyValidatedObjects.contains(entry.getValue())) {
					validateFieldValueTypesForObject(entry.getValue(), previouslyValidatedObjects);
				}
			}
		}
		handleObject(map, previouslyValidatedObjects, false); //Make sure that primitives are valid
	}

	private static void handleObject(Object obj, Set<Object> previouslyValidatedObjects, boolean delveIntoObjects) throws IllegalArgumentException, IllegalAccessException, Exception {
		Class<?> currentClass = obj.getClass();
		while(currentClass != Object.class && currentClass != null) {
			Field[] fields = currentClass.getDeclaredFields();
			if(fields != null) {
				for(Field field : fields) {
					if(!Modifier.isStatic(field.getModifiers())) {
						field.setAccessible(true);
						Object fieldValue = field.get(obj);
						if(fieldValue != null) {
							validateFieldValueType(field, fieldValue);
							
							if(delveIntoObjects && shouldDelveIntoObject(field, fieldValue, previouslyValidatedObjects)) {
								validateFieldValueTypesForObject(fieldValue, previouslyValidatedObjects);
							}
						}
					}
				}
			}
			currentClass = currentClass.getSuperclass();
		}
	}

	protected static void validateFieldValueType(Field field, Object value) throws Exception {
		Class<?> fieldType = field.getType();
		Class<? extends Object> valueType = value.getClass();
		
		if(fieldType.equals(boolean.class) && valueType.equals(Boolean.class)) {
			return;
		}
		if(fieldType.equals(int.class) && valueType.equals(Integer.class)) {
			return;
		}
		if(fieldType.equals(long.class) && valueType.equals(Long.class)) {
			return;
		}
		if(fieldType.equals(float.class) && valueType.equals(Float.class)) {
			return;
		}
		if(fieldType.equals(double.class) && valueType.equals(Double.class)) {
			return;
		}
		if(fieldType.equals(byte.class) && valueType.equals(Byte.class)) {
			return;
		}
		if(fieldType.equals(char.class) && valueType.equals(Character.class)) {
			return;
		}
		if(fieldType.equals(short.class) && valueType.equals(Short.class)) {
			return;
		}
		if(!fieldType.isAssignableFrom(valueType)) {
			throw new Exception("Field " + field + " cannot hold a value of type " + valueType);
		}
	}

	private static boolean shouldDelveIntoObject(Field field, Object fieldValue, Set<Object> previouslyValidatedObjects) {
		return 	
			!field.getType().isPrimitive() && 
			field.getType() != String.class && 
			!field.getType().isEnum() &&
			isNotEmptyCollection(fieldValue) &&
			!isByteArray(field) &&
			!previouslyValidatedObjects.contains(fieldValue);
	}

	private static boolean isNotEmptyCollection(Object obj) {
		if(obj instanceof Collection<?>) {
			return !((Collection<?>)obj).isEmpty();
		}
		return true;
	}

	private static boolean isByteArray(Field field) {
		return field.getType().equals(byte[].class) || field.getType().equals(Byte[].class);
	}
}
