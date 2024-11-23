package io.github.kydzombie.cairn.impl.util;

import io.github.kydzombie.cairn.api.gui.SyncedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class SyncUtil {
    public static @Nullable BlockEntity getSyncedBlockEntity(ScreenHandler handler) {
        Class<?> currentClass = handler.getClass();
        while (currentClass != null) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(SyncedBlockEntity.class)) {
                    try {
                        field.setAccessible(true);
                        return (BlockEntity) field.get(handler);
                    } catch (ClassCastException e) {
                        throw new RuntimeException("Field annotated with @SyncedBlockEntity must be of type BlockEntity");
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        return null;
    }
}
