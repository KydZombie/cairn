package io.github.kydzombie.cairn.impl.mixin;

import io.github.kydzombie.cairn.api.block.entity.SaveToNbt;
import io.github.kydzombie.cairn.api.block.entity.UpdatePacketReceiver;
import io.github.kydzombie.cairn.api.packet.BlockEntityUpdatePacket;
import io.github.kydzombie.cairn.api.storage.HasItemStorage;
import io.github.kydzombie.cairn.api.storage.ItemStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {
    @Shadow
    public int x;
    @Shadow
    public int y;
    @Shadow
    public int z;

    @Inject(method = "readNbt", at = @At("RETURN"))
    private void cairn_injectReadNbt(NbtCompound nbt, CallbackInfo ci) {
        if (!(this instanceof HasItemStorage)) return;
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
                        if (value instanceof ItemStorage) {
                            ((ItemStorage) value).readNbt(nbt, key);
                        } else if (value instanceof Integer) {
                            field.setAccessible(true);
                            field.set(this, nbt.getInt(key));
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
        if (!(this instanceof HasItemStorage)) return;
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
                        if (value instanceof ItemStorage) {
                            ((ItemStorage) value).writeNbt(nbt, key);
                        } else if (value instanceof Integer) {
                            field.setAccessible(true);
                            nbt.putInt(key, (int) field.get(this));
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
