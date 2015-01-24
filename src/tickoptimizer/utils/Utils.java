package tickoptimizer.utils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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

}
