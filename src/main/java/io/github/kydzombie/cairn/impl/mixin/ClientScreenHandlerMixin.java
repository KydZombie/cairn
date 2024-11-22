package io.github.kydzombie.cairn.impl.mixin;

import io.github.kydzombie.cairn.Cairn;
import io.github.kydzombie.cairn.api.gui.SyncField;
import io.github.kydzombie.cairn.api.gui.SyncGetter;
import io.github.kydzombie.cairn.api.gui.SyncSetter;
import io.github.kydzombie.cairn.api.gui.SyncableScreenHandler;
import io.github.kydzombie.cairn.impl.CairnImplConstants;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

@Mixin(ScreenHandler.class)
public class ClientScreenHandlerMixin {
    @Inject(method = "setProperty(II)V", at = @At("HEAD"))
    private void cairn_setSyncedData(int id, int value, CallbackInfo ci) {
        if (!(this instanceof SyncableScreenHandler)) return;
        BlockEntity syncEntity = ((SyncableScreenHandler) this).getSyncedBlockEntity();
        ScreenHandler handler = (ScreenHandler) (Object) this;

        ArrayList<String> handlerProperties = new ArrayList<>();

        for (Field field : this.getClass().getFields()) {
            if (!field.isAnnotationPresent(SyncField.class)) continue;
            if (field.getType() != int.class && field.getType() != Integer.class) {
                throw new RuntimeException("Field annotated with @SyncField must be an int");
            }
            SyncField syncField = field.getAnnotation(SyncField.class);
            handlerProperties.add(syncField.value());
        }

        for (Method method : this.getClass().getMethods()) {
            if (!method.isAnnotationPresent(SyncGetter.class)) continue;
            if (method.getReturnType() != int.class && method.getReturnType() != Integer.class) {
                throw new RuntimeException("Method annotated with @SyncGetter must return an int");
            }
            SyncGetter syncGetter = method.getAnnotation(SyncGetter.class);
            handlerProperties.add(syncGetter.value());
        }

        if (id - CairnImplConstants.CAIRN_SCREEN_HANDLER_START_ID > handlerProperties.size()) {
            Cairn.LOGGER.error("Received invalid property update for screen handler {}. " +
                    "Cairn's id was {} but there are only {} properties",
                    handler.getClass().getName(), id -  - CairnImplConstants.CAIRN_SCREEN_HANDLER_START_ID, handlerProperties.size()
            );
        }

        HashMap<String, Object> entitySetters = new HashMap<>();
        for (Field field : syncEntity.getClass().getFields()) {
            if (!field.isAnnotationPresent(SyncField.class)) continue;
            if (field.getType() != int.class && field.getType() != Integer.class) {
                throw new RuntimeException("Field annotated with @SyncField must be an int");
            }
            SyncField syncField = field.getAnnotation(SyncField.class);

            field.setAccessible(true);
            entitySetters.put(syncField.value(), field);
        }

        for (Method method : syncEntity.getClass().getMethods()) {
            if (!method.isAnnotationPresent(SyncSetter.class)) continue;
            if (method.getReturnType() != void.class) {
                throw new RuntimeException("Method annotated with @SyncSetter must return void");
            }
            SyncSetter syncSetter = method.getAnnotation(SyncSetter.class);
            method.setAccessible(true);
            entitySetters.put(syncSetter.value(), method);
        }

        if (handlerProperties.size() != entitySetters.size()) {
            throw new RuntimeException("Mismatched number of properties between " +
                    this.getClass().getSimpleName() + " and " + syncEntity.getClass().getSimpleName());
        }

        Object setter = entitySetters.get(handlerProperties.get(id - CairnImplConstants.CAIRN_SCREEN_HANDLER_START_ID));
        if (setter instanceof Field) {
            try {
                ((Field) setter).set(syncEntity, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else if (setter instanceof Method) {
            try {
                ((Method) setter).invoke(syncEntity, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
