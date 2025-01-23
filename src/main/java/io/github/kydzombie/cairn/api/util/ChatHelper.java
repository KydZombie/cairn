package io.github.kydzombie.cairn.api.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.play.ChatMessagePacket;
import net.modificationstation.stationapi.api.network.packet.PacketHelper;

public class ChatHelper {
    public static void sendChatMessageTo(PlayerEntity receiver, String message) {
        if (receiver.world.isRemote) return;

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ((Minecraft) FabricLoader.getInstance().getGameInstance()).inGameHud.addChatMessage(message);
        } else {
            PacketHelper.sendTo(receiver, new ChatMessagePacket(message));
        }
    }
}
