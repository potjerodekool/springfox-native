package org.platonos.test;

import java.lang.reflect.Field;

public class ReflectionTestUtils {

    public static void setField(final Object target,
                                final String fieldName,
                                final Object value) {
        Field field = null;
        boolean canAccess = false;

        try {
            field = target.getClass().getDeclaredField(fieldName);
            canAccess = field.canAccess(target);
            field.trySetAccessible();
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            if (field != null) {
                field.setAccessible(canAccess);
            }
        }
    }
}
