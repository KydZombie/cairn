package io.github.kydzombie.cairn.impl.mixin;

import io.github.kydzombie.cairn.api.item.ItemBreakHandler;
import io.github.kydzombie.cairn.api.item.ItemBreakResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.stat.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public int count;
    @Shadow
    public int itemId;
    @Shadow
    private int damage;

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract void readNbt(NbtCompound nbt);

    @Unique
    private void cairn_transformStack(ItemBreakHandler item, ItemStack newStack) {
        if (newStack == null) {
            --this.count;
            if (this.count < 0) {
                this.count = 0;
            }

            this.damage = 0;
            return;
        }

        this.readNbt(newStack.writeNbt(new NbtCompound()));
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;increaseStat(Lnet/minecraft/stat/Stat;I)V"), cancellable = true)
    private void cairn_callOnBrokenByPlayer(int amount, Entity entity, CallbackInfo ci) {
        if (getItem() instanceof ItemBreakHandler item) {
            ItemBreakResult result = item.onItemBroken(ItemStack.class.cast(this), entity);
            cairn_transformStack(item, result.stack());
            if (result.broken()) {
                ((PlayerEntity) entity).increaseStat(Stats.BROKEN[itemId], 1);
            }
            ci.cancel();
        }
    }

    @Inject(method = "damage", at = @At(value = "FIELD", target = "Lnet/minecraft/item/ItemStack;count:I", ordinal = 0), cancellable = true)
    private void cairn_callOnBrokenByNonPlayer(int amount, Entity entity, CallbackInfo ci) {
        if (getItem() instanceof ItemBreakHandler item) {
            ItemBreakResult result = item.onItemBroken(ItemStack.class.cast(this), entity);
            cairn_transformStack(item, result.stack());
            ci.cancel();
        }
    }
}
