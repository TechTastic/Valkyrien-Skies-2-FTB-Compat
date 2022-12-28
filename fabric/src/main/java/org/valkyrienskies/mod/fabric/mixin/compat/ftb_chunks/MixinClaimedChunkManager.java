package org.valkyrienskies.mod.fabric.mixin.compat.ftb_chunks;

import dev.ftb.mods.ftbchunks.data.ClaimedChunk;
import dev.ftb.mods.ftbchunks.data.ClaimedChunkManager;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Pseudo
@Mixin(ClaimedChunkManager.class)
public abstract class MixinClaimedChunkManager {
    @Unique
    private Entity entity = null;

    @Shadow
    public abstract @Nullable ClaimedChunk getChunk(ChunkDimPos pos);

    @ModifyVariable(method = "protect", at = @At("HEAD"), name = "entity", remap = false)
    private Entity ValkyrienSkies$entity(final Entity entity) {
        this.entity = entity;
        return entity;
    }

    @Inject(method = "getChunk", at = @At("RETURN"), cancellable = true, remap = false)
    public void ValkyrienSkies$getChunk(final ChunkDimPos dimPos, final CallbackInfoReturnable<ClaimedChunk> cir) {
        if (entity instanceof ServerPlayer player) {
            final ChunkPos chunk = dimPos.getChunkPos();
            final BlockPos pos =
                new BlockPos(chunk.getMiddleBlockX(), chunk.getWorldPosition().getY(), chunk.getMiddleBlockZ());
            final Ship ship = VSGameUtilsKt.getShipManagingPos(player.level, pos);
            if (ship != null) {
                final Vector3d vec =
                    ship.getShipToWorld().transformPosition(new Vector3d(pos.getX(), pos.getY(), pos.getZ()));
                cir.setReturnValue(getChunk(new ChunkDimPos(player.level, new BlockPos(vec.x, vec.y, vec.z))));
            }
        }
    }
}
