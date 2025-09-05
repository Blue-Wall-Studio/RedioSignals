package org.BlueWallStudio.argest.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.BlueWallStudio.argest.debug.DebugManager;

public class NetworkHandler {
    public static void init() {
        // FIRST, Register package type
        PayloadTypeRegistry.playC2S().register(DebugTogglePayload.ID, DebugTogglePayload.CODEC);

        // THEN register handler
        ServerPlayNetworking.registerGlobalReceiver(DebugTogglePayload.ID, (payload, context) -> context.server()
                .execute(() -> DebugManager.getInstance().toggleDebug(context.player())));
    }
}
