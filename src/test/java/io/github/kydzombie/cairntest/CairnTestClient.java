package io.github.kydzombie.cairntest;

import io.github.kydzombie.cairntest.block.entity.SimpleStorageBlockEntity;
import io.github.kydzombie.cairntest.gui.ingame.SimpleStorageScreen;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.item.Item;
import net.modificationstation.stationapi.api.client.event.texture.TextureRegisterEvent;
import net.modificationstation.stationapi.api.event.registry.GuiHandlerRegistryEvent;
import uk.co.benjiweber.expressions.tuple.BiTuple;

public class CairnTestClient {
    @EventListener
    private void registerTextures(TextureRegisterEvent event) {
        CairnTest.glassSword.setTextureId(Item.IRON_SWORD.getTextureId(0));
    }

    @EventListener
    private void registerGuiHandlers(GuiHandlerRegistryEvent event) {
        event.registry.registerValueNoMessage(CairnTest.NAMESPACE.id("simple_storage"), BiTuple.of(
                (player, inventory) -> new SimpleStorageScreen(player.inventory,
                        (SimpleStorageBlockEntity) inventory), SimpleStorageBlockEntity::new)
        );
    }
}
