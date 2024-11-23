package io.github.kydzombie.cairn.impl.mixin;

import io.github.kydzombie.cairn.api.gui.*;
import io.github.kydzombie.cairn.impl.CairnImplConstants;
import io.github.kydzombie.cairn.impl.util.SyncUtil;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    @Shadow protected List listeners;

    @Inject(method = "sendContentUpdates()V", at = @At("RETURN"))
    private void cairn_sendSyncedData(CallbackInfo ci) {
        if (!this.getClass().isAnnotationPresent(Syncable.class)) return;
        ScreenHandler handler = (ScreenHandler) (Object) this;
        BlockEntity syncEntity = SyncUtil.getSyncedBlockEntity(handler);

        ArrayList<Triple<String, Object, Integer>> handlerProperties = new ArrayList<>();

        for (Field field : this.getClass().getFields()) {
            if (!field.isAnnotationPresent(SyncField.class)) continue;
            if (field.getType() != int.class && field.getType() != Integer.class) {
                throw new RuntimeException("Field annotated with @SyncField must be an int");
            }
            SyncField syncField = field.getAnnotation(SyncField.class);
            try {
                field.setAccessible(true);
                handlerProperties.add(Triple.of(syncField.value(), field, field.getInt(this)));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        for (Method method : this.getClass().getMethods()) {
            if (!method.isAnnotationPresent(SyncGetter.class)) continue;
            if (method.getReturnType() != int.class && method.getReturnType() != Integer.class) {
                throw new RuntimeException("Method annotated with @SyncGetter must return an int");
            }
            SyncGetter syncGetter = method.getAnnotation(SyncGetter.class);
            Method setterMethod = null;
            for (Method method2 : this.getClass().getMethods()) {
                if (!method2.isAnnotationPresent(SyncSetter.class)) continue;
                if (setterMethod != null) {
                    throw new RuntimeException("Cannot have more than one setter for a property");
                }
                if (method2.getReturnType() != void.class) {
                    throw new RuntimeException("Setter must return void");
                }
                setterMethod = method2;
            }

            if (setterMethod == null) {
                throw new RuntimeException("No setter found for non-field property " + syncGetter.value());
            }

            try {
                method.setAccessible(true);
                handlerProperties.add(Triple.of(syncGetter.value(), setterMethod, (int) method.invoke(this)));
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

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
            Triple<String, Object, Integer> handlerProperty = handlerProperties.get(i);
            String propertyName = handlerProperty.getLeft();
            Object setter = handlerProperty.getMiddle();
            int handlerValue = handlerProperty.getRight();
            int entityValue = entityProperties.get(propertyName);
            //noinspection unchecked
            for (ScreenHandlerListener listener : (List<ScreenHandlerListener>) listeners) {
                if (handlerValue != entityValue) {
                    System.out.println("Sending update to property " + propertyName + " to " + entityValue);
                    listener.onPropertyUpdate(handler, CairnImplConstants.CAIRN_SCREEN_HANDLER_START_ID + i, entityValue);
                }
            }

            System.out.println("Updating property " + propertyName + " locally");

            if (setter instanceof Field) {
                try {
                    ((Field) setter).set(this, entityValue);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    ((Method) setter).invoke(this, entityValue);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
