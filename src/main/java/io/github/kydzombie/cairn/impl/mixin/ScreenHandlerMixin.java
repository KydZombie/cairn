package io.github.kydzombie.cairn.impl.mixin;

import io.github.kydzombie.cairn.api.gui.*;
import io.github.kydzombie.cairn.impl.CairnImplConstants;
import io.github.kydzombie.cairn.impl.storage.DirectPropertyAccessor;
import io.github.kydzombie.cairn.impl.storage.IndirectPropertyAccessor;
import io.github.kydzombie.cairn.impl.storage.PropertyAccessor;
import io.github.kydzombie.cairn.impl.storage.PropertyInfo;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    @Unique
    private static final Map<Class<?>, PropertyInfo> cairn_PROPERTY_INFO_MAP = new HashMap<>();

    @SuppressWarnings("rawtypes")
    @Shadow
    protected List listeners;

    @Unique
    private static @Nullable BlockEntity cairn_getSyncedBlockEntity(ScreenHandler handler) {
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

    @Unique
    private static TreeMap<String, PropertyAccessor> cairn_getAccessors(Class<?> clazz) {
        TreeMap<String, Field> fields = new TreeMap<>();
        TreeMap<String, Method> getterMethods = new TreeMap<>();
        TreeMap<String, Method> setterMethods = new TreeMap<>();

        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(SyncField.class)) continue;
                if (field.getType() != int.class && field.getType() != Integer.class) {
                    throw new RuntimeException("Field annotated with @SyncField must be an int");
                }

                String propertyName = field.getAnnotation(SyncField.class).value();
                if (fields.containsKey(propertyName)) {
                    if (fields.get(propertyName).getDeclaringClass() == field.getDeclaringClass()) {
                        throw new RuntimeException("Cannot have more than one field for a property");
                    }
                    continue;
                }

                field.setAccessible(true);
                fields.put(field.getAnnotation(SyncField.class).value(), field);
            }

            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(SyncGetter.class)) {
                    if (method.getReturnType() != int.class && method.getReturnType() != Integer.class) {
                        throw new RuntimeException("Method annotated with @SyncGetter must return an int");
                    }
                    String propertyName = method.getAnnotation(SyncGetter.class).value();
                    if (getterMethods.containsKey(propertyName)) {
                        if (getterMethods.get(propertyName).getDeclaringClass() == method.getDeclaringClass()) {
                            throw new RuntimeException("Cannot have more than one getter for a property");
                        }
                        continue;
                    }

                    method.setAccessible(true);
                    getterMethods.put(propertyName, method);
                } else if (method.isAnnotationPresent(SyncSetter.class)) {
                    if (method.getReturnType() != void.class) {
                        throw new RuntimeException("Method annotated with @SyncSetter must return void");
                    }
                    String propertyName = method.getAnnotation(SyncSetter.class).value();

                    if (setterMethods.containsKey(propertyName)) {
                        if (setterMethods.get(propertyName).getDeclaringClass() == method.getDeclaringClass()) {
                            throw new RuntimeException("Cannot have more than one setter for a property");
                        }
                        continue;
                    }

                    method.setAccessible(true);
                    setterMethods.put(propertyName, method);
                }
            }

            clazz = clazz.getSuperclass();
        }

        TreeMap<String, PropertyAccessor> accessors = new TreeMap<>();

        fields.forEach((name, field) -> {
            if (getterMethods.containsKey(name) || setterMethods.containsKey(name)) {
                throw new RuntimeException("Cannot have both a field and a getter/setter for a property");
            }
            accessors.put(name, new DirectPropertyAccessor(field));
        });

        Set<String> getterKeys = getterMethods.keySet();
        Set<String> setterKeys = setterMethods.keySet();
        Set<String> differences = new HashSet<>(getterKeys);
        differences.addAll(setterKeys);
        differences.retainAll(setterKeys);
        differences.removeAll(getterKeys);

        if (!differences.isEmpty()) {
            throw new RuntimeException("Mismatched getter/setter for properties: " + differences);
        }

        getterMethods.forEach((name, getter) -> accessors.put(name, new IndirectPropertyAccessor(getter, setterMethods.get(name))));

        return accessors;
    }

    @Unique
    private PropertyInfo cairn_getPropertyInfo() {
        if (!getClass().isAnnotationPresent(Syncable.class)) return null;

        if (cairn_PROPERTY_INFO_MAP.containsKey(getClass())) {
            return cairn_PROPERTY_INFO_MAP.get(getClass());
        }

        TreeMap<String, PropertyAccessor> handlerAccessors = cairn_getAccessors(getClass());
        BlockEntity syncEntity = cairn_getSyncedBlockEntity((ScreenHandler) (Object) this);
        if (syncEntity == null) {
            throw new RuntimeException(this.getClass().getSimpleName() + " must annotate a BlockEntity with @SyncedBlockEntity");
        }
        TreeMap<String, PropertyAccessor> blockEntityAccessors = cairn_getAccessors(syncEntity.getClass());
        TreeMap<String, Pair<PropertyAccessor, PropertyAccessor>> accessors = new TreeMap<>();
        handlerAccessors.forEach((name, handlerAccessor) -> {
            if (!blockEntityAccessors.containsKey(name)) {
                throw new RuntimeException("Property " + name + " in " + this.getClass().getSimpleName() + " does not exist in " + syncEntity.getClass().getSimpleName());
            }
            accessors.put(name, Pair.of(handlerAccessor, blockEntityAccessors.get(name)));
        });

        cairn_PROPERTY_INFO_MAP.put(getClass(), new PropertyInfo(accessors));
        return cairn_PROPERTY_INFO_MAP.get(getClass());
    }

    @Inject(method = "sendContentUpdates()V", at = @At("RETURN"))
    private void cairn_sendSyncedData(CallbackInfo ci) {
        PropertyInfo propertyInfo = cairn_getPropertyInfo();
        if (propertyInfo == null) return;

        ScreenHandler handler = (ScreenHandler) (Object) this;
        BlockEntity syncEntity = cairn_getSyncedBlockEntity(handler);

        for (int i = 0; i < propertyInfo.accessors().keySet().size(); i++) {
            String propertyName = propertyInfo.properties()[i];
            int handlerValue = propertyInfo.get(propertyName, handler);
            int entityValue = propertyInfo.get(propertyName, syncEntity);
            //noinspection unchecked
            for (ScreenHandlerListener listener : (List<ScreenHandlerListener>) listeners) {
                if (handlerValue != entityValue) {
                    System.out.println("Sending update to property " + propertyName + " to " + entityValue);
                    listener.onPropertyUpdate(handler, CairnImplConstants.CAIRN_SCREEN_HANDLER_START_ID + i, entityValue);
                }
            }

            System.out.println("Updating property " + propertyName + " locally");
            propertyInfo.set(propertyName, handler, entityValue);
        }
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "setProperty(II)V", at = @At("HEAD"))
    private void cairn_setSyncedData(int id, int value, CallbackInfo ci) {
        PropertyInfo propertyInfo = cairn_getPropertyInfo();
        if (propertyInfo == null) return;

        ScreenHandler handler = (ScreenHandler) (Object) this;
        BlockEntity syncEntity = cairn_getSyncedBlockEntity(handler);

        String property = propertyInfo.properties()[id - CairnImplConstants.CAIRN_SCREEN_HANDLER_START_ID];
        propertyInfo.set(property, syncEntity, value);
    }

    @Environment(EnvType.SERVER)
    @Inject(method = "addListener(Lnet/minecraft/screen/ScreenHandlerListener;)V", at = @At("RETURN"))
    private void cairn_addListeners(ScreenHandlerListener listener, CallbackInfo ci) {
        PropertyInfo propertyInfo = cairn_getPropertyInfo();
        if (propertyInfo == null) return;

        ScreenHandler handler = (ScreenHandler) (Object) this;
        BlockEntity syncEntity = cairn_getSyncedBlockEntity(handler);

        String[] properties = propertyInfo.properties();
        for (int i = 0; i < properties.length; i++) {
            String property = properties[i];
            int value = propertyInfo.get(property, syncEntity);
            listener.onPropertyUpdate(handler, CairnImplConstants.CAIRN_SCREEN_HANDLER_START_ID + i, value);
        }
    }
}
