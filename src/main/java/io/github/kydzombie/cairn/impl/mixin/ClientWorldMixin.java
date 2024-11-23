package io.github.kydzombie.cairn.impl.mixin;

import io.github.kydzombie.cairn.Cairn;
import io.github.kydzombie.cairn.api.block.entity.UpdatePacketReceiver;
import io.github.kydzombie.cairn.api.packet.BlockEntityUpdatePacket;
import io.github.kydzombie.cairn.api.packet.PacketDeserializer;
import io.github.kydzombie.cairn.api.packet.UpdatePacketHelper;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.ClientWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.WorldStorage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {
    public ClientWorldMixin(WorldStorage dimensionData, String name, Dimension dimension, long seed) {
        super(dimensionData, name, dimension, seed);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void cairn_updateBlockEntities(CallbackInfo ci) {
        try {
            @NotNull BlockEntityUpdatePacket<?> packet = BlockEntityUpdatePacket.pending.remove();
            BlockEntity blockEntity = getBlockEntity(packet.x, packet.y, packet.z);
            if (blockEntity == null) {
                Cairn.LOGGER.error("BlockEntityUpdatePacket was sent to {}, {}, {} but " +
                        "there is not a block entity there.", packet.x, packet.y, packet.z);
            } else //noinspection rawtypes
                if (blockEntity instanceof UpdatePacketReceiver receiver) {
                    for (Type type : blockEntity.getClass().getGenericInterfaces()) {
                        if (type instanceof ParameterizedType pType) {
                            if (pType.getRawType() != UpdatePacketReceiver.class) continue;
                            Type typeArg = pType.getActualTypeArguments()[0];
                            Class<?> clazz = Class.forName(typeArg.getTypeName());
                            for (Method method : clazz.getDeclaredMethods()) {
                                if (method.isAnnotationPresent(PacketDeserializer.class)) {
                                    method.setAccessible(true);
                                    Record data = (Record) method.invoke(null, packet.rawData);

                                    try {
                                        //noinspection unchecked
                                        receiver.receiveUpdateData(data);
                                    } catch (Exception ignored) {
                                        Cairn.LOGGER.error("Block entity at {}, {}, {} was sent the " +
                                                "wrong type of update data.", packet.x, packet.y, packet.z);
                                    }
                                    return;
                                }
                            }

                            try {
                                Record data = UpdatePacketHelper.autoDeserialize((Class<Record>) clazz, packet.rawData);
                                //noinspection unchecked
                                receiver.receiveUpdateData(data);
                            } catch (Exception e) {
                                Cairn.LOGGER.error("Auto deserializing failed on {}. " +
                                        "Consider implementing a @PacketDeserializer method", clazz.getName());
                            }
                        }
                    }
                } else {
                    Cairn.LOGGER.error("Block entity at {}, {}, {} does not implement " +
                            "UpdatePacketReceiver, but was sent a BlockEntityUpdatePacket.", packet.x, packet.y, packet.z);
                }
        } catch (Exception ignored) {
        }
    }
}
