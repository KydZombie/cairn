package io.github.kydzombie.cairn.impl.mixin;

import io.github.kydzombie.cairn.api.gui.SyncField;
import io.github.kydzombie.cairn.api.gui.SyncGetter;
import io.github.kydzombie.cairn.api.gui.Syncable;
import io.github.kydzombie.cairn.impl.CairnImplConstants;
import io.github.kydzombie.cairn.impl.util.SyncUtil;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

@Mixin(ScreenHandler.class)
public class ServerScreenHandlerMixin {
    @Inject(method = "addListener(Lnet/minecraft/screen/ScreenHandlerListener;)V", at = @At("RETURN"))
    private void cairn_addListeners(ScreenHandlerListener listener, CallbackInfo ci) {
        extracted(listener);
    }

    private void extracted(ScreenHandlerListener listener) {
        if (!this.getClass().isAnnotationPresent(Syncable.class)) return;
        ScreenHandler handler = (ScreenHandler) (Object) this;
        BlockEntity syncEntity = SyncUtil.getSyncedBlockEntity(handler);

        if (syncEntity == null) {
            throw new RuntimeException("SyncableScreenHandler must return a non-null BlockEntity from getSyncedBlockEntity");
        }

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

        HashMap<String, Integer> entityProperties = new HashMap<>();
        for (Field field : syncEntity.getClass().getFields()) {
            if (!field.isAnnotationPresent(SyncField.class)) continue;
            if (field.getType() != int.class && field.getType() != Integer.class) {
                throw new RuntimeException("Field annotated with @SyncField must be an int");
            }
            SyncField syncField = field.getAnnotation(SyncField.class);
            try {
                field.setAccessible(true);
                entityProperties.put(syncField.value(), field.getInt(syncEntity));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        for (Method method : syncEntity.getClass().getMethods()) {
            if (!method.isAnnotationPresent(SyncGetter.class)) continue;
            if (method.getReturnType() != int.class && method.getReturnType() != Integer.class) {
                throw new RuntimeException("Method annotated with @SyncGetter must return an int");
            }
            SyncGetter syncGetter = method.getAnnotation(SyncGetter.class);
            try {
                method.setAccessible(true);
                entityProperties.put(syncGetter.value(), (int) method.invoke(syncEntity));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (handlerProperties.size() != entityProperties.size()) {
            throw new RuntimeException("Mismatched number of properties between " +
                    this.getClass().getSimpleName() + " and " + syncEntity.getClass().getSimpleName());
        }

        for (int i = 0; i < handlerProperties.size(); i++) {
            String propertyName = handlerProperties.get(i);
            int value = entityProperties.get(propertyName);
            System.out.println("Setting property " + propertyName + " to " + value);
            listener.onPropertyUpdate(handler, CairnImplConstants.CAIRN_SCREEN_HANDLER_START_ID + i, value);
        }
    }
}
