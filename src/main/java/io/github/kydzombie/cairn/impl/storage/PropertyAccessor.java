package io.github.kydzombie.cairn.impl.storage;

public interface PropertyAccessor {
    int get(Object instance);
    void set(Object instance, int value);
}
