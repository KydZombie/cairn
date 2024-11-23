package io.github.kydzombie.cairn.impl.storage;

import java.lang.reflect.Field;

public record DirectPropertyAccessor(Field field) implements PropertyAccessor {
    @Override
    public int get(Object instance) {
        try {
            return field.getInt(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void set(Object instance, int value) {
        try {
            field.setInt(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
