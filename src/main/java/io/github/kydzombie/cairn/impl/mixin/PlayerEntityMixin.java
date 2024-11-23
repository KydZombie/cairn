package io.github.kydzombie.cairn.impl.mixin;

import io.github.kydzombie.cairn.api.gui.TickableScreenHandler;
import io.github.kydzombie.cairn.api.item.ItemKillHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Shadow
    public ScreenHandler currentScreenHandler;

    @Shadow
    public abstract ItemStack getHand();

    @Inject(method = "tick", at = @At("HEAD"))
    private void cairn_tickScreenHandler(CallbackInfo info) {
        if (currentScreenHandler instanceof TickableScreenHandler handler) {
            handler.tick();
        }
    }

    @Inject(
            method = "attack(Lnet/minecraft/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;postHit(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/player/PlayerEntity;)V"
            )
    )
    private void cairn_injectKillMethod(Entity target, CallbackInfo ci) {
        if (getHand() != null && getHand().getItem() instanceof ItemKillHandler handler) {
            if (target instanceof LivingEntity entity) {
                if (!entity.isAlive()) {
                    handler.onKill(getHand(), entity, (PlayerEntity) (Object) this);
                }
            }
        }
    }
}
