package io.github.kydzombie.cairntest;

import io.github.kydzombie.cairntest.block.entity.RenderTestBlockEntity;
import io.github.kydzombie.cairntest.block.entity.SimpleStorageBlockEntity;
import io.github.kydzombie.cairntest.client.RenderTestBlockEntityRenderer;
import io.github.kydzombie.cairntest.gui.ingame.SimpleStorageScreen;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.item.Item;
import net.modificationstation.stationapi.api.client.event.block.entity.BlockEntityRendererRegisterEvent;
import net.modificationstation.stationapi.api.client.event.texture.TextureRegisterEvent;
import net.modificationstation.stationapi.api.client.gui.screen.GuiHandler;
import net.modificationstation.stationapi.api.event.registry.GuiHandlerRegistryEvent;
import net.modificationstation.stationapi.api.registry.Registry;

public class CairnTestClient {
    @EventListener
    private void registerTextures(TextureRegisterEvent event) {
        CairnTest.glassSword.setTextureId(Item.IRON_SWORD.getTextureId(0));
    }

    @EventListener
    private void registerGuiHandlers(GuiHandlerRegistryEvent event) {
        Registry.register(
                event.registry,
                CairnTest.NAMESPACE.id("simple_storage"),
                new GuiHandler(
                        (player, inventory, messagePacket) ->
                                new SimpleStorageScreen(player.inventory, (SimpleStorageBlockEntity) inventory),
                        SimpleStorageBlockEntity::new
                )
        );
    }

    @EventListener
    private void registerBlockEntityRenderers(BlockEntityRendererRegisterEvent event) {
        event.renderers.put(RenderTestBlockEntity.class, new RenderTestBlockEntityRenderer());
    }
}
