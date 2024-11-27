package io.github.kydzombie.cairn.api.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public interface StatefulDamage {
    int getAttackDamage(Entity wielder, ItemStack stack, Entity attackedEntity);
}
