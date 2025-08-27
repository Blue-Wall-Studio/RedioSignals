package org.BlueWallStudio.argest.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.BlueWallStudio.argest.debug.DebugManager;

public class NetworkHandler {
    public static void init() {
        // СНАЧАЛА регистрируем тип пакета
        PayloadTypeRegistry.playC2S().register(DebugTogglePayload.ID, DebugTogglePayload.CODEC);

        // ПОТОМ регистрируем обработчик
        ServerPlayNetworking.registerGlobalReceiver(DebugTogglePayload.ID, (payload, context) -> context.server().execute(() -> DebugManager.getInstance().toggleDebug(context.player())));
    }
}
