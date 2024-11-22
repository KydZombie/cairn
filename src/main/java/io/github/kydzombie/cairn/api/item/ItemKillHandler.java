package io.github.kydzombie.cairn.api.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface ItemKillHandler {
    void onKill(ItemStack stack, LivingEntity target, LivingEntity attacker);
}
