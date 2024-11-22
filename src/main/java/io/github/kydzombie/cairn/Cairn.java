package io.github.kydzombie.cairn;

import io.github.kydzombie.cairn.api.packet.BlockEntityUpdatePacket;
import net.fabricmc.api.ModInitializer;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.event.network.packet.PacketRegisterEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.Entrypoint;
import net.modificationstation.stationapi.api.network.packet.IdentifiablePacket;
import net.modificationstation.stationapi.api.util.Namespace;
import net.modificationstation.stationapi.api.util.Null;
import org.apache.logging.log4j.Logger;

public class Cairn implements ModInitializer {
    @Entrypoint.Namespace
    public static final Namespace NAMESPACE = Null.get();
    @Entrypoint.Logger
    public static final Logger LOGGER = Null.get();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Cairn");
    }

    @EventListener
    private void registerPackets(PacketRegisterEvent event) {
        IdentifiablePacket.register(Cairn.NAMESPACE.id("block_entity_update"), true, false, BlockEntityUpdatePacket::new);
    }
}
