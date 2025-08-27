package org.BlueWallStudio.argest.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.BlueWallStudio.argest.Argest;

public record DebugTogglePayload() implements CustomPayload {
    public static final Id<DebugTogglePayload> ID =
            new Id<>(Identifier.of(Argest.MOD_ID, "debug_toggle"));

    // раз у пакета нет данных – используем unit
    public static final PacketCodec<PacketByteBuf, DebugTogglePayload> CODEC =
            PacketCodec.unit(new DebugTogglePayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}