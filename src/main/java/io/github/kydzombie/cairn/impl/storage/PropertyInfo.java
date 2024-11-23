package io.github.kydzombie.cairn.impl.storage;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.screen.ScreenHandler;

import java.util.TreeMap;

public record PropertyInfo(TreeMap<String, Pair<PropertyAccessor, PropertyAccessor>> accessors) {
    public String[] properties() {
        return accessors.keySet().toArray(String[]::new);
    }

    public int get(String name, ScreenHandler handler) {
        return accessors.get(name).left().get(handler);
    }

    public void set(String name, ScreenHandler handler, int value) {
        accessors.get(name).left().set(handler, value);
    }

    public int get(String name, Object blockEntity) {
        return accessors.get(name).right().get(blockEntity);
    }

    public void set(String name, Object blockEntity, int value) {
        accessors.get(name).right().set(blockEntity, value);
    }
}
