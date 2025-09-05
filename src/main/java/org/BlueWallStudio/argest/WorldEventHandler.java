package org.BlueWallStudio.argest;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;
import org.BlueWallStudio.argest.signal.SignalManager;
import org.BlueWallStudio.argest.config.ModConfig;

public class WorldEventHandler {
    private static int tickCounter = 0;
    private static int INTERVAL = ModConfig.getInstance().signalProcessingDelay;

    public static void registerEvents() {
        // Регистрируем тик сервера для обработки пакетов
        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            tickCounter++;
            if (tickCounter >= INTERVAL) {
                for (ServerWorld world : server.getWorlds()) {
                    SignalManager.tick(world);
                }
                tickCounter = 0;
            }
        });
    }
}
