package io.github.kydzombie.cairn.api.packet;

import io.github.kydzombie.cairn.Cairn;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.NetworkHandler;
import net.minecraft.network.packet.Packet;
import net.modificationstation.stationapi.api.network.packet.ManagedPacket;
import net.modificationstation.stationapi.api.network.packet.PacketType;
import net.modificationstation.stationapi.api.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Queue;

public final class BlockEntityUpdatePacket<CustomData extends Record> extends Packet implements ManagedPacket<BlockEntityUpdatePacket<?>> {
    public static final PacketType<BlockEntityUpdatePacket<?>> TYPE = PacketType.builder(true, false, BlockEntityUpdatePacket::new).build();

    public static Queue<@NotNull BlockEntityUpdatePacket<?>> pending = new LinkedList<>();
    public int x;
    public int y;
    public int z;
    public byte[] rawData;
    private CustomData data;
    private int dataSize;

    public BlockEntityUpdatePacket() {
    }

    public BlockEntityUpdatePacket(int x, int y, int z, CustomData data) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.data = data;
    }

    @Override
    public void read(DataInputStream stream) {
        try {
            this.x = stream.readInt();
            this.y = stream.readInt();
            this.z = stream.readInt();

            try {
                dataSize = stream.readInt();
                rawData = stream.readNBytes(dataSize);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(DataOutputStream stream) {
        try {
            stream.writeInt(this.x);
            stream.writeInt(this.y);
            stream.writeInt(this.z);

            try {
                for (Method method : data.getClass().getDeclaredMethods()) {
                    if (method.isAnnotationPresent(PacketSerializer.class)) {
                        method.setAccessible(true);
                        byte[] bytes = (byte[]) method.invoke(null, data);
                        dataSize = bytes.length;
                        stream.writeInt(dataSize);
                        stream.write(bytes);
                        return;
                    }
                }

                try {
                    byte[] bytes = UpdatePacketHelper.autoSerialize(data);
                    dataSize = bytes.length;
                    stream.writeInt(dataSize);
                    stream.write(bytes);
                    return;
                } catch (Exception e) {
                    Cairn.LOGGER.error("Auto serializing failed on {}. " +
                            "Consider implementing a @PacketSerializer method", data.getClass().getName());
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            throw new RuntimeException("No PacketSerializer method found in " + data.getClass().getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void apply(NetworkHandler networkHandler) {
        if (!networkHandler.isServerSide()) {
            clientApply(networkHandler);
        }
    }

    @Environment(EnvType.CLIENT)
    private void clientApply(NetworkHandler networkHandler) {
        pending.add(this);
    }

    @Override
    public int size() {
        return 12 + 4 + dataSize;
    }

    @Override
    public @NotNull PacketType<BlockEntityUpdatePacket<?>> getType() {
        return TYPE;
    }
}
