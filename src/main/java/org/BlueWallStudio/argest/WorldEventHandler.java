package org.BlueWallStudio.argest;

import net.minecraft.server.world.ServerWorld;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.BlueWallStudio.argest.config.ModConfig;
import org.BlueWallStudio.argest.packet.PacketManager;

public class WorldEventHandler {
    private static int tickCounter = 0;
    private static int INTERVAL = ModConfig.getInstance().packetProcessingDelay;

    public static void registerEvents() {
        // Register server world tick event to process packets every N ticks
        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            tickCounter++;
            if (tickCounter >= INTERVAL) {
                for (ServerWorld world : server.getWorlds()) {
                    PacketManager.tick(world);
                }
                tickCounter = 0;
            }
        });
    }

    public static void configReload() {
        INTERVAL = ModConfig.getInstance().packetProcessingDelay;
    }
}
