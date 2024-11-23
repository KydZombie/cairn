package io.github.kydzombie.cairn.impl.storage;

import java.lang.reflect.Method;

public record IndirectPropertyAccessor(Method getter, Method setter) implements PropertyAccessor {
    @Override
    public int get(Object instance) {
        try {
            return (int) getter.invoke(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void set(Object instance, int value) {
        try {
            setter.invoke(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
