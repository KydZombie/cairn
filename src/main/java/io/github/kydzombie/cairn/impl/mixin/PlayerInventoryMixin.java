package io.github.kydzombie.cairn.impl.mixin;

import io.github.kydzombie.cairn.api.item.StatefulDamage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Shadow public abstract ItemStack getStack(int slot);

    @Shadow public int selectedSlot;

    @Inject(method = "getAttackDamage(Lnet/minecraft/entity/Entity;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getAttackDamage(Lnet/minecraft/entity/Entity;)I"), cancellable = true)
    private void a(Entity attackedEntity, CallbackInfoReturnable<Integer> cir) {
        ItemStack itemStack = getStack(selectedSlot);
        if (itemStack != null && itemStack.getItem() instanceof StatefulDamage damager) {
            cir.setReturnValue(damager.getAttackDamage(attackedEntity, itemStack, attackedEntity));
        }
    }
}
