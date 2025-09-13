package org.BlueWallStudio.rediosignals.wire.types;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.BlueWallStudio.rediosignals.ModTags;
import org.BlueWallStudio.rediosignals.packet.Packet;

/**
 * Bulb on copper wire. Re-uses copper logic. Changes LIT state at each package
 * receive, if NOT powered by redstone
 */
public class CopperBulbWireType extends CopperWireType {

    @Override
    public boolean canHandle(BlockState blockState) {
        return blockState.isIn(ModTags.COPPER_BULB_WIRES);
    }

    @Override
    public boolean processPacket(World world, BlockPos pos, Packet packet) {
        BlockState currentState = world.getBlockState(pos);

        // Check if lamp is powered by external redstone
        boolean isPowered = world.isReceivingRedstonePower(pos);

        if (!isPowered) {
            // Take current LIT value (assuming BulbBlock has LIT property)
            boolean currentLit = currentState.get(net.minecraft.block.BulbBlock.LIT);
            world.setBlockState(pos, currentState.with(net.minecraft.block.BulbBlock.LIT, !currentLit));
        }

        // Continue transmission as a regular wire
        return true;
    }

    // Don't redefine getExitDirections - use CopperWireType behaviour
}
