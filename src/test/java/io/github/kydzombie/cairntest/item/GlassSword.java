package io.github.kydzombie.cairntest.item;

import io.github.kydzombie.cairn.api.item.ItemBreakHandler;
import io.github.kydzombie.cairn.api.item.ItemBreakResult;
import io.github.kydzombie.cairn.api.item.ItemKillHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.modificationstation.stationapi.api.template.item.TemplateSwordItem;
import net.modificationstation.stationapi.api.util.Identifier;

public class GlassSword extends TemplateSwordItem implements ItemBreakHandler, ItemKillHandler {
    public GlassSword(Identifier identifier, ToolMaterial arg) {
        super(identifier, arg);
        setTranslationKey(identifier);
        setMaxCount(1);
    }

    @Override
    public ItemBreakResult onItemBroken(ItemStack stack, Entity user) {
        return new ItemBreakResult(new ItemStack(Block.GLASS), true);
    }

    @Override
    public void onKill(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof PlayerEntity player) {
            player.inventory.addStack(new ItemStack(Block.GLASS));
        }
    }
}
