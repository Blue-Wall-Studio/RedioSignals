package org.BlueWallStudio.rediosignals.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.BlueWallStudio.rediosignals.RedioSignals;

public record DebugTogglePayload() implements CustomPayload {
    public static final Id<DebugTogglePayload> ID = new Id<>(Identifier.of(RedioSignals.MOD_ID, "debug_toggle"));

    // Package has no data - use unit instead
    public static final PacketCodec<PacketByteBuf, DebugTogglePayload> CODEC = PacketCodec
            .unit(new DebugTogglePayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
