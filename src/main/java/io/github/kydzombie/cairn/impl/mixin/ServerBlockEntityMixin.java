package io.github.kydzombie.cairn.impl.mixin;

import io.github.kydzombie.cairn.api.block.entity.UpdatePacketReceiver;
import io.github.kydzombie.cairn.api.packet.BlockEntityUpdatePacket;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public class ServerBlockEntityMixin {
    @Shadow public int x;
    @Shadow public int y;
    @Shadow public int z;

    @Inject(method = "createUpdatePacket", at = @At("HEAD"), cancellable = true)
    private void cairn_customUpdatePacket(CallbackInfoReturnable<Packet> cir) {
        if (this instanceof UpdatePacketReceiver<?> receiver) {
            cir.setReturnValue(new BlockEntityUpdatePacket<>(x, y, z, receiver.createUpdateData()));
        }
    }
}
