package io.github.kydzombie.cairn.impl.mixin;

import io.github.kydzombie.cairn.api.block.entity.NbtSerializable;
import io.github.kydzombie.cairn.api.block.entity.SaveToNbt;
import io.github.kydzombie.cairn.api.block.entity.UpdatePacketReceiver;
import io.github.kydzombie.cairn.api.packet.BlockEntityUpdatePacket;
import io.github.kydzombie.cairn.api.storage.HasItemStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {
    @Shadow
    public int x;
    @Shadow
    public int y;
    @Shadow
    public int z;

    @Shadow public abstract void readNbt(NbtCompound nbt);

    @Inject(method = "readNbt", at = @At("RETURN"))
    private void cairn_injectReadNbt(NbtCompound nbt, CallbackInfo ci) {
        Class<?> clazz = getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                var annotation = field.getAnnotation(SaveToNbt.class);
                if (annotation != null) {
                    String key = annotation.value();
                    if (key.isEmpty()) {
                        key = field.getName();
                    }
                    try {
                        field.setAccessible(true);
                        Object value = field.get(this);
                        if (value instanceof NbtSerializable nbtSerializable) {
                            NbtElement nbtElement = (NbtElement) nbt.entries.get(key);
                            if (nbtElement != null) {
                                nbtSerializable.readNbt(nbtElement);
                            }
                        } else if (value instanceof Integer) {
                            field.set(this, nbt.getInt(key));
                        } else if (value instanceof Boolean) {
                            field.set(this, nbt.getBoolean(key));
                        } else if (value instanceof String) {
                            field.set(this, nbt.getString(key));
                        } else if (value instanceof Long) {
                            field.set(this, nbt.getLong(key));
                        } else if (value instanceof Float) {
                            field.set(this, nbt.getFloat(key));
                        } else if (value instanceof Double) {
                            field.set(this, nbt.getDouble(key));
                        } else if (value instanceof Byte) {
                            field.set(this, nbt.getByte(key));
                        } else if (value instanceof Short) {
                            field.set(this, nbt.getShort(key));
                        } else if (value instanceof byte[]) {
                            field.set(this, nbt.getByteArray(key));
                        } else if (value instanceof int[]) {
                            field.set(this, nbt.getIntArray(key));
                        } else if (value instanceof long[]) {
                            field.set(this, nbt.getLongArray(key));
                        } else if (field.getType().isEnum()) {
                            field.set(this, Enum.valueOf((Class<Enum>) field.getType(), nbt.getString(key)));
                        } else {
                            throw new RuntimeException("Unsupported type for @SaveToNbt: " + value.getClass() + ", consider implementing NbtSerializable");
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void cairn_injectWriteNbt(NbtCompound nbt, CallbackInfo ci) {
        Class<?> clazz = getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                var annotation = field.getAnnotation(SaveToNbt.class);
                if (annotation != null) {
                    String key = annotation.value();
                    if (key.isEmpty()) {
                        key = field.getName();
                    }
                    try {
                        field.setAccessible(true);
                        Object value = field.get(this);
                        if (value instanceof NbtSerializable nbtSerializable) {
                            nbt.put(key, nbtSerializable.writeNbt());
                        } else if (value instanceof Integer) {
                            nbt.putInt(key, (int) field.get(this));
                        } else if (value instanceof Boolean) {
                            nbt.putBoolean(key, (boolean) field.get(this));
                        } else if (value instanceof String) {
                            nbt.putString(key, (String) field.get(this));
                        } else if (value instanceof Long) {
                            nbt.putLong(key, (long) field.get(this));
                        } else if (value instanceof Float) {
                            nbt.putFloat(key, (float) field.get(this));
                        } else if (value instanceof Double) {
                            nbt.putDouble(key, (double) field.get(this));
                        } else if (value instanceof Byte) {
                            nbt.putByte(key, (byte) field.get(this));
                        } else if (value instanceof Short) {
                            nbt.putShort(key, (short) field.get(this));
                        } else if (value instanceof byte[]) {
                            nbt.putByteArray(key, (byte[]) field.get(this));
                        } else if (value instanceof int[]) {
                            nbt.put(key, (int[]) field.get(this));
                        } else if (value instanceof long[]) {
                            nbt.put(key, (long[]) field.get(this));
                        } else if (field.getType().isEnum()) {
                            nbt.putString(key, ((Enum<?>) value).name());
                        } else {
                            throw new RuntimeException("Unsupported type for @SaveToNbt: " + value.getClass() + ", consider implementing NbtSerializable");
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    @Environment(EnvType.SERVER)
    @Inject(method = "createUpdatePacket", at = @At("HEAD"), cancellable = true)
    private void cairn_customUpdatePacket(CallbackInfoReturnable<Packet> cir) {
        if (this instanceof UpdatePacketReceiver<?> receiver) {
            cir.setReturnValue(new BlockEntityUpdatePacket<>(x, y, z, receiver.createUpdateData()));
        }
    }
}
