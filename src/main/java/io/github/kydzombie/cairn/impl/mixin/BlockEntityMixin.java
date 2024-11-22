package io.github.kydzombie.cairn.impl.mixin;

import io.github.kydzombie.cairn.api.storage.AutoNbt;
import io.github.kydzombie.cairn.api.storage.HasItemStorage;
import io.github.kydzombie.cairn.api.storage.ItemStorage;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {
    @Inject(method = "readNbt", at = @At("RETURN"))
    private void cairn_injectReadNbt(NbtCompound nbt, CallbackInfo ci) {
        if (!(this instanceof HasItemStorage)) return;
        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(AutoNbt.class)) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(this);
                    if (value instanceof ItemStorage) {
                        ((ItemStorage) value).readNbt(nbt);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void cairn_injectWriteNbt(NbtCompound nbt, CallbackInfo ci) {
        if (!(this instanceof HasItemStorage)) return;
        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(AutoNbt.class)) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(this);
                    if (value instanceof ItemStorage) {
                        ((ItemStorage) value).writeNbt(nbt);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
