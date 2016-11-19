package tickoptimizer.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.bukkit.Bukkit;

public class Utils {

	public static <T extends AccessibleObject> T setAccessible(T object) {
		object.setAccessible(true);
		return object;
	}

	public static void setFinalField(Field field, Object obj, Object newValue) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		setAccessible(Field.class.getDeclaredField("modifiers")).setInt(field, field.getModifiers() & ~Modifier.FINAL);
		setAccessible(Field.class.getDeclaredField("root")).set(field, null);
		setAccessible(Field.class.getDeclaredField("overrideFieldAccessor")).set(field, null);
		setAccessible(field).set(obj, newValue);
	}

	public static MethodHandle getFieldSetter(Class<?> classIn, String fieldName, Class<?> newFieldClass) {
		try {
			return MethodHandles
					.lookup()
					.unreflectSetter(setAccessible(classIn.getDeclaredField(fieldName)))
					.asType(MethodType.methodType(void.class, classIn, newFieldClass));
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <V> V getFieldValue(Object obj, Class<?> classIn, String fieldName) {
		try {
			return (V) setAccessible(classIn.getDeclaredField(fieldName)).get(obj);
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
		return null;
	}

	public static void setFieldValue(Object obj, String name, Object newValue) throws IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = obj.getClass();
		do {
			for (Field field : clazz.getDeclaredFields()) {
				if (field.getName().equals(name)) {
					setAccessible(field).set(obj, newValue);
					return;
				}
			}
		} while ((clazz = clazz.getSuperclass()) != null);
		throw new RuntimeException("Can't find field "+name);
	}

}
